package org.fiz;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * A ClientRequest object provides access to all of the interesting state
 * needed to process an HTTP request coming from the browser.  It is
 * typically passed to all of the major methods that service the request.
 * ClientRequest objects include the HttpServletRequest, HttpServletResponse,
 * and HttpServlet objects provided by the servlet container .  ClientRequests
 * also include additional Fiz objects such as a dataset containing the
 * request's query values and an Html object for building the response.
 * <p>
 * It may be useful for an application to extend ClientRequests with additional
 * data;  in this case the application should subclass ClientRequest to add the
 * additional fields and override the {@code getRequest} method in the
 * application's Interactors to supply the ClientRequest subclass.
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
    protected Dataset dataset = null;

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
}
