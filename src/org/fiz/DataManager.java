package org.fiz;

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
public interface DataManager {
}
