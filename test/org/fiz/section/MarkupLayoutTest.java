/* Copyright (c) 2008-2010 Stanford University
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

import java.io.*;
import java.lang.reflect.*;

import org.fiz.*;
import org.fiz.test.*;

/**
 * Junit tests for the MarkupLayout class.
 */

public class MarkupLayoutTest extends junit.framework.TestCase {
    public static MarkupLayout markup;
    public static ClientRequest cr;
    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_string() {
        markup = new MarkupLayout("foo");
        assertEquals("foo", markup.properties.getString("format"));
    }

    public void test_render_markupTransformation() {
        markup = new MarkupLayout(new Dataset("format", "** blah **"));
        markup.render(cr);
        assertEquals("<p><strong> blah </strong></p>\n", cr.getHtml().getBody().toString());
    }

    public void test_render_atsignSubstitution() {
        markup = new MarkupLayout(new Dataset("format", "@foo @bar"));
        markup.addData(new Dataset("foo", new TemplateSection("abc"),
                                   "bar", "xyz"));
        markup.render(cr);
        assertEquals("<p>abc xyz</p>\n", cr.getHtml().getBody().toString());
    }
}
