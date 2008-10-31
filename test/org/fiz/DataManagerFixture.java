package org.fiz;
import java.util.*;
import org.apache.log4j.*;

/**
 * DataManagerFixture defines a simple DataManager for use in tests.
 */

public class DataManagerFixture extends DataManager {
    // The following variable maintains a log of methods that have been
    // invoked on each DataManagerFixture object, so that tests can check
    // to see that methods really were invoked.  The logs are kept
    // separately for each data manager and then merged by the getLogs
    // method; this is needed to generate predictable test results, because
    // the order in which data manager start methods are invoked is
    // unpredictable; by sorting the merged log according to data manager
    // name we enforce a canonical order.
    public static HashMap<String,StringBuilder> logs =
            new HashMap<String,StringBuilder>();

    // The following variable holds the input dataset from the most
    // recent request that was started.
    public static Dataset requestData = null;

    // If the following variable is non-null, it will be used as the response
    // for the next request that is started.
    public static Dataset responseData = null;

    // If the following variable is non-null, then an error response will
    // be generated for the next request that is started, with this
    // variable providing the error data.
    public static Dataset[] errorDatasets = null;

    public Dataset constructorArgs = null;
    public boolean destroyed = false;
    public DataManagerFixture(Dataset constructorArgs) {
        this.constructorArgs = constructorArgs;
    }
    public void startRequests(Collection<DataRequest> requests) {
        StringBuilder log = getLog();
        String prefix = ((log.length() > 0) ? "; " : "") + name + " started ";
        for (DataRequest request : requests) {
            log.append(prefix);
            log.append(getId(request));
            prefix = ", ";
            requestData = request.getRequestData();
            if (responseData != null) {
                request.setComplete(responseData);
                responseData = null;
            } else if (errorDatasets != null) {
                request.setError(errorDatasets);
                errorDatasets = null;
            }
        }
    }
    public void cancelRequest(DataRequest request) {
        StringBuilder log = getLog();
        if (log.length() > 0) {
            log.append("; ");
        }
        log.append(name + " canceled " + getId(request));
    }

    public void clearCache() {
        StringBuilder log = getLog();
        if (log.length() > 0) {
            log.append("; ");
        }
        log.append(name + " clearCache");
    }

    public void destroy() {
        destroyed = true;
    }

    // Invoke the following method in "setUp" methods for tests to
    // create configuration information that defines a data manager
    // named "testManager" implemented by this class.  This method
    // will overwrite the "dataManagers" configuration dataset
    // and delete any existing data managers.
    public static void init() {
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "testManager:\n" +
                "  class: org.fiz.DataManagerFixture\n" +
                "t2:\n" +
                "  class: org.fiz.DataManagerFixture\n"));
        DataManager.destroyAll();
        clearLogs();
        logger.setLevel(Level.ERROR);
    }

    protected StringBuilder getLog() {
        StringBuilder log = logs.get(name);
        if (log == null) {
            log = new StringBuilder();
            logs.put(name, log);
        }
        return log;
    }

    protected String getId(DataRequest request) {
        String id = request.getRequestData().check("id");
        if (id != null) {
            return id;
        }
        return "??";
    }

    /**
     * Arrange for the next request to return an error, with the argument
     * providing information about the error(s) to return.
     * @param datasets             One or more datasets, which will be
     *                             returned as an error response to the
     *                             next request made by this data manager.
     */
    public static void setErrorData(Dataset... datasets) {
        errorDatasets = datasets;
    }

    // This method is invoked by tests to retrieve all of the accumulated
    // log information for all of the DataManagerFixtures, collated by
    // data manager name.  The collation is done to ensure that the logs
    // come out in the same order even if data managers are invoked in
    // varying orders (which can happen in DatedRequest.start).  Entries
    // for each data manager are in the order the operations occurred.
    public static String getLogs() {
        StringBuilder result = new StringBuilder();
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(logs.keySet());
        Collections.sort(names);
        for (String name : names) {
            StringBuilder log = logs.get(name);
            if (result.length() != 0) {
                result.append("; ");
            }
            result.append(log);
        }
        return result.toString();
    }

    // Clear out all the information in any existing logs.
    public static void clearLogs() {
        logs.clear();
    }
}
