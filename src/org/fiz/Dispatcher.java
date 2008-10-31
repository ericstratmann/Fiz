package org.fiz;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
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

    protected static class InteractorMethod {
        public Method method;      // Java reflection info about the method;
                                   // used to invoke it.
        public Interactor interactor;
                                   // The Interactor object to which "method"
                                   // belongs.  We invoke method on this
                                   // object to service incoming requests.
        public int invocations;    // Number of times an incoming request
                                   // invoked this method.
        public double totalNs;     // Total time spent executing requests
                                   // for this method, and nanoseconds.
        public double totalSquaredNs;
                                   // The sum across all requests for this
                                   // method of the squares of the execution
                                   // times.  Used to compute standard
                                   // deviations.

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
    // TODO: change from Hashtable to HashMap?

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

    // The following object is used to gather performance statistics
    // for requests that cannot be mapped to an Interactor method
    // (i.e., all of the requests that don't get logged in methodMap).
    protected InteractorMethod unsupportedURL =
            new InteractorMethod(null, null);

    // If the following variable is true, we flush all of our internal
    // caches on every request.  This variable mirrors the "clearCaches"
    // entry in the main configuration dataset.  If the dataset value
    // changes to false then we stop flushing caches (and will no longer
    // see any changes to that dataset; to reenable cache flushing you
    // will have to change the dataset and also restart the application).
    protected boolean clearCaches = true;

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
        Config.init(contextRoot + "/WEB-INF/config",
                contextRoot + "/WEB-INF/fiz/config");
        initMainConfigDataset(contextRoot);
        logger.info("main configuration dataset:\n    " +
                Config.getDataset("main").toString().trim().replace(
                "\n", "\n    "));
        Css.init(contextRoot + "/WEB-INF/css",
                contextRoot + "/WEB-INF/fiz/css");

        // If you want more detailed logging, such as request URLs, uncomment
        // the following line.
        // logger.setLevel(Level.WARN);
    }

    /**
     * Clear all of the caches maintained by Fiz, so that it will be
     * reloaded from disk the next time is needed.  This is typically done
     * when debugging, so that file changes will be reflected immediately,
     * without restarting the application.
     */
    public static void clearCaches() {
        Config.clearCache();
        Css.clearCache();
        DataManager.clearCaches();
        Html.clearJsDependencyCache();
        TabSection.clearCache();
    }

    /**
     * Resets all of the statistics about Interactor execution time.
     */
    public void clearInteractorStatistics() {
        for (String methodName : methodMap.keySet()) {
            InteractorMethod method = methodMap.get(methodName);
            method.invocations = 0;
            method.totalNs = method.totalSquaredNs = 0.0;
        }
        unsupportedURL.invocations = 0;
        unsupportedURL.totalNs = unsupportedURL.totalSquaredNs = 0.0;
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
     * Generates a dataset containing usage statistics for all of the
     * Interactor methods that have been invoked.
     * @return                     The return value is a collection containing
     * one Dataset for each Interactor method that has been invoked so far,
     * plus an extra child with the name "unsupportedURL", which records
     * information about URLs that could not be mapped to a method.  Each
     * dataset contains the following elements:
     *   averageMs:                Average execution time (total, including
     *                             dispatch overhead) for each invocation
     *                             of this method, in milliseconds (float).
     *   invocations:              Number of times this method has been
     *                             invoked.
     *   name:                     Name of the Interactor method (class/name).
     *   standardDeviationMs:      The standard deviation of the execution
     *                             time for this method, in milliseconds
     *                             float).
     */
    public ArrayList<Dataset> getInteractorStatistics() {
        ArrayList<Dataset> result = new ArrayList<Dataset>();
        for (String methodName : methodMap.keySet()) {
            InteractorMethod method = methodMap.get(methodName);
            addStatistics(methodName, method, result);
        }
        addStatistics("unsupportedURL", unsupportedURL, result);
        return result;
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
        long startTime = System.nanoTime();
//        Perf.resetIntervals();
        InteractorMethod method = null;
        ClientRequest cr = null;
        boolean isAjax = false;
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("incoming URL: " + Util.getUrlWithQuery(request));
            }

            // See if we are in a debugging mode where we should flush caches
            // before every request.
            if (clearCaches) {
                String clear = Config.get("main", "clearCaches");
                if ((clear != null) && (!clear.equals("true"))) {
                    clearCaches = false;
                } else {
                    clearCaches();
                    initMainConfigDataset(getServletContext().getRealPath(""));
                }
            }

            // Use UTF-8 as the default encoding for all responses.
            response.setCharacterEncoding("UTF-8");

            // The "pathInfo" portion of the URL (the part that belongs to
            // us) must have the form /class/method/... Peel off the
            // "class/method" part and lookup the method.

            String pathInfo = request.getPathInfo();
            String classAndMethod;
            int endOfMethod = -1;
            int endOfClass = pathInfo.indexOf('/', 1);
            if (endOfClass >= 1) {
                endOfMethod = pathInfo.indexOf('/', endOfClass+1);
                if (endOfMethod == -1) {
                    endOfMethod = pathInfo.length();
                }
            }
            if ((endOfClass < 2) || (endOfMethod < (endOfClass+2))) {
                throw new UnsupportedUrlError(request.getRequestURI(),
                        "URL doesn't contain class name and/or method "
                        + "name");
            }
            isAjax = pathInfo.startsWith("ajax", endOfClass+1);
            method = findMethod(pathInfo.substring(1, endOfMethod),
                    endOfClass-1, request);

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
//        Perf.markInterval("end");
//        logger.warn("Time intervals:\n" + Perf.intervalInfo("  "));
        if (method == null) {
            method = unsupportedURL;
        }
        long endTime = System.nanoTime();
        double elapsed = endTime - startTime;
        method.invocations++;
        method.totalNs += elapsed;
        method.totalSquaredNs += elapsed*elapsed;
    }

    /**
     * This method does most of the work of {@code getInteractorStatistics}:
     * it adds information for one method to the result Dataset being
     * assembled.
     * @param methodName           Name of the method.
     * @param method               Record containing statistics about the
     *                             method.
     * @param result               A new dataset, containing the information
     *                             for {@code method}, is appended here..
     */
    protected static void addStatistics(String methodName,
            InteractorMethod method, ArrayList<Dataset> result) {
        if (method.invocations == 0) {
            return;
        }
        double average = method.totalNs/method.invocations;
        double deviation = Math.sqrt(method.totalSquaredNs/method.invocations
                - average*average);
        result.add(new Dataset("name", methodName,
                "invocations", Integer.toString(method.invocations),
                "averageMs", String.format("%.3f", average/1.0e6),
                "standardDeviationMs", String.format("%.3f", deviation/1.0e6)));
    }

    /**
     * This utility method looks up a class and generates an appropriate
     * Error if the class can't be found.
     * @param className            Name of the desired class
     * @param request              Information about the HTTP request (used
     *                             for generating error messages).
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

    /**
     * Find the method that will handle a request.  If needed, information
     * is added to our cache of known methods.
     * @param classPlusMethod      Portion of the URL identifies the method
     *                             to handle the request. Has the form
     *                             {@code class/method}.
     * @param slashIndex           Index within {@code classPlusMethod} of
     *                             the letter slash that separates the class
     *                             and method.
     * @param request              Information about the HTTP request (used
     *                             for generating error messages).
     * @return                     An InteractorMethod object describing
     *                             the method corresponding to
     *                             {@code classPlusMethod}.
     */
    protected synchronized InteractorMethod findMethod(String classPlusMethod,
            int slashIndex, HttpServletRequest request) {
        InteractorMethod method = methodMap.get(classPlusMethod);
        if (method != null) {
            return method;
        }

        // We don't currently have any information about this method.
        // If we haven't already done so, scan the class specified
        // in the URL and update our tables with information about it.
        // First, see if we already know about this class.  The class
        // name is computed by upper-casing the first character of the
        // class in the URL and appending "Interactor".
        String className = StringUtil.ucFirst(classPlusMethod.substring(0,
                slashIndex)) + "Interactor";
        String methodName = classPlusMethod.substring(slashIndex+1);
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
                String key = classPlusMethod.substring(0, slashIndex+1)
                        + m.getName();
                methodMap.put(key, new InteractorMethod(m, interactor));
            }

            // Try one more time to find the method we need.
            method = methodMap.get(classPlusMethod);
            if (method != null) {
                return method;
            }
        }

        throw new UnsupportedUrlError(request.getRequestURI(),
                "couldn't find method \"" + methodName
                + "\" with proper signature in class " + className);

    }

    /**
     * Load the main configuration data set and add a "home" entry to
     * it that refers to our context root (the directory that contains
     * the WEB-INF directory).
     * @param home                 Root directory for this application.
     */
    protected static void initMainConfigDataset(String home) {
        Dataset main = Config.getDataset("main");
        if (main instanceof CompoundDataset) {
            Dataset[] components = ((CompoundDataset) main).getComponents();
            components[0].set("home", home);
        } else {
            main.set("home", home);
        }
    }
}
