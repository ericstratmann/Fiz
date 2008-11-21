package org.fiz;

/**
 * Junit tests for the Link class.
 */

public class LinkTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    // No tests for basic constructor: nothing interesting to test.

    public void test_constructor_withPropertiesAndDisplayForm () {
        StringBuilder out = new StringBuilder();
        Link link = new Link(new Dataset("text", "name", "url", "/a/b/c",
                "iconUrl", "/x/y"), Link.DisplayForm.TEXT);
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "<a href=\"/a/b/c\">name</a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());

    }

    public void test_constructor_withPropertiesOnly () {
        StringBuilder out = new StringBuilder();
        Link link = new Link(new Dataset("text", "name", "url", "/a/b/c",
                "iconUrl", "/x/y"));
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "<a href=\"/a/b/c\"><table " +
                "class=\"Link\" cellspacing=\"0\"><tr><td><img " +
                "class=\"Link\" src=\"/x/y\" alt=\"\" /></td><td " +
                "class=\"text\">name</td></tr></table></a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }

    public void test_constructor_withNameAndDisplayForm () {
        StringBuilder out = new StringBuilder();
        Config.setDataset ("links", YamlDataset.newStringInstance(
                "first:\n" +
                "  text: name\n" +
                "  url: /a/b/c\n" +
                "  iconUrl: /x/y/z\n"));
        Link link = new Link("first", Link.DisplayForm.TEXT);
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "<a href=\"/a/b/c\">name</a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }

    public void test_constructor_withNameOnly () {
        StringBuilder out = new StringBuilder();
        Config.setDataset ("links", YamlDataset.newStringInstance(
                "first:\n" +
                "  text: name\n" +
                "  url: /a/b/c\n" +
                "  iconUrl: /x/y/z\n"));
        Link link = new Link("first");
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link",
                "<a href=\"/a/b/c\"><table class=\"Link\" cellspacing" +
                "=\"0\"><tr><td><img class=\"Link\" src=\"/x/y/z\" " +
                "alt=\"\" /></td><td class=\"text\">name</td></tr>" +
                "</table></a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }

    public void test_html_setDisplayText() {
        StringBuilder out = new StringBuilder("123");
        Link link = new Link(new Dataset("url", "http://foo",
                "text", "abc"));
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "123<a href=\"http://foo\">abc</a>",
                out.toString());
        link = new Link(new Dataset("url", "http://foo",
                "text", "abc"), Link.DisplayForm.ICON);
        out.setLength(3);
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
        link = new Link(new Dataset("url", "http://foo", "icon", "xyz"),
                Link.DisplayForm.TEXT);
        out.setLength(3);
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
    }
    public void test_html_setDisplayIcon() {
        StringBuilder out = new StringBuilder("123");
        Link link = new Link(new Dataset("url", "http://foo",
                "iconUrl", "/xyz"));
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "123<a href=\"http://foo\">"
                + "<img class=\"Link\" src=\"/xyz\" alt=\"\" /></a>",
                out.toString());
        link = new Link(new Dataset("url", "http://foo",
                "iconUrl", "/xyz"), Link.DisplayForm.TEXT);
        out.setLength(3);
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
        link = new Link(new Dataset("url", "http://foo", "text", "abc"),
                Link.DisplayForm.ICON);
        out.setLength(3);
        link.html(cr, new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
    }
    public void test_html_expandUrlTemplate() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        Link link = new Link(new Dataset("text", "@name",
                "url", "/url?age=24&name=@name&weight=@weight"));
        link.html(cr, data, out);
        assertEquals("HTML for link", "<a href=\"/url?age=24&amp;"
                + "name=Alice&amp;weight=%22110%22\">Alice</a>", out.toString());
        TestUtil.assertXHTML(out.toString());
    }
    public void test_html_ajaxUrl() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        Link link = new Link(new Dataset("text", "@name",
                "ajaxUrl", "/ajaxUrl?age=24&name=@name&weight=@weight"));
        link.html(cr, data, out);
        assertEquals("HTML for link", "<a href=\"#\" onclick=\"void new " +
                "Fiz.Ajax({url: &quot;/ajaxUrl?age=24&amp;name=Alice" +
                "&amp;weight=%22110%22&quot;}); return false;\">Alice</a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }
    public void test_html_javascriptHref() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        Link link = new Link(new Dataset("text", "@name",
                "javascript", "alert(\"name: @name, weight: @weight\");"));
        link.html(cr, data, out);
        assertEquals("HTML for link", "<a href=\"#\" onclick=\"alert(&quot;" +
                "name: Alice, weight: \\&quot;110\\&quot;&quot;); " +
                "return false;\">Alice</a>", out.toString());
        TestUtil.assertXHTML(out.toString());
    }
    public void test_html_confirmation() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("url", "/url", "text", "@name",
                "confirm", "Do you really want to delete @(name)?"));
        link.html(cr, data, out);
        assertEquals("HTML for link", "<a href=\"/url\" onclick=\"if "
                + "(!confirm(&quot;Do you really want to delete Alice?&quot;) "
                + "{return false;}\">Alice</a>",
                out.toString());
    }
    public void test_html_renderBothIconAndText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("url", "/url",
                "text", "@name", "iconUrl", "/icons/x.gif"));
        link.html(cr, data, out);
        assertEquals("HTML for link", "<a href=\"/url\">"
                + "<table class=\"Link\" cellspacing=\"0\"><tr>"
                + "<td><img class=\"Link\" src=\"/icons/x.gif\" "
                + "alt=\"\" /></td>"
                + "<td class=\"text\">Alice</td></tr></table></a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }
    public void test_html_renderText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("url", "/url",
                "text", "@name"));
        link.html(cr, data, out);
        assertEquals("HTML for link", "<a href=\"/url\">Alice</a>",
                out.toString());
    }
    public void test_html_renderIcon() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("url", "/url",
                "iconUrl", "/icons/x.gif"));
        link.html(cr, data, out);
        assertEquals("HTML for link", "<a href=\"/url\">"
                + "<img class=\"Link\" src=\"/icons/x.gif\" alt=\"\" />"
                + "</a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }

    public void test_iconHtml_expandUrl() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "value", "\"xyz\"");
        Link link = new Link(new Dataset("url", "http://foo",
                "iconUrl", "/icons/x.gif?name=@name&value=@value"));
        link.iconHtml(cr, data, out);
        assertEquals("HTML for icon",
                "<img class=\"Link\" src=\"/icons/x.gif?name=Alice&amp;" +
                "value=%22xyz%22\" alt=\"\" />",
                out.toString());
    }
    public void test_iconHtml_noAltText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        Link link = new Link(new Dataset("url", "http://foo",
                "iconUrl", "/icons/x.gif"));
        link.iconHtml(cr, data, out);
        assertEquals("HTML for icon",
                "<img class=\"Link\" src=\"/icons/x.gif\" alt=\"\" />",
                out.toString());
    }
    public void test_iconHtml_altText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        Link link = new Link(new Dataset("url", "http://foo",
                "iconUrl", "/icons/x.gif", "alt", "picture of @name"));
        link.iconHtml(cr, data, out);
        assertEquals("HTML for icon",
                "<img class=\"Link\" src=\"/icons/x.gif\" "
                + "alt=\"picture of Alice\" />",
                out.toString());
    }

    public void test_confirmHtml_expandTemplate() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        Link link = new Link(new Dataset("url", "http://foo"));
        link.confirmHtml("about to modify name: @name", data, out);
        assertEquals("confirmation attribute",
                " onclick=\"if (!confirm(&quot;about to modify name: "
                + "Alice&quot;) {return false;}\"", out.toString());
    }
    public void test_confirmHtml_quoteMessageChars() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        Link link = new Link(new Dataset("url", "http://foo"));
        link.confirmHtml("OK to modify \"@name\"?", data, out);
        assertEquals("confirmation attribute",
                " onclick=\"if (!confirm(&quot;OK to modify "
                + "\\&quot;Alice\\&quot;?&quot;) {return false;}\"",
                out.toString());
    }
}