package org.fiz;

/**
 * SyntaxError is thrown by various Fiz methods when they encounter
 * improperly formatted data.  The problem may due to an external
 * agent generating incorrect data, or to an internal problem within
 * Fiz.
 */
public class SyntaxError extends Error {
    /**
     * Construct a SyntaxError object with a given message.
     * @param message              Information about the problem
     */
    public SyntaxError(String message) {
        super(message);
    }
}
