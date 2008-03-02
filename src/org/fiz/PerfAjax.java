/**
 * This servlet is used to measure the basic round-trip time for AJAX requests.
 * It operates in conjunction with perfAjax.html and testPing.js.
 * User: John Ousterhout
 * Date: Dec 12, 2007
 * Time: 9:30:14 AM
 */
package org.fiz;
import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;

public class PerfAjax extends Interactor {
    public void ping(HttpServletRequest request,
                        HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        PrintWriter writer = response.getWriter();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            writer.println(line);
        }
        writer.println(System.nanoTime());
    }
}
