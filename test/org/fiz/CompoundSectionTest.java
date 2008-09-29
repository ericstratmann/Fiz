package org.fiz;

/**
 * Junit tests for the CompoundSection class.
 */

public class CompoundSectionTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
        TabSection.setTemplate(null);
    }

    public void test_constructor() {
        CompoundSection section = new CompoundSection(new Dataset(
                "a", "1", "b", "2"), new TemplateSection("abc"),
                new TemplateSection("xyz"));
        assertEquals("configuration properties", "a: 1\n" +
                "b: 2\n",
                section.properties.toString());
        assertEquals("count of child sections", 2, section.children.length);
    }

    public void test_html_basics() {
        CompoundSection section = new CompoundSection(
                new Dataset("borderBase", "a/b/c"),
                new TemplateSection("<h1>First section</h1>\n"),
                new TemplateSection("<h1>Second section</h1>\n"));
        section.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection -->\n" +
                "<table cellspacing=\"0\">\n" +
                "  <tr style=\"line-height: 0px;\">\n" +
                "    <td><img src=\"a/b/c-nw.gif\" alt=\"\" /></td>\n" +
                "    <td style=\"background-image: url(a/b/c-n.gif); " +
                "background-repeat: repeat-x;\"></td>\n" +
                "    <td><img src=\"a/b/c-ne.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td style=\"background-image: url(a/b/c-w.gif); " +
                "background-repeat: repeat-y;\"></td>\n" +
                "    <td class=\"compoundBody\">\n" +
                "<h1>First section</h1>\n" +
                "<h1>Second section</h1>\n" +
                "    </td>\n" +
                "    <td style=\"background-image: url(a/b/c-e.gif); " +
                "background-repeat: repeat-y;\"></td>\n" +
                "  </tr>\n" +
                "  <tr style=\"line-height: 0px;\">\n" +
                "    <td><img src=\"a/b/c-sw.gif\" alt=\"\" /></td>\n" +
                "    <td style=\"background-image: url(a/b/c-s.gif); " +
                "background-repeat: repeat-x;\"></td>\n" +
                "    <td><img src=\"a/b/c-se.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End CompoundSection -->\n",
                html);
    }
    public void test_html_idProperty() {
        CompoundSection section = new CompoundSection(
                new Dataset("id", "test44", "borderBase", "a/b/c"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        TestUtil.assertSubstring("display id in start comment",
                "<!-- Start CompoundSection test44 -->", html);
        TestUtil.assertSubstring("generate id attribute for table",
                "<table id=\"test44\" cellspacing=\"0\">", html);
        TestUtil.assertSubstring("display id in end comment",
                "<!-- End CompoundSection test44 -->", html);
    }
    public void test_html_classProperty() {
        CompoundSection section = new CompoundSection(
                new Dataset("class", "class22", "borderBase", "a/b/c"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        TestUtil.assertSubstring("generate class attribute for table",
                "<table class=\"class22\" cellspacing=\"0\">", html);
    }
    public void test_html_backgroundProperty() {
        CompoundSection section = new CompoundSection(
                new Dataset("background", "#ff0000", "borderBase", "a/b/c"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        TestUtil.assertSubstring("use background property",
                "<td class=\"compoundBody\" style=\"background: #ff0000;\">",
                html);
    }
    public void test_html_divSimple() {
        CompoundSection section = new CompoundSection(
                new Dataset(),
                new TemplateSection("<h1>First section</h1>\n"));
        section.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection -->\n" +
                "<div>\n" +
                "<h1>First section</h1>\n" +
                "</div>\n" +
                "<!-- End CompoundSection -->\n",
                html);
    }
    public void test_html_divWithIdAndClassAndBackground() {
        CompoundSection section = new CompoundSection(
                new Dataset("id", "test44", "class", "class22",
                "background", "#ff0000"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection test44 -->\n" +
                "<div id=\"test44\" class=\"class22\" " +
                "style=\"background: #ff0000;\">\n" +
                "<h1>First section</h1>\n" +
                "</div>\n" +
                "<!-- End CompoundSection test44 -->\n",
                html);
    }

    public void test_registerRequests() {
        CompoundSection section = new CompoundSection(
                new Dataset("id", "test44", "class", "class22",
                "background", "#ff0000"),
                new TemplateSection("getPerson", "<h1>First section</h1>\n"),
                new TemplateSection("getPeople", "<h1>First section</h1>\n"));
        section.registerRequests(cr);
        assertEquals("registered requests", "getPeople, getPerson",
                cr.getRequestNames());
    }
}
