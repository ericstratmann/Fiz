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

package org.fiz;

/**
 * Junit tests for the HandledError class.
 */
public class DatasetErrorTest extends junit.framework.TestCase {
    public void test_getErrorData_oneError() {
        DatasetError e = new DatasetError(new Dataset("x", "123",
            "message", "Sample error message"));
        assertEquals("number of data sets returned", 1,
                e.getErrorData().length);
        assertEquals("getErrorData result", "message: Sample error message\n" +
                "x:       123\n",
                e.getErrorData()[0].toString());
    }
    public void test_getErrorData_multipleErrors() {
        DatasetError e = new DatasetError(new Dataset("error", "first"),
                new Dataset("error", "second"),
                new Dataset("error", "third"));
        assertEquals("number of data sets returned", 3,
                e.getErrorData().length);
        assertEquals("getErrorData first result", "error: first\n",
                e.getErrorData()[0].toString());
        assertEquals("getErrorData second result", "error: second\n",
                e.getErrorData()[1].toString());
        assertEquals("getErrorData third result", "error: third\n",
                e.getErrorData()[2].toString());
    }

    public void test_getMessage_oneError() {
        DatasetError e = new DatasetError(new Dataset("x", "123",
            "message", "Sample error message"));
        assertEquals("error message", "Sample error message",
                e.getMessage());
    }
    public void test_getMessage_multipleErrors() {
        DatasetError e = new DatasetError(
                new Dataset("message", "first"),
                new Dataset("message", "second"),
                new Dataset("message", "third"));
        assertEquals("error message", "first\n" +
                "second\n" +
                "third",
                e.getMessage());
    }
}
