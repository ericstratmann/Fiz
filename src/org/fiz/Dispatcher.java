/**
 * The Dispatcher class is the top-level entry point invoked to handle
 * incoming HTTP requests.  Its main job is to pick another class and
 * method to handle the request, based on information in the URL, and
 * invoke that class.  This class also serves as a last-ditch  handler
 * for errors that occur during the request.  This class should be
 * invisible to Fiz applications.
 */

package org.fiz;
import java.io.*;
import java.lang.reflect.*;
import java.util.Hashtable;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

public class Dispatcher extends HttpServlet {
    /**
     * UnsupportedUriError is thrown when a URI arrives that can't
     * be mapped to an appopriate method to handle it.
     */
    public static class UnsupportedUriError extends Error {
        /**
         * Constructor for UnsupportedUriError.
         * @param uri              The incoming URI (the full one).
         * @param message          More details about why we couldn't find
         *                         a method to handle this URI, or null
         *                         if no additional information available.
         */
        public UnsupportedUriError(String uri, String message) {
            super("unsupported URI \"" + uri
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

    /**
     * This method is invoked by the servlet container when the servlet
     * is first loaded; we use it to perform our own initialization.
     */
    public void init() {
        // Right now, nothing to do.
    }

    /**
     * This method is invoked by the servlet container when the servlet
     * is about to be unloaded.  Provides us with an opportunity to do
     * internal cleanups.
     */
    public void destroy() {
        logger.info("destroying dispatcher");

        // Invoke destroy methods in all of the Interactors that have been
        // loaded, so that they can clean themselves up.
        for (Interactor interactor : classMap.values()) {
            logger.info("destroying Interactor "
                    + interactor.getClass().getName());
            interactor.destroy();
        }
    }

    /**
     * This method is invoked for each incoming HTTP request whose URI
     * matches this application.  This method dispatches the request to
     * a method in an Interactor object based on information in the URI.
     * This method also loads Interactor classes and creates Interactor
     * objects at the time of the first request for each class.
     * @param request              Information about the HTTP request
     * @param response             Used to generate the response
     */
    public void service (HttpServletRequest request,
        HttpServletResponse response) {
        try {
            if (logger.isTraceEnabled()) {
                String query = request.getQueryString();
                logger.trace("incoming URI: " + Util.getUriAndQuery(request));
            }
            // The "pathInfo" portion of the URI (the part that belongs to
            // us) has the form /class/method/... Peel off the "class/method"
            // part and see if we already have information about the method.

            String methodKey = null;
            InteractorMethod method = null;
            int endOfMethod = -1;
            String pathInfo = request.getPathInfo();
            int endOfClass = pathInfo.indexOf('/', 1);
            if (endOfClass >= 1) {
                endOfMethod = pathInfo.indexOf('/', endOfClass+1);
                if (endOfMethod == -1) {
                    endOfMethod = pathInfo.length();
                }
                methodKey = pathInfo.substring(1, endOfMethod);
                method = methodMap.get(methodKey);
            }

            if (method == null) {
                // We don't currently have any information about this method.
                // If we haven't already done so, scan the class specified
                // in the URI and update our tables with information about it.

                if ((endOfClass < 2) || (endOfMethod < (endOfClass+2))) {
                    throw new UnsupportedUriError(request.getRequestURI(),
                            "URI doesn't contain class name and/or method "
                            + "name");
                }

                // See if we already know about this class.  Note: URI
                // characters are all lower-case, but class names must have
                // leading upper-case char.
                // TODO: must generalize to load classes not in org.fiz
                String className = "org.fiz."
                        + Character.toUpperCase(pathInfo.charAt(1))
                        + pathInfo.substring(2, endOfClass);
                Interactor interactor = classMap.get(className);
                if (interactor == null) {
                    // This is a new class.  First load the class.
                    Class<?> cl;
                    try {
                        cl =  Class.forName(className);
                    }
                    catch (ClassNotFoundException e) {
                        throw new UnsupportedUriError(request.getRequestURI(),
                                "can't find class \"" + className + "\"");
                    }

                    // Make sure the class is a subclass of Interactor.
                    Class<?> interactorClass = null;
                    try {
                        interactorClass = Class.forName("org.fiz.Interactor");
                    }
                    catch (ClassNotFoundException e) {
                        logger.error("Dispatcher.init couldn't find "
                                + "org.fiz.Interactor class: "
                                + e.getMessage());
                    }
                    if (!interactorClass.isAssignableFrom(cl)) {
                        throw new UnsupportedUriError(request.getRequestURI(),
                                "class \"" + className
                                + "\" isn't a subclass of org.fiz.Interactor");
                    }

                    // Find a no-argument constructor and create an instance.
                    Constructor constructor;
                    try {
                        constructor = cl.getConstructor();
                    }
                    catch (Exception e) {
                        throw new UnsupportedUriError(request.getRequestURI(),
                                "couldn't find no-argument constructor for "
                                + "class \"" + className + "\": "
                                + e.getMessage());
                    }
                    try {
                        interactor = (Interactor) constructor.newInstance();
                    }
                    catch (Exception e) {
                        Throwable cause = e.getCause();
                        if (cause == null) {
                            cause = e;
                        }
                        throw new UnsupportedUriError(request.getRequestURI(),
                                "couldn't create instance of class \""
                                + className + "\": " + cause.getMessage());
                    }
                    classMap.put(className, interactor);
                    interactor.init();

                    // Scan the methods for the class and remember each method
                    // that is public and has the appropriate number and types
                    // of arguments.
                    for (Method m : cl.getMethods()) {
                        if (!Modifier.isPublic(m.getModifiers())) {
                            continue;
                        }
                        Class[] parameterTypes = m.getParameterTypes();
                        if (parameterTypes.length != 2) {
                            continue;
                        }
                        if (!parameterTypes[0].getName().equals(
                                "javax.servlet.http.HttpServletRequest")
                                || !parameterTypes[1].getName().equals(
                                "javax.servlet.http.HttpServletResponse")) {
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
                    throw new UnsupportedUriError(request.getRequestURI(),
                            "no method \"" + pathInfo.substring(endOfClass+1,
                            endOfMethod) + "\" in class " + className);
                }
            }

            // At this point we have located the method to service this
            // request.  Invoke it.
            method.method.invoke(method.interactor, request, response);
        }
        catch (Throwable e) {
            // TODO.  Possible approaches:
            // * Generate a standard result page (handle AJAX especially)
            // * Generate a log message.
            // * Invoke application-specific code to handle (but catch
            //   any errors in that code).  Use configuration information
            //   to figure out what to invoke?  Or, just look for a
            //   well-defined class?

            StringWriter sWriter= new StringWriter();
            Throwable cause =  e.getCause();
            if (cause == null) {
                cause = e;
            }
            basicMessage = cause.getMessage();
            cause.printStackTrace(new PrintWriter(sWriter));
            fullMessage = "unhandled exception for URI \""
                    + Util.getUriAndQuery(request) + "\"\n"
                    + sWriter.toString();
            if (logger.isTraceEnabled()) {
                logger.error(fullMessage);
            }
        }
    }
}
