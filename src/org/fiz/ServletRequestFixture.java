/**
 * This class provides a dummy implementation of the HttpServletRequest
 * interface; it is used for testing.
 */

package org.fiz;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Vector;
import java.io.BufferedReader;

@SuppressWarnings("deprecation")
public class ServletRequestFixture implements HttpServletRequest {
    // Some methods just set the following variable to indicate that they
    // were called.  This is used in situations where the method can't easily
    // synthesize an appropriate return type (such as HttpSession).
    public String lastMethod = null;

    // Methods from HttpServletRequest.
    public String getAuthType() {return "authType";}
    public String getContextPath() {return "contextPath";}
    public Cookie[] getCookies() {return new Cookie[2];}
    public long getDateHeader(String name) {return Integer.parseInt(name);}
    public String getHeader(String name) {return "header: " + name;}
    public java.util.Enumeration getHeaderNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("name1");
        v.addElement("name2");
        return v.elements();
    }
    public java.util.Enumeration getHeaders(String name) {
        Vector<String> v = new Vector<String>();
        v.addElement(name + "_header1");
        v.addElement(name + "_header2");
        return v.elements();
    }
    public int getIntHeader(String name) {return Integer.parseInt(name);}
    public String getMethod() {return "method";}
    public String getPathInfo() {return "pathInfo";}
    public String getPathTranslated() {return "pathTranslated";}
    public String getQueryString() {return "queryString";}
    public String getRemoteUser() {return "remoteUser";}
    public String getRequestedSessionId() {return "requestedSessionId";}
    public String getRequestURI() {return "requestURI";}
    public StringBuffer getRequestURL() {return new StringBuffer("requestURL");}
    public String getServletPath() {return "servletPath";}
    public HttpSession getSession() {
        lastMethod = "getSession";
        return null;
    }
    public HttpSession getSession(boolean create) {
        lastMethod = "getSession(" + create + ")";
        return null;
    }
    public java.security.Principal getUserPrincipal() {
        lastMethod = "getUserPrincipal";
        return null;
    }
    public boolean isRequestedSessionIdFromCookie() {
        lastMethod = "isRequestedSessionIdFromCookie";
        return false;
    }
    public boolean isRequestedSessionIdFromUrl() {
        lastMethod = "isRequestedSessionIdFromUrl";
        return true;
    }
    public boolean isRequestedSessionIdFromURL() {
        lastMethod = "isRequestedSessionIdFromURL";
        return true;
    }
    public boolean isRequestedSessionIdValid() {
        lastMethod = "isRequestedSessionIdValid";
        return true;
    }
    public boolean isUserInRole(String role) {
        lastMethod = "isUserInRole(" + role + ")";
        return true;
    }

    // Methods from ServletRequest.
    public Object getAttribute(String name) {
        lastMethod = "getAttribute(" + name + ")";
        return null;
    }
    public java.util.Enumeration getAttributeNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("name1");
        return v.elements();
    }
    public String getCharacterEncoding() {return "characterEncoding";}
    public int getContentLength() {return 444;}
    public String getContentType() {return "contentType";}
    public ServletInputStream getInputStream() {
        lastMethod = "getInputStream";
        return null;
    }
    public String getLocalAddr() {return "localAddr";}
    public java.util.Locale getLocale() {
        lastMethod = "getLocale";
        return null;
    }
    public java.util.Enumeration getLocales() {
        lastMethod = "getLocales";
        return null;
    }
    public String getLocalName() {return "localName";}
    public int getLocalPort() {return 80;}
    public String getParameter(String name) {return "parameter " + name;}
    public java.util.Map getParameterMap() {
        lastMethod = "getParameterMap";
        return null;
    }
    public java.util.Enumeration getParameterNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("pname1");
        return v.elements();
    }
    public String[] getParameterValues(String name) {
        return new String[] {"first value", "second value"};
    }
    public String getProtocol() {return "protocol";}
    public BufferedReader getReader() {
        lastMethod = "getReader";
        return null;
    }
    public String getRealPath(String path) {return "path " + path;}
    public String getRemoteAddr() {return "remoteAddr";}
    public String getRemoteHost() {return "remoteHost";}
    public int getRemotePort() {return 1023;}
    public RequestDispatcher getRequestDispatcher(String path) {
        lastMethod = "getRequestDispatcher(" + path + ")";
        return null;
    }
    public String getScheme() {return "scheme";}
    public String getServerName() {return "serverName";}
    public int getServerPort() {return 8080;}
    public boolean isSecure() {
        lastMethod = "isSecure";
        return true;
    }
    public void removeAttribute(String name) {
        lastMethod = "removeAttribute(\"" + name + "\")";
    }
    public void setAttribute(String name, Object o) {
        lastMethod = "setAttribute(\"" + name + "\", " + o.toString() + ")";
    }
    public void setCharacterEncoding(String env) {
        lastMethod = "setCharacterEncoding(\"" + env+ "\")";
    }
}
