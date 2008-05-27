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
    protected Dataset errorData;

    /**
     * Construct a HandledError object from a dataset describing the error.
     * @param errorData            Dataset whose values described the problem.
     */
    public HandledError(Dataset errorData) {
        super(errorData.get("message"));
        this.errorData = errorData;
    }

    /**
     * Returns the dataset with detailed information about this error.
     * @return                     The Dataset from which this Error was
     *                             constructed.
     */
    public Dataset getErrorData() {
        return errorData;
    }
}