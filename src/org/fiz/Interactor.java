package org.fiz;
import javax.servlet.http.*;

/**
 * The Interactor class is subclassed by classes that process
 * incoming HTTP requests.  Fiz automatically dispatches incoming
 * requests to methods of an Interactor subclass based on the URI.  For
 * example, if a class MyClass subclasses Interactor and has methods
 * {@code create} and {@code delete}, than the {@code create} method will be
 * invoked to service URIs of the form
 * {@code /appName/fiz/myClass/create/extra}:
 *     - {@code appName} is a URI prefix identifying this particular
 *       application; it can have any number of path levels, including 0.
 *       Tomcat must have been configured to pass all requests for this
 *       prefix to the Fiz Dispatcher servlet.
 *     - {@code myClass} is the name of the class, except with a lower-case
 *       initial letter; this class must be a subclass of Interactor.
 *     - {@code create} is the name of the method to invoke in
 *       {@code myClass}; this method must be public and take 1 arguments
 *       consisting of a ClientRequest.
 *     - {@code extra} can be any additional URI information, or nothing; this
 *       information is made available to the Interactor for its use in
 *       processing the request.
 *
 * Fiz will automatically load the class and create a single instance
 * during the first request whose URI references the class.
 */

public class Interactor {

    /**
     * This method is invoked exactly once, just before the first
     * request on this class is invoked.  Subclasses can override
     * this method to obtain a hook for initialization, such as
     * reading configuration information, etc.
     */
    public void init() {
    }

    /**
     * This method is invoked when the Fiz Dispatcher is shut down.
     * Subclasses can override this method if they need to perform cleanups
     * such as closing files and external connections.
     */
    public void destroy() {
    }

    /**
     * This method is invoked by the dispatcher at the beginning of handling
     * each request to create a ClientRequest object for the request.
     * Applications can override this method in their Interactor subclasses,
     * returning a subclass of ClientRequest holding additional
     * application-specific nformation.
     * @param servlet              Servlet under which this request is running.
     * @param servletRequest       The HttpServletRequest provided by the
     *                             container.
     * @param servletResponse      The HttpServletResponse provided by the
     *                             container.
     * @return                     A ClientRequest object to use for managing
     *                             the current request.
     */
    public ClientRequest getRequest(HttpServlet servlet,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        return new ClientRequest(servlet, servletRequest, servletResponse);
    }
}
