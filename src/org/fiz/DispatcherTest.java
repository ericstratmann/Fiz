/**
 * Junit tests for the Dispatcher class.
 */

package org.fiz;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class DispatcherTest  extends junit.framework.TestCase {
    public void test_sanityCheck() {
        Object o = new DispatcherTest1();
        Interactor i = (Interactor) o;
        i.destroy();
        Dispatcher dispatcher = new Dispatcher();
        DispatcherTest1.count = 43;
        dispatcher.service(new TestRequest("/dispatcherTest1/incCount/extra"),
                null);
        assertEquals("error message", null, dispatcher.fullMessage);
        assertEquals("invocation count", 44, DispatcherTest1.count);
    }

    public void testDestroy() {
        TestDispatcher dispatcher = new TestDispatcher();
        DispatcherTest1.count = 0;
        DispatcherTest1.destroyCount = 0;
        DispatcherTest5.destroyCount = 0;
        dispatcher.service(new TestRequest("/dispatcherTest1/incCount"),
                null);
        dispatcher.service(new TestRequest("/dispatcherTest5/xyz"),
                null);
        assertEquals("first interactor not yet destroyed", 0,
                DispatcherTest1.destroyCount);
        assertEquals("second interactor not yet destroyed", 0,
                DispatcherTest5.destroyCount);
        dispatcher.destroy();
        assertEquals("first interactor destroyed", 1,
                DispatcherTest1.destroyCount);
        assertEquals("second interactor destroyed", 3,
                DispatcherTest5.destroyCount);
    }

    public void testService_parseMethodEndingInSlash() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest1/bogus/a/b/c"),
                null);
        TestUtil.assertSubstring("error message", "no method \"bogus\"",
                dispatcher.basicMessage);
    }
    public void testService_parseMethodNotEndingInSlash() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest1/bogus"), null);
        TestUtil.assertSubstring("error message", "no method \"bogus\"",
                dispatcher.basicMessage);
    }
    public void testService_notEnoughInfoInUri() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("abc"), null);
        TestUtil.assertSubstring("no slashes",
                "URI doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new TestRequest("//a/b/c"), null);
        TestUtil.assertSubstring("empty class name",
                "URI doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new TestRequest("/a//b/c"), null);
        TestUtil.assertSubstring("empty method name",
                "URI doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new TestRequest("/a/b/c"), null);
        TestUtil.assertSubstring("simplest valid URI",
                "can't find class \"org.fiz.A\"",
                dispatcher.basicMessage);
    }
    public void testService_nonexistentClass() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/missingClass/a"), null);
        TestUtil.assertSubstring("error message",
                "can't find class \"org.fiz.MissingClass\"",
                dispatcher.basicMessage);
    }
    public void testService_classDoesntImplementInteractor() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest4/x"), null);
        TestUtil.assertSubstring("error message",
                "class \"org.fiz.DispatcherTest4\" isn't a subclass of "
                + "org.fiz.Interactor", dispatcher.basicMessage);
    }
    public void testService_cantFindConstructor() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest2/a"), null);
        TestUtil.assertSubstring("error message",
                "couldn't find no-argument constructor for class "
                + "\"org.fiz.DispatcherTest2\"", dispatcher.basicMessage);
    }
    public void testService_classCantBeInstantiated() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest3/a"), null);
        TestUtil.assertSubstring("error message", "couldn't create instance "
                + "of class \"org.fiz.DispatcherTest3\": sample error",
                dispatcher.basicMessage);
    }
    public void testService_updateClassMap() {
        TestDispatcher dispatcher = new TestDispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest1/incCount/extra"),
                null);
        assertEquals("org.fiz.DispatcherTest1", dispatcher.getClassKeys());
    }
    public void testService_invokeInit() {
        TestDispatcher dispatcher = new TestDispatcher();
        DispatcherTest1.count = 0;
        DispatcherTest1.initCount = 0;
        dispatcher.service(new TestRequest("/dispatcherTest1/incCount"),
                null);
        assertEquals("init invoked during first request", 1,
                DispatcherTest1.initCount);
        dispatcher.service(new TestRequest("/dispatcherTest1/incCount"),
                null);
        assertEquals("init not invoked during second request", 1,
                DispatcherTest1.initCount);
        assertEquals("method invoked during both requests", 2,
                DispatcherTest1.count);
    }
    public void testService_scanMethods() {
        TestDispatcher dispatcher = new TestDispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest1/incCount/extra"),
                null);
        assertEquals("dispatcherTest1/incCount, dispatcherTest1/resetCount"
                + ", dispatcherTest1/error", dispatcher.getMethodKeys());
    }
    public void testService_methodNotFound() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest1/oneArg"),
                null);
        TestUtil.assertSubstring("error message",
                "no method \"oneArg\" in class org.fiz.DispatcherTest1",
                dispatcher.basicMessage);
    }
    public void testService_exceptionInMethod() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest1/error"),
                null);
        TestUtil.assertSubstring("error message", "error in method",
                dispatcher.basicMessage);
    }
    public void testService_handlingException() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new TestRequest("/dispatcherTest1/error"),
                null);
        assertEquals("error message", "error in method",
                dispatcher.basicMessage);
        String message = dispatcher.fullMessage.replaceAll("\\r\\n", "\n");
        TestUtil.assertSubstring("error message", "unhandled exception for URI "
                + "\"/a/b/c?day=Monday\"\n"
                + "java.lang.Error: error in method\n"
                + "\tat org.fiz.DispatcherTest1.error",
                message);
    }
}

// The following class implements just enough of the HttpServletRequest
// interface to provide request objects for tests.
@SuppressWarnings("deprecation")
class TestRequest implements HttpServletRequest {
    public String pathInfo;
    public StringBuffer url = new StringBuffer("http://localhost/a/b/c");
    public String uri = "/a/b/c";
    public String queryString = "day=Monday";

    public TestRequest(String path) {
        pathInfo = path;
    }

    // Methods from HttpServletRequest.
    public String getAuthType() {return null;}
    public String getContextPath() {return null;}
    public Cookie[] getCookies() {return new Cookie[0];}
    public long getDateHeader(String name) {return 0;}
    public String getHeader(String name) {return null;}
    public java.util.Enumeration getHeaderNames() {return null;}
    public java.util.Enumeration getHeaders(String name) {return null;}
    public int getIntHeader(String name) {return 0;}
    public String getMethod() {return null;}
    public String getPathInfo() {return pathInfo;}
    public String getPathTranslated() {return null;}
    public String getQueryString() {return queryString;}
    public String getRemoteUser() {return null;}
    public String getRequestedSessionId() {return null;}
    public String getRequestURI() {return uri;}
    public StringBuffer getRequestURL() {return url;}
    public String getServletPath() {return null;}
    public HttpSession getSession() {return null;}
    public HttpSession getSession(boolean create) {return null;}
    public java.security.Principal getUserPrincipal() {return null;}
    public boolean isRequestedSessionIdFromCookie() {return true;}
    public boolean isRequestedSessionIdFromUrl() {return false;}
    public boolean isRequestedSessionIdFromURL() {return false;}
    public boolean isRequestedSessionIdValid() {return false;}
    public boolean isUserInRole(String role) {return false;}

    // Methods from ServletRequest.
    public Object getAttribute(String name) {return null;}
    public java.util.Enumeration getAttributeNames() {return null;}
    public String getCharacterEncoding() {return "UTF-8";}
    public int getContentLength() {return 0;}
    public String getContentType() {return null;}
    public ServletInputStream getInputStream() {return null;}
    public String getLocalAddr() {return null;}
    public java.util.Locale getLocale() {return null;}
    public java.util.Enumeration getLocales() {return null;}
    public String getLocalName() {return "local_name";}
    public int getLocalPort() {return 80;}
    public String getParameter(String name) {return null;}
    public java.util.Map getParameterMap() {return null;}
    public java.util.Enumeration getParameterNames() {return null;}
    public String[] getParameterValues(String name) {return new String[0];}
    public String getProtocol() {return null;}
    public BufferedReader getReader() {return null;}
    public String getRealPath(String path) {return null;}
    public String getRemoteAddr() {return null;}
    public String getRemoteHost() {return null;}
    public int getRemotePort() {return 0;}
    public RequestDispatcher getRequestDispatcher(String path) {return null;}
    public String getScheme() {return "http";}
    public String getServerName() {return null;}
    public int getServerPort() {return 0;}
    public boolean isSecure() {return false;}
    public void removeAttribute(String name) {}
    public void setAttribute(String name, Object o) {}
    public void setCharacterEncoding(String env) {}
}

// The following class definition provides a mechanism for accessing
// protected/private fields and methods.
class TestDispatcher extends Dispatcher {
    public String getClassKeys() {
        return Util.join(classMap.keySet(), ", ");
    }
    public String getMethodKeys() {
        return Util.join(methodMap.keySet(), ", ");
    }
}
