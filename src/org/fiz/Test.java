package org.fiz;
import java.io.*;
import org.apache.log4j.Logger;

/**
 * This Interactor is used for temporary tests and has no long-term
 * significance for Fiz.  It can be run either as a stand-alone application
 * or as an Interactor invoked by the Dispatcher.
 */

public class Test extends Interactor {
    protected Logger logger = Logger.getLogger("Test");
    public void path(ClientRequest request) {
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

    public void page(ClientRequest request) {
        Html html = request.getHtml();
        html.setTitle("Test Page");
        html.getBody().append("<h1>First Section</h1>\n" +
                "<p>Here are a couple of lines of sample text.</p>\n" +
                "<h2>Subsection</h2>\n" +
                "<p>Check to see that the " +
                "fonts and colors match the stylesheets.</p>\n");
    }

    // The following entry point generates 2 different pages (based on
    // the "current" query value) each of which references the other with
    // a link.
    public void link(ClientRequest request) {
        Html html = request.getHtml();
        Dataset globalData = request.getDataset();
        if (globalData.get("current").equals("1")) {
            globalData.set("next", "2");
        } else {
            globalData.set("next", "1");
        }
        html.setTitle("Link Page");
        StringBuilder body = html.getBody();
        Template.expand("<h1>Test Links</h1>\n" +
                "<p>This is page @current.  Click on the link below " +
                "to go to page @next.</p>\n" +
                "<p>", globalData, body);
        Link link = new Link(new Dataset("text", "Go to page @next",
                "base", "link", "args", "current: next"));
        link.html(globalData, body);
        body.append("</p>\n");
    }

    public void test(ClientRequest request) throws IOException {
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
