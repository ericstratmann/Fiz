/**
 * Junit tests for the Request class.
 */

package org.fiz;
import java.io.*;
import javax.servlet.http.*;

public class RequestTest  extends junit.framework.TestCase {
    protected TestServletRequest servletRequest;
    protected TestServletResponse servletResponse;
    protected Request request1;
    protected Request request2;

    public void setUp() {
        servletRequest = new TestServletRequest();
        servletResponse = new TestServletResponse();
        request1 = new Request(null, null);
        request2 = new Request(servletRequest, servletResponse);
    }

    public void testGetServletRequest() {
        assertEquals(servletRequest, request2.getServletRequest());
    }

    public void testGetServletResponse() {
        assertEquals(servletResponse, request2.getServletResponse());
    }

    public void testGetAuthType() {
        assertEquals(null, request1.getAuthType());
        assertEquals("authType", request2.getAuthType());
    }
    public void testGetContextPath() {
        assertEquals(null, request1.getContextPath());
        assertEquals("contextPath", request2.getContextPath());
    }
    public void testGetCookies() {
        assertEquals(0, request1.getCookies().length);
        assertEquals(2, request2.getCookies().length);
    }
    public void testGetDateHeader() {
        assertEquals(0, request1.getDateHeader("909"));
        assertEquals(909, request2.getDateHeader("909"));
    }
    public void testGetHeader() {
        assertEquals(null, request1.getHeader("name"));
        assertEquals("header: name", request2.getHeader("name"));
    }
    public void testGetHeaderNames() {
        assertEquals(null, request1.getHeaderNames());
        assertEquals("name1",
                request2.getHeaderNames().nextElement().toString());
    }
    public void testGetHeaders() {
        assertEquals(null, request1.getHeaders("name"));
        assertEquals("name_header1",
                request2.getHeaders("name").nextElement().toString());
    }
    public void testGetIntHeader() {
        assertEquals(0, request1.getIntHeader("1414"));
        assertEquals(1414, request2.getIntHeader("1414"));
    }
    public void testGetMethod() {
        assertEquals(null, request1.getMethod());
        assertEquals("method", request2.getMethod());
    }
    public void testGetPathInfo() {
        assertEquals(null, request1.getPathInfo());
        assertEquals("pathInfo", request2.getPathInfo());
    }
    public void testGetPathTranslated() {
        assertEquals(null, request1.getPathTranslated());
        assertEquals("pathTranslated", request2.getPathTranslated());
    }
    public void testGetQueryString() {
        assertEquals(null, request1.getQueryString());
        assertEquals("queryString", request2.getQueryString());
    }
    public void testGetRemoteUser() {
        assertEquals(null, request1.getRemoteUser());
        assertEquals("remoteUser", request2.getRemoteUser());
    }
    public void testGetRequestedSessionId() {
        assertEquals(null, request1.getRequestedSessionId());
        assertEquals("requestedSessionId", request2.getRequestedSessionId());
    }
    public void testGetRequestURI() {
        assertEquals(null, request1.getRequestURI());
        assertEquals("requestURI", request2.getRequestURI());
    }
    public void testGetRequestURL() {
        assertEquals(null, request1.getRequestURL());
        assertEquals("requestURL", request2.getRequestURL().toString());
    }
    public void testGetServletPath() {
        assertEquals(null, request1.getServletPath());
        assertEquals("servletPath", request2.getServletPath());
    }
    public void testGetSession() {
        assertEquals(null, request1.getSession());
        request2.getSession();
        assertEquals("getSession", servletRequest.lastMethod);
    }
    public void testGetSession_create() {
        assertEquals(null, request1.getSession(true));
        request2.getSession(true);
        assertEquals("getSession(true)", servletRequest.lastMethod);
    }
    public void testGetUserPrincipal() {
        assertEquals(null, request1.getUserPrincipal());
        request2.getUserPrincipal();
        assertEquals("getUserPrincipal", servletRequest.lastMethod);
    }
    public void testIsRequestedSessionIdFromCookie() {
        assertEquals(true, request1.isRequestedSessionIdFromCookie());
        assertEquals(false, request2.isRequestedSessionIdFromCookie());
        assertEquals("isRequestedSessionIdFromCookie",
                servletRequest.lastMethod);
    }
    public void testIsRequestedSessionIdFromUrl() {
        boolean gotException = false;
        try {
            request1.isRequestedSessionIdFromUrl();
        }
        catch (Request.DeprecatedMethodError e) {
            assertEquals("exception message",
                    "invoked deprecated method isRequestedSessionIdFromUrl",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void testIsRequestedSessionIdFromURL() {
        assertEquals(false, request1.isRequestedSessionIdFromURL());
        assertEquals(true, request2.isRequestedSessionIdFromURL());
        assertEquals("isRequestedSessionIdFromURL",
                servletRequest.lastMethod);
    }
    public void testIsRequestedSessionIdValid() {
        assertEquals(false, request1.isRequestedSessionIdValid());
        assertEquals(true, request2.isRequestedSessionIdValid());
        assertEquals("isRequestedSessionIdValid",
                servletRequest.lastMethod);
    }
    public void testIsUserInRole() {
        assertEquals(false, request1.isUserInRole("role1"));
        assertEquals(true, request2.isUserInRole("role2"));
        assertEquals("isUserInRole(role2)", servletRequest.lastMethod);
    }

    public void testGetAttribute() {
        assertEquals(null, request1.getAttribute("att_name"));
        assertEquals(null, request2.getAttribute("att_name"));
        assertEquals("getAttribute(att_name)", servletRequest.lastMethod);
    }
    public void testGetAttributeNames() {
        assertEquals(null, request1.getAttributeNames());
        assertEquals("name1",
                request2.getAttributeNames().nextElement().toString());
    }
    public void testGetCharacterEncoding() {
        assertEquals(null, request1.getCharacterEncoding());
        assertEquals("characterEncoding", request2.getCharacterEncoding());
    }
    public void testGetContentLength() {
        assertEquals(0, request1.getContentLength());
        assertEquals(444, request2.getContentLength());
    }
    public void testGetContentType() {
        assertEquals(null, request1.getContentType());
        assertEquals("contentType", request2.getContentType());
    }
    public void testGetInputStream() throws IOException {
        assertEquals(null, request1.getInputStream());
        assertEquals(null, request2.getInputStream());
        assertEquals("getInputStream", servletRequest.lastMethod);
    }
    public void testGetLocalAddr() {
        assertEquals(null, request1.getLocalAddr());
        assertEquals("localAddr", request2.getLocalAddr());
    }
    public void testGetLocale() {
        assertEquals(null, request1.getLocale());
        assertEquals(null, request2.getLocale());
        assertEquals("getLocale", servletRequest.lastMethod);
    }
    public void testGetLocalName() {
        assertEquals(null, request1.getLocalName());
        assertEquals("localName", request2.getLocalName());
    }
    public void testGetLocalPort() {
        assertEquals(0, request1.getLocalPort());
        assertEquals(80, request2.getLocalPort());
    }
    public void testGetParameter() {
        assertEquals(null, request1.getParameter("p1"));
        assertEquals("parameter p1", request2.getParameter("p1"));
    }
    public void testGetParameterNames() {
        assertEquals(null, request1.getParameterNames());
        assertEquals("pname1",
                request2.getParameterNames().nextElement().toString());
    }
    public void testGetParameterValues() {
        assertEquals(null, request1.getParameterValues("p1"));
        assertEquals("second value", request2.getParameterValues("p1")[1]);
    }
    public void testGetProtocol() {
        assertEquals(null, request1.getProtocol());
        assertEquals("protocol", request2.getProtocol());
    }
    public void testGetReader() throws IOException {
        assertEquals(null, request1.getReader());
        assertEquals(null, request2.getReader());
        assertEquals("getReader", servletRequest.lastMethod);
    }
    public void testGetRealPath() {
        boolean gotException = false;
        try {
            request1.getRealPath("/a/b/c");
        }
        catch (Request.DeprecatedMethodError e) {
            assertEquals("exception message",
                    "invoked deprecated method getRealPath",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void testGetRemoteAddr() {
        assertEquals(null, request1.getRemoteAddr());
        assertEquals("remoteAddr", request2.getRemoteAddr());
    }
    public void testGetRemoteHost() {
        assertEquals(null, request1.getRemoteHost());
        assertEquals("remoteHost", request2.getRemoteHost());
    }
    public void testGetRemotePort() {
        assertEquals(0, request1.getRemotePort());
        assertEquals(1023, request2.getRemotePort());
    }
    public void testGetRequestDispatcher() {
        assertEquals(null, request1.getRequestDispatcher("/top"));
        assertEquals(null, request2.getRequestDispatcher("/top"));
        assertEquals("getRequestDispatcher(/top)", servletRequest.lastMethod);
    }
    public void testGetScheme() {
        assertEquals(null, request1.getScheme());
        assertEquals("scheme", request2.getScheme());
    }
    public void testGetServerName() {
        assertEquals(null, request1.getServerName());
        assertEquals("serverName", request2.getServerName());
    }
    public void testGetServerPort() {
        assertEquals(0, request1.getServerPort());
        assertEquals(8080, request2.getServerPort());
    }
    public void testIsSecure() {
        assertEquals(false, request1.isSecure());
        assertEquals(true, request2.isSecure());
        assertEquals("isSecure", servletRequest.lastMethod);
    }
    public void testRemoveAttribute() {
        request1.removeAttribute("att1");
        request2.removeAttribute("att1");
        assertEquals("removeAttribute(\"att1\")", servletRequest.lastMethod);
    }
    public void testSetAttribute() {
        request1.setAttribute("att2", "value2");
        request2.setAttribute("att2", 732);
        assertEquals("setAttribute(\"att2\", 732)", servletRequest.lastMethod);
    }
    public void testSetCharacterEncoding() throws UnsupportedEncodingException {
        request1.setCharacterEncoding("env1");
        request2.setCharacterEncoding("env2");
        assertEquals("setCharacterEncoding(\"env2\")",
                servletRequest.lastMethod);
    }

    public void testAddCookie() {
        Cookie cookie = new Cookie("cname", "cvalue");
        request1.addCookie(cookie);
        request2.addCookie(cookie);
        assertEquals("addCookie(\"cname\")", servletResponse.lastMethod);
    }
    public void testAddDateHeader() {
        request1.addDateHeader("header", 1234);
        request2.addDateHeader("header", 1234);
        assertEquals("addDateHeader(\"header\", 1234)",
                servletResponse.lastMethod);
    }
    public void testAddHeader() {
        request1.addHeader("hname", "hvalue");
        request2.addHeader("hname", "hvalue");
        assertEquals("addHeader(\"hname\", \"hvalue\")",
                servletResponse.lastMethod);
    }
    public void testAddIntHeader() {
        request1.addIntHeader("hname", 66);
        request2.addIntHeader("hname", 66);
        assertEquals("addIntHeader(\"hname\", 66)",
                servletResponse.lastMethod);
    }
    public void testContainsHeader() {
        assertEquals(false, request1.containsHeader("hname"));
        assertEquals(true, request2.containsHeader("hname"));
        assertEquals("containsHeader(\"hname\")", servletResponse.lastMethod);
    }
    public void testEncodeRedirectUrl() {
        boolean gotException = false;
        try {
            request1.encodeRedirectUrl("/a/b/c");
        }
        catch (Request.DeprecatedMethodError e) {
            assertEquals("exception message",
                    "invoked deprecated method encodeRedirectUrl",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void testEncodeRedirectURL() {
        assertEquals(null, request1.encodeRedirectURL("a/b/c"));
        assertEquals("encodeRedirectURL", request2.encodeRedirectURL("a/b/c"));
        assertEquals("encodeRedirectURL(\"a/b/c\")", servletResponse.lastMethod);
    }
    public void testEncodeUrl() {
        boolean gotException = false;
        try {
            request1.encodeUrl("/a/b/c");
        }
        catch (Request.DeprecatedMethodError e) {
            assertEquals("exception message",
                    "invoked deprecated method encodeUrl",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void testEncodeURL() {
        assertEquals(null, request1.encodeURL("a/b/c"));
        assertEquals("encodeURL", request2.encodeURL("a/b/c"));
        assertEquals("encodeURL(\"a/b/c\")", servletResponse.lastMethod);
    }
    public void testSendError() throws IOException {
        request1.sendError(101);
        request2.sendError(202);
        assertEquals("sendError(202)", servletResponse.lastMethod);
    }
    public void testSendError_withMessage() throws IOException {
        request1.sendError(101, "message1");
        request2.sendError(202, "message2");
        assertEquals("sendError(202, \"message2\")",
                servletResponse.lastMethod);
    }
    public void testSendRedirect() throws IOException {
        request1.sendRedirect("/x/y/z");
        request2.sendRedirect("/x/y/z");
        assertEquals("sendRedirect(\"/x/y/z\")", servletResponse.lastMethod);
    }
    public void testSetDateHeader() {
        request1.setDateHeader("hname", 101);
        request2.setDateHeader("hname", 202);
        assertEquals("setDateHeader(\"hname\", 202)",
                servletResponse.lastMethod);
    }
    public void testSetHeader() {
        request1.setHeader("hname", "hvalue");
        request2.setHeader("hname", "hvalue");
        assertEquals("setHeader(\"hname\", \"hvalue\")",
                servletResponse.lastMethod);
    }
    public void testSetIntHeader() {
        request1.setIntHeader("name17", 44);
        request2.setIntHeader("name18", 55);
        assertEquals("setIntHeader(\"name18\", 55)",
                servletResponse.lastMethod);
    }
    public void testSetStatus() {
        request1.setStatus(123);
        request2.setStatus(234);
        assertEquals("setStatus(234)", servletResponse.lastMethod);
    }
    public void testSetStatus_withMessage() {
        boolean gotException = false;
        try {
            request1.setStatus(44, "status message");
        }
        catch (Request.DeprecatedMethodError e) {
            assertEquals("exception message",
                    "invoked deprecated method setStatus(status, message)",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void testFlushBuffer() throws IOException {
        request1.flushBuffer();
        request2.flushBuffer();
        assertEquals("flushBuffer", servletResponse.lastMethod);
    }
    public void testGetBufferSize() {
        assertEquals(0, request1.getBufferSize());
        assertEquals(4096, request2.getBufferSize());
    }
    public void testGetResponseCharacterEncoding() {
        assertEquals(null, request1.getResponseCharacterEncoding());
        assertEquals("characterEncoding",
                request2.getResponseCharacterEncoding());
    }
    public void testGetResponseContentType() {
        assertEquals(null, request1.getResponseContentType());
        assertEquals("contentType", request2.getResponseContentType());
    }
    public void testGetResponseLocale() {
        assertEquals(null, request1.getResponseLocale());
        assertEquals(null, request2.getResponseLocale());
        assertEquals("getResponseLocale", servletResponse.lastMethod);
    }
    public void testGetOutputStream() throws IOException {
        assertEquals(null, request1.getOutputStream());
        assertEquals(null, request2.getOutputStream());
        assertEquals("getOutputStream", servletResponse.lastMethod);
    }
    public void testGetWriter() throws IOException {
        assertEquals(null, request1.getWriter());
        assertEquals(null, request2.getWriter());
        assertEquals("getWriter", servletResponse.lastMethod);
    }
    public void testIsCommitted() {
        assertEquals(false, request1.isCommitted());
        assertEquals(true, request2.isCommitted());
        assertEquals("isCommitted", servletResponse.lastMethod);
    }
    public void testReset() {
        request1.reset();
        request2.reset();
        assertEquals("reset", servletResponse.lastMethod);
    }
    public void testResetBuffer() {
        request1.resetBuffer();
        request2.resetBuffer();
        assertEquals("resetBuffer", servletResponse.lastMethod);
    }
    public void testSetBufferSize() {
        request1.setBufferSize(400);
        request2.setBufferSize(500);
        assertEquals("setBufferSize(500)", servletResponse.lastMethod);
    }
    public void testSetResponseCharacterEncoding() {
        request1.setResponseCharacterEncoding("xyz");
        request2.setResponseCharacterEncoding("abc");
        assertEquals("setCharacterEncoding(\"abc\")",
                servletResponse.lastMethod);
    }
    public void testSetContentType() {
        request1.setContentType("abc");
        request2.setContentType("xyz");
        assertEquals("setContentType(\"xyz\")", servletResponse.lastMethod);
    }
    public void testSetLocale() {
        request1.setLocale(null);
        request2.setLocale(null);
        assertEquals("setLocale", servletResponse.lastMethod);
    }
}
