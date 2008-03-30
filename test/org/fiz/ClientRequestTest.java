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

    public void test_getServletRequest() {
        assertEquals(servletRequest, request2.getServletRequest());
    }

    public void test_getServletResponse() {
        assertEquals(servletResponse, request2.getServletResponse());
    }

    public void test_getDataset() {
        assertEquals("request data set contents", "p1: param_value1\n" +
                "p2: param_value2\n", request2.getDataset().toString());
    }
    public void test_getDataset_noQueryData() {
        servletRequest.setParameters();
        assertEquals("request data set contents", "",
                request2.getDataset().toString());
    }
}
