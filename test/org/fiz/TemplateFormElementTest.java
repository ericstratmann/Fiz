package org.fiz;

/**
 * Junit tests for the TemplateFormElement class.
 */
public class TemplateFormElementTest extends junit.framework.TestCase {
    // No tests for constructor: nothing interesting to test.

    public void test_html() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "template", "name: @name"));
        StringBuilder out = new StringBuilder();
        element.html(null, new Dataset("name", "<Alice>"), out);
        assertEquals("generated HTML", "name: &lt;Alice&gt;",
                out.toString());
    }

    public void test_labelHtml_span() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "label", "name: @name", "span", "true"));
        StringBuilder out = new StringBuilder();
        assertEquals ("labelHtml return value", false,
                element.labelHtml(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "", out.toString());
    }
    public void test_labelHtml_noSpan() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "label", "name: @name"));
        StringBuilder out = new StringBuilder();
        assertEquals ("labelHtml return value", true,
                element.labelHtml(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "name: &lt;Alice&gt;", out.toString());
    }
    public void test_labelHtml_noSpanNoLabel() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11"));
        StringBuilder out = new StringBuilder();
        assertEquals ("labelHtml return value", true,
                element.labelHtml(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "", out.toString());
    }
}
