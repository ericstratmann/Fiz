package org.fiz;

/**
 * Junit tests for the Column class.
 */

public class ColumnTest extends junit.framework.TestCase {
    public void test_constructor_labelAndTemplate() {
        Column c = new Column ("label111", "@name");
        assertEquals("label value", "label111", c.label);
        assertEquals("template value", "@name", c.template);
    }

    public void test_constructor_labelAndFormatter() {
        Link link = new Link(new Dataset("text", "click here",
                "url", "/a/b"));
        Column c = new Column ("label111", link);
        assertEquals("label value", "label111", c.label);
        assertEquals("formatter value", link, c.formatter);
    }

    public void test_headerHtml() {
        Column c = new Column ("<label>", "id44");
        StringBuilder out = new StringBuilder();
        c.headerHtml(null, out);
        assertEquals("generated HTML", "&lt;label&gt;", out.toString());
    }

    public void test_html_formatter() {
        Link link = new Link(new Dataset("text", "click here",
                "url", "/a/b/@name"));
        Column c = new Column ("<label>", link);
        StringBuilder out = new StringBuilder();
        c.html(null, new Dataset("name", "Alice"), out);
        assertEquals("generated HTML",
                "<a href=\"/a/b/Alice\">click here</a>", out.toString());
    }
    public void test_html_template() {
        Column c = new Column ("<label>", "@id44");
        StringBuilder out = new StringBuilder();
        c.html(null, new Dataset("name", "Alice", "id44", "a&b"), out);
        assertEquals("generated HTML", "a&amp;b", out.toString());
    }
}
