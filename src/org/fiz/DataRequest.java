package org.fiz;

import java.util.*;

/**
 * A DataRequest represents an operation to be performed (eventually) by a
 * data manager, such as reading information from some source, or updating
 * an existing record.  The DataRequest stores information about the
 * outbound request as well as the response that is eventually returned
 * by the data manager.  The response consists either of a dataset
 * containing result information or one or more error datasets describing
 * why the request failed.  DataRequests are typically created by
 * invoking a factory method in a DataManager, which creates a new
 * request and initializes it with data manager-specific information.
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

    // Human-readable name for this request, typically of the form
    // manager.operation.
    protected String name = null;

    // Result information returned by the data manager.  Null means
    // the request has not completed or it returned an error.
    protected Dataset response = null;

    // If setError has been called, this holds information about all of
    // the errors.  Null means setError has not been called.
    protected Dataset[] errorDatasets = null;

    // Indicates the state of the request:
    protected boolean completed = false;

    /**
     * Construct an unnamed DataRequest object.
     */
    public DataRequest() {
    }

    /**
     * Construct a named DataRequest object.  Normally the constructor will also
     * initiate processing of the request (unless the data manager wants
     * to delay in order to batch several requests together).
     * @param name                 Human-readable string describing the data
     *                             request (recommended form: manager.request).
     *                             Used only for error messages.
     */
    public DataRequest(String name) {
        this.name = name;
    }

    /**
     * This method is invoked to abort a request.  If the data manager
     * doesn't override this implementation, then cancel requests are
     * ignored.
     */
    public void cancel() {
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
     * Indicates whether or not a request has completed.
     * @return                     True means the processing of this request
     *                             has finished; false means the request is
     *                             still in progress.
     */
    public boolean isComplete() {
        return completed;
    }

    /**
     * Returns the response from the request. If the request hasn't yet
     * completed, this method will wait for the request to complete before
     * returning.
     * @return                     A dataset containing the results of the
     *                             request.  If an error occurred then
     *                             the return value will be null.
     */
    public synchronized Dataset getResponseData() {
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
     * Wait for the request to complete and either return its response
     * (if it completed successfully) or throw a RequestError if it failed.
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
     * whenever appropriate.  Data managers may also provide additional
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
     *                     meaningful to a user.
     * trace -             A stack trace of execution at the time of the error.
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
     * this method first waits for the request to complete.
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
     * this method first waits for the request to complete.
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
     * to complete.
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
     * Throws a RequestError exception containing information about the
     * failure in this request.  This method should only be invoked if
     * the request has failed.
     */
    public void throwError() {
        throw new RequestError("error in data request" +
                ((name != null) ? (" " + name) : "") +
                ": " + getDetailedErrorMessage());
    }

    /**
     * Modify the human-readable name associated with this request.
     * @param name                 Human-readable string describing the data
     *                             request (recommended form: manager.request).
     *                             Used only for error messages.  Null means
     *                             "no name".
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returned the name for this request, which was associated with the request
     * when it was constructed or by calling {@code setName}.
     * @return                     The name of the request, or null if there
     *                             is none.
     */
    public String getName() {
        return name;
    }
}
