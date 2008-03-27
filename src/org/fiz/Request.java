package org.fiz;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Locale;
import java.util.Enumeration;

/**
 * A Request object provides access to all of the interesting state needed
 * to process a Fiz request.  It is typically passed to all of the major
 * methods that service the request. Request objects include the
 * HttpServletRequest and HttpServletResponse objects provided by the
 * servlet container and support all of the methods that are valid for
 * those objects.  Requests also include additional Fiz objects such as
 * a dataset containing the request's query values and an Html object
 * for building the response.
 * <p>
 * It may be useful for an application to extend Requests with additional
 * data;  in this case the application should subclass Request to add the
 * additional fields and override the {@code getRequest} method in the
 * application's Interactors to supply the Request subclass.
 */

@SuppressWarnings("deprecation")
public class Request implements HttpServletRequest {
    /**
     * DeprecatedMethodError is thrown when deprecated methods such as
     * isRequestedSessionIdFromUrl are invoked.
     */
    public static class DeprecatedMethodError extends Error {
        /**
         * Constructs a DeprecatedMethodError with a message describing
         * the deprecated method that was invoked.
         * @param methodName       Name of the method that was invoked;
         *                         used to generate a message in the
         *                         exception.
         */
        public DeprecatedMethodError(String methodName) {
            super("invoked deprecated method " + methodName);
        }
    }

    // The servlet under which this Request is being processed.
    protected HttpServlet servlet;

    // The HttpServletRequest and HttpServletResponse objects provided by
    // the servlet container.  See documentation for the constructor arguments
    // for more information.
    protected HttpServletRequest servletRequest;
    protected HttpServletResponse servletResponse;

    // Used to accumulate information that will eventually be returned as
    // HTML output.
    protected Html html = null;

    // Top-level dataset for this request.  See getRequest documentation for
    // details.
    protected Dataset dataset = null;

    /**
     * Constructs a Request object.  Typically invoked by the
     * getRequest method of an Interactor object.
     * @param servlet              Servlet under which this request is running.
     * @param servletRequest       The HttpServletRequest provided by the
     *                             container.  May be null, in which case
     *                             the corresponding methods will become
     *                             no-ops (e.g. if servletRequest is null
     *                             then getAuthType will return null and
     *                             getCookies will return an empty array).
     *                             Null is used primarily for testing.
     * @param servletResponse      The HttpServletResponse provided by the
     *                             container; if null (primarily for testing),
     *                             the corresponding methods become no-ops.
     */
    public Request(HttpServlet servlet, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.servlet = servlet;
    }

    /**
     * Returns the HttpServletRequest object associated with this Request.
     * @return                     HttpServletRequest object managed by the
     *                             underlying servlet container.
     */
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    /**
     * Returns the HttpServletResponse object associated with this Request.
     * @return                     HttpServletResponse object managed by the
     *                             underlying servlet container.
     */
    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    /**
     * Returns information about the servlet under which the Request is
     * being processed.
     * @return                     HttpServlet for this request.
     */
    public HttpServlet getServlet() {
        return servlet;
    }

    /**
     * @return                     The ServletContext object associated with
     *                             this request's servlet.
     */
    public ServletContext getServletContext() {
        ServletConfig config = servlet.getServletConfig();
        return config.getServletContext();
    }

    /**
     * Returns an object used to generate and buffer the request's HTML
     * output.
     * @return                     Html object for this request.
     */
    public Html getHtml() {
        if (html == null) {
            html = new Html(servletRequest.getContextPath());
        }
        return html;
    }

    /**
     * Returns the main dataset for this request.  Initially the dataset
     * contains query values provided in the URL, but requests may choose to
     * additional data to the dataset in cases where the data needs to be
     * used globally across the request.
     * @return                     Global dataset for this request.
     */
    public Dataset getDataset() {
        if (dataset != null) {
            return dataset;
        }

        // This is the first time someone has asked for the dataset, so we
        // need to build it.  Its initial contents consist of the query
        // data provided to the request, if any.
        dataset = new Dataset();
        Enumeration e = servletRequest.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            dataset.set(name, servletRequest.getParameter(name));
        }
        return dataset;
    }

    // The following methods simply reflect HttpServletRequest methods
    // through to servletRequest.  If servletRequest is null then they
    // act as no-ops (do-nothing, return nothing).  Deprecated methods
    // generate DeprecatedMethodError exceptions.
    // TODO: Javadoc for HttpServletRequest and HttpServletResponse methods.
    // TODO: Consider breaking away from HttpServletRequest interface?

    public String getAuthType() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getAuthType();
    }
    public String getContextPath() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getContextPath();
    }
    public Cookie[] getCookies() {
        if (servletRequest == null) {
            return new Cookie[0];
        }
        return servletRequest.getCookies();
    }
    public long getDateHeader(String name) {
        if (servletRequest == null) {
            return 0;
        }
        return servletRequest.getDateHeader(name);
    }
    public String getHeader(String name) {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getHeader(name);
    }
    public java.util.Enumeration getHeaderNames() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getHeaderNames();
    }
    public java.util.Enumeration getHeaders(String name) {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getHeaders(name);
    }
    public int getIntHeader(String name) {
        if (servletRequest == null) {
            return 0;
        }
        return servletRequest.getIntHeader(name);
    }
    public String getMethod() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getMethod();
    }
    public String getPathInfo() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getPathInfo();
    }
    public String getPathTranslated() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getPathTranslated();
    }
    public String getQueryString() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getQueryString();
    }
    public String getRemoteUser() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getRemoteUser();
    }
    public String getRequestedSessionId() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getRequestedSessionId();
    }
    public String getRequestURI() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getRequestURI();
    }
    public StringBuffer getRequestURL() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getRequestURL();
    }
    public String getServletPath() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getServletPath();
    }
    public HttpSession getSession() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getSession();
    }
    public HttpSession getSession(boolean create) {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getSession(create);
    }
    public java.security.Principal getUserPrincipal() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getUserPrincipal();
    }
    public boolean isRequestedSessionIdFromCookie() {
        if (servletRequest == null) {
            return true;
        }
        return servletRequest.isRequestedSessionIdFromCookie();
    }
    public boolean isRequestedSessionIdFromUrl() {
        throw new DeprecatedMethodError("isRequestedSessionIdFromUrl");
    }
    public boolean isRequestedSessionIdFromURL() {
        if (servletRequest == null) {
            return false;
        }
        return servletRequest.isRequestedSessionIdFromURL();
    }
    public boolean isRequestedSessionIdValid() {
        if (servletRequest == null) {
            return false;
        }
        return servletRequest.isRequestedSessionIdValid();
    }
    public boolean isUserInRole(String role) {
        if (servletRequest == null) {
            return false;
        }
        return servletRequest.isUserInRole(role);
    }

    // Methods from ServletRequest.
    public Object getAttribute(String name) {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getAttribute(name);
    }
    public java.util.Enumeration getAttributeNames() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getAttributeNames();
    }
    public String getCharacterEncoding() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getCharacterEncoding();
    }
    public int getContentLength() {
        if (servletRequest == null) {
            return 0;
        }
        return servletRequest.getContentLength();
    }
    public String getContentType() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getContentType();
    }
    public ServletInputStream getInputStream() throws java.io.IOException {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getInputStream();
    }
    public String getLocalAddr() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getLocalAddr();
    }
    public java.util.Locale getLocale() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getLocale();
    }
    public java.util.Enumeration getLocales() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getLocales();
    }
    public String getLocalName() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getLocalName();
    }
    public int getLocalPort() {
        if (servletRequest == null) {
            return 0;
        }
        return servletRequest.getLocalPort();
    }
    public String getParameter(String name) {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getParameter(name);
    }
    public java.util.Map getParameterMap() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getParameterMap();
    }
    public java.util.Enumeration getParameterNames() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getParameterNames();
    }
    public String[] getParameterValues(String name) {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getParameterValues(name);
    }
    public String getProtocol() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getProtocol();
    }
    public BufferedReader getReader() throws java.io.IOException {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getReader();
    }
    public String getRealPath(String path) {
        throw new DeprecatedMethodError("getRealPath");
    }
    public String getRemoteAddr() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getRemoteAddr();
    }
    public String getRemoteHost() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getRemoteHost();
    }
    public int getRemotePort() {
        if (servletRequest == null) {
            return 0;
        }
        return servletRequest.getRemotePort();
    }
    public RequestDispatcher getRequestDispatcher(String path) {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getRequestDispatcher(path);
    }
    public String getScheme() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getScheme();
    }
    public String getServerName() {
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getServerName();
    }
    public int getServerPort() {
        if (servletRequest == null) {
            return 0;
        }
        return servletRequest.getServerPort();
    }
    public boolean isSecure() {
        if (servletRequest == null) {
            return false;
        }
        return servletRequest.isSecure();
    }
    public void removeAttribute(String name) {
        if (servletRequest == null) {
            return;
        }
        servletRequest.removeAttribute(name);
    }
    public void setAttribute(String name, Object o) {
        if (servletRequest == null) {
            return;
        }
        servletRequest.setAttribute(name, o);
    }
    public void setCharacterEncoding(String env)
            throws java.io.UnsupportedEncodingException{
        if (servletRequest == null) {
            return;
        }
        servletRequest.setCharacterEncoding(env);
    }

    // The following methods simply reflect HttpServletResponse methods
    // through to servletResponse.  If servletResponse is null then they
    // act as no-ops (do-nothing, return nothing).  Deprecated methods
    // generate DeprecatedMethodError exceptions.

    public void addCookie(Cookie cookie) {
        if (servletResponse != null) {
            servletResponse.addCookie(cookie);
        }
    }
    public void addDateHeader(String name, long date) {
        if (servletResponse != null) {
            servletResponse.addDateHeader(name, date);
        }
    }
    public void addHeader(String name, String value) {
        if (servletResponse != null) {
            servletResponse.addHeader(name, value);
        }
    }
    public void addIntHeader(String name, int value) {
        if (servletResponse != null) {
            servletResponse.addIntHeader(name, value);
        }
    }
    public boolean containsHeader(String name) {
        if (servletResponse != null) {
            return servletResponse.containsHeader(name);
        }
        return false;
    }
    public String encodeRedirectUrl(String url) {
        throw new DeprecatedMethodError("encodeRedirectUrl");
    }
    public String encodeRedirectURL(String url) {
        if (servletResponse != null) {
            return servletResponse.encodeRedirectURL(url);
        }
        return null;
    }
    public String encodeUrl(String url) {
        throw new DeprecatedMethodError("encodeUrl");
    }
    public String encodeURL(String url) {
        if (servletResponse != null) {
            return servletResponse.encodeURL(url);
        }
        return null;
    }
    public void sendError(int status) throws IOException {
        if (servletResponse != null) {
            servletResponse.sendError(status);
        }
    }
    public void sendError(int status, String message) throws IOException {
        if (servletResponse != null) {
            servletResponse.sendError(status, message);
        }
    }
    public void sendRedirect(String location) throws IOException {
        if (servletResponse != null) {
            servletResponse.sendRedirect(location);
        }
    }
    public void setDateHeader(String name, long date) {
        if (servletResponse != null) {
            servletResponse.setDateHeader(name, date);
        }
    }
    public void setHeader(String name, String value) {
        if (servletResponse != null) {
            servletResponse.setHeader(name, value);
        }
    }
    public void setIntHeader(String name, int value) {
        if (servletResponse != null) {
            servletResponse.setIntHeader(name, value);
        }
    }
    public void setStatus(int status) {
        if (servletResponse != null) {
            servletResponse.setStatus(status);
        }
    }
    public void setStatus(int status, String message) {
        throw new DeprecatedMethodError("setStatus(status, message)");
    }

    // Methods from ServletResponse.

    public void flushBuffer() throws IOException {
        if (servletResponse != null) {
            servletResponse.flushBuffer();
        }
    }
    public int getBufferSize() {
        if (servletResponse != null) {
            return servletResponse.getBufferSize();
        }
        return 0;
    }
    public String getResponseCharacterEncoding() {
        if (servletResponse != null) {
            return servletResponse.getCharacterEncoding();
        }
        return null;
    }
    public String getResponseContentType() {
        if (servletResponse != null) {
            return servletResponse.getContentType();
        }
        return null;
    }
    public Locale getResponseLocale() {
        if (servletResponse != null) {
            return servletResponse.getLocale();
        }
        return null;
    }
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletResponse != null) {
            return servletResponse.getOutputStream();
        }
        return null;
    }
    public PrintWriter getWriter()  throws IOException {
        if (servletResponse != null) {
            return servletResponse.getWriter();
        }
        return null;
    }
    public boolean isCommitted() {
        if (servletResponse != null) {
            return servletResponse.isCommitted();
        }
        return false;
    }
    public void reset() {
        if (servletResponse != null) {
            servletResponse.reset();
        }
    }
    public void resetBuffer() {
        if (servletResponse != null) {
            servletResponse.resetBuffer();
        }
    }
    public void setBufferSize(int size) {
        if (servletResponse != null) {
            servletResponse.setBufferSize(size);
        }
    }
    public void setResponseCharacterEncoding(String charset) {
        if (servletResponse != null) {
            servletResponse.setCharacterEncoding(charset);
        }
    }
    public void setContentLength(int length) {
        if (servletResponse != null) {
            servletResponse.setContentLength(length);
        }
    }
    public void setContentType(String type) {
        if (servletResponse != null) {
            servletResponse.setContentType(type);
        }
    }
    public void setLocale(Locale locale) {
        if (servletResponse != null) {
            servletResponse.setLocale(locale);
        }
    }
}
