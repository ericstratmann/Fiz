package org.fiz;

import java.lang.reflect.*;
import java.util.*;

/**
 * Note: this class doesn't seem useful anymore, and is a candidate
 * for deletion.
 * MethodDataManager allows arbitrary methods in arbitrary classes
 * to be invoked to handle DataRequests.  This provides a lightweight
 * way to make a variety of data available through the DataRequest
 * mechanism.  Note: this class does not currently support cancellation
 * of requests.
 * <p>
 * MethodDataManager does not use any information in the configuration
 * dataset for the data manager (though {@code class} is used by
 * other methods to find this class).  It supports the following
 * fields in DataRequests:
 *   class:     Name of a Java class accessible to Fiz.  This class
 *              must have a no-argument constructor.  One instance of
 *              this class will be created when needed and shared by all
 *              DataRequests that specify the class.  However, if
 *              {@code method} is a static method then no class instance
 *              is needed or created.
 *   method:    Name of a method in {@code class}, which will be
 *              invoked when the request is started.  The method must
 *              take a single argument of type DataRequest.  The method
 *              must behave in the same way as the {@code startRequests}
 *              method of a DataManager.  In particular, it must eventually
 *              invoke {@code setComplete} or {@code setError} on the
 *              DataRequest.
 * Additional fields may be set in DataRequests to provide parameters
 * to {@code method}.
 */
public class MethodDataManager extends DataManager {
    // There exists one object of the following type for each distinct
    // class that has been named in a DataRequest.
    protected class ClassInfo {
        public Class<?> cl;        // Java reflection information about
                                   // the class.
        public HashMap<String,Method> methodMap;
                                   // Maps from a method name to a Java
                                   // reflection structure for that method;
                                   // one entry for each method encountered
                                   // so far for this class.
        public Object instance;    // An instance of this class, used for
                                   // all requests that invoke non-static
                                   // methods.
    }

    // The following table maps from class names to the corresponding
    // ClassInfo objects.
    protected HashMap<String,ClassInfo> classMap
            = new HashMap<String,ClassInfo>();

    // The following class is used to return multiple values from
    // {@code findMethod}.
    protected class MethodInfo {
        public Method method;      // Used to invoke the method.
        public Object instance;    // Object instance; needed to invoke the
                                   // method unless it is static.
    }

    // The following field exists purely to provide a convenient reference
    // to the DataRequest class.
    DataRequest dummyRequest = new DataRequest(new Dataset());

    /**
     * Construct a MethodDataManager.
     */
    public MethodDataManager() {
        // Nothing to do here.
    }

    /**
     * Construct a MethodDataManager from a configuration dataset.  This
     * method is provided because the DataManager expects it.
     * TODO: eliminate the need for this method.
     * @param config               Ignored.
     */
    public MethodDataManager(Dataset config) {
        // Nothing to do here.
    }

    /**
     * This method is invoked by DataRequest.startRequests to process
     * one or more requests for this data manager.  This method finds and
     * invokes the appropriate method for each request; it is up to that
     * method to actually process the request and signal completion.
     * @param requests             DataRequest objects describing the
     *                             requests to be processed.
     */
    @Override
    public void startRequests(Collection<DataRequest> requests) {
        for (DataRequest request : requests) {
            try {
                Dataset parameters = request.getRequestData();
                String className = parameters.check("class");
                if (className == null) {
                    request.setError(new Dataset("culprit", "class",
                            "message", "no class name supplied"));
                    continue;
                }
                String methodName = parameters.check("method");
                if (methodName == null) {
                    request.setError(new Dataset("culprit", "method",
                            "message", "no method name supplied"));
                    continue;
                }
                MethodInfo info = findMethod(className, methodName);
                try {
                    info.method.invoke(info.instance, request);
                }
                catch (Throwable e) {
                    // If the exception happened in the target method, the
                    // real information we want is encapsulated inside e.
                    Throwable cause =  e.getCause();
                    if (cause == null) {
                        cause = e;
                    }
                    request.setError(new Dataset("message",
                            "uncaught exception in method \"" + methodName +
                            "\" of class \"" + className + "\": " +
                            cause.getMessage()));
                }
            }
            catch (DatasetError e) {
                request.setError(e.getErrorData());
            }
        }
    }

    /**
     * Lookup information needed to invoke a method.  Once this information
     * is found it is saved in a cache to speed up future lookups.
     * @param className            Name of the class containing the method.
     * @param methodName           Name of the desired method; may be static
     *                             or not.
     * @return                     Information that can be used to invoke
     *                             the method.
     */
    protected synchronized MethodInfo findMethod(String className,
            String methodName) {
        // First find information about the class.
        ClassInfo classInfo = classMap.get(className);
        if (classInfo == null) {
            classInfo = new ClassInfo();
            classInfo.cl = Util.findClass(className);
            if (classInfo.cl == null) {
                throw new DatasetError(new Dataset("culprit", "class",
                        "message", "can't find class \"" + className + "\""));
            }
            classInfo.methodMap = new HashMap<String,Method>();
            classInfo.instance = null;
            classMap.put(className, classInfo);
        }

        // Lookup the method.
        Method method = classInfo.methodMap.get(methodName);
        if (method == null) {
            try {
                method = classInfo.cl.getMethod(methodName,
                        dummyRequest.getClass());
            }
            catch (Throwable t) {
                throw new DatasetError(new Dataset("culprit", "method",
                        "message", "can't find method \"" + methodName +
                        "\" in class \"" + className + "\""));
            }
            classInfo.methodMap.put(methodName, method);
        }

        // If the method isn't static, make sure we have an instance of the
        // class.
        if (((method.getModifiers() & Modifier.STATIC) == 0)
                && (classInfo.instance == null)) {
            try {
                classInfo.instance = classInfo.cl.newInstance();
            }
            catch (Throwable t) {
                throw new DatasetError(new Dataset("culprit", "class",
                        "message", "can't create instance of class \"" +
                        className + "\" (is there a no-argument " +
                        "constructor?)"));

            }
        }

        MethodInfo result = new MethodInfo();
        result.method = method;
        result.instance = classInfo.instance;
        return result;
    }
}
