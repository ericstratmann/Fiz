/* Copyright (c) 2009 Stanford University
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
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.mozilla.javascript.*;

/**
 * This Interactor is used for temporary tests and has no long-term
 * significance for Fiz.  It can be run either as a stand-alone application
 * or as an Interactor invoked by the Dispatcher.
 */

public class TestInteractor extends Interactor {
    protected Logger logger = Logger.getLogger("TestInteractor");

    // The following fields are used for measuring Ajax latency.
    int ajaxCount = 0;
    long startTime;

    public void path(ClientRequest cr) {
        logger.info("getRealPath(\"a/b/c\"): "
                + cr.getServletContext().getRealPath("a/b/c"));
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

    // Display form with a multiple-select element in it, so we can see
    // how these values are handled.
    public void select(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder body = html.getBody();
        html.setTitle("Test Form Selct Element");
        body.append("<form id=\"form1\" name=\"form1\">\n" +
                "<select id=\"select1\" name=\"s1\" size=\"10\" " +
                "multiple=\"multiple\">\n" +
                "<option id=\"option1\" value=\"alice\">Alice</option>\n" +
                "<option id=\"option2\" value=\"bob\">Bob</option>\n" +
                "<option id=\"option3\" value=\"carol\">Carol</option>\n" +
                "<option id=\"option4\" value=\"david\">David</option>\n" +
                "</select>\n" +
                "<br> <input type=\"checkbox\" id=\"check1\" name=\"married\" " +
                "       value=\"yes\" />Married?\n" +
                "</form>\n");
        Link link = new Link(new Dataset("text", "See select value",
                "javascript", "alert(\"Select value: \" + " +
                "document.getElementById(\"select1\").value);"));
        link.render(cr, cr.getMainDataset(), body);
        body.append("<br />\n");
        Link link2 = new Link(new Dataset("text", "See checkbox value",
                "javascript", "alert(\"Checkbox value: \" + " +
                "document.getElementById(\"check1\").value);"));
        link2.render(cr, cr.getMainDataset(), body);
    }

    public void showTime(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder body = html.getBody();
        html.setTitle("Ajax Test Page");
        Link link = new Link(new Dataset("text", "Click here",
                "ajaxUrl", "test/ajaxUpdateTime"));
        body.append("<h1>Ajax Demo</h1>\n" +
                "<p>This page demonstrates a simple Ajax updater.  ");
        link.render(cr, cr.getMainDataset(), body);
        body.append(" to update the time below.</p>\n" +
                "<p>Latest date/time from Ajax: " +
                "<span id=\"updateMe\">None</span></p>\n");
    }

    // This entry point is used to initiate an Ajax performance
    // measurement.
    public void ajaxPerf(ClientRequest cr) {
        ajaxCount = 0;
        startTime = System.nanoTime();
        cr.updateElement("perf", "<br>Measuring ...");
        cr.evalJavascript("new Fiz.Ajax(\"ajaxPerf2\");");
    }

    // This entry point is visited repeatedly during Ajax performance
    // testing.  Each visit responds with a request for another visit,
    // until the desired number of round-trips has occurred.
    public void ajaxPerf2(ClientRequest cr) {
        ajaxCount++;
        final int count = 100;
        if (ajaxCount < count) {
            cr.evalJavascript("new Fiz.Ajax(\"ajaxPerf2\");");
            return;
        }

        // Compute statistics and display them on the page.
        long endTime = System.nanoTime();
        cr.updateElement("perf",
                String.format("<br>Time per round-trip: %.2fms",
                (endTime - startTime)/(count*1000000.0)));
    }

    public void ajaxUpdateTime(ClientRequest cr) throws IOException {
        cr.updateElement("updateMe", (new Date()).toString());
    }

    // Return the contents of a file, specified with the "name"
    // query value.
    public void file(ClientRequest cr) throws IOException {
        String name = cr.getMainDataset().check("name");
        if (name == null) {
            name = "src/src/org/fiz/Dataset.java";
        }
        cr.returnFile(name, new  FileInputStream("C:/Documents and " +
                "Settings/John Ousterhout/My Documents/Fiz/" + name));
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
        html.includeJsFile("static/fiz/Ajax.js");
        StringBuilder body = html.getBody();
        Template.appendHtml(body, "<h1>Test Links</h1>\n" +
                "<p>This page demonstrates several uses of links.</p>\n" +
                "<p>This is page @current.  Click on the link below " +
                "to go to page @next.</p>\n" +
                "<p>", globalData);
        Link link = new Link(new Dataset("text", "Go to page @next",
                "url", "link?current=@next"));
        link.render(cr, globalData, body);
        body.append("</p>\n");
        body.append("<p>The following link uses Javascript to display " +
                "an alert: ");
        Link link2 = new Link(new Dataset("text", "Click me",
                "javascript",
                "alert(\"Her name is @name, favorite saying @saying\");"));
        link2.render(cr, globalData, body);
        body.append("</p>\n");
        body.append("<p>The following link sends an Ajax request to the " +
                "server, which will log the current page id: ");
        Template.appendHtml(body,
                "<a href=\"#\" onclick =\"@1; return false;\">" +
                "Click me</a>\n", Ajax.invoke(cr, "ajaxPrintId"));
        cr.setPageProperty("ajaxInfo", "phone number is 650-322-1980");
        body.append("<p>The following link issues an Ajax request " +
                "to a nonexistent URL: ");
        Template.appendHtml(body, "<a href=\"#\" onclick =\"@1; return false;\">" +
                "Click me</a>\n",
                Ajax.invoke(cr, "/static/bogus/nonexistent"));
        Link link3 = new Link(new Dataset("text", "Click here",
                "ajaxUrl", "/test/ajaxUpdateTime"));
        body.append("<p>");
        link3.render(cr, globalData, body);
        body.append(" to update the time below.</p>\n" +
                "<p>Latest date/time from Ajax: " +
                "<span id=\"updateMe\">None</span></p>\n");
        Link link4 = new Link(new Dataset("text", "Click here.",
                "ajaxUrl", "ajaxPerf"));
        body.append("Want to measure the round-trip latency for Ajax?  ");
        link4.render(cr, globalData, body);
        body.append("  Or, ");
        Button.render(cr, new Dataset("text", "Click", "ajaxUrl", "ajaxPerf"),
                new Dataset(), body);
        body.append("<span id=\"perf\"></span>\n");
    }

    public void ajaxPrintId(ClientRequest cr) throws IOException {
        logger.info("Main dataset:\n" + cr.getMainDataset().toString());
        logger.info("Page property: " + cr.getPageProperty("ajaxInfo"));
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
