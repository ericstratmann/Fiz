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
        String css = Css.getStylesheet("Test.css");
        (new File("_csstest_/child")).mkdir();
        TestUtil.writeFile("_csstest_/child/test.css",
                "Child template: @value");
        String[] path = new String[] {"_csstest_/child"};
        Css.init(path);
        path[0] = ".";
        assertEquals("make sure path was cloned by init",
                "_csstest_/child", Css.getPath()[0]);
        css = Css.getStylesheet("Test.css");
        assertEquals("make sure init flushes cache and resets path",
                "Child template: 99234", css);
    }

    public void test_getStylesheet() {
        String css = Css.getStylesheet("Test.css");
        assertEquals("first call: nothing cached", "Sample: 99234", css);
        TestUtil.deleteTree("_csstest_");
        css = Css.getStylesheet("Test.css");
        assertEquals("use caches for second call", "Sample: 99234", css);
    }

    public void test_handleRequest() throws IOException {
        ServletResponseFixture response = new ServletResponseFixture();
        Css.handleRequest(null, response, "test.css");
        assertEquals("first call: nothing cached", "Sample: 99234",
                response.out.toString());
    }

    public void test_getPath() {
        Css.init("a/b", "y/z");
        String[] path = Css.getPath();
        assertEquals("directories in path", "a/b, y/z",
                Util.join(path, ", "));
        path[0] = "modified";
        assertEquals("make sure result is cloned", "a/b, y/z",
                Util.join(Css.getPath(), ", "));
    }
}
