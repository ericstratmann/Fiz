package org.fiz;
import java.util.*;

/**
 * FileDataManager allows files containing datasets to be accessed using
 * the DataManager/DataRequest mechanism.  It is intended primarily for
 * testing (to eliminate the need for a "real" data manager) but may have
 * other uses also.
 * <p>
 * The configuration dataset for a FileDataManager contains the
 * following values:
 *   class:            (required) The name of this class.
 *   pathTemplate:     (required) Template that is expanded in the context
 *                     of the main configuration dataset to produce a list
 *                     of directories (separated by commas) that will be
 *                     searched for dataset files specified in requests.
 *                     Typically "@home" is used in the path to generate
 *                     directory names that are descendents of the run-time
 *                     home directory.
 * <p>
 * The following fields are defined for requests:
 *   request:  (required) Specifies the operation to be performed; must be
 *             one of the following:
 *             create:   Create a new dataset at the location specified
 *                       by {@code file} and {@code dataset}, overwriting
 *                       any previously existing dataset, if there was one.
 *                       Returns nothing.
 *             read:     Returns the target dataset.
 *             update:   For each value in {@code values} replace the
 *                       corresponding value in the target dataset, or create
 *                       a new value in the target dataset if there was not
 *                       one previously.  The target dataset must already
 *                       exist.  Returns nothing.
 *             delete:   Delete the target dataset.  Returns nothing.  If
 *                       {@code data} is omitted this request removes all
 *                       elements in the top-level dataset but retains the
 *                       dataset and file.
 *             error:    Generates an error, using the target dataset
 *                       as the error information to return.  If the target
 *                       dataset is a list of datasets, then all of them
 *                       are returned as errors.
 *   file:     (required) Name of the file containing the desired dataset.
 *             If the file already exists then it must contain a
 *             dataset in a supported format.  The file name is looked up
 *             using the search path of directories defined by the
 *             {@code path} entry in the configuration dataset.
 *   dataset:  (optional) Specifies the path of the target dataset within
 *             the file.  If omitted, the request will operate on the
 *             top-level dataset (i.e., the entire file).
 *   values:   For {@code create} and {@code update} requests this
 *             is a nested dataset containing values to replace those
 *             in the target dataset.  Ignored for other requests.
 * <p>
 * Note: this class is thread-safe: it is safe for multiple threads to
 * issue concurrent requests for the same file.
 */

public class FileDataManager extends DataManager {
    /**
     * RequestAbortedError is thrown when an area in a request has
     * been detected and request.setError has already been called; all
     * that remains is to unwind our handling of the request.
     */
    protected static class RequestAbortedError extends Error {
        /**
         * Construct a RequestAbortedError.
         */
        public RequestAbortedError() {
            super();
        }
    }

    // The following object is used to locate datasets.
    protected String[] path;

    // The following hash table maps from the {@code file} parameter in
    // a request to the main-memory-cached copy of the corresponding dataset.
    protected HashMap<String,Dataset> datasetCache
            = new HashMap<String,Dataset>();

    /**
     * Construct a FileDataManager using a dataset containing configuration
     * parameters.
     * @param config               Parameters for this data manager;
     *                             see top-level class documentation
     *                             for supported values.
     */
    public FileDataManager(Dataset config) {
        StringBuilder expandedPath = new StringBuilder();
        Template.expand(config.get("pathTemplate"), Config.getDataset("main"),
                expandedPath, Template.SpecialChars.NONE);
        path = StringUtil.split(expandedPath.toString(), ',');
    }

    /**
     * This method is invoked by DataRequest.startRequests to process
     * one or more requests for this data manager.  The requests are
     * processed synchronously, so that they are all complete before
     * this method returns.
     * @param requests             DataRequest objects describing the
     *                             requests to be processed.
     */
    @Override
    public synchronized void startRequests(Collection<DataRequest> requests) {
        for (DataRequest request : requests) {
            Dataset parameters = request.getRequestData();
            String operation = null;
            try {
                operation = parameters.get("request");
                if (operation.equals("create")) {
                    createOperation(request, parameters);
                } else if (operation.equals("read")) {
                    readOperation(request, parameters);
                } else if (operation.equals("update")) {
                    updateOperation(request, parameters);
                } else if (operation.equals("delete")) {
                    deleteOperation(request, parameters);
                } else if (operation.equals("error")) {
                    errorOperation(request, parameters);
                } else {
                    request.setError(new Dataset("message",
                            "unknown request \"" + operation +
                            "\" for FileDataManager; must be create, " +
                            "read, update, delete, or error"));
                }
            }
            catch (Dataset.MissingValueError e) {
                request.setError(new Dataset("message",
                        "FileDataManager " +
                        ((operation != null) ? ("\"" + operation + "\" ") : "") +
                        "request didn't contain required " +
                        "parameter \"" + e.getMissingKey() + "\""));
            }
            catch (RequestAbortedError e) {
                // Nothing to do here; error information has already
                // been recorded.
            }
            catch (Error e) {
                request.setError(new Dataset("message",
                        "internal error in FileDataManager " +
                        ((operation != null) ? ("\"" + operation + "\" ") : "") +
                        "request: " + StringUtil.lcFirst(e.getMessage())));
            }
        }
    }

    /**
     * This method implements the "create" request.  Upon return the request
     * has been completed with either a response or an error.
     * @param request              The request being serviced.
     * @param parameters           The input dataset for the request.
     */
    protected void createOperation(DataRequest request, Dataset parameters) {
        Dataset root = loadDataset(parameters.get("file"));
        Dataset target = root;
        String path = parameters.check("dataset");
        if (path != null) {
            target = root.createChildPath(path);
        }
        target.clear();
        target.copyFrom(parameters.getChild("values"));
        root.writeFile(root.getFileName(), null);
        request.setComplete(new Dataset());
    }

    /**
     * This method implements the "error" request.  Upon return the request
     * has been completed with either an error.
     * @param request              The request being serviced.
     * @param parameters           The input dataset for the request.
     */
    protected void errorOperation(DataRequest request, Dataset parameters) {
        Dataset d = loadDataset(parameters.get("file"));

        String path = parameters.check("dataset");
        if ((path == null) || (path.length() == 0)) {
            request.setError(d);
            return;
        }
        ArrayList<Dataset> errors = d.getChildrenPath(path);
        if (errors.size() == 0) {
            request.setError (new Dataset("message", "nested dataset \"" +
                    path + "\" doesn't exist", "culprit", "dataset"));
            throw new RequestAbortedError();
        }
        request.setError(errors);
    }

    /**
     * This method implements the "read" request.  Upon return the request
     * has been completed with either a response or an error.
     * @param request              The request being serviced.
     * @param parameters           The input dataset for the request.
     */
    protected void readOperation(DataRequest request, Dataset parameters) {
        Dataset d = loadDataset(parameters.get("file"));
        Dataset target = findNestedDataset(d, parameters.check("dataset"),
                request);
        request.setComplete(target);
    }

    /**
     * This method implements the "update" request.  Upon return the request
     * has been completed with either a response or an error.
     * @param request              The request being serviced.
     * @param parameters           The input dataset for the request.
     */
    protected void updateOperation(DataRequest request, Dataset parameters) {
        Dataset root = loadDataset(parameters.get("file"));
        Dataset target = findNestedDataset(root, parameters.check("dataset"),
                request);
        target.copyFrom(parameters.getChild("values"));
        root.writeFile(root.getFileName(), null);
        request.setComplete(new Dataset());
    }

    /**
     * This method implements the "delete" request.  Upon return the request
     * has been completed with either a response or an error.
     * @param request              The request being serviced.
     * @param parameters           The input dataset for the request.
     */
    protected void deleteOperation(DataRequest request, Dataset parameters) {
        Dataset d = loadDataset(parameters.get("file"));
        String path = parameters.check("dataset");
        if (path != null) {
            d.deletePath(path);
        } else {
            d.clear();
        }
        d.writeFile(d.getFileName(), null);
        request.setComplete(new Dataset());
    }

    /**
     * Given the name of a file containing a dataset, load the dataset
     * into the main-memory cache (if it isn't there already) and return it.
     * @param fileName             Name of a file containing a dataset
     *                             (if it has no extension, the Dataset
     *                             code will search for a supported extension).
     * @return                     Dataset corresponding to {@code fileName}.
     */
    protected Dataset loadDataset(String fileName) {
        Dataset result = datasetCache.get(fileName);
        if (result == null) {
            result = Dataset.newFileInstanceFromPath(fileName, path,
                    Dataset.Quantity.FIRST_ONLY);
            datasetCache.put(fileName, result);
        }
        return result;
    }

    /**
     * Locates a nested dataset within a larger dataset, and generates
     * an appropriate request error if there is no such nested dataset.
     * @param root                 Top-level dataset.
     * @param path                 Path to a nested dataset within
     *                             {@code root}; null or empty means use
     *                             the top-level dataset.
     * @param request              The DataRequest we are processing; used
     *                             to return error information.
     * @return                     Dataset named by {@code path}.
     */
    protected Dataset findNestedDataset(Dataset root, String path,
            DataRequest request) {
        if ((path == null) || (path.length() == 0)) {
            return root;
        }
        try {
            return root.getChildPath(path);
        }
        catch (Dataset.MissingValueError e) {
            request.setError (new Dataset("message", "nested dataset \"" +
                    path + "\" doesn't exist", "culprit", "dataset"));
            throw new RequestAbortedError();
        }
    }

    /**
     * Flushes all datasets that have been cached in memory, so that they
     * will be reread from disk the next time they are needed.
     */
    public void flush() {
        datasetCache.clear();
    }
}
