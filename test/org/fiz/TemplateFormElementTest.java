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
 * Junit tests for the TemplateFormElement class.
 */
public class TemplateFormElementTest extends junit.framework.TestCase {
    // No tests for constructor: nothing interesting to test.

    public void test_render() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "template", "name: @name"));
        StringBuilder out = new StringBuilder();
        element.render(null, new Dataset("name", "<Alice>"), out);
        assertEquals("generated HTML", "name: &lt;Alice&gt;",
                out.toString());
    }

    public void test_renderLabel_span() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "label", "name: @name", "span", "true"));
        StringBuilder out = new StringBuilder();
        assertEquals ("renderLabel return value", false,
                element.renderLabel(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "", out.toString());
    }
    public void test_renderLabel_noSpan() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11", "label", "name: @name"));
        StringBuilder out = new StringBuilder();
        assertEquals ("renderLabel return value", true,
                element.renderLabel(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "name: &lt;Alice&gt;", out.toString());
    }
    public void test_renderLabel_noSpanNoLabel() {
        TemplateFormElement element = new TemplateFormElement(new Dataset(
                "id", "id11"));
        StringBuilder out = new StringBuilder();
        assertEquals ("renderLabel return value", true,
                element.renderLabel(null, new Dataset("name", "<Alice>"), out));
        assertEquals("generated HTML", "", out.toString());
    }
}
