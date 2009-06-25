package org.fiz;

import java.io.*;
import javax.servlet.*;
import org.apache.log4j.*;

import org.fiz.test.*;

/**
 * Junit tests for the StaticServlet class.
 */
public class StaticServletTest extends junit.framework.TestCase {
    protected StaticServlet servlet;
    protected ServletConfigFixture config;
    protected ServletContextFixture context;
    protected ServletResponseFixture response;
    protected StringAppender log4jLog;

    public void setUp() throws ServletException {
        // Set up a StaticServlet, along with its context and config.
        context = new ServletContextFixture();
        config = new ServletConfigFixture(context);
        servlet = new StaticServlet();
        servlet.init(config);
        response = new ServletResponseFixture();

        // Redirect the servlet's log so we can read any output
        // generated.
        log4jLog = new StringAppender();
        StaticServlet.logger = Logger.getRootLogger();
        StaticServlet.logger.removeAllAppenders();
        StaticServlet.logger.addAppender(log4jLog);
    }

    public void test_init_absolutePath() throws ServletException {
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            config.parameterValue = "C:/Temp";
        } else {
            config.parameterValue = "/abc/def";
        }
        servlet.init(config);
        assertEquals("prefix value", config.parameterValue, servlet.prefix);
    }
    public void test_init_relativePath() throws ServletException {
        config.parameterValue = "x/y/z";
        servlet.init(config);
        assertEquals("prefix value", "test/testData/x/y/z", servlet.prefix);
    }
    public void test_init_noBaseParameter() throws ServletException {
        config.parameterValue = null;
        servlet.init(config);
        assertEquals("prefix value", "test/testData/static", servlet.prefix);
    }

    public void test_doGet_basics() {
        (new File("_test_")).mkdir();
        TestUtil.writeFile("_test_/abc", "Sample file to return.");
        servlet.prefix = "_test_/";
        servlet.doGet(new ServletRequestFixture("abc"), response);
        TestUtil.deleteTree("_test_");
        assertEquals("information returned", "Sample file to return.",
                response.toString());
    }
    public void test_doGet_noPathInfo() {
        TestUtil.writeFile("_test_", "Data for empty path.");
        servlet.prefix = "_test_";
        servlet.doGet(new ServletRequestFixture(null), response);
        TestUtil.deleteTree("_test_");
        assertEquals("information returned", "Data for empty path.",
                response.toString());
    }

    public void test_returnFile_badFileName() throws ServletException {
        StaticServlet.returnFile("bogus/x/y/z", context, response);
        assertEquals("response log", "sendError(404)",
                response.log.toString());
    }
    public void test_returnFile_cantOpenOutputStream() throws ServletException {
        TestUtil.writeFile("_test_", "Sample file to return.");
        response.getOutputStreamException = true;
        StaticServlet.returnFile("_test_", context, response);
        assertEquals("log4j log output",
                "I/O error retrieving response output stream in " +
                "StaticServlet.returnFile: getOutputStream failed",
                log4jLog.log.toString());
        TestUtil.deleteTree("_test_");
    }
    public void test_returnFile_setMimeType() {
        TestUtil.writeFile("_test_.xls", "Sample file to return.");
        StaticServlet.returnFile("_test_.xls", context, response);
        assertEquals("content type for response", "application/vnd.ms-excel",
                response.contentType);
        TestUtil.deleteTree("_test_.xls");
    }
    public void test_returnFile_defaultMimeType() {
        TestUtil.writeFile("_test_.xxx", "Sample file to return.");
        StaticServlet.returnFile("_test_.xxx", context, response);
        assertEquals("content type for response", "application/octet-stream",
                response.contentType);
        TestUtil.deleteTree("_test_.xxx");
    }
    public void test_returnFile_contentDispositionHeader() {
        TestUtil.writeFile("_test_", "Sample file to return.");
        StaticServlet.returnFile("_test_", context, response);
        assertEquals("log info about headers",
                "getOutputStream; setContentType(\"application/octet-" +
                "stream\"); setHeader(\"Content-Disposition\", " +
                "\"filename=\"_test_\"\")",
                response.log.toString());
        TestUtil.deleteTree("_test_");
    }
    public void test_returnFile_sendFileData() {
        TestUtil.writeFile("_test_", "Sample file to return.");
        StaticServlet.returnFile("_test_", context, response);
        assertEquals("information returned", "Sample file to return.",
                response.toString());
        TestUtil.deleteTree("_test_");
    }
    public void test_returnFile_errorSendingFileData() {
        TestUtil.writeFile("_test_", "Sample file to return.");
        response.stream.setWriteError();
        StaticServlet.returnFile("_test_", context, response);
        assertEquals("log4j log output",
                "I/O error sending response in StaticServlet.returnFile: " +
                "error during write",
                log4jLog.log.toString());
        TestUtil.deleteTree("_test_");
    }
    public void test_returnFile_errorFlushingOutputStream() {
        TestUtil.writeFile("_test_", "Sample file to return.");
        response.stream.setFlushError();
        StaticServlet.returnFile("_test_", context, response);
        assertEquals("log4j log output",
                "I/O error flushing output stream in StaticServlet." +
                "returnFile: error during flush",
                log4jLog.log.toString());
        TestUtil.deleteTree("_test_");
    }

    public void test_return404() {
        StaticServlet.return404(response);
        assertEquals("log output", "sendError(404)", response.log.toString());
    }
    public void test_return404_exceptionReturningError() {
        response.sendErrorException = true;
        StaticServlet.return404(response);
        assertEquals("log4j log output",
                "I/O error sending 404 error in StaticServlet.return404: " +
                "exception in sendError",
                log4jLog.log.toString());
    }
}
