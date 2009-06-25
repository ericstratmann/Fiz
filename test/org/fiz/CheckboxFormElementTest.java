package org.fiz;

import org.fiz.test.*;

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

    public void test_render_initialValueSupplied() {
        ClientRequest cr = new ClientRequestFixture();
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "trueValue", "111"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("id11", "111"), out);
        assertEquals("HTML", "<div class=\"CheckboxFormElement\">" +
                "<input type=\"checkbox\" name=\"id11\" id=\"id11\" " +
                "value=\"true\" checked=\"checked\" /></div>",
                out.toString());
        assertEquals("CSS includes", "CheckboxFormElement.css",
                cr.getHtml().getCssFiles());
    }
    public void test_render_explicitClassNoInitialValue() {
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "class", "xyzzy"));
        StringBuilder out = new StringBuilder();
        element.render(new ClientRequestFixture(), new Dataset(), out);
        assertEquals("HTML", "<div class=\"xyzzy\"><input type=\"checkbox\" " +
                "name=\"id11\" id=\"id11\" value=\"true\" /></div>",
                out.toString());
    }
    public void test_render_extraTemplate() {
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "extra", "extra: @name"));
        StringBuilder out = new StringBuilder();
        element.render(new ClientRequestFixture(),
                new Dataset("name", "Alice"), out);
        assertEquals("HTML", "<div class=\"CheckboxFormElement\">" +
                "<input type=\"checkbox\" name=\"id11\" id=\"id11\" " +
                "value=\"true\" /><span class=\"extra\" onclick=\"" +
                "el=getElementById(&quot;id11&quot;); el.checked=" +
                "!el.checked;\">extra: Alice</span></div>",
                out.toString());
    }
}
