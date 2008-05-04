package org.fiz;

/**
 * IOError is thrown by various Fiz methods when there are problems reading
 * or writing external data.  Typically, java.io.IOException exceptions are
 * turned into IOError's to make it easier to unwind the entire request
 * without having to declare exceptions at every level.
 */

public class IOError extends Error {
    /**
     * Construct an IOError object with a given message.
     * @param message              Information about the problem
     */
    public IOError(String message) {
        super(message);
    }

    /**
     * Creates an IOError object suitable for reporting exceptions
     * on a named file.
     * @param fileName             Name of the file for which the error occurred
     * @param details              Detailed information about the error;
     *                             typically e.getMessage() for an exception.
     * @return                     An Error object ready to throw.
     */
    public static IOError newFileInstance(String fileName, String details) {
        return new IOError("I/O error in file \"" + fileName + "\": "
                + StringUtil.extractInnerMessage(details));
    }
}
