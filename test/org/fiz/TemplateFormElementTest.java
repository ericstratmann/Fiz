package org.fiz;

/**
 * Junit tests for the TemplateFormElement class.
 */
public class TemplateFormElementTest extends junit.framework.TestCase {
    // No tests for constructor: nothing interesting to test.

    public void test_render() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "template", "name: @name"));
        StringBuilder out = new StringBuilder();
        element.render(null, new Dataset("name", "<Alice>"), out);
        assertEquals("generated HTML", "name: &lt;Alice&gt;",
                out.toString());
    }

    public void test_renderLabel_span() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "label", "name: @name", "span", "true"));
        StringBuilder out = new StringBuilder();
        assertEquals ("renderLabel return value", false,
                element.renderLabel(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "", out.toString());
    }
    public void test_renderLabel_noSpan() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "label", "name: @name"));
        StringBuilder out = new StringBuilder();
        assertEquals ("renderLabel return value", true,
                element.renderLabel(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "name: &lt;Alice&gt;", out.toString());
    }
    public void test_renderLabel_noSpanNoLabel() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11"));
        StringBuilder out = new StringBuilder();
        assertEquals ("renderLabel return value", true,
                element.renderLabel(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "", out.toString());
    }
}
