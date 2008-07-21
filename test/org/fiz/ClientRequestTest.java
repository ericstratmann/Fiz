package org.fiz;
import java.io.*;

/**
 * Junit tests for the ClientRequest class.
 */

public class ClientRequestTest extends junit.framework.TestCase {
    // The following class is used to test for proper handling of exceptions
    // during I/O.
    protected static class ExceptionReader extends BufferedReader {
        public ExceptionReader(Reader reader) {
            super(reader);
        }
        public int read() throws IOException {
            throw new IOException("simulated error");
        }
    }

    protected ServletRequestFixture servletRequest;
    protected ClientRequest cr;
    protected StringWriter out;

    public void setUp() {
        cr = new ClientRequestFixture();
        servletRequest = (ServletRequestFixture) cr.getServletRequest();
    }

    public void test_addMessageToBulletin_ajax() {
        cr.setAjax(true);

        // The first call should clear the existing bulletin.
        cr.addMessageToBulletin("name: @name", new Dataset("name", "<Alice>"),
                "xyzzy");
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("ajax response after first add",
                "var actions = [{type: \"eval\", javascript: " +
                "\"Fiz.clearBulletin(); " +
                "Fiz.addBulletinMessage(\\\"xyzzy\\\", " +
                "\\\"name: &lt;Alice&gt;\\\");\"}",
                out.toString());

        // The second call should add on without clearing the bulletin again.
        cr.addMessageToBulletin("message #2", null, "class2");
        assertEquals("ajax response after second add",
                "var actions = [{type: \"eval\", javascript: " +
                "\"Fiz.clearBulletin(); " +
                "Fiz.addBulletinMessage(\\\"xyzzy\\\", " +
                "\\\"name: &lt;Alice&gt;\\\");\"}, " +
                "{type: \"eval\", javascript: \"Fiz.addBulletinMessage(" +
                "\\\"class2\\\", \\\"message #2\\\");\"}",
                out.toString());
    }
    public void test_addMessageToBulletin_html() {
        cr.addMessageToBulletin("name: @name", new Dataset("name", "<Alice>"),
                "xyzzy");
        assertEquals("Javascript code",
                "Fiz.clearBulletin(); Fiz.addBulletinMessage(\"xyzzy\", " +
                "\"name: &lt;Alice&gt;\");",
                cr.getHtml().jsCode.toString());
    }

    public void test_addErrorsToBulletin() {
        Config.setDataset("errors", new Dataset("bulletin",
                "error: @message"));
        cr.addErrorsToBulletin(
                new Dataset("message", "first"),
                new Dataset("message", "second"),
                new Dataset("message", "third"));
        assertEquals("Javascript code",
                "Fiz.clearBulletin(); " +
                "Fiz.addBulletinMessage(\"bulletinError\", " +
                "\"error: first\");Fiz.addBulletinMessage(" +
                "\"bulletinError\", \"error: second\");" +
                "Fiz.addBulletinMessage(\"bulletinError\", " +
                "\"error: third\");",
                cr.getHtml().jsCode.toString());
    }

    public void test_ajaxErrorAction() {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        Dataset properties = new Dataset("message", "catastrophe",
                "value", "92");
        cr.ajaxErrorAction(properties);
        assertEquals("response",
                "var actions = [{type: \"error\", properties: " +
                "{message: \"catastrophe\", value: \"92\"}}",
                out.toString());
    }

    public void test_ajaxEvalAction() {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        cr.ajaxEvalAction("var x = \"test\";");
        cr.ajaxEvalAction("var y = 101;");
        assertEquals("response",
                "var actions = [{type: \"eval\", " +
                "javascript: \"var x = \\\"test\\\";\"}, " +
                "{type: \"eval\", javascript: \"var y = 101;\"}",
                out.toString());
    }

    public void test_ajaxEvalAction_template() {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        Dataset d = new Dataset("value", "<&\n\t>");
        cr.ajaxEvalAction("var x = \"@value\";", d);
        assertEquals("response",
                "var actions = [{type: \"eval\", javascript: " +
                "\"var x = \\\"<&\\\\n\\\\t>\\\";\"}",
                out.toString());
    }

    public void test_ajaxRedirectAction() {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        cr.ajaxRedirectAction("/x/y/z?x=45&name=Alice");
        assertEquals("response",
                "var actions = [{type: \"redirect\", url: " +
                "\"/x/y/z?x=45&name=Alice\"}",
                out.toString());
    }

    public void test_ajaxUpdateAction() {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        cr.ajaxUpdateAction("id44", "Alice");
        cr.ajaxUpdateAction("id55", "Special: <\">");
        assertEquals("response",
                "var actions = [{type: \"update\", id: \"id44\", " +
                "html: \"Alice\"}, {type: \"update\", id: \"id55\", " +
                "html: \"Special: <\\\">\"}",
                out.toString());
    }

    public void test_finish_ajax() throws IOException {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        cr.ajaxUpdateAction("id44", "Alice");
        cr.finish();
        assertEquals("response",
                "var actions = [{type: \"update\", id: \"id44\", " +
                "html: \"Alice\"}];",
                out.toString());
    }
    public void test_finish_ajax_noActions() throws IOException {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        cr.finish();
        assertEquals("response",
                "var actions = [];",
                out.toString());
    }
    public void test_finish_html() throws IOException {
        PrintWriter writer = cr.getServletResponse().getWriter();
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.getHtml().getBody().append("page body");
        cr.finish();
        TestUtil.assertSubstring("response",
                "</head>\n" +
                "<body>\n" +
                "<script type=\"text/javascript\" src=\"/servlet/Fiz.js\">" +
                "</script>\n" +
                "page body</body>\n" +
                "</html>",
                out.toString());
    }

    public void test_getMainDataset_ajaxDataOnly() {
        cr.setAjax(true);
        cr.mainDataset = null;          // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/plain";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "2.p2(1.a3.999\n1.b2.88\n)\n4.name5.Alice\n"));
        assertEquals("main dataset contents", "name: Alice\n" +
                "p2:\n" +
                "    a: 999\n" +
                "    b: 88\n", cr.getMainDataset().toString());
    }
    public void test_getMainDataset_ajaxButWrongContentType() {
        cr.setAjax(true);
        cr.mainDataset = null;          // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "bogus";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "2.p2(1.a3.999\n1.b2.88\n)\n4.name5.Alice\n"));
        assertEquals("main dataset contents", "",
                cr.getMainDataset().toString());
    }
    public void test_getMainDataset_exceptionReadingAjaxData() {
        cr.setAjax(true);
        cr.mainDataset = null;          // Discard default info from fixture.
        servletRequest.contentType = "text/plain";
        servletRequest.inputReader = new ExceptionReader(
                new StringReader("2.p2.88\n"));
        boolean gotException = false;
        try {
            cr.getMainDataset();
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "error reading Ajax data: simulated error", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getMainDataset_queryDataOnly() {
        cr.mainDataset = null;          // Discard default info from fixture.
        assertEquals("main dataset contents", "p1: param_value1\n" +
                "p2: param_value2\n", cr.getMainDataset().toString());
    }
    public void test_getMainDataset_queryDataAndAjax() {
        cr.setAjax(true);
        cr.mainDataset = null;          // Discard default info from fixture.
        servletRequest.contentType = "text/plain";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "2.p2(1.a3.999\n1.b2.88\n)\n4.name5.Alice\n"));
        assertEquals("main dataset contents", "name: Alice\n" +
                "p1:   param_value1\n" +
                "p2:   param_value2\n", cr.getMainDataset().toString());
    }
    public void test_getMainDataset_noDataAtAll() {
        cr.mainDataset = null;          // Discard default info from fixture.
        servletRequest.setParameters();
        assertEquals("main dataset contents", "",
                cr.getMainDataset().toString());
    }

    public void test_getRequestNames() {
        assertEquals("no requests registered yet", "",
                cr.getRequestNames());
        cr.registerDataRequest("fixture1");
        cr.registerDataRequest("fixture2");
        cr.registerDataRequest("fixture1");
        cr.registerDataRequest("getPeople");
        assertEquals("names of registered requests",
                "fixture1, fixture2, getPeople",
                cr.getRequestNames());
    }

    public void test_getServletRequest() {
        ServletRequestFixture newRequest = new ServletRequestFixture();
        cr.servletRequest = newRequest;
        assertEquals(newRequest, cr.getServletRequest());
    }

    public void test_getServletResponse() {
        ServletResponseFixture newResponse = new ServletResponseFixture();
        cr.servletResponse = newResponse;
        assertEquals(newResponse, cr.getServletResponse());
    }

    public void test_getUrlPrefix_useSavedValue() {
        cr.urlPrefix = "/foo/bar";
        assertEquals("request already has a value", "/foo/bar",
                cr.getUrlPrefix());
    }
    public void test_getUrlPrefix_generateValue() {
        assertEquals("computed value", "/context/cpath/servlet/spath",
                cr.getUrlPrefix());
    }

    // isAjax is tested by the tests for setAjax.

    public void test_registerDataRequest_withName() {
        DataRequest data1 = cr.registerDataRequest("fixture1");
        DataRequest data2 = cr.registerDataRequest("fixture2");
        DataRequest data3 = cr.registerDataRequest("fixture1");
        assertEquals("count of registered requests", 2,
                cr.namedRequests.size());
        assertEquals("share duplicate requests", data1, data3);
        assertEquals("contents of request", "id:      fixture2\n" +
                "manager: fixture\n" +
                "name:    Alice\n",
                data2.getRequestData().toString());
    }

    public void test_registerDataRequest_withDataRequest() {
        DataRequest data1 = cr.registerDataRequest(
                new DataRequest(new Dataset("name", "Bill")));
        DataRequest data2 = cr.registerDataRequest(
                new DataRequest(new Dataset("name", "Carol")));
        assertEquals("count of registered requests", 2,
                cr.unnamedRequests.size());
        assertEquals("contents of request", "name: Carol\n",
                cr.unnamedRequests.get(1).getRequestData().toString());
    }

    public void test_registerDataRequest_withDatasetAndPath_nonexistentPath() {
        DataRequest request = cr.registerDataRequest(
                new Dataset("a:", "1234"), "b");
        assertEquals("no request created", null, request);
    }
    public void test_registerDataRequest_withDatasetAndPath_string() {
        DataRequest request1 = cr.registerDataRequest(
                YamlDataset.newStringInstance("a:\n  request: fixture1\n"),
                "a.request");
        DataRequest request2 = cr.registerDataRequest("fixture1");
        assertEquals("count of registered requests", 1,
                cr.namedRequests.size());
        assertEquals("contents of request", "id:      fixture1\n" +
                "manager: fixture\n",
                request1.getRequestData().toString());
        assertEquals("request is shared", request1, request2);
    }
    public void test_registerDataRequest_withDatasetAndPath_dataset() {
        DataRequest request = cr.registerDataRequest(
                YamlDataset.newStringInstance("a:\n  request:\n" +
                "    first: 16\n" +
                "    second: '@99'\n"),
                "a.request");
        assertEquals("count of registered requests", 1,
                cr.unnamedRequests.size());
        assertEquals("contents of request", "first:  16\n" +
                "second: \"@99\"\n",
                request.getRequestData().toString());
    }

    public void test_setAjax() {
        assertEquals("initially false", false, cr.isAjax());
        cr.setAjax(true);
        assertEquals("set to true", true, cr.isAjax());
        cr.setAjax(false);
        assertEquals("set to false", false, cr.isAjax());
    }

    public void test_showErrorInfo_bulletin() {
        Config.setDataset("errors", new Dataset("bulletin123",
                "Bulletin: @message (from @name)"));
        cr.showErrorInfo("bulletin123", "sample",
                new Dataset("message", "sample <error>"));
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("Javascript code",
                "Fiz.addBulletinMessage(\"Bulletin: sample &lt;error&gt; " +
                "(from Alice)\");",
                cr.getHtml().jsCode.toString());
    }
    public void test_showErrorInfo_defaultStyle() {
        Config.setDataset("errors", new Dataset("bulletin123",
                "Bulletin: @message"));
        cr.showErrorInfo(null, "bulletin123",
                new Dataset("message", "sample <error>"));
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("Javascript code",
                "Fiz.addBulletinMessage(\"Bulletin: sample &lt;error&gt;\");",
                cr.getHtml().jsCode.toString());
    }
    public void test_showErrorInfo_notBulletin() {
        Config.setDataset("errors", new Dataset("style",
                "<div class=\"error\">@message (from @name)</div>"));
        cr.showErrorInfo("style", null,
                new Dataset("message", "sample <error>"));
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("generated HTML",
                "<div class=\"error\">sample &lt;error&gt; (from Alice)</div>",
                cr.getHtml().getBody().toString());
    }
    public void test_showErrorInfo_multipleDatasets() {
        Config.setDataset("errors", new Dataset("style",
                "error: @message\n"));
        cr.showErrorInfo("style", null,
                new Dataset("message", "error 1"),
                new Dataset("message", "error 2"),
                new Dataset("message", "error 3"));
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("generated HTML",
                "error: error 1\n" +
                        "error: error 2\n" +
                        "error: error 3\n",
                cr.getHtml().getBody().toString());
    }

    public void test_showSections() {
        cr.showSections(
                new TemplateSection("first\n"),
                new TemplateSection("getState", "second: @name\n"),
                new TemplateSection("getState", "third: @capital\n"));
        assertEquals("generated HTML", "first\n" +
                "second: California\n" +
                "third: Sacramento\n",
                cr.getHtml().getBody().toString());
        assertEquals("registered requests", "getState",
                cr.getRequestNames());
    }

    public void test_startDataRequests_namedAndUnnamed() {
        DataRequest data1 = cr.registerDataRequest("fixture1");
        DataRequest data2 = cr.registerDataRequest("fixture2");
        DataRequest data3 = cr.registerDataRequest("fixture1");
        DataRequest data4 = cr.registerDataRequest(
                new DataRequest(new Dataset("manager", "fixture",
                "name", "Carol", "id", "xyzzy")));
        cr.startDataRequests();
        assertEquals("data manager log",
                "fixture started xyzzy, fixture2, fixture1",
                DataManagerFixture.getLogs());
    }
    public void test_startDataRequests_noRequests() {
        cr.startDataRequests();
        assertEquals("data manager log", "", DataManagerFixture.getLogs());
    }

    public void test_ajaxActionHeader() throws IOException {
        PrintWriter writer = cr.getServletResponse().getWriter();
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        assertEquals("initial response", "", out.toString());
        assertEquals("return value", writer, cr.ajaxActionHeader("first"));
        assertEquals("response after first header",
                "var actions = [{type: \"first\"", out.toString());
        cr.ajaxActionHeader("second");
        assertEquals("response after second header",
                "var actions = [{type: \"first\", {type: \"second\"",
                out.toString());
    }
    public void test_printWriter_exception() {
        ((ServletResponseFixture)
                cr.getServletResponse()).getWriterException = true;
        cr.setAjax(true);
        boolean gotException = false;
        try {
            cr.ajaxActionHeader("first");
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "getWriter failed", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}

