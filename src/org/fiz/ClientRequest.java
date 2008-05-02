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
     * Returns the main dataset for this request.  Initially the dataset
     * contains query values provided in the URI, but requests may add
     * values to the dataset in cases where the data needs to be used
     * globally across the request.
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
     * Generate a string containing the names of all of the DataRequests
     * registered so far for this ClientRequests.  This method is used
     * primarily for testing
     * @return                     A string containing names of all of the
     *                             named requests registered so far, in
     *                             alphabetical order.
     */
    public String getRequestNames() {
        StringBuffer result = new StringBuffer();
        String prefix = "";
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(namedRequests.keySet());
        Collections.sort(names);
        for (String name: names) {
            result.append(prefix);
            result.append(name);
            prefix = ", ";
        }
        return result.toString();
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
            result = new DataRequest(name, getDataset());
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
}
