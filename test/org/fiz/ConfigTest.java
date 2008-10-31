package org.fiz;
import java.io.*;

/**
 * Junit tests for the Config class.
 */

public class ConfigTest extends junit.framework.TestCase {
    public void test_init() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/main.yaml", "directory: _test1_\n");
        TestUtil.writeFile("_test1_/child/main.yaml",
                "directory: _test1_/child\n");
        Config.init("_test1_");
        Dataset d = Config.getDataset("main");
        assertEquals("dataset in _test1_", "_test1_", d.check("directory"));
        Config.init("_test1_/child");
        d = Config.getDataset("main");
        assertEquals("dataset in _test1_/child", "_test1_/child",
                d.check("directory"));
        TestUtil.deleteTree("_test1_");
    }

    public void test_clearCache() {
        Config.init(".");
        TestUtil.writeFile("main.yaml",
                "name1: value1\nname2: value2\n");
        Config.get("main", "name1");
        assertEquals("size of cache after reading a value",
                1, Config.cache.size());
        Config.clearCache();
        assertEquals("size of cache after clearing",
                0, Config.cache.size());
    }

    public void test_get() {
        Config.init(".");
        TestUtil.writeFile("main.yaml",
                "name1: value1\nname2: value2\n");
        assertEquals("value1", Config.get("main", "name1"));
        TestUtil.deleteTree("main.yaml");
    }

    public void test_getDataset() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        Config.init("_test1_", "_test1_/child");
        TestUtil.writeFile("_test1_/child/main.yaml",
                "name1: value1\nname2: value2\n");
        Dataset d = Config.getDataset("main");
        assertEquals("value from first call (uncached)", "value2",
                d.check("name2"));
        TestUtil.deleteTree("_test1_");
        d = Config.getDataset("main");
        assertEquals("value from second call (cached)", "value1",
                d.check("name1"));
    }

    public void test_setDataset() {
        (new File("_test1_")).mkdir();
        Config.init("_test1_");
        TestUtil.writeFile("_test1_/main.yaml",
                "name1: value1\nname2: value2\n");
        Dataset d = Config.getDataset("main");
        assertEquals("value from first call (file from disk)", "value1",
                d.check("name1"));
        Config.setDataset("main", new Dataset("name1", "xyzzy"));
        d = Config.getDataset("main");
        assertEquals("value from second call (overridden with setDataset)",
                "xyzzy", d.check("name1"));
        TestUtil.deleteTree("_test1_");

    }
    public void test_getSearchPath() {
        Config.init("a/b/c", "x/y");
        String[] path = Config.getSearchPath();
        assertEquals("length of path", 2, path.length);
        assertEquals("first directory in path", "a/b/c", path[0]);
        assertEquals("second directory in path", "x/y", path[1]);
        path[0] = "modified";
        assertEquals("return value is cloned", "a/b/c", Config.getSearchPath()[0]);
    }
}
