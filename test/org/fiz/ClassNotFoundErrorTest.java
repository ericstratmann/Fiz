package org.fiz;

/**
 * Junit tests for the ClassNotFoundError class.
 */

public class ClassNotFoundErrorTest extends junit.framework.TestCase {
    public void test_ClassNotFoundErrory() {
        Error e = new ClassNotFoundError("class_name");
        assertEquals("exception messagee",
                "couldn't find class \"class_name\"",
                e.getMessage());
    }
}
