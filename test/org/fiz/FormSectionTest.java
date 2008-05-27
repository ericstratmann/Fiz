package org.fiz;

import java.io.*;

/**
 * Junit tests for the FormSection class.
 */
public class FormSectionTest extends junit.framework.TestCase {
    // The following class is a simple FormElement that makes its own
    // data request, named after its id.
    private static class FormElementFixture extends FormElement {
        public FormElementFixture(String id) {
            super(new Dataset("id", id));
        }
        public void html(ClientRequest cr, Dataset data, StringBuilder out) {
            // Do nothing.
        }
        public void registerRequests(ClientRequest cr, String query) {
            cr.registerDataRequest(query + "_" + id);
        }
    }

    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_defaultButtonStyle() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson"),
                new EntryFormElement("name", "Name:"),
                new EntryFormElement("age", "Age:"));
        assertEquals("properties", "id:      form1\n" +
                "request: getPerson\n", form.properties.toString());
        assertEquals("number of elements", 2, form.elements.length);
        assertEquals("buttonStyle", "standard", form.buttonStyle);
    }
    public void test_constructor_explicitButtonStyle() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "buttonStyle", "explicit"));
        assertEquals("buttonStyle", "explicit", form.buttonStyle);
    }

    public void test_html_requestErrorDefaultStyle() {
        Config.setDataset("errors", new Dataset(
                "formSection", "error from @name: @message"));
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "error"));
        form.registerRequests(cr);
        form.html(cr);
        assertEquals("generated HTML", "\n" +
                "<!-- Start FormSection form1 -->\n" +
                "error from Alice: sample &lt;error&gt;\n" +
                "<!-- End FormSection form1 -->\n",
                cr.getHtml().getBody().toString());
    }
    public void test_html_requestErrorExplicitStyle() {
        Config.setDataset("errors", new Dataset(
                "custom", "custom message: @message"));
        FormSection form = new FormSection(new Dataset(
                "id", "form1", "request", "error", "errorStyle", "custom"));
        form.registerRequests(cr);
        form.html(cr);
        assertEquals("generated HTML", "\n" +
                "<!-- Start FormSection form1 -->\n" +
                "custom message: sample &lt;error&gt;\n" +
                "<!-- End FormSection form1 -->\n",
                cr.getHtml().getBody().toString());
    }
    public void test_html_lookForNestedData() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson"),
                new EntryFormElement("name", "Name:"));
        cr.showSections(form);
        TestUtil.assertMatchingSubstring("form element displaying name",
                "<input type=\"text\" name=\"name\" " +
                "class=\"EntryFormElement\" value=\"David\" />",
                cr.getHtml().getBody().toString(), "<input[^>]*>");
    }
    public void test_html_dataNotNested() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getState"),
                new EntryFormElement("name", "Name:"));
        cr.showSections(form);
        TestUtil.assertMatchingSubstring("form element displaying name",
                "<input type=\"text\" name=\"name\" " +
                "class=\"EntryFormElement\" value=\"California\" />",
                cr.getHtml().getBody().toString(), "<input[^>]*>");
    }
    public void test_html_noRequest() {
        // In this case, the "name" value is supplied from the main dataset.
        FormSection form = new FormSection(
                new Dataset("id", "form1"),
                new EntryFormElement("name", "Name:"));
        cr.showSections(form);
        TestUtil.assertMatchingSubstring("form element displaying name",
                "<input type=\"text\" name=\"name\" " +
                "class=\"EntryFormElement\" value=\"Alice\" />",
                cr.getHtml().getBody().toString(), "<input[^>]*>");
    }
    public void test_html_defaultCssFile() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson"),
                new EntryFormElement("name", "Name:"));
        cr.showSections(form);
        assertEquals("CSS files", "EntryFormElement.css, FormSection.css",
                cr.getHtml().getCssFiles());
    }
    public void test_html_noCssFile() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "class", "class22"),
                new EntryFormElement("name", "Name:"));
        cr.showSections(form);
        assertEquals("CSS files", "EntryFormElement.css",
                cr.getHtml().getCssFiles());
    }
    public void test_html_elementsAndButtons() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson"),
                new EntryFormElement("name", "Name:"),
                new EntryFormElement("age", "Age:"));
        cr.showSections(form);
        assertEquals("generated HTML", "\n" +
                "<!-- Start FormSection form1 -->\n" +
                "<form id=\"form1\" class=\"FormSection\" " +
                "action=\"javascript: form_form1.post();\" " +
                "method=\"post\">\n" +
                "  <table cellspacing=\"0\">\n" +
                "    <tr>\n" +
                "      <td class=\"label\">Name:</td>\n" +
                "      <td class=\"control\"><input type=\"text\" " +
                "name=\"name\" class=\"EntryFormElement\" " +
                "value=\"David\" />" +
                "<div id=\"name.diagnostic\" class=\"diagnostic\" " +
                "style=\"display:none\"></div></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td class=\"label\">Age:</td>\n" +
                "      <td class=\"control\"><input type=\"text\" " +
                "name=\"age\" class=\"EntryFormElement\" " +
                "value=\"66\" /><div id=\"age.diagnostic\" " +
                "class=\"diagnostic\" style=\"display:none\"></div></td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "<div class=\"buttons\">\n" +
                "  <input type=\"submit\" name=\"action\" " +
                "value=\"Submit\" accesskey=\"s\"\n" +
                "         title=\"Press Alt+Shift+s to submit.\" />\n" +
                "</div>\n" +
                "</form>\n" +
                "<!-- End FormSection form1 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_noButtons() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson",
                "buttonStyle", "none"),
                new EntryFormElement("name", "Name:"));
        cr.showSections(form);
        assertEquals("generated HTML", "\n" +
                "<!-- Start FormSection form1 -->\n" +
                "<form id=\"form1\" class=\"FormSection\" " +
                "action=\"javascript: form_form1.post();\" " +
                "method=\"post\">\n" +
                "  <table cellspacing=\"0\">\n" +
                "    <tr>\n" +
                "      <td class=\"label\">Name:</td>\n" +
                "      <td class=\"control\"><input type=\"text\" " +
                "name=\"name\" class=\"EntryFormElement\" value=\"David\" " +
                "/><div id=\"name.diagnostic\" " +
                "class=\"diagnostic\" style=\"display:none\"></div></td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</form>\n" +
                "<!-- End FormSection form1 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_javascript() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson",
                "buttonStyle", "none"),
                new EntryFormElement("name", "Name:"));
        cr.showSections(form);
        assertEquals("Javascript code",
                "window.form_form1 = new Fiz.FormSection(\"form1\", " +
                "\"ajaxPost\");\n",
                cr.getHtml().jsCode.toString());
        assertEquals("Javascript files", "Ajax.js, Fiz.js, FormSection.js",
                cr.getHtml().getJsFiles());
    }

    public void test_post_collectDataAndReturnResponse() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson"),
                new EntryFormElement("name", "Name:"),
                new EntryFormElement("age", "Age:"));
        DataManagerFixture.responseData = new Dataset("response", "4567");
        Dataset main = cr.getMainDataset();
        main.set("name", "Alice");
        main.set("age", "21");
        Dataset result = form.post(cr, "fixture1");
        assertEquals("request dataset",
                "data:\n" +
                "    age:  21\n" +
                "    name: Alice\n" +
                "id:      fixture1\n" +
                "manager: fixture\n",
                DataManagerFixture.requestData.toString());
        assertEquals("returned dataset", "response: 4567\n",
                result.toString());
    }
    public void test_post_errorWithNoCulpritValue() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson"),
                new EntryFormElement("name", "Name:"),
                new EntryFormElement("age", "Age:"));
        cr.setAjax(true);
        DataManagerFixture.errorData = new Dataset("message", "<failure>");
        Dataset main = cr.getMainDataset();
        main.set("name", "Alice");
        main.set("age", "21");
        try {
            Dataset result = form.post(cr, "fixture1");
        }
        catch (FormSection.PostError e) {
        }
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("response Javascript for Ajax",
                "var actions = [{type: \"eval\", javascript: " +
                "\"Fiz.setBulletin(\\\"&lt;failure&gt;\\\");\"}, " +
                "{type: \"eval\", javascript: " +
                "\"form_form1.clearElementError();\"}",
                out.toString());
    }
    public void test_post_errorWithCulprit() {
        FormSection form = new FormSection(
                new Dataset("id", "form1", "elementErrorStyle", "style22"),
                new EntryFormElement("name", "Name:"),
                new EntryFormElement("age", "Age:"));
        cr.setAjax(true);
        // Make sure that the template references data in the main dataset,
        // to verify that it is available.
        Config.setDataset("errors", new Dataset(
                "style22", "error for @name: @message",
                "bulletin", "bulletin: @message"));
        DataManagerFixture.errorData = new Dataset("message", "<failure>",
                "culprit", "age");
        Dataset main = cr.getMainDataset();
        main.set("name", "Alice");
        main.set("age", "21");
        try {
            Dataset result = form.post(cr, "fixture1");
        }
        catch (FormSection.PostError e) {
        }
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("response Javascript for Ajax",
                "var actions = [{type: \"eval\", javascript: " +
                "\"form_form1.elementError(\\\"age\\\", \\\"error for Alice: " +
                "&lt;failure&gt;\\\");\"}, {type: \"eval\", javascript: " +
                "\"Fiz.setBulletin(\\\"bulletin: Some of the input fields " +
                "are invalid; see details below\\\");\"}",
                out.toString());
    }
    public void test_post_errorWithCulprit_defaultStyle() {
        FormSection form = new FormSection(
                new Dataset("id", "form1"),
                new EntryFormElement("name", "Name:"),
                new EntryFormElement("age", "Age:"));
        cr.setAjax(true);
        // Make sure that the template references data in the main dataset,
        // to verify that it is available.
        Config.setDataset("errors", new Dataset(
                "formElement", "default style: @message",
                "bulletin", ""));
        DataManagerFixture.errorData = new Dataset("message", "<failure>",
                "culprit", "age");
        Dataset main = cr.getMainDataset();
        try {
            Dataset result = form.post(cr, "fixture1");
        }
        catch (FormSection.PostError e) {
        }
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("response Javascript for Ajax",
                "var actions = [{type: \"eval\", javascript: " +
                "\"form_form1.elementError(\\\"age\\\", \\\"default style: " +
                "&lt;failure&gt;\\\");\"}, {type: \"eval\", " +
                "javascript: \"Fiz.setBulletin(\\\"\\\");\"}",
                out.toString());
    }
    public void test_post_errorButCulpritNotFound() {
        FormSection form = new FormSection(
                new Dataset("id", "form1"),
                new EntryFormElement("name", "Name:"),
                new EntryFormElement("age", "Age:"));
        cr.setAjax(true);
        DataManagerFixture.errorData = new Dataset("message", "<failure>",
                "culprit", "height");
        Dataset main = cr.getMainDataset();
        try {
            Dataset result = form.post(cr, "fixture1");
        }
        catch (FormSection.PostError e) {
        }
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("response Javascript for Ajax",
                "var actions = [{type: \"eval\", javascript: " +
                "\"Fiz.setBulletin(\\\"&lt;failure&gt;\\\");\"}, " +
                "{type: \"eval\", " +
                "javascript: \"form_form1.clearElementError();\"}",
                out.toString());
    }
    public void test_registeredRequests() {
        Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                "getPerson:\n" +
                "  manager: fixture\n" +
                "getPerson_id1:\n" +
                "  manager: fixture\n" +
                "getPerson_id2:\n" +
                "  manager: fixture\n"));
        FormSection form = new FormSection(
                new Dataset("id", "form1", "request", "getPerson"),
                new FormElementFixture("id1"),
                new FormElementFixture("id2"));
        form.registerRequests(cr);
        assertEquals("registered requests",
                "getPerson, getPerson_id1, getPerson_id2",
                cr.getRequestNames());
    }
    public void test_registeredRequests_noRequest() {
        FormSection form = new FormSection(
                new Dataset("id", "form1"));
        form.registerRequests(cr);
        assertEquals("registered requests", "", cr.getRequestNames());
    }
}
