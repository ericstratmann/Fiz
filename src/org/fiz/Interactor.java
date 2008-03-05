/**
 * The Interactor class is subclassed by classes that process
 * incoming HTTP requests.  Fiz automatically dispatches incoming
 * requests to methods of an Interactor subclass based on the URI.  For
 * example, if a class MyClass subclasses Interactor and has methods
 * create and delete, than the create method will be invoked to service
 * URIs of the form /appName/myClass/create/extra:
 *     - "appName" is a URI prefix identifying this particular application;
 *       it can have any number of path levels, including 0.  Tomcat must
 *       have been configured to pass all requests for this prefix to the
 *       Fiz Dispatcher servlet.
 *     - "myClass" is the name of the class, except with a lower-case
 *       initial letter; this class must be a subclass of Interactor.
 *     - "create" is the name of the method to invoke on an object of
 *       type myClass; this method must be public and take 2 arguments
 *       consisting of an HttpServletRequest and an HttpServletResponse.
 *     - "extra" can be any additional URI information, or nothing; this
 *       information is made available to the Interactor for its use in
 *       processing the request.
 *
 * Fiz will automatically load the class and create a single instance
 * during the first request whose URI references the class.
 */

package org.fiz;

import javax.servlet.http.*;

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
     * each request to create a Request object for the request.  Applications
     * can override this method in their Interactor subclasses, returning
     * a subclass of Request holding additional application-specific
     * information.
     * @param servletRequest       The HttpServletRequest provided by the
     *                             container.
     * @param servletResponse      The HttpServletResponse provided by the
     *                             container.
     * @return                     A Request object to use for managing
     *                             the current request.
     */
    public Request getRequest(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        return new Request(servletRequest, servletResponse);
    }
}
