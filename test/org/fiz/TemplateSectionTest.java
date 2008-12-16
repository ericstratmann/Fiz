package org.fiz;

import java.io.*;

/**
 * Junit tests for the TableSection class.
 */
public class TemplateSectionTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_withDataset() {
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
        cr.showSections(new TemplateSection("getState",
                "capital: @capital, height: @height"));
        assertEquals("generated HTML", "capital: Sacramento, height: 66",
                cr.getHtml().getBody().toString());
    }

    public void test_html_errorInRequest() {
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
    public void test_html_errorInRequest_defaultHandler() {
        Config.setDataset("styles", new Dataset("TemplateSection",
                new Dataset("error", "error: @message")));
        cr.showSections(new TemplateSection("error",
                "name: @name, height: @height"));
        assertEquals("generated HTML", "error: sample &lt;error&gt;",
                cr.getHtml().getBody().toString());
    }
    public void test_html_withRequest() {
        cr.showSections(new TemplateSection("getState",
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: California, height: 66",
                cr.getHtml().getBody().toString());
    }
    public void test_html_withoutRequest() {
        cr.showSections(new TemplateSection(
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: Alice, height: 66",
                cr.getHtml().getBody().toString());
    }
    public void test_html_templateInFile() {
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
