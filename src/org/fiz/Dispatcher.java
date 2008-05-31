package org.fiz;
import java.io.*;
import java.lang.reflect.*;
import java.util.Hashtable;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

/**
 * The Dispatcher class is used internally by Fiz to provide the top-level
 * entry point for handling requests.  Its main job is to pick another class
 * and method to handle the request, based on information in the URL, and
 * invoke that method.  This class also serves as a last-ditch handler
 * for errors that occur during the request.  Dispatcher should be
 * invisible to Fiz applications.
 */
public class Dispatcher extends HttpServlet {
    /**
     * UnsupportedUriError is thrown when a URL arrives that can't
     * be mapped to an appopriate method to handle it.
     */
    protected static class UnsupportedUrlError extends Error {
        /**
         * Constructor for UnsupportedUriError.
         * @param url              The incoming URL (the full one).
         * @param message          More details about why we couldn't find
         *                         a method to handle this URL, or null
         *                         if no additional information available.
         */
        public UnsupportedUrlError(String url, String message) {
            super("unsupported URL \"" + url
                    + ((message != null) ? "\": " + message : "\""));
        }
    }

    // There exists one object of the following type for each method we
    // have discovered that can service an incoming request.

    protected class InteractorMethod {
        public Method method;      // Java reflection info about the method;
                                   // used to invoke it.
        public Interactor interactor;
                                   // The Interactor object to which "method"
                                   // belongs.  We invoke method on this
                                   // object to service incoming requests.
        public InteractorMethod(Method method, Interactor interactor) {
           this.method = method;
           this.interactor = interactor;
       }
    }

    // The following table keeps track of all the Interactor classes we have
    // encountered.  Keys in the table are class names and each value is
    // an object of the Key's class, which we used to invoke methods on
    // the class.
    //
    // Note: this table may be accessed and updated concurrently by
    // requests running in parallel.

    protected Hashtable<String,Interactor> classMap
            = new Hashtable<String,Interactor>();

    // The following table maps from strings of the form "class/method"
    // to an InteractorMethod object that allows us to invoke the method.
    //
    // Note: this table may be accessed and updated concurrently by
    // requests running in parallel.
    protected Hashtable<String, InteractorMethod> methodMap
            = new Hashtable<String, InteractorMethod>();

    // The following variable is used for log4j-based logging.
    protected Logger logger = Logger.getLogger("org.fiz.Dispatcher");

    // The following variables hold information from the last uncaught
    // exception handled by the "service" method.  This information is
    // intended primarily for use during testing.

    protected String basicMessage;    // getMessage() value from uncaught
                                      // exception.
    protected String fullMessage;     // The complete error message logged
                                      // by "service", including stack trace.

    // If the following variable is true, it means we are running tests
    // and should configure slightly differently.
    protected static boolean testMode = false;

    /**
     * This method is invoked by the servlet container when the servlet
     * is first loaded; we use it to perform our own initialization.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (testMode) {
            // Reduce the level of logging while running tests.
            logger.setLevel(Level.FATAL);
        }
        String contextRoot = config.getServletContext().getRealPath("");
        logger.info("Fiz initializing with context root " + contextRoot);
        Config.init(contextRoot + "/WEB-INF/config");
        Dataset main = Config.getDataset("main");
        if (main instanceof CompoundDataset) {
            Dataset[] components = ((CompoundDataset) main).getComponents();
            components[0].set("home", contextRoot);
        } else {
            main.set("home", contextRoot);
        }
        logger.info("main configuration dataset:\n    " +
                main.toString().trim().replace("\n", "\n    "));
        Css.init(contextRoot + "/WEB-INF/css");
    }

    /**
     * This method is invoked by the servlet container when the servlet
     * is about to be unloaded.  Provides us with an opportunity to do
     * internal cleanup.
     */
    @Override
    public void destroy() {
        logger.info("destroying dispatcher");

        // Invoke destroy methods in all of the Interactors that have been
        // loaded, so that they can clean themselves up.
        for (Interactor interactor : classMap.values()) {
            logger.info("destroying Interactor "
                    + interactor.getClass().getName());
            interactor.destroy();
        }
        classMap.clear();

        // Clean up all of the other Fiz modules.
        DataManager.destroyAll();
    }

    /**
     * This method is invoked for each incoming HTTP request whose URL
     * matches this application.  PathInfo (the portion of the URL
     * "owned" by this servlet) must have the form {@code /class/method/...},
     * which means it should be handled by method "method" in an Interactor
     * object of class "class".  The Interactor class is loaded and
     * instantiated if necessary, then the Interactor method is invoked.
     * @param request              Information about the HTTP request.
     * @param response             Used to generate the response.
     */
    @Override
    public void service (HttpServletRequest request,
        HttpServletResponse response) {
        ClientRequest cr = null;
        boolean isAjax = false;
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("incoming URL: " + Util.getUrlWithQuery(request));
            }

            // Use UTF-8 as the default encoding for all responses.
            response.setCharacterEncoding("UTF-8");

            // The "pathInfo" portion of the URL (the part that belongs to
            // us) must have the form /class/method/... Peel off the
            // "class/method" part and see if we already have information
            // about the method.

            String pathInfo = request.getPathInfo();
            String methodKey = null;
            InteractorMethod method = null;
            int endOfMethod = -1;
            int endOfClass = pathInfo.indexOf('/', 1);
            if (endOfClass >= 1) {
                endOfMethod = pathInfo.indexOf('/', endOfClass+1);
                if (endOfMethod == -1) {
                    endOfMethod = pathInfo.length();
                }
                methodKey = pathInfo.substring(1, endOfMethod);
                method = methodMap.get(methodKey);
            }
            isAjax = pathInfo.startsWith("ajax", endOfClass+1);

            if (method == null) {
                // We don't currently have any information about this method.
                // If we haven't already done so, scan the class specified
                // in the URL and update our tables with information about it.

                if ((endOfClass < 2) || (endOfMethod < (endOfClass+2))) {
                    throw new UnsupportedUrlError(request.getRequestURI(),
                            "URL doesn't contain class name and/or method "
                            + "name");
                }

                // See if we already know about this class.  Note: URL
                // characters are all lower-case, but class names must have
                // leading upper-case char.
                String className = Character.toUpperCase(pathInfo.charAt(1))
                        + pathInfo.substring(2, endOfClass);
                Interactor interactor = classMap.get(className);
                if (interactor == null) {
                    // The class name is not already known; load the class
                    // and create an instance of it.
                    interactor = (Interactor) Util.newInstance(className,
                            "org.fiz.Interactor");
                    classMap.put(className, interactor);
                    interactor.init();
                    logger.info("loaded Interactor " +
                            interactor.getClass().getName());

                    // Scan the methods for the class and remember each method
                    // that is public and takes a single argument that is a
                    // subclass of ClientRequest.
                    Class<?> requestClass = findClass("org.fiz.ClientRequest",
                            request);
                    for (Method m : interactor.getClass().getMethods()) {
                        Class[] parameterTypes = m.getParameterTypes();
                        if ((parameterTypes.length != 1)
                                || !requestClass.isAssignableFrom(
                                parameterTypes[0])) {
                            continue;
                        }
                        String key = pathInfo.substring(1, endOfClass+1)
                                + m.getName();
                        methodMap.put(key, new InteractorMethod(m, interactor));
                    }

                    // Try one more time to find the method we need.
                    method = methodMap.get(methodKey);
                }
                if (method == null) {
                    throw new UnsupportedUrlError(request.getRequestURI(),
                            "couldn't find method \""
                            + pathInfo.substring(endOfClass+1, endOfMethod)
                            + "\" with proper signature in class " + className);
                }
            }

            // At this point we have located the method to service this
            // request.  Package up relevant information into a ClientRequest
            // object and invoke the method.
            cr = method.interactor.getRequest(this,
                    request, response);
            if (isAjax) {
                cr.setAjax(true);
            }
            method.method.invoke(method.interactor, cr);

            // The service method has completed successfully.  Output the
            // response that was generated.
            cr.finish();
        }
        catch (Throwable e) {
            // If the exception happened in the Interactor method, the
            // real information we want is encapsulated inside e.
            Throwable cause =  e.getCause();
            if (cause == null) {
                cause = e;
            }

            // If the error occurred before we created a ClientRequest,
            // create one here so we can use it for reporting the error.
            if (cr == null) {
                cr = new ClientRequest(this, request, response);
                cr.setAjax(isAjax);
            }
            if (cause instanceof HandledError) {
                // There was an error, but it was already handled.  The
                // error was thrown simply to terminate the processing of
                // the request.
                cr.finish();
                return;
            }

            // Print details about the error to the log.
            StringWriter sWriter= new StringWriter();
            basicMessage = cause.getMessage();
            cause.printStackTrace(new PrintWriter(sWriter));
            fullMessage = "unhandled exception for URL \""
                    + Util.getUrlWithQuery(request) + "\":\n"
                    + sWriter.toString();
            logger.error(fullMessage);

            // If this is an AJAX request then return the error message
            // via the AJAX protocol.
            if (cr.isAjax()) {
                cr.ajaxErrorAction(new Dataset("message",
                        Template.expand(Config.get("errors", "uncaughtAjax"),
                        new Dataset("message", basicMessage),
                        Template.SpecialChars.NONE)));
                cr.finish();
                return;
            }

            // This is a normal HTML request; use a template from the
            // "errors" configuration dataset to generate HTML describing
            // the error.
            Html html = cr.getHtml();
            try {
                if (Config.get("errors", "clearOnUncaught").equals("true")) {
                    html.clear();
                }
                Template.expand(Config.get("errors", "uncaughtHtml"),
                        new Dataset("message", basicMessage), html.getBody());
            }
            catch (Throwable t) {
                // Things are really messed up: an exception happened while
                // trying to report an exception.  Generate an HTML report.
                sWriter.getBuffer().setLength(0);
                t.printStackTrace(new PrintWriter(sWriter));
                logger.error("unhandled exception while reporting " +
                        "an unhandled exception in Dispatcher:\n" +
                        sWriter.toString());
                html.clear();
                html.getBody().append("<div class=\"uncaughtException\">" +
                        "Multiple internal errors in the server!  Details " +
                        "are in the server's log</div>.\n");
            }
            cr.finish();
        }
    }

    /**
     * This utility method looks up a class and generates an appropriate
     * Error if the class can't be found.
     * @param className            Name of the desired class
     * @param request              Information about the HTTP request (used
     *                             for generating error messages)
     * @return                     The Class object corresponding to
     *                             className.
     */
    protected Class<?> findClass(String className, HttpServletRequest request) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new UnsupportedUrlError(request.getRequestURI(),
                    "can't find class \"" + className + "\"");
        }
    }
}
