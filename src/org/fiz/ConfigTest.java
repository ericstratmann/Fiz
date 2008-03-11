/**
 * Junit tests for the Config class.
 */

package org.fiz;
import java.io.*;

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

    public void test_getPath() {
        Config.init("a/b/c", "x/y");
        String[] path = Config.getPath();
        assertEquals("length of path", 2, path.length);
        assertEquals("first directory in path", "a/b/c", path[0]);
        assertEquals("second directory in path", "x/y", path[1]);
        path[0] = "modified";
        assertEquals("return value is cloned", "a/b/c", Config.getPath()[0]);
    }
}
