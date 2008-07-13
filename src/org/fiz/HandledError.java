package org.fiz;

/**
 * HandledError is thrown in situations where an error has been detected
 * and properly dealt with, but any further processing of the current request
 * should probably be aborted.  This error is used to unwind execution back
 * to the dispatcher, where the request can be finished up.  Intermediate
 * code can catch this error in the unusual cases where it wants to
 * continue processing or perform its own additional error handling.
 */

public class HandledError extends Error {
    // Copied from the constructor parameter with the same name.
    protected Dataset[] errorDatasets;

    /**
     * Construct a HandledError object from dataset(s) describing the error(s).
     * @param errorDatasets        One or more datasets, each describing a
     *                             problem that occurred.
     */
    public HandledError(Dataset... errorDatasets) {
        super();
        this.errorDatasets = errorDatasets;
    }

    /**
     * Returns the datasets with detailed information about the error(s).
     * @return                     The datasets from which this Error was
     *                             constructed.
     */
    public Dataset[] getErrorData() {
        return errorDatasets;
    }

    /**
     * Returns a string describing all of the errors that resulted
     * in this Error.
     * @return                     A human-readable string describing each
     *                             of the errors in the {@code errorData}
     *                             parameter passed to the constructor.
     */
    public String getMessage() {
        return StringUtil.errorMessage(errorDatasets);
    }
}