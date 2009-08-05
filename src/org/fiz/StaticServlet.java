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

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

/**
 * This servlet serves static resources for Fiz (URLs starting with
 * "/static/").  Ideally we'd prefer to let the Tomcat default servlet
 * handle these, but unfortunately it doesn't serve them from the right
 * place (it ignores the "/static/" part of the URL). All this servlet
 * does is look up the file, compute the correct MIME type for it, and
 * return it.
 * TODO: set caching headers in response
 */
public class StaticServlet extends HttpServlet {
    // File name prefix to prepend to each URL to select a file.
    protected String prefix;

    // The following variable is used for log4j-based logging.
    protected static Logger logger = Logger.getLogger("org.fiz.Dispatcher");

    /**
     * This method is invoked by the servlet container when the servlet
     * is first loaded; we use it to perform our own initialization.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // Figure out the prefix to add onto URLs to generate file names.
        String base = config.getInitParameter("base");
        if (base != null) {
            if ((new File(base)).isAbsolute()) {
                // Choice #1: absolute prefix (can point anywhere in the
                // file system).
                prefix = base;
            } else {
                // Choice #2: prefix is relative to the deployment directory
                // for this application.
                prefix = config.getServletContext().getRealPath(base);
            }
        } else {
            // Choice #3: no "base" parameter supplied.  Just use the
            // "static" directory in the deployment area for this application.
            prefix = config.getServletContext().getRealPath("static");
        }
    }

    /**
     * This method is invoked by the servlet container to handle HTTP
     * GET requests that map to this servlet. The incoming URL is used
     * to identify a static file, which is returned as the HTTP response.
     * @param request              Information about the HTTP request.
     * @param response             Used to generate the HTTP response.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        returnFile(prefix + pathInfo, getServletConfig().getServletContext(),
                response);
    }

    /**
     * This method does all the real work of returning a static file as
     * the response to an HTTP request.
     * @param fileName             Path name for the file to be returned.
     * @param context              Used to compute the MIME type for the file.
     * @param response             Used to generate the response.
     */
    protected static void returnFile(String fileName, ServletContext context,
            HttpServletResponse response) {
        InputStream fileSource;
        try {
            fileSource = new FileInputStream(fileName);
        }
        catch (IOException e) {
            return404(response);
            return;
        }
        ServletOutputStream out = null;
        String message = "I/O error retrieving response output stream in " +
                "StaticServlet.returnFile: ";
        try {
            out = response.getOutputStream();

            // Get the correct MIME type for this file.
            String mimeType = null;
            mimeType = context.getMimeType(fileName);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition",
                    "filename=\"" + new File(fileName).getName() + "\"");

            // Write the contents of fileSource into the servlet response
            // stream.
            message = "I/O error sending response in " +
                    "StaticServlet.returnFile: ";
            int length;
            byte[] buf = new byte[4096];
            while ((length = fileSource.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        }
        catch (IOException e) {
            logger.error(message +
                    StringUtil.lcFirst(e.getMessage()), e);
        }
        try {
            // Close the input stream.
            fileSource.close();
        } catch (IOException e) {
            logger.warn("I/O error closing file input stream in " +
                    "StaticServlet.returnFile: " +
                    StringUtil.lcFirst(e.getMessage()), e);
        }
        if (out != null) {
            try {
                // Flush the output stream so we find out about any errors
                // right now.
                out.flush();
            } catch (IOException e) {
                logger.error("I/O error flushing output stream in " +
                        "StaticServlet.returnFile: " +
                        StringUtil.lcFirst(e.getMessage()), e);
            }
        }
    }

    /**
     * Return a "file not found" response to the HTTP request.
     * @param response             Information about the HTTP response,
     *                             provided by the servlet container.
     */
    protected static void return404(HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        catch (IOException e) {
            logger.error("I/O error sending 404 error in " +
                    "StaticServlet.return404: " +
                    StringUtil.lcFirst(e.getMessage()), e);
        }
    }
}
