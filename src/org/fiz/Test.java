/**
 * This Interactor is used for temporary tests and has no long-term
 * significance for Fiz.  It can be used either as a stand-alone application
 * or as an Interactor invoked by the Dispatcher.
 */

package org.fiz;

import javax.servlet.*;
import java.io.*;
import org.apache.log4j.Logger;

public class Test extends Interactor {
    protected Logger logger = Logger.getLogger("Test");
    public void path(Request request) {
        logger.info("getRealPath(\"/WEB-INF/a/b/c\"): "
                + request.getServletContext().getRealPath("///WEB-INF/a/b/c"));
        logger.info("getPathTranslated(): "
                + request.getServletRequest().getPathTranslated());
        logger.info("getContextPath(): "
                + request.getServletContext().getContextPath());
        logger.info("getMimeType(foo.xls): "
                + request.getServletContext().getMimeType("foo.xls"));
        logger.info("getMimeType(foo.html): "
                + request.getServletContext().getMimeType("foo.html"));
    }

    public void page(Request request) {
        Html html = request.getHtml();
        html.setTitle("Test Page");
        html.getBody().append("<h1>First Section</h1>\n"
                + "<p>Here are a couple of lines of sample text.</p>\n"
                + "<h2>Subsection</h2>\n"
                + "<p>Check to see that the "
                + "fonts and colors match the stylesheets.</p>\n");
    }

    public void test(Request request) throws IOException {
        request.setContentType("text/html");
        request.setHeader("Transfer-Encoding", "chunked");
        PrintWriter writer = request.getWriter();
        writer.write("abcdef\n");
        writer.flush();
    }

    static class TestFilter extends FilterOutputStream {
        public boolean invoked = false;
        public TestFilter(OutputStream out) {
            super(out);
        }
        public void write(int b) {
            invoked = true;
            System.out.printf("write(int b): %x\n", b);
        }
        public void write(byte[] b) {
            invoked = true;
            System.out.printf("write(byte[] b):\n");
            for (int i = 0; i < b.length; i++) {
                System.out.printf("  %x\n", b[i]);
            }
        }
        public void write(byte[] b, int first, int count) {
            invoked = true;
            System.out.printf("write(byte[] b, int first: %d, int count: %d):\n",
                    first, count);
            for (int i = first; i < first + count; i++) {
                System.out.printf("  %x\n", b[i]);
            }
        }
    }

    public static void main(String[] argv) throws IOException {
        System.out.printf("os.name: %s\n", System.getProperty("os.name"));
        System.out.printf("os.version: %s\n", System.getProperty("os.version"));
        System.out.printf("os.arch: %s\n", System.getProperty("os.arch"));
    }
}
