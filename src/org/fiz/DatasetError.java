package org.fiz;

/**
 * DatasetError is a generic exception class used in situations where
 * the problem is described by a dataset with various fields, such as
 * errors that occur in DataRequests.
 */
public class DatasetError extends Error {
    // The dataset(s) that were supplied to the constructor.
    protected Dataset errorDatasets[];

    /**
     * Construct a HandledError object from dataset(s) describing the error(s).
     * @param errorDatasets        One or more datasets, each describing a
     *                             problem that occurred.
     */
    public DatasetError(Dataset... errorDatasets) {
        super();
        this.errorDatasets = errorDatasets;
    }

    /**
     * Returns the datasets containing detailed information about the error(s).
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
