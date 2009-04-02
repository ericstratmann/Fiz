/**
 * This class provides a partial implementation of the log4j Appender
 * interface, which is enough (barely) to collect log messages in a string
 * for unit testing purposes.
 */

package org.fiz;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

public class StringAppender implements Appender {
    public StringBuilder log = new StringBuilder();

    public void doAppend(LoggingEvent event) {
        if (log.length() != 0) {
            log.append('\n');
        }
        log.append(event.getMessage().toString());
    }

    // Stubs for methods we don't care about here:

    public void addFilter(Filter newFilter) {
    }
    public void clearFilters() {
    }
    public void close() {
    }
    public ErrorHandler getErrorHandler() {
        return null;
    }
    public Filter getFilter() {
        return null;
    }
    public Layout getLayout() {
        return null;
    }
    public String getName() {
        return "StringAppender";
    }
    public boolean requiresLayout() {
        return false;
    }
    public void setErrorHandler(ErrorHandler errorHeader) {
    }
    public void setLayout(Layout layout) {
    }
    public void setName(String name) {
    }
}
