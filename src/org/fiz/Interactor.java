package org.fiz;
import javax.servlet.http.*;

/**
 * The Interactor class is subclassed by classes that process
 * incoming HTTP requests.  Fiz automatically dispatches incoming
 * requests to methods of an Interactor subclass based on the URL.  For
 * example, if a class MyClassInteractor subclasses Interactor and has methods
 * {@code create} and {@code delete}, than the {@code create} method will be
 * invoked to service URLs of the form
 * {@code /appName/fiz/myClass/create/extra}:
 *     - {@code appName} is a URL prefix identifying this particular
 *       application; it can have any number of path levels, including 0.
 *       Tomcat must have been configured to pass all requests for this
 *       prefix to the Fiz Dispatcher servlet.
 *     - {@code myClass} selects the class to service the request: the
 *       className is computed by upper-casing the first letter and adding
 *       a suffix of {@code Interactor}.  The class must be a subclass of
 *       Interactor.
 *     - {@code create} is the name of the method to invoke in
 *       {@code MyClassInteractor}; this method must be public and take
 *       1 argument consisting of a ClientRequest.
 *     - {@code extra} can be any additional URL information, or nothing; this
 *       information is made available to the Interactor for its use in
 *       processing the request.
 *
 * Fiz will automatically load the class and create a single instance
 * during the first request whose URL references the class.
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
