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

package org.fiz.section;

import org.fiz.*;
import org.fiz.test.*;

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

    public void test_render_defaultClassNoInitialValue() {
        ClientRequest cr = new ClientRequestFixture();
        TextAreaFormElement element = new TextAreaFormElement(
                "id11", "label22");
        element.render(cr, new Dataset());
        assertEquals("HTML", "<textarea id=\"id11\" name=\"id11\" " +
                "class=\"TextAreaFormElement\" rows=\"10\"></textarea>",
                cr.getHtml().getBody().toString());
        assertEquals("CSS includes", "TextAreaFormElement.css",
                cr.getHtml().getCssFiles());
    }
    public void test_render_explicitClassAndRowsAndInitialValue() {
        ClientRequest cr = new ClientRequestFixture();
        TextAreaFormElement element = new TextAreaFormElement(
                new Dataset("id", "id11", "class", "xyzzy",
                "rows", "6"));
        element.render(cr, new Dataset("id11", "Line 1\n<Line 2>\n"));
        assertEquals("HTML", "<textarea id=\"id11\" name=\"id11\" " +
                "class=\"xyzzy\" rows=\"6\">Line 1\n" +
                "&lt;Line 2&gt;\n" +
                "</textarea>",
                cr.getHtml().getBody().toString());
    }
}
