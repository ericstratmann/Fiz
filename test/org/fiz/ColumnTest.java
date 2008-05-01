package org.fiz;

/**
 * Junit tests for the Column class.
 */

public class ColumnTest extends junit.framework.TestCase {
    public void test_constructor_labelAndId() {
        Column c = new Column ("label111", "@name");
        assertEquals("label value", "label111", c.label);
        assertEquals("template value", "@name", c.template);
    }

    public void test_headerHtml() {
        Column c = new Column ("<label>", "id44");
        StringBuilder out = new StringBuilder();
        c.headerHtml(out, null);
        assertEquals("generated HTML", "&lt;label&gt;", out.toString());
    }

    public void test_html() {
        Column c = new Column ("<label>", "@id44");
        StringBuilder out = new StringBuilder();
        c.html(new Dataset("name", "Alice", "id44", "a&b"), out, null);
        assertEquals("generated HTML", "a&amp;b", out.toString());
    }
}
