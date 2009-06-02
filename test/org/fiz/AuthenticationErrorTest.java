package org.fiz;

/**
 * Junit tests for the AuthenticationError class.
 */

public class AuthenticationErrorTest extends junit.framework.TestCase {
    public void test_AuthenticationError() {
        Error e = new AuthenticationError();
        assertEquals("exception message", "invalid or missing authentication " +
                "token; most likely the page is stale and needs to be " +
                "refreshed",
                e.getMessage());
    }
}