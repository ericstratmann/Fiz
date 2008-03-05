/**
 * Junit tests for the Dispatcher class.
 */

package org.fiz;
import javax.servlet.http.*;
import org.apache.log4j.Level;

public class DispatcherTest  extends junit.framework.TestCase {
    public void test_sanityCheck() {
        Object o = new DispatcherTest1();
        Interactor i = (Interactor) o;
        i.destroy();
        Dispatcher dispatcher = new Dispatcher();
        DispatcherTest1.count = 43;
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/incCount/extra"), null);
        assertEquals("error message", null, dispatcher.fullMessage);
        assertEquals("invocation count", 44, DispatcherTest1.count);
    }

    public void testUnsupportedUriError() {
        Error e = new Dispatcher.UnsupportedUriError("/a/b/c", "smelled funny");
        assertEquals("exception message",
                "unsupported URI \"/a/b/c\": smelled funny", e.getMessage());
    }

    public void testDestroy() {
        TestDispatcher dispatcher = new TestDispatcher();
        DispatcherTest1.count = 0;
        DispatcherTest1.destroyCount = 0;
        DispatcherTest5.destroyCount = 0;
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/incCount"), null);
        dispatcher.service(new DispatcherTestRequest("/dispatcherTest5/xyz"),
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
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/bogus/a/b/c"), null);
        TestUtil.assertSubstring("error message",
                "couldn't find method \"bogus\" with proper signature",
                dispatcher.basicMessage);
    }
    public void testService_parseMethodNotEndingInSlash() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/bogus"), null);
        TestUtil.assertSubstring("error message",
                "couldn't find method \"bogus\" with proper signature",
                dispatcher.basicMessage);
    }
    public void testService_notEnoughInfoInUri() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest("abc"), null);
        TestUtil.assertSubstring("no slashes",
                "URI doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new DispatcherTestRequest("//a/b/c"), null);
        TestUtil.assertSubstring("empty class name",
                "URI doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new DispatcherTestRequest("/a//b/c"), null);
        TestUtil.assertSubstring("empty method name",
                "URI doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new DispatcherTestRequest("/a/b/c"), null);
        TestUtil.assertSubstring("simplest valid URI",
                "can't find class \"org.fiz.A\"",
                dispatcher.basicMessage);
    }
    public void testService_nonexistentClass() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest("/missingClass/a"), null);
        TestUtil.assertSubstring("error message",
                "can't find class \"org.fiz.MissingClass\"",
                dispatcher.basicMessage);
    }
    public void testService_classNotInteractorSubclass() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest("/dispatcherTest4/x"),
                null);
        TestUtil.assertSubstring("error message",
                "class \"org.fiz.DispatcherTest4\" isn't a subclass of "
                + "org.fiz.Interactor", dispatcher.basicMessage);
    }
    public void testService_cantFindConstructor() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest("/dispatcherTest2/a"),
                null);
        TestUtil.assertSubstring("error message",
                "couldn't find no-argument constructor for class "
                + "\"org.fiz.DispatcherTest2\"", dispatcher.basicMessage);
    }
    public void testService_classCantBeInstantiated() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest("/dispatcherTest3/a"),
                null);
        TestUtil.assertSubstring("error message", "couldn't create instance "
                + "of class \"org.fiz.DispatcherTest3\": sample error",
                dispatcher.basicMessage);
    }
    public void testService_updateClassMap() {
        TestDispatcher dispatcher = new TestDispatcher();
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/incCount/extra"), null);
        assertEquals("org.fiz.DispatcherTest1", dispatcher.getClassKeys());
    }
    public void testService_invokeInit() {
        TestDispatcher dispatcher = new TestDispatcher();
        DispatcherTest1.count = 0;
        DispatcherTest1.initCount = 0;
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/incCount"), null);
        assertEquals("init invoked during first request", 1,
                DispatcherTest1.initCount);
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/incCount"), null);
        assertEquals("init not invoked during second request", 1,
                DispatcherTest1.initCount);
        assertEquals("method invoked during both requests", 2,
                DispatcherTest1.count);
    }
    public void testService_scanMethods() {
        TestDispatcher dispatcher = new TestDispatcher();
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/incCount/extra"), null);
        assertEquals("dispatcherTest1/incCount, dispatcherTest1/resetCount"
                + ", dispatcherTest1/error", dispatcher.getMethodKeys());
    }
    public void testService_methodNotFound() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest(
                "/dispatcherTest1/twoArgs"), null);
        TestUtil.assertSubstring("error message",
                "couldn't find method \"twoArgs\" with proper signature "
                + "in class org.fiz.DispatcherTest1",
                dispatcher.basicMessage);
    }
    public void testService_exceptionInMethod() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest("/dispatcherTest1/error"),
                null);
        TestUtil.assertSubstring("error message", "error in method",
                dispatcher.basicMessage);
    }
    public void testService_handlingException() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherTestRequest("/dispatcherTest1/error"),
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

    public void testGetClass_exceptionInMethod() {
        Dispatcher dispatcher = new TestDispatcher();
        Class<?> cl = dispatcher.findClass("org.fiz.Interactor", null);
        TestUtil.assertSubstring("name of found class", "org.fiz.Interactor",
                cl.getName());
        boolean gotException = false;
        try {
            dispatcher.findClass("bogus_xyz",
                    new DispatcherTestRequest("/first/second"));
        }
        catch (Dispatcher.UnsupportedUriError e) {
            assertEquals("exception message",
                    "unsupported URI \"/a/b/c\": can't find class "
                    + "\"bogus_xyz\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}

// The following class implements just enough of the HttpServletRequest
// interface to provide request objects for tests.
class DispatcherTestRequest extends TestServletRequest {
    public String pathInfo;
    public StringBuffer url = new StringBuffer("http://localhost/a/b/c");
    public String uri = "/a/b/c";
    public String queryString = "day=Monday";

    public DispatcherTestRequest(String path) {
        pathInfo = path;
    }

    public String getPathInfo() {return pathInfo;}
    public String getQueryString() {return queryString;}
    public String getRequestURI() {return uri;}
    public StringBuffer getRequestURL() {return url;}
}

// The following class definition provides a mechanism for accessing
// protected/private fields and methods.
class TestDispatcher extends Dispatcher {
    public TestDispatcher() {
        logger.setLevel(Level.FATAL);
    }
    public String getClassKeys() {
        return Util.join(classMap.keySet(), ", ");
    }
    public String getMethodKeys() {
        return Util.join(methodMap.keySet(), ", ");
    }
    public Class<?> findClass(String className, HttpServletRequest request) {
        return super.findClass(className, request);
    }
}
