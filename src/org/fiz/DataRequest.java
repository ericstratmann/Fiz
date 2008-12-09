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
 *   - By convention there is a {@code request} value in the dataset,
 *     which specifies the operation to be performed.
 *   - Other values in the dataset provide parameters as needed by the
 *     particular operation; each data manager defines its own conventions
 *     for the values it expects.
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
 *     is separated from the code and kept in a template dataset;  the
 *     code that invokes a request can refer to the request by a single
 *     string identifier.
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
    /**
     * RequestError is used to abort execution when an unrecoverable
     * error occurs in a DataRequest.
     */
    public static class RequestError extends Error {
        /**
         * Constructs a RequestError based on information in a failed
         * DataRequest.  Note: don't use this constructor directly;
         * invoke DataRequest.throwError instead.
         * @param message            Message describing the failure.
         */
        protected RequestError(String message) {
            super(message);
        }
    }

    // DataManager that will service the request.  This value isn't computed
    // until it is actually needed (e.g., in startRequests).  This makes
    // life a bit easier for tests (they don't have to set up data manager
    // configuration information in many cases).
    protected DataManager dataManager = null;

    // Information passed to dataManager, which specifies the request.
    // This dataset can have any of the following forms:
    // * An explicit dataset passed in to the constructor, in which case
    //   we must treat it as read-only.
    // * A dataset derived from a template; since this dataset was generated
    //   by us, we can modify it to add parameters.
    // * A CompoundDataset containing 2 children: extraParams and the
    //   read-only dataset passed in to the constructor.  This happens when
    //   addParameter is called and {@code request} is read-only.
    protected Dataset request;

    // A dataset into which additional parameters can be added by
    // addParameter.  Null means {@code request} is read-only and
    // addParameter has not yet been called.
    protected Dataset extraParams = null;

    // Result information returned to us by dataManager.  Null means
    // the request has not completed or it returned an error.
    protected Dataset response = null;

    // If setError has been called, this holds information about all of
    // the errors.  Null means setError has not been called.
    protected Dataset[] errorDatasets = null;

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
     *                             the query values in the URL for the current
     *                             page.
     */
    public DataRequest(String name, Dataset aux) {
        request = Config.getDataset("dataRequests").getChildPath(
                name).expand(aux);
        extraParams = request;
    }

    /**
     * Add a parameter to an existing request, overriding any previous
     * value for that parameter.
     * @param name                 Name for the parameter.
     * @param value                Value for the parameter.
     */
    public void addParameter(String name, String value) {
        // If the current request is read-only then turn the request
        // into a CompoundDataset containing a new dataset (for additional
        // parameters), plus the original read-only dataset.
        if (extraParams == null) {
            extraParams = new Dataset();
            request = new CompoundDataset(extraParams, request);
        }
        extraParams.set(name, value);
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
     * has been completed successfully.  Either this method or
     * {@code setError} should be invoked by the data manager exactly once
     * for each request.
     * @param response             Result information provided by the data
     *                             manager.
     */
    public synchronized void setComplete(Dataset response) {
        this.response = response;
        completed = true;
        notifyAll();
    }

    /**
     * This method is invoked by data managers to indicate that a request
     * has been completed successfully and should return an empty Dataset
     * as result.
     */
    public synchronized void setComplete() {
        this.response = new Dataset();
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
     * Wait for the request to complete (starting it if necessary) and either
     * return its response (if it completed successfully) or throw a
     * RequestError if it failed.
     * @return                     A dataset containing the results of the
     *                             request.
     */
    public Dataset getResponseOrAbort() {
        Dataset response = getResponseData();
        if (response == null) {
            throwError();
        }
        return response;
    }

    /**
     * This method is invoked by data managers to indicate that one or more
     * errors occurred while processing the request.  It also marks the
     * request as complete and supplies a dataset containing information
     * about each error.  This method should be invoked at most once for
     * each request and should not be invoked if {@code setComplete} has
     * been invoked.  The following values in the dataset(s) have
     * well-defined interpretation and usage, so they should be included
     * whenever appropriate.  DataManagers may also provide additional
     * values of their own choosing.
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
     * @param errorInfo            One or more datasets, each containing
     *                             information about one error that occurred
     *                             while processing the request.
     */
    public synchronized void setError(Dataset... errorInfo) {
        errorDatasets = errorInfo;
        completed = true;
        notifyAll();
    }

    /**
     * Marks the request as having failed, and records detailed information
     * about the error(s) that occurred.
     * @param errorInfo            One or more datasets, each containing
     *                             information about one error that occurred
     *                             while processing the request.
     */
    public synchronized void setError(ArrayList<Dataset> errorInfo) {
        Dataset[] datasetArray;
        int length = errorInfo.size();
        datasetArray = new Dataset[length];
        for (int i = 0; i < length; i++) {
            datasetArray[i] = errorInfo.get(i);
        }
        setError(datasetArray);
    }

    /**
     * Returns detailed information about the error(s) that occurred during
     * this request, if there were any.  If the request hasn't yet completed
     * this method first waits for the request to complete (if the request
     * hasn't yet started then this method will start it also).
     * @return                     An array of datasets; each dataset
     *                             contains elements describing one error.
     *                             If the request completed successfully,
     *                             null is returned.
     */
    public synchronized Dataset[] getErrorData() {
        if (!completed) {
            getResponseData();
        }
        return errorDatasets;
    }

    /**
     * Returns a human-readable message describing the errors that occurred in
     * this request, if there were any.  If the request hasn't yet completed
     * this method first waits for the request to complete (if the request
     * hasn't yet started then this method will start it also).
     * @return                     If the request completed successfully then
     *                             null is returned.  Otherwise the return
     *                             value is a description of the problem(s),
     *                             intended for presentation to users (as
     *                             opposed to system maintainers).
     */
    public String getErrorMessage() {
        if (!completed) {
            getResponseData();
        }
        if (errorDatasets == null) {
            return null;
        }
        return StringUtil.errorMessage(errorDatasets);
    }

    /**
     * Returns a message containing all of the information available
     * for the error(s) that occurred in this request; intended for logs
     * or for examination by developers to track down problems.  If the
     * request hasn't yet completed this method first waits for the request
     * to complete (if the request hasn't yet started then this method
     * will start it also).
     * @return                     If the request completed successfully, null
     *                             is returned.  Otherwise the return value
     *                             is a string containing all of the
     *                             information available for the error(s)
     *                             that have been recorded for the request.
     */
    public String getDetailedErrorMessage() {
        if (!completed) {
            getResponseData();
        }
        if (errorDatasets == null) {
            return null;
        }
        return StringUtil.detailedErrorMessage(errorDatasets);
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
     * Throws a RequestError exception containing information about the
     * failure in this request.  This method should only be invoked if
     * the request has failed.
     */
    public void throwError() {
        String managerName = request.get("manager");
        String requestName = request.check("request");
        if (requestName == null) {
            requestName = "??";
        }
        throw new RequestError("error in DataRequest " + managerName +
                ":" + requestName + ": " + getDetailedErrorMessage());
    }
}
