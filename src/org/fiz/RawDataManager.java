package org.fiz;

/**
 * RawDataManager implements two requests (one normal and one for returning
 * errors) that return precomputed data provided by the caller.
 */

public class RawDataManager {
    /**
     * Create a DataRequest that will return a given dataset as its result.
     * @param result               Use this as the result of the request.
     * @return                     A DataRequest whose response will be
     *                             {@code result}.
     */
    public static DataRequest newRequest(Dataset result) {
        DataRequest request = new DataRequest("raw.normal");
        request.setComplete(result);
        return request;
    }

    /**
     * Create a DataRequest that will return an error.
     * @param errorInfo            The arguments consist of any number
     *                             of datasets, which provide information
     *                             about the "error".
     * @return                     A DataRequest that will return with
     *                             an error.
     */
    public static DataRequest newError(Dataset... errorInfo) {
        DataRequest request = new DataRequest("raw.error");
        request.setError(errorInfo);
        return request;
    }
}
