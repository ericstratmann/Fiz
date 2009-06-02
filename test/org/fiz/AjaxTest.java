package org.fiz;

/**
 * Junit tests for the Ajax class.
 */

public class AjaxTest extends junit.framework.TestCase {

    public void test_invoke_withStringBuilder() {
        ClientRequest cr = new ClientRequestFixture();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        StringBuilder out = new StringBuilder();
        Ajax.invoke(cr, "/fiz/test/alert?age=24&name=@name&weight=@weight",
                data, out);
        TestUtil.assertSubstring("set authentication token",
                "Fiz.auth =", cr.getHtml().jsCode.toString());
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/fizlib/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax({url: \"/fiz/test/alert?age=24&" +
                "name=Alice&weight=%22110%22\"});",
                out.toString());
    }

    public void test_invoke_withoutStringBuilder() {
        ClientRequest cr = new ClientRequestFixture();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        String out = Ajax.invoke(cr,
                "/fiz/test/alert?age=24&name=@name&weight=@weight",
                data);
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/fizlib/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax({url: \"/fiz/test/alert?age=24&" +
                "name=Alice&weight=%22110%22\"});",
                out);
    }

    public void test_invoke_withIndexedData() {
        ClientRequest cr = new ClientRequestFixture();
        String out = Ajax.invoke(cr,
                "/fiz/test/alert?age=24&name=@1&weight=@2",
                "<abcd>", "120");
        TestUtil.assertSubstring("set authentication token",
                "Fiz.auth =", cr.getHtml().jsCode.toString());
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/fizlib/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax({url: \"/fiz/test/alert?age=24&" +
                "name=%3cabcd%3e&weight=120\"});",
                out);
    }
}
