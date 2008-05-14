package org.fiz;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class provides a dummy implementation of the HttpServletRequest
 * interface; it is used for testing.
 */

@SuppressWarnings("deprecation")
public class ServletRequestFixture implements HttpServletRequest {
    // Some methods just set the following variable to indicate that they
    // were called.  This is used in situations where the method can't easily
    // synthesize an appropriate return type (such as HttpSession).
    public String lastMethod = null;

    // Used to simulate parameter data via setParameters().
    protected Hashtable<String,String> parameterMap = null;

    // The following variables provide return values for some of the methods;
    // tests can modify these variables to test different scenarios.
    public String servletPath = "/servlet/spath";
    public String pathInfo = "/pathInfo";
    public String uri = "/x/y/z";
    public String queryString = "a=b&c=d";
    public String contentType = "contentType";
    public BufferedReader inputReader = null;

    public ServletRequestFixture() {
        parameterMap = new Hashtable<String,String>();
        parameterMap.put("p1", "param_value1");
        parameterMap.put("p2", "param_value2");
    }

    public ServletRequestFixture(String path) {
        super();
        pathInfo = path;
    }

    /**
     * Invoke this method to set up fake parameter data.
     * @param keysAndValues        Alternating names and values for
     *                             parameters.
     */
    public void setParameters(String... keysAndValues) {
        parameterMap.clear();
        int last = keysAndValues.length - 2;
        for (int i = 0; i <= last; i += 2) {
            parameterMap.put(keysAndValues[i], keysAndValues[i+1]);
        }
    }

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
    public String getPathInfo() {return pathInfo;}
    public String getPathTranslated() {return "pathTranslated";}
    public String getQueryString() {return queryString;}
    public String getRemoteUser() {return "remoteUser";}
    public String getRequestedSessionId() {return "requestedSessionId";}
    public String getRequestURI() {return uri;}
    public StringBuffer getRequestURL() {return new StringBuffer("requestURL");}
    public String getServletPath() {return servletPath;}
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
    public String getContentType() {return contentType;}
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
    public String getParameter(String name) {
        if (parameterMap == null) {
            return "parameter " + name;
        }
        return parameterMap.get(name);
    }
    public java.util.Map getParameterMap() {
        lastMethod = "getParameterMap";
        return parameterMap;
    }
    public java.util.Enumeration getParameterNames() {
        return parameterMap.keys();
    }
    public String[] getParameterValues(String name) {
        String value = parameterMap.get(name);
        if (value == null) {
            return null;
        }
        String[] values = new String[2];
        values[0] = value;
        values[1] = value + "_next";
        return values;
    }
    public String getProtocol() {return "protocol";}
    public BufferedReader getReader() {
        lastMethod = "getReader";
        return inputReader;
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
