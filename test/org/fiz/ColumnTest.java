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

/**
 * Junit tests for the Column class.
 */

public class ColumnTest extends junit.framework.TestCase {
    public void test_constructor_labelAndTemplate() {
        Column c = new Column ("label111", "@name");
        assertEquals("label value", "label111", c.label);
        assertEquals("template value", "@name", c.template);
    }

    public void test_constructor_labelAndFormatter() {
        Link link = new Link(new Dataset("text", "click here",
                "url", "/a/b"));
        Column c = new Column ("label111", link);
        assertEquals("label value", "label111", c.label);
        assertEquals("formatter value", link, c.formatter);
    }

    public void test_render_formatter() {
        Link link = new Link(new Dataset("text", "click here",
                "url", "/a/b/@name"));
        Column c = new Column ("<label>", link);
        StringBuilder out = new StringBuilder();
        c.render(null, new Dataset("name", "Alice"), out);
        assertEquals("generated HTML",
                "<a href=\"/a/b/Alice\">click here</a>", out.toString());
    }
    public void test_render_template() {
        Column c = new Column ("<label>", "@id44");
        StringBuilder out = new StringBuilder();
        c.render(null, new Dataset("name", "Alice", "id44", "a&b"), out);
        assertEquals("generated HTML", "a&amp;b", out.toString());
    }

    public void test_renderHeader() {
        Column c = new Column ("<label>", "id44");
        StringBuilder out = new StringBuilder();
        c.renderHeader(null, out);
        assertEquals("generated HTML", "&lt;label&gt;", out.toString());
    }
}
