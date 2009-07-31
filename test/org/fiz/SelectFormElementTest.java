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
 * Junit tests for the SelectFormElement class.
 */
public class SelectFormElementTest extends junit.framework.TestCase {
    protected Dataset fruits = new Dataset(
            "fruit", new Dataset("name", "Peach", "value", "peach"),
            "fruit", new Dataset("name", "Pear", "value", "pear"));

    public void test_constructor_checkMultiple() {
        // First constructor call should work fine.
        SelectFormElement element = new SelectFormElement(
                new Dataset("id", "id11", "multiple", "multiple"));
        assertEquals("multiple property value", "multiple", element.multiple);

        // Second constructor call should generate an error.
        boolean gotException = false;
        try {
            element = new SelectFormElement(
                new Dataset("id", "id11", "multiple", "silly"));
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "\"multiple\" property for SelectFormElement has " +
                    "illegal value \"silly\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_singleSelection() {
        SelectFormElement element = new SelectFormElement(
                new Dataset("id", "id11"));
        Dataset out = new Dataset();
        element.collect(null, new Dataset("id11", "California"), out);
        assertEquals("output dataset", "id11: California\n",
                out.toString());
    }
    public void test_multipleSelections() {
        SelectFormElement element = new SelectFormElement(
                new Dataset("id", "id11", "multiple", "multiple"));
        Dataset out = new Dataset();
        Dataset in = YamlDataset.newStringInstance(
                "a: 111\n" +
                "id11:\n" +
                "  - value: 123\n" +
                "  - value: 345\n");
        element.collect(null, in, out);
        assertEquals("output dataset", "id11:\n" +
                "  - value: 123\n" +
                "  - value: 345\n",
                out.toString());
         out.clear();
        element.collect(null, new Dataset(), out);
        assertEquals("output dataset (no selections)", "",
                out.toString());
    }

    public void test_render_basics() {
        SelectFormElement element = new SelectFormElement(
                YamlDataset.newStringInstance(
                "id: id11\n" +
                "choice:\n" +
                "  - name:  <Apple>\n" +
                "    value: <apple>\n" +
                "  - name:  Banana\n" +
                "    value: banana\n"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start SelectFormElement id11 -->\n" +
                "<select id=\"id11\" name=\"id11\" class=\"SelectFormElement\">\n" +
                "  <option value=\"&lt;apple&gt;\">&lt;Apple&gt;</option>\n" +
                "  <option value=\"banana\">Banana</option>\n" +
                "</select>\n" +
                "<!-- End SelectFormElement @id -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\"><p>\n");
        out.append("</p></form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_singleInitialValue() {
        SelectFormElement element = new SelectFormElement(
                YamlDataset.newStringInstance(
                "id: id11\n" +
                "choice:\n" +
                "  - name:  Apple\n" +
                "    value: apple\n" +
                "  - name:  Grape\n" +
                "    value: grape\n"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset("id11", "grape"), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start SelectFormElement id11 -->\n" +
                "<select id=\"id11\" name=\"id11\" class=\"SelectFormElement\">\n" +
                "  <option value=\"apple\">Apple</option>\n" +
                "  <option selected=\"selected\" value=\"grape\">" +
                "Grape</option>\n" +
                "</select>\n" +
                "<!-- End SelectFormElement @id -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\"><p>\n");
        out.append("</p></form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_multipleInitialValues() {
        SelectFormElement element = new SelectFormElement(
                YamlDataset.newStringInstance(
                "id: id11\n" +
                "multiple: multiple\n" +
                "choice:\n" +
                "  - name:  Apple\n" +
                "    value: apple\n" +
                "  - name:  Banana\n" +
                "    value: banana\n" +
                "  - name:  Grape\n" +
                "    value: grape\n"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, YamlDataset.newStringInstance(
                "id11:\n" +
                "  - value:  apple\n" +
                "  - value: grape\n"), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start SelectFormElement id11 -->\n" +
                "<select id=\"id11\" name=\"id11\" class=\"SelectFormElement\" " +
                "multiple=\"multiple\">\n" +
                "  <option selected=\"selected\" value=\"apple\">" +
                "Apple</option>\n" +
                "  <option value=\"banana\">Banana</option>\n" +
                "  <option selected=\"selected\" value=\"grape\">" +
                "Grape</option>\n" +
                "</select>\n" +
                "<!-- End SelectFormElement @id -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\"><p>\n");
        out.append("</p></form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_choiceNameProperty() {
        SelectFormElement element = new SelectFormElement(
                YamlDataset.newStringInstance(
                "id: id11\n" +
                "choiceName: alt\n" +
                "choice:\n" +
                "  - name:  Apple\n" +
                "    value: apple\n" +
                "alt:\n" +
                "  - name:  Banana\n" +
                "    value: banana\n"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start SelectFormElement id11 -->\n" +
                "<select id=\"id11\" name=\"id11\" class=\"SelectFormElement\">\n" +
                "  <option value=\"banana\">Banana</option>\n" +
                "</select>\n" +
                "<!-- End SelectFormElement @id -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\"><p>\n");
        out.append("</p></form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_choiceRequestError() {
        SelectFormElement element = new SelectFormElement(
                new Dataset("id", "id11", "choiceRequest", "error"));
        ClientRequest cr = new ClientRequestFixture();
        cr.addDataRequest("error", RawDataManager.newError(new Dataset(
                "message", "sample <error>", "value", "47")));
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("Javascript for HTML",
                "Fiz.addBulletinMessage(\"bulletinError\", " +
                "\"bulletin: sample &lt;error&gt;\");\n",
                cr.getHtml().jsCode.toString());
    }
    public void test_render_choiceRequestSucceeds() {
        SelectFormElement element = new SelectFormElement(
                new Dataset("id", "id11", "choiceRequest",
                        "getFruits", "choiceName", "fruit"));
        ClientRequest cr = new ClientRequestFixture();
        cr.addDataRequest("getFruits", RawDataManager.newRequest(fruits));
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start SelectFormElement id11 -->\n" +
                "<select id=\"id11\" name=\"id11\" class=\"SelectFormElement\">\n" +
                "  <option value=\"peach\">Peach</option>\n" +
                "  <option value=\"pear\">Pear</option>\n" +
                "</select>\n" +
                "<!-- End SelectFormElement @id -->\n",
                out.toString());
    }
    public void test_render_explicitClassAndHeight() {
        SelectFormElement element = new SelectFormElement(
                YamlDataset.newStringInstance(
                "id: id11\n" +
                "class: xyzzy\n" +
                "height: 5\n" +
                "choice:\n" +
                "  - name:  Apple\n" +
                "    value: apple\n"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, YamlDataset.newStringInstance(
                "id11:\n" +
                "  - value:  apple\n" +
                "  - value: grape\n"), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start SelectFormElement id11 -->\n" +
                "<select id=\"id11\" name=\"id11\" class=\"xyzzy\" size=\"5\">\n" +
                "  <option value=\"apple\">Apple</option>\n" +
                "</select>\n" +
                "<!-- End SelectFormElement @id -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\"><p>\n");
        out.append("</p></form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_choiceHasValueButNoName() {
        SelectFormElement element = new SelectFormElement(
                YamlDataset.newStringInstance(
                "id: id11\n" +
                "choice:\n" +
                "  - value: 2008\n" +
                "  - value: 2009\n"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset("id 11", "2008"), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start SelectFormElement id11 -->\n" +
                "<select id=\"id11\" name=\"id11\" class=\"SelectFormElement\">\n" +
                "  <option value=\"2008\">2008</option>\n" +
                "  <option value=\"2009\">2009</option>\n" +
                "</select>\n" +
                "<!-- End SelectFormElement @id -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\"><p>\n");
        out.append("</p></form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
}
