/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz.test;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

/**
 * This class provides a partial implementation of the log4j Appender
 * interface, which is enough (barely) to collect log messages in a string
 * for unit testing purposes.
 */
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
