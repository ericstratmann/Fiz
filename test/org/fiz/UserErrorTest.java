package org.fiz;

/**
 * Junit tests for the UserError class.
 */
public class UserErrorTest extends junit.framework.TestCase {
    public void test_UserError() {
        Error e = new UserError("sample message");
        assertEquals("exception message", "sample message",
                e.getMessage());
    }
}
