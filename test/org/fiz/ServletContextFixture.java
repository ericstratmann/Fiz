package org.fiz;
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
    public String contextRoot = null;
    public String contextPath = "/context/path";

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
        return "getMimeType: " + file;
    }
    public int getMinorVersion() {
        return -2;
    }
    public RequestDispatcher getNamedDispatcher(String name) {
        lastMethod = "getNamedDispatcher(\"" + name + "\")";
        return null;
    }
    public String getRealPath(String path) {
        if (contextRoot == null) {
            return "getRealPath: " + path;
        } else {
            return contextRoot + path;
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
