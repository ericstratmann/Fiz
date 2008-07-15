package org.fiz;
import java.io.*;
import javax.servlet.*;

/**
 * Junit tests for the Dispatcher class.
 */

public class DispatcherTest  extends junit.framework.TestCase {

    protected Dispatcher dispatcher;

    public void setUp() throws ServletException {
        dispatcher = new Dispatcher();
        Dispatcher.testMode = true;
        dispatcher.init(new ServletConfigFixture(
                new ServletContextFixture()));
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
                Config.getSearchPath()[0]);
        assertEquals("home entry in main config dataset", "test/testData",
                Config.getDataset("main").check("home"));
        assertEquals("Css path", "test/testData/WEB-INF/css",
                Css.getSearchPath()[0]);
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
    public void test_service_setAjaxAndInvokeMethod() {
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
    public void test_service_exception_createClientRequest() {
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/ajaxBogus"),
                response);
        assertEquals("AJAX response", "var actions = [{type: \"error\", " +
                "properties: {message: \"uncaughtAjax: unsupported URL " +
                "\\\"/x/y/z\\\": couldn't find method \\\"ajaxBogus\\\" with " +
                "proper signature in class DispatcherTest2\"}}];",
                response.out.toString());
    }
    public void test_service_exception_HandledError() {
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/handledError"),
                new ServletResponseFixture());
        assertEquals("error message", null, dispatcher.basicMessage);
    }
    public void test_service_exception_logMessage() {
        dispatcher.service(new ServletRequestFixture("/dispatcherTest1/error"),
                new ServletResponseFixture());
        assertEquals("error message", "error in method",
                dispatcher.basicMessage);
        String message = dispatcher.fullMessage.replaceAll("\\r\\n", "\n");
        TestUtil.assertSubstring("error message",
                "unhandled exception for URL \"/x/y/z?a=b&c=d\":\n" +
                "java.lang.Error: error in method\n" +
                "\tat org.fiz.DispatcherTest1.error",
                message);
    }
    public void test_service_exception_ajaxResponse() {
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/ajaxBogus"),
                response);
        assertEquals("AJAX response", "var actions = [{type: \"error\", " +
                "properties: {message: \"uncaughtAjax: unsupported URL " +
                "\\\"/x/y/z\\\": couldn't find method \\\"ajaxBogus\\\" with " +
                "proper signature in class DispatcherTest2\"}}];",
                response.out.toString());
    }
    public void test_service_exception_htmlResponseClearFirst() {
        Config.setDataset("errors", new Dataset(
                "uncaughtHtml", "test: @message",
                "clearOnUncaught", "true"));
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                response);
        TestUtil.assertSubstring("HTML body", "<body>\n" +
                "<script type=\"text/javascript\" src=\"/context" +
                "/cpath/Fiz.js\"></script>\n" +
                "test: error in DispatcherTest2.x</body>\n",
                response.out.toString());
    }
    public void test_service_exception_htmlResponseDontClear() {
        Config.setDataset("errors", new Dataset(
                "uncaughtHtml", "test: @message",
                "clearOnUncaught", "false"));
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                response);
        TestUtil.assertSubstring("HTML body", "<body>\n" +
                "<script type=\"text/javascript\" src=\"/context/" +
                "cpath/Fiz.js\"></script>\n" +
                "<p>Sample text.</p>\n" +
                "test: error in DispatcherTest2.x</body>\n",
                response.out.toString());
    }
    public void test_service_exception_multipleErrors() {
        Config.setDataset("errors", new Dataset(
                "uncaughtHtml", "test: @message @bogus",
                "clearOnUncaught", "true"));
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                response);
        TestUtil.assertSubstring("HTML body", "<body>\n" +
                "<script type=\"text/javascript\" src=\"/context/cpath" +
                "/Fiz.js\"></script>\n" +
                "<div class=\"uncaughtException\">Multiple internal errors in " +
                "the server!  Details are in the server's log</div>.\n" +
                "</body>\n",
                response.out.toString());
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

    public void test_findMethod_basics() {
        DispatcherTest1.initCount = 0;
        Dispatcher.InteractorMethod method = dispatcher.findMethod(
                "dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init invoked during first call", 1,
                DispatcherTest1.initCount);
        assertEquals("method name from first call", "incCount",
                method.method.getName());
        assertEquals("class of Interactor object", "DispatcherTest1",
                method.interactor.getClass().getSimpleName());
        DispatcherTest1.initCount = 0;
        method = dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init not invoked during second call", 0,
                DispatcherTest1.initCount);
        assertEquals("method name from second call", "incCount",
                method.method.getName());
    }
    public void test_findMethod_nonexistentClass() {
        boolean gotException = false;
        try {
            dispatcher.findMethod("missingClass/a", 12,
                    new ServletRequestFixture("/missingClass/a"));
        }
        catch (ClassNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find class \"MissingClass\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findMethod_updateClassMap() {
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount/extra"));
        assertEquals("DispatcherTest1",
                StringUtil.join(dispatcher.classMap.keySet(), ", "));
    }
    public void test_findMethod_invokeInit() {
        DispatcherTest1.count = 0;
        DispatcherTest1.initCount = 0;
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init invoked during first request", 1,
                DispatcherTest1.initCount);
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init not invoked during second request", 1,
                DispatcherTest1.initCount);
    }
    public void test_findMethod_scanMethods() {
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount/extra"));
        assertEquals("dispatcherTest1/incCount, " +
                "dispatcherTest1/ajaxIncCount, " +
                "dispatcherTest1/handledError, " +
                "dispatcherTest1/resetCount, dispatcherTest1/error",
                StringUtil.join(dispatcher.methodMap.keySet(), ", "));
    }
    public void test_findMethod_methodNotFound() {
        Class<?> cl = dispatcher.findClass("org.fiz.Interactor", null);
        TestUtil.assertSubstring("name of found class", "org.fiz.Interactor",
                cl.getName());
        boolean gotException = false;
        try {
        dispatcher.findMethod("dispatcherTest1/twoArgs", 15,
                new ServletRequestFixture("/dispatcherTest1/twoArgs"));
        }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": couldn't find method " +
                    "\"twoArgs\" with proper signature in class " +
                    "DispatcherTest1", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
