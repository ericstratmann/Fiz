package org.fiz;

/**
 * InstantiationError is thrown by various Fiz methods when a dynamically
 * loaded class cannot be instantiated.    It is used in situations where
 * it's unlikely that the caller will be able to handle the problem; most
 * likely, the current request will be aborted.  Using an Error allows us
 * to unwind the call stack easily without every ancestor having to declare
 * an exception.
 */

public class InstantiationError extends Error {
    /**
     * Construct a InstantiationError with a message that contains the
     * className and an additional message.
     * @param className            Name of the class that could not be found.
     * @param message              Additional information about the problem,
     *                             such as "class isn't a subclass of Foo".
     */
    public InstantiationError(String className, String message) {
        super("couldn't create an instance of class \"" + className +
                "\": " + message );
    }
}