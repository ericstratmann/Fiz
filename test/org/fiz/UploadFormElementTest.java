package org.fiz;

import org.fiz.test.*;

/**
 * Junit tests for the UploadFormElement class.
 */
public class UploadFormElementTest extends junit.framework.TestCase {
    public void test_constructor_withIdAndLabel() {
        UploadFormElement element = new UploadFormElement("id11", "label22");
        assertEquals("properties dataset", "id:    id11\n" +
                "label: label22\n", element.properties.toString());
    }

    public void test_collect() {
        ClientRequest cr = new ClientRequestFixture();
        UploadFormElement element = new UploadFormElement("age", "Age:");
        Dataset out = new Dataset("name", "Bob");
        element.collect(null, new Dataset("name", "Carol", "age", "36"),
                out);
        assertEquals("output dataset", "name: Bob\n", out.toString());
    }

    public void test_render() {
        ClientRequest cr = new ClientRequestFixture();
        UploadFormElement element = new UploadFormElement("age", "Age:");
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("age", "<confidential>"), out);
        assertEquals("CSS includes", "UploadFormElement.css",
                cr.getHtml().getCssFiles());
        assertEquals("generated HTML",
                "<input type=\"file\" name=\"age\" " +
                "class=\"UploadFormElement\" />",
                out.toString());
    }
    public void test_render_explicitClass() {
        ClientRequest cr = new ClientRequestFixture();
        UploadFormElement element = new UploadFormElement(
                new Dataset("id", "age", "label", "Age:",
                "class", "class16"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("age", "<confidential>"), out);
        assertEquals("generated HTML",
                "<input type=\"file\" name=\"age\" " +
                "class=\"class16\" />",
                out.toString());
    }
}
