/**
 * Junit tests for the Request class.
 */

package org.fiz;
import java.io.*;
import javax.servlet.http.*;

public class RequestTest  extends junit.framework.TestCase {
    protected ServletRequestFixture servletRequest;
    protected ServletResponseFixture servletResponse;
    protected Request request1;
    protected Request request2;

    public void setUp() {
        servletRequest = new ServletRequestFixture();
        servletResponse = new ServletResponseFixture();
        request1 = new Request(null, null, null);
        request2 = new Request(null, servletRequest, servletResponse);
    }

    public void test_getServletRequest() {
        assertEquals(servletRequest, request2.getServletRequest());
    }

    public void test_getServletResponse() {
        assertEquals(servletResponse, request2.getServletResponse());
    }

    public void test_getAuthType() {
        assertEquals(null, request1.getAuthType());
        assertEquals("authType", request2.getAuthType());
    }
    public void test_getContextPath() {
        assertEquals(null, request1.getContextPath());
        assertEquals("contextPath", request2.getContextPath());
    }
    public void test_getCookies() {
        assertEquals(0, request1.getCookies().length);
        assertEquals(2, request2.getCookies().length);
    }
    public void test_getDateHeader() {
        assertEquals(0, request1.getDateHeader("909"));
        assertEquals(909, request2.getDateHeader("909"));
    }
    public void test_getHeader() {
        assertEquals(null, request1.getHeader("name"));
        assertEquals("header: name", request2.getHeader("name"));
    }
    public void test_getHeaderNames() {
        assertEquals(null, request1.getHeaderNames());
        assertEquals("name1",
                request2.getHeaderNames().nextElement().toString());
    }
    public void test_getHeaders() {
        assertEquals(null, request1.getHeaders("name"));
        assertEquals("name_header1",
                request2.getHeaders("name").nextElement().toString());
    }
    public void test_getIntHeader() {
        assertEquals(0, request1.getIntHeader("1414"));
        assertEquals(1414, request2.getIntHeader("1414"));
    }
    public void test_getMethod() {
        assertEquals(null, request1.getMethod());
        assertEquals("method", request2.getMethod());
    }
    public void test_getPathInfo() {
        assertEquals(null, request1.getPathInfo());
        assertEquals("pathInfo", request2.getPathInfo());
    }
    public void test_getPathTranslated() {
        assertEquals(null, request1.getPathTranslated());
        assertEquals("pathTranslated", request2.getPathTranslated());
    }
    public void test_getQueryString() {
        assertEquals(null, request1.getQueryString());
        assertEquals("queryString", request2.getQueryString());
    }
    public void test_getRemoteUser() {
        assertEquals(null, request1.getRemoteUser());
        assertEquals("remoteUser", request2.getRemoteUser());
    }
    public void test_getRequestedSessionId() {
        assertEquals(null, request1.getRequestedSessionId());
        assertEquals("requestedSessionId", request2.getRequestedSessionId());
    }
    public void test_getRequestURI() {
        assertEquals(null, request1.getRequestURI());
        assertEquals("requestURI", request2.getRequestURI());
    }
    public void test_getRequestURL() {
        assertEquals(null, request1.getRequestURL());
        assertEquals("requestURL", request2.getRequestURL().toString());
    }
    public void test_getServletPath() {
        assertEquals(null, request1.getServletPath());
        assertEquals("servletPath", request2.getServletPath());
    }
    public void test_getSession() {
        assertEquals(null, request1.getSession());
        request2.getSession();
        assertEquals("getSession", servletRequest.lastMethod);
    }
    public void test_getSession_create() {
        assertEquals(null, request1.getSession(true));
        request2.getSession(true);
        assertEquals("getSession(true)", servletRequest.lastMethod);
    }
    public void test_getUserPrincipal() {
        assertEquals(null, request1.getUserPrincipal());
        request2.getUserPrincipal();
        assertEquals("getUserPrincipal", servletRequest.lastMethod);
    }
    public void test_isRequestedSessionIdFromCookie() {
        assertEquals(true, request1.isRequestedSessionIdFromCookie());
        assertEquals(false, request2.isRequestedSessionIdFromCookie());
        assertEquals("isRequestedSessionIdFromCookie",
                servletRequest.lastMethod);
    }
    public void test_isRequestedSessionIdFromUrl() {
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
    public void test_isRequestedSessionIdFromURL() {
        assertEquals(false, request1.isRequestedSessionIdFromURL());
        assertEquals(true, request2.isRequestedSessionIdFromURL());
        assertEquals("isRequestedSessionIdFromURL",
                servletRequest.lastMethod);
    }
    public void test_isRequestedSessionIdValid() {
        assertEquals(false, request1.isRequestedSessionIdValid());
        assertEquals(true, request2.isRequestedSessionIdValid());
        assertEquals("isRequestedSessionIdValid",
                servletRequest.lastMethod);
    }
    public void test_isUserInRole() {
        assertEquals(false, request1.isUserInRole("role1"));
        assertEquals(true, request2.isUserInRole("role2"));
        assertEquals("isUserInRole(role2)", servletRequest.lastMethod);
    }

    public void test_getAttribute() {
        assertEquals(null, request1.getAttribute("att_name"));
        assertEquals(null, request2.getAttribute("att_name"));
        assertEquals("getAttribute(att_name)", servletRequest.lastMethod);
    }
    public void test_getAttributeNames() {
        assertEquals(null, request1.getAttributeNames());
        assertEquals("name1",
                request2.getAttributeNames().nextElement().toString());
    }
    public void test_getCharacterEncoding() {
        assertEquals(null, request1.getCharacterEncoding());
        assertEquals("characterEncoding", request2.getCharacterEncoding());
    }
    public void test_getContentLength() {
        assertEquals(0, request1.getContentLength());
        assertEquals(444, request2.getContentLength());
    }
    public void test_getContentType() {
        assertEquals(null, request1.getContentType());
        assertEquals("contentType", request2.getContentType());
    }
    public void test_getInputStream() throws IOException {
        assertEquals(null, request1.getInputStream());
        assertEquals(null, request2.getInputStream());
        assertEquals("getInputStream", servletRequest.lastMethod);
    }
    public void test_getLocalAddr() {
        assertEquals(null, request1.getLocalAddr());
        assertEquals("localAddr", request2.getLocalAddr());
    }
    public void test_getLocale() {
        assertEquals(null, request1.getLocale());
        assertEquals(null, request2.getLocale());
        assertEquals("getLocale", servletRequest.lastMethod);
    }
    public void test_getLocalName() {
        assertEquals(null, request1.getLocalName());
        assertEquals("localName", request2.getLocalName());
    }
    public void test_getLocalPort() {
        assertEquals(0, request1.getLocalPort());
        assertEquals(80, request2.getLocalPort());
    }
    public void test_getParameter() {
        assertEquals(null, request1.getParameter("p1"));
        assertEquals("parameter p1", request2.getParameter("p1"));
    }
    public void test_getParameterNames() {
        assertEquals(null, request1.getParameterNames());
        assertEquals("pname1",
                request2.getParameterNames().nextElement().toString());
    }
    public void test_getParameterValues() {
        assertEquals(null, request1.getParameterValues("p1"));
        assertEquals("second value", request2.getParameterValues("p1")[1]);
    }
    public void test_getProtocol() {
        assertEquals(null, request1.getProtocol());
        assertEquals("protocol", request2.getProtocol());
    }
    public void test_getReader() throws IOException {
        assertEquals(null, request1.getReader());
        assertEquals(null, request2.getReader());
        assertEquals("getReader", servletRequest.lastMethod);
    }
    public void test_getRealPath() {
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
    public void test_getRemoteAddr() {
        assertEquals(null, request1.getRemoteAddr());
        assertEquals("remoteAddr", request2.getRemoteAddr());
    }
    public void test_getRemoteHost() {
        assertEquals(null, request1.getRemoteHost());
        assertEquals("remoteHost", request2.getRemoteHost());
    }
    public void test_getRemotePort() {
        assertEquals(0, request1.getRemotePort());
        assertEquals(1023, request2.getRemotePort());
    }
    public void test_getRequestDispatcher() {
        assertEquals(null, request1.getRequestDispatcher("/top"));
        assertEquals(null, request2.getRequestDispatcher("/top"));
        assertEquals("getRequestDispatcher(/top)", servletRequest.lastMethod);
    }
    public void test_getScheme() {
        assertEquals(null, request1.getScheme());
        assertEquals("scheme", request2.getScheme());
    }
    public void test_getServerName() {
        assertEquals(null, request1.getServerName());
        assertEquals("serverName", request2.getServerName());
    }
    public void test_getServerPort() {
        assertEquals(0, request1.getServerPort());
        assertEquals(8080, request2.getServerPort());
    }
    public void test_isSecure() {
        assertEquals(false, request1.isSecure());
        assertEquals(true, request2.isSecure());
        assertEquals("isSecure", servletRequest.lastMethod);
    }
    public void test_removeAttribute() {
        request1.removeAttribute("att1");
        request2.removeAttribute("att1");
        assertEquals("removeAttribute(\"att1\")", servletRequest.lastMethod);
    }
    public void test_setAttribute() {
        request1.setAttribute("att2", "value2");
        request2.setAttribute("att2", 732);
        assertEquals("setAttribute(\"att2\", 732)", servletRequest.lastMethod);
    }
    public void test_setCharacterEncoding() throws UnsupportedEncodingException {
        request1.setCharacterEncoding("env1");
        request2.setCharacterEncoding("env2");
        assertEquals("setCharacterEncoding(\"env2\")",
                servletRequest.lastMethod);
    }

    public void test_addCookie() {
        Cookie cookie = new Cookie("cname", "cvalue");
        request1.addCookie(cookie);
        request2.addCookie(cookie);
        assertEquals("addCookie(\"cname\")", servletResponse.lastMethod);
    }
    public void test_addDateHeader() {
        request1.addDateHeader("header", 1234);
        request2.addDateHeader("header", 1234);
        assertEquals("addDateHeader(\"header\", 1234)",
                servletResponse.lastMethod);
    }
    public void test_addHeader() {
        request1.addHeader("hname", "hvalue");
        request2.addHeader("hname", "hvalue");
        assertEquals("addHeader(\"hname\", \"hvalue\")",
                servletResponse.lastMethod);
    }
    public void test_addIntHeader() {
        request1.addIntHeader("hname", 66);
        request2.addIntHeader("hname", 66);
        assertEquals("addIntHeader(\"hname\", 66)",
                servletResponse.lastMethod);
    }
    public void test_containsHeader() {
        assertEquals(false, request1.containsHeader("hname"));
        assertEquals(true, request2.containsHeader("hname"));
        assertEquals("containsHeader(\"hname\")", servletResponse.lastMethod);
    }
    public void test_encodeRedirectUrl() {
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
    public void test_encodeRedirectURL() {
        assertEquals(null, request1.encodeRedirectURL("a/b/c"));
        assertEquals("encodeRedirectURL", request2.encodeRedirectURL("a/b/c"));
        assertEquals("encodeRedirectURL(\"a/b/c\")", servletResponse.lastMethod);
    }
    public void test_encodeUrl() {
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
    public void test_encodeURL() {
        assertEquals(null, request1.encodeURL("a/b/c"));
        assertEquals("encodeURL", request2.encodeURL("a/b/c"));
        assertEquals("encodeURL(\"a/b/c\")", servletResponse.lastMethod);
    }
    public void test_sendError() throws IOException {
        request1.sendError(101);
        request2.sendError(202);
        assertEquals("sendError(202)", servletResponse.lastMethod);
    }
    public void test_sendError_withMessage() throws IOException {
        request1.sendError(101, "message1");
        request2.sendError(202, "message2");
        assertEquals("sendError(202, \"message2\")",
                servletResponse.lastMethod);
    }
    public void test_sendRedirect() throws IOException {
        request1.sendRedirect("/x/y/z");
        request2.sendRedirect("/x/y/z");
        assertEquals("sendRedirect(\"/x/y/z\")", servletResponse.lastMethod);
    }
    public void test_setDateHeader() {
        request1.setDateHeader("hname", 101);
        request2.setDateHeader("hname", 202);
        assertEquals("setDateHeader(\"hname\", 202)",
                servletResponse.lastMethod);
    }
    public void test_setHeader() {
        request1.setHeader("hname", "hvalue");
        request2.setHeader("hname", "hvalue");
        assertEquals("setHeader(\"hname\", \"hvalue\")",
                servletResponse.lastMethod);
    }
    public void test_setIntHeader() {
        request1.setIntHeader("name17", 44);
        request2.setIntHeader("name18", 55);
        assertEquals("setIntHeader(\"name18\", 55)",
                servletResponse.lastMethod);
    }
    public void test_setStatus() {
        request1.setStatus(123);
        request2.setStatus(234);
        assertEquals("setStatus(234)", servletResponse.lastMethod);
    }
    public void test_setStatus_withMessage() {
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
    public void test_flushBuffer() throws IOException {
        request1.flushBuffer();
        request2.flushBuffer();
        assertEquals("flushBuffer", servletResponse.lastMethod);
    }
    public void test_getBufferSize() {
        assertEquals(0, request1.getBufferSize());
        assertEquals(4096, request2.getBufferSize());
    }
    public void test_getResponseCharacterEncoding() {
        assertEquals(null, request1.getResponseCharacterEncoding());
        assertEquals("characterEncoding",
                request2.getResponseCharacterEncoding());
    }
    public void test_getResponseContentType() {
        assertEquals(null, request1.getResponseContentType());
        assertEquals("contentType", request2.getResponseContentType());
    }
    public void test_getResponseLocale() {
        assertEquals(null, request1.getResponseLocale());
        assertEquals(null, request2.getResponseLocale());
        assertEquals("getResponseLocale", servletResponse.lastMethod);
    }
    public void test_getOutputStream() throws IOException {
        assertEquals(null, request1.getOutputStream());
        assertEquals(null, request2.getOutputStream());
        assertEquals("getOutputStream", servletResponse.lastMethod);
    }
    public void test_getWriter() throws IOException {
        assertEquals(null, request1.getWriter());
        assertEquals(servletResponse.getWriter(), request2.getWriter());
        assertEquals("getWriter", servletResponse.lastMethod);
    }
    public void test_isCommitted() {
        assertEquals(false, request1.isCommitted());
        assertEquals(true, request2.isCommitted());
        assertEquals("isCommitted", servletResponse.lastMethod);
    }
    public void test_reset() {
        request1.reset();
        request2.reset();
        assertEquals("reset", servletResponse.lastMethod);
    }
    public void test_resetBuffer() {
        request1.resetBuffer();
        request2.resetBuffer();
        assertEquals("resetBuffer", servletResponse.lastMethod);
    }
    public void test_setBufferSize() {
        request1.setBufferSize(400);
        request2.setBufferSize(500);
        assertEquals("setBufferSize(500)", servletResponse.lastMethod);
    }
    public void test_setResponseCharacterEncoding() {
        request1.setResponseCharacterEncoding("xyz");
        request2.setResponseCharacterEncoding("abc");
        assertEquals("setCharacterEncoding(\"abc\")",
                servletResponse.lastMethod);
    }
    public void test_setContentType() {
        request1.setContentType("abc");
        request2.setContentType("xyz");
        assertEquals("setContentType(\"xyz\")", servletResponse.lastMethod);
    }
    public void test_setLocale() {
        request1.setLocale(null);
        request2.setLocale(null);
        assertEquals("setLocale", servletResponse.lastMethod);
    }
}
