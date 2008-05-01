package org.fiz;
import java.util.*;

/**
 * A DataRequest represents an operation to be performed (eventually) by a
 * data manager.  The DataRequest stores the outbound request as well as
 * the response that is eventually returned by the data manager.  The outbound
 * request is specified with a dataset and the response consists either of a
 * dataset containing result information or an error dataset describing why
 * the request failed.
 * <p>
 * The following conventions are used for the request dataset:
 *   - There must be a {@code manager} value in the dataset, which specifies
 *     the data manager that will perform the request.  The {@code manager}
 *     value names a nested dataset within the {@code DataManagers}
 *     configuration dataset, which selects the data manager and also
 *     provides additional properties used by that data manager.
 *   - Other values in the dataset specify the operation to be performed
 *     and any parameters needed by that operation; each data manager
 *     defines its own conventions for the values it expects.
 *   - By convention, a request may contain extraneous values not needed
 *     by the data manager.  If this happens, the data manager will ignore
 *     the extra values.
 *<p>
 * In most cases the dataset for the outbound request is generated from
 * a template:
 *   - A "request name" is passed to the DataRequest constructor.
 *   - The request name selects a template dataset, which describes the
 *     structure of the request dataset but does not contain all of the
 *     actual argument values.
 *   - The names and values from the template are copied to the request
 *     dataset, except that in some cases the values are replaced with data
 *     from an auxiliary dataset; this substitution is controlled by the
 *     template (see below for details).
 *   - The auxiliary dataset is provided by the method that invokes the
 *     DataRequest constructor; typically it is the main dataset for the
 *     ClientRequest, containing query values and other global information.
 *     Not all of the values in the auxiliary dataset will necessarily
 *     be used in the DataRequest.
 * The template approach has several advantages:
 *   - Detailed information about the parameters required for each request
 *    is separated from the code and kept in a template dataset;  the
 *    code that invokes a request can refer to the request by a single
 *    string identifier.
 *   - If a request is invoked in multiple places, its detailed
 *     specification still exists only once, in the template dataset.
 *   - It may be possible to generate the template dataset automatically
 *     from information about the data manager.
 *<p>
 * Request templates are expanded as follows.  For each
 * <i>name</i>,<i>value</i> pair in the template, an identical
 * <i>name</i>,<i>value</i> pair is created in the request dataset
 * except for the following special cases:
 *   - If <i>value</i> starts with {@code @} then the remainder of
 *     the value is used as the name of an entry in the auxiliary dataset;
 *     the value from the auxiliary dataset is used for the request dataset
 *     in place of <i>value</i>.
 *   - If <i>value</i> starts with {@code @@} then it is not treated as the
 *     name of an auxiliary value; <i>value</i> is passed through to the
 *     request dataset, except that the two {@code @} characters are collapsed
 *     into a single {@code @}.
 * Thus, template values starting with {@code @} are used for request
 * arguments that vary from request to request, while values that don't start
 * with {@code @} are used for values that are the same every time this
 * request is invoked.
 * <p>
 * There are no particular requirements for response datasets; data
 * managers can define the return values in any way they wish.  Response
 * datasets should be treated as read-only; they may refer to cached
 * information, so modifying a response could have unintended side-effects.
 * <p>
 * If an error occurs in processing a request the data manager provides
 * a dataset containing information about the error.  Although the error
 * dataset can contain arbitrary values, there are a few conventions
 * that data managers should follow in order to simplify error reporting;
 * see the documentation for {@code setError} fwr details.
 */
public class DataRequest {
    // DataManager that will service the request.  This value isn't chmputed
    // until it is actually needed (e.g., in startRequests).  This makes
    // life a bit easier for tests (they don't have to set up data manager
    // configuration information in many cases).
    protected DataManager dataManager = null;

    // Information passed to dataManager, which specifies the request:
    protected Dataset request;

    // Result information returned to us by dataManager.  Null means
    // the request has not completed or it returned an error.
    protected Dataset response = null;

    // If setError has been called, this holds the information about the
    // error.  Null means setError has not been called.
    protected Dataset error = null;

    // Indicates the state of the request:
    protected boolean started = false;
    protected boolean completed = false;

    /**
     * Construct a DataRequest object where the arguments are supplied in
     * a dataset.  The request will retain a pointer to that dataset for
     * use while serving the request, so the caller should not modify
     * {@code args} until the request has been completed.
     * @param args                 The names and values in this dataset
     *                             specify the data manager to handle the
     *                             request and the operation to be
     *                             performed.  {@code args} must contain a
     *                             {@code manager} value specifying the
     *                             data manager to serve the request.
     */
    public DataRequest(Dataset args) {
        request = args;
    }

    /**
     * Construct a DataRequest object using a template.
     * @param name                 Symbolic name for the request; used to
     *                             locate a template in the
     *                             {@code dataRequests} configuration dataset.
     * @param aux                  Auxiliary data, some of which may be used
     *                             in the request.  This usually consists of
     *                             the query values in the URI for the current
     *                             page.
     */
    public DataRequest(String name, Dataset aux) {
        makeRequest(name, aux);
    }

    /**
     * Construct a DataRequest object using a template.  This form of the
     * constructor overrides the {@code manager} request property.
     * @param name                 Symbolic name for the request; used to
     *                             locate a template in the
     *                             {@code dataRequests} configuration dataset.
     * @param aux                  Auxiliary data, some of which may be used
     *                             in the request.
     * @param manager              If non-null, gives the name of the data
     *                             manager to use for this request, overriding
     *                             any {@code manager} value from the template.
     */
    public DataRequest(String name, Dataset aux, String manager) {
        makeRequest(name, aux);
        if (manager != null) {
            request.set("manager", manager);
        }
    }

    /**
     * Notify the data manager for this request that it should begin
     * processing the request.
     */
    public void start() {
        if (started) {
            return;
        }
        started = true;
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>(1);
        requests.add(this);
        getDataManager().startRequests(requests);
    }

    /**
     * Start a group of requests at the same time.  This method is
     * clever enough to group the requests by data manager so that
     * data managers can handle each group as a batch.
     * @param requests             Any number of DataRequests, all of which
     *                             are to be started.
     */
    public static void start(Collection<DataRequest> requests) {
        // Two issues to deal with in this method:
        //   - We have to separate the requests into a collection for each
        //     DataManager.
        //   - We must not start a request if it has already been started.

        ArrayList<DataRequest> currentList;
        HashMap<DataManager,ArrayList<DataRequest>> map =
                new HashMap<DataManager,ArrayList<DataRequest>>();
        for (DataRequest request : requests) {
            if (request.started) {
                continue;
            }
            request.started = true;
            DataManager manager = request.getDataManager();
            currentList = map.get(manager);
            if (currentList == null) {
                currentList = new ArrayList<DataRequest>(5);
                map.put(manager, currentList);
            }
            currentList.add(request);
        }
        for (ArrayList<DataRequest> collection : map.values()) {
            collection.get(0).getDataManager().startRequests(collection);
        }
    }

    /**
     * Ask the data manager for this request to abort the request.  If
     * the request hasn't been started, or if it has already completed,
     * or if the data manager doesn't support request cancellation, then
     * this method has no effect.
     */
    public void cancel() {
        if (started && !completed) {
            dataManager.cancelRequest(this);
        }
    }

    /**
     * This method is invoked by data managers to indicate that a request
     * has been completed successfully.
     * @param response             Result information provided by the data
     *                             manager.
     */
    public synchronized void setComplete(Dataset response) {
        this.response = response;
        completed = true;
        notifyAll();
    }

    /**
     * Returns the response from the request. If the request hasn't yet
     * completed, this method will wait for the request to complete before
     * returning.  If the request hasn't yet started, this method will
     * first start the request, then wait for it to complete.
     * @return                     A dataset containing the results of the
     *                             request.  If an error occurred then
     *                             the return value will be null.
     */
    public synchronized Dataset getResponseData() {
        if (!started && !completed) {
            start();
        }
        while (!completed) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                // Ignore interruptions.
            }
        }
        return response;
    }

    /**
     * This method is invoked by data managers to indicate that an error
     * occurred while processing the request.  It also marks the request as
     * complete and supplies a dataset containing information about the
     * error. The following values in the dataset have well-defined
     * interpretation and usage, so they should be included whenever
     * possible.  DataManagers may also provide additional values of
     * their own choosing.
     * code -              A succinct name for the error, typically useful
     *                     for debugging but not particularly meaningful
     *                     to an ordinary user.
     * message -           A human-readable message describing the error,
     *                     intended for presentation to non-wizards.  Err on
     *                     the side of including too much information here
     *                     rather than too little (e.g., if a file can't be
     *                     found, include the name of the file and the
     *                     context in which the file was being opened).
     * details -           Additional information about the error that might
     *                     help during debugging, but probably isn't
     *                     meaningful to a user (e.g., this might include
     *                     a stack trace).
     * culprit -           If the error occurred because a particular value
     *                     in the request was invalid, this gives the name
     *                     of the invalid value.  If the invalid value was
     *                     nested in the request dataset, this will be a path
     *                     with elements separated by dots.
     * @param errorInfo            Dataset containing information about the
     *                             error.
     */
    public synchronized void setError(Dataset errorInfo) {
        error = errorInfo;
        completed = true;
        notifyAll();
    }

    /**
     * Returns detailed information about the error that occurred during
     * this request, if there was one.
     * @return                     A dataset whose elements describe the
     *                             error.  If the request hasn't completed,
     *                             or if it completed successfully, null
     *                             is returned.
     */
    public synchronized Dataset getErrorData() {
        return error;
    }

    /**
     * Returns a human-readable message describing the error that occurred in
     * this request, if there was one.
     * @return                     If the request hasn't completed, or if it
     *                             completed successfully, then null is
     *                             returned.  Otherwise the return value is a
     *                             description of the problem, intended for
     *                             presentation to users (as opposed to
     *                             system maintainers).
     */
    public String getErrorMessage() {
        if (error == null) {
            return null;
        }

        // Ideally, there is a "message" value in the error; if so, it is
        // just what we're looking for, so return it.
        String message = error.check("message");
        if (message != null) {
            return message;
        }

        // Next choice is a "code" value in the error; this is pretty terse,
        // but better than nothing.
        message = error.check("code");
        if (message != null) {
            return "error " + message;
        }

        // No reasonable information is available.
        return "unknown error";
    }

    /**
     * Returns a message containing all of the information available
     * for this error; intended for logs, or for examination by developers
     * to track down problems.
     * @return                     If the request hasn't completed, or if it
     *                             completed successfully, then null is
     *                             returned.  Otherwise the return value is a
     *                             string containing all of the information
     *                             available for the error.
     */
    public String getDetailedErrorMessage() {
        if (error == null) {
            return null;
        }

        // First compute a header line containing the message, if there
        // is one.
        StringBuilder result = new StringBuilder();
        String message = error.check("message");
        if (message != null) {
            result.append(message);
        } else {
            result.append("error");
        }

        // Add all of the values in the error dataset to the message,
        // with "code" and "details" at the top.  If any of the values
        // include newline characters, add indentation to the additional
        // lines.
        String prefix = ":\n  ";
        String prefix2 = "\n  ";
        String indent = "\n               ";
        String code = error.check("code");
        if (code != null) {
            result.append(prefix);
            result.append(String.format("%-12s %s", "code:",
                    code.replace("\n", indent)));
            prefix = prefix2;
        }
        String details = error.check("details");
        if (details != null) {
            result.append(prefix);
            result.append(String.format("%-12s %s", "details:",
                    details.replace("\n", indent)));
            prefix = prefix2;
        }
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(error.keySet());
        Collections.sort(names);
        for (Object nameObject : names) {
            String name = (String) nameObject;
            if (name.equals("code") || name.equals("details")
                    || name.equals("message")) {
                continue;
            }
            Object value = error.lookup(name, Dataset.DesiredType.ANYTHING);
            if (!(value instanceof String)) {
                continue;
            }
            result.append(prefix);
            result.append(String.format("%-12s %s", (name + ":"),
                    ((String) value).replace("\n", indent)));
            prefix = prefix2;
        }
        return result.toString();
    }

    /**
     * Returns the DataManager associated with the request.
     * @return                     DataManager responsible for handling the
     *                             request.
     */
    public DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = DataManager.getDataManager(request.get("manager"));
        }
        return dataManager;
    }

    /**
     * Returns the dataset that specifies the arguments for this request.
     * @return                     Dataset whose values specify the
     *                             operation that the data manager will
     *                             perform.
     */
    public Dataset getRequestData() {
        return request;
    }

    /**
     * Compute the input data for the request by substituting values into
     * a template dataset.
     * @param name                 Symbolic name for the request; used to
     *                             locate a template in the
     *                             {@code dataRequests} configuration dataset.
     * @param aux                  Auxiliary data, some of which may be used
     *                             in the request.
     */
    protected void makeRequest(String name, Dataset aux) {
        request = new Dataset();
        Dataset config = Config.getDataset("dataRequests");
        Dataset template = config.getChild(name);
        for (String key : template.keySet()) {
            String value = template.get(key);
            if ((value.length() < 1) || (value.charAt(0) != '@')) {
                // Choice #1: template value doesn't start with '@'; copy
                // the template name and value through to the request dataset.
                request.set(key, value);
            } else if ((value.length() >= 2) && (value.charAt(1) == '@')) {
                // Choice #2: template value starts with '@@'; convert
                // the '@@' to '@' and copy through to the request dataset.
                request.set(key, value.substring(1));
            } else {
                // Choice #3: template value starts with '@': use the value
                // (excluding the '@') as the name of a value and the values
                // dataset and copy that through to the request dataset.
                request.set(key, aux.get(value.substring(1)));
            }
        }
    }
}
