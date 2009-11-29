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

import java.io.*;
import java.util.*;
import javax.crypto.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.*;
import org.fiz.test.*;

/**
 * Junit tests for the ClientRequest class.
 */

public class ClientRequestTest extends junit.framework.TestCase {
    protected static class SectionFixture extends TemplateSection {
        public static StringBuilder log = new StringBuilder();
        public SectionFixture(String s1) {
            super(s1);
        }
        public SectionFixture(String s1, String s2) {
            super(s1, s2);
        }
        public SectionFixture(Dataset d) {
            super(d);
        }
        public void addDataRequests(ClientRequest cr) {
            log.append("data request \"" +
                    ((properties != null) ? properties.get("request")
                    : "none") + "\";");
        }
    }
    
    // Dummy FileUpload class, used in tests of ClientRequest.readFileUpload().
    protected static class FileUploadFixture extends FileUpload {
        public boolean isFormField = false;
        public String fieldName = null;
        public FileUploadFixture(boolean isFormField, String fieldName) {
            super((FileItem)null);
            this.isFormField = isFormField;
            this.fieldName = fieldName;
        }
        public String getFieldName() {
            return fieldName;
        }
        public boolean isFormField() {
            return isFormField;
        }
        public String getString() {
            return "FileUploadFixture";
        }
    }

    protected ServletRequestFixture servletRequest;
    protected ClientRequestFixture cr;
    protected Dataset state = new Dataset("name", "California",
            "country", "USA", "population", "37,000,000",
            "capital", "Sacramento", "nearestOcean", "Pacific",
            "governor", "Schwarzenegger");

    public void setUp() {
        cr = new ClientRequestFixture();
        servletRequest = (ServletRequestFixture) cr.getServletRequest();
    }

    public void test_MissingPagePropertyError() {
        ClientRequest.MissingPagePropertyError e =
                new ClientRequest.MissingPagePropertyError("keyName");
        assertEquals("exception message",
                "couldn't find page property \"keyName\"", e.getMessage());
    }

    public void test_StalePageError() {
        ClientRequest.StalePageError e =
                new ClientRequest.StalePageError();
        assertEquals("exception message",
                "Stale page: the current page is so old that the server " +
                "has discarded its data about the page; if you want to " +
                "keep using this page please click on the refresh button",
                e.getMessage());
    }

    public void test_addDataRequest_withName() {
        DataRequest r1 = new DataRequest("test1");
        DataRequest r2 = new DataRequest("test2");
        cr.addDataRequest("request1", r1);
        cr.addDataRequest("request2", r2);
        assertEquals("number of named requests",
                2, cr.namedRequests.size());
        assertEquals("first request", r1, cr.getDataRequest("request1"));
        assertEquals("second request", r2, cr.getDataRequest("request2"));
    }

    public void test_addDataRequest_noName() {
        DataRequest r1 = new DataRequest("test1");
        DataRequest r2 = new DataRequest("test2");
        cr.addDataRequest(r1);
        cr.addDataRequest(r2);
        assertEquals("number of unnamed requests",
                2, cr.unnamedRequests.size());
        assertEquals("first request", r1, cr.unnamedRequests.get(0));
        assertEquals("second request", r2, cr.unnamedRequests.get(1));
    }

    public void test_addMessageToBulletin() {
        cr.addMessageToBulletin("name: @name", new Dataset("name", "<Alice>"));
        assertEquals("javascript",
                "Fiz.addBulletinMessage(\"name: &lt;Alice&gt;\");\n",
                cr.getHtml().jsCode.toString());
    }

    public void test_addErrorsToBulletin() {
        Config.setDataset("styles", new Dataset("bulletin",
                new Dataset("error", "error: @message")));
        cr.addErrorsToBulletin(
                new Dataset("message", "first"),
                new Dataset("message", "second"),
                new Dataset("message", "third"));
        assertEquals("Javascript code",
                "Fiz.addBulletinMessage(\"error: first\");\n" +
                "Fiz.addBulletinMessage(\"error: second\");\n" +
                "Fiz.addBulletinMessage(\"error: third\");\n",
                cr.getHtml().jsCode.toString());
    }

    public void test_checkAuthToken_noInputToken() {
        HttpSession session = cr.getServletRequest().getSession(true);
        session.setAttribute("fiz.ClientRequest.sessionToken", "xyzzy");
        boolean gotException = false;
        try {
            cr.checkAuthToken();
        }
        catch (AuthenticationError e) {
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_checkAuthToken_tokensMatch() {
        cr.getMainDataset().set("fiz_auth", "xyzzy");
        HttpSession session = cr.getServletRequest().getSession(true);
        session.setAttribute("fiz.ClientRequest.sessionToken", "xyzzy");
        boolean gotException = false;
        try {
            cr.checkAuthToken();
        }
        catch (AuthenticationError e) {
            gotException = true;
        }
        assertEquals("exception didn't happen", false, gotException);
    }
    public void test_checkAuthToken_tokensDontMatch() {
        cr.getMainDataset().set("fiz_auth", "xyzzy");
        HttpSession session = cr.getServletRequest().getSession(true);
        session.setAttribute("fiz.ClientRequest.sessionToken", "xyzzy2");
        boolean gotException = false;
        try {
            cr.checkAuthToken();
        }
        catch (AuthenticationError e) {
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
        cr.evalJavascript("var y = 44;");
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
        cr.evalJavascript("var y = 44;");
        assertEquals("ClientRequest.jsCode",
                "var x = \"<&\\n\\t>\";var y = 44;",
                cr.jsCode.toString());
    }

    public void test_evalJavascript_templateWithIndexedData_normalRequest() {
        cr.evalJavascript("var @1 = \"@2\";", "x", "<&\n\t>");
        assertEquals("Javascript in HTML",
                "var x = \"<&\\n\\t>\";",
                cr.getHtml().jsCode.toString());
        assertEquals("ClientRequest.jsCode", null, cr.jsCode);
    }
    public void test_evalJavascript_templateWithIndexedData_ajaxRequest() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        Dataset d = new Dataset("value", "<&\n\t>");
        cr.evalJavascript("var @1 = \"@2\";", "x", "<&\n\t>");
        assertEquals("Javascript in HTML", "",
                cr.getHtml().jsCode.toString());
        assertEquals("ClientRequest.jsCode", "var x = \"<&\\n\\t>\";",
                cr.jsCode.toString());
        cr.evalJavascript("var y = 44;");
        assertEquals("ClientRequest.jsCode",
                "var x = \"<&\\n\\t>\";var y = 44;",
                cr.jsCode.toString());
    }
    
    public void test_finish_flushPageState() {
        ServletRequestFixture.session = null;
        cr.serverConfigData.set("googleAppEngine", true);
        cr.pageState = new PageState();
        cr.finish();
        
        // Ensure that PageState.flushPageState() was called (session will no 
        // longer be null).
        assertTrue("session contains AllPageInfo", 
                ServletRequestFixture.session.getAttribute("fiz.PageState") 
                    instanceof PageState.AllPageInfo);
    }
    
    public void test_finish_deleteFileItems() {
        cr.uploads = new HashMap<String, FileUpload>();
        cr.uploads.put("upload", new FileUploadFixture(true, ""));
        // Calling finish() with a FileUploadFixture should yield a 
        // NullPointerException.
        boolean exception = false;
        try {
            cr.finish();
        } catch (NullPointerException e) {
            exception = true;
        }
        assertTrue("Attempted to call delete() on a null FileItem", exception);
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
    public void test_finish_noJavascriptForAjax() throws IOException {
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        cr.finish();
        assertEquals("response", "",
                response.toString());
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
    public void test_finish_noJavascriptForPost() throws IOException {
        ServletResponseFixture response =
                ((ServletResponseFixture)cr.getServletResponse());
        cr.setClientRequestType(ClientRequest.Type.POST);
        cr.finish();
        TestUtil.assertMatchingSubstring("response",
                "Fiz.FormSection.handleResponse(\"\");",
                response.toString(), "Fiz.FormSection[^\n]*");
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

    public void test_getAuthToken_useExistingToken() {
        HttpSession session = cr.getServletRequest().getSession(true);
        session.setAttribute("fiz.ClientRequest.sessionToken", "xyzzy");
        assertEquals("pre-existing token value", "xyzzy",
                cr.getAuthToken());
    }
    public void test_getAuthToken_makeNewToken() {
        cr.testMode = true;
        HttpSession session = cr.getServletRequest().getSession(true);
        session.removeAttribute("fiz.ClientRequest.sessionToken");
        String token = cr.getAuthToken();
        String source = new String(StringUtil.decode4to3(token, 0,
                token.length()));
        assertEquals("token source", "**fake auth**", source);
        assertEquals("cached token", token, session.getAttribute(
                "fiz.ClientRequest.sessionToken"));
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

    public void test_getDataRequest_requestExists() {
        DataRequest r1 = new DataRequest("test1");
        cr.addDataRequest("request1", r1);
        assertEquals("existing request", r1, cr.getDataRequest("request1"));
    }
    public void test_getDataRequest_noSuchRequest() {
        boolean gotException = false;
        try {
            cr.getDataRequest("bogus");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "couldn't find data request named \"bogus\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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

    public void test_getPageProperty_noPageState_normalRequest() {
        cr.pageId = "bogus";
        boolean gotException = false;
        try {
            cr.getPageProperty("bogus");
        }
        catch (ClientRequest.MissingPagePropertyError e) {
            assertEquals("exception message",
                    "couldn't find page property \"bogus\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getPageProperty_noPageState_formPost() {
        cr.pageId = "bogus";
        cr.requestType = ClientRequest.Type.POST;
        boolean gotException = false;
        try {
            cr.getPageProperty("bogus");
        }
        catch (ClientRequest.StalePageError e) {
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getPageProperty_pageStateExistsButNotProperty() {
        cr.setPageProperty("prop1", "12345");
        boolean gotException = false;
        try {
            cr.getPageProperty("bogus");
        }
        catch (ClientRequest.MissingPagePropertyError e) {
            assertEquals("exception message",
                    "couldn't find page property \"bogus\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getPageProperty_success() {
        cr.setPageProperty("prop1", "12345");
        assertEquals("property value", "12345", cr.getPageProperty("prop1"));
    }

    public void test_setPageProperty_notSerializable() {
        class NotSerializable {
            int a, b;
        }
        boolean gotException = false;
        try {
            cr.setPageProperty("bogus", new NotSerializable());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "ClientRequest.setPageProperty received " +
                    "non-serializable NotSerializable object", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_setPageProperty_normalOperation() {
        // First request creates the page state.
        cr.setPageProperty("prop1", "12345");
        assertEquals("# defined properties", 1, cr.pageState.properties.size());
        assertEquals("value of property", "12345",
                cr.getPageProperty("prop1").toString());

        // Second request adds to the existing page state.
        cr.setPageProperty("prop2", "999");
        assertEquals("# defined properties", 2,
                cr.pageState.properties.size());
        assertEquals("value of prop1", "12345",
                cr.getPageProperty("prop1").toString());
        assertEquals("value of prop2", "999",
                cr.getPageProperty("prop2").toString());
    }

    public void test_getRequestNames() {
        assertEquals("no requests registered yet", "",
                cr.getRequestNames());
        DataRequest request = RawDataManager.newRequest(new Dataset());
        cr.addDataRequest("test1", request);
        cr.addDataRequest("test2", request);
        cr.addDataRequest("getPeople", request);
        assertEquals("names of requests", "getPeople, test1, test2",
                cr.getRequestNames());
    }
    
    public void test_getServerConfig() {
        cr.serverConfigData = null;
        Dataset serverConfig1 = cr.getServerConfig();
        // serverConfig when cached value is initially null.
        assertTrue("serverConfig has googleAppEngine field", 
                serverConfig1.get("googleAppEngine") instanceof Boolean);
        assertTrue("serverConfig has serverFileAccess field", 
                serverConfig1.get("serverFileAccess") instanceof Boolean);
        
        cr.serverConfigData = new Dataset("googleAppEngine", true, 
                "serverFileAccess", false);
        Dataset serverConfig2 = cr.getServerConfig();
        // serverConfig when there is a cached value.
        assertTrue("serverConfig has correct googleAppEngine value", 
                serverConfig2.getBool("googleAppEngine"));
        assertFalse("serverConfig has correct serverFileAccess value", 
                serverConfig2.getBool("serverFileAccess"));
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
    
    public void test_isFileAccessPermitted() {
        cr.serverConfigData = new Dataset("googleAppEngine", true, 
                "serverFileAccess", false);
        assertFalse("set to false", cr.isFileAccessPermitted());
        cr.serverConfigData.set("serverFileAccess", true);
        assertTrue("set to true", cr.isFileAccessPermitted());
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

    public void test_saveUploadedFile_noMultipartData() {
        assertEquals("fieldName doesn't exist", false,
                cr.saveUploadedFile("bogus", "a/b/c"));
    }
    public void test_saveUploadedFile_fileAccessNotPermitted() {
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
        cr.serverConfigData = new Dataset("googleAppEngine", false, 
                "serverFileAccess", false);
        assertFalse("Cannot save a file if filesystem access is not permitted",
                cr.saveUploadedFile("first", "_test1_/xyz"));
        TestUtil.deleteTree("_test1_");
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

    public void test_setAuthToken_alreadySet() {
        cr.authTokenSet = true;
        cr.setAuthToken();
        assertEquals("no javascript generated", "",
                cr.getHtml().jsCode.toString());
    }
    public void test_setAuthToken_notAlreadySet() {
        ServletRequestFixture.session = null;
        cr.testMode = true;
        cr.setAuthToken();
        assertEquals("javascript code generated",
                "Fiz.auth = \"JHB9AM69@$6=TAF*J \";\n",
                cr.getHtml().jsCode.toString());
    }

    public void test_setRequestType_basics() {
        assertEquals("initially NORMAL", false, cr.isAjax());
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        assertEquals("set to AJAX", true, cr.isAjax());
        cr.setClientRequestType(ClientRequest.Type.NORMAL);
        assertEquals("set to NORMAL", false, cr.isAjax());
    }
    public void test_setRequestType_checkToken() {
        ClientRequest.testSkipTokenCheck = false;
        cr.getMainDataset().set("fiz_auth", "xyzzy");
        HttpSession session = cr.getServletRequest().getSession(true);
        session.setAttribute("fiz.ClientRequest.sessionToken", "xyzzy");
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        assertEquals("token checked OK", true, cr.isAjax());
        assertEquals("authTokenSet", true, cr.authTokenSet);

        // Now arrange for a token mismatch.
        cr.getMainDataset().set("fiz_auth", "bogus");
        boolean gotException = false;
        try {
            cr.setClientRequestType(ClientRequest.Type.POST);
        }
        catch (AuthenticationError e) {
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
        assertEquals("js files in Html object", "static/fiz/Fiz.js",
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
        assertEquals("js files in Html object", "static/fiz/Fiz.js",
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
        cr.addDataRequest("getState", RawDataManager.newRequest(state));
        cr.showSections(
                new SectionFixture("first\n"),
                new SectionFixture("getState", "second: @name\n"),
                new SectionFixture("getState", "third: @capital\n"));
        assertEquals("addDataRequests calls", "data request \"none\";" +
                "data request \"getState\";data request \"getState\";",
                SectionFixture.log.toString());
        assertEquals("generated HTML", "first\n" +
                "second: California\n" +
                "third: Sacramento\n",
                cr.getHtml().getBody().toString());
    }

    public void test_updateElement() {
        cr.updateElement("table\"41\"", "<p>\"<Quoted>\"</p>");
        assertEquals("generated javascript",
                "document.getElementById(\"table\\\"41\\\"\").innerHTML " +
                "= \"<p>\\\"<Quoted>\\\"</p>\";\n",
                cr.getHtml().jsCode.toString());
    }

    public void test_updateSections() {
        cr.addDataRequest("getState", RawDataManager.newRequest(state));
        Section section1 = new SectionFixture(new Dataset(
                "template", "state: @state",
                "request", "getState"));
        Section section2 = new SectionFixture(new Dataset(
                "template", "capital: @capital",
                "request", "getState"));
        cr.getHtml().getBody().append("Original text");
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        SectionFixture.log.setLength(0);
        cr.updateSections("id44", section1, "id55", section2);
        assertEquals("calls to addDataRequests",
                "data request \"getState\";data request \"getState\";",
                SectionFixture.log.toString());
        assertEquals("response Javascript",
                "document.getElementById(\"id44\").innerHTML = " +
                "\"state: California\";\n" +
                "document.getElementById(\"id55\").innerHTML = " +
                "\"capital: Sacramento\";\n",
                cr.jsCode.toString());
        assertEquals("don't leave permanent modifications in HTML body",
                "Original text", cr.getHtml().getBody().toString());
    }

    public void test_getPageId_alreadyAvailable() {
        cr.pageId = "12345";
        assertEquals("page identifier", "12345", cr.getPageId());
    }
    public void test_getPageId_idInMainDataset() {
        cr.getMainDataset().set("fiz_pageId", "sample16");
        assertEquals("page identifier", "sample16", cr.getPageId());
    }
    public void test_getPageId_idNotInMainDataset() {
        cr.getServletRequest().getSession(true).setAttribute(
                "fiz.ClientRequest.lastPageId", 73);
        assertEquals("page identifier", "74", cr.getPageId());
    }
    public void test_getPageId_emptyValueInMainDataset() {
        cr.getMainDataset().set("fiz_pageId", "");
        cr.getServletRequest().getSession(true).setAttribute(
                "fiz.ClientRequest.lastPageId", 74);
        assertEquals("page identifier", "75", cr.getPageId());
    }
    public void test_getPageId_firstPageInSession() {
        cr.getServletRequest().getSession(true).removeAttribute(
                "fiz.ClientRequest.lastPageId");
        assertEquals("page identifier", "1", cr.getPageId());
    }
    public void test_getPageId_notFirstPage() {
        HttpSession session = cr.getServletRequest().getSession(true);
        session.setAttribute("fiz.ClientRequest.lastPageId", 77);
        assertEquals("page identifier", "78", cr.getPageId());
        assertEquals("id in session", "78", session.getAttribute(
                "fiz.ClientRequest.lastPageId").toString());
    }
    public void test_getPageId_javascript() {
        cr.getServletRequest().getSession(true).setAttribute(
                "fiz.ClientRequest.lastPageId", 73);
        assertEquals("page identifier", "74", cr.getPageId());
        assertEquals("ClientRequest.jsCode", "Fiz.pageId = \"74\";\n",
                cr.getHtml().jsCode.toString());
    }

    /*
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

    */
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
    
    public void test_readFileUpload_isFormField() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        FileUploadFixture upload = new FileUploadFixture(true, "fieldName");
        
        cr.readFileUpload(upload);
        // After one call to readFileUpload.
        assertEquals("main dataset contents (1 call)", "fieldName: " +
        		"FileUploadFixture\n", cr.getMainDataset().toString());
        
        cr.readFileUpload(upload);
        // After two calls to readFileUpload.
        assertEquals("main dataset contents (2 calls)", "fieldName:\n  " +
        		"- value: FileUploadFixture\n  - value: FileUploadFixture\n", 
        		cr.getMainDataset().toString());
        
        cr.readFileUpload(upload);
        // After three calls to readFileUpload.
        assertEquals("main dataset contents (3 calls)", "fieldName:\n  " +
        		"- value: FileUploadFixture\n  - value: FileUploadFixture\n  " +
        		"- value: FileUploadFixture\n", cr.getMainDataset().toString());
    }
    
    public void test_readFileUpload_notFormField() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        FileUploadFixture upload = new FileUploadFixture(false, "fieldName");
        
        cr.readFileUpload(upload);
        // After one call to readFileUpload.
        Object[] keys1 = cr.uploads.keySet().toArray();
        Arrays.sort(keys1);
        assertEquals("uploads HashMap", "fieldName",
                StringUtil.join(keys1, ", "));
        
        upload = new FileUploadFixture(false, "fieldName2");
        cr.readFileUpload(upload);
        // After two calls to readFileUpload.
        Object[] keys2 = cr.uploads.keySet().toArray();
        Arrays.sort(keys2);
        assertEquals("uploads HashMap", "fieldName, fieldName2",
                StringUtil.join(keys2, ", "));
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

    public void test_readMultipartFormData_fileUploads_withFilesystemAccess() {
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

    public void test_readMultipartFormData_fileUploads_noFilesystemAccess() {
        cr.clearData();                 // Discard default info from fixture.
        cr.mainDataset = new Dataset();
        cr.serverConfigData.set("serverFileAccess", false);
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
        
        // When filesystem access is permitted.
        boolean gotException1 = false;
        try {
            cr.readMultipartFormData();
        }
        catch (InternalError e) {
            assertEquals("exception message", "error reading multi-part " +
                    "form data: stream ended unexpectedly",
                    e.getMessage());
            gotException1 = true;
        }
        assertEquals("exception happened", true, gotException1);
        
        // When filesystem access isn't permitted.
        cr.serverConfigData.set("serverFileAccess", false);
        boolean gotException2 = false;
        try {
            cr.readMultipartFormData();
        }
        catch (InternalError e) {
            assertEquals("exception message", "error iterating over uploaded " +
            		"FileItemStreams: stream ended unexpectedly",
            		e.getMessage());
            gotException2 = true;
        }
        assertEquals("exception happened", true, gotException2);
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

