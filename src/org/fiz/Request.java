/**
 * A Request object provides overall control over the process of handling
 * a Fiz request.   It is typically passed to all of the methods that service
 * the request, and provides information that is shared across those methods.
 * Request objects include the HttpServletRequest and HttpServletResponse
 * objects provided by the servlet container and support all of the methods
 * that are valid for those objects.  Requests also provide additional Fiz
 * features such as datasets containing information about query values and
 * configuration information and additional methods for managing the response.  Finally,
 * applications can subclass Request in order to incorporate their own state
 * information (this happens when an Interactor overrides the "getRequest"
 * method to create an application-specific Request subclass).
 */

package org.fiz;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class Request implements HttpServletRequest {

    /**
     * DeprecatedMethodError is thrown when deprecated methods such as
     * isRequestedSessionIdFromUrl are invoked.
     */
    public static class DeprecatedMethodError extends Error {
        /**
         * Constructor for FileNotFoundError.
         * @param methodName       Name of the method that was invoked;
         *                         used to generate a message in the
         *                         exception
         */
        public DeprecatedMethodError(String methodName) {
            super("invoked deprecated method " + methodName);
        }
    }

    // The following fields provide access to the HttpServletRequest
    // and HttpServletResponse objects provided by the servlet container.
    // See documentation for the constructor arguments for more information.
    protected HttpServletRequest servletRequest;
    protected HttpServletResponse servletResponse;

    /**
     * Constructor for Request objects.  Typically invoked by the
     * getRequest method of an Interactor object.
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
    public Request(HttpServletRequest servletRequest, HttpServletResponse
            servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
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
