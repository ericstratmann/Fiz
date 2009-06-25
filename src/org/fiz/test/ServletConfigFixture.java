package org.fiz.test;
import javax.servlet.*;
import java.util.*;

/**
 * This class provides a dummy implementation of the ServletConfig
 * interface; it is used for testing.
 */

public class ServletConfigFixture implements ServletConfig {
    // Some methods just set the following variable to indicate that they
    // were called.  This is used in situations where the method can't easily
    // synthesize an appropriate return type.
    public String lastMethod = null;

    public ServletContext servletContext;

    // Returned this as the result of the next call to getInitParameter.
    public String parameterValue = null;

    public ServletConfigFixture(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String getInitParameter(String name) {
        return parameterValue;
    }
    public Enumeration getInitParameterNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("pname1");
        return v.elements();
    }
    public ServletContext getServletContext() {
        lastMethod = "getServletContext";
        return servletContext;
    }
    public String getServletName() {
        return "getServletName";
    }
}
