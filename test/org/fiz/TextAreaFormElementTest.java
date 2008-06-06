package org.fiz;

/**
 * Junit tests for the TextAreaFormElement class.
 */
public class TextAreaFormElementTest extends junit.framework.TestCase {
    public void test_constructor_withIdAndLabel() {
        TextAreaFormElement element = new TextAreaFormElement(
                "id11", "label22");
        assertEquals("properties dataset", "id:    id11\n" +
                "label: label22\n", element.properties.toString());
    }

    public void test_collect() {
        TextAreaFormElement element = new TextAreaFormElement(
                "id11", "label22");
        assertEquals("properties dataset", "id:    id11\n" +
                "label: label22\n", element.properties.toString());
        Dataset out = new Dataset();
        element.collect(null, new Dataset(), out);
        assertEquals("no value in input", "", out.toString());
        out.clear();
        element.collect(null, new Dataset("id11",
                "Line 1\r\nLine 2\nLine 3\r\nLine 4\n"), out);
        assertEquals("convert CRLF to LF",
                "id11: \"Line 1\\nLine 2\\nLine 3\\nLine 4\\n\"\n",
                out.toString());
    }

    public void test_html_defaultClassNoInitialValue() {
        ClientRequest cr = new ClientRequestFixture();
        TextAreaFormElement element = new TextAreaFormElement(
                "id11", "label22");
        StringBuilder out = new StringBuilder();
        element.html(cr, new Dataset(), out);
        assertEquals("HTML", "<textarea name=\"id11\" " +
                "class=\"TextAreaFormElement\" rows=\"10\"></textarea>",
                out.toString());
        assertEquals("CSS includes", "TextAreaFormElement.css",
                cr.getHtml().getCssFiles());
    }
    public void test_html_explicitClassAndRowsAndInitialValue() {
        ClientRequest cr = new ClientRequestFixture();
        TextAreaFormElement element = new TextAreaFormElement(
                new Dataset("id", "id11", "class", "xyzzy",
                "rows", "6"));
        StringBuilder out = new StringBuilder();
        element.html(cr, new Dataset("id11", "Line 1\nLine 2\n"), out);
        assertEquals("HTML", "<textarea name=\"id11\" class=\"xyzzy\" " +
                "rows=\"6\">Line 1\n" +
                "Line 2\n" +
                "</textarea>",
                out.toString());
    }
}
