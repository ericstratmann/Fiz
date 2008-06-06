package org.fiz;

/**
 * Junit tests for the CheckboxFormElement class.
 */
public class CheckboxFormElementTest extends junit.framework.TestCase {
    public void test_constructor() {
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "44", "trueValue", "111",
                "falseValue", "000"));
        assertEquals("explicit trueValue", "111", element.trueValue);
        assertEquals("explicit falseValue", "000", element.falseValue);
        element = new CheckboxFormElement(new Dataset("id", "44"));
        assertEquals("default trueValue", "true", element.trueValue);
        assertEquals("default falseValue", "false", element.falseValue);
    }

    public void test_constructor_withIdAndLabel() {
        CheckboxFormElement element = new CheckboxFormElement(
                "id11", "label22");
        assertEquals("properties dataset", "id:    id11\n" +
                "label: label22\n", element.properties.toString());
    }

    public void test_collect() {
        CheckboxFormElement element = new CheckboxFormElement(
                "id11", "label22");
        Dataset out = new Dataset();
        element.collect(null, new Dataset(), out);
        assertEquals("no value in input", "id11: false\n", out.toString());
        out.clear();
        element.collect(null, new Dataset("id11", "1"), out);
        assertEquals("non-true value in input", "id11: false\n",
                out.toString());
        out.clear();
        element.collect(null, new Dataset("id11", "true"), out);
        assertEquals("true value in input", "id11: true\n",
                out.toString());
    }

    public void test_html_initialValueSupplied() {
        ClientRequest cr = new ClientRequestFixture();
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "trueValue", "111"));
        StringBuilder out = new StringBuilder();
        element.html(cr, new Dataset("id11", "111"), out);
        assertEquals("HTML", "<input type=\"checkbox\" " +
                "class=\"CheckboxFormElement\" name=\"id11\" " +
                "value=\"true\" checked=\"checked\" />",
                out.toString());
        assertEquals("CSS includes", "CheckboxFormElement.css",
                cr.getHtml().getCssFiles());
    }
    public void test_html_defaultInitialValue() {
        ClientRequest cr = new ClientRequestFixture();
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "defaultValue", "true"));
        StringBuilder out = new StringBuilder();
        element.html(cr, new Dataset(), out);
        assertEquals("HTML", "<input type=\"checkbox\" " +
                "class=\"CheckboxFormElement\" name=\"id11\" " +
                "value=\"true\" checked=\"checked\" />",
                out.toString());
    }
    public void test_html_explicitClassNoInitialValue() {
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "class", "xyzzy"));
        StringBuilder out = new StringBuilder();
        element.html(new ClientRequestFixture(), new Dataset(), out);
        assertEquals("HTML", "<input type=\"checkbox\" " +
                "class=\"xyzzy\" name=\"id11\" value=\"true\" />",
                out.toString());
    }
}
