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
 * Junit tests for the HiddenFormElement class.
 */
public class HiddenFormElementTest extends junit.framework.TestCase {
    public void test_constructor() {
        HiddenFormElement element = new HiddenFormElement("id11");
        assertEquals("properties dataset", "id: id11\n",
                element.properties.toString());
    }

    public void test_render() {
        ClientRequest cr = new ClientRequestFixture();
        HiddenFormElement element = new HiddenFormElement("age");
        element.render(cr, new Dataset("age", "<confidential>"));
        assertEquals("CSS includes", "",
                cr.getHtml().getCssFiles());
        assertEquals("generated HTML",
                "<input type=\"hidden\" id=\"age\" name=\"age\" " +
                "value=\"&lt;confidential&gt;\" />",
                cr.getHtml().getBody().toString());
    }
    public void test_render_noValue() {
        ClientRequest cr = new ClientRequestFixture();
        HiddenFormElement element = new HiddenFormElement("age");
        element.render(cr, new Dataset());
        assertEquals("generated HTML",
                "<input type=\"hidden\" id=\"age\" name=\"age\" />",
                cr.getHtml().getBody().toString());
    }
}
