package org.fiz;
import java.io.File;
import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.log4j.Level;

/**
 * Junit tests for the Dispatcher class.
 */

public class DispatcherTest  extends junit.framework.TestCase {

    protected Dispatcher dispatcher;

    public void setUp() {
        dispatcher = new Dispatcher();
        dispatcher.logger.setLevel(Level.FATAL);
        Dispatcher.testMode = true;
        Config.setDataset("main", new Dataset("searchPackages", "org.fiz"));
    }

    public void test_UnsupportedUriError() {
        Error e = new Dispatcher.UnsupportedUrlError("/a/b/c", "smelled funny");
        assertEquals("exception message",
                "unsupported URL \"/a/b/c\": smelled funny", e.getMessage());
    }

    public void test_init() throws ServletException {
        ServletContextFixture context = new ServletContextFixture();
        ServletConfigFixture config = new ServletConfigFixture(context);
        dispatcher.init(config);
        assertEquals("Config path", "test/testData/WEB-INF/config",
                Config.getPath()[0]);
        assertEquals("home entry in main config dataset", "test/testData",
                Config.getDataset("main").check("home"));
        assertEquals("Css path", "test/testData/WEB-INF/css",
                Css.getPath()[0]);
    }

    public void test_destroy() {
        DispatcherTest1.count = 0;
        DispatcherTest1.destroyCount = 0;
        DispatcherTest2.destroyCount = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/xyz"),
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
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/bogus/a/b/c"), new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find method \"bogus\" with proper signature",
                dispatcher.basicMessage);
    }
    public void test_service_parseMethodNotEndingInSlash() {
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/bogus"), new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find method \"bogus\" with proper signature",
                dispatcher.basicMessage);
    }
    public void test_service_notEnoughInfoInUri() {
        dispatcher.service(new ServletRequestFixture("abc"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("no slashes",
                "URL doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new ServletRequestFixture("//a/b/c"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("empty class name",
                "URL doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new ServletRequestFixture("/a//b/c"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("empty method name",
                "URL doesn't contain class name and/or method name",
                dispatcher.basicMessage);
        dispatcher.basicMessage = null;
        dispatcher.service(new ServletRequestFixture("/a/b/c"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("simplest valid URL",
                "couldn't find class \"A\"",
                dispatcher.basicMessage);
    }
    public void test_service_nonexistentClass() {
        dispatcher.service(new ServletRequestFixture("/missingClass/a"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find class \"MissingClass\"",
                dispatcher.basicMessage);
    }
    public void test_service_updateClassMap() {
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount/extra"),
                new ServletResponseFixture());
        assertEquals("DispatcherTest1",
                StringUtil.join(dispatcher.classMap.keySet(), ", "));
    }
    public void test_service_invokeInit() {
        DispatcherTest1.count = 0;
        DispatcherTest1.initCount = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("init invoked during first request", 1,
                DispatcherTest1.initCount);
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("init not invoked during second request", 1,
                DispatcherTest1.initCount);
        assertEquals("method invoked during both requests", 2,
                DispatcherTest1.count);
    }
    public void test_service_scanMethods() {
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount/extra"),
                new ServletResponseFixture());
        assertEquals("dispatcherTest1/incCount, " +
                "dispatcherTest1/ajaxIncCount, dispatcherTest1/resetCount" +
                ", dispatcherTest1/error",
                StringUtil.join(dispatcher.methodMap.keySet(), ", "));
    }
    public void test_service_methodNotFound() {
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/twoArgs"), new ServletResponseFixture());
        TestUtil.assertSubstring("error message",
                "couldn't find method \"twoArgs\" with proper signature " +
                "in class DispatcherTest1",
                dispatcher.basicMessage);
    }
    public void test_service_setAjax() {
        DispatcherTest1.count = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("non-Ajax request", false, DispatcherTest1.isAjax);
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/ajaxIncCount"),
                new ServletResponseFixture());
        assertEquals("Ajax request", true, DispatcherTest1.isAjax);
        assertEquals("count variable", 2, DispatcherTest1.count);
    }
    public void test_service_exceptionInMethod() {
        dispatcher.service(new ServletRequestFixture("/dispatcherTest1/error"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("error message", "error in method",
                dispatcher.basicMessage);
    }
    public void test_service_handlingException() {
        dispatcher.service(new ServletRequestFixture("/dispatcherTest1/error"),
                new ServletResponseFixture());
        assertEquals("error message", "error in method",
                dispatcher.basicMessage);
        String message = dispatcher.fullMessage.replaceAll("\\r\\n", "\n");
        TestUtil.assertSubstring("error message",
                "unhandled exception for URL \"/x/y/z?a=b&c=d\"\n" +
                "java.lang.Error: error in method\n" +
                "\tat org.fiz.DispatcherTest1.error",
                message);
    }

    public void test_findClass() {
        Class<?> cl = dispatcher.findClass("org.fiz.Interactor", null);
        TestUtil.assertSubstring("name of found class", "org.fiz.Interactor",
                cl.getName());
        boolean gotException = false;
        try {
            dispatcher.findClass("bogus_xyz",
                    new ServletRequestFixture("/first/second"));
        }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": can't find class " +
                    "\"bogus_xyz\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
