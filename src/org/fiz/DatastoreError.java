package org.fiz;

public class DatastoreError extends Error {
    public DatastoreError(String message, Throwable e) {
        super (message, e);
    }
}
