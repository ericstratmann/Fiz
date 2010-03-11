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
 * Junit tests for the TableSection class.
 */

public class TableSectionTest extends junit.framework.TestCase {
    protected ClientRequest cr;
    protected Dataset people = new Dataset(
            "record", new Dataset("name", "Alice", "age", "24",
            "height", "65", "ssn", "242-88-1702", "weight", "110"),
            "record", new Dataset("name", "Bill", "age", "41",
            "height", "73","weight", "195"),
            "record", new Dataset("name", "Carol", "age", "12",
            "height", "60", "weight", "85"),
            "record", new Dataset("name", "David", "age", "66",
            "height", "71", "weight", "220"));
    protected Dataset person = new Dataset(
            "record", new Dataset("name", "David", "age", "66",
            "height", "71", "weight", "220"));
    protected Dataset errorData = new Dataset("message", "sample <error>",
            "value", "47");

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_noData() {
        boolean gotException = false;
        try {
            new TableSection(new Dataset());
        }
        catch (org.fiz.InternalError e) {
            assertEquals("exception message",
                    "TableSection constructor invoked without a " +
                    "\"data\" property",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_render_basics() {
        TableSection table = new TableSection(
                new Dataset("data", people),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        cr.showSections(table);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection -->\n" +
                "<table class=\"TableSection\" cellspacing=\"0\">\n" +
                "  <tr class=\"header\">\n" +
                "    <td class=\"left\">Name</td>\n" +
                "    <td>Age</td>\n" +
                "    <td class=\"right\">Weight</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\">Alice</td>\n" +
                "    <td>24</td>\n" +
                "    <td class=\"right\">110</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"odd\">\n" +
                "    <td class=\"left\">Bill</td>\n" +
                "    <td>41</td>\n" +
                "    <td class=\"right\">195</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\">Carol</td>\n" +
                "    <td>12</td>\n" +
                "    <td class=\"right\">85</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"last odd\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td>66</td>\n" +
                "    <td class=\"right\">220</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_includeCss() {
        TableSection table = new TableSection(
                new Dataset("data", person, "id", "id.44"));
        cr.showSections(table);
        TestUtil.assertSubstring("CSS files requested",
                "TableSection.css",
                cr.getHtml().getCssFiles());
    }
    public void test_render_dontIncludeCss() {
        TableSection table = new TableSection(
                new Dataset("data", person, "class", "special"));
        cr.showSections(table);
        TestUtil.assertSubstring("CSS files requested",
                "", cr.getHtml().getCssFiles());
    }
    public void test_render_idAndClass() {
        TableSection table = new TableSection(
                new Dataset("data", person, "id", "id.44",
                "class", "<class>"),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertSubstring("table prolog",
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"&lt;class&gt;\" " +
                "cellspacing=\"0\">\n",
                html);
        TestUtil.assertSubstring("table prolog",
                "<!-- End TableSection id.44 -->\n",
                html);
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_noHeader() {
        TableSection table = new TableSection(
                new Dataset("data", person, "id", "id.44",
                "noHeader", "true"),
                new Column("Name", "@name"),
                new Column("Age", "@age"));
        cr.showSections(table);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"last even\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_header_columnIsFormatter() {
        TableSection table = new TableSection(
                new Dataset("data", person, "id", "id.44"),
                new Link(new Dataset("text", "@name",
                        "url", "/a/b?name=@name")),
                new Column("Age", "@age"));
        cr.showSections(table);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"header\">\n" +
                "    <td class=\"left\"></td>\n" +
                "    <td class=\"right\">Age</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"last even\">\n" +
                "    <td class=\"left\"><a href=\"/a/b?name=" +
                "David\">David</a></td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_header_cancelHeaderRow() {
        TableSection table = new TableSection(
                new Dataset("data", person, "id", "id.44"),
                new Column("", "@name"),
                new Column("", "@age"));
        cr.showSections(table);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"last even\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_errorInData() {
        // The error template requests data from both the error report and
        // the main dataset, to make sure that both are available.
        Config.setDataset("styles", new Dataset("TableSection",
                new Dataset("error",
                "Error in @sectionType for @name: @message")));
        Dataset data = new Dataset();
        data.setError(new Dataset("message", "sample <error>"));
        TableSection table = new TableSection(
                new Dataset("data", data),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.render(cr);
        TestUtil.assertSubstring("row with empty message",
               "  <tr class=\"error\">\n" +
               "    <td colspan=\"3\">Error in table for Alice: " +
               "sample &lt;error&gt;</td>\n" +
               "  </tr>\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_emptyWithTemplate() {
        TableSection table = new TableSection(
                new Dataset("data", new Dataset("sample", "value"),
                "emptyTemplate", "No data for @name"),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.render(cr);
        TestUtil.assertSubstring("row with empty message",
               "  <tr class=\"empty\">\n" +
                "    <td colspan=\"3\">No data for Alice</td>\n" +
                "  </tr>\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_emptyUseDefaultTemplate() {
        TableSection table = new TableSection(
                new Dataset("data", new Dataset("sample", "value")),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.render(cr);
        TestUtil.assertSubstring("wrote with empty message",
               "  <tr class=\"empty\">\n" +
                "    <td colspan=\"3\">There are no records to display</td>\n" +
                "  </tr>\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_render_includeMainDataset() {
        // This test makes sure that the data passed to each Column includes
        // both the row data and the main dataset.
        TableSection table = new TableSection(
                new Dataset("data", person, "id", "id.44",
                "noHeader", "true"),
                new Column("Name", "@name from @state"),
                new Column("Age", "@age"));
        cr.showSections(table);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"last even\">\n" +
                "    <td class=\"left\">David from California</td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }

    public void test_printTd() {
        StringBuilder out = new StringBuilder();
        TableSection table = new TableSection(
                new Dataset("data", people),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Religion", "@religion"),
                new Column("Social Security", "@ssn"));
        table.printTd(0, out);
        assertEquals("first column", "    <td class=\"left\">",
                out.toString());
        out.setLength (0);
        table.printTd(1, out);
        assertEquals("middle column", "    <td>",
                out.toString());
        out.setLength (0);
        table.printTd(3, out);
        assertEquals("right column", "    <td class=\"right\">",
                out.toString());
    }
}
