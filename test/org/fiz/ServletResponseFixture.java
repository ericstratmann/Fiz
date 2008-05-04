/**
 * This class provides a dummy implementation of the HttpServletResponse
 * interface; it is used for testing.
 */

package org.fiz;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Vector;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class ServletResponseFixture implements HttpServletResponse{
    // Some methods just set the following variable to indicate that they
    // were called.  This is used in situations where the method can't easily
    // synthesize an appropriate return type (such as getOutputStream).
    public String lastMethod = null;

    public String contentType;
    public StringWriter out = new StringWriter();
    public PrintWriter writer = new PrintWriter(out);
    public int contentLength = 0;

    // The following variables determine the output from certain methods.
    String uri = "uriString";
    String queryString = "?a=b&c=d";

    // HttpServletResponse methods:

    public void addCookie(Cookie cookie) {
        lastMethod = "addCookie(\"" + cookie.getName() + "\")";
    }
    public void addDateHeader(String name, long date) {
        lastMethod = "addDateHeader(\"" + name + "\", " + date + ")";
    }
    public void addHeader(String name, String value) {
        lastMethod = "addHeader(\"" + name + "\", \"" + value + "\")";
    }
    public void addIntHeader(String name, int value) {
        lastMethod = "addIntHeader(\"" + name + "\", " + value + ")";
    }
    public boolean containsHeader(String name) {
        lastMethod = "containsHeader(\"" + name + "\")";
        return true;
    }
    public String encodeRedirectUrl(String url) {
        return "encodeRedirectUrl";
    }
    public String encodeRedirectURL(String url) {
        lastMethod = "encodeRedirectURL(\"" + url + "\")";
        return "encodeRedirectURL";
    }
    public String encodeUrl(String url) {
        lastMethod = "encodeUrl(\"" + url + "\")";
        return "encodeUrl";
    }
    public String encodeURL(String url) {
        lastMethod = "encodeURL(\"" + url + "\")";
        return "encodeURL";
    }
    public void sendError(int status) {
        lastMethod = "sendError(" + status + ")";
    }
    public void sendError(int status, String message) throws IOException {
        lastMethod = "sendError(" + status + ", \"" + message + "\")";
    }
    public void sendRedirect(String location) throws IOException {
        lastMethod = "sendRedirect(\"" + location + "\")";
    }
    public void setDateHeader(String name, long date) {
        lastMethod = "setDateHeader(\"" + name + "\", " + date + ")";
    }
    public void setHeader(String name, String value) {
        lastMethod = "setHeader(\"" + name + "\", \"" + value + "\")";
    }
    public void setIntHeader(String name, int value) {
        lastMethod = "setIntHeader(\"" + name + "\", " + value + ")";
    }
    public void setStatus(int status) {
        lastMethod = "setStatus(" + status + ")";
    }
    public void setStatus(int status, String message) {
        lastMethod = "setStatus(" + status + ", \"" + message + "\")";
    }

    // Methods from ServletResponse.

    public void flushBuffer() {
        lastMethod = "flushBuffer";
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
        lastMethod = "getResponseLocale";
        return null;
    }
    public ServletOutputStream getOutputStream() {
        lastMethod = "getOutputStream";
        return null;
    }
    public PrintWriter getWriter() {
        lastMethod = "getWriter";
        return writer;
    }
    public boolean isCommitted() {
        lastMethod = "isCommitted";
        return true;
    }
    public void reset() {
        lastMethod = "reset";
    }
    public void resetBuffer() {
        lastMethod = "resetBuffer";
    }
    public void setBufferSize(int size) {
        lastMethod = "setBufferSize(" + size + ")";
    }
    public void setCharacterEncoding(String charset) {
        lastMethod = "setCharacterEncoding(\"" + charset + "\")";
    }
    public void setContentLength(int length) {
        contentLength = length;
        lastMethod = "setContentLength(" + length + ")";
    }
    public void setContentType(String type) {
        contentType = type;
        lastMethod = "setContentType(\"" + type + "\")";
    }
    public void setLocale(Locale locale) {
        lastMethod = "setLocale";
    }
}
