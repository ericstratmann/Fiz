package org.fiz;
import java.io.*;
import javax.servlet.http.*;

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
    protected ServletResponseFixture servletResponse;
    protected ServletContextFixture servletContext;
    protected ServletConfigFixture servletConfig;
    protected HttpServlet servlet;
    protected ClientRequest request1;
    protected ClientRequest request2;

    public void setUp() {
        servletRequest = new ServletRequestFixture();
        servletResponse = new ServletResponseFixture();
        servletContext = new ServletContextFixture();
        servletConfig = new ServletConfigFixture(servletContext);
        servlet = new ServletFixture(servletConfig);
        request1 = new ClientRequest(null, null, null);
        request2 = new ClientRequest(servlet, servletRequest,
                servletResponse);
    }

    public void test_getMainDataset_ajaxDataOnly() {
        request2.setAjax(true);
        servletRequest.setParameters();
        servletRequest.contentType = "text/plain";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "2.p2(1.a3.999\n1.b2.88\n)\n4.name5.Alice\n"));
        assertEquals("main dataset contents", "name: Alice\n" +
                "p2:\n" +
                "    a: 999\n" +
                "    b: 88\n", request2.getMainDataset().toString());
    }
    public void test_getMainDataset_ajaxButWrongContentType() {
        request2.setAjax(true);
        servletRequest.setParameters();
        servletRequest.contentType = "bogus";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "2.p2(1.a3.999\n1.b2.88\n)\n4.name5.Alice\n"));
        assertEquals("main dataset contents", "",
                request2.getMainDataset().toString());
    }
    public void test_getMainDataset_exceptionReadingAjaxData() {
        request2.setAjax(true);
        servletRequest.contentType = "text/plain";
        servletRequest.inputReader = new ExceptionReader(
                new StringReader("2.p2.88\n"));
        boolean gotException = false;
        try {
            request2.getMainDataset();
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "error reading Ajax data: simulated error", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getMainDataset_queryDataOnly() {
        assertEquals("main dataset contents", "p1: param_value1\n" +
                "p2: param_value2\n", request2.getMainDataset().toString());
    }
    public void test_getMainDataset_queryDataAndAjax() {
        request2.setAjax(true);
        servletRequest.contentType = "text/plain";
        servletRequest.inputReader = new BufferedReader(new StringReader(
                "2.p2(1.a3.999\n1.b2.88\n)\n4.name5.Alice\n"));
        assertEquals("main dataset contents", "name: Alice\n" +
                "p1:   param_value1\n" +
                "p2:   param_value2\n", request2.getMainDataset().toString());
    }
    public void test_getMainDataset_noDataAtAll() {
        servletRequest.setParameters();
        assertEquals("main dataset contents", "",
                request2.getMainDataset().toString());
    }

    public void test_getRequestNames() {
        ClientRequest request = TestUtil.setUp();
        assertEquals("no requests registered yet", "",
                request.getRequestNames());
        request.registerDataRequest("fixture1");
        request.registerDataRequest("fixture2");
        request.registerDataRequest("fixture1");
        request.registerDataRequest("getPeople");
        assertEquals("names of registered requests",
                "fixture1, fixture2, getPeople",
                request.getRequestNames());
    }

    public void test_getServletRequest() {
        assertEquals(servletRequest, request2.getServletRequest());
    }

    public void test_getServletResponse() {
        assertEquals(servletResponse, request2.getServletResponse());
    }

    public void test_getUrlPrefix_useSavedValue() {
        request1.urlPrefix = "/foo/bar";
        assertEquals("request already has a value", "/foo/bar",
                request1.getUrlPrefix());
    }
    public void test_getUrlPrefix_generateValue() {
        assertEquals("computed value", "/servlet/path/context/path",
                request2.getUrlPrefix());
    }

    // isAjax is tested by the tests for setAjax.

    public void test_registerDataRequest_byName() {
        ClientRequest request = TestUtil.setUp();
        DataRequest data1 = request.registerDataRequest("fixture1");
        DataRequest data2 = request.registerDataRequest("fixture2");
        DataRequest data3 = request.registerDataRequest("fixture1");
        assertEquals("count of registered requests", 2,
                request.namedRequests.size());
        assertEquals("share duplicate requests", data1, data3);
        assertEquals("contents of request", "id:      fixture2\n" +
                "manager: fixture\n" +
                "name:    Alice\n",
                data2.getRequestData().toString());
    }

    public void test_registerDataRequest_byDataset() {
        ClientRequest request = TestUtil.setUp();
        DataRequest data1 = request.registerDataRequest(
                new DataRequest(new Dataset("name", "Bill")));
        DataRequest data2 = request.registerDataRequest(
                new DataRequest(new Dataset("name", "Carol")));
        assertEquals("count of registered requests", 2,
                request.unnamedRequests.size());
        assertEquals("contents of request", "name: Carol\n",
                request.unnamedRequests.get(1).getRequestData().toString());
    }

    public void test_setAjax() {
        ClientRequest cr = TestUtil.setUp();
        assertEquals("initially false", false, cr.isAjax());
        cr.setAjax(true);
        assertEquals("set to true", true, cr.isAjax());
        cr.setAjax(false);
        assertEquals("set to false", false, cr.isAjax());
    }

    public void test_showSections() {
        ClientRequest cr = TestUtil.setUp();
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
        ClientRequest request = TestUtil.setUp();
        DataRequest data1 = request.registerDataRequest("fixture1");
        DataRequest data2 = request.registerDataRequest("fixture2");
        DataRequest data3 = request.registerDataRequest("fixture1");
        DataRequest data4 = request.registerDataRequest(
                new DataRequest(new Dataset("manager", "fixture",
                "name", "Carol", "id", "xyzzy")));
        request.startDataRequests();
        assertEquals("data manager log",
                "fixture started xyzzy, fixture2, fixture1",
                DataManagerFixture.getLogs());
    }
    public void test_startDataRequests_noRequests() {
        ClientRequest request = TestUtil.setUp();
        request.startDataRequests();
        assertEquals("data manager log", "", DataManagerFixture.getLogs());
    }
}

