/**
 * This servlet provides a simple mechanism to test the notification mechanism
 * provided by the Message class.  Query values:
 * watch -                Index of chat window to observe.
 * source -               Index of chat window that this page can modify.
 * User: John Ousterhout
 * Date: Dec 17, 2007
 * Time: 11:50:45 AM
 */

package org.fiz;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
        PrintWriter out = request.getWriter();
        String watch = request.getParameter("watch");
        String source = request.getParameter("source");
        StringBuffer code = new StringBuffer();
        int id = 1;

        out.printf("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"%n"
                + "    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">%n"
                + "<head>%n"
                + "  <title>Simple Tomcat Test</title>%n"
                + "</head>%n"
                + "<body>%n"
                + "<script type=\"text/javascript\" src=\"/AutoScroller.js\"></script>%n"
                + "<script type=\"text/javascript\" src=\"/chat.js\"></script>%n"
                + "<script type=\"text/javascript\" src=\"/debug.js\"></script>%n");

        if (watch != null) {
            int index = Integer.parseInt(watch);
            out.printf("<p>Watch below for changes to message %d:</p>%n" +
                    "<textarea name=\"watch%d\" id=\"watch%d\" " +
                    "rows=\"10\" cols=\"64\"></textarea>%n",
                    index, id, id);
            code.append(String.format("var watch%d = new Watch(\"watch%d\", %d);%n",
                    id, id, index));
            id++;
        }
        if (source != null) {
            int index = Integer.parseInt(source);
            out.printf("<p>Use this to modify message %d:</p>%n" +
                    "<textarea name=\"source%d\" id=\"source%d\" " +
                    "rows=\"10\" cols=\"64\"></textarea>%n",
                    index, id, id);
            code.append(String.format("var source%d = new Source(\"source%d\", %d);%n",
                    id, id, index));
        }

        out.printf("<p>Try typing here (nothing will happen):</p>%n" +
                "<textarea name=\"test\" id=\"test\" " +
                "rows=\"10\" cols=\"64\"></textarea>%n");

        out.printf("<script type=\"text/javascript\">%n"
                + "//<![CDATA[%n%s"
                + "//]]>%n"
                + "</script>%n"
                + "</body>%n"
                + "</html>%n", code);
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
