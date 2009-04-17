package org.fiz;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import org.apache.log4j.*;

/**
 * Junit tests for the ClientRequest class.
 */

public class ClientRequestTest extends junit.framework.TestCase {
    protected ServletRequestFixture servletRequest;
    protected ClientRequestFixture cr;

    public void setUp() {
        cr = new ClientRequestFixture();
        servletRequest = (ServletRequestFixture) cr.getServletRequest();
    }

    public void test_addMessageToBulletin() {
        // The first call should clear the existing bulletin.
        cr.addMessageToBulletin("name: @name", new Dataset("name", "<Alice>"),
                "xyzzy");
        assertEquals("javascript after first add",
                "Fiz.clearBulletin();\n" +
                "Fiz.addBulletinMessage(\"xyzzy\", \"name: &lt;Alice&gt;\");\n",
                cr.getHtml().jsCode.toString());
        assertEquals("js files in Html object",
                "fizlib/Fiz.js",
                cr.getHtml().getJsFiles());

        // The second call should add on without clearing the bulletin again.
        cr.addMessageToBulletin("message #2", null, "class2");
        assertEquals("javascript after second add",
                "Fiz.clearBulletin();\n" +
                "Fiz.addBulletinMessage(\"xyzzy\", \"name: &lt;Alice&gt;\");\n" +
                "Fiz.addBulletinMessage(\"class2\", \"message #2\");\n",
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
                "Fiz.clearBulletin();\n" +
                "Fiz.addBulletinMessage(\"bulletinError\", \"error: first\");\n" +
                "Fiz.addBulletinMessage(\"bulletinError\", \"error: second\");\n" +
                "Fiz.addBulletinMessage(\"bulletinError\", \"error: third\");\n",
                cr.getHtml().jsCode.toString());
    }

    public void test_checkReminder() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        Reminder reminder = new Reminder("id44", "name16", "name", "first",
                "id", "66");
        StringBuilder data = new StringBuilder("reminder.");
        data.append(reminder.get(cr));
        servletRequest.setInput(data.toString());
        assertEquals("reminder exists", "id:   66\n" +
                "name: first\n", cr.checkReminder("name16").toString());
        assertEquals("reminder doesn't exist", null,
                cr.checkReminder("bogus"));
    }

    public void test_evalJavascript_normalRequest() {
        cr.evalJavascript("var x = \"@value\";");
        assertEquals("Javascript in HTML",
                "var x = \"@value\";",
                cr.getHtml().jsCode.toString());
        assertEquals("ClientRequest.jsCode", null, cr.jsCode);
    }
    public void test_evalJavascript_formPost() {
        cr.setClientRequestType(ClientRequest.Type.POST);
        cr.evalJavascript("var x = \"@value\";");
        assertEquals("Javascript in HTML", "",
                cr.getHtml().jsCode.toString());
        assertEquals("ClientRequest.jsCode", "var x = \"@value\";",
                cr.jsCode.toString());
        cr.evalJavascript("var y = 44;", null);
        assertEquals("ClientRequest.jsCode",
                "var x = \"@value\";var y = 44;",
                cr.jsCode.toString());
    }

    public void test_evalJavascript_template_normalRequest() {
        Dataset d = new Dataset("value", "<&\n\t>");
        cr.evalJavascript("var x = \"@value\";", d);
        assertEquals("Javascript in HTML",
                "var x = \"<&\\n\\t>\";",
                cr.getHtml().jsCode.toString());
        assertEquals("ClientRequest.jsCode", null, cr.jsCode);
    }
    public void test_evalJavascript_template_ajaxRequest() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        Dataset d = new Dataset("value", "<&\n\t>");
        cr.evalJavascript("var x = \"@value\";", d);
        assertEquals("Javascript in HTML", "",
                cr.getHtml().jsCode.toString());
        assertEquals("ClientRequest.jsCode", "var x = \"<&\\n\\t>\";",
                cr.jsCode.toString());
        cr.evalJavascript("var y = 44;", null);
        assertEquals("ClientRequest.jsCode",
                "var x = \"<&\\n\\t>\";var y = 44;",
                cr.jsCode.toString());
    }

    public void test_finish_returnFile_text() {
        cr.getHtml().getBody().append("<html>Some text</html>");
        byte b[] = "lorem ipsum".getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        cr.returnFile("somefile.xyz", is);
        cr.finish();
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        assertTrue("response", Arrays.equals(b, response.getBytes()));
    }
    public void test_finish_returnFile_textNoName() {
        byte b[] = "lorem ipsum".getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        cr.returnFile(null, is);
        cr.finish();
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        assertTrue("response", Arrays.equals(b, response.getBytes()));
        assertEquals("MIME type", "application/octet-stream",
                response.contentType);
    }
    public void test_finish_returnFile_binary() throws IOException {
        // Read file into a byte array.
        byte b[] = {1, 2, -128, 0, -1, -2};
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        cr.returnFile("test.xls", is);
        cr.finish();
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        assertTrue("response", Arrays.equals(b, response.getBytes()));
        assertEquals("MIME type", "application/vnd.ms-excel",
                response.contentType);
        assertEquals("response headers", "filename=\"test.xls\"",
                response.headers);
    }
    public void test_finish_returnFile_ignoreAjax() {
        byte b[] = "lorem ipsum".getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        cr.returnFile("somefile.xyz", is);
        cr.finish();
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        assertEquals("response", "lorem ipsum", response.toString());
    }
    public void test_finish_exceptionGettingPrintWriter() {
        StringAppender appender = new StringAppender();
        ClientRequest.logger = Logger.getRootLogger();
        ClientRequest.logger.removeAllAppenders();
        ClientRequest.logger.addAppender(appender);
        ((ServletResponseFixture)
                cr.getServletResponse()).getWriterException = true;
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        cr.evalJavascript("x = 33;");
        cr.finish();
        assertEquals("log output", "I/O error retrieving response writer " +
                "in ClientRequest.finish: getWriter failed",
                appender.log.toString());
    }
    public void test_finish_javascriptForAjax() throws IOException {
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        cr.evalJavascript("if (x < y) alert(\"error!\");");
        cr.finish();
        assertEquals("response",
                "if (x < y) alert(\"error!\");",
                response.toString());
    }
    public void test_finish_javascriptForPost() throws IOException {
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        cr.setClientRequestType(ClientRequest.Type.POST);
        cr.evalJavascript("if (x < y) alert(\"error!\");");
        cr.finish();
        TestUtil.assertMatchingSubstring("response",
                "Fiz.FormSection.handleResponse(" +
                "\"if (x < y) alert(\\\"error!\\\");\");",
                response.toString(), "Fiz.FormSection[^\n]*");
    }
    public void test_finish_html() throws IOException {
        PrintWriter writer = cr.getServletResponse().getWriter();
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        cr.getHtml().getBody().append("page body");
        cr.finish();
        TestUtil.assertSubstring("response",
                "</head>\n" +
                "<body>\n" +
                "page body</body>\n" +
                "</html>",
                response.toString());
    }

    public void test_getClientRequestType() {
        assertEquals(" NORMAL", ClientRequest.Type.NORMAL,
                cr.getClientRequestType());
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        assertEquals("AJAX", ClientRequest.Type.AJAX,
                cr.getClientRequestType());
        cr.setClientRequestType(ClientRequest.Type.POST);
        assertEquals("POST", ClientRequest.Type.POST,
                cr.getClientRequestType());
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

    public void test_getMainDataset_mainDatasetAlreadyExists() {
        assertEquals("value from Main dataset", "California",
                cr.getMainDataset().get("state"));
    }
    public void test_getMainDataset_ajaxData() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.contentType = "text/fiz";
        servletRequest.setInput(
                "main.(2.p2(1.a3.999\n1.b2.88)\n4.name5.Alice)");
        assertEquals("main dataset contents", "name: Alice\n" +
                "p1:   param_value1\n" +
                "p2:   param_value2\n", cr.getMainDataset().toString());
    }
    public void test_getMainDataset_queryData() {
        cr.mainDataset = null;          // Discard default info from fixture.
        servletRequest.parameterMap = new Hashtable<String,String>();
        servletRequest.parameterMap.put("name", "Alice");
        servletRequest.parameterMap.put("favorites", "multiple");
        assertEquals("contents of main dataset",
                "favorites:\n" +
                "  - value: value1\n" +
                "  - value: value2\n" +
                "  - value: value3\n" +
                "name: Alice\n", cr.getMainDataset().toString());
    }

    public void test_getReminder_noSuchReminder() {
        cr.clearData();                 // Discard default info from fixture.
        cr.requestDataProcessed = true;
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
        servletRequest.setInput(data.toString());
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

    public void test_getUploadedFile_noMultipartData() {
        assertEquals("fieldName doesn't exist", null,
                cr.getUploadedFile("bogus"));
    }
    public void test_getUploadedFile_noSuchUpload() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Alice\r\n" +
                "--xyzzy--\r\n");
        assertEquals("fieldName doesn't exist", null,
                cr.getUploadedFile("name"));
    }
    public void test_getUploadedFile_fileExists() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Alice\r\n" +
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"first\"; " +
                "filename=\"file1.txt\"\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Line 1\n" +
                "Line 2\r\n" +
                "--xyzzy--\r\n");
        assertEquals("contents of uploaded file", "Line 1\nLine 2",
                cr.getUploadedFile("first").getString());
    }

    @SuppressWarnings("deprecation")
    public void test_returnFile() {
        InputStream is = new StringBufferInputStream("sample text");
        cr.returnFile("abcdef", is);
        assertEquals("fileName", "abcdef", cr.fileName);
        assertEquals("fileSource", is, cr.fileSource);
    }

    public void test_isAjax() {
        assertEquals("initially false", false, cr.isAjax());
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        assertEquals("set to true", true, cr.isAjax());
        cr.setClientRequestType(ClientRequest.Type.POST);
        assertEquals("set to false", false, cr.isAjax());
    }

    public void test_isPost() {
        assertEquals("initially false", false, cr.isPost());
        cr.setClientRequestType(ClientRequest.Type.POST);
        assertEquals("set to true", true, cr.isPost());
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        assertEquals("set to false", false, cr.isPost());
    }

    public void test_redirect_normal() {
        ServletResponseFixture response =
                (ServletResponseFixture) cr.getServletResponse();
        cr.redirect("/a/b/c");
        assertEquals("log information", "sendRedirect(\"/a/b/c\")",
                response.log.toString());
    }
    public void test_redirect_normalException() {
        StringAppender appender = new StringAppender();
        ClientRequest.logger = Logger.getRootLogger();
        ClientRequest.logger.removeAllAppenders();
        ClientRequest.logger.addAppender(appender);
        ServletResponseFixture response =
                (ServletResponseFixture) cr.getServletResponse();
        response.sendRedirectException = true;
        cr.redirect("/a/b/c");
        assertEquals("log message",
                "I/O error in ClientRequest.redirect: fake IOException " +
                "for testing",
                appender.log.toString());
    }
    public void test_redirect_ajaxOrPost() {
        cr.setClientRequestType(ClientRequest.Type.POST);
        cr.evalJavascript("this code should be cleared");
        cr.redirect("/a/\"b\"/c");
        assertEquals("javascript response",
                "document.location.href = \"/a/\\\"b\\\"/c\";\n",
                cr.jsCode.toString());
    }

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

    public void test_saveUploadedFile_noMultipartData() {
        assertEquals("fieldName doesn't exist", false,
                cr.saveUploadedFile("bogus", "a/b/c"));
    }
    public void test_saveUploadedFile_noSuchUpload() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Alice\r\n" +
                "--xyzzy--\r\n");
        assertEquals("fieldName doesn't exist", false,
                cr.saveUploadedFile("name", "a/b/c"));
    }
    public void test_saveUploadedFile_saveFile() throws FileNotFoundException {
        (new File("_test1_")).mkdir();
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Alice\r\n" +
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"first\"; " +
                "filename=\"file1.txt\"\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Line 1\n" +
                "Line 2\r\n" +
                "--xyzzy--\r\n");
        assertEquals("return value", true,
                cr.saveUploadedFile("first", "_test1_/xyz"));
        assertEquals("contents of saved file", "Line 1\nLine 2",
                Util.readFile("_test1_/xyz").toString());
        TestUtil.deleteTree("_test1_");
    }

    public void test_setReminder() {
        cr.setReminder("first", new Dataset("name", "Alice"));
        cr.setReminder("second", new Dataset("name", "Bob"));
        assertEquals("contents of first reminder", "name: Alice\n",
                cr.getReminder("first").toString());
        assertEquals("contents of second reminder", "name: Bob\n",
                cr.getReminder("second").toString());
    }

    public void test_setRequestType() {
        assertEquals("initially NORMAL", false, cr.isAjax());
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        assertEquals("set to AJAX", true, cr.isAjax());
        cr.setClientRequestType(ClientRequest.Type.NORMAL);
        assertEquals("set to NORMAL", false, cr.isAjax());
    }

    public void test_showErrorInfo_multipleDatasets() {
        Config.setDataset("styles", new Dataset("style",
                "error: @message\n"));
        cr.showErrorInfo("style", null,
                new Dataset("message", "error 1"),
                new Dataset("message", "error 2"),
                new Dataset("message", "error 3"));
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
        assertEquals("Javascript code",
                "Fiz.addBulletinMessage(\"Bulletin: sample &lt;error&gt;\");",
                cr.getHtml().jsCode.toString());
        assertEquals("js files in Html object", "fizlib/Fiz.js",
                cr.getHtml().getJsFiles());
    }
    public void test_showErrorInfo_html() {
        Config.setDataset("styles", new Dataset("test", new Dataset(
                "style",
                "<div class=\"error\">@message (from @name)</div>")));
        cr.showErrorInfo("test.style", null,
                new Dataset("message", "sample <error>"));
        assertEquals("generated HTML",
                "<div class=\"error\">sample &lt;error&gt; (from Alice)</div>",
                cr.getHtml().getBody().toString());
    }
    public void test_showErrorInfo_bulletin() {
        Config.setDataset("styles", new Dataset("test", new Dataset(
                "123-bulletin", "Bulletin: @message (from @name)")));
        cr.showErrorInfo("test.123", "sample",
                new Dataset("message", "sample <error>"));
        assertEquals("Javascript code",
                "Fiz.addBulletinMessage(\"Bulletin: sample &lt;error&gt; " +
                "(from Alice)\");",
                cr.getHtml().jsCode.toString());
        assertEquals("js files in Html object", "fizlib/Fiz.js",
                cr.getHtml().getJsFiles());
    }
    public void test_showErrorInfo_noTemplates() {
        Config.setDataset("styles", new Dataset("test", "abc"));
        cr.setClientRequestType(ClientRequest.Type.AJAX);
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

    public void test_updateElement() {
        cr.updateElement("table\"41\"", "<p>\"<Quoted>\"</p>");
        assertEquals("generated javascript",
                "document.getElementById(\"table\\\"41\\\"\").innerHTML " +
                "= \"<p>\\\"<Quoted>\\\"</p>\";\n",
                cr.getHtml().jsCode.toString());
    }

    public void test_updateSections() {
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
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        cr.updateSections("id44", section1, "id55", section2);
        assertEquals("response Javascript",
                "document.getElementById(\"id44\").innerHTML = " +
                "\"state: California\";\n" +
                "document.getElementById(\"id55\").innerHTML = " +
                "\"capital: Sacramento\";\n",
                cr.jsCode.toString());
        assertEquals("don't leave permanent modifications in HTML body",
                "Original text", cr.getHtml().getBody().toString());
    }

    public void test_readAjaxData_basics() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.setInput("main.(2.p2(1.a3.999\n1.b2.88)\n4.name5.Alice)");
        cr.readAjaxData();
        assertEquals("main dataset contents", "name: Alice\n" +
                "p2:\n" +
                "    a: 999\n" +
                "    b: 88\n", cr.getMainDataset().toString());
    }
    public void test_readAjaxData_exceptionReadingAjaxData() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.contentType = "text/fiz";
        boolean gotException = false;
        try {
            cr.readAjaxData();
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "I/O error in ClientRequest.readAjaxData: simulated error",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readAjaxData_multipleBlocks() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.setInput("main.(4.name5.Alice)main.(3.age2.24)");
        cr.readAjaxData();
        assertEquals("main dataset contents", "age:  24\n" +
                "name: Alice\n", cr.getMainDataset().toString());
    }
    public void test_readAjaxData_missingDotAfterType() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.contentType = "text/fiz";
        servletRequest.setInput("main.()foobar");
        boolean gotException = false;
        try {
            cr.readAjaxData();
        }
        catch (SyntaxError e) {
            assertEquals("exception message",
                    "missing \".\" after type \"foobar\" in Fiz browser data",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readAjaxData_reminders() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
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
        servletRequest.setInput(data.toString());
        cr.readAjaxData();
        assertEquals("number of reminders", 2, cr.reminders.size());
        assertEquals("contents of first reminder", "id:   66\n" +
                "name: first\n", cr.reminders.get("first").toString());
        assertEquals("contents of second reminder", "id:   88\n" +
                "name: second\n", cr.reminders.get("second").toString());
    }
    public void test_readAjaxData_unknownType() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.contentType = "text/fiz";
        servletRequest.setInput("bogus.()foobar");
        boolean gotException = false;
        try {
            cr.readAjaxData();
        }
        catch (SyntaxError e) {
            assertEquals("exception message",
                    "unknown type \"bogus\" in Fiz browser data",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_readMultipartFormData_basics() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"field1\"\r\n" +
                "\r\n" +
                "field1_value\r\n" +
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"field2\"\r\n" +
                "\r\n" +
                "field2_value\r\n" +
                "--xyzzy--\r\n");
        cr.readMultipartFormData();
        assertEquals("main dataset contents",
                "field1: field1_value\n" +
                "field2: field2_value\n", cr.getMainDataset().toString());
    }
    public void test_readMultipartFormData_uploadTempDirectory() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        (new File("_test1_")).mkdir();
        Config.setDataset("main", new Dataset("uploadTempDirectory",
                "_test1_"));
        cr.testSizeThreshold = 5;
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"first\"; " +
                "filename=\"file1.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Please write this file to disk.\r\n" +
                "--xyzzy--\r\n");
        cr.readMultipartFormData();
        assertEquals("files in temp directory", 1,
                (new File("_test1_")).list().length);
        TestUtil.deleteTree("_test1_");
    }
    public void test_readMultipartFormData_bogusUploadMaxSize() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        Config.setDataset("main", new Dataset("uploadMaxSize", "xyz"));
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"first\"; " +
                "filename=\"file1.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Line 1 is much much too long\r\n" +
                "--xyzzy--\r\n");
        boolean gotException = false;
        try {
            cr.readMultipartFormData();
        }
        catch (InternalError e) {
            assertEquals("uploadMaxSize element in main configuration " +
                    "dataset has bad value \"xyz\": must be an integer",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readMultipartFormData_fileUploadMaxSize() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        Config.setDataset("main", new Dataset("uploadMaxSize", "5"));
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"first\"; " +
                "filename=\"file1.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Line 1 is much much too long\r\n" +
                "--xyzzy--\r\n");
        boolean gotException = false;
        try {
            cr.readMultipartFormData();
        }
        catch (UserError e) {
            assertEquals("uploaded file exceeded length limit of 5 bytes",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readMultipartFormData_multipleValuesForName() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Alice\r\n" +
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Bob\r\n" +
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Carol\r\n" +
                "--xyzzy--\r\n");
        cr.readMultipartFormData();
        assertEquals("main dataset contents",
                "name:\n" +
                "  - value: Alice\n" +
                "  - value: Bob\n" +
                "  - value: Carol\n", cr.getMainDataset().toString());
    }
    public void test_readMultipartFormData_fileUploads() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Alice\r\n" +
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"first\"; " +
                "filename=\"file1.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Line 1\r\n" +
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"second\"; " +
                "filename=\"file2.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Line 2\r\n" +
                "--xyzzy--\r\n");
        cr.readMultipartFormData();
        assertEquals("main dataset contents",
                "name: Alice\n", cr.getMainDataset().toString());
        Object[] keys = cr.uploads.keySet().toArray();
        Arrays.sort(keys);
        assertEquals("uploads HashMap", "first, second",
                StringUtil.join(keys, ", "));
    }
    public void test_readMultipartFormData_exceptionParsingInput() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput("--xyzzy\r\n");
        boolean gotException = false;
        try {
            cr.readMultipartFormData();
        }
        catch (InternalError e) {
            assertEquals("exception message", "error reading multi-part " +
                    "form data: stream ended unexpectedly",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_readRequestData_requestDataProcessed() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset("name", "Bob");
        cr.requestDataProcessed = true;
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.setInput("main.(4.name5.Alice)");
        cr.readRequestData();
        assertEquals("main dataset", "name: Bob\n",
                cr.getMainDataset().toString());
    }
    public void test_readRequestData_createMainDataset() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = null;
        servletRequest.setInput("main.(4.name5.Alice)");
        cr.readRequestData();
        assertEquals("main dataset", "",
                cr.getMainDataset().toString());
    }
    public void test_readRequestData_noMimeType() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset("name", "Bob");
        servletRequest.setParameters();
        servletRequest.contentType = null;
        servletRequest.setInput("main.(4.name5.Alice)");
        cr.readRequestData();
        assertEquals("main dataset", "name: Bob\n",
                cr.getMainDataset().toString());
    }
    public void test_readRequestData_formData() {
        cr.clearData();                 // Discard default info from fixture.
        servletRequest.setParameters();
        servletRequest.contentType = "multipart/form-data, boundary=xyzzy";
        servletRequest.setInput(
                "--xyzzy\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Alice\r\n" +
                "--xyzzy--\r\n");
        cr.readRequestData();
        assertEquals("main dataset contents",
                "name: Alice\n", cr.getMainDataset().toString());
    }
    public void test_readRequestData_fizData() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset("age", "18");
        servletRequest.setParameters();
        servletRequest.contentType = "text/fiz";
        servletRequest.setInput("main.(4.name5.Alice)");
        cr.readRequestData();
        assertEquals("main dataset", "age:  18\n" +
                "name: Alice\n",
                cr.getMainDataset().toString());
    }
    public void test_readRequestData_unknownMimeType() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset("name", "Bob");
        servletRequest.setParameters();
        servletRequest.contentType = "text/plain";
        servletRequest.setInput("main.(4.name5.Alice)");
        cr.readRequestData();
        assertEquals("main dataset", "name: Bob\n",
                cr.getMainDataset().toString());
    }

    public void test_uniqueId() {
        assertEquals("First Id", cr.uniqueId("foo"), "foo0");
        assertEquals("Second Id", cr.uniqueId("foo"), "foo1");
        assertEquals("Different base", cr.uniqueId("bar"), "bar0");
    }
}

