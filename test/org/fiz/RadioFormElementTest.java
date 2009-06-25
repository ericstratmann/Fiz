package org.fiz;

import org.fiz.test.*;

/**
 * Junit tests for the RadioFormElement class.
 */
public class RadioFormElementTest extends junit.framework.TestCase {
    public void test_constructor() {
        RadioFormElement element = new RadioFormElement(
                new Dataset("id", "44", "value", "111"));
        assertEquals("value property", "111", element.value);
    }

    public void test_render_basics() {
        ClientRequest cr = new ClientRequestFixture();
        RadioFormElement element = new RadioFormElement(
                new Dataset("id", "id11", "value", "222"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("id11", "111"), out);
        assertEquals("HTML", "<div class=\"RadioFormElement\">" +
                "<input type=\"radio\" name=\"id11\" id=\"id11.222\" " +
                "value=\"222\" /></div>",
                out.toString());
        assertEquals("CSS includes", "RadioFormElement.css",
                cr.getHtml().getCssFiles());
    }
    public void test_render_explicitClass() {
        ClientRequest cr = new ClientRequestFixture();
        RadioFormElement element = new RadioFormElement(
                new Dataset("id", "id11", "value", "222", "class", "xyzzy"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("id11", "111"), out);
        assertEquals("HTML", "<div class=\"xyzzy\">" +
                "<input type=\"radio\" name=\"id11\" id=\"id11.222\" " +
                "value=\"222\" /></div>",
                out.toString());
    }
    public void test_render_noInitialValue() {
        ClientRequest cr = new ClientRequestFixture();
        RadioFormElement element = new RadioFormElement(
                new Dataset("id", "id11", "value", "222"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset(), out);
        assertEquals("HTML", "<div class=\"RadioFormElement\">" +
                "<input type=\"radio\" name=\"id11\" id=\"id11.222\" " +
                "value=\"222\" /></div>",
                out.toString());
    }
    public void test_render_initiallyChecked() {
        ClientRequest cr = new ClientRequestFixture();
        RadioFormElement element = new RadioFormElement(
                new Dataset("id", "id11", "value", "222"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("id11", "222"), out);
        assertEquals("HTML", "<div class=\"RadioFormElement\">" +
                "<input type=\"radio\" name=\"id11\" id=\"id11.222\" " +
                "value=\"222\" checked=\"checked\" /></div>",
                out.toString());
    }
    public void test_render_extraTemplate() {
        ClientRequest cr = new ClientRequestFixture();
        RadioFormElement element = new RadioFormElement(
                new Dataset("id", "id11", "value", "222", "class", "xyzzy",
                        "extra", "template: @name"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("id11", "111", "name", "Alice"), out);
        assertEquals("HTML", "<div class=\"xyzzy\"><input type=\"radio\" " +
                "name=\"id11\" id=\"id11.222\" value=\"222\" /><span " +
                "class=\"extra\" onclick=\"getElementById" +
                "(&quot;id11.222&quot;).checked=true;\">" +
                "template: Alice</span></div>",
                out.toString());
    }
}
