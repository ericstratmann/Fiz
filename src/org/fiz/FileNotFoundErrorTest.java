/**
 * Junit tests for the FileNotFoundError class.
 */

package org.fiz;
public class FileNotFoundErrorTest extends junit.framework.TestCase {
    public void test_FileNotFoundError_nameAndMessage() {
        Error e = new FileNotFoundError("foo.txt", null,
                "foo.txt (sample message)");
        assertEquals("exception message without type",
                "couldn't open file \"foo.txt\": sample message",
                e.getMessage());
        e = new FileNotFoundError("foo.txt", "dataset",
                "sample message");
        assertEquals("exception message with type",
                "couldn't open dataset file \"foo.txt\": sample message",
                e.getMessage());
    }
    public void test_FileNotFoundError_messageOnly() {
        Error e = new FileNotFoundError("foo.txt (sample message)");
        assertEquals("exception message", "foo.txt (sample message)",
                e.getMessage());
    }

    public void test_newPathInstance() {
        Error e = FileNotFoundError.newPathInstance("foo.yaml", null,
                new String[] {"/bin", "/usr/local/tools", "C:/code"});
        assertEquals("exception message without type",
                "couldn't find file \"foo.yaml\" in path (\"/bin\", "
                + "\"/usr/local/tools\", \"C:/code\")",
                e.getMessage());
        e = FileNotFoundError.newPathInstance("foo.yaml", "image",
                new String[] {"/bin", "/usr/local/tools", "C:/code"});
        assertEquals("exception message without type",
                "couldn't find image file \"foo.yaml\" in path (\"/bin\", "
                + "\"/usr/local/tools\", \"C:/code\")",
                e.getMessage());
    }
}