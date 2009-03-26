package org.fiz;

/**
 * Junit tests for the HiddenFormElement class.
 */
public class HiddenFormElementTest extends junit.framework.TestCase {
    public void test_constructor() {
        HiddenFormElement element = new HiddenFormElement("id11");
        assertEquals("properties dataset", "id: id11\n",
                element.properties.toString());
    }

    public void test_html() {
        ClientRequest cr = new ClientRequestFixture();
        HiddenFormElement element = new HiddenFormElement("age");
        StringBuilder out = new StringBuilder();
        element.html(cr, new Dataset("age", "<confidential>"), out);
        assertEquals("CSS includes", "",
                cr.getHtml().getCssFiles());
        assertEquals("generated HTML",
                "<input type=\"hidden\" name=\"age\" " +
                "value=\"&lt;confidential&gt;\" />",
                out.toString());
    }
    public void test_html_noValue() {
        ClientRequest cr = new ClientRequestFixture();
        HiddenFormElement element = new HiddenFormElement("age");
        StringBuilder out = new StringBuilder();
        element.html(cr, new Dataset(), out);
        assertEquals("generated HTML",
                "<input type=\"hidden\" name=\"age\" />",
                out.toString());
    }
}
