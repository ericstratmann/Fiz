package org.fiz;

/**
 * Junit tests for the TableSection class.
 */
public class TemplateSectionTest extends junit.framework.TestCase {
    public void test_constructor_withDataset() {
        ClientRequest clientRequest = TestUtil.setUp();
        clientRequest.showSections(new TemplateSection(
                new Dataset("request", "getState",
                "template", "@name's capital: @capital")));
        assertEquals("generated HTML", "California's capital: Sacramento",
                clientRequest.getHtml().getBody().toString());
    }
    public void test_constructor_withDatasetNoRequest() {
        ClientRequest clientRequest = TestUtil.setUp();
        clientRequest.showSections(new TemplateSection(
                new Dataset("template", "name: @name")));
        assertEquals("generated HTML", "name: Alice",
                clientRequest.getHtml().getBody().toString());
    }

    public void test_constructor_withOneString() {
        ClientRequest clientRequest = TestUtil.setUp();
        clientRequest.showSections(new TemplateSection("name: @name"));
        assertEquals("generated HTML", "name: Alice",
                clientRequest.getHtml().getBody().toString());
    }

    public void test_constructor_withTwoStrings() {
        ClientRequest clientRequest = TestUtil.setUp();
        clientRequest.showSections(new TemplateSection("getState",
                "capital: @capital, height: @height"));
        assertEquals("generated HTML", "capital: Sacramento, height: 66",
                clientRequest.getHtml().getBody().toString());
    }

    public void test_html_withRequest() {
        ClientRequest clientRequest = TestUtil.setUp();
        clientRequest.showSections(new TemplateSection("getState",
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: California, height: 66",
                clientRequest.getHtml().getBody().toString());
    }
    public void test_html_withoutRequest() {
        ClientRequest clientRequest = TestUtil.setUp();
        clientRequest.showSections(new TemplateSection(
                "name: @name, height: @height"));
        assertEquals("generated HTML", "name: Alice, height: 66",
                clientRequest.getHtml().getBody().toString());
    }

    public void test_registerRequests() {
        ClientRequest clientRequest = TestUtil.setUp();
        Section section = new TemplateSection("abc");
        section.registerRequests(clientRequest);
        assertEquals("registered requests", "",
                clientRequest.getRequestNames());

        section = new TemplateSection("getState", "abc");
        section.registerRequests(clientRequest);
        assertEquals("registered requests", "getState",
                clientRequest.getRequestNames());
    }
}
