package org.fiz;
import java.io.*;
import org.apache.log4j.Logger;
import org.ho.yaml.YamlEncoder;

/**
 * This Interactor is used for temporary tests and has no long-term
 * significance for Fiz.  It can be run either as a stand-alone application
 * or as an Interactor invoked by the Dispatcher.
 */

public class Test extends Interactor {
    protected Logger logger = Logger.getLogger("Test");
    public void path(ClientRequest cr) {
        logger.info("getRealPath(\"/WEB-INF/a/b/c\"): "
                + cr.getServletContext().getRealPath("///WEB-INF/a/b/c"));
        logger.info("getPathTranslated(): "
                + cr.getServletRequest().getPathTranslated());
        logger.info("getServletPath(): "
                + cr.getServletRequest().getServletPath());
        logger.info("getContextPath(): "
                + cr.getServletContext().getContextPath());
        logger.info("working directory: "
                + System.getProperty("user.dir"));
        logger.info("getMimeType(foo.xls): "
                + cr.getServletContext().getMimeType("foo.xls"));
        logger.info("getMimeType(foo.html): "
                + cr.getServletContext().getMimeType("foo.html"));
    }

    public void page(ClientRequest cr) {
        Html html = cr.getHtml();
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
    public void link(ClientRequest cr) {
        Html html = cr.getHtml();
        Dataset globalData = cr.getMainDataset();
        String current = globalData.check("current");
        if ((current != null) && (current.equals("2"))) {
            globalData.set("next", "1");
        } else {
            globalData.set("next", "2");
            globalData.set("current", "1");
        }
        html.setTitle("Link Page");
        StringBuilder body = html.getBody();
        Template.expand("<h1>Test Links</h1>\n" +
                "<p>This is page @current.  Click on the link below " +
                "to go to page @next.</p>\n" +
                "<p>", globalData, body);
        Link link = new Link(new Dataset("text", "Go to page @next",
                "url", "test/link?current=@next"));
        link.html(cr, globalData, body);
        body.append("</p>\n");
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
        Dataset d = YamlDataset.newStringInstance(
                "name: Alice\n" +
                "weight: 125\n" +
                "child:\n" +
                "  - name: Bob\n" +
                "    age: 8\n" +
                "  - name: Carol\n" +
                "    age: 12\n");
        FileOutputStream output = new FileOutputStream("out.yaml");
        YamlEncoder encoder = new YamlEncoder(output);
        encoder.writeObject(d.map);
        encoder.close();
        output.close();
    }
}
