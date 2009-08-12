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
 * Junit tests for the ImageSelector class
 */

public class ImageSelectorTest extends junit.framework.TestCase {
    protected ClientRequest cr;
    protected StringBuilder out;
    protected Dataset testRow;
    protected ImageSelector is;

    public void setUp() {
        cr = new ClientRequestFixture();
        out = new StringBuilder();
        testRow = new Dataset("foo", "bar");
    }


    public void test_constructor_properties() {
        Dataset data = new Dataset("id", "foo", "map",
            new Dataset("bar", "baz"));
        is = new ImageSelector(data);
        assertEquals("foo", is.id);
        assertEquals("baz", is.map.get("bar"));
    }

    public void test_constructor_emptyProperties() {
        try {
            is = new ImageSelector(new Dataset());
        } catch (Dataset.MissingValueError e) {
            return;
        }
        fail("Expected exception not thrown");
    }

    public void test_constructor_separate() {
        is = new ImageSelector("foo", new Dataset("bar", "baz"));
        assertEquals("foo", is.id);
        assertEquals("baz", is.map.get("bar"));
    }

    public void test_render_keyNotInDataset() {
        Dataset data = new Dataset("id", "baz", "map", new Dataset());
        is = new ImageSelector(data);
        try {
            is.render(cr, testRow, out);
        } catch (Dataset.MissingValueError e) {
            assertEquals("couldn't find dataset element \"baz\"",
                e.getMessage());
            return;
        }
        fail("Expected exception not thrown");
    }

    public void test_render_keyNotFoundNoDefault() {
        Dataset data = new Dataset("id", "foo", "map", new Dataset());
        is = new ImageSelector(data);
        try {
            is.render(cr, testRow, out);
        } catch (Dataset.MissingValueError e) {
            assertEquals("couldn't find dataset element \"bar\"",
                e.getMessage());
            return;
        }
        fail("Expected exception not thrown");
    }

    public void test_render_keyNotFoundUseDefault() {
        Dataset data = new Dataset("id", "foo", "map", new Dataset(
                "default", "default.png"));
        is = new ImageSelector(data);
        is.render(cr, testRow, out);
        assertEquals("<img src=\"default.png\" alt=\"bar\" />",
                out.toString());

    }

    public void test_render_valueIsString() {
        Dataset data = new Dataset("id", "foo", "map", new Dataset(
                "bar", "bar.png"));
        is = new ImageSelector(data);
        is.render(cr, testRow, out);
        assertEquals("<img src=\"bar.png\" alt=\"bar\" />", out.toString());
    }

    public void test_render_valueIsDatasetNoSrc() {
        Dataset data = new Dataset("id", "foo", "map", new Dataset(
                "bar", new Dataset()));
        is = new ImageSelector(data);
        try {
            is.render(cr, testRow, out);
        } catch (Dataset.MissingValueError e) {
            assertEquals("couldn't find dataset element \"src\"",
                e.getMessage());
            return;
        }
        fail("Expected exception not thrown");
    }

    public void test_render_valueIsDatasetWithAlt() {
        Dataset data = new Dataset("id", "foo", "map", new Dataset(
                "bar", new Dataset(
                "src", "bar.png", "alt", "baz")));
        is = new ImageSelector(data);
        is.render(cr, testRow, out);
        assertEquals("<img src=\"bar.png\" alt=\"baz\" />", out.toString());
    }

    public void test_render_valueIsDatasetWithoutAlt() {
        Dataset data = new Dataset("id", "foo", "map", new Dataset(
                "bar", new Dataset("src", "bar.png")));
        is = new ImageSelector(data);
        is.render(cr, testRow, out);
        assertEquals("<img src=\"bar.png\" alt=\"bar\" />", out.toString());
    }

    public void test_render_useTemplate() {
        Dataset map = new Dataset("bar", new Dataset(
                "src", "@a", "alt", "@a"));

        // It's a weird template to make sure escaping works properly
        testRow.set("a", "a <\"~");
        is = new ImageSelector("foo", map);
        is.render(cr, testRow, out);
        assertEquals("<img src=\"a+%3c%22%7e\" alt=\"a &lt;&quot;~\" />",
            out.toString());
    }
}
