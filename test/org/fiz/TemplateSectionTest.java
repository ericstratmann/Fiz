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

import java.io.*;

import org.fiz.test.*;

/**
 * Junit tests for the TableSection class.
 */
public class TemplateSectionTest extends junit.framework.TestCase {
    protected ClientRequest cr;
    protected Dataset state = new Dataset("name", "California",
            "country", "USA", "population", "37,000,000",
            "capital", "Sacramento", "nearestOcean", "Pacific",
            "governor", "Schwarzenegger");

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_withDataset() {
        cr.addDataRequest("getState", RawDataManager.newRequest(state));
        cr.showSections(new TemplateSection(
                new Dataset("request", "getState",
                "template", "@name's capital: @capital")));
        assertEquals("generated HTML", "California's capital: Sacramento",
                cr.getHtml().getBody().toString());
    }
    public void test_constructor_withDatasetNoRequest() {
        cr.showSections(new TemplateSection(
                new Dataset("template", "name: @name")));
        assertEquals("generated HTML", "name: Alice",
                cr.getHtml().getBody().toString());
    }

    public void test_constructor_withOneString() {
        cr.showSections(new TemplateSection("name: @name"));
        assertEquals("generated HTML", "name: Alice",
                cr.getHtml().getBody().toString());
    }

    public void test_constructor_withTwoStrings() {
        cr.addDataRequest("getState", RawDataManager.newRequest(state));
        cr.showSections(new TemplateSection("getState",
                "capital: @capital, height: @height"));
        assertEquals("generated HTML", "capital: Sacramento, height: 66",
                cr.getHtml().getBody().toString());
    }

    public void test_render_errorInRequest() {
        cr.addDataRequest("error", RawDataManager.newError(new Dataset(
                "message", "sample <error>", "value", "47")));
        Config.setDataset("styles", new Dataset("test111",
                "error for @name: @message"));
        cr.showSections(new TemplateSection(new Dataset(
                "request", "error",
                "template", "name: @name, height: @height",
                "errorStyle", "test111")));
        assertEquals("generated HTML",
                "error for Alice: sample &lt;error&gt;",
                cr.getHtml().getBody().toString());
    }
    public void test_render_errorInRequest_defaultHandler() {
        cr.addDataRequest("error", RawDataManager.newError(new Dataset(
                "message", "sample <error>", "value", "47")));
        Config.setDataset("styles", new Dataset("TemplateSection",
                new Dataset("error", "error: @message")));
        cr.showSections(new TemplateSection("error",
                "name: @name, height: @height"));
        assertEquals("generated HTML", "error: sample &lt;error&gt;",
                cr.getHtml().getBody().toString());
    }
    public void test_render_withRequest() {
        cr.addDataRequest("getState", RawDataManager.newRequest(state));
        cr.showSections(new TemplateSection("getState",
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: California, height: 66",
                cr.getHtml().getBody().toString());
    }
    public void test_render_withoutRequest() {
        cr.showSections(new TemplateSection(
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: Alice, height: 66",
                cr.getHtml().getBody().toString());
    }
    public void test_render_templateInFile() {
        (new File("_testData_/WEB-INF")).mkdirs();
        TestUtil.writeFile("_testData_/WEB-INF/template",
                "@name is @height inches tall.");
        ServletContextFixture context = (ServletContextFixture)
                cr.getServletContext();
        context.contextRoot = "_testData_/";
        cr.showSections(new TemplateSection(new Dataset(
                "file", "template")));
        assertEquals("generated HTML", "Alice is 66 inches tall.",
                cr.getHtml().getBody().toString());
        TestUtil.deleteTree("_testData_");
    }
}
