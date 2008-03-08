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
        logger.info("getContextPath(): " + request.getContextPath());
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
        ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
        TestFilter stream = new TestFilter(bufferStream);
        OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
        writer.write("abc\u00ffxyz\uaaaa");
        while (!stream.invoked) {
            writer.write("abc");
        }
        System.out.printf("about to flush\n", bufferStream.size());
        writer.flush();
        System.out.printf("bytes buffered: %d\n", bufferStream.size());
    }
}
