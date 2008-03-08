/**
 * This Interactor provides a simple mechanism to test the notification
 * mechanism provided by the Message class.  Query values:
 * watch -                Index of chat window to observe.
 * source -               Index of chat window that this page can modify.
 */

package org.fiz;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;

public class Chat extends Interactor {
    protected Message[] messages;
    protected Logger logger = Logger.getLogger("Chat");

    public Chat() {
        messages = new Message[10];
    }

    public void page(Request request)
            throws ServletException, IOException {
        logger.info("chat initiated");
        StringBuilder out = request.getHtml().getBody();
        String watch = request.getParameter("watch");
        String source = request.getParameter("source");
        StringBuilder code = new StringBuilder();
        int id = 1;

        out.append("<script type=\"text/javascript\" src=\"/fiz/AutoScroller.js\">"
                + "</script>\n"
                + "<script type=\"text/javascript\" src=\"/fiz/chat.js\">"
                + "</script>\n"
                + "<script type=\"text/javascript\" src=\"/fiz/debug.js\">"
                + "</script>\n");

        if (watch != null) {
            int index = Integer.parseInt(watch);
            out.append(String.format("<p>Watch below for changes to message "
                    + "%d:</p>\n<textarea name=\"watch%d\" id=\"watch%d\" "
                    + "rows=\"10\" cols=\"64\"></textarea>\n",
                    index, id, id));
            code.append(String.format("var watch%d = new Watch(\"watch%d\", "
                    + "%d);\n", id, id, index));
            id++;
        }
        if (source != null) {
            int index = Integer.parseInt(source);
            out.append(String.format("<p>Use this to modify message %d:</p>\n"
                    + "<textarea name=\"source%d\" id=\"source%d\" "
                    + "rows=\"10\" cols=\"64\"></textarea>\n",
                    index, id, id));
            code.append(String.format("var source%d = new Source(\"source%d\", %d);%n",
                    id, id, index));
        }

        out.append(String.format("<p>Try typing here (nothing will happen):"
                + "</p>\n<textarea name=\"test\" id=\"test\" " +
                "rows=\"10\" cols=\"64\"></textarea>\n"));

         out.append(String.format("<script type=\"text/javascript\">%n"
                + "//<![CDATA[\n%s"
                + "//]]>\n"
                + "</script>\n", code));
    }

    public void watch(Request request)
            throws ServletException, IOException {
        String parameter = request.getParameter("watch");
        int index = (parameter != null) ? Integer.parseInt(parameter) : 0;
        parameter = request.getParameter("generation");
        int generation = (parameter != null) ? Integer.parseInt(parameter) : -1;
        if (messages[index] == null) {
            messages[index] = new Message();
        }
        int newGeneration = messages[index].waitChange(generation);
        request.getWriter().printf("%d:%s", newGeneration,
                messages[index].getValue());
    }

    public void source(Request request)
            throws ServletException, IOException {
        char buffer[] = new char[1024];
        int length = request.getReader().read(buffer, 0, buffer.length);
        if (length < 0) {
            length = 0;
        }
        String parameter = request.getParameter("source");
        int index = (parameter != null) ? Integer.parseInt(parameter) : 0;
        if (messages[index] == null) {
            messages[index] = new Message();
        }
        messages[index].setValue(new String(buffer, 0, length));
        request.getWriter().printf("%s, generation %d",
                messages[index].getValue(), messages[index].getGeneration());
    }
}
