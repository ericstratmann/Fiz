package org.fiz;

/**
 * InternalError is thrown by various Fiz methods when they encounter a
 * coding bug and need to abort the current request.
 */

public class InternalError extends Error {
    /**
     * Construct an InternalError object with a given message.
     * @param message              Information about the problem.
     */
    public InternalError(String message) {
        super(message);
    }
}
