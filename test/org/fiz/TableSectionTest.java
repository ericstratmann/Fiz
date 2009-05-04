package org.fiz;

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

    public void test_constructor_noRequest() {
        boolean gotException = false;
        try {
            TableSection table = new TableSection(new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "TableSection constructor invoked without a " +
                    "\"request\" property",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_html_basics() {
        cr.addDataRequest("getPeople", RawDataManager.newRequest(people));
        TableSection table = new TableSection(
                new Dataset("request", "getPeople"),
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
                "  <tr class=\"odd\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td>66</td>\n" +
                "    <td class=\"right\">220</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_includeCss() {
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44"));
        cr.showSections(table);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertSubstring("CSS files requested",
                "TableSection.css",
                cr.getHtml().getCssFiles());
    }
    public void test_html_dontIncludeCss() {
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "class", "special"));
        cr.showSections(table);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertSubstring("CSS files requested",
                "", cr.getHtml().getCssFiles());
    }
    public void test_html_idAndClass() {
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44",
                "class", "<class>"),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.html(cr);
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
    public void test_html_noHeader() {
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44",
                "noHeader", "true"),
                new Column("Name", "@name"),
                new Column("Age", "@age"));
        cr.showSections(table);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_header_columnIsFormatter() {
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44"),
                new Link(new Dataset("text", "@name",
                        "url", "/a/b?name=@name")),
                new Column("Age", "@age"));
        cr.showSections(table);
        String html = cr.getHtml().getBody().toString();
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"header\">\n" +
                "    <td class=\"left\"></td>\n" +
                "    <td class=\"right\">Age</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\"><a href=\"/a/b?name=" +
                "David\">David</a></td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_header_cancelHeaderRow() {
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44"),
                new Column("", "@name"),
                new Column("", "@age"));
        cr.showSections(table);
        String html = cr.getHtml().getBody().toString();
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_errorInRequest() {
        // The error template requests data from both the error report and
        // the main dataset, to make sure that both are available.
        cr.addDataRequest("error", RawDataManager.newError(new Dataset(
                "message", "sample <error>", "value", "47")));
        Config.setDataset("styles", new Dataset("TableSection",
                new Dataset("error",
                "Error in @sectionType for @name: @message")));
        TableSection table = new TableSection(
                new Dataset("request", "error"),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertSubstring("row with empty message",
               "  <tr class=\"error\">\n" +
               "    <td colspan=\"3\">Error in table for Alice: " +
               "sample &lt;error&gt;</td>\n" +
               "  </tr>\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_emptyWithTemplate() {
        cr.addDataRequest("getNothing", RawDataManager.newRequest(
                new Dataset("sample", "value")));
        TableSection table = new TableSection(
                new Dataset("request", "getNothing",
                "emptyTemplate", "No data for @name"),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertSubstring("row with empty message",
               "  <tr class=\"empty\">\n" +
                "    <td colspan=\"3\">No data for Alice</td>\n" +
                "  </tr>\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_emptyUseDefaultTemplate() {
        cr.addDataRequest("getNothing", RawDataManager.newRequest(
                new Dataset("sample", "value")));
        TableSection table = new TableSection(
                new Dataset("request", "getNothing"),
                new Column("Name", "@name"),
                new Column("Age", "@age"),
                new Column("Weight", "@weight"));
        table.html(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertSubstring("wrote with empty message",
               "  <tr class=\"empty\">\n" +
                "    <td colspan=\"3\">There are no records to display</td>\n" +
                "  </tr>\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_includeMainDataset() {
        // This test makes sure that the data passed to each Column includes
        // both the row data and the main dataset.
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44",
                "noHeader", "true"),
                new Column("Name", "@name from @state"),
                new Column("Age", "@age"));
        cr.showSections(table);
        String html = cr.getHtml().getBody().toString();
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\">David from California</td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    public void test_html_lastRowClass() {
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44",
                "noHeader", "true", "lastRowClass", "last"),
                new Column("Name", "@name"),
                new Column("Age", "@age"));
        cr.showSections(table);
        String html = cr.getHtml().getBody().toString();
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"TableSection\" " +
                "cellspacing=\"0\">\n" +
                "  <tr class=\"last\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td class=\"right\">66</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection id.44 -->\n",
                cr.getHtml().getBody().toString());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }

    public void test_printTd() {
        cr.addDataRequest("getPeople", RawDataManager.newRequest(people));
        StringBuilder out = new StringBuilder();
        TableSection table = new TableSection(
                new Dataset("request", "getPeople"),
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
