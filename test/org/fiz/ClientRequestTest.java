package org.fiz;

import java.io.*;
import javax.crypto.*;

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
    protected ClientRequestFixture cr;
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
        Config.setDataset("styles", new Dataset("bulletin",
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

    public void test_ajaxUpdateSections() {
        Section section1 = new TemplateSection(new Dataset(
                "template", "state: @state",
                "request", new Dataset("manager", "raw",
                "result", new Dataset("state", "California"))
        ));
        Section section2 = new TemplateSection(new Dataset(
                "template", "capital: @capital",
                "request", new Dataset("manager", "raw",
                "result", new Dataset("capital", "Sacramento"))
        ));
        cr.getHtml().getBody().append("Original text");
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        cr.ajaxUpdateSections("id44", section1, "id55", section2);
        assertEquals("response",
                "var actions = [{type: \"update\", id: \"id44\", html: " +
                "\"state: California\"}, {type: \"update\", id: \"id55\", " +
                "html: \"capital: Sacramento\"}",
                out.toString());
        assertEquals("don't leave permanent modifications in HTML body",
                "Original text", cr.getHtml().getBody().toString());
    }

    public void test_checkReminder() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        Reminder reminder = new Reminder("id44", "name16", "name", "first",
                "id", "66");
        StringBuilder data = new StringBuilder("reminder.");
        data.append(reminder.get(cr));
        servletRequest.inputReader = new BufferedReader(new StringReader(
                data.toString()));
        assertEquals("reminder exists", "id:   66\n" +
                "name: first\n", cr.checkReminder("name16").toString());
        assertEquals("reminder doesn't exist", null,
                cr.checkReminder("bogus"));
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
                "<script type=\"text/javascript\" " +
                "src=\"/servlet/fizlib/Fiz.js\">" +
                "</script>\n" +
                "page body</body>\n" +
                "</html>",
                out.toString());
    }

    public void test_getMac() {
        // Make sure that (a) the Mac object can be used to create
        // signatures and (b) the same Mac object is returned in
        // future calls for the same session.
        Mac mac = cr.getMac();
        byte[] input = ("123456789a123456789b").getBytes();
        byte[] result = mac.doFinal(input);
        Mac mac2 = cr.getMac();
        byte[] result2 = mac.doFinal(input);
        assertEquals("size of results", result.length,
                result2.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals("byte " + i + " of signature", result[i],
                    result2[i]);
        }
    }

    public void test_getMainDataset_ajaxDataOnly() {
        cr.setAjax(true);
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(2.p2(1.a3.999\n1.b2.88)\n4.name5.Alice)"));
        assertEquals("main dataset contents", "name: Alice\n" +
                "p2:\n" +
                "    a: 999\n" +
                "    b: 88\n", cr.getMainDataset().toString());
    }
    public void test_getMainDataset_ajaxButWrongContentType() {
        cr.setAjax(true);
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "bogus";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(2.p2(1.a3.999\n1.b2.88)\n4.name5.Alice)"));
        assertEquals("main dataset contents", "",
                cr.getMainDataset().toString());
    }
    public void test_getMainDataset_exceptionReadingAjaxData() {
        cr.setAjax(true);
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new ExceptionReader(
                new StringReader("2.p2.88\n"));
        boolean gotException = false;
        try {
            cr.getMainDataset();
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "I/O error in ClientRequest.readFizData: simulated error",
                    e.getMessage());
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
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(2.p2(1.a3.999\n1.b2.88)\n4.name5.Alice)"));
        assertEquals("main dataset contents", "name: Alice\n" +
                "p1:   param_value1\n" +
                "p2:   param_value2\n", cr.getMainDataset().toString());
    }
    public void test_getMainDataset_noDataAtAll() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        assertEquals("main dataset contents", "",
                cr.getMainDataset().toString());
    }

    public void test_getReminder_noSuchReminder() {
        cr.clearData();                 // Discard default info from fixture.
        cr.fizDataProcessed = true;
        boolean gotException = false;
        try {
            cr.getReminder("bogus");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "couldn't find reminder \"bogus\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getReminder_exists() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        Reminder reminder = new Reminder("id22","name16", "name", "first",
                "id", "66");
        StringBuilder data = new StringBuilder("reminder.");
        data.append(reminder.get(cr));
        servletRequest.inputReader = new BufferedReader(new StringReader(
                data.toString()));
        assertEquals("reminder data", "id:   66\n" +
                "name: first\n", cr.getReminder("name16").toString());
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

    public void test_includeJavascript_ajax() {
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        cr.setAjax(true);
        cr.includeJavascript("var x = \"test\";");
        assertEquals("response",
                "var actions = [{type: \"eval\", " +
                "javascript: \"var x = \\\"test\\\";\"}",
                out.toString());
    }
    public void test_includeJavascript_html() {
        cr.includeJavascript("var x = \"test\";");
        assertEquals("accumulated Javascript",
                "var x = \"test\";",
                 cr.getHtml().jsCode.toString());
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

    public void test_setReminder() {
        cr.setReminder("first", new Dataset("name", "Alice"));
        cr.setReminder("second", new Dataset("name", "Bob"));
        assertEquals("contents of first reminder", "name: Alice\n",
                cr.getReminder("first").toString());
        assertEquals("contents of second reminder", "name: Bob\n",
                cr.getReminder("second").toString());
    }

    public void test_showErrorInfo_multipleDatasets() {
        Config.setDataset("styles", new Dataset("style",
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
    public void test_showErrorInfo_defaultStyle() {
        Config.setDataset("styles", new Dataset("123-bulletin",
                "Bulletin: @message"));
        cr.showErrorInfo(null, "123",
                new Dataset("message", "sample <error>"));
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("Javascript code",
                "Fiz.addBulletinMessage(\"Bulletin: sample &lt;error&gt;\");",
                cr.getHtml().jsCode.toString());
    }

    public void test_showErrorInfo_html() {
        Config.setDataset("styles", new Dataset("test", new Dataset(
                "style",
                "<div class=\"error\">@message (from @name)</div>")));
        cr.showErrorInfo("test.style", null,
                new Dataset("message", "sample <error>"));
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("generated HTML",
                "<div class=\"error\">sample &lt;error&gt; (from Alice)</div>",
                cr.getHtml().getBody().toString());
    }
    public void test_showErrorInfo_bulletin() {
        Config.setDataset("styles", new Dataset("test", new Dataset(
                "123-bulletin", "Bulletin: @message (from @name)")));
        cr.showErrorInfo("test.123", "sample",
                new Dataset("message", "sample <error>"));
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("Javascript code",
                "Fiz.addBulletinMessage(\"Bulletin: sample &lt;error&gt; " +
                "(from Alice)\");",
                cr.getHtml().jsCode.toString());
    }
    public void test_showErrorInfo_noTemplates() {
        Config.setDataset("styles", new Dataset("test", "abc"));
        cr.setAjax(true);
        boolean gotException = false;
        try {
            cr.showErrorInfo("bogus", "sample",
                    new Dataset("message", "sample <error>"));
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "showErrorInfo found no \"bogus\" template for " +
                    "displaying error information", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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

    public void test_readFizData_basics() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(2.p2(1.a3.999\n1.b2.88)\n4.name5.Alice)"));
        cr.readFizData();
        assertEquals("main dataset contents", "name: Alice\n" +
                "p2:\n" +
                "    a: 999\n" +
                "    b: 88\n", cr.getMainDataset().toString());
    }
    public void test_readFizData_fizDataProcessed() {
        cr.clearData();                 // Discard default info from fixture.
        cr.fizDataProcessed = true;
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(4.name5.Alice)"));
        cr.readFizData();
        assertEquals("main dataset", null, cr.mainDataset);
    }
    public void test_readFizData_noMimeType() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = null;
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(4.name5.Alice)"));
        cr.readFizData();
        assertEquals("main dataset", null, cr.mainDataset);
    }
    public void test_readFizData_wrongMimeType() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/plain";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(4.name5.Alice)"));
        cr.readFizData();
        assertEquals("main dataset", null, cr.mainDataset);
    }
    public void test_readFizData_exceptionReadingAjaxData() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new ExceptionReader(
                new StringReader("foobar"));
        boolean gotException = false;
        try {
            cr.readFizData();
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "I/O error in ClientRequest.readFizData: simulated error",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readFizData_multipleBlocks() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.(4.name5.Alice)main.(3.age2.24)"));
        cr.readFizData();
        assertEquals("main dataset contents", "age:  24\n" +
                "name: Alice\n", cr.getMainDataset().toString());
    }
    public void test_readFizData_missingDotAfterType() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "main.()foobar"));
        boolean gotException = false;
        try {
            cr.readFizData();
        }
        catch (SyntaxError e) {
            assertEquals("exception message",
                    "missing \".\" after type \"foobar\" in Fiz browser data",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readFizData_reminders() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        Reminder reminder = new Reminder("id22","first", "name", "first",
                "id", "66");
        StringBuilder data = new StringBuilder("reminder.");
        data.append(reminder.get(cr));
        reminder = new Reminder("id33", "second", "name", "second",
                "id", "88");
        data.append("reminder.");
        data.append(reminder.get(cr));
        servletRequest.inputReader = new BufferedReader(new StringReader(
                data.toString()));
        cr.readFizData();
        assertEquals("number of reminders", 2, cr.reminders.size());
        assertEquals("contents of first reminder", "id:   66\n" +
                "name: first\n", cr.reminders.get("first").toString());
        assertEquals("contents of second reminder", "id:   88\n" +
                "name: second\n", cr.reminders.get("second").toString());
    }
    public void test_readFizData_unknownType() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.contentType = "text/fiz";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "bogus.()foobar"));
        boolean gotException = false;
        try {
            cr.readFizData();
        }
        catch (SyntaxError e) {
            assertEquals("exception message",
                    "unknown type \"bogus\" in Fiz browser data",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}

