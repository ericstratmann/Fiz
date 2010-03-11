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

package org.fiz.section;

import org.fiz.*;
import org.fiz.test.*;

/**
 * Junit tests for the AutocompleteFormElement class.
 */
public class AutocompleteFormElementTest extends junit.framework.TestCase {
    protected ClientRequest cr;
    protected Dataset main;
    protected static class DataFactory  {
        public static Dataset dataMany(String query) {
            return new Dataset(
                "record", new Dataset("choice", "test1"),
                "record", new Dataset("choice", "test2"),
                "record", new Dataset("choice", "test3")
            );
        }

        public static Dataset dataNone(String query) {
            return new Dataset();
        }

        public static Dataset dataCustom(String query) {
            return new Dataset(
                "record", new Dataset("customChoice", "test1"),
                "record", new Dataset("customChoice", "test2"),
                "record", new Dataset("customChoice", "test3")
            );
        }
    }

    public void setUp() {
        cr = new ClientRequestFixture();
        main = cr.getMainDataset();
        main.clear();
        ServletRequestFixture.session = null;
    }

    public void test_constructor_properties() {
        AutocompleteFormElement element = new AutocompleteFormElement(
                new Dataset("id", "1234", "dataFactory", "getResults"));
        assertEquals("id property", "1234",
                element.pageProperty.id);
        assertEquals("dataFactory property", "getResults",
                element.pageProperty.dataFactory);
    }

    public void test_ajaxQuery_unmatchedQuery() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        AutocompleteFormElement.PageProperty p =
                new AutocompleteFormElement.PageProperty("auto",
                    "section.AutocompleteFormElementTest$DataFactory.dataMany",
                    "choice");
        cr.setPageProperty("auto", p);
        main.set("id", "auto");
        main.set("userInput", "query");

        AutocompleteFormElement.ajaxQuery(cr);
        TestUtil.assertSubstring("Ajax javascript: Dataset size 3",
                "document.getElementById(\"auto_dropdown\").innerHTML = \"" +
                "<ul id=\\\"auto_choices\\\">" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "test1</li>" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "test2</li>" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "test3</li></ul>\";\n" +
                "Fiz.ids.auto.showDropdown();\n",
                cr.getJs());
    }

    public void test_ajaxQuery_manyResults() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        AutocompleteFormElement.PageProperty p =
                new AutocompleteFormElement.PageProperty("auto",
                    "section.AutocompleteFormElementTest$DataFactory.dataMany",
                    "choice");
        cr.setPageProperty("auto", p);
        main.set("id", "auto");
        main.set("userInput", "test");
        AutocompleteFormElement.ajaxQuery(cr);
        TestUtil.assertSubstring("Ajax javascript: Dataset size 3",
                "document.getElementById(\"auto_dropdown\").innerHTML = \"" +
                "<ul id=\\\"auto_choices\\\">" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "<strong>test</strong>1</li>" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "<strong>test</strong>2</li>" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "<strong>test</strong>3</li></ul>\";\n" +
                "Fiz.ids.auto.showDropdown();\n",
                cr.getJs());
    }

    public void test_ajaxQuery_noResults() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        AutocompleteFormElement.PageProperty p =
            new AutocompleteFormElement.PageProperty("auto",
                "section.AutocompleteFormElementTest$DataFactory.dataNone",
                "choice");
        cr.setPageProperty("auto", p);
        main.set("id", "auto");
        main.set("userInput", "test");
        AutocompleteFormElement.ajaxQuery(cr);
        TestUtil.assertSubstring("Ajax javascript: Dataset size 0",
                "document.getElementById(\"auto_dropdown\").innerHTML = \"\";\n" +
                "Fiz.ids.auto.hideDropdown(true);\n",
                cr.getJs());
    }

    public void test_ajaxQuery_choiceName() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        AutocompleteFormElement.PageProperty p =
            new AutocompleteFormElement.PageProperty("auto",
                "section.AutocompleteFormElementTest$DataFactory.dataCustom",
                "customChoice");
        cr.setPageProperty("auto", p);
        main.set("id", "auto");
        main.set("userInput", "test");
        AutocompleteFormElement.ajaxQuery(cr);
        TestUtil.assertSubstring("Ajax javascript: Dataset size 3",
                "document.getElementById(\"auto_dropdown\").innerHTML = \"" +
                "<ul id=\\\"auto_choices\\\">" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "<strong>test</strong>1</li>" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "<strong>test</strong>2</li>" +
                "<li onclick=\\\"Fiz.ids.auto.selectChoice(this, true)\\\" " +
                "onmouseover=\\\"Fiz.ids.auto.highlightChoice(this)\\\">" +
                "<strong>test</strong>3</li></ul>\";\n" +
                "Fiz.ids.auto.showDropdown();\n",
                cr.getJs());
    }

    public void test_render_basics() {
        AutocompleteFormElement element = new AutocompleteFormElement(
                new Dataset(
                        "id", "auto",
                        "label", "Autocomplete",
                        "dataFactory", "getQuery"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset());
        assertEquals("generated HTML", "\n" +
                "<!-- Start AutocompleteFormElement auto -->\n" +
                "<div class=\"AutocompleteFormElement\" id=\"auto_container\">\n" +
                "  <input type=\"text\" id=\"auto\" name=\"auto\" " +
                "onkeyup=\"Fiz.ids.auto.refreshMenu()\" " +
                "onkeydown=\"Fiz.ids.auto.captureKeydown(event)\" " +
                "onblur=\"Fiz.ids.auto.hideDropdown()\" />\n" +
                "  <div id=\"auto_dropdown\" class=\"dropdown\" " +
                "onmouseover=\"Fiz.ids.auto.keepOpen = true\" " +
                "onmouseout=\"Fiz.ids.auto.keepOpen = false\"></div>\n" +
                "</div>\n" +
                "<!-- End AutocompleteFormElement auto -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\">\n");
        out.append("</form>\n");
        assertEquals("accumulated Javascript",
                "Fiz.pageId = \"1\";\n" +
                "Fiz.auth = \"JHB9AM69@$6=TAF*J \";\n" +
                "Fiz.ids.auto = new Fiz.AutocompleteFormElement(\"auto\");\n",
                cr.getHtml().getJs());
        TestUtil.assertSubstring("CSS file names", "AutocompleteFormElement.css",
                cr.getHtml().getCssFiles());
        assertEquals("Javascript file names",
                "static/fiz/Ajax.js, static/fiz/AutocompleteFormElement.js, static/fiz/Fiz.js",
                cr.getHtml().getJsFiles());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
}
