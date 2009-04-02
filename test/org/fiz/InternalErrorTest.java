package org.fiz;

/**
 * Junit tests for the InternalError class.
 */
public class InternalErrorTest extends junit.framework.TestCase {
    public void test_InternalError() {
        Error e = new InternalError("sample message");
        assertEquals("exception message", "sample message",
                e.getMessage());
    }
}
