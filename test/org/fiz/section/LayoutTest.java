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
 * Junit tests for the Layout class.
 */

public class LayoutTest extends junit.framework.TestCase {
    public static Layout layout;
    public static ClientRequest cr;
    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_addsData() {
        layout = new Layout(new Dataset("data", new Dataset("a", "b")));
        assertEquals("b", layout.getData(cr).getString("a"));
    }

    public void test_addData() {
        layout = new Layout(new Dataset());
        layout.addData(new Dataset("a", "b", "c", "d"));
        assertEquals("First data added", "b", layout.getData(cr).getString("a"));

        layout.addData(new Dataset("a", "e"));
        assertEquals("New data should replace old data", "e",
                     layout.getData(cr).getString("a"));
        assertEquals("Old data sould still be accessible", "d",
                     layout.getData(cr).getString("c"));
    }

    public void test_render_1arg() {
        layout = new Layout(new Dataset("format", "<h1>@foo</h1>"));
        layout.addData(new Dataset("foo", new TemplateSection("bar")));
        layout.render(cr);
        assertEquals("<h1>bar</h1>", cr.getHtml().getBody().toString());
    }

    public void test_render_2args() {
        layout = new Layout(new Dataset("format", "<h2>@a</h2>"));
        layout.render(cr, new Dataset("a", "b"));
        assertEquals("<h2>b</h2>", cr.getHtml().getBody().toString());
    }

    public void test_findFormat_file() {
        (new File("_testData_/WEB-INF")).mkdirs();
        TestUtil.writeFile("_testData_/WEB-INF/template", "hello");

        ServletContextFixture context = (ServletContextFixture)
            cr.getServletContext();
        context.contextRoot = "_testData_/";

        layout = new Layout(new Dataset("file", "template"));
        String result = layout.findFormat(cr);
        TestUtil.deleteTree("_testData_");

        assertEquals("hello", result);
    }

    public void test_findFormat_fileDoesNotExist() {
        (new File("_testData_/WEB-INF")).mkdirs();

        ServletContextFixture context = (ServletContextFixture)
            cr.getServletContext();
        context.contextRoot = "_testData_/";

        try {
            layout = new Layout(new Dataset("file", "template"));
            layout.findFormat(cr);
        } catch (FileNotFoundError e) {
            assertEquals("couldn't find layout format file \"template\" in " +
                         "path (\"_testData_//WEB-INF\")", e.getMessage());
        } finally {
            TestUtil.deleteTree("_testData_");
        }
    }

    public void test_findFormat_string() {
        layout = new Layout(new Dataset("format", "bye"));
        String result = layout.findFormat(cr);
        assertEquals("bye", result);
    }

    public void test_findFormat_noFormat() {
        layout = new Layout(new Dataset());
        try {
            String result = layout.findFormat(cr);
            fail("Error not thrown");
        } catch (org.fiz.InternalError e) {
            assertEquals("Format string not found in Layout", e.getMessage());
        }
    }

    public void test_getData() {
        layout.addData(new Dataset("name", "Sue"));
        // ClientRequestFixture uses "name" and "age" in its dummy
        // main dataset, but the original name is Alice
        assertEquals("Sue", layout.getData(cr).getString("name"));
        assertEquals("36", layout.getData(cr).getString("age"));
    }

}
