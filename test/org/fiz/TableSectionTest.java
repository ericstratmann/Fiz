package org.fiz;

/**
 * Junit tests for the TableSection class.
 */

public class TableSectionTest extends junit.framework.TestCase {
    public void test_html_basics() {
        ClientRequest clientRequest = TestUtil.setUp();
        TableSection table = new TableSection(
                new Dataset("request", "getPeople"),
                new Column("Name", "name"),
                new Column("Age", "age"),
                new Column("Weight", "weight"));
        table.registerRequests(clientRequest);
        table.html(clientRequest);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TableSection -->\n" +
                "<table class=\"TableSection\" cellspacing=\"0\">\n" +
                "  <tr class=\"header\">\n" +
                "    <td class=\"left\">Name</td>\n" +
                "    <td>Age</td>\n" +
                "    <td class=\"right\">Weight</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"odd\">\n" +
                "    <td class=\"left\">Alice</td>\n" +
                "    <td>24</td>\n" +
                "    <td class=\"right\">110</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\">Bill</td>\n" +
                "    <td>41</td>\n" +
                "    <td class=\"right\">195</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"odd\">\n" +
                "    <td class=\"left\">Carol</td>\n" +
                "    <td>12</td>\n" +
                "    <td class=\"right\">85</td>\n" +
                "  </tr>\n" +
                "  <tr class=\"even\">\n" +
                "    <td class=\"left\">David</td>\n" +
                "    <td>66</td>\n" +
                "    <td class=\"right\">220</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TableSection -->\n",
                clientRequest.getHtml().getBody().toString());
        TestUtil.assertXHTML(clientRequest.getHtml().toString());
    }
    public void test_html_idAndClass() {
        ClientRequest clientRequest = TestUtil.setUp();
        TableSection table = new TableSection(
                new Dataset("request", "getPerson", "id", "id.44",
                "class", "<class>"),
                new Column("Name", "name"),
                new Column("Age", "age"),
                new Column("Weight", "weight"));
        table.registerRequests(clientRequest);
        table.html(clientRequest);
        String html = clientRequest.getHtml().getBody().toString();
        TestUtil.assertSubstring("table prolog",
                "<!-- Start TableSection id.44 -->\n" +
                "<table id=\"id.44\" class=\"&lt;class&gt;\" " +
                "cellspacing=\"0\">\n",
                html);
        TestUtil.assertSubstring("table prolog",
                "<!-- End TableSection id.44 -->\n",
                html);
        TestUtil.assertXHTML(clientRequest.getHtml().toString());
    }
    public void test_html_errorInRequest() {
        ClientRequest clientRequest = TestUtil.setUp();
        TableSection table = new TableSection(
                new Dataset("request", "error",
                "errorTemplate", "Error for @name: @message"),
                new Column("Name", "name"),
                new Column("Age", "age"),
                new Column("Weight", "weight"));
        table.registerRequests(clientRequest);
        table.html(clientRequest);
        String html = clientRequest.getHtml().getBody().toString();
        TestUtil.assertSubstring("row with empty message",
               "  <tr class=\"error\">\n" +
               "    <td colspan=\"3\">Error for Alice: unknown request " +
               "&quot;bogus&quot; for FileDataManager; must be " +
               "create, read, update, or delete</td>\n" +
               "  </tr>\n",
                clientRequest.getHtml().getBody().toString());
        TestUtil.assertXHTML(clientRequest.getHtml().toString());
    }
    public void test_html_emptyWithTemplate() {
        ClientRequest clientRequest = TestUtil.setUp();
        TableSection table = new TableSection(
                new Dataset("request", "getNothing",
                "emptyTemplate", "No data for @name"),
                new Column("Name", "name"),
                new Column("Age", "age"),
                new Column("Weight", "weight"));
        table.registerRequests(clientRequest);
        table.html(clientRequest);
        String html = clientRequest.getHtml().getBody().toString();
        TestUtil.assertSubstring("row with empty message",
               "  <tr class=\"empty\">\n" +
                "    <td colspan=\"3\">No data for Alice</td>\n" +
                "  </tr>\n",
                clientRequest.getHtml().getBody().toString());
        TestUtil.assertXHTML(clientRequest.getHtml().toString());
    }
    public void test_html_emptyUseDefaultTemplate() {
        ClientRequest clientRequest = TestUtil.setUp();
        TableSection table = new TableSection(
                new Dataset("request", "getNothing"),
                new Column("Name", "name"),
                new Column("Age", "age"),
                new Column("Weight", "weight"));
        table.registerRequests(clientRequest);
        table.html(clientRequest);
        String html = clientRequest.getHtml().getBody().toString();
        TestUtil.assertSubstring("wrote with empty message",
               "  <tr class=\"empty\">\n" +
                "    <td colspan=\"3\">There are no records to display</td>\n" +
                "  </tr>\n",
                clientRequest.getHtml().getBody().toString());
        TestUtil.assertXHTML(clientRequest.getHtml().toString());
    }

    public void test_registerRequests() {
        ClientRequest clientRequest = TestUtil.setUp();
        TableSection table = new TableSection(new Dataset(
                "request", "getPeople"));
        table.registerRequests(clientRequest);
        assertEquals("names of registered requests", "getPeople",
                clientRequest.getRequestNames());
    }

    public void test_printTd() {
        StringBuilder out = new StringBuilder();
        TableSection table = new TableSection(
                new Dataset("request", "getPeople"),
                new Column("Name", "name"),
                new Column("Age", "age"),
                new Column("Religion", "religion"),
                new Column("Social Security", "ssn"));
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
