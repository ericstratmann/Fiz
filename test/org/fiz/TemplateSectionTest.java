package org.fiz;

/**
 * Junit tests for the TableSection class.
 */
public class TemplateSectionTest extends junit.framework.TestCase {
    public void test_constructor_withDataset() {
        ClientRequest cr = TestUtil.setUp();
        cr.showSections(new TemplateSection(
                new Dataset("request", "getState",
                "template", "@name's capital: @capital")));
        assertEquals("generated HTML", "California's capital: Sacramento",
                cr.getHtml().getBody().toString());
    }
    public void test_constructor_withDatasetNoRequest() {
        ClientRequest cr = TestUtil.setUp();
        cr.showSections(new TemplateSection(
                new Dataset("template", "name: @name")));
        assertEquals("generated HTML", "name: Alice",
                cr.getHtml().getBody().toString());
    }

    public void test_constructor_withOneString() {
        ClientRequest cr = TestUtil.setUp();
        cr.showSections(new TemplateSection("name: @name"));
        assertEquals("generated HTML", "name: Alice",
                cr.getHtml().getBody().toString());
    }

    public void test_constructor_withTwoStrings() {
        ClientRequest cr = TestUtil.setUp();
        cr.showSections(new TemplateSection("getState",
                "capital: @capital, height: @height"));
        assertEquals("generated HTML", "capital: Sacramento, height: 66",
                cr.getHtml().getBody().toString());
    }

    public void test_html_withRequest() {
        ClientRequest cr = TestUtil.setUp();
        cr.showSections(new TemplateSection("getState",
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: California, height: 66",
                cr.getHtml().getBody().toString());
    }
    public void test_html_withoutRequest() {
        ClientRequest cr = TestUtil.setUp();
        cr.showSections(new TemplateSection(
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: Alice, height: 66",
                cr.getHtml().getBody().toString());
    }

    public void test_registerRequests() {
        ClientRequest cr = TestUtil.setUp();
        Section section = new TemplateSection("abc");
        section.registerRequests(cr);
        assertEquals("registered requests", "",
                cr.getRequestNames());

        section = new TemplateSection("getState", "abc");
        section.registerRequests(cr);
        assertEquals("registered requests", "getState",
                cr.getRequestNames());
    }
}
