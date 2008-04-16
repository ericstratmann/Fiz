package org.fiz;
import java.lang.reflect.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * Data managers are responsible for managing the persistent data of a
 * Fiz application, such as information in a relational database system.
 * The DataManager interface defines how Interactors and other front-end
 * classes communicate with the back-end data managers.  A request to a
 * data manager consists of a dataset containing input arguments.  In the
 * most common case this is just a collection of name-value pairs, but
 * it is also possible to pass in nested datasets containing complex
 * data structures.  It is up to the data manager to validate the input
 * dataset.  After the data manager has processed a request, it returns
 * a response dataset; the exact format of the response depends on the
 * data manager and the specific requests.
 * <p>
 * Here are some other properties of the DataManager mechanism:
 *   - A DataManager can implement its functionality locally, or it can
 *     serve as a front-and for a remote manager.
 *   - The DataManager mechanism supports batching, where the DataManager
 *     receives multiple requests to handle of the same time; this permits
 *     greater efficiency in some cases (e.g., several requests can be sent
 *     to a remote server in a single message).
 *   - Requests are assumed to be asynchronous: the data manager is invoked
 *     once to begin processing a batch of requests, and again later to
 *     retrieve the response(s).  This allows multiple data managers to
 *     work in parallel on different requests.
 *   - The DataManager includes a well-defined mechanism for reporting
 *     errors and also for returning advisory messages.
 *   - There is no need to register data managers; if a class has a
 *     name ending in DataManager and implements the DataManager interface,
 *     that it can be referenced in requests; the request mechanism will
 *     dynamically load the relevant class.
 *   - The data manager owns the data: front-end classes retain no
 *     persistent data between client requests except some information
 *     in the session, and even that is often accessed through a data
 *     manager.  Caching, if any, is the responsibility of the data
 *     manager.
 *   - Data managers are responsible for data validation, consistency
 *     checking, and so on.
 */
public abstract class DataManager {
    // The following hash table maps from the string name of a data manager
    // to the corresponding DataManager object.
    protected static HashMap<String,DataManager> cache
            = new HashMap<String,DataManager>();

    // The following variable is used for log4j-based logging.
    protected static Logger logger = Logger.getLogger("org.fiz.DataManager");

    // Name used to create this data manager.
    protected String name;

    /**
     * This method is invoked by DataRequest.start to initiate the
     * processing of one or more requests for a particular DataManager
     * (under normal conditions no-one but DataRequest.start should
     * invoke this method; everyone else should invoke DataRequest.start).
     * There is no guarantee that any of the requests have been completed
     * at the time this method returns, though the method may choose to
     * process some or all of the requests synchronously before it returns.
     * When a request completes, either before this method returns or
     * after, this data manager will invoke a method on the request to
     * indicate completion.
     * @param requests             Each element of this collection specifies
     *                             one request to be served by this
     *                             DataManager.
     */
    public abstract void startRequests(Collection<DataRequest> requests);

    /**
     * This method is invoked to request that the data manager cancel a
     * particular request if it has not already been completed.  If the
     * request has been completed, or if the data manager has no record of
     * this request, or if the data manager does not implement request
     * cancellation, then this invocation will have no effect.  Normally
     * the data manager will cancel the request and then invoke
     * setError on the request to indicate that it has been canceled.
     * @param request              The request to cancel.
     */
    public void cancelRequest(DataRequest request) {
    }

    /**
     * This method is invoked to shut down the DataManager; this happens
     * when DataManager.destroyAll is invoked, which is typically when the
     * servlet is about to be unloaded.  This provides the data manager
     * an opportunity to do internal cleanup, such as canceling all
     * outstanding requests.
     */
    public void destroy() {
    }

    public String toString() {
        if (name != null) {
            return name + " data manager";
        }
        return "unnamed data manager";
    }

    /**
     * Maps from the name of a data manager to a DataManager object
     * that can be used to issue requests.  Caches these mappings to
     * avoid duplicate DataManagers and to make calls go faster in the
     * future .
     * @param name                 Name of the desired data manager.
     * @return                     DataManager object corresponding to
     *                             {@code name}.
     */
    public synchronized static DataManager getDataManager(String name) {
        // See if we have already used this data manager before.
        DataManager manager = cache.get(name);
        if (manager != null) {
            return manager;
        }

        // We haven't seen this name before.  First, find the configuration
        // info that describes the data manager, then extract the name of the
        // DataManager class and instantiate it.
        Dataset config = Config.getDataset("dataManagers").getChild(name);
        String dmClass = config.get("class");
        manager = (DataManager) Util.newInstance(dmClass,
                "org.fiz.DataManager", config);
        manager.name = name;
        cache.put(name, manager);
        logger.info("loaded DataManager " + manager.getClass().getName());
        return manager;
    }

    /**
     * Shut down all existing data managers and clear the internal cache
     * that maps from names to data managers.
     */
    public synchronized static void destroyAll() {
        for (DataManager manager : cache.values()) {
            logger.info("destroying DataManager "
                    + manager.getClass().getName());
            manager.destroy();
        }
        cache.clear();
    }
}
