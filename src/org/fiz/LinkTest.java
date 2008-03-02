/**
 * Junit tests for the Link class.
 */

package org.fiz;

public class LinkTest extends junit.framework.TestCase {

    public void testConstructor_missingBase() {
        boolean gotException = false;
        try {
            Link link = new Link(new Dataset());
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"base\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void testConstructor_parseArgs() {
        TestLink link = new TestLink(new Dataset("base", "http://www.xyz.com",
                "args", "project, procedure:subproc, x:  y, final"));
        assertEquals("query names", "project, procedure, x, final",
                link.getQueryNames());
        assertEquals("query data", "project, subproc, y, final",
                link.getQueryData());
    }
    public void testConstructor_noArgs() {
        TestLink link = new TestLink(new Dataset("base", "http://www.xyz.com"));
        assertEquals("query names", "", link.getQueryNames());
        assertEquals("query data", "", link.getQueryData());
    }

    public void testHtml_setDisplayText() {
        StringBuilder out = new StringBuilder("123");
        Link link = new Link(new Dataset("base", "http://foo",
                "text", "abc"));
        link.html(new Dataset(), out);
        assertEquals("HTML for link", "123<a href=\"http://foo\">abc</a>",
                out.toString());
        link = new Link(new Dataset("base", "http://foo",
                "text", "abc"), Link.DisplayForm.ICON);
        out.setLength(3);
        link.html(new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
        link = new Link(new Dataset("base", "http://foo", "icon", "xyz"),
                Link.DisplayForm.TEXT);
        out.setLength(3);
        link.html(new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
    }
    public void testHtml_setDisplayIcon() {
        StringBuilder out = new StringBuilder("123");
        Link link = new Link(new Dataset("base", "http://foo",
                "icon", "xyz"));
        link.html(new Dataset(), out);
        assertEquals("HTML for link", "123<a href=\"http://foo\">"
                + "<img class=\"link_image\" src=\"xyz\" alt=\"\" /></a>",
                out.toString());
        link = new Link(new Dataset("base", "http://foo",
                "icon", "xyz"), Link.DisplayForm.TEXT);
        out.setLength(3);
        link.html(new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
        link = new Link(new Dataset("base", "http://foo", "text", "abc"),
                Link.DisplayForm.ICON);
        out.setLength(3);
        link.html(new Dataset(), out);
        assertEquals("HTML for link", "123", out.toString());
    }
    public void testHtml_baseAlreadyHasQueryData() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("base", "/url?age=24",
                "args", "name,weight", "text", "@name"));
        link.html(data, out);
        assertEquals("HTML for link", "<a href=\"/url?age=24&amp;"
                + "name=Alice&amp;weight=110\">Alice</a>", out.toString());
        TestUtil.assertXHTML(out.toString());
    }
    public void testHtml_baseHasNoQueryData() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("base", "/url",
                "args", "name,weight", "text", "@name"));
        link.html(data, out);
        assertEquals("HTML for link", "<a href=\"/url?name=Alice"
                + "&amp;weight=110\">Alice</a>", out.toString());
    }
    public void testHtml_noArgsInUrl() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("base", "/url",
                "text", "@name"));
        link.html(data, out);
        assertEquals("HTML for link", "<a href=\"/url\">Alice</a>",
                out.toString());
    }
    public void testHtml_confirmation() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("base", "/url", "text", "@name",
                "confirm", "Do you really want to delete @name?"));
        link.html(data, out);
        assertEquals("HTML for link", "<a href=\"/url\" onclick=\"if "
                + "(!confirm(&quot;Do you really want to delete Alice?&quot;) "
                + "{return false;}\">Alice</a>",
                out.toString());
    }
    public void testHtml_renderBothIconAndText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("base", "/url",
                "text", "@name", "icon", "/icons/x.gif"));
        link.html(data, out);
        assertEquals("HTML for link", "<a href=\"/url\">"
                + "<table class=\"link_table\" cellspacing=\"0\"><tr>"
                + "<td><img class=\"link_image\" src=\"/icons/x.gif\" "
                + "alt=\"\" /></td>"
                + "<td class=\"link_text\">Alice</td></tr></table></a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }
    public void testHtml_renderText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("base", "/url",
                "text", "@name"));
        link.html(data, out);
        assertEquals("HTML for link", "<a href=\"/url\">Alice</a>",
                out.toString());
    }
    public void testHtml_renderIcon() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice", "weight", "110");
        Link link = new Link(new Dataset("base", "/url",
                "icon", "/icons/x.gif"));
        link.html(data, out);
        assertEquals("HTML for link", "<a href=\"/url\">"
                + "<img class=\"link_image\" src=\"/icons/x.gif\" alt=\"\" />"
                + "</a>",
                out.toString());
        TestUtil.assertXHTML(out.toString());
    }

    public void testIconHtml_noAltText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        TestLink link = new TestLink(new Dataset("base", "http://foo",
                "icon", "/icons/x.gif"));
        link.iconHtml(data, out);
        assertEquals("HTML for icon",
                "<img class=\"link_image\" src=\"/icons/x.gif\" alt=\"\" />",
                out.toString());
    }
    public void testIconHtml_altText() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        TestLink link = new TestLink(new Dataset("base", "http://foo",
                "icon", "/icons/x.gif", "alt", "picture of @name"));
        link.iconHtml(data, out);
        assertEquals("HTML for icon",
                "<img class=\"link_image\" src=\"/icons/x.gif\" "
                + "alt=\"picture of Alice\" />",
                out.toString());
    }

    public void testConfirmHtml_expandTemplate() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        TestLink link = new TestLink(new Dataset("base", "http://foo"));
        link.confirmHtml("about to modify name: @name", data, out);
        assertEquals("confirmation attribute",
                "onclick=\"if (!confirm(&quot;about to modify name: "
                + "Alice&quot;) {return false;}\"", out.toString());
    }
    public void testConfirmHtml_MessageChars() {
        StringBuilder out = new StringBuilder();
        Dataset data = new Dataset("name", "Alice");
        TestLink link = new TestLink(new Dataset("base", "http://foo"));
        link.confirmHtml("OK to modify \"@name\"?", data, out);
        assertEquals("confirmation attribute",
                "onclick=\"if (!confirm(&quot;OK to modify "
                + "\\&quot;Alice\\&quot;?&quot;) {return false;}\"",
                out.toString());
    }
}

// The following class definition provides a mechanism for accessing
// protected/private fields and methods.
class TestLink extends Link {
    public TestLink(Dataset config) {
        super(config);
    }
    public String getIcon() {
        return icon;
    }
    public String getAlt() {
        return alt;
    }
    public String getBase() {
        return base;
    }
    public String getQueryNames() {
        return Util.join(queryNames, ", ");
    }
    public String getQueryData() {
        return  Util.join(queryData, ", ");
    }
    public String getConfirm() {
        return confirm;
    }
    public void iconHtml(Dataset data, StringBuilder out) {
        super.iconHtml(data, out);
    }
    public void confirmHtml(String template, Dataset data,
            StringBuilder out) {
        super.confirmHtml(template, data, out);
    }
}