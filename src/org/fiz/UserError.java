package org.fiz;

/**
 * UserError is thrown by various Fiz methods when they encounter a problem
 * caused by the user (such as a file upload that exceeds the size limit).
 * This error will cause the current request to be aborted, and a message
 * will be displayed for the user by the browser.
 */
public class UserError extends Error {
    /**
     * Construct a UserError object with a given message.
     * @param message              Information about the problem.
     */
    public UserError(String message) {
        super(message);
    }
}
