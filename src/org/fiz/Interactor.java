package org.fiz;
import javax.servlet.http.*;

/**
 * The Interactor class is subclassed by classes that process
 * incoming HTTP requests.  Fiz automatically dispatches incoming
 * requests to methods of an Interactor subclass based on the URL.
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
