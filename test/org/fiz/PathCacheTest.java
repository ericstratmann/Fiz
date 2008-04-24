package org.fiz;
import java.io.*;

/**
 * Junit tests for the PathCache class.
 */

public class PathCacheTest extends junit.framework.TestCase {
    public void test_constructor() {
        PathCache p = new PathCache("a/b", "c/d", "e/f");
        assertEquals("saved path", "a/b; c/d; e/f",
                Util.join(p.path, "; "));
    }

    public void test_find_searchThroughPath() {
        (new File("_test1_/child")).mkdirs();
        TestUtil.writeFile("_test1_/child/sample", "test data");
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");
        assertEquals("result", "_test1_/child/sample",
                p.find("child/sample"));
        assertEquals("result", "_test1_/child", p.find("child"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_find_useCachedInfo() {
        (new File("_test1_/child")).mkdirs();
        TestUtil.writeFile("_test1_/child/sample", "test data");
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");

        // The next will fail if it tries to actually find the file, since
        // the file has now been deleted.
        assertEquals("result", "_test1_/child/sample",
                p.find("child/sample"));
    }
    public void test_find_fileNotFound() {
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");
        boolean gotException = false;
        try {
            p.find("xyz");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find file \"xyz\" in path (\"_bogus_\", " +
                    "\"_test1_\", \"test1_/child\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_clear() {
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");
        TestUtil.writeFile("_test1_/child/sample", "test data");
        p.find("child/sample");
        TestUtil.deleteTree("_test1_");
        p.flush();
        boolean gotException = false;
        try {
            // This should fail, since the file has been deleted and
            // the cache has been flushed.
            p.find("child/sample");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find file \"child/sample\" in path " +
                    "(\"_bogus_\", \"_test1_\", \"test1_/child\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
