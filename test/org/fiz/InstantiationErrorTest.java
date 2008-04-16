package org.fiz;

/**
 * Junit tests for the ClassNotFoundError class.
 */

public class InstantiationErrorTest extends junit.framework.TestCase {
    public void test_InstantiationError() {
        Error e = new InstantiationError("class_name",
                "it's not you, it's me");
        assertEquals("exception messagee",
                "couldn't create an instance of class \"class_name\": " +
                "it's not you, it's me",
                e.getMessage());
    }
}
