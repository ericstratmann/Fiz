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

package org.fiz.test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class provides a dummy implementation of the HttpServletResponse
 * interface; it is used for testing.
 */
public class ServletResponseFixture implements HttpServletResponse{
    // Some methods just append to the following variable to indicate that they
    // were called.  This is used in situations where the method can't easily
    // synthesize an appropriate return type (such as getOutputStream).
    public StringBuilder log = new StringBuilder();

    // The underlying stream for this ServletResponse object.
    protected ByteArrayOutputStream out = new ByteArrayOutputStream();

    public ServletOutputStreamFixture stream =
            new ServletOutputStreamFixture(out);
    public PrintWriter writer = new PrintWriter(out);

    public String contentType = null;

    public String headers = null;

    public int contentLength = 0;

    // Only one of getWriter() or getOutputStream() may be called during a
    // single request, else an IllegalStateException is thrown.
    public boolean getWriterInvoked = false;
    public boolean getOutputStreamInvoked = false;

    // The following variables determine the output from certain methods.
    public String uri = "uriString";
    public String queryString = "?a=b&c=d";
    public boolean getOutputStreamException = false;
    public boolean getWriterException = false;
    public boolean sendRedirectException = false;
    public boolean sendErrorException = false;

    // HttpServletResponse methods:

    public void addCookie(Cookie cookie) {
        appendToLog("addCookie(\"" + cookie.getName() + "\")");
    }
    public void addDateHeader(String name, long date) {
        appendToLog("addDateHeader(\"" + name + "\", " + date + ")");
    }
    public void addHeader(String name, String value) {
        appendToLog("addHeader(\"" + name + "\", \"" + value + "\")");
    }
    public void addIntHeader(String name, int value) {
        appendToLog("addIntHeader(\"" + name + "\", " + value + ")");
    }
    public boolean containsHeader(String name) {
        appendToLog("containsHeader(\"" + name + "\")");
        return true;
    }
    public String encodeRedirectUrl(String url) {
        return "encodeRedirectUrl";
    }
    public String encodeRedirectURL(String url) {
        appendToLog("encodeRedirectURL(\"" + url + "\")");
        return "encodeRedirectURL";
    }
    public String encodeUrl(String url) {
        appendToLog("encodeUrl(\"" + url + "\")");
        return "encodeUrl";
    }
    public String encodeURL(String url) {
        appendToLog("encodeURL(\"" + url + "\")");
        return "encodeURL";
    }
    public void sendError(int status) throws IOException {
        if (sendErrorException) {
            throw new IOException("exception in sendError");
        }
        appendToLog("sendError(" + status + ")");
    }
    public void sendError(int status, String message) throws IOException {
        if (sendErrorException) {
            throw new IOException("exception in sendError");
        }
        appendToLog("sendError(" + status + ", \"" + message + "\")");
    }
    public void sendRedirect(String location) throws IOException {
        appendToLog("sendRedirect(\"" + location + "\")");
        if (sendRedirectException) {
            throw new IOException("fake IOException for testing");
        }
    }
    public void setDateHeader(String name, long date) {
        appendToLog("setDateHeader(\"" + name + "\", " + date + ")");
    }
    public void setHeader(String name, String value) {
        if (headers != null) {
            headers = headers + " " + value;
        } else {
            headers = value;
        }
        appendToLog("setHeader(\"" + name + "\", \"" + value + "\")");
    }
    public void setIntHeader(String name, int value) {
        appendToLog("setIntHeader(\"" + name + "\", " + value + ")");
    }
    public void setStatus(int status) {
        appendToLog("setStatus(" + status + ")");
    }
    public void setStatus(int status, String message) {
        appendToLog("setStatus(" + status + ", \"" + message + "\")");
    }

    // Methods from ServletResponse.

    public void flushBuffer() {
        appendToLog("flushBuffer");
    }
    public int getBufferSize() {
        return 4096;
    }
    public String getCharacterEncoding() {
        return "characterEncoding";
    }
    public String getContentType() {
        return "contentType";
    }
    public Locale getLocale() {
        appendToLog("getResponseLocale");
        return null;
    }
    public ServletOutputStream getOutputStream() throws IOException {
        appendToLog("getOutputStream");
        if (getOutputStreamInvoked) {
            throw new IllegalStateException("getWriter() has already " +
                    "been invoked");
        }
        getOutputStreamInvoked = true;
        if (getOutputStreamException) {
            throw new IOException("getOutputStream failed");
        }
        return stream;
    }
    public PrintWriter getWriter() throws IOException {
        appendToLog("getWriter");
        if (getOutputStreamInvoked) {
            throw new IllegalStateException("getOutputStream() has already " +
                    "been invoked");
        }
        getWriterInvoked = true;
        if (getWriterException) {
            throw new IOException("getWriter failed");
        }
        return writer;
    }
    public boolean isCommitted() {
        appendToLog("isCommitted");
        return true;
    }
    public void reset() {
        appendToLog("reset");
    }
    public void resetBuffer() {
        appendToLog("resetBuffer");
    }
    public void setBufferSize(int size) {
        appendToLog("setBufferSize(" + size + ")");
    }
    public void setCharacterEncoding(String charset) {
        appendToLog("setCharacterEncoding(\"" + charset + "\")");
    }
    public void setContentLength(int length) {
        contentLength = length;
        appendToLog("setContentLength(" + length + ")");
    }
    public void setContentType(String type) {
        contentType = type;
        appendToLog("setContentType(\"" + type + "\")");
    }
    public void setLocale(Locale locale) {
        appendToLog("setLocale");
    }

    // Methods used to get access to the contents of the underlying
    // output stream.

    public String toString() {
        writer.flush();
        return new String(out.toByteArray());
    }

    public byte[] getBytes() {
        return out.toByteArray();
    }

    public String getLog() {
        return log.toString();
    }

    // Internal methods:

    protected void appendToLog(String message) {
        if (log.length() != 0) {
            log.append("; ");
        }
        log.append(message);
    }
}
