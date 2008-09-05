package org.fiz;
import java.lang.reflect.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * Data managers are responsible for managing the persistent data of a
 * Fiz application, such as information in a relational database system.
 * Each subclass of DataManager implements a particular kind of data
 * storage mechanism.  Interactors and other front-end classes communicate
 * with data managers using DataRequest objects; see the DataRequest class
 * for details on how request parameters are provided to data managers
 * and how data managers respond to requests.
 * <p>
 * The {@code dataManagers} dataset provides configuration information
 * for the data managers of the application, with one child dataset
 * for each data manager.  The name of the child is the name of the data
 * manager and the values within the child specify the configuration for that
 * data manager.  The {@code class} value specifies the class that implements
 * the data manager; any other values are used by the data manager's
 * constructor in a manager-specific fashion.  If there is no {@code class}
 * value, and the class name is generated automatically from the data manager
 * name: if the data manager name is {@code foo}, then the class name will
 * be {@code FooDataManager}.
 * <p>
 * It is possible to use a data manager without creating any configuration
 * information for it: if the class of the data manager is
 * {@code FooDataManager}, it can be referenced with the name "foo"; it
 * will receive an empty dataset as its configuration dataset.
 * <p>
 * Here are some other properties of the DataManager mechanism:
 *   - In addition to the abstract methods declared here, each data manager
 *     must provide a constructor that takes a configuration Dataset as
 *     argument.
 *   - A DataManager can implement its functionality locally, or it can
 *     serve as a front-and for a remote manager.
 *   - The DataManager mechanism supports batching, where the DataManager
 *     receives multiple requests to handle of the same time; this permits
 *     greater efficiency in some cases (e.g., several requests can be sent
 *     to a remote server in a single message).
 *   - Requests may be handled asynchronously.  A data manager is notified
 *     when it should begin processing a batch of requests; it can either
 *     complete the request before it returns, or simply start the processing
 *     of the request and return before has been completed.  When the request
 *     eventually completes, the data manager invokes a method on the
 *     requests to indicate that it is now finished.
 *   - The data manager owns the data: front-end classes retain no persistent
 *     data between client requests.  Caching, if any, is the responsibility
 *     of the data manager.
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
     * If the data manager has cached information locally, this method will
     * delete all such cached information so that it gets reloaded from
     * its ultimate source the next time it is referenced.  If the cache
     * contains modified data that has not been written to the backing
     * storage, it will be written as part of the flush operation.
     */
    public void flush() {
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
        Dataset config = Config.getDataset("dataManagers").checkChild(name);
        if (config == null) {
            config = new Dataset();
        }
        String dmClass = config.check("class");
        if (dmClass == null) {
            dmClass = StringUtil.ucFirst(name) + "DataManager";
        }
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
