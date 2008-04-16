package org.fiz;

/**
 * ClassNotFoundError is thrown by various Fiz methods when a desired
 * class cannot be found.  It is used in situations where it's unlikely
 * that the caller will be able to handle the problem; most likely, the
 * current request will be aborted.  Using an Error allows us to unwind
 * the call stack easily without every ancestor having to declare an
 * exception.
 */

public class ClassNotFoundError extends Error {
    /**
     * Construct a ClassNotFoundError that indicates the missing class.
     * @param className            Name of the class that could not be found.
     */
    public ClassNotFoundError(String className) {
        super("couldn't find class \"" + className + "\"");
    }
}
