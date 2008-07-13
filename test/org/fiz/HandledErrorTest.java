package org.fiz;

/**
 * Junit tests for the HandledError class.
 */
public class HandledErrorTest extends junit.framework.TestCase {
    public void test_getErrorData_oneError() {
        HandledError e = new HandledError(new Dataset("x", "123",
            "message", "Sample error message"));
        assertEquals("number of data sets returned", 1,
                e.getErrorData().length);
        assertEquals("getErrorData result", "message: Sample error message\n" +
                "x:       123\n",
                e.getErrorData()[0].toString());
    }
    public void test_getErrorData_multipleErrors() {
        HandledError e = new HandledError(new Dataset("error", "first"),
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
        HandledError e = new HandledError(new Dataset("x", "123",
            "message", "Sample error message"));
        assertEquals("error message", "Sample error message",
                e.getMessage());
    }
    public void test_getMessage_multipleErrors() {
        HandledError e = new HandledError(
                new Dataset("message", "first"),
                new Dataset("message", "second"),
                new Dataset("message", "third"));
        assertEquals("error message", "first\n" +
                "second\n" +
                "third",
                e.getMessage());
    }
}
