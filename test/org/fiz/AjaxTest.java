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
 * Junit tests for the Ajax class.
 */

public class AjaxTest extends junit.framework.TestCase {

    public void test_invoke_withStringBuilder() {
        ClientRequest cr = new ClientRequestFixture();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        StringBuilder out = new StringBuilder();
        Ajax.invoke(cr, "/fiz/test/alert?age=24&name=@name&weight=@weight",
                data, out);
        TestUtil.assertSubstring("set authentication token",
                "Fiz.auth =", cr.getHtml().jsCode.toString());
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/static/fiz/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax({url: \"/fiz/test/alert?age=24&" +
                "name=Alice&weight=%22110%22\"});",
                out.toString());
    }

    public void test_invoke_withoutStringBuilder() {
        ClientRequest cr = new ClientRequestFixture();
        Dataset data = new Dataset("name", "Alice", "weight", "\"110\"");
        String out = Ajax.invoke(cr,
                "/fiz/test/alert?age=24&name=@name&weight=@weight",
                data);
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/static/fiz/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax({url: \"/fiz/test/alert?age=24&" +
                "name=Alice&weight=%22110%22\"});",
                out);
    }

    public void test_invoke_withIndexedData() {
        ClientRequest cr = new ClientRequestFixture();
        String out = Ajax.invoke(cr,
                "/fiz/test/alert?age=24&name=@1&weight=@2",
                "<abcd>", "120");
        TestUtil.assertSubstring("set authentication token",
                "Fiz.auth =", cr.getHtml().jsCode.toString());
        TestUtil.assertSubstring("include Javascript file",
                "/servlet/static/fiz/Ajax.js",
                cr.getHtml().jsFileHtml.toString());
        assertEquals("generated HTML",
                "void new Fiz.Ajax({url: \"/fiz/test/alert?age=24&" +
                "name=%3cabcd%3e&weight=120\"});",
                out);
    }
}
