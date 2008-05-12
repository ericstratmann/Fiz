package org.fiz;
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.ho.yaml.YamlEncoder;
import org.mozilla.javascript.*;

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

    public void showTime(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Ajax Test Page");
        String code = "new Fiz.Ajax({url: '/fiz/fiz/test/ajaxUpdateTime'}); " +
                "return false;";
        html.getBody().append("<h1>Ajax Demo</h1>\n" +
                "<script type=\"text/javascript\" src=\"/fiz/Ajax.js\"></script>\n" +
                "<p>This page demonstrates a simple Ajax updater.  " +
                "Click <a href=\"#\" onclick=\"" + code + "\">here</a> " +
                "to update the time below.</p>\n" +
                "<p>Latest date/time from Ajax: " +
                "<span id=\"updateMe\">None</span></p>\n");
    }

    public void ajaxUpdateTime(ClientRequest cr) throws IOException {
        cr.getServletResponse().getWriter().write(
                "actions = [{type: \"update\", id: \"updateMe\", " +
                "html: \"" + (new Date()).toString() + "\"}];");
    }

    // The following entry point generates 2 different pages (based on
    // the "current" query value) each of which references the other with
    // a link.
    public void link(ClientRequest cr) {
        Html html = cr.getHtml();
        Dataset globalData = cr.getMainDataset();
        globalData.set("name", "Alice");
        globalData.set("age", "21");
        globalData.set("saying", "\"All's well that ends well\"");
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
                "<p>This page demonstrates several uses of links.</p>\n" +
                "<p>This is page @current.  Click on the link below " +
                "to go to page @next.</p>\n" +
                "<p>", globalData, body);
        Link link = new Link(new Dataset("text", "Go to page @next",
                "url", "test/link?current=@next"));
        link.html(cr, globalData, body);
        body.append("</p>\n");
        body.append("<p>The following link uses Javascript to display " +
                "an alert: ");
        Link link2 = new Link(new Dataset("text", "Click me",
                "javascript",
                "alert(\"Her name is @name, favorite saying @saying\");"));
        link2.html(cr, globalData, body);
        body.append("</p>\n");
        Link link3 = new Link(new Dataset("text", "here",
                "ajaxUrl", "test/ajaxUpdateTime"));
        body.append("<script type=\"text/javascript\" src=\"/fiz/Ajax.js\">" +
                "</script>\n" +
                "<p>Click ");
        link3.html(cr, globalData, body);
        body.append(" to update the time below.</p>\n" +
                "<p>Latest date/time from Ajax: " +
                "<span id=\"updateMe\">None</span></p>\n");
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

    // Test the Rhino Javascript engine.
    public static void rhino() {
        Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects();
        Object result = cx.evaluateString(scope, "print(\"Hi there\");",
                "<cmd>", 1, null);
        System.out.printf("result: %s\n", cx.toString(result));
        Context.exit();
    }

    public static void main(String[] argv) throws IOException {
        rhino();
    }
}
