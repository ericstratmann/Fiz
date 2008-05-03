package org.fiz;
import java.io.*;
import javax.servlet.http.*;

/**
 * Junit tests for the ClientRequest class.
 */

public class ClientRequestTest extends junit.framework.TestCase {
    protected ServletRequestFixture servletRequest;
    protected ServletResponseFixture servletResponse;
    protected ClientRequest request1;
    protected ClientRequest request2;

    public void setUp() {
        servletRequest = new ServletRequestFixture();
        servletResponse = new ServletResponseFixture();
        request1 = new ClientRequest(null, null, null);
        request2 = new ClientRequest(null, servletRequest, servletResponse);
    }

    public void test_getDataset() {
        assertEquals("request dataset contents", "p1: param_value1\n" +
                "p2: param_value2\n", request2.getDataset().toString());
    }
    public void test_getDataset_noQueryData() {
        servletRequest.setParameters();
        assertEquals("request dataset contents", "",
                request2.getDataset().toString());
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
