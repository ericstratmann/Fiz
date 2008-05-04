package org.fiz;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class provides a dummy implementation of HttpServlet for
 * use in tests.
 */
public class ServletFixture extends HttpServlet {
    protected ServletConfig config;

    public ServletFixture(ServletConfig config) {
        this.config = config;
    }
    public ServletConfig getServletConfig() {
        return config;
    }
}
