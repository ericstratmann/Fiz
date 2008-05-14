package org.fiz;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

/**
 * A ClientRequest object is the master data structure providing access
 * to everything needed to serve an incoming HTTP request.  Each incoming
 * request is allocated its own ClientRequest object, which stays with
 * the request until it has completed.  A ClientRequest provides access
 * to the following things:
 *   * Information about the request itself, such as the HttpServletRequest
 *     object and a Dataset called the "main dataset" that holds the request's
 *     query values and other global information.
 *   * Information about the response generated for the request, including
 *     the HttpServletResponse object and a Fiz Html object.
 *   * Global information about the application, including the HttpServlet
 *     object for the servlet container and additional Fiz information such
 *     as configuration datasets, the CSS cache, and data managers.
 * Most of the major methods for servicing a request take a ClientRequest
 * object as their first argument; the ClientRequest is normally referred to
 * with a variable named {@code cr}.
 * <p>
 * Individual applications may have additional global data that needs to
 * be accessible throughout the application.  They can achieve this by
 * creating a subclass of ClientRequest that holds the additional data,
 * then override the {@code getRequest} method in the application's
 * Interactor(s) to supply an instance of the ClientRequest subclass.
 * Alternatively, the application can store its data as attributes in the
 * ServletContext ({@code cr.getServletContext.getAttribute()}).
 */

@SuppressWarnings("deprecation")
public class ClientRequest {
    // The servlet under which this ClientRequest is being processed.
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
    protected Dataset mainDataset = null;

    // Hash table that maps from the {@code name} argument passed to
    // {@code registerDataRequest} to the DataRequest that was returned
    // for that name.  Used to detect duplicate requests and share a single
    // DataRequest between them.
    protected HashMap<String,DataRequest> namedRequests =
            new HashMap<String,DataRequest>();

    // Keeps track of all of the DataRequests that were passed explicitly to
    // {@code registerDataRequest} (i.e., all of the registered DataRequests
    // that aren't in the {@code namedRequests} table).
    protected ArrayList<DataRequest> unnamedRequests =
            new ArrayList<DataRequest>();

    // The following variable contains the return value from the last call to
    // getUrlPrefix, or null if that method hasn't yet been called.
    protected String urlPrefix = null;

    // The following variable indicates whether this request is an Ajax
    // request (false means it is a normal HTML request).
    protected boolean ajaxRequest = false;

    // The following variable indicates whether any Ajax actions have been
    // generated in response to this request.
    protected boolean anyAjaxActions = false;

    // The following variable is used for log4j-based logging.
    protected static Logger logger = Logger.getLogger("org.fiz.ClientRequest");

    /**
     * Constructs a ClientRequest object.  Typically invoked by the
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
    public ClientRequest(HttpServlet servlet, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.servlet = servlet;
    }

    /**
     * Output an Ajax {@code error} action in response to the current Ajax
     * request.  This action will cause the browser to report the error
     * described by {@code properties}.
     * @param properties           Dataset containing information about
     *                             the error.  Must have at least a
     *                             {@code message} element.
     */
    public void ajaxErrorAction(Dataset properties) {
        PrintWriter writer = ajaxActionHeader("error");
        writer.append(", properties: ");
        properties.toJavascript(writer);
        writer.append("}");
    }

    /**
     * Output an Ajax {@code eval} action in response to the current Ajax
     * request.  This action will cause the browser to execute the
     * Javascript code specified by {@code javascript}.
     * @param javascript           Javascript code for the browser to
     *                             execute when the Ajax response is received.
     */
    public void ajaxEvalAction(CharSequence javascript) {
        PrintWriter writer = ajaxActionHeader("eval");
        writer.append(", javascript: \"");
        Html.escapeStringChars(javascript, writer);
        writer.append("\"}");
    }

    /**
     * Output an Ajax {@code eval} action in response to the current Ajax
     * request.  This action will cause the browser to execute the
     * Javascript code specified by {@code template} and {@code data}.
     * @param template             Template for the Javascript code, which will
     *                             be expanded using {@code data}.
     * @param data                 Dataset to use for expanding
     *                             {@code template}.
     */
    public void ajaxEvalAction(String template, Dataset data) {
        StringBuilder expanded = new StringBuilder();
        Template.expand(template, data, expanded,
                Template.SpecialChars.JAVASCRIPT);
        ajaxEvalAction(expanded);
    }

    /**
     * Output an Ajax {@code redirect} action in response to the current Ajax
     * request.  This action will cause the browser to display the page
     * specified by {@code url}.
     * @param url                  New URL for the browser to display.
     */
    public void ajaxRedirectAction(String url) {
        PrintWriter writer = ajaxActionHeader("redirect");
        writer.append(", url: \"");

        // No need to worry about quoting the characters in the URL: if
        // it is a proper URL, there are no characters in it that have
        // special meaning in Javascript strings.
        writer.append(url);
        writer.append("\"}");
    }

    /**
     * Output an Ajax {@code update} action in the response to the current
     * Ajax request.  This action will cause the browser to replace the
     * innerHTML of the element given by {@code id} with new HTML.
     * @param id                   DOM identifier for the element whose
     *                             contents are to be replaced.
     * @param html                 New HTML contents for element {@code id}.
     */
    public void ajaxUpdateAction(CharSequence id, CharSequence html) {
        PrintWriter writer = ajaxActionHeader("update");
        writer.append(", id: \"");
        writer.append(id);
        writer.append("\", html: \"");
        Html.escapeStringChars(html, writer);
        writer.append("\"}");
    }

    /**
     * This method is invoked by the Dispatcher at the end of a request
     * to complete the transmission of the response back to the client.
     */
    public void finish() {
        PrintWriter out;
        try {
            out = servletResponse.getWriter();
        }
        catch (IOException e) {
            logger.error("I/O error retrieving response writer in " +
                    "ClientRequest.finish: " + e.getMessage());
            return;
        }
        if (ajaxRequest) {
            if (anyAjaxActions) {
                // Close off the Javascript code for the actions.
                out.append("];");
            }
        } else {
            // This is an HTML request.  Transmit the accumulated
            // HTML, if any.
            servletResponse.setContentType("text/html");
            getHtml().print(out);
        }
        if (out.checkError()) {
            logger.error("I/O error sending response in " +
                    "ClientRequest.finish");
        }
    }

    /**
     * Returns an object used to generate and buffer the request's HTML
     * output.
     * @return                     Html object for this request.
     */
    public Html getHtml() {
        if (html == null) {
            html = new Html(this);
        }
        return html;
    }

    /**
     * Returns the main dataset for this request.  Initially the dataset
     * contains all of the data that arrived with the request, including
     * query values from the URL and POST data (either URL-encoded or in
     * the special Fiz Ajax format excepted by {@code Ajax.readInputData}.
     * If the same name is used both in a URL query value and in POST data,
     * the query value is used and the POST data is ignored.  In addition
     * to the initial values, requests may add values to the main dataset
     * in cases where the data needs to be used globally across the request.
     * This dataset will be available in (almost?) all template expansions
     * used while processing the request.
     * @return                     Global dataset for this request.
     */
    public Dataset getMainDataset() {
        if (mainDataset != null) {
            return mainDataset;
        }

        // This is the first time someone has asked for the dataset, so we
        // need to build it.  First, if this is an Ajax request read in the
        // POST data for the request, extract the Ajax data from it, and
        // add that to the main dataset.
        mainDataset = new Dataset();
        if (ajaxRequest) {
            String contentType = (servletRequest.getContentType());
            if ((contentType != null) && (contentType.equals("text/plain"))) {
                // First, read the data into a string.
                StringBuilder postData = new StringBuilder();
                try {
                    BufferedReader reader = servletRequest.getReader();
                    while (true) {
                        int c = reader.read();
                        if (c == -1) {
                            break;
                        }
                        postData.append((char) c);
                    }
                }
                catch (IOException e) {
                    throw new IOError("error reading Ajax data: " +
                            e.getMessage());
                }
                Ajax.readInputData(postData, mainDataset);
            }
        }

        // Next, load any query data provided to the request.
        Enumeration params = servletRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String) params.nextElement();
            mainDataset.set(name, servletRequest.getParameter(name));
        }
        return mainDataset;
    }

    /**
     * Generate a string containing the names of all of the DataRequests
     * registered so far for this ClientRequests.  This method is used
     * primarily for testing
     * @return                     A string containing names of all of the
     *                             named requests registered so far, in
     *                             alphabetical order.
     */
    public String getRequestNames() {
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(namedRequests.keySet());
        Collections.sort(names);
        return StringUtil.join(names, ", ");
    }

    /**
     * Returns information about the servlet under which the ClientRequest is
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
     * Returns the HttpServletRequest object associated with this
     * ClientRequest.
     * @return                     HttpServletRequest object managed by the
     *                             underlying servlet container.
     */
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    /**
     * Returns the HttpServletResponse object associated with this
     * ClientRequest.
     * @return                     HttpServletResponse object managed by the
     *                             underlying servlet container.
     */
    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    /**
     * This method returns a string representing the path prefix that
     * is shared by all URL's in this application that refer to
     * Fiz Interactors.  In other words, if the full URL to invoke
     * method {@code method} in class {@code class} is
     * {@code http://www.company.com/a/b/c/class/method}, this method
     * will return {@code /a/b/c}.  The return value does not end with
     * a slash.  In servlet terms, the return value is the concatenation
     * of the servlet path and the context path.
     * @return                     See above.
     */
    public String getUrlPrefix() {
        if (urlPrefix == null) {
            urlPrefix = getServletContext().getContextPath () +
                    getServletRequest().getServletPath();
        }
        return urlPrefix;
    }

    /**
     * Returns true if this client request is an Ajax request, false
     * if it is a traditional HTTP/HTML request.
     * @return                     See above.
     */
    public boolean isAjax() {
        return ajaxRequest;
    }

    /**
     * This method is typically invoked by the {@code registerDataRequests}
     * methods of Sections to specify the data they will need to display
     * themselves.  This object keeps track of all of the requests that have
     * been requested; if the same request is registered multiple times, a
     * single request is shared by all of the requesters.
     * @param name                 Symbolic name for the request; must
     *                             correspond to a template in the
     *                             {@code dataRequests} configuration dataset.
     *                             Values from the main dataset for this
     *                             request will be used to expand the template.
     * @return                     The DataRequest corresponding to
     *                             {@code name}.  If this method is invoked
     *                             multiple times with the same {@code name},
     *                             all of the invocations will share the same
     *                             DataRequest.
     */
    public DataRequest registerDataRequest(String name) {
        DataRequest result = namedRequests.get(name);
        if (result == null) {
            result = new DataRequest(name, getMainDataset());
            namedRequests.put(name, result);
        }
        return result;
    }

    /**
     * This method is typically invoked by the {@code registerDataRequests}
     * methods of Sections to indicate the data they will need to display
     * themselves.  In this version of the method the caller has already
     * created the DataRequest; it will not be shared with any other Section.
     * @param request              DataRequest that will retrieve data needed
     *                             by the caller.
     * @return                     {@code request}.
     */
    public DataRequest registerDataRequest(DataRequest request) {
        unnamedRequests.add(request);
        return request;
    }

    /**
     * This method is invoked (typically by the dispatcher) to indicate
     * what kind of request this is.
     * @param isAjax               True means the current request should be
     *                             processed using the Fiz Ajax protocol;
     *                             false means it is a traditional HTML
     *                             request.
     */
    public void setAjax(boolean isAjax) {
        ajaxRequest = isAjax;
    }

    /**
     * Generate HTML for one or more Sections, appending it to the response
     * from this ClientRequest.  Each Section is first given a chance to
     * specify the DataRequests needed to supply its data, then the requests
     * are processed, then each Section uses the request results to
     * generate its HTML.
     * @param sections             Contents of the page: any number of Sections.
     */
    public void showSections(Section ... sections) {
        for (Section section : sections) {
            section.registerRequests(this);
        }
        startDataRequests();
        for (Section section : sections) {
            section.html(this);
        }
    }

    /**
     * Begin processing all of the DataRequests that have been registered
     * so far.  This method is typically invoked after all of the Sections
     * in a page have had a chance to register their requests; delaying
     * the start of the requests until now allows the requests to be
     * processed concurrently and/or batched.
     */
    public void startDataRequests() {
        if (unnamedRequests.size() > 0) {
            // If we have both named and unnamed requests, combined them
            // into a single collection to pass to DataRequest.start().
            for (DataRequest request : namedRequests.values()) {
                unnamedRequests.add(request);
            }
            DataRequest.start(unnamedRequests);
        } else {
            DataRequest.start(namedRequests.values());
        }
    }

    /**
     * This is a utility method shared by Ajax action generators; it
     * generates the initial part of the action's Javascript description,
     * including separator from the previous action (if any) and the
     * {@code type} property for this action (up to but not including
     * a comma separator).
     * @param type                 Value of the {@code type} property for
     *                             this action.
     * @return                     The PrintWriter for the response.
     */
    protected PrintWriter ajaxActionHeader(String type) {
        PrintWriter writer;
        try {
            writer = servletResponse.getWriter();
        }
        catch (IOException e) {
            throw new IOError(e.getMessage());
        }
        if (!anyAjaxActions) {
            // This is the first Ajax action.
            writer.append("var actions = [{type: \"");
            anyAjaxActions = true;
        } else {
            writer.append(", {type: \"");
        }
        writer.append(type);
        writer.append("\"");
        return writer;
    }
}
