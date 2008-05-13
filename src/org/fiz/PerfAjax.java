package org.fiz;
import java.io.*;
import javax.servlet.*;

/**
 * This servlet is used to measure the basic round-trip time for AJAX requests.
 * It operates in conjunction with perfAjax.html and testPing.js.
 */

public class PerfAjax extends Interactor {
    public void ping(ClientRequest request)
            throws ServletException, IOException {
        BufferedReader reader = request.getServletRequest().getReader();
        StringBuilder output = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            output.append(line + "\n");
        }
        output.append(System.nanoTime() + "\n");
        request.getServletResponse().setHeader("Content-Length", Integer.toString(output.length()));
        PrintWriter writer = request.getServletResponse().getWriter();
        writer.write(output.toString());
    }
}
