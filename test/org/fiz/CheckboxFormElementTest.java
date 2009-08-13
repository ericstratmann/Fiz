/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz;

import org.fiz.test.*;

/**
 * Junit tests for the CheckboxFormElement class.
 */
public class CheckboxFormElementTest extends junit.framework.TestCase {
    public void test_constructor() {
        Dataset.sortOutput = true;
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "44", "trueValue", "111",
                "falseValue", "000"));
        assertEquals("explicit trueValue", "111", element.trueValue);
        assertEquals("explicit falseValue", "000", element.falseValue);
        assertEquals("explicit properties dataset",
                "falseValue: 000\n" +
                "id:         44\n" +
                "trueValue:  111\n", element.properties.toString());
        element = new CheckboxFormElement(new Dataset("id", "44"));
        assertEquals("default trueValue", "true", element.trueValue);
        assertEquals("default falseValue", "false", element.falseValue);
        assertEquals("default properties dataset",
                "id: 44\n", element.properties.toString());
    }

    public void test_constructor_withIdAndLabel() {
        Dataset.sortOutput = true;
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
        element.render(cr, new Dataset("id11", "111"));
        assertEquals("HTML", "<div class=\"CheckboxFormElement\">" +
                "<input type=\"checkbox\" id=\"id11\" name=\"id11\" " +
                "value=\"true\" checked=\"checked\" /></div>",
                cr.getHtml().getBody().toString());
        assertEquals("CSS includes", "CheckboxFormElement.css",
                cr.getHtml().getCssFiles());
    }
    public void test_render_explicitClassNoInitialValue() {
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "class", "xyzzy"));
        ClientRequest cr = new ClientRequestFixture();
        element.render(cr, new Dataset());
        assertEquals("HTML", "<div class=\"xyzzy\"><input type=\"checkbox\" " +
                "id=\"id11\" name=\"id11\" value=\"true\" /></div>",
                cr.getHtml().getBody().toString());
    }
    public void test_render_extraTemplate() {
        CheckboxFormElement element = new CheckboxFormElement(
                new Dataset("id", "id11", "extra", "extra: @name"));
        ClientRequest cr = new ClientRequestFixture();
        element.render(cr, new Dataset("name", "Alice"));
        assertEquals("HTML", "<div class=\"CheckboxFormElement\">" +
                "<input type=\"checkbox\" id=\"id11\" name=\"id11\" " +
                "value=\"true\" /><span class=\"extra\" onclick=\"" +
                "el=getElementById(&quot;id11&quot;); el.checked=" +
                "!el.checked;\">extra: Alice</span></div>",
                cr.getHtml().getBody().toString());
    }
}
