package org.fiz;

/**
 * HandledError is a dummy interface.  If an error class implements this
 * interface that means that the error has been detected and properly dealt
 * with; all that's needed is to abort the current request.  This error
 * is used to unwind execution back to the dispatcher, where the request
 * can be finished up.  Intermediate code can catch this error in the
 * unusual cases where it wants to continue processing or perform its
 * own additional error handling.
 */

public interface HandledError {
}