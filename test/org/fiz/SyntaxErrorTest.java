package org.fiz;

/**
 * Junit tests for the SyntaxError class.
 */
public class SyntaxErrorTest extends junit.framework.TestCase {
    public void test_constructor() {
        Error e = new SyntaxError("sample message");
        assertEquals("exception message", "sample message",
                e.getMessage());
    }
}
