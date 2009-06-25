package org.fiz;

import org.fiz.test.*;

/**
 * Junit tests for the EntryFormElement class.
 */
public class EntryFormElementTest extends junit.framework.TestCase {
    public void test_constructor_withIdAndLabel() {
        EntryFormElement element = new EntryFormElement("id11", "label22");
        assertEquals("properties dataset", "id:    id11\n" +
                "label: label22\n", element.properties.toString());
    }

    public void test_html() {
        ClientRequest cr = new ClientRequestFixture();
        EntryFormElement element = new EntryFormElement("age", "Age:");
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("age", "<confidential>"), out);
        assertEquals("CSS includes", "EntryFormElement.css",
                cr.getHtml().getCssFiles());
        assertEquals("generated HTML",
                "<input type=\"text\" name=\"age\" " +
                "class=\"EntryFormElement\" " +
                "value=\"&lt;confidential&gt;\" />",
                out.toString());
    }
    public void test_render_explicitClass() {
        ClientRequest cr = new ClientRequestFixture();
        EntryFormElement element = new EntryFormElement(
                new Dataset("id", "age", "label", "Age:",
                "class", "class16"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("age", "<confidential>"), out);
        assertEquals("generated HTML",
                "<input type=\"text\" name=\"age\" " +
                "class=\"class16\" value=\"&lt;confidential&gt;\" />",
                out.toString());
    }
    public void test_render_noValue() {
        ClientRequest cr = new ClientRequestFixture();
        EntryFormElement element = new EntryFormElement(
                new Dataset("id", "age", "label", "Age:",
                "class", "class16"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML",
                "<input type=\"text\" name=\"age\" class=\"class16\" />",
                out.toString());
    }
}
