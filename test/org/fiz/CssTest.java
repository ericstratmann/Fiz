package org.fiz;
import java.io.*;

/**
 * Junit tests for the Config class.
 */

public class CssTest extends junit.framework.TestCase {
    public void setUp() {
        (new File("_csstest_")).mkdir();
        TestUtil.writeFile("_csstest_/test.css", "Sample: @value");
        TestUtil.writeFile("_csstest_/css.yaml", "value: 99234\n");
        Css.init("_csstest_");
        Config.init("_csstest_");
    }

    public void tearDown() {
        Util.deleteTree("_csstest_");
    }

    public void test_init() {
        String css = Css.getStylesheet("test.css");
        (new File("_csstest_/child")).mkdir();
        TestUtil.writeFile("_csstest_/child/test.css",
                "Child template: @value");
        String[] path = new String[] {"_csstest_/child"};
        Css.init(path);
        path[0] = ".";
        assertEquals("make sure path was cloned by init",
                "_csstest_/child", Css.getSearchPath()[0]);
        css = Css.getStylesheet("test.css");
        assertEquals("make sure init flushes cache and resets path",
                "Child template: 99234", css);
    }

    public void test_clearCache() {
        String css = Css.getStylesheet("test.css");
        assertEquals("size of cache after loading a stylesheet",
                1, Css.cache.size());
        Css.clearCache();
        assertEquals("size of cache after clearing",
                0, Css.cache.size());
    }

    public void test_getPath() {
        Css.init("a/b", "c/d", "e/f");
        String[] path = Css.getPath();
        assertEquals("a/b, c/d, e/f", StringUtil.join(path, ", "));
    }

    public void test_getStylesheet() {
        String css = Css.getStylesheet("test.css");
        assertEquals("first call: nothing cached", "Sample: 99234", css);
        TestUtil.deleteTree("_csstest_");
        css = Css.getStylesheet("test.css");
        assertEquals("use caches for second call", "Sample: 99234", css);
    }

    public void test_getSearchPath() {
        Css.init("a/b", "y/z");
        String[] path = Css.getSearchPath();
        assertEquals("directories in path", "a/b, y/z",
                StringUtil.join(path, ", "));
        path[0] = "modified";
        assertEquals("make sure result is cloned", "a/b, y/z",
                StringUtil.join(Css.getSearchPath(), ", "));
    }
}
