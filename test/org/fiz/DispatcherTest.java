/* Copyright (c) 2008-2010 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz;

import javax.servlet.*;

import java.io.File;
import java.util.*;
import org.apache.log4j.*;

import org.fiz.test.*;

/**
 * Junit tests for the Dispatcher class.
 */

public class DispatcherTest extends junit.framework.TestCase {

    protected Dispatcher dispatcher;

    public void setUp() throws ServletException {
        dispatcher = new Dispatcher();
        Dispatcher.testMode = true;
        dispatcher.clearCaches = false;
        dispatcher.init(new ServletConfigFixture(
                new ServletContextFixture()));
        ClientRequest.testSkipTokenCheck = true;
        Config.setDataset("main", new Dataset("searchPackages", "org.fiz"));
    }

    public void test_UnsupportedUriError() {
        Error e = new Dispatcher.UnsupportedUrlError("/a/b/c", "smelled funny");
        assertEquals("exception message",
                "unsupported URL \"/a/b/c\": smelled funny", e.getMessage());
    }

    public void test_init_no_exts() throws ServletException {
        // Ensure that no WEB-INF/ext directory was accidentally left around.
        String toDelete = "test/testData/WEB-INF/ext";
        if (new File(toDelete).exists()) {
            assertTrue("WEB-INF/ext deleted", Util.deleteTree(toDelete));
        }

        ServletContextFixture context = new ServletContextFixture();
        ServletConfigFixture config = new ServletConfigFixture(context);
        dispatcher.init(config);

        assertEquals("Config path", "test/testData/WEB-INF/app/config",
                Config.getSearchPath()[0]);
        assertEquals("Css path", "test/testData/WEB-INF/app/css",
                Css.getSearchPath()[0]);
        assertEquals("Config path length", 3, Config.getSearchPath().length);
        assertEquals("Css path length", 3, Css.getSearchPath().length);

        assertEquals("clearCaches variable", false,
                dispatcher.clearCaches);
    }

    public void test_init_with_exts() throws ServletException {
        ServletContextFixture context = new ServletContextFixture();
        ServletConfigFixture config = new ServletConfigFixture(context);

        // Test to ensure that the extensions' config and css directories
        // can be found.
        assertTrue("extConfig created", new File("test/testData/WEB-INF/ext/sampleExt/config").mkdirs());
        assertTrue("extCss created", new File("test/testData/WEB-INF/ext/sampleExt/css").mkdirs());

        dispatcher.init(config);

        assertEquals("Config path", "test/testData/WEB-INF/app/config",
                Config.getSearchPath()[0]);
        assertEquals("Css path", "test/testData/WEB-INF/app/css",
                Css.getSearchPath()[0]);
        assertEquals("Ext config path", "sampleExt/config",
                Config.getSearchPath()[3].substring(26));
        assertEquals("Ext css path", "sampleExt/css",
                Css.getSearchPath()[3].substring(26));
        assertEquals("Config path length", 4, Config.getSearchPath().length);
        assertEquals("Css path length", 4, Css.getSearchPath().length);

        assertEquals("clearCaches variable", false,
                dispatcher.clearCaches);

        TestUtil.deleteTree("test/testData/WEB-INF/ext");
    }

    // Node tests for clearCaches: it is already exercised elsewhere.

    public void test_destroy() {
        DispatcherTest1Interactor.count = 0;
        DispatcherTest1Interactor.destroyCount = 0;
        DispatcherTest2Interactor.destroyCount = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/xyz"),
                new ServletResponseFixture());
        assertEquals("first interactor not yet destroyed", 0,
                DispatcherTest1Interactor.destroyCount);
        assertEquals("second interactor not yet destroyed", 0,
                DispatcherTest2Interactor.destroyCount);
        dispatcher.destroy();
        assertEquals("first interactor destroyed", 1,
                DispatcherTest1Interactor.destroyCount);
        assertEquals("second interactor destroyed", 3,
                DispatcherTest2Interactor.destroyCount);
    }

    public void test_service_clearCaches() {
        dispatcher.clearCaches = true;
        Config.setDataset("test", new Dataset());
        Config.setDataset("main", new Dataset());
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("Config cache size (caches cleared)", 1,
                Config.cache.size());
        dispatcher.clearCaches = false;
        Config.setDataset("test", new Dataset());
        Config.setDataset("main", new Dataset());
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("Config cache size (caches no longer cleared)", 2,
                Config.cache.size());
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
    public void test_service_redirectHomePage() {
        Config.setDataset("main", new Dataset("homeRedirectUrl", "x/y"));
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/"), response);
        assertEquals("log information",
                "setCharacterEncoding(\"UTF-8\"); sendRedirect(\"x/y\")",
                response.log.toString());
    }
    public void test_service_redirectHomePageNoConfigInfo() {
        dispatcher.service(new ServletRequestFixture(null),
                new ServletResponseFixture());
        TestUtil.assertSubstring("no path info, no homeRedirectUrl",
                "URL doesn't contain class name and/or method name",
                dispatcher.basicMessage);
    }
    public void test_service_notEnoughInfoInUrl() {
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
        dispatcher.service(new ServletRequestFixture("/a"),
                new ServletResponseFixture());
        TestUtil.assertSubstring("class but no method",
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
                "couldn't find class \"AInteractor\"",
                dispatcher.basicMessage);
    }
    public void test_service_setAjaxAndInvokeMethod() {
        DispatcherTest1Interactor.count = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals("non-Ajax request", false, DispatcherTest1Interactor.isAjax);
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/ajaxIncCount"),
                new ServletResponseFixture());
        assertEquals("Ajax request", true, DispatcherTest1Interactor.isAjax);
        assertEquals("count variable", 2, DispatcherTest1Interactor.count);
    }
    public void test_service_StartMethodsCalled() {
        Config.setDataset("main", new Dataset("globalRequestWrapper",
                                              "DispatcherTest1Interactor"));
        DispatcherTest1Interactor.startCount = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals(2, DispatcherTest1Interactor.startCount);
    }
    public void test_service_EndMethodsCalled() {
        Config.setDataset("main", new Dataset("globalRequestWrapper",
                                              "DispatcherTest1Interactor"));
        DispatcherTest1Interactor.endCount = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/incCount"), new ServletResponseFixture());
        assertEquals(2, DispatcherTest1Interactor.endCount);
    }
    public void test_service_setPostType() {
        DispatcherTest1Interactor.count = 0;
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/postTest"), new ServletResponseFixture());
        assertEquals("request type", ClientRequest.Type.POST,
                DispatcherTest1Interactor.requestType);
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
        assertEquals("AJAX response",
                "Fiz.addBulletinMessage(\"uncaughtAjax: " +
                "unsupported URL &quot;/x/y/z&quot;: couldn't find method " +
                "&quot;ajaxBogus&quot; with proper signature in class " +
                "DispatcherTest2Interactor\");\n",
                response.toString());
    }
    public void test_service_exception_HandledError() {
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/handledError"),
                new ServletResponseFixture());
        assertEquals("error message", null, dispatcher.basicMessage);
    }
    public void test_service_exception_dontLogUserError() {
        StringAppender appender = new StringAppender();
        dispatcher.logger = Logger.getRootLogger();
        dispatcher.logger.removeAllAppenders();
        dispatcher.logger.addAppender(appender);
        dispatcher.logger.setLevel(Level.WARN);
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/userError"), new ServletResponseFixture());
        assertEquals("log message", "",
                appender.log.toString());
    }
    public void test_service_exception_logMessage() {
        StringAppender appender = new StringAppender();
        dispatcher.logger = Logger.getRootLogger();
        dispatcher.logger.removeAllAppenders();
        dispatcher.logger.addAppender(appender);
        dispatcher.service(new ServletRequestFixture("/dispatcherTest1/error"),
                new ServletResponseFixture());
        assertEquals("error message", "error in method",
                dispatcher.basicMessage);
        String message = appender.log.toString().replaceAll("\\r\\n", "\n");
        TestUtil.assertSubstring("log message",
                "unhandled exception for URL \"/x/y/z?a=b&c=d\":\n" +
                "java.lang.Error: error in method\n" +
                "\tat org.fiz.DispatcherTest1Interactor.error",
                message);
    }
    public void test_service_exception_ajaxUserError() {
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest1/ajaxUserError"), response);
        assertEquals("AJAX response",
                "Fiz.addBulletinMessage(\"userError: ajax user error\");\n",
                response.toString());
    }
    public void test_service_exception_ajaxInternalError() {
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture(
                "/dispatcherTest2/ajaxBogus"), response);
        assertEquals("AJAX response",
                "Fiz.addBulletinMessage(\"uncaughtAjax: " +
                "unsupported URL &quot;/x/y/z&quot;: couldn't find method " +
                "&quot;ajaxBogus&quot; with proper signature in class " +
                "DispatcherTest2Interactor\");\n",
                response.toString());
    }
    public void test_service_exception_postUserError() {
        ServletResponseFixture response = new ServletResponseFixture();
        ServletRequestFixture request = new ServletRequestFixture(
                "/dispatcherTest1/postUserError");
        request.parameterMap = new Hashtable<String,String>();
        dispatcher.service(request, response);
        TestUtil.assertMatchingSubstring("Response Javascript",
                "window.parent.Fiz.FormSection.handleResponse(" +
                "\"Fiz.addBulletinMessage(\\\"userError: post user " +
                "error\\\");\\n\");",
                response.toString(), "window.parent.Fiz[^\n]*");
    }
    public void test_service_exception_postInternalError() {
        ServletResponseFixture response = new ServletResponseFixture();
        ServletRequestFixture request = new ServletRequestFixture(
                "/dispatcherTest2/postBogus");
        request.parameterMap = new Hashtable<String,String>();
        dispatcher.service(request, response);
        TestUtil.assertMatchingSubstring("Response Javascript",
                "window.parent.Fiz.FormSection.handleResponse(" +
                "\"Fiz.addBulletinMessage(\\\"uncaughtPost: unsupported URL " +
                "&quot;/x/y/z&quot;: couldn't find method &quot;postBogus" +
                "&quot; with proper signature in class " +
                "DispatcherTest2Interactor\\\");\\n\");",
                response.toString(), "window.parent.Fiz[^\n]*");
    }
    public void test_service_exception_htmlResponseClearFirst() {
        Config.setDataset("styles", new Dataset("uncaught",
                new Dataset("html", "test: @message",
                "clearHtml", "true")));
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                response);
        TestUtil.assertSubstring("HTML body", "<body>\n" +
                "test: error in DispatcherTest2.x</body>\n",
                response.toString());
    }
    public void test_service_exception_htmlResponseDontClear() {
        Config.setDataset("styles", new Dataset("uncaught",
                new Dataset("html", "test: @message",
                "clearHtml", "false")));
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                response);
        TestUtil.assertSubstring("HTML body", "<body>\n" +
                "<p>Sample text.</p>\n" +
                "test: error in DispatcherTest2.x</body>\n",
                response.toString());
    }
    public void test_service_exception_multipleErrors() {
        Config.setDataset("styles", new Dataset("uncaught",
                new Dataset("html", "test: @message @bogus",
                "clearHtml", "true")));
        ServletResponseFixture response = new ServletResponseFixture();
        dispatcher.service(new ServletRequestFixture("/dispatcherTest2/x"),
                response);
        TestUtil.assertSubstring("HTML body", "<body>\n" +
                "<div class=\"uncaughtException\">Multiple internal errors in " +
                "the server!  Details are in the server's log</div>.\n" +
                "</body>\n",
                response.toString());
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
        DispatcherTest1Interactor.initCount = 0;
        Dispatcher.UrlMethod method = dispatcher.findMethod(
                "dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init invoked during first call", 1,
                DispatcherTest1Interactor.initCount);
        assertEquals("method name from first call", "incCount",
                method.method.getName());
        assertEquals("class of Interactor object", "DispatcherTest1Interactor",
                method.interactor.getClass().getSimpleName());
        DispatcherTest1Interactor.initCount = 0;
        method = dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init not invoked during second call", 0,
                DispatcherTest1Interactor.initCount);
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
                    "couldn't find class \"MissingClassInteractor\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findMethod_updateClassMap() {
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount/extra"));
        assertEquals("DispatcherTest1Interactor",
                StringUtil.join(dispatcher.interactorMap.keySet(), ", "));
    }
    public void test_findMethod_invokeInit() {
        DispatcherTest1Interactor.count = 0;
        DispatcherTest1Interactor.initCount = 0;
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init invoked during first request", 1,
                DispatcherTest1Interactor.initCount);
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount"));
        assertEquals("init not invoked during second request", 1,
                DispatcherTest1Interactor.initCount);
    }
    public void test_findMethod_scanMethods() {
        dispatcher.findMethod("dispatcherTest1/incCount", 15,
                new ServletRequestFixture("/dispatcherTest1/incCount/extra"));
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(dispatcher.methodMap.keySet());
        Collections.sort(names);
        assertEquals("dispatcherTest1/ajaxIncCount, dispatcherTest1/ajaxUserError, " +
                    "dispatcherTest1/end, dispatcherTest1/endAjax, " +
                    "dispatcherTest1/endPost, dispatcherTest1/error, " +
                    "dispatcherTest1/handledError, dispatcherTest1/incCount, " +
                    "dispatcherTest1/postTest, dispatcherTest1/postUserError, " +
                    "dispatcherTest1/resetCount, dispatcherTest1/start, " +
                    "dispatcherTest1/startAjax, dispatcherTest1/startPost, " +
                    "dispatcherTest1/userError", StringUtil.join(names, ", "));
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
                    "DispatcherTest1Interactor", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_findDirectMethod_basics() {
        String name = "DispatcherTest3/ajaxRequest";
        Dispatcher.UrlMethod method = dispatcher.findDirectMethod(
                name, name.indexOf("/"),
                new ServletRequestFixture());
        assertEquals("method name", "ajaxRequest", method.method.getName());
        assertEquals("DispatcherTest3/ajaxRequest",
                StringUtil.join(dispatcher.methodMap.keySet(), ", "));
    }
    public void test_findDirectMethod_methodNameDoesntStartWithAjax() {
        boolean gotException = false;
        try {
            String name = "DispatcherTest3/bogus";
            dispatcher.findDirectMethod(name, name.indexOf("/"),
                    new ServletRequestFixture());
            }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": method name must start " +
                    "with \"ajax\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findDirectMethod_badClassName() {
        boolean gotException = false;
        try {
            String name = "Bogus/ajaxRequest";
            dispatcher.findDirectMethod(name, name.indexOf("/"),
                    new ServletRequestFixture());
            }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": can't find " +
                    "class \"Bogus\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findDirectMethod_classDoesntImplementDirectAjax() {
        boolean gotException = false;
        try {
            String name = "IOError/ajaxRequest";
            dispatcher.findDirectMethod(name, name.indexOf("/"),
                    new ServletRequestFixture());
            }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": class IOError doesn't " +
                    "implement DirectAjax interface", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findDirectMethod_noSuchMethod() {
        boolean gotException = false;
        try {
            String name = "DispatcherTest3/ajaxBogus";
            dispatcher.findDirectMethod(name, name.indexOf("/"),
                    new ServletRequestFixture());
            }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": couldn't find " +
                    "method \"ajaxBogus\" with proper signature in class " +
                    "DispatcherTest3", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findDirectMethod_badMethodSignature() {
        boolean gotException = false;
        try {
            String name = "DispatcherTest3/ajaxExtraArgument";
            dispatcher.findDirectMethod(name, name.indexOf("/"),
                    new ServletRequestFixture());
            }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": couldn't find " +
                    "method \"ajaxExtraArgument\" with proper signature " +
                    "in class DispatcherTest3", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findDirectMethod_badMethodSignature2() {
        boolean gotException = false;
        try {
            String name = "DispatcherTest3/ajaxBadSignature";
            dispatcher.findDirectMethod(name, name.indexOf("/"),
                    new ServletRequestFixture());
            }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": couldn't find " +
                    "method \"ajaxBadSignature\" with proper signature " +
                    "in class DispatcherTest3", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findDirectMethod_methodNotStatic() {
        boolean gotException = false;
        try {
            String name = "DispatcherTest3/ajaxNotStatic";
            dispatcher.findDirectMethod(name, name.indexOf("/"),
                    new ServletRequestFixture());
            }
        catch (Dispatcher.UnsupportedUrlError e) {
            assertEquals("exception message",
                    "unsupported URL \"/x/y/z\": method \"ajaxNotStatic\" " +
                    "in class DispatcherTest3 isn't static", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findDirectMethod_invokeMethod() {
        dispatcher.service(new ServletRequestFixture(
                "/DispatcherTest3/ajaxRequest"), new ServletResponseFixture());
        assertEquals("DispatcherTest3 log", "Invoked ajaxRequest",
                DispatcherTest3.log);
    }

    public void test_initMainConfigDataset_compound() {
        Config.setDataset("main", new CompoundDataset(new Dataset("a", "1"),
                new Dataset("b", "2")));
        Dispatcher.initMainConfigDataset("/a/b/c");
        assertEquals("updated main dataset", "Component #0:\n" +
                "  a:    1\n" +
                "  home: /a/b/c\n" +
                "Component #1:\n" +
                "  b: 2\n",
                Config.getDataset("main").toString());
    }
    public void test_initMainConfigDataset_simpleDataset() {
        Config.setDataset("main", new Dataset("a", "1"));
        Dispatcher.initMainConfigDataset("/a/b/c");
        assertEquals("updated main dataset", "a:    1\n" +
                "home: /a/b/c\n",
                Config.getDataset("main").toString());
    }

    // helper method
    private DispatcherTest1Interactor getTest1Interactor() {
        return (DispatcherTest1Interactor)  Util.newInstance(
               "org.fiz.DispatcherTest1Interactor", "DispatcherTest1Interactor");
    }


    public void test_getGlobalRequestWrapper_noWrapper() {
        Config.setDataset("main", new Dataset());
        assertEquals(null, dispatcher.getGlobalRequestWrapper());
    }

    public void test_getGlobalRequestWrapper_notInMap() {
        Config.setDataset("main", new Dataset("globalRequestWrapper",
                                              "DispatcherTest1Interactor"));
        assertTrue(dispatcher.getGlobalRequestWrapper() instanceof
                   org.fiz.DispatcherTest1Interactor);
    }

    public void test_getGlobalRequestWrapper_inMap() {
        Config.setDataset("main", new Dataset("globalRequestWrapper",
                                              "DispatcherTest1Interactor"));
        Interactor interactor =
            (Interactor) Util.newInstance("DispatcherTest2Interactor",
                    "org.fiz.Interactor");
        dispatcher.interactorMap.put("DispatcherTest1Interactor", interactor);
        assertTrue(dispatcher.getGlobalRequestWrapper() instanceof
                   org.fiz.DispatcherTest2Interactor);
    }


    public void test_invokeStartMethod_noInteractor() {
        ClientRequest cr = new ClientRequestFixture();
        dispatcher.invokeStartMethod(null, cr);
        // Make sure we don't throw any exceptions
    }

    public void test_invokeStartMethod() {
        ClientRequest cr = new ClientRequestFixture();
        DispatcherTest1Interactor interactor = getTest1Interactor();

        cr.setClientRequestType(ClientRequest.Type.NORMAL);
        DispatcherTest1Interactor.startCount = 0;
        dispatcher.invokeStartMethod(interactor, cr);
        assertEquals("normal request", 1, interactor.startCount);

        cr.setClientRequestType(ClientRequest.Type.AJAX);
        DispatcherTest1Interactor.ajaxStartCount = 0;
        dispatcher.invokeStartMethod(interactor, cr);
        assertEquals("ajax request", 1, interactor.ajaxStartCount);

        cr.setClientRequestType(ClientRequest.Type.POST);
        DispatcherTest1Interactor.postStartCount = 0;
        dispatcher.invokeStartMethod(interactor, cr);
        assertEquals("post request", 1, interactor.postStartCount);
    }

    public void test_invokeEndMethod_noInteractor() {
        ClientRequest cr = new ClientRequestFixture();
        dispatcher.invokeEndMethod(null, cr);
        // Make sure we don't throw any exceptions
    }

    public void test_invokeEndMethod() {
        ClientRequest cr = new ClientRequestFixture();
        DispatcherTest1Interactor interactor = getTest1Interactor();

        cr.setClientRequestType(ClientRequest.Type.NORMAL);
        DispatcherTest1Interactor.endCount = 0;
        dispatcher.invokeEndMethod(interactor, cr);
        assertEquals("normal request", 1, interactor.endCount);

        cr.setClientRequestType(ClientRequest.Type.AJAX);
        DispatcherTest1Interactor.ajaxEndCount = 0;
        dispatcher.invokeEndMethod(interactor, cr);
        assertEquals("ajax request", 1, interactor.ajaxEndCount);

        cr.setClientRequestType(ClientRequest.Type.POST);
        DispatcherTest1Interactor.postEndCount = 0;
        dispatcher.invokeEndMethod(interactor, cr);
        assertEquals("post request", 1, interactor.postEndCount);
    }

}
