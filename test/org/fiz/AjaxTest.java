package org.fiz;

/**
 * Junit tests for the Ajax class.
 */

public class AjaxTest extends junit.framework.TestCase {
    public void test_SyntaxError() {
        Ajax.SyntaxError error = new Ajax.SyntaxError("missing comma");
        assertEquals("missing comma", error.getMessage());
    }

    public void test_invoke_withStringBuilder() {
        ClientRequest cr = new ClientRequestFixture();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        StringBuilder out = new StringBuilder();
        Ajax.invoke(cr, "/fiz/test/alert?age=24&name=@name&weight=@weight",
                data, out);
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/fizlib/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax(\"/fiz/test/alert?age=24&name=Alice&" +
                "weight=%22110%22\");",
                out.toString());
    }

    public void test_invoke_withoutStringBuilder() {
        ClientRequest cr = new ClientRequestFixture();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        StringBuilder out = Ajax.invoke(cr,
                "/fiz/test/alert?age=24&name=@name&weight=@weight",
                data);
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/fizlib/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax(\"/fiz/test/alert?age=24&name=Alice&" +
                "weight=%22110%22\");",
                out.toString());
    }
}
