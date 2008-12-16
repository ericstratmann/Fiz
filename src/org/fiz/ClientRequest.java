package org.fiz;

import java.io.*;
import java.util.*;
import javax.crypto.*;
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

    // Top-level dataset containing global information for this request,
    // such as query values and POST data.
    protected Dataset mainDataset = null;

    // The following variable keeps track of all of the reminders that
    // have been read from the POST data for the request.  Null means
    // either the data has not been processed yet, or it contained
    // no reminders.
    protected HashMap<String,Dataset> reminders = null;

    // Indicates whether or not we have already attempted to process
    // Ajax POST data for this request.  If true, don't try again.
    protected boolean fizDataProcessed = false;

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

    // The following variable indicates whether this request is an Ajax
    // request (false means it is a normal HTML request).
    protected boolean ajaxRequest = false;

    // The following variable indicates whether any Ajax actions have been
    // generated in response to this request.
    protected boolean anyAjaxActions = false;

    // The following variable indicates whether any messages have been added
    // to the bulletin during this request (it's used to clear the bulletin
    // before the first message is added).
    protected boolean anyBulletinMessages = false;

    // If returnFile has been invoked, the following variables record
    // information about the file to be returned at the end of the request.
    protected String fileName = null;
    protected InputStream fileSource = null;

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
     * Expands a template to generate HTML and adds it to the bulletin
     * as a new message.  If this is the first message added to the bulletin
     * for this request, any previous bulletin contents are cleared.  This
     * method can be invoked for either Ajax requests or normal requests
     * and does the right thing in either case.
     * @param template             Template to expand to generate the HTML
     *                             for the bulletin message.
     * @param data                 Dataset to use when expanding
     *                             {@code template}.
     * @param divClass             The HTML will be added to the bulletin as
     *                             the contents of a new div element; this
     *                             parameter specifies the class of that
     *                             element (typically this selects CSS
     *                             that determines how the message is
     *                             displayed).
     */
    public void addMessageToBulletin(String template, Dataset data,
            String divClass) {
        String html = Template.expand(template, data);
        StringBuilder javascript = new StringBuilder();
        if (!anyBulletinMessages) {
            javascript.append("Fiz.clearBulletin(); ");
            anyBulletinMessages = true;
        }
        Template.expand("Fiz.addBulletinMessage(\"@1\", \"@2\");",
                javascript, Template.SpecialChars.JAVASCRIPT, divClass, html);
        if (ajaxRequest) {
            ajaxEvalAction(javascript.toString());
        } else {
            getHtml().includeJavascript(javascript.toString());
        }
    }

    /**
     * Generate bulletin messages for one or more errors, each described
     * by a dataset.  Typically the errors are the result of a DataRequest.
     * A separate bulletin message is generated for each dataset in
     * {@code errors}, using the {@code bulletin} template from the
     * {@code errors} configuration dataset to generate HTML from the
     * dataset.  The messages will appear in divs with class "bulletinError".
     * @param errors               One or more Datasets, each containing
     *                             information about a particular error;
     *                             typically this is the return value from
     *                             DataRequest.getErrors().
     */
    public void addErrorsToBulletin(Dataset... errors) {
        for (Dataset error : errors) {
            addMessageToBulletin(Config.get("styles", "bulletin"), error,
                    "bulletinError");
        }
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
        // TODO: buffer Ajax actions just like HTML?
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
     * Generate Ajax actions that will replace the innerHTML for one or more
     * Sections.
     * @param idsAndSections       An even number of arguments, grouped in
     *                             pairs of which the first argument is a
     *                             String containing an HTML element id, and
     *                             the second is a Section.  Each Section's
     *                             HTML will be regenerated and an Ajax
     *                             action will be created that uses the HTML
     *                             to replace the innerHTML of the
     *                             corresponding HTML element.
     */
    public void ajaxUpdateSections(Object... idsAndSections) {
        StringBuilder out = getHtml().getBody();
        int oldLength = out.length();
        int lastId = idsAndSections.length - 2;

        // This method operates in 2 passes, much like showSections:
        // the first pass registers of data requests, which can then
        // execute in parallel.  The second pass generates the HTML
        // and the Ajax actions.
        for (int i = 0; i <= lastId; i += 2) {
            Section section = (Section) idsAndSections[i+1];
            section.registerRequests(this);
        }
        startDataRequests();
        for (int i = 0; i <= lastId; i += 2) {
            String id = (String) idsAndSections[i];
            Section section = (Section) idsAndSections[i+1];
            section.html(this);
            ajaxUpdateAction(id, out.substring(oldLength));
            out.setLength(oldLength);
        }
    }

    /**
     * Return the contents of a given reminder if it exists, or null if
     * it doesn't.
     * @param name                 Name of the desired reminder.
     * @return                     The dataset whose contents represent the
     *                             value of the reminder named {@code name}.
     *                             If there is no such reminder in the current
     *                             request null is returned.
     */
    public Dataset checkReminder(String name) {
        if (!fizDataProcessed) {
            readFizData();
        }
        if (reminders == null) {
            return null;
        }
        return reminders.get(name);
    }

    /**
     * This method is invoked by the Dispatcher at the end of a request
     * to complete the transmission of the response back to the client.
     */
    public void finish() {
        if (fileSource != null) {
            // This is a file download request.
            ServletOutputStream out;
            try {
                out = servletResponse.getOutputStream();
            }
            catch (IOException e) {
                logger.error("I/O error retrieving response output stream in " +
                        "ClientRequest.finish: " +
                        StringUtil.lcFirst(e.getMessage()));
                return;
            }
            // Get the correct MIME type for this file.
            String mimeType = null;
            if (fileName != null) {
                // Set the file name if one was specified.
                servletResponse.setHeader("Content-Disposition",
                        "filename=\"" + fileName + "\"");
                mimeType = getServletContext().getMimeType(fileName);
            }
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            servletResponse.setContentType(mimeType);
            try {
                // Write the contents of fileSource into the servlet response
                // stream.
                int length;
                byte[] buf = new byte[4096];
                while ((length = fileSource.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
            } catch (IOException e) {
                logger.error("I/O error sending response in " +
                    "ClientRequest.finish");
            }
            try {
                // Close the input stream.
                fileSource.close();
            } catch (IOException e) {
                logger.warn("I/O error closing file input stream in " +
                        "ClientRequest.finish", e);
            }
            try {
                // Flush the output stream so we find out about any errors
                // right now.
                out.flush();
            } catch (IOException e) {
                logger.error("I/O error flushing output stream in " +
                        "ClientRequest.finish", e);
            }
            return;
        }

        // This is a "normal" HTML or Ajax response.
        PrintWriter out;
        try {
            out = servletResponse.getWriter();
        }
        catch (IOException e) {
            logger.error("I/O error retrieving response writer in " +
                    "ClientRequest.finish: " +
                    StringUtil.lcFirst(e.getMessage()));
            return;
        }
        if (ajaxRequest) {
            if (anyAjaxActions) {
                // Close off the Javascript code for the actions.
                out.append("];");
            } else {
                // Output Javascript code for an empty action array (the
                // browser will be expecting the array, even if it is empty.
                out.append("var actions = [];");
            }
        } else {
            // This is an HTML request.  Transmit the accumulated
            // HTML, if any.
            servletResponse.setContentType("text/html");
            if (html != null) {
                html.print(out);
            }
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
     * Returns a Mac object specific to the session for this request,
     * which can be used to cryptographically sign data to prevent
     * tampering by this session and replay in other sessions.
     * @return                     An HMAC-SHA256 Mac object.
     */
    public Mac getMac() {
        HttpSession session = servletRequest.getSession(true);

        // There could potentially be multiple requests for the same
        // session running concurrently, so synchronization is needed
        // here.
        synchronized(session) {
            Object o = session.getAttribute("fiz.mac");
            if (o != null) {
                return (Mac) o;
            }

            // This is the first time we have needed a Mac object in this
            // session, so we have to create a new one.  First generate a
            // secret key for HMAC-MD5.
            try {
                KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256");
                SecretKey sk = kg.generateKey();

                // Create a Mac object implementing HMAC-MD5, and
                // initialize it with the above secret key.
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(sk);
                session.setAttribute("fiz.mac", mac);
                return mac;
            } catch (Exception e) {
                throw new InternalError(
                        "ClientRequest.getMac couldn't create a new Mac: "
                        + e.getMessage());
            }
        }
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
        if (!fizDataProcessed) {
            readFizData();
        }

        // Next, load any query data provided to the request.
        if (mainDataset == null) {
            mainDataset = new Dataset();
        }
        Enumeration params = servletRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String) params.nextElement();
            mainDataset.set(name, servletRequest.getParameter(name));
        }
        return mainDataset;
    }

    /**
     * Return the contents of a given reminder if it exists, or generate
     * an exception if it doesn't.
     * @param name                 Name of the desired reminder.
     * @return                     The dataset whose contents represent the
     *                             value of the reminder named {@code name}.
     * @throws InternalError       No such reminder is available in the
     *                             current request.
     */
    public Dataset getReminder(String name) throws InternalError {
        Dataset result = checkReminder(name);
        if (result == null) {
            throw new InternalError("couldn't find reminder \"" +
                    name + "\"");
        }
        return result;
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
     * Arrange for the response to include Javascript code, which will be
     * evaluated by the browser when it receives a response.  This method
     * works for both AJAX requests and normal HTML requests.
     * @param javascript           Javascript code for the browser to
     *                             execute.
     */
    public void includeJavascript(CharSequence javascript) {
        if (ajaxRequest) {
            ajaxEvalAction(javascript);
        } else {
            getHtml().includeJavascript(javascript);
        }
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
     * Create a new DataRequest based on the name of an entry in the
     * {@code dataRequests} configuration dataset, and associate it with
     * this ClientRequest so that it will be started by the
     * {@code startDataRequests} method.  This method is typically invoked
     * by the {@code registerDataRequests} methods of Sections to specify
     * the data they will need in order to display themselves.
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
     * Given a DatedRequest created by the caller, associate it with this
     * ClientRequest so that it will be started by the
     * {@code startDataRequests} method.  This method is typically invoked
     * by the {@code registerDataRequests} methods of Sections to specify
     * the data they will need in order to display themselves.
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
     * Create a DataRequest based on an element of a dataset, and associate
     * it with this  ClientRequest so that it will be started by the
     * {@code startDataRequests} method.  If the dataset element named by
     * {@code d} and {@code path} is a string, then a (potentially shared)
     * DataRequest is created in the same way as if the string had been
     * passed directly to {@code registerDataRequest}.  If the dataset element
     * is a nested dataset, then its contents are used directly as the
     * arguments for the request.
     * @param d                    Dataset whose contents will be used to
     *                             create a DataRequest.
     * @param path                 Specifies the path to an element
     *                             within {@code d}.  If there is no such
     *                             element in the dataset then no DataRequest
     *                             is created.
     * @return                     The DataRequest corresponding to
     *                             {@code d} and {@code path}, or  null if
     *                             {@code path} doesn't exist.
     */
    public DataRequest registerDataRequest(Dataset d, String path) {
        Object value = d.lookupPath(path, Dataset.DesiredType.ANY,
                Dataset.Quantity.FIRST_ONLY);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return registerDataRequest((String) value);
        }
        DataRequest request = new DataRequest((Dataset) value);
        unnamedRequests.add(request);
        return request;
    }

    /**
     * Arrange for the response to this request to consist of the contents
     * of a file, rather than HTML or Ajax actions.  Any other response
     * information, such as HTML, will be ignored once this method has been
     * invoked.
     *
     * @param fileName              The name of the file to send to the
     *                              browser; used to compute an appropriate
     *                              MIME tie for the response.
     * @param fileSource            An {@code InputStream} that contains
     *                              the file contents. This input stream will
     *                              be closed by the ClientRequest object in
     *                              its {@code finish} method.  Null means
     *                              cancel any previous call to this method,
     *                              restoring the normal HTML or Ajax
     *                              response.
     */
    public void returnFile(String fileName, InputStream fileSource) {
        this.fileName = fileName;
        this.fileSource = fileSource;
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
     * Set the value of a reminder explicitly, overriding the normal
     * mechanism for processing incoming data.  This method is intended
     * primarily for used in unit tests.
     * @param name                 Name of a reminder.
     * @param contents             Value for the reminder.
     */
    public void setReminder(String name, Dataset contents) {
        if (reminders == null) {
            reminders = new HashMap<String,Dataset>();
        }
        reminders.put(name, contents);
        fizDataProcessed = true;
    }

    /**
     * This method is invoked to display appropriate information after an
     * error has occurred.  {@code style} and {@code defaultStyle} are used
     * to select a template from the {@code styles} dataset.  If there exists
     * such a template, it is expanded in the context of {@code errorData}
     * plus the main dataset and the result is appended to the HTML for the
     * page.  Then {@code -bulletin} is appended to the style name; if
     * the {@code styles} configuration dataset contains a template by this
     * name, it is expanded and the result is appended to the bulletin for
     * the page.  If neither of the templates exists, then an error is
     * generated.
     * @param style                Name of a value in the {@code errors}
     *                             dataset, which is a template for generating
     *                             HTML to describe the error.  May be null.
     * @param defaultStyle         If {@code style} is null, then this is used
     *                             as the name of the template.
     * @param errorDatasets        One or more datasets, each describing one
     *                             error; each dataset should contain
     *                             at least a {@code message} value.
     */
    public void showErrorInfo(String style, String defaultStyle,
            Dataset... errorDatasets) {
        for (Dataset errorData: errorDatasets) {
            CompoundDataset compound = new CompoundDataset(errorData,
                    getMainDataset());
            if (style == null) {
                style = defaultStyle;
            }
            boolean foundTemplate = false;
            Dataset styles = Config.getDataset("styles");
            String template = (String) styles.lookupPath(style,
                    Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY);
            if (template != null) {
                foundTemplate = true;
                Template.expand(template, compound, getHtml().getBody());
            }
            template = (String) styles.lookupPath(style + "-bulletin",
                    Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY);
            if (template != null) {
                foundTemplate = true;
                getHtml().includeJavascript("Fiz.addBulletinMessage(\"@html\");",
                        new Dataset("html", Template.expand(template,
                        compound)));
            }
            if (!foundTemplate) {
                throw new InternalError("showErrorInfo found no \"" +
                        style + "\" template " +
                        "for displaying error information");
            }
        }
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

    /**
     * This method reads data in the special format used to transmit
     * information from the browser to the server during Ajax requests,
     * populating various internal data structures with the results.
     * If this method has already been called for the current request,
     * or if there is no data in the expected format, than this method
     * does nothing.
     */
    protected void readFizData () {
        if (fizDataProcessed) {
            // We have already processed the data once; no need to
            // do it again.
            return;
        }
        fizDataProcessed = true;

        // Make sure that the incoming data has the right MIME type.
        String contentType = (servletRequest.getContentType());
        if ((contentType == null)
                || !contentType.startsWith("text/fiz")) {
            return;
        }

        // Read in all of the posted data.
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
            throw new IOError("I/O error in ClientRequest.readFizData: " +
                    e.getMessage());
        }

        // The posted data has the form <type>.<data><type>.<data>...
        // where <type> (a string delimited by ".") indicates the
        // kind of information that follows.  The format of each
        // <data> block depends on its type.
        //
        // Each iteration of the following loop processes one block.

        int current = 0;
        int length = postData.length();
        IntBox end = new IntBox();
        while (current < length) {
            // Extract the next <type> field.
            int start = current;
            while (true) {
                if (current >= length) {
                    throw new SyntaxError("missing \".\" after type \"" +
                            postData.substring(start, current) +
                            "\" in Fiz browser data");
                }
                if (postData.charAt(current) == '.')  {
                    break;
                }
                current++;
            }
            String type = postData.substring(start, current);
            current++;

            // Check against the known types and handle appropriately.
            if (type.equals("main")) {
                if (mainDataset == null) {
                    mainDataset = new Dataset();
                }
                current = mainDataset.addSerializedData(postData, current);
            } else if (type.equals("reminder")) {
                Dataset d = new Dataset();
                String reminderName = Reminder.decode(this, postData,
                        current, d, end);
                if (reminders == null) {
                    reminders = new HashMap<String,Dataset>();
                }
                reminders.put(reminderName, d);
                current = end.value;
            } else {
                throw new SyntaxError("unknown type \"" + type +
                        "\" in Fiz browser data");
            }
        }
    }
}
