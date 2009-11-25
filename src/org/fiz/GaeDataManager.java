/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz;

import java.util.*;
import org.apache.log4j.Logger;
import com.google.appengine.api.datastore.*;

/**
 * GaeDataManager provides access to Google AppEngine's Datastore via its 
 * low-level API.  Unlike traditional relational database schemas, the Datastore
 * uses a single large table with varying row types.  Each row, an Entity, can 
 * have varying "columns" (referred to as "properties") with varying values - 
 * although rows have types (kinds), that typing does not place any restriction 
 * on what properties or values an Entity can or cannot have.  In 
 * GaeDataManager, Entities are presented to the developer as Datasets - an 
 * Entity property-value pair is encapsulated as a Dataset key-value pair.
 * 
 * Although there are no separate tables in Datastore, Entities are grouped both
 * by their kind and by their ancestry.  Every Entity has a pointer to an 
 * optional parent Entity, creating a tree-like organization of Entities.  
 * Entities with the same root (a parent-less ancestor) are all stored near each
 * other within the Datastore, which makes queries by common ancestor very
 * efficient.
 * 
 * Each Entity in the Datastore has an associated Key.  Keys uniquely 
 * identify Entities, and encapsulate the Entity's kind, a unique ID, and an 
 * optional pointer to a parent Key.  In GaeDataManager, Keys are often 
 * returned to the developer and are expected as arguments to many functions. 
 * 
 * While Datasets support values of arbitrary classes, the Datastore is more 
 * restrictive.  See {@link http://code.google.com/appengine/docs/java/datastore
 * /dataclasses.html#Core_Value_Types} for a list of approved Datastore Objects.
 * Datastore also supports collections of these Objects - as in Datasets, 
 * Entities support the notion of multiple values associated with a key or 
 * property.
 * 
 * GaeDataManager exports a number of Datastore operations found below, most 
 * of which fall into one of five categories:
 * - Find By Key:       given a list of Keys, retrieve their associated records.
 * - Insert:            given a Dataset, convert it to an Entity and store it.
 * - Delete:            given a Key, delete the record associated with that Key.
 * - Find By Query:     given a series of filters and sorts, return a list of 
 *                      records that satisfy the parameters.  Queries can only 
 *                      be performed on Entities of the same kind.
 * - Find By Ancestor:  given an ancestor Key, return all records who have that
 *                      Key as an ancestor.  Entities and their ancestors do 
 *                      not necessarily have the same kind.
 *
 */
public class GaeDataManager {

    /* The Dataset key in which an Entity's Key is placed */
    public static final String KEY_PROPERTY = "_key_";
    
    /* The following variable is used for log4j-based logging */
    protected static Logger logger = Logger.getLogger(
            "org.fiz.DatastoreDataManager");
    
    /* Used for specifying messages for common Datastore-related errors */
    protected static enum ErrType {
        DS_FAILURE,
        TXN_INACTIVE,
        BAD_ARG
    }
    
    /* The handle on the Datastore used by this DataManager. */
    protected DatastoreService datastore;
    
    /**
     * Constructs a new GaeDataManager, acquiring a connection to the 
     * Datastore.
     * 
     */
    public GaeDataManager() {
        datastore = DatastoreServiceFactory.getDatastoreService();
        logger.info("DatastoreService instance successfully created.");
    }
    
    /**
     * Returns a new Transaction on the Datastore.  That Transaction is 
     * considered the current Transaction (and is used implicitly for all 
     * subsequent Datastore actions) until a new Transaction is created, or
     * until the Transaction is committed or rolled back.  Note: all actions 
     * taken under a Transaction must act only on Entities with the same 
     * root ancestor.
     * 
     * @return          A new, current Transaction.
     */
    public Transaction getNewTransaction() {
        return datastore.beginTransaction();
    }
    
    /**
     * Given the Key of a parent Entity and an Entity kind, returns an array of 
     * new Keys that can be used for inserting Entities of that kind and 
     * ancestry.  
     * 
     * @param parent    The parent Key for the newly allocated Keys.
     * @param kind      The kind of the newly allocated Keys.
     * @param nKeys     The number of new Keys to allocate.
     * @return          A Key[] containing the newly allocated Keys.
     */
    public Key[] allocateKeys(Key parent, String kind, int nKeys) {
        return convertKeyRange(datastore.allocateIds(parent, kind, nKeys));
    }

    /**
     * Given an Entity kind, returns an array of new Keys that can be used for 
     * inserting Entities of that kind.  
     * 
     * @param kind      The kind of the newly allocated Keys.
     * @param nKeys     The number of new Keys to allocate.
     * @return          A Key[] containing the newly allocated Keys.
     */
    public Key[] allocateKeys(String kind, int nKeys) {
        return convertKeyRange(datastore.allocateIds(kind, nKeys));
    }
    
    /**
     * Converts a given KeyRange into a more easily usable Key[].
     * 
     * @param range     The KeyRange to convert.
     * @return          A Key[] with the contents of the KeyRange.
     */
    protected Key[] convertKeyRange(KeyRange range) {
        Iterator<Key> rangeIter = range.iterator();
        Key[] result = new Key[(int)range.getSize()];
        int counter = 0;
        while (rangeIter.hasNext()) {
            result[counter++] = rangeIter.next();
        }
        return result;
    }
    
    /* DATAREQUEST SUBCLASSES */
    /**
     * An abstract superclass for the four Datastore request classes 
     * (GetRequest, InsertRequest, DeleteRequest, QueryRequest).  All actions 
     * on the Datastore are ultimately converted into one of these four 
     * requests.  The superclass contains fields shared by all four subclasses,
     * specifically the GaeDataManager instance and the specified Transaction. 
     * As DataRequests, the results of all four queries are saved via a call 
     * to DataRequest.setComplete(Dataset).
     */
    protected static abstract class DatastoreRequest extends DataRequest 
            implements Runnable {
        /* The GaeDataManager class containing the DatastoreService instance. */
        protected GaeDataManager manager;
        /* An optional Transaction used explicitly by this Datastore 
         * operation. */
        protected Transaction txn;
        
        /**
         * Constructs a new DatastoreRequest, caching the specified 
         * GaeDataManager and Transaction.
         * 
         * @param manager       The GaeDataManager running this Datastore 
         *                      operation.
         * @param txn           The Transaction with which to execture this 
         *                      Datastore operation.
         */
        protected DatastoreRequest(GaeDataManager manager, 
                Transaction txn) {
            this.manager = manager;
            this.txn = txn;
        }
    }
    
    /**
     * Encapsulates a get operation on the Datastore.  Given the current 
     * manager, a Transaction to use, whether or not the return format should 
     * map Keys to Entities, and the Keys whose Entities we wish to retrieve, 
     * makes the necessary Datastore calls to perform the get and saves the 
     * resulting Dataset of records.
     */
    protected static class GetRequest extends DatastoreRequest {
        /* Whether or not the request should store its results mapped by 
         * the key "record" or by the result's String-ified Key. */
        protected boolean keyMapping;
        /* The Keys whose Entities we should get with this request. */
        protected Key[] keys;
        
        /**
         * Constructs a new GetRequest, caching the specified parameters.
         * 
         * @param manager       The GaeDataManager running this Get.
         * @param txn           The Transaction with which to execute this Get.
         * @param keyMapping    Whether or not the request should store its 
         *                      results mapped by String-ified Key.
         * @param keys          The Keys whose Entities we should retrieve.
         */
        public GetRequest(GaeDataManager manager, Transaction txn, 
                boolean keyMapping, Key ... keys) {
            super(manager, txn);
            this.keyMapping = keyMapping;
            this.keys = keys;
        }
        
        /**
         * Executes the Get operation with the parameters specified in the 
         * constructor.  Executes a different version of DatastoreService.get() 
         * depending on how many Keys were specified and whether or not the 
         * Transaction is null.  Stores the retrieved Entities (as Datasets) as 
         * the result of the DataRequest. 
         */
        public void run() {
            boolean nullTxn = (txn == null);
            try {
                if (keys.length == 1) { // If only one Key was specified.
                    Entity entity = nullTxn ? 
                            manager.datastore.get(keys[0]) : 
                            manager.datastore.get(txn, keys[0]);
                    setComplete(entityToDataset(entity));
                } else { // If more than one Key was specified.
                    Iterable<Key> iter = Arrays.asList(keys);
                    Map<Key, Entity> entities = nullTxn ? 
                            manager.datastore.get(iter) : 
                            manager.datastore.get(txn, iter);
                    Dataset result = new Dataset();
                    // Store each Entity record into the result Dataset, mapping
                    // the Entity either by "record" or by its String-ified Key.
                    for (Map.Entry<Key, Entity> entity : entities.entrySet()) {
                        String key = keyMapping ? keyToString(entity.getKey()) :
                          "record";
                        result.add(key, entityToDataset(entity.getValue()));
                    }
                    setComplete(result);
                }
            } catch (EntityNotFoundException e) {
                // Thrown if the single-Key Datastore get failed to return a 
                // result.  In this case, return an empty Dataset as the result.
                setComplete();
            } catch (IllegalArgumentException e) {
                error(ErrType.BAD_ARG, "Key", e);
            } catch (IllegalStateException e) {
                error(ErrType.TXN_INACTIVE, "", e);
            } catch (DatastoreFailureException e) {
                error(ErrType.DS_FAILURE, "", e);
            }
        }
    }
    
    /**
     * Encapsulates an insert operation on the Datastore.  Given the current 
     * manager, a Transaction to use, and the Entities to insert, makes the 
     * necessary Datastore calls to insert the Entities and saves the Keys of 
     * the newly inserted records.
     */
    protected static class InsertRequest extends DatastoreRequest {
        /* The Entities to insert with this request. */
        protected Entity[] entities;
        
        /**
         * Constructs a new InsertRequest, caching the specified parameters.
         * 
         * @param manager       The GaeDataManager running this Insert.
         * @param txn           The Transaction with which to execute this 
         *                      Insert.
         * @param entities      The Entities to insert with this request.
         */
        public InsertRequest(GaeDataManager manager, Transaction txn, 
                Entity ... entities) {
            super(manager, txn);
            this.entities = entities;
        }
        
        /**
         * Executes the Insert operation with the parameters specified in the 
         * constructor.  Calls a different version of DatastoreService.insert() 
         * depending on the number of Entities specified and whether or not the 
         * Transaction is null.  Stores the Keys of the inserted Entities as the
         * result of the DataReqeust. 
         */
        public void run() {
            try {
                if (entities.length == 1) { // If only one Entity was specified.
                    Key key = (txn == null) ?
                            manager.datastore.put(entities[0]) :
                            manager.datastore.put(txn, entities[0]);
                    // Save the Datastore Key for the inserted Entity.
                    setComplete(new Dataset("record", key));
                } else { // If more than one Entity was specified.
                    Iterable<Entity> iter = Arrays.asList(entities);
                    Iterable<Key> keys = (txn == null) ?
                            manager.datastore.put(iter) :
                            manager.datastore.put(txn, iter);
                    Dataset records = new Dataset();
                    // Save the Datastore Keys for the inserted Entities.
                    for (Key key : keys) {
                        records.add("record", key);
                    }
                    setComplete(records);
                }
            } catch (IllegalArgumentException e) {
                error(ErrType.BAD_ARG, "Entity", e);
            } catch (IllegalStateException e) {
                error(ErrType.TXN_INACTIVE, "", e);
            } catch (DatastoreFailureException e) {
                error(ErrType.DS_FAILURE, "", e);
            }
        }
    }

    /**
     * Encapsulates a delete operation on the Datastore.  Given the current 
     * manager, a Transaction to use, and the Keys of Entities to delete, makes 
     * the necessary Datastore calls to delete the Entities.
     *
     */
    protected static class DeleteRequest extends DatastoreRequest {
        /* The Keys of the Entities to delete with this request. */
        protected Key[] keys;
        
        /**
         * Constructs a new DeleteRequest, caching the specified parameters.
         * 
         * @param manager       The GaeDataManager running this Delete.
         * @param txn           The Transaction with which to execute this 
         *                      Delete.
         * @param keys          The Keys of the Entities to delete with this 
         *                      request.
         */
        public DeleteRequest(GaeDataManager manager, Transaction txn, 
                Key ... keys) {
            super(manager, txn);
            this.keys = keys;
        }
        
        /**
         * Executes the Delete operation, calling a different version of 
         * DatastoreService.delete() depending on whether or not the Transaction
         * is null.  Stores nothing as the result of the DataRequest - if any 
         * error occurred, it will be reflected as a thrown DatastoreError.
         */
        public void run() {
            try {
                // Perform the delete with the Transaction if it isn't null.
                if (txn == null) {
                    manager.datastore.delete(keys);
                } else {
                    manager.datastore.delete(txn, keys);
                }
                setComplete();
            } catch (IllegalArgumentException e) {
                error(ErrType.BAD_ARG, "Key", e);
            } catch (IllegalStateException e) {
                error(ErrType.TXN_INACTIVE, "", e);
            } catch (DatastoreFailureException e) {
                error(ErrType.DS_FAILURE, "", e);
            }
        }
    }
    
    /**
     * Encapsulates a query operation on the Datastore.  Given the current 
     * manager, a Transaction to use, result formatting parameters, and the 
     * Query to perform, makes the necessary Datastore calls to perform the 
     * Query and saves the resulting Dataset of records.
     */
    protected static class QueryRequest extends DatastoreRequest {
        /* Whether or not the request should store its results mapped by 
         * the key "record" or by the result's String-ified Key. */
        protected boolean keyMapping;
        /* Whether or not the request should retrieve only the Keys of the 
         * Entities matching the query. */
        protected boolean keysOnly;
        /* The Datastore Query to execute. */
        protected Query query;
        
        /**
         * Constructs a new QueryRequest, caching the specified parameters.
         * 
         * @param manager       The GaeDataManager running this Query.
         * @param txn           The Transaction with which to execute this 
         *                      Query.
         * @param keyMapping    Whether or not the request should store its 
         *                      results mapped by String-ified Key.
         * @param keysOnly      Whether or not the request should retrieve only 
         *                      the Keys of the Entities matching the query.
         * @param query         The Datastore Query to execture.
         */
        public QueryRequest(GaeDataManager manager, Transaction txn, 
                boolean keyMapping, boolean keysOnly, Query query) {
            super(manager, txn);
            this.keyMapping = keyMapping;
            this.keysOnly = keysOnly;
            this.query = query;
            // If the Query is meant to only retrieve keys, set this flag on 
            // the query to optimize the process.
            if (keysOnly) {
                query.setKeysOnly();
            }
        }
        
        /**
         * Executes the Query with the cached parameters.  Calls a different 
         * version of DatastoreService.prepare() depending on whether or not the
         * Transaction is null.  Stores the results of the Query in varying 
         * formats, depending on constructor-specified parameters.
         */
        public void run() {
            try {
                // Prepare and execute the query.
                PreparedQuery pq = (txn == null) ? 
                        manager.datastore.prepare(query) :
                        manager.datastore.prepare(txn, query);
                Dataset results = new Dataset();
                for (Entity record : pq.asIterable()) {
                    Key key = record.getKey();
                    // For each record, store it in the Dataset:
                    // - mapped by either "record" or its String-ified Key.
                    // - as a Dataset (the entire Entity) or as just its Key.
                    String attrName = keyMapping ? keyToString(key) : "record";
                    Object attrVal = keysOnly ? key : entityToDataset(record);
                    results.add(attrName, attrVal);
                }
                setComplete(results);
            } catch (IllegalStateException e) {
                error(ErrType.TXN_INACTIVE, "", e);
            } catch (IllegalArgumentException e) {
                error(ErrType.BAD_ARG, "QueryArgument", e);
            } catch (DatastoreFailureException e) {
                error(ErrType.DS_FAILURE, "", e);
            }
        }
    }
    
    /**
     * An interface for the two classes used to specify filters and sorts on 
     * Datastore queries.  Specifies that these classes must know how to add 
     * whatever query restriction they encapsulate to a given Query object.
     */
    public static interface QueryAttribute {
        /**
         * Adds this QueryAttribute as a filter or sort on the specified Query.
         * 
         * @param query         The Query to filter or sort.
         */
        public void addToQuery(Query query);
    }
    
    /**
     * Encapsulates a Query term or filter.  Given an attribute name, one of 
     * the five supported Datastore comparison tests, and an Object with which 
     * to compare, adds that restriction to the Query in which this QueryTerm 
     * is used.  The following Query restrictions apply:
     * 
     * - Only one attribute per query may have any inequality tests.  However, 
     * that attribute may have more than one inequality test placed on it.
     * - When testing on an attribute with more than one value, the test will 
     * pass if any one of the values satisfies the constraint.
     * - Collections are not supported as comparison attribute values - only a 
     * single Object of a Datastore-supported type is acceptable.  See {@link 
     * http://code.google.com/appengine/docs/java/datastore/dataclasses.html
     * #Core_Value_Types} for a list of supported types.
     */
    public static class QueryTerm implements QueryAttribute {
        /* The name of the Entity property that this QueryTerm should apply 
         * to. */
        protected String attrName;
        /* The Object with which to compare the value of the specified Entity 
         * property. */
        protected Object attrValue;
        /* The operator to use when comparing the specified value to the Entity 
         * values (one of: =, <, >, <=, >=). */
        protected Query.FilterOperator operator;
        
        /**
         * Constructs a new QueryTerm, caching the name of the Entity property 
         * to filter and the value of the property with which to compare.  Also 
         * converts the specified String operand to one of the five supported 
         * Datastore Query filter operators.  If one of the five operands is 
         * not detected, throws a DatastoreError.
         * 
         * @param attrName      The name of the Entity property that this 
         *                      QueryTerm should apply to.
         * @param op            The operator to use for comparing Entity values.
         * @param attrValue     The Object with which to compare the value of 
         *                      the specified Entity property.
         */
        public QueryTerm(String attrName, String op, Object attrValue) {
            this.attrName = attrName;
            this.attrValue = attrValue;
            
            if (op.equals("=")) {
                operator = Query.FilterOperator.EQUAL;
            } else if (op.equals("<")) {
                operator = Query.FilterOperator.LESS_THAN;
            } else if (op.equals(">")) {
                operator = Query.FilterOperator.GREATER_THAN;
            } else if (op.equals("<=")) {
                operator = Query.FilterOperator.LESS_THAN_OR_EQUAL;
            } else if (op.equals(">=")) {
                operator = Query.FilterOperator.GREATER_THAN_OR_EQUAL;
            } else {
                // Throw an error if the operator is unexpected.
                error(ErrType.BAD_ARG, "QueryTerm operator (" + op + ")", 
                        new IllegalArgumentException("Operator must be one " +
                                "of: {=, <, >, <=, >=}."));
            }
        }

        /**
         * Adds this QueryTerm as a filter on the specified Query.
         * 
         * @param query         The Query to filter.
         */
        public void addToQuery(Query query) {
            query.addFilter(attrName, operator, attrValue);
        }
    }
    
    /**
     * Encapsulates a Query sort.  Given an attribute name and an optional 
     * specification of sort direction (ascending by default), adds that sort 
     * to the Query in which this QuerySort is used.  The following Query 
     * restrictions apply:
     * 
     * - If a column has an inequality filter on it, that column must be sorted
     * before any other sorts can be placed on that query.  The ordering is 
     * important as well - if column A has an inequality filter and you wish to 
     * sort on column B, you must specify (in this order): filter A, sort A, 
     * sort B.
     * - Sorts are applied in the order in which they are specified.  For 
     * example, specifying a sort on A and then on B will first sort on A, and 
     * wherever two A values are equal, the sort on B will be used to determine 
     * the relative ordering.
     * - An ascending sort on a attribute with multiple values will sort using 
     * the lowest-valued Entity.  A descending sort will use the highest-
     * valued Entity.
     * - If a sorted attribute has values of multiple types (classes), Entities 
     * with values of the same type will be grouped together and sorted in the 
     * result; however, the relative ordering of the types is unspecified.  
     */
    public static class QuerySort implements QueryAttribute {
        /* The name of the Entity property that should be sorted. */
        protected String attrName;
        /* The direction in which to sort (ascending or descending). */
        protected Query.SortDirection direction;
        
        /**
         * Constructs a new ascending QuerySort on the specified Entity 
         * property.
         * 
         * @param attrName      The Entity property to sort.
         */
        public QuerySort(String attrName) {
            this.attrName = attrName;
            this.direction = Query.SortDirection.ASCENDING;
        }
        
        /**
         * Constructs a new QuerySort on the specified Entity property, with the
         * specified sort direction.
         * 
         * @param attrName      The Entity property to sort.
         * @param dir           A String representation of the direction in 
         *                      which to sort.  If (@code dir} cannot be parsed 
         *                      as either ascending or descending, a 
         *                      DatastoreError is thrown.
         */
        public QuerySort(String attrName, String dir) {
            this.attrName = attrName;
            
            if (dir.equalsIgnoreCase("ascending") 
                    || dir.equalsIgnoreCase("asc")) {
                direction = Query.SortDirection.ASCENDING;
            } else if (dir.equalsIgnoreCase("descending") 
                    || dir.equalsIgnoreCase("desc")) {
                direction = Query.SortDirection.DESCENDING;
            } else {
                // Throw an error if the sort direction is unexpected.
                error(ErrType.BAD_ARG, "QuerySort direction (" + dir + ")", 
                        new IllegalArgumentException("Direction must be (case" +
                        		" insensitive) one of: {ascending, asc, " +
                        		"descending, desc}."));                
            }
        }
        
        /**
         * Adds this QuerySort as a sort on the specified Query.
         * 
         * @param query         The Query to sort.
         */
        public void addToQuery(Query query) {
            query.addSort(attrName, direction);
        }
    }
    
    
    /* FIND BY KEY */
    /**
     * Finds the records with the specified Keys.
     * 
     * @param keys      The Keys of the records to find.
     * @return          A Dataset containing the found records.  Records are 
     *                  mapped by the attribute "record".  No error is thrown 
     *                  if a Key did not match a record.
     */
    public Dataset findByKey(Key ... keys) {
        return handleFindKey(null, false, keys);
    }
    
    /**
     * Finds the records with the specified Keys.
     * 
     * @param txn       The Transaction under which to perform the find.
     * @param keys      The Keys of the records to find.
     * @return          A Dataset containing the found records.  Records are 
     *                  mapped by the attribute "record".  No error is thrown 
     *                  if a Key did not match a record.
     */
    public Dataset findByKey(Transaction txn, Key ... keys) {
        return handleFindKey(txn, false, keys);
    }
    
    /* FIND MAPPING BY KEY */
    /**
     * Finds the records with the specified Keys, mapped by their String-ified 
     * Key.
     * 
     * @param keys      The Keys of the records to find.
     * @return          A Dataset containing the found records.  Records are 
     *                  mapped by their String-ified Key.  No error is thrown 
     *                  if a Key did not match a record.
     */
    public Dataset findMappingByKey(Key ... keys) {
        return handleFindKey(null, true, keys);
    }
    
    /**
     * Finds the records with the specified Keys, mapped by their String-ified 
     * Key.
     * 
     * @param txn       The Transaction under which to perform the find.
     * @param keys      The Keys of the records to find.
     * @return          A Dataset containing the found records.  Records are 
     *                  mapped by their String-ified Key.  No error is thrown 
     *                  if a Key did not match a record.
     */
    public Dataset findMappingByKey(Transaction txn, Key ... keys) {
        return handleFindKey(txn, true, keys);
    }
    
    /**
     * An intermediary method used for finding records by Key.  Leverages 
     * {@code handleNewFindByKeyRequest()}.
     * 
     * @param txn           The Transaction under which to perform the find.
     * @param keyMapping    Whether or not the results should be mapped by 
     *                      String-ified Key.
     * @param keys          The Keys of the records to find.
     * @return              A Dataset containing the found records.  No error 
     *                      is thrown if a Key did not match a record.
     */
    protected Dataset handleFindKey(Transaction txn, boolean keyMapping, 
            Key ... keys) {
        return handleNewFindKeyRequest(txn, keyMapping, 
                keys).getResponseOrAbort();
    }
    
    /* FIND BY KEY - DATA REQUEST */
    /**
     * Creates a new DataRequest that will find the records with the specified 
     * Keys.
     * 
     * @param keys      The Keys of the records to find.
     * @return          A DataRequest that will produce the desired records.  
     *                  Records are mapped by the attribute "record".  No error 
     *                  is thrown if a Key did not match a record.
     */
    public DataRequest newFindByKeyRequest(Key ... keys) {
        return handleNewFindKeyRequest(null, false, keys);
    }
    
    /**
     * Creates a new DataRequest that will find the records with the specified 
     * Keys.
     * 
     * @param txn       The Transaction under which to perform the find.
     * @param keys      The Keys of the records to find.
     * @return          A DataRequest that will produce the desired records.  
     *                  Records are mapped by the attribute "record".  No error 
     *                  is thrown if a Key did not match a record.
     */
    public DataRequest newFindByKeyRequest(Transaction txn, Key ... keys) {
        return handleNewFindKeyRequest(txn, false, keys);
    }
    
    /* FIND MAPPING BY KEY - DATA REQUEST */
    /**
     * Creates a new DataRequest that will find the records with the specified 
     * Keys, mapped by their String-ified Key.
     * 
     * @param keys      The Keys of the records to find.
     * @return          A DataRequest that will produce the desired records.  
     *                  Records are mapped by their String-ified Key.  No error 
     *                  is thrown if a Key did not match a record.
     */
    public DataRequest newFindMappingByKeyRequest(Key ... keys) {
        return handleNewFindKeyRequest(null, true, keys);
    }
    
    /**
     * Creates a new DataRequest that will find the records with the specified 
     * Keys, mapped by their String-ified Key.
     * 
     * @param txn       The Transaction under which to perform the find.
     * @param keys      The Keys of the records to find.
     * @return          A DataRequest that will produce the desired records.  
     *                  Records are mapped by their String-ified Key.  No error 
     *                  is thrown if a Key did not match a record.
     */
    public DataRequest newFindMappingByKeyRequest(Transaction txn, 
            Key ... keys) {
        return handleNewFindKeyRequest(txn, true, keys);
    }

    /**
     * An intermediary method used for finding records by Key.  Creates, runs, 
     * and returns a new GetRequest with the specified parameters.
     * 
     * @param txn           The Transaction under which to perform the find.
     * @param keyMapping    Whether or not the results should be mapped by 
     *                      String-ified Key.
     * @param keys          The Keys of the records to find.
     * @return              A DataRequest that will produce the desired records.
     *                      No error is thrown if a Key did not match a record.
     */
    protected DataRequest handleNewFindKeyRequest(Transaction txn,
            boolean keyMapping, Key ... keys) {
        DatastoreRequest request = new GetRequest(this, txn, keyMapping, keys);
        request.run();
        return request;
    }
    
    
    /* INSERT (single record) */
    /**
     * Inserts the specified Dataset into the Datastore, using the Datastore 
     * Key implicitly located in that Dataset.  
     * 
     * @param dataset       The Dataset to insert.
     * @result              The Key corresponding to the inserted record.
     */
    public Key insert(Dataset dataset) {
        return insert((Transaction)null, dataset);
    }
    
    /**
     * Inserts the specified Dataset into the Datastore, giving it the 
     * specified kind.  
     * 
     * @param kind          The kind (type) of the inserted record.
     * @param dataset       The Dataset to insert.
     * @result              The Key corresponding to the inserted record.
     */
    public Key insert(String kind, Dataset dataset) {
        return insert((Transaction)null, kind, dataset);
    }
    
    /**
     * Inserts the specified Dataset into the Datastore, giving it the 
     * specified kind and making it a descendant of the specified Key.  
     * 
     * @param parentKey     The ancestor Key of the inserted record.
     * @param kind          The kind (type) of the inserted record.
     * @param dataset       The Dataset to insert.
     * @result              The Key corresponding to the inserted record.
     */
    public Key insert(Key parentKey, String kind, Dataset dataset) {
        return insert((Transaction)null, parentKey, kind, dataset);
    }
    
    /**
     * Inserts the specified Dataset into the Datastore, using the Datastore 
     * Key implicitly located in that Dataset.  
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param dataset       The Dataset to insert.
     * @result              The Key corresponding to the inserted record.
     */
    public Key insert(Transaction txn, Dataset dataset) {
        return handleInsert(txn, datasetToEntity(dataset));
    }
    
    /**
     * Inserts the specified Dataset into the Datastore, giving it the 
     * specified kind.  
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param kind          The kind (type) of the inserted record.
     * @param dataset       The Dataset to insert.
     * @result              The Key corresponding to the inserted record.
     */
    public Key insert(Transaction txn, String kind, Dataset dataset) {
        return handleInsert(txn, datasetToEntity(kind, dataset));
    }
    
    /**
     * Inserts the specified Dataset into the Datastore, giving it the 
     * specified kind and making it a descendant of the specified Key.  
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param parentKey     The parent Key of the inserted record.
     * @param kind          The kind (type) of the inserted record.
     * @param dataset       The Dataset to insert.
     * @result              The Key corresponding to the inserted record.
     */
    public Key insert(Transaction txn, Key parentKey, String kind, 
            Dataset dataset) {
        return handleInsert(txn, datasetToEntity(parentKey, kind, dataset));
    }
    
    /**
     * An intermediary method for inserting a single record.  Creates, runs, 
     * and returns the result of an InsertRequest.
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param entity        The Entity to insert.
     * @return              The Key corresponding to the inserted record.
     */
    protected Key handleInsert(Transaction txn, Entity entity) {
        InsertRequest request = new InsertRequest(this, txn, entity);
        request.run();
        return (Key)request.getResponseOrAbort().get("record");
    }
    
    /* INSERT (multiple records) */
    /**
     * Inserts the specified Datasets into the Datastore, using the Datastore 
     * Key implicitly located in those Datasets.  
     * 
     * @param datasets      The Datasets to insert.
     * @result              The Keys corresponding to the inserted records.
     */
    public Key[] insert(Dataset ... datasets) {
        return insert((Transaction)null, datasets);
    }
    
    /**
     * Inserts the specified Datasets into the Datastore, giving them the 
     * specified kind.  
     * 
     * @param kind          The kind (type) of the inserted records.
     * @param datasets      The Datasets to insert.
     * @result              The Keys corresponding to the inserted records.
     */
    public Key[] insert(String kind, Dataset ... datasets) {
        Entity entities[] = new Entity[datasets.length];
        for (int i=0; i<datasets.length; i++) {
            entities[i] = datasetToEntity(kind, datasets[i]);
        }
        return handleInsert(null, entities);
    }
    
    /**
     * Inserts the specified Datasets into the Datastore, giving them the 
     * specified kind and making them descendants of the specified Key.  
     * 
     * @param parentKey     The ancestor Key of the inserted records.
     * @param kind          The kind (type) of the inserted records.
     * @param datasets      The Datasets to insert.
     * @result              The Keys corresponding to the inserted records.
     */
    public Key[] insert(Key parentKey, String kind, Dataset ... datasets) {
        return insert((Transaction)null, parentKey, kind, datasets);
    }
    
    /**
     * Inserts the specified Datasets into the Datastore, using the Datastore 
     * Key implicitly located in those Datasets.  
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param datasets      The Datasets to insert.
     * @result              The Keys corresponding to the inserted records.
     */
    public Key[] insert(Transaction txn, Dataset ... datasets) {
        Entity entities[] = new Entity[datasets.length];
        for (int i=0; i<datasets.length; i++) {
            entities[i] = datasetToEntity(datasets[i]);
        }
        return handleInsert(txn, entities);
    }
    
    /**
     * Inserts the specified Datasets into the Datastore, giving them the 
     * specified kind and making them descendants of the specified Key.  
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param parentKey     The parent Key of the inserted records.
     * @param kind          The kind (type) of the inserted records.
     * @param datasets      The Datasets to insert.
     * @result              The Keys corresponding to the inserted records.
     */
    public Key[] insert(Transaction txn, Key parentKey, String kind, 
            Dataset ... datasets) {
        Entity entities[] = new Entity[datasets.length];
        for (int i=0; i<datasets.length; i++) {
            entities[i] = datasetToEntity(parentKey, kind, datasets[i]);
        }
        return handleInsert(txn, entities);
    }
    
    /**
     * An intermediary method for inserting multiple records.  Creates, runs, 
     * and returns the result of an InsertRequest.
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param entities      The Entities to insert.
     * @return              The Keys corresponding to the inserted records.
     */
    protected Key[] handleInsert(Transaction txn, Entity[] entities) {
        InsertRequest request = new InsertRequest(this, txn, entities);
        request.run();
        return request.getResponseOrAbort().getList("record").toArray(
                new Key[0]);
    }
    
    /* INSERT - DATA REQUEST */
    /**
     * Creates a new DataRequest that will insert the specified Datasets into 
     * the Datastore, using the Datastore Key implicitly located in those 
     * Datasets.  
     * 
     * @param datasets      The Datasets to insert.
     * @result              A DataRequest that will produce the Keys 
     *                      corresponding to the inserted records.
     */
    public DataRequest newInsertRequest(Dataset ... datasets) {
        return newInsertRequest((Transaction)null, datasets);
    }
    
    /**
     * Creates a new DataRequest that will insert the specified Datasets into 
     * the Datastore, giving them the specified kind. 
     *
     * @param kind          The kind (type) of the inserted records.
     * @param datasets      The Datasets to insert.
     * @result              A DataRequest that will produce the Keys 
     *                      corresponding to the inserted records.
     */
    public DataRequest newInsertRequest(String kind, Dataset ... datasets) {
        Entity entities[] = new Entity[datasets.length];
        for (int i=0; i<datasets.length; i++) {
            entities[i] = datasetToEntity(kind, datasets[i]);
        }
        return handleNewInsertRequest(null, entities);
    }
    
    /**
     * Creates a new DataRequest that will insert the specified Datasets into 
     * the Datastore, giving them the specified kind and making them descendants
     * of the specified Key.  
     * 
     * @param parentKey     The parent Key of the inserted records.
     * @param kind          The kind (type) of the inserted records.
     * @param datasets      The Datasets to insert.
     * @result              A DataRequest that will produce the Keys 
     *                      corresponding to the inserted records.
     */
    public DataRequest newInsertRequest(Key parentKey, String kind, 
            Dataset ... datasets) {
        return newInsertRequest(null, parentKey, kind, datasets);
    }
    
    /**
     * Creates a new DataRequest that will insert the specified Datasets into 
     * the Datastore, using the Datastore Key implicitly located in those 
     * Datasets.  
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param datasets      The Datasets to insert.
     * @result              A DataRequest that will produce the Keys 
     *                      corresponding to the inserted records.
     */
    public DataRequest newInsertRequest(Transaction txn, Dataset ... datasets) {
        Entity entities[] = new Entity[datasets.length];
        for (int i=0; i<datasets.length; i++) {
            entities[i] = datasetToEntity(datasets[i]);
        }
        return handleNewInsertRequest(txn, entities);
    }
    
    /**
     * Creates a new DataRequest that will insert the specified Dataset into the
     * Datastore, giving it the specified kind.  NOTE: this method only accepts 
     * one insertion record because, when executing under a Transaction, all 
     * records must have the same ancestor.  This is the only newInsertRequest()
     * instance where, given the inputs, multiple inserted records would 
     * necessarily have different ancestors.
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param kind          The kind (type) of the inserted record.
     * @param dataset       The Dataset to insert.
     * @result              A DataRequest that will produce the Key 
     *                      corresponding to the inserted record.
     */
    public DataRequest newInsertRequest(Transaction txn, String kind, 
            Dataset dataset) {
        return handleNewInsertRequest(txn, datasetToEntity(kind, dataset));
    }
    
    /**
     * Creates a new DataRequest that will insert the specified Datasets into 
     * the Datastore, giving them the specified kind and making them descendants
     * of the specified Key. 
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param parentKey     The parent Key of the inserted records.
     * @param kind          The kind (type) of the inserted records.
     * @param datasets      The Datasets to insert.
     * @result              A DataRequest that will produce the Keys 
     *                      corresponding to the inserted records.
     */
    public DataRequest newInsertRequest(Transaction txn, Key parentKey, 
            String kind, Dataset ... datasets) {
        Entity entities[] = new Entity[datasets.length];
        for (int i=0; i<datasets.length; i++) {
            entities[i] = datasetToEntity(parentKey, kind, datasets[i]);
        }
        return handleNewInsertRequest(txn, entities);
    }
    
    /**
     * An intermediary method for inserting multiple records.  Creates, runs, 
     * and returns a DataRequest containing the result of an InsertRequest.
     * 
     * @param txn           The Transaction under which this insert will take 
     *                      place.
     * @param entities      The Entities to insert.
     * @return              A DataRequest that will produce the Keys 
     *                      corresponding to the inserted records.
     */
    protected DataRequest handleNewInsertRequest(Transaction txn, 
            Entity ... entities) {
        DatastoreRequest request = new InsertRequest(this, txn, entities);
        request.run();
        return request;
    }
    
    
    /* DELETE */
    /**
     * Deletes the records with the specified Keys.
     * 
     * @param keys          The Keys corresponding to the records to delete.
     */
    public void delete(Key ... keys) {
        handleDelete(null, keys);
    }
    
    /**
     * Deletes the records with the specified Keys.
     * 
     * @param txn           The Transaction under which this delete will take 
     *                      place.
     * @param keys          The Keys corresponding to the records to delete.
     */
    public void delete(Transaction txn, Key ... keys) {
        handleDelete(txn, keys);
    }

    /**
     * Creates a new DataRequest that will delete the records with the specified
     * Keys.  The DataRequest will produce no contents.
     * 
     * @param keys          The Keys corresponding to the records to delete.
     * @return              A DataRequest with no contents.
     */
    public DataRequest newDeleteRequest(Key ... keys) {
        return handleDelete(null, keys);
    }

    /**
     * Creates a new DataRequest that will delete the records with the specified
     * Keys.  The DataRequest will produce no contents.
     * 
     * @param txn           The Transaction under which this delete will take 
     *                      place.
     * @param keys          The Keys corresponding to the records to delete.
     * @return              A DataRequest with no contents.
     */
    public DataRequest newDeleteRequest(Transaction txn, Key ... keys) {
        return handleDelete(txn, keys);
    }
    
    /**
     * An intermediary method for deleting records by Key.  Creates, runs, and 
     * returns a new DeleteRequest.
     * 
     * @param txn           The Transaction under which this delete will take 
     *                      place.
     * @param keys          The Keys corresponding to the records to delete.
     * @return              A DataRequest with no contents.
     */
    protected DataRequest handleDelete(Transaction txn, Key ... keys) {
        DeleteRequest request = new DeleteRequest(this, txn, keys);
        request.run();
        return request;
    }
    
    
    /* FIND BY QUERY */
    /**
     * Finds records based on their kind and a list of QueryAttributes.
     * 
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A Dataset containing the records matching the query.
     *                      Records are mapped by the attribute "record".
     */
    public Dataset findByQuery(String kind, QueryAttribute ... attributes) {
        return findByQuery(null, null, kind, attributes);
    }

    /**
     * Finds records based on their ancestor Key, their kind, and a list of 
     * QueryAttributes.
     * 
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A Dataset containing the records matching the query.
     *                      Records are mapped by the attribute "record".
     */
    public Dataset findByQuery(Key ancestorKey, String kind, 
            QueryAttribute ... attributes) {
        return findByQuery(null, ancestorKey, kind, attributes);
    }

    /**
     * Finds records based on their ancestor Key, their kind, and a 
     * list of QueryAttributes.
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A Dataset containing the records matching the query.
     *                      Records are mapped by the attribute "record".
     */
    public Dataset findByQuery(Transaction txn, Key ancestorKey, String kind, 
            QueryAttribute ... attributes) {
        return handleFindQuery(txn, ancestorKey, kind, false, false, 
                attributes);
    }

    /* FIND MAPPING BY QUERY */
    /**
     * Finds records based on their kind and a list of QueryAttributes, 
     * mapping them by their String-ified Key.
     * 
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A Dataset containing the Key-mapped records matching
     *                      the query.
     */
    public Dataset findMappingByQuery(String kind, 
            QueryAttribute ... attributes) {
        return findMappingByQuery(null, null, kind, attributes);
    }

    /**
     * Finds records based on their ancestor Key, their kind, and a list of 
     * QueryAttributes, mapping them by their String-ified Key.
     * 
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A Dataset containing the Key-mapped records matching
     *                      the query.
     */
    public Dataset findMappingByQuery(Key ancestorKey, String kind, 
            QueryAttribute ... attributes) {
        return findMappingByQuery(null, ancestorKey, kind, attributes);
    }

    /**
     * Finds records based on their ancestor Key, their kind, and a list of 
     * QueryAttributes, mapping them by their String-ified Key.
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A Dataset containing the Key-mapped records matching
     *                      the query.
     */
    public Dataset findMappingByQuery(Transaction txn, Key ancestorKey, 
            String kind, QueryAttribute ... attributes) {
        return handleFindQuery(txn, ancestorKey, kind, true, false, attributes);
    }

    /* FIND KEY BY QUERY */
    /**
     * Finds the Keys of records based on their kind and a list of 
     * QueryAttributes.
     * 
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              The Keys of the records matching the query.
     */
    public Key[] findKeyByQuery(String kind, 
            QueryAttribute ... attributes) {
        return findKeyByQuery(null, null, kind, attributes);
    }

    /**
     * Finds the Keys of records based on their ancestor Key, their kind, and a 
     * list of QueryAttributes.
     * 
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              The Keys of the records matching the query.
     */
    public Key[] findKeyByQuery(Key ancestorKey, String kind, 
            QueryAttribute ... attributes) {
        return findKeyByQuery(null, ancestorKey, kind, attributes);
    }

    /**
     * Finds the Keys of records based on their ancestor Key, their kind, and a 
     * list of QueryAttributes.
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor (parent) Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              The Keys of the records matching the query.
     */
    public Key[] findKeyByQuery(Transaction txn, Key ancestorKey, 
            String kind, QueryAttribute ... attributes) {
        return handleFindQuery(txn, ancestorKey, kind, false, true, attributes)
            .getList("record").toArray(new Key[0]);
    }

    /**
     * An intermediary method for finding by query.  Given a series of query 
     * and formatting configurations, creates a DataRequest that will produce 
     * the desired results (via {@code handleNewFindQueryRequest()}) and returns
     * its results.
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param keyMapping    Whether or not the records should be mapped by their
     *                      String-ified Keys.
     * @param keysOnly      Whether or not the results should be the entire 
     *                      records or just their Keys.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A Dataset containing the records matching the query.
     */
    protected Dataset handleFindQuery(Transaction txn, Key ancestorKey, 
            String kind, boolean keyMapping, boolean keysOnly, 
            QueryAttribute ... attributes) {
        return handleNewFindQueryRequest(txn, ancestorKey, kind, keyMapping, 
                keysOnly, attributes).getResponseOrAbort();
    }
    
    /* FIND BY QUERY - DATA REQUEST */
    /**
     * Creates a DataRequest that will produce records matching a specified kind
     * and a list of QueryAttributes.
     * 
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the records matching
     *                      the query.  Records are mapped by the attribute 
     *                      "record".
     */
    public DataRequest newFindByQueryRequest(String kind, 
            QueryAttribute ... attributes) {
        return newFindByQueryRequest(null, null, kind, attributes);
    }

    /**
     * Creates a DataRequest that will produce records matching a specified 
     * ancestor Key, kind, and a list of QueryAttributes.
     * 
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the records matching
     *                      the query.  Records are mapped by the attribute 
     *                      "record".
     */
    public DataRequest newFindByQueryRequest(Key ancestorKey, String kind, 
            QueryAttribute ... attributes) {
        return newFindByQueryRequest(null, ancestorKey, kind, attributes);
    }

    /**
     * Creates a DataRequest that will produce records matching a specified 
     * ancestor Key, kind, and a list of QueryAttributes.  Runs under the 
     * specified Transaction.
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the records matching
     *                      the query.  Records are mapped by the attribute 
     *                      "record".
     */
    public DataRequest newFindByQueryRequest(Transaction txn, Key ancestorKey, 
            String kind, QueryAttribute ... attributes) {
        return handleNewFindQueryRequest(txn, ancestorKey, kind, false, false, 
                attributes);
    }

    /* FIND MAPPING BY QUERY - DATA REQUEST */
    /**
     * Creates a DataRequest that will produce records matching a specified kind
     * and a list of QueryAttributes, mapping them by their String-ified Key.
     * 
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the Key-mapped 
     *                      records matching the query.
     */
    public DataRequest newFindMappingByQueryRequest(String kind, 
            QueryAttribute ... attributes) {
        return newFindMappingByQueryRequest(null, null, kind, attributes);
    }

    /**
     * Creates a DataRequest that will produce records matching a specified 
     * ancestor Key, kind, and a list of QueryAttributes, mapping them by their 
     * String-ified Key.
     * 
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the Key-mapped 
     *                      records matching the query.
     */
    public DataRequest newFindMappingByQueryRequest(Key ancestorKey, 
            String kind, QueryAttribute ... attributes) {
        return newFindMappingByQueryRequest(null, ancestorKey, kind, 
                attributes);
    }

    /**
     * Creates a DataRequest that will produce records matching a specified 
     * ancestor Key, kind, and a list of QueryAttributes, mapping them by their 
     * String-ified Key.Runs under the specified Transaction.
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the Key-mapped 
     *                      records matching the query.
     */
    public DataRequest newFindMappingByQueryRequest(Transaction txn, 
            Key ancestorKey, String kind, QueryAttribute ... attributes) {
        return handleNewFindQueryRequest(txn, ancestorKey, kind, true, false, 
                attributes);
    }

    /* FIND KEY BY QUERY - DATA REQUEST */
    /**
     * Creates a DataRequest that will produce the Keys of records matching a 
     * specified kind and a list of QueryAttributes.
     * 
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the Keys of the 
     *                      records matching the query.
     */
    public DataRequest newFindKeyByQueryRequest(String kind, 
            QueryAttribute ... attributes) {
        return newFindKeyByQueryRequest(null, null, kind, attributes);
    }

    /**
     * Creates a DataRequest that will produce the Keys of records matching a 
     * specified ancestor Key, kind, and a list of QueryAttributes.
     * 
     * @param parentKey     The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the Keys of the 
     *                      records matching the query.
     */
    public DataRequest newFindKeyByQueryRequest(Key parentKey, String kind, 
            QueryAttribute ... attributes) {
        return newFindKeyByQueryRequest(null, parentKey, kind, attributes);
    }

    /**
     * Creates a DataRequest that will produce the Keys of records matching a 
     * specified ancestor Key, kind, and a list of QueryAttributes.  Runs under 
     * the specified Transaction.
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the Keys of the 
     *                      records matching the query.
     */
    public DataRequest newFindKeyByQueryRequest(Transaction txn, 
            Key ancestorKey, String kind, QueryAttribute ... attributes) {
        return handleNewFindQueryRequest(txn, ancestorKey, kind, false, true, 
                attributes);
    }

    /**
     * An intermediary method for finding by query.  Given a series of query 
     * and formatting configurations, creates a Query with the necessary 
     * restrictions, filters, and sorts.  That Query is then executed via a new 
     * QueryRequest, which is subsequently returned.  
     * 
     * @param txn           The Transaction under which this query will take 
     *                      place.
     * @param ancestorKey   The ancestor Key of the records to search.
     * @param kind          The kind (type) of the records to search.
     * @param keyMapping    Whether or not the records should be mapped by their
     *                      String-ified Keys.
     * @param keysOnly      Whether or not the results should be the entire 
     *                      records or just their Keys.
     * @param attributes    A series of filters and sorts to govern the query.
     * @return              A DataRequest that will produce the records matching
     *                      the query.
     */
    protected DataRequest handleNewFindQueryRequest(Transaction txn, 
            Key ancestorKey, String kind, boolean keyMapping, boolean keysOnly, 
            QueryAttribute ... attributes) {
        Query query = new Query(kind, ancestorKey);
        for (QueryAttribute attr : attributes) {
            attr.addToQuery(query);
        }
        QueryRequest request = new QueryRequest(this, txn, keyMapping, 
                keysOnly, query);
        request.run();
        return request;
    }
    
    
    /* FIND BY ANCESTOR */
    /**
     * Finds records with a common ancestor.  A record's ancestry includes 
     * itself, its parent, and its parent's ancestors.
     * 
     * @param parentKey     The Key whose descendants are returned.  
     * @return              A Dataset containing all records with {@code 
     *                      ancestorKey} as an ancestor.  Records are mapped by 
     *                      the attribute "record".
     */
    public Dataset findByAncestor(Key ancestorKey) {
        return findByAncestor(null, ancestorKey);
    }

    /**
     * Finds records with a common ancestor.  A record's ancestry includes 
     * itself, its parent, and its parent's ancestors.
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A Dataset containing all records with {@code 
     *                      ancestorKey} as an ancestor.  Records are mapped by 
     *                      the attribute "record".
     */
    public Dataset findByAncestor(Transaction txn, Key ancestorKey) {
        return handleFindAncestor(txn, false, false, ancestorKey);
    }
    
    /* FIND MAPPING BY ANCESTOR */
    /**
     * Finds records with a common ancestor, mapping them by their 
     * String-ified Key.  A record's ancestry includes itself, its parent, and 
     * its parent's ancestors.
     * 
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A Dataset containing the Key-mapped records with 
     *                      {@code ancestorKey} as an ancestor.
     */
    public Dataset findMappingByAncestor(Key ancestorKey) {
        return findMappingByAncestor(null, ancestorKey);
    }

    /**
     * Finds records with a common ancestor, mapping them by their 
     * String-ified Key.  A record's ancestry includes itself, its parent, and 
     * its parent's ancestors.
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A Dataset containing the Key-mapped records with 
     *                      {@code ancestorKey} as an ancestor.
     */
    public Dataset findMappingByAncestor(Transaction txn, Key ancestorKey) {
        return handleFindAncestor(txn, true, false, ancestorKey);
    }
    
    /* FIND KEY BY ANCESTOR */
    /**
     * Finds the Keys of records with a common ancestor.  A record's ancestry 
     * includes itself, its parent, and its parent's ancestors.
     * 
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              The Keys of all records with {@code ancestorKey} as 
     *                      an ancestor.
     */
    public Key[] findKeyByAncestor(Key ancestorKey) {
        return findKeyByAncestor(null, ancestorKey);
    }

    /**
     * Finds the Keys of records with a common ancestor.  A record's ancestry 
     * includes itself, its parent, and its parent's ancestors.
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              The Keys of all records with {@code ancestorKey} as 
     *                      an ancestor.
     */
    public Key[] findKeyByAncestor(Transaction txn, Key ancestorKey) {
        return handleFindAncestor(txn, false, true, ancestorKey).getList(
                "record").toArray(new Key[0]);
    }
    
    /**
     * An intermediary method for finding by ancestor key.  Gets a DataRequest
     * containing the desired results (via {@code 
     * handleNewFindByAncestorRequest()}), and returns its contents.  
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param keyMapping    Whether or not the records should be mapped by their
     *                      String-ified Keys.
     * @param keysOnly      Whether or not the results should be the entire 
     *                      records or just their Keys.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A Dataset containing all records with {@code 
     *                      ancestorKey} as an ancestor.
     */
    protected Dataset handleFindAncestor(Transaction txn, boolean keyMapping, 
            boolean keysOnly, Key ancestorKey) {
        return handleNewFindAncestorRequest(txn, keyMapping, keysOnly, 
                ancestorKey).getResponseOrAbort();
    }
    
    /* FIND BY ANCESTOR - DATA REQUEST */
    /**
     * Creates a DataRequest that will produce records with a common ancestor.  
     * A record's ancestry includes itself, its parent, and its parent's 
     * ancestors.  
     * 
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A DataRequest that will produce all records with 
     *                      {@code ancestorKey} as an ancestor.
     */
    public DataRequest newFindByAncestorRequest(Key ancestorKey) {
        return newFindByAncestorRequest(null, ancestorKey);
    }

    /**
     * Creates a DataRequest that will produce records with a common ancestor.  
     * A record's ancestry includes itself, its parent, and its parent's 
     * ancestors.
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A DataRequest that will produce all records with 
     *                      {@code ancestorKey} as an ancestor.
     */
    public DataRequest newFindByAncestorRequest(Transaction txn, 
            Key parentKey) {
        return handleNewFindAncestorRequest(txn, false, false, parentKey);
    }
    
    /* FIND MAPPING BY ANCESTOR - DATA REQUEST */
    /**
     * Creates a DataRequest that will produce records with a common ancestor, 
     * mapping them by their String-ified Key.  A record's ancestry includes 
     * itself, its parent, and its parent's ancestors.
     * 
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A DataRequest that will produce the Key-mapped 
     *                      records with {@code ancestorKey} as an ancestor.
     */
    public DataRequest newFindMappingByAncestorRequest(Key ancestorKey) {
        return newFindMappingByAncestorRequest(null, ancestorKey);
    }

    /**
     * Creates a DataRequest that will produce records with a common ancestor, 
     * mapping them by their String-ified Key.  A record's ancestry includes 
     * itself, its parent, and its parent's ancestors.
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A DataRequest that will produce the Key-mapped 
     *                      records with {@code ancestorKey} as an ancestor.
     */
    public DataRequest newFindMappingByAncestorRequest(Transaction txn, 
            Key ancestorKey) {
        return handleNewFindAncestorRequest(txn, true, false, ancestorKey);
    }
    
    /* FIND KEY BY ANCESTOR - DATA REQUEST */
    /**
     * Creates a DataRequest that will produce the Keys of records with a common
     * ancestor.  A record's ancestry includes itself, its parent, and its 
     * parent's ancestors.
     * 
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A DataRequest that will produce the Keys of all 
     *                      records with {@code ancestorKey} as an ancestor.
     */
    public DataRequest newFindKeyByAncestorRequest(Key ancestorKey) {
        return newFindKeyByAncestorRequest(null, ancestorKey);
    }

    /**
     * Creates a DataRequest that will produce the Keys of records with a common
     * ancestor.  A record's ancestry includes itself, its parent, and its 
     * parent's ancestors.
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A DataRequest that will produce the Keys of all 
     *                      records with {@code ancestorKey} as an ancestor.
     */
    public DataRequest newFindKeyByAncestorRequest(Transaction txn, 
            Key ancestorKey) {
        return handleNewFindAncestorRequest(txn, false, true, ancestorKey);
    }
    
    /**
     * An intermediary method for finding by parent (ancestor) key.  Creates 
     * a new ancestor Query, sorted only by Key (this is the only sort allowed 
     * on ancestor queries as of November 2009).  That Query is then wrapped in 
     * a QueryRequest, which is then executed and returned. 
     * 
     * @param txn           The Transaction under which this search will take 
     *                      place.
     * @param keyMapping    Whether or not the records should be mapped by their
     *                      String-ified Keys.
     * @param keysOnly      Whether or not the results should be the entire 
     *                      records or just their Keys.
     * @param ancestorKey   The Key whose descendants are returned.  
     * @return              A DataRequest that will produce all records with 
     *                      {@code ancestorKey} as an ancestor.
     */
    protected DataRequest handleNewFindAncestorRequest(Transaction txn, 
            boolean keyMapping, boolean keysOnly, Key ancestorKey) {
        Query query = new Query(ancestorKey);
        query.addSort(Entity.KEY_RESERVED_PROPERTY);
        QueryRequest request = new QueryRequest(this, txn, keyMapping, 
                keysOnly, query);
        request.run();
        return request;
    }
    
    
    /* ENTITY - DATASET CONVERSION UTILITIES */
    /**
     * Converts an Entity to a Dataset, copying over all Entity fields and 
     * storing the Entity Key as the Dataset's {@code KEY_PROPERTY} value.
     * 
     * @param entity        The Entity to convert.
     * @return              A Dataset containing the Key and contents of the 
     *                      Entity.
     */
    protected static Dataset entityToDataset(Entity entity) {
        Dataset result = new Dataset();
        
        for (Map.Entry<String, Object> property : 
                entity.getProperties().entrySet()) {
            String key = property.getKey();
            Object value = property.getValue();
            // Ensure that the resulting Dataset internally stores an Entity 
            // Collection<T> as a series of single T objects, instead of as a 
            // single Collection<T> object; all Entity collections are intended 
            // as multiple values with the same key, not a single Collection 
            // value.  
            if (value instanceof Collection) {
                for (Object subVal : (Collection)value) {
                    result.add(key, subVal);
                }
            } else {
                result.add(key, value);
            }
        }
        
        // Add the Entity Key.
        result.set(KEY_PROPERTY, entity.getKey());
        
        return result;
    }
    
    /**
     * Converts a Dataset to an Entity with the specified parent key and kind.
     * 
     * @param parentKey     The parent key for the Entity.
     * @param kind          The kind of the Entity.
     * @param dataset       The contents of the Entity.
     * @return              An Entity with the specified contents, ancestry, 
     *                      and kind.
     */
    protected static Entity datasetToEntity(Key parentKey, String kind, 
            Dataset dataset) {
        // Create a new Entity of the specified kind with the specified parent.
        Entity result = new Entity(kind, parentKey);
        // Fill the Entity.
        return fillEntity(dataset, result);
    }
    
    /**
     * Converts a Dataset to an Entity with the specified kind.
     * 
     * @param kind          The kind of the Entity.
     * @param dataset       The contents of the Entity.
     * @return              An Entity with the specified contents and kind. 
     */
    protected static Entity datasetToEntity(String kind, Dataset dataset) {
        // Create a new Entity of the specified kind.
        Entity result = new Entity(kind);
        // Fill the Entity.
        return fillEntity(dataset, result);
    }
    
    /**
     * Converts a Dataset to an Entity, acting under the assumption that the 
     * Entity Key is already present in the specified Dataset.  If a Key is not 
     * present where expected, a DatastoreError is thrown.
     * 
     * @param dataset       The contents of the Entity, including its Key.
     * @return              An Entity with the specified contents and Key. 
     */
    protected static Entity datasetToEntity(Dataset dataset) {
        // Extract the Key, and create a new Entity with that Key.
        Entity result = null;
        if (dataset.containsKey(KEY_PROPERTY)) {
            Object keyObj = dataset.get(KEY_PROPERTY);
            Key key = null;
            if (keyObj instanceof Key) {
                key = (Key)keyObj;
            } else if (keyObj instanceof String) {
                key = stringToKey((String)keyObj);
            } else {
                error("Key property (" + KEY_PROPERTY + ") is not of type " +
                		"Key or String: " + keyObj.getClass().getName(), null);
            }
            result = new Entity(key);
        } else {
            error("Dataset must contain a Key attribute " + KEY_PROPERTY, null);
        }
        // Fill the Entity.
        return fillEntity(dataset, result);
    }
    
    /**
     * Given an Entity and a Dataset, fills the Entity with the key-value 
     * pairs of the Dataset.  
     * 
     * @param dataset       A Dataset whose contents will be copied into the 
     *                      Entity.
     * @param entity        An empty Entity.
     * @return              The populated Entity.
     */
    protected static Entity fillEntity(Dataset dataset, Entity entity) {
        for (String property : dataset.keySet()) {
            if (property.equals(KEY_PROPERTY)) continue; // Ignore any Key.
            ArrayList<Object> value = dataset.getList(property);
            // If there are any one-value lists in the dataset, just insert 
            // the single value.
            Object toInsert = (value.size() == 1) ? value.get(0) : value;
            entity.setProperty(property, toInsert);
        }
        return entity;
    }
    
    
    /* KEY - STRING CONVERSION UTILITIES */
    /**
     * Converts a String to a Key, throwing a DatastoreError if the conversion 
     * fails.
     * 
     * @param key           The String to convert.
     * @return              The converted Key.
     */
    public static Key stringToKey(String key) {
        try {
            return KeyFactory.stringToKey(key);
        } catch (IllegalArgumentException e) {
            error("String cannot be converted into a valid Key: " + key, e);
        }
        return null;
    }
    
    /**
     * Converts a Key to a String.  The resulting string is guaranteed by 
     * Google to be web-safe.  This String-ified Key is what is used to map 
     * Entity records in the various findMappingBy...() methods ({@code 
     * findMappingByKey()}, {@code findMappingByQuery()}, {@code 
     * findMappingByAncestor()}).  This differs from Key.toString(), which 
     * yields a different value.
     * 
     * @param key           The Key to convert.
     * @return              The converted String.
     */
    public static String keyToString(Key key) {
        return KeyFactory.keyToString(key);
    }

    
    /* INTERNAL ERROR-HANDLING UTILITIES */
    /**
     * Given a message and a Throwable cause, creates and throws a new 
     * DatastoreError.
     * 
     * @param message       The error message.
     * @param cause         A Throwable cause: the original Error, or null.
     * @throws DatastoreError
     */
    protected static void error(String message, Throwable cause) 
            throws DatastoreError {
        logger.error("DatastoreError: " + message + 
                (cause == null ? "" : cause.getMessage()));
        throw new DatastoreError(message, cause);
    }
    
    /**
     * A convenience method for common errors.  Given an ErrType for a common 
     * error, a message, and a Throwable cause, creates and throws a new 
     * DatastoreError with a message composed of the specified message and a 
     * preset description of the problem (based on the ErrType).
     * 
     * @param type          The type (ErrType) of the error.
     * @param message       An additional error message.
     * @param cause         A Throwable cause: the original Error, or null.
     * @throws DatastoreError
     */
    protected static void error(ErrType type, String message, Throwable cause) 
            throws DatastoreError {
        String typeMsg = "";
        switch (type) {
            case DS_FAILURE: typeMsg = "Datastore failure: "; break;
            case TXN_INACTIVE: typeMsg = "Transaction is inactive: "; break;
            case BAD_ARG: typeMsg = "Invalid, incomplete, or malformed " +
            		"argument of type: "; break;
        }
        error(typeMsg + message, cause);
    }
}
