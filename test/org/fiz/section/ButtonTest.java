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
 * Junit tests for the Link class.
 */

public class ButtonTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_and_render () {
        Button button = new Button(new Dataset ("text", "Clear @name",
                "ajaxUrl", "/fiz/form/clear"));
        button.render(cr, new Dataset("name", "Alice"));
        assertEquals("HTML for Button",
                "<button onclick=\"void new Fiz.Ajax({url: " +
                "&quot;/fiz/form/clear&quot;}); return false;\">" +
                "Clear Alice</button>",
                cr.getHtml().getBody().toString());
    }

    public void test_render_static_ajaxWithClass() {
        Dataset properties = new Dataset("text", "special: @special",
                "ajaxUrl", "/fiz/a/b?user=@name", "class", "class123");
        Dataset data = new Dataset("name", "\"Alice\"", "special", "<>");
        Button.render(cr, properties, data);
        assertEquals("HTML for Button",
                "<button class=\"class123\" onclick=\"void new Fiz.Ajax(" +
                "{url: &quot;/fiz/a/b?user=%22Alice%22&quot;}); " +
                "return false;\">" +
                "special: &lt;&gt;</button>",
                cr.getHtml().getBody().toString());
    }
    public void test_render_static_javascriptNoClass() {
        Dataset properties = new Dataset("text", "special: @special",
                "javascript", "alert(\"@name\");");
        Dataset data = new Dataset("name", "\"Alice\"", "special", "<>");
        Button.render(cr, properties, data);
        assertEquals("HTML for Button",
                "<button onclick=\"alert(&quot;\\&quot;Alice\\&quot;" +
                "&quot;); return false;\">special: &lt;&gt;</button>",
                cr.getHtml().getBody().toString());
    }
}
