package org.fiz;

/**
 * Junit tests for the HandledError class.
 */
public class HandledErrorTest extends junit.framework.TestCase {
    public void test_handledError() {
        HandledError e = new HandledError(new Dataset("x", "123",
            "message", "Sample error message"));
        assertEquals("error message", "Sample error message",
                e.getMessage());
        assertEquals("getErrorData result", "message: Sample error message\n" +
                "x:       123\n",
                e.getErrorData().toString());
    }
}
