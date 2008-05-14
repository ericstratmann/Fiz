package org.fiz;
import java.io.File;
import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.log4j.Level;

/**
 * Junit tests for the Dispatcher class.
 */

public class DispatcherTest  extends junit.framework.TestCase {
    // The following class implements just enough of the HttpServletRequest
    // interface to provide request objects for tests.
    protected static class DispatcherRequestFixture
            extends ServletRequestFixture {
        public String pathInfo;
        public StringBuffer url = new StringBuffer("http://localhost/a/b/c");
        public String uri = "/a/b/c";
        public String queryString = "day=Monday";

        public DispatcherRequestFixture(String path) {
            pathInfo = path;
        }

        public String getPathInfo() {return pathInfo;}
        public String getQueryString() {return queryString;}
        public String getRequestURI() {return uri;}
        public StringBuffer getRequestURL() {return url;}
    }

    // The following class definition provides a mechanism for accessing
    // protected/private fields and methods.
    protected static class DispatcherFixture extends Dispatcher {
        public DispatcherFixture() {
            logger.setLevel(Level.FATAL);
        }
        public String getClassKeys() {
            return StringUtil.join(classMap.keySet(), ", ");
        }
        public String getMethodKeys() {
            return StringUtil.join(methodMap.keySet(), ", ");
        }
        public Class<?> findClass(String className, HttpServletRequest request) {
            return super.findClass(className, request);
        }
    }

    public void setUp() {
        Dispatcher.testMode = true;
        Config.setDataset("main", new Dataset("searchPackages", "org.fiz"));
    }

    public void test_sanityCheck() throws ServletException {
        (new File("_test1_/WEB-INF/config")).mkdirs();
        TestUtil.writeFile("_test1_/WEB-INF/config/main.yaml",
                "searchPackages: org.fiz\n");
        Object o = new DispatcherTest1();
        Interactor i = (Interactor) o;
        i.destroy();
        Dispatcher dispatcher = new Dispatcher();
        ServletContextFixture context = new ServletContextFixture();
        context.contextRoot = "_test1_";
        dispatcher.init(new ServletConfigFixture(context));
        dispatcher.logger.setLevel(Level.FATAL);
        DispatcherTest1.count = 43;
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/incCount/extra"), new ServletResponseFixture());
        assertEquals("error message", null, dispatcher.fullMessage);
        assertEquals("invocation count", 44, DispatcherTest1.count);
        TestUtil.deleteTree("_test1_");
    }

    public void test_UnsupportedUriError() {
        Error e = new Dispatcher.UnsupportedUrlError("/a/b/c", "smelled funny");
        assertEquals("exception message",
                "unsupported URL \"/a/b/c\": smelled funny", e.getMessage());
    }

    public void test_init() throws ServletException {
        (new File("_test1_/WEB-INF/config")).mkdirs();
        ServletContextFixture context = new ServletContextFixture();
        context.contextRoot = "_test1_";
        TestUtil.writeFile("_test1_/WEB-INF/config/main.yaml",
                "home: xyzzy\n");
        ServletConfigFixture config = new ServletConfigFixture(context);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.init(config);
        assertEquals("Config path", "_test1_/WEB-INF/config",
                Config.getPath()[0]);
        assertEquals("home entry in main config dataset", "_test1_",
                Config.getDataset("main").check("home"));
        assertEquals("Css path", "_test1_/WEB-INF/css", Css.getPath()[0]);
        TestUtil.deleteTree("_test1_");
    }

    public void test_destroy() {
        DispatcherFixture dispatcher = new DispatcherFixture();
        DispatcherTest1.count = 0;
        DispatcherTest1.destroyCount = 0;
        DispatcherTest2.destroyCount = 0;
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        dispatcher.service(new DispatcherRequestFixture("/dispatcherTest2/xyz"),
                new ServletResponseFixture());
        assertEquals("first interactor not yet destroyed", 0,
                DispatcherTest1.destroyCount);
        assertEquals("second interactor not yet destroyed", 0,
                DispatcherTest2.destroyCount);
        dispatcher.destroy();
        assertEquals("first interactor destroyed", 1,
                DispatcherTest1.destroyCount);
        assertEquals("second interactor destroyed", 3,
                DispatcherTest2.destroyCount);
    }

    public void test_service_parseMethodEndingInSlash() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/bogus/a/b/c"), new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find method \"bogus\" with proper signature",
                dispatcher.basicMessage);
    }
    public void test_service_parseMethodNotEndingInSlash() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/bogus"), new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find method \"bogus\" with proper signature",
                dispatcher.basicMessage);
    }
    public void test_service_notEnoughInfoInUri() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherRequestFixture("abc"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("no slashes",
                "URL doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new DispatcherRequestFixture("//a/b/c"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("empty class name",
                "URL doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new DispatcherRequestFixture("/a//b/c"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("empty method name",
                "URL doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new DispatcherRequestFixture("/a/b/c"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("simplest valid URL",
                "couldn't find class \"A\"",
                dispatcher.basicMessage);
    }
    public void test_service_nonexistentClass() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherRequestFixture("/missingClass/a"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find class \"MissingClass\"",
                dispatcher.basicMessage);
    }
    public void test_service_updateClassMap() {
        DispatcherFixture dispatcher = new DispatcherFixture();
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/incCount/extra"), new ServletResponseFixture());
        assertEquals("DispatcherTest1", dispatcher.getClassKeys());
    }
    public void test_service_invokeInit() {
        DispatcherFixture dispatcher = new DispatcherFixture();
        DispatcherTest1.count = 0;
        DispatcherTest1.initCount = 0;
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("init invoked during first request", 1,
                DispatcherTest1.initCount);
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("init not invoked during second request", 1,
                DispatcherTest1.initCount);
        assertEquals("method invoked during both requests", 2,
                DispatcherTest1.count);
    }
    public void test_service_scanMethods() {
        DispatcherFixture dispatcher = new DispatcherFixture();
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/incCount/extra"), new ServletResponseFixture());
        assertEquals("dispatcherTest1/incCount, " +
                "dispatcherTest1/ajaxIncCount, dispatcherTest1/resetCount" +
                ", dispatcherTest1/error", dispatcher.getMethodKeys());
    }
    public void test_service_methodNotFound() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/twoArgs"), new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find method \"twoArgs\" with proper signature " +
                "in class DispatcherTest1",
                dispatcher.basicMessage);
    }
    public void test_service_setAjax() {
        DispatcherFixture dispatcher = new DispatcherFixture();
        DispatcherTest1.count = 0;
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("non-Ajax request", false, DispatcherTest1.isAjax);
        dispatcher.service(new DispatcherRequestFixture(
                "/dispatcherTest1/ajaxIncCount"),
                new ServletResponseFixture());
        assertEquals("Ajax request", true, DispatcherTest1.isAjax);
        assertEquals("count variable", 2, DispatcherTest1.count);
    }
    public void test_service_exceptionInMethod() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherRequestFixture("/dispatcherTest1/error"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("error message", "error in method",
                dispatcher.basicMessage);
    }
    public void test_service_handlingException() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.service(new DispatcherRequestFixture("/dispatcherTest1/error"),
                new ServletResponseFixture());
        assertEquals("error message", "error in method",
                dispatcher.basicMessage);
        String message = dispatcher.fullMessage.replaceAll("\\r\\n", "\n");
        TestUtil.assertSubstring("error message",
                "unhandled exception for URL \"/a/b/c?day=Monday\"\n" +
                "java.lang.Error: error in method\n" +
                "\tat org.fiz.DispatcherTest1.error",
                message);
    }

    public void test_findClass() {
        Dispatcher dispatcher = new DispatcherFixture();
        Class<?> cl = dispatcher.findClass("org.fiz.Interactor", null);
        TestUtil.assertSubstring("name of found class", "org.fiz.Interactor",
                cl.getName());
        boolean gotException = false;
        try {
            dispatcher.findClass("bogus_xyz",
                    new DispatcherRequestFixture("/first/second"));
        }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/a/b/c\": can't find class " +
                    "\"bogus_xyz\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
