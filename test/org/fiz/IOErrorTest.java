package org.fiz;

/**
 * Junit tests for the IOError class.
 */

public class IOErrorTest extends junit.framework.TestCase {
    public void test_IOError() {
        Error e = new IOError("sample message");
        assertEquals("exception message", "sample message",
                e.getMessage());
    }

    public void test_newFileInstance() {
        Error e = IOError.newFileInstance("foo.yaml",
                "foo.yaml (disk exploded)");
        assertEquals("exception message",
                "I/O error in file \"foo.yaml\": disk exploded",
                e.getMessage());
    }
}
