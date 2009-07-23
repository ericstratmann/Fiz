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
import java.net.*;
import javax.servlet.*;

/**
 * This class provides a dummy implementation of the ServletContext
 * interface; it is used for testing.
 */

@SuppressWarnings("deprecation")
public class ServletContextFixture implements ServletContext {
    // Some methods just set the following variable to indicate that they
    // were called.  This is used in situations where the method can't easily
    // synthesize an appropriate return type.
    public String lastMethod = null;

    // Tests can set the values below to prespecify the return values
    // from some methods.
    public String contextRoot = "test/testData";
    public String contextPath = "/context/cpath";

    public Object getAttribute(String name) {
        return "getAttribute: " + name;
    }
    public Enumeration getAttributeNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("name1");
        v.addElement("name2");
        return v.elements();
    }
    public ServletContext getContext(String uriPath) {
        lastMethod = "getContext(\"" + uriPath + "\")";
        return null;
    }
    public String getContextPath() {
        return contextPath;
    }
    public String getInitParameter(String name) {
        return "getInitParameter: " + name;
    }
    public Enumeration getInitParameterNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("ipname1");
        return v.elements();
    }
    public int getMajorVersion() {
        return -1;
    }
    public String getMimeType(String file) {
        if (file.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (file.endsWith(".html")) {
            return "text/html";
        }
        return null;
    }
    public int getMinorVersion() {
        return -2;
    }
    public RequestDispatcher getNamedDispatcher(String name) {
        lastMethod = "getNamedDispatcher(\"" + name + "\")";
        return null;
    }
    public String getRealPath(String path) {
        if (path.length() == 0) {
            return contextRoot;
        }
        if (path.startsWith("/")) {
            return contextRoot + path;
        } else {
            return contextRoot + '/' + path;
        }
    }
    public RequestDispatcher getRequestDispatcher(String path) {
        lastMethod = "getRequestDispatcher(\"" + path + "\")";
        return null;
    }
    public URL getResource(String path) {
        lastMethod = "getResource(\"" + path + "\")";
        return null;
    }
    public InputStream getResourceAsStream(String path) {
        lastMethod = "getResourceAsStream(\"" + path + "\")";
        return null;
    }
    public Set getResourcePaths(String path) {
        lastMethod = "getResourcePaths(\"" + path + "\")";
        return null;
    }
    public String getServerInfo() {
        return "getServerInfo";
    }
    public Servlet getServlet(String name) {
        lastMethod = "getServlet(\"" + name + "\")";
        return null;
    }
    public String getServletContextName() {
        return "getServletContextName";
    }
    public Enumeration getServletNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("servletName1");
        return v.elements();
    }
    public Enumeration getServlets() {
        Vector<String> v = new Vector<String>();
        v.addElement("servlet1");
        return v.elements();
    }
    public void log(Exception exception, String message) {
        lastMethod = "log(exception, \"" + message + "\")";
        return;
    }
    public void log(String message) {
        lastMethod = "log(\"" + message + "\")";
        return;
    }
    public void log(String message, Throwable throwable) {
        lastMethod = "log(\"" + message + "\", throwable)";
        return;
    }
    public void removeAttribute(String name) {
        lastMethod = "removeAttribute(\"" + name + "\")";
        return;
    }
    public void setAttribute(String name, Object object) {
        lastMethod = "setAttribute(\"" + name + "\", \""
                + object.toString() + "\")";
        return;
    }
}
