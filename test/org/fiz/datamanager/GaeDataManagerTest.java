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

package org.fiz.datamanager;

import org.fiz.*;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.dev.LocalDatastoreService;
import com.google.appengine.tools.development.ApiProxyLocalImpl;
import com.google.apphosting.api.ApiProxy;

import java.io.File;
import java.util.*;

/**
 * Junit tests for the GaeDataManager class.
 */
public class GaeDataManagerTest extends junit.framework.TestCase {

    protected GaeDataManager manager = null;

    /* Sample data set used throughout the unit tests */
    protected static final String ID = "order_id";
    protected static final String FIRSTN = "first_name";
    protected static final String MI = "m_i";
    protected static final String LASTN = "last_name";
    protected static final String YEAR = "start_date";
    protected DatasetComparator sampleDataComp = new DatasetComparator(ID);
    protected Dataset[] sampleData = new Dataset[] {
        new Dataset(ID, 0, FIRSTN, "John", LASTN, "Hanson", YEAR, "1781"),
        new Dataset(ID, 1, FIRSTN, "George", LASTN, "Washington", YEAR, 1789),
        new Dataset(ID, 2, FIRSTN, "John", LASTN, "Adams", YEAR, 1797),
        new Dataset(ID, 3, FIRSTN, "Thomas", LASTN, "Jefferson", YEAR, 1801),
        new Dataset(ID, 4, FIRSTN, "James", LASTN, "Madison", YEAR, 1809),
        new Dataset(ID, 5, FIRSTN, "James", LASTN, "Monroe", YEAR, 1817),
        new Dataset(ID, 6, FIRSTN, "John", MI, "Q", LASTN, "Adams", YEAR, 1825),
        new Dataset(ID, 7, FIRSTN, "Andrew", LASTN, "Jackson", YEAR, 1829),
        new Dataset(ID, 8, FIRSTN, "Martin", LASTN, "Van Buren", YEAR, 1837),
        new Dataset(ID, 9, FIRSTN, "William", MI, "H", LASTN, "Harrison", YEAR,
                1841),
        new Dataset(ID, 10, FIRSTN, "John", LASTN, "Tyler", YEAR, 1841),
        new Dataset(ID, 11, FIRSTN, "James", MI, "K", LASTN, "Polk", YEAR,
                1845),
        new Dataset(ID, 12, FIRSTN, "Zachary", LASTN, "Taylor", YEAR, 1849),
        new Dataset(ID, 13, FIRSTN, "Millard", LASTN, "Fillmore", YEAR, 1850),
        new Dataset(ID, 14, FIRSTN, "Andrew", LASTN, "Pierce", YEAR, 1853),
        new Dataset(ID, 15, FIRSTN, "James", LASTN, "Buchannan", YEAR, 1857),
        new Dataset(ID, 16, FIRSTN, "Abraham", LASTN, "Lincoln", YEAR, 1861),
        new Dataset(ID, 17, FIRSTN, "Andrew", LASTN, "Johnson", YEAR, 1865),
        new Dataset(ID, 18, FIRSTN, "Ulysses", MI, "S", LASTN, "Grant", YEAR,
                1869),
        new Dataset(ID, 19, FIRSTN, "Rutherford", MI, "B", LASTN, "Hayes", YEAR,
                1877),
        new Dataset(ID, 20, FIRSTN, "James", LASTN, "Garfield", YEAR, 1881),
        new Dataset(ID, 21, FIRSTN, "Chester", MI, "A", LASTN, "Arthur", YEAR,
                1881),
        new Dataset(ID, 22, FIRSTN, "Grover", LASTN, "Cleveland", YEAR, 1885),
        new Dataset(ID, 23, FIRSTN, "Benjamin", LASTN, "Harrison", YEAR, 1889),
        new Dataset(ID, 24, FIRSTN, "Grover", LASTN, "Cleveland", YEAR, 1893),
        new Dataset(ID, 25, FIRSTN, "William", LASTN, "McKinley", YEAR, 1897),
        new Dataset(ID, 26, FIRSTN, "Theodore", LASTN, "Roosevelt", YEAR, 1901),
        new Dataset(ID, 27, FIRSTN, "William", MI, "H", LASTN, "Taft", YEAR,
                1909),
        new Dataset(ID, 28, FIRSTN, "Woodrow", LASTN, "Wilson", YEAR, 1913),
        new Dataset(ID, 29, FIRSTN, "Warren", MI, "G", LASTN, "Harding", YEAR,
                1921),
        new Dataset(ID, 30, FIRSTN, "Calvin", LASTN, "Coolidge", YEAR, 1923),
        new Dataset(ID, 31, FIRSTN, "Herbert", LASTN, "Hoover", YEAR, 1929),
        new Dataset(ID, 32, FIRSTN, "Franklin", MI, "D", LASTN, "Roosevelt",
                YEAR, 1933),
        new Dataset(ID, 33, FIRSTN, "Harry", LASTN, "Truman", YEAR, 1945),
        new Dataset(ID, 34, FIRSTN, "Dwight", MI, "D", LASTN, "Eisenhower",
                YEAR, 1953),
        new Dataset(ID, 35, FIRSTN, "John", MI, "F", LASTN, "Kennedy", YEAR,
                1961),
        new Dataset(ID, 36, FIRSTN, "Lyndon", MI, "B", LASTN, "Johnson", YEAR,
                1963),
        new Dataset(ID, 37, FIRSTN, "Richard", MI, "M", LASTN, "Nixon", YEAR,
                1969),
        new Dataset(ID, 38, FIRSTN, "Gerald", LASTN, "Ford", YEAR, 1974),
        new Dataset(ID, 39, FIRSTN, "James", LASTN, "Carter", YEAR, 1977),
        new Dataset(ID, 40, FIRSTN, "Ronald", LASTN, "Reagan", YEAR, 1981),
        new Dataset(ID, 41, FIRSTN, "George", MI, "H", MI, "W", LASTN, "Bush",
                YEAR, 1989),
        new Dataset(ID, 42, FIRSTN, "William", MI, "J", LASTN, "Clinton", YEAR,
                1993),
        new Dataset(ID, 43, FIRSTN, "George", MI, "W", LASTN, "Bush", YEAR,
                2001),
        new Dataset(ID, 44, FIRSTN, "Barack", MI, "H", LASTN, "Obama", YEAR,
                2009)
    };

    /*
     * Methods responsible for creating and destroying the dummy Datastore
     * environment used for these unit tests.
     */
    public void setUp() {
        ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
        ApiProxy.setDelegate(new ApiProxyLocalImpl(
                new File("test/testData")){});
        ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
        proxy.setProperty(LocalDatastoreService.NO_STORAGE_PROPERTY,
                Boolean.TRUE.toString());

        manager = new GaeDataManager();
    }

    public void tearDown() {
        ApiProxyLocalImpl proxy = (ApiProxyLocalImpl)ApiProxy.getDelegate();
        LocalDatastoreService datastoreService =
            (LocalDatastoreService)proxy.getService("datastore_v3");
        datastoreService.clearProfiles();
        ApiProxy.setDelegate(null);
        ApiProxy.setEnvironmentForCurrentThread(null);
    }

    public void test_InvalidQueryError() {
        GaeDataManager.InvalidQueryError e =
            new GaeDataManager.InvalidQueryError("abc");
        assertEquals("abc", e.getMessage());
    }


    public void test_GaeException() {
        GaeDataManager.GaeException e = new GaeDataManager.GaeException("hi");
        assertEquals("hi", e.getMessage());
    }

    public void test_constructor() {
        // Make sure the DatastoreService was created successfully.
        assertFalse("DatastoreService is not null", manager.datastore == null);
    }

    public void test_getNewTransaction() {
        // Make sure getNewTransaction returns active, distinct Transactions.
        Transaction txn1 = manager.getNewTransaction();
        Transaction txn2 = manager.getNewTransaction();
        assertTrue("Transaction1 active before commit", txn1.isActive());
        txn1.commit();
        assertFalse("Transaction1 inactive after commit", txn1.isActive());
        assertTrue("Transaction2 active before rollback", txn2.isActive());
        txn2.rollback();
        assertFalse("Transaction2 inactive after rollback", txn2.isActive());
    }

    public void test_allocateKeys_withParent() {
        Key parentKey = (Key) manager.insert("president", sampleData[0]).get("record");
        Key[] keys = manager.allocateKeys(parentKey, "president",
                sampleData.length);
        // Verify that the Keys all have the same parent.
        for (Key key : keys) {
            assertEquals("Correct parent Key", parentKey, key.getParent());
        }
        // Verify that the Keys are new and distinct by inserting them into the
        // Datastore.
        for (int i=0; i<sampleData.length; i++) {
            sampleData[i].add(GaeDataManager.KEY_PROPERTY, keys[i]);
        }
        manager.insert(sampleData);
    }

    public void test_allocateKeys_noParent() {
        Key[] keys = manager.allocateKeys("president", sampleData.length);
        // Verify that the Keys have no parent.
        for (Key key : keys) {
            assertEquals("No parent Key", null, key.getParent());
        }
        // Verify that the Keys are new and distinct by inserting them into the
        // Datastore.
        for (int i=0; i<sampleData.length; i++) {
            sampleData[i].add(GaeDataManager.KEY_PROPERTY, keys[i]);
        }
        manager.insert(sampleData);
    }

    public void test_convertKeyRange() {
        KeyRange range = manager.datastore.allocateIds("president", 5);
        Key[] rangeKeys = manager.convertKeyRange(range);
        Iterator<Key> rangeIter = range.iterator();
        int counter = 0;
        while (rangeIter.hasNext()) {
            Key key = rangeIter.next();
            assertEquals("KeyRange and Key[] contain the same Keys", key,
                    rangeKeys[counter++]);
        }
        assertEquals("KeyRange and Key[] contain the same number of Keys",
                counter, rangeKeys.length);
    }

    /* TEST QUERY ATTRIBUTES */
    public void test_QueryTerm_constructor() {
        // Equals (=).
        GaeDataManager.QueryTerm term1 =
            new GaeDataManager.QueryTerm("a", "=", 1);
        assertEquals("term1 attrName", "a", term1.attrName);
        assertEquals("term1 attrValue", 1, term1.attrValue);
        assertEquals("term1 operator", Query.FilterOperator.EQUAL,
                term1.operator);

        // Less than (<).
        GaeDataManager.QueryTerm term2 =
            new GaeDataManager.QueryTerm("b", "<", "2");
        assertEquals("term2 operator", Query.FilterOperator.LESS_THAN,
                term2.operator);

        // Greater than (>).
        GaeDataManager.QueryTerm term3 =
            new GaeDataManager.QueryTerm("c", ">", 3);
        assertEquals("term3 operator", Query.FilterOperator.GREATER_THAN,
                term3.operator);

        // Less than or equal (<=).
        GaeDataManager.QueryTerm term4 =
            new GaeDataManager.QueryTerm("d", "<=", "4");
        assertEquals("term4 operator", Query.FilterOperator.LESS_THAN_OR_EQUAL,
                term4.operator);

        // Greater than or equal (>=).
        GaeDataManager.QueryTerm term5 =
            new GaeDataManager.QueryTerm("e", ">=", 5);
        assertEquals("term5 operator",
                Query.FilterOperator.GREATER_THAN_OR_EQUAL, term5.operator);

        // Inequality (not recognized - error).
        try {
            new GaeDataManager.QueryTerm("f", "!=", "6");
            fail("Error not thrown");
        } catch (GaeDataManager.InvalidQueryError e) {
            assertEquals("term6 error message", "Operator (!=) must be one of:" +
                         " {=, <, >, <=, >=}.", e.getMessage());
        }
    }

    public void test_QueryTerm_addToQuery() {
        Query query = new Query();
        new GaeDataManager.QueryTerm("a", "=", 1).addToQuery(query);
        Query.FilterPredicate predicate = query.getFilterPredicates().get(0);

        assertEquals("QueryTerm attrName", "a", predicate.getPropertyName());
        assertEquals("QueryTerm attrValue", 1, predicate.getValue());
        assertEquals("QueryTerm operator", Query.FilterOperator.EQUAL,
                predicate.getOperator());
    }

    public void test_QuerySort_constructor() {
        // No direction specified (default ascending).
        GaeDataManager.QuerySort sort1 = new GaeDataManager.QuerySort("a");
        assertEquals("sort1 attrName", "a", sort1.attrName);
        assertEquals("sort1 operator", Query.SortDirection.ASCENDING,
                sort1.direction);

        // "ascending"
        GaeDataManager.QuerySort sort2 =
            new GaeDataManager.QuerySort("b", "asCeNdINg");
        assertEquals("sort2 operator", Query.SortDirection.ASCENDING,
                sort2.direction);

        // "asc".
        GaeDataManager.QuerySort sort3 =
            new GaeDataManager.QuerySort("c", "asc");
        assertEquals("sort3 operator", Query.SortDirection.ASCENDING,
                sort3.direction);

        // "descending".
        GaeDataManager.QuerySort sort4 =
            new GaeDataManager.QuerySort("d", "DEsceNDiNg");
        assertEquals("sort4 operator", Query.SortDirection.DESCENDING,
                sort4.direction);

        // "desc".
        GaeDataManager.QuerySort sort5 =
            new GaeDataManager.QuerySort("e", "DESC");
        assertEquals("sort5 operator", Query.SortDirection.DESCENDING,
                sort5.direction);

        // "increasing" (not recognized - error).
        try {
            new GaeDataManager.QuerySort("f", "increasing");
            fail("Error not thrown");
        } catch (GaeDataManager.InvalidQueryError e) {
            assertEquals("sort6 error message", "QuerySort Direction (increasing)" +
                         " must be (case insensitive) one of: {ascending, asc, " +
                         "descending, desc}.", e.getMessage());
        }
    }

    public void test_QuerySort_addToQuery() {
        Query query = new Query();
        new GaeDataManager.QuerySort("a", "asc").addToQuery(query);
        Query.SortPredicate predicate = query.getSortPredicates().get(0);

        assertEquals("QueryTerm attrName", "a", predicate.getPropertyName());
        assertEquals("QueryTerm direction", Query.SortDirection.ASCENDING,
                predicate.getDirection());
    }

    /* TEST FIND BY KEY */
    public void test_findByKey() {
        Key[] keys =  getKeys(manager.insert("president", sampleData));

        // Without Transaction.
        checkRecordsIntegrity(manager.findByKey(keys), sampleData,
                sampleDataComp);
        // With Transaction.
        Transaction txn = manager.getNewTransaction();
        checkRecordIntegrity(manager.findByKey(txn, keys[44]), sampleData[44]);
        txn.commit();
    }

    public void test_findMappingByKey() {
        Key parentKey = getKeys(manager.insert("president", sampleData[0]))[0];
        Key[] keys = getKeys(manager.insert(parentKey, "president", sampleData));

        // Without Transaction.
        checkMappedRecordsIntegrity(manager.findMappingByKey(keys), sampleData,
                keys);
        // With Transaction.
        Transaction txn = manager.getNewTransaction();
        checkMappedRecordsIntegrity(manager.findMappingByKey(txn, keys),
                sampleData, keys);
        txn.commit();
    }

    public void test_handleFindKey() {
        Key[] keys = getKeys(manager.insert("president", sampleData));
        Key[] keysWithParent = getKeys(manager.insert(keys[0], "president", sampleData));

        checkMappedRecordsIntegrity(manager.handleFindKey(null, true, keys),
                sampleData, keys);

        // No Transaction, single Key.
        Dataset request1 = manager.handleFindKey(null, false, keys[0]);
        checkRecordIntegrity(request1, sampleData[0]);

        // Transaction, single Key.
        Transaction txn2 = manager.getNewTransaction();
        Dataset request2 = manager.handleFindKey(txn2, false, keys[0]);
        checkRecordIntegrity(request2, sampleData[0]);
        txn2.commit();

        // No Transaction, multiple Keys.
        Dataset request3 = manager.handleFindKey(null, false, keys);
        checkRecordsIntegrity(request3, sampleData, sampleDataComp);

        // Transaction, multiple Keys (error - multiple Entity groups).
        Transaction txn4 = manager.getNewTransaction();
        Dataset data4 = manager.handleFindKey(txn4, false, keys);
        txn4.rollback();
        assertEquals("Get with Transaction across multiple Entity groups " +
                     "yields an error", "Invalid, incomplete, or malformed " +
                     "argument of type: Key", data4.getErrorMessage());

        // Transaction, multiple Keys (no error).
        Transaction txn5 = manager.getNewTransaction();
        Dataset request5 = manager.handleFindKey(txn5, false, keysWithParent);
        checkRecordsIntegrity(request5, sampleData, sampleDataComp);
        txn5.commit();

        // Multiple Keys with Key mapping.
        Dataset request6 = manager.handleFindKey(null, true, keys);
        checkMappedRecordsIntegrity(request6, sampleData, keys);

        // Single Key matching no Entity.
        Key[] notInserted = manager.allocateKeys("president", 1);
        Dataset request7 = manager.handleFindKey(null, false, notInserted[0]);
        assertTrue("No records returned when no Entity Key matched",
                isEmptyDataset(request7));

        // Inactive Transaction.
        Transaction txn8 = manager.getNewTransaction();
        txn8.commit();
        Dataset data8 = manager.handleFindKey(txn8, false, keys[0]);
        assertEquals("Get with inactive transaction yields error" +
                     "yields an error", "Transaction is inactive. ",
                     data8.getErrorMessage());
    }

    /* TEST INSERT */
    public void test_insert() {
        // No Transaction, no pre-allocated Keys.
        Key k1 = getKeys(manager.insert("president", sampleData[0]))[0];
        Key[] k2 = getKeys(manager.insert("president", sampleData));
        Key k3 = getKeys(manager.insert(k1, "president", sampleData[0]))[0];
        Key[] k4 = getKeys(manager.insert(k1, "president", sampleData));
        checkRecordIntegrity(manager.findByKey(k1), sampleData[0]);
        checkRecordsIntegrity(manager.findByKey(k2),sampleData, sampleDataComp);
        checkRecordIntegrity(manager.findByKey(k3), sampleData[0]);
        checkRecordsIntegrity(manager.findByKey(k4),sampleData, sampleDataComp);

        // Transaction, no pre-allocated Keys.
        Transaction txn1 = manager.getNewTransaction();
        Key tk1 = getKeys(manager.insert(txn1, "president", sampleData[0]))[0];
        Key tk2 = getKeys(manager.insert(txn1, tk1, "president", sampleData[0]))[0];
        Key[] tk3 = getKeys(manager.insert(txn1, tk1, "president", sampleData));
        txn1.commit();
        checkRecordIntegrity(manager.findByKey(tk1), sampleData[0]);
        checkRecordIntegrity(manager.findByKey(tk2), sampleData[0]);
        checkRecordsIntegrity(manager.findByKey(tk3),sampleData,sampleDataComp);

        // Pre-allocated Keys.
        Key[] alloc = manager.allocateKeys(k1, "president", sampleData.length);
        for (int i=0; i<sampleData.length; i++) {
            sampleData[i].set(GaeDataManager.KEY_PROPERTY, alloc[i]);
        }
        Key ak1 = getKeys(manager.insert(sampleData[0]))[0];
        Key[] ak2 = getKeys(manager.insert(sampleData));
        Transaction txn2 = manager.getNewTransaction();
        Key ak3 = getKeys(manager.insert(txn2, sampleData[0]))[0];
        Key[] ak4 = getKeys(manager.insert(txn2, sampleData));
        txn2.commit();
        checkRecordIntegrity(manager.findByKey(ak1), sampleData[0], true);
        checkRecordsIntegrity(manager.findByKey(ak2), sampleData,
                sampleDataComp, true);
        checkRecordIntegrity(manager.findByKey(ak3), sampleData[0], true);
        checkRecordsIntegrity(manager.findByKey(ak4), sampleData,
                sampleDataComp, true);
    }

    public void test_handleInsert() {
        Key parentKey = getKeys(manager.insert("president", sampleData[0]))[0];
        Entity[] sampleEntities = new Entity[sampleData.length];
        for (int i=1; i<20; i++) {
            // Some Entities with no parent Key.
            sampleEntities[i] = GaeDataManager.datasetToEntity("president",
                    sampleData[i]);
        }
        for (int i=20; i<sampleData.length; i++) {
            // Some Entities with a parent Key.
            sampleEntities[i] = GaeDataManager.datasetToEntity(parentKey,
                    "president", sampleData[i]);
        }

        // No Transaction, single Entity.
        Dataset request1 = manager.handleInsert(null, sampleEntities[1]);
        checkRecordIntegrity(manager.findByKey(extractKeys(request1)[0]),
                sampleData[1]);

        // Transaction (commit), single Entity.
        Transaction txn2 = manager.getNewTransaction();
        Dataset request2 = manager.handleInsert(txn2, sampleEntities[2]);
        assertTrue("No records returned when Transaction isn't committed",
                isEmptyDataset(manager.findByKey(extractKeys(request2)[0])));
        txn2.commit();
        checkRecordIntegrity(manager.findByKey(extractKeys(request2)[0]),
                sampleData[2]);

        // Transaction (rollback), single Entity.
        Transaction txn3 = manager.getNewTransaction();
        Dataset request3 = manager.handleInsert(txn3, sampleEntities[3]);
        assertTrue("No records returned when Transaction isn't committed",
                isEmptyDataset(manager.findByKey(extractKeys(request3)[0])));
        txn3.rollback();
        assertTrue("No records returned when Transaction isn't committed",
                isEmptyDataset(manager.findByKey(extractKeys(request3)[0])));

        // No Transaction, multiple Entities.
        Dataset request4 = manager.handleInsert(null,
                    Arrays.copyOfRange(sampleEntities, 4, 12));
        checkRecordsIntegrity(manager.findByKey(extractKeys(request4)),
                Arrays.copyOfRange(sampleData, 4, 12), sampleDataComp);

        // Transaction, multiple Entities (error - multiple Entity groups).
        Transaction txn5 = manager.getNewTransaction();
        Dataset data5 = manager.handleInsert(txn5, Arrays.copyOfRange(sampleEntities, 12, 20));
        txn5.rollback();
        assertEquals("Insert with Transaction across multiple Entity groups " +
                     "yields an error", "Invalid, incomplete, or malformed " +
                     "argument of type: Entity", data5.getErrorMessage());

        // Transaction, multiple Entities (no error).
        Transaction txn6 = manager.getNewTransaction();
        Dataset request6 = manager.handleInsert(txn6,
                    Arrays.copyOfRange(sampleEntities, 20, 42));
        txn6.commit();
        checkRecordsIntegrity(manager.findByKey(extractKeys(request6)),
                Arrays.copyOfRange(sampleData, 20, 42), sampleDataComp);

        // Previously inserted Entities - performs an update.
        sampleEntities[20].setProperty(YEAR, 1234);
        sampleData[20].set(YEAR, 1234);
        Dataset request7 = manager.handleInsert(null, sampleEntities[20]);
        checkRecordIntegrity(manager.findByKey(extractKeys(request7)),
                sampleData[20]);

        // Inactive Transaction.
        Transaction txn8 = manager.getNewTransaction();
        txn8.commit();
        Dataset data8 = manager.handleInsert(txn8, sampleEntities[44]);
        assertEquals("Insert with inactive transaction yields error",
                     "Transaction is inactive. ", data8.getErrorMessage());
    }


    /* TEST DELETE */
    public void test_delete() {
        Key[] keys = getKeys(manager.insert("president", sampleData));
        checkRecordsIntegrity(manager.findByKey(keys), sampleData,
                sampleDataComp);
        // With Transaction.
        Transaction txn = manager.getNewTransaction();
        manager.delete(txn, keys[0]);
        txn.commit();
        // Without Transaction.
        manager.delete(Arrays.copyOfRange(keys, 1, keys.length));
        assertTrue("All deleted", isEmptyDataset(manager.findByKey(keys)));
    }

    public void test_handleDelete() {
        Key[] rootKeys = getKeys(manager.insert("president",
                Arrays.copyOfRange(sampleData, 0, 20)));
        Key[] childKeys = getKeys(manager.insert(rootKeys[0], "president",
                 Arrays.copyOfRange(sampleData, 20, sampleData.length)));

        // No Transaction.
        manager.handleDelete(null, Arrays.copyOfRange(rootKeys, 0, 10));
        assertTrue("Records successfully deleted", isEmptyDataset(
                manager.findByKey(Arrays.copyOfRange(rootKeys, 0, 10))));

        // Transaction (error - multiple Entity groups).
        Transaction txn2 = manager.getNewTransaction();
        Dataset data2 = manager.handleDelete(txn2, Arrays.copyOfRange(rootKeys, 10, 20));
        txn2.rollback();
        assertEquals("Delete across multiple Entity groups yields an error",
                     "Invalid, incomplete, or malformed argument of type: Key",
                     data2.getErrorMessage());

        // Transaction (commit).
        Transaction txn3 = manager.getNewTransaction();
        manager.handleDelete(txn3, childKeys);
        checkRecordsIntegrity(manager.findByKey(childKeys),
                Arrays.copyOfRange(sampleData, 20, sampleData.length),
                sampleDataComp);
        txn3.commit();
        assertTrue("Records successfully deleted", isEmptyDataset(
                manager.findByKey(childKeys)));

        // Transaction (rollback).
        Transaction txn4 = manager.getNewTransaction();
        manager.handleDelete(txn4, rootKeys[11]);
        checkRecordIntegrity(manager.findByKey(rootKeys[11]), sampleData[11]);
        txn4.rollback();
        checkRecordIntegrity(manager.findByKey(rootKeys[11]), sampleData[11]);

        // Inactive Transaction.
        Transaction txn5 = manager.getNewTransaction();
        txn5.rollback();
        Dataset data5 = manager.handleDelete(txn5, rootKeys[12]);
        assertEquals("Delete with inactive Transaction yields error",
                     "Transaction is inactive. ", data5.getErrorMessage());

        // Previously deleted Entities - no error.
        Dataset data6 = manager.handleDelete(null,
                        Arrays.copyOfRange(rootKeys, 0, 10));
        assertTrue("Records successfully deleted", isEmptyDataset(
                    manager.findByKey(Arrays.copyOfRange(rootKeys, 0, 10))));
        assertEquals("Deleting previously deleted Entities yielded an error",
                     null, data6);
    }

    /* TEST FIND BY QUERY */
    public void test_findByQuery_equality() {
        manager.insert("president", sampleData);

        // Query with one result.
        Dataset result1 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(FIRSTN, "=", "Barack"));
        checkRecordIntegrity(result1.getDataset("record"), sampleData[44]);

        // Query with no results.
        Dataset result2 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(FIRSTN, "=", "Barack"),
                new GaeDataManager.QueryTerm(LASTN, "=", "Bush"));
        assertTrue("Result of Query with no results", isEmptyDataset(result2));

        // Query with more than one result.
        Dataset result3 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(FIRSTN, "=", "John"));
        checkRecordsIntegrity(result3, new Dataset[] {sampleData[0],
                sampleData[2], sampleData[6], sampleData[10], sampleData[35]},
                sampleDataComp);

        // Query on an attribute that can have multiple values.
        Dataset result4 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(MI, "=", "H"),
                new GaeDataManager.QueryTerm(MI, "=", "W"));
        checkRecordIntegrity(result4.getDataset("record"), sampleData[41]);
    }

    public void test_findByQuery_inequality() {
        manager.insert("president", sampleData);

        // Query with inequality on a String value.
        Dataset result1 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(FIRSTN, ">=", "Woodrow"));
        checkRecordsIntegrity(result1, new Dataset[] {sampleData[12],
                sampleData[28]}, sampleDataComp);

        // Query with inequality on a Number value.
        Dataset result2 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(YEAR, "<", 1800));
        checkRecordsIntegrity(result2, new Dataset[] {sampleData[1],
                sampleData[2]}, sampleDataComp);

        // Query with an inequality and an equality.
        Dataset result3 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(YEAR, "<", 1850),
                new GaeDataManager.QueryTerm(LASTN, "=", "Harrison"));
        checkRecordIntegrity(result3.getDataset("record"), sampleData[9]);

        // Query with multiple inequalities on the same attribute.
        Dataset result4 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(YEAR, ">", 1930),
                new GaeDataManager.QueryTerm(YEAR, "<=", 1950));
        checkRecordsIntegrity(result4, new Dataset[] {sampleData[32],
                sampleData[33]}, sampleDataComp);

        // Query with multiple inequalities on different attributes (fails).
        Dataset data5 = manager.findByQuery("president",
                    new GaeDataManager.QueryTerm(LASTN, "<", "Rocky"),
                    new GaeDataManager.QueryTerm(FIRSTN, ">", "Bullwinkle"));
        assertEquals("Error when querying with multiple inequalities on same " +
                     "attribute","Invalid, incomplete, or malformed argument " +
                     "of type: QueryArgument", data5.getErrorMessage());

        // Query on an attribute that doesn't exist in some entities.
        Dataset result6 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(MI, "<", "D"));
        checkRecordsIntegrity(result6, new Dataset[] {sampleData[19],
                sampleData[21], sampleData[36]}, sampleDataComp);
    }

    public void test_findByQuery_sorted() {
        manager.insert("president", sampleData);

        // Query sorted without specifying direction (ascending).
        Dataset request1 = manager.findByQuery("president",
                new GaeDataManager.QuerySort(ID));
        checkOrderedRecordsIntegrity(request1, sampleData);

        // Query sorted with a specified direction.
        Dataset request2 = manager.findByQuery("president",
                new GaeDataManager.QuerySort(ID, "desc"));
        Dataset[] reverseSampleData = new Dataset[sampleData.length];
        for (int i=0; i<sampleData.length; i++) {
            reverseSampleData[i] = sampleData[sampleData.length - i - 1];
        }
        checkOrderedRecordsIntegrity(request2, reverseSampleData);

        // Query sorted with an inequality filter (correctly).
        Dataset request3 = manager.findByQuery("president",
                new GaeDataManager.QueryTerm(LASTN, "<", "C"),
                new GaeDataManager.QuerySort(LASTN),
                new GaeDataManager.QuerySort(YEAR, "desc"));
        checkOrderedRecordsIntegrity(request3, new Dataset[] {sampleData[6],
                sampleData[2], sampleData[21], sampleData[15], sampleData[43],
                sampleData[41]});

        // Query sorted with an inequality filter (badly-ordered sorts).
        Dataset data4 = manager.findByQuery("president",
                    new GaeDataManager.QueryTerm(LASTN, "<", "C"),
                    new GaeDataManager.QuerySort(YEAR, "desc"),
                    new GaeDataManager.QuerySort(LASTN));
        assertEquals("Sort on an inequality must come first",
                     "Invalid, incomplete, or malformed argument of type: " +
                     "QueryArgument", data4.getErrorMessage());

        // Query sorted with an inequality filter (on different attributes).
        Dataset data5 = manager.findByQuery("president",
                    new GaeDataManager.QueryTerm(FIRSTN, ">", "X"),
                    new GaeDataManager.QuerySort(LASTN));
        assertEquals("If sorted, must sort on inequality-filtered attrs",
                     "Invalid, incomplete, or malformed argument of type: " +
                     "QueryArgument", data5.getErrorMessage());

        manager.insert("president2", Arrays.copyOfRange(sampleData, 41, 44));

        // Queries sorted on an attribute with multiple values.
        Dataset request6 = manager.findByQuery("president2",
                new GaeDataManager.QuerySort(MI, "asc"));
        checkOrderedRecordsIntegrity(request6, new Dataset[] {sampleData[41],
                sampleData[42], sampleData[43]});
        Dataset request7 = manager.findByQuery("president2",
                new GaeDataManager.QueryTerm(MI, "=", "W"),
                new GaeDataManager.QuerySort(MI, "asc"),
                new GaeDataManager.QuerySort(ID, "desc"));
        checkOrderedRecordsIntegrity(request7, new Dataset[] {sampleData[43],
                sampleData[41]});
        Dataset request8 = manager.findByQuery("president2",
                new GaeDataManager.QuerySort(MI, "desc"),
                new GaeDataManager.QuerySort(ID));
        checkOrderedRecordsIntegrity(request8, new Dataset[] {sampleData[41],
                sampleData[43], sampleData[42]});
    }

    public void test_findByQuery() {
        Key parentKey = getKeys(manager.insert("parent", sampleData[0]))[0];
        manager.insert(parentKey, "president", sampleData);

        // Just checking output format - more detailed tests on QueryRequest.
        checkRecordsIntegrity(manager.findByQuery("president"), sampleData,
                sampleDataComp);
        checkRecordsIntegrity(manager.findByQuery(parentKey, "president"),
                sampleData, sampleDataComp);
        Transaction txn = manager.getNewTransaction();
        checkRecordsIntegrity(manager.findByQuery(txn, parentKey, "president"),
                sampleData, sampleDataComp);
        txn.commit();
    }

    public void test_findMappingByQuery() {
        Key parentKey = getKeys(manager.insert("parent", sampleData[0]))[0];
        Key[] keys = getKeys(manager.insert(parentKey, "president", sampleData));

        // Just checking output format - more detailed tests on QueryRequest.
        checkMappedRecordsIntegrity(manager.findMappingByQuery("president"),
                sampleData, keys);
        checkMappedRecordsIntegrity(manager.findMappingByQuery(parentKey,
                "president"), sampleData, keys);
        Transaction txn = manager.getNewTransaction();
        checkMappedRecordsIntegrity(manager.findMappingByQuery(txn, parentKey,
                "president"), sampleData, keys);
        txn.commit();
    }

    public void test_findKeyByQuery() {
        Key parentKey = getKeys(manager.insert("parent", sampleData[0]))[0];
        Key[] keys = getKeys(manager.insert(parentKey, "president", sampleData));

        // Just checking output format - more detailed tests on QueryRequest.
        checkKeyRecordsIntegrity(manager.findKeyByQuery("president"), keys);
        checkKeyRecordsIntegrity(manager.findKeyByQuery(parentKey, "president"),
                keys);
        Transaction txn = manager.getNewTransaction();
        checkKeyRecordsIntegrity(manager.findKeyByQuery(txn, parentKey,
                "president"), keys);
        txn.commit();
    }

    public void test_handleFindByQuery() {
        manager.insert("president", sampleData);

        // Just checking output format - more detailed tests on QueryRequest.
        checkRecordsIntegrity(manager.handleFindQuery(null, null, "president",
                false, false, new GaeDataManager.QueryTerm(ID, ">=", 20)),
                Arrays.copyOfRange(sampleData, 20, sampleData.length),
                sampleDataComp);
    }

    /* TEST FIND BY ANCESTOR */
    // NOTE: There is a known problem with the AppEngine development
    // environment, in which ancestor queries do not work.  Until that is
    // fixed, none of the findByAncestor tests below will work.
    /*
    public void test_findByAncestor() {
        Key [] keys = createKeyAncestryStructure();

        // Check that ancestor queries retrieve the right child Entities.
        checkOrderedRecordsIntegrity(manager.findByAncestor(keys[0]),
                Arrays.copyOfRange(sampleData, 0, 7));
        checkOrderedRecordsIntegrity(manager.findByAncestor(keys[1]),
                new Dataset[] {sampleData[1], sampleData[3], sampleData[4]});
        checkOrderedRecordsIntegrity(manager.findByAncestor(keys[2]),
                new Dataset[] {sampleData[2], sampleData[5], sampleData[6]});
        checkRecordIntegrity(manager.findByAncestor(keys[4]).getDataset(
                "record"), sampleData[4]);
        // With Transaction.
        Transaction txn = manager.getNewTransaction();
        checkOrderedRecordsIntegrity(manager.findByAncestor(txn, keys[0]),
                Arrays.copyOfRange(sampleData, 0, 7));
        txn.commit();
    }

    public void test_findMappingByAncestor() {
        Key [] keys = createKeyAncestryStructure();
        Dataset[] reducedSampleData = Arrays.copyOfRange(sampleData, 0, 7);

        // Without Transaction.
        checkMappedRecordsIntegrity(manager.findMappingByAncestor(keys[0]),
                reducedSampleData, keys);
        // With Transaction.
        Transaction txn = manager.getNewTransaction();
        checkMappedRecordsIntegrity(manager.findByAncestor(txn, keys[0]),
                reducedSampleData, keys);
        txn.commit();
    }

    public void test_findKeyByAncestor() {
        Key[] keys = createKeyAncestryStructure();

        // Without Transaction.
        checkKeyRecordsIntegrity(manager.findKeyByAncestor(keys[0]), keys);
        // With Transaction.
        Transaction txn = manager.getNewTransaction();
        checkKeyRecordsIntegrity(manager.findKeyByAncestor(txn, keys[0]), keys);
        txn.commit();
    }

    public void test_handleFindAncestor() {
        Key [] keys = createKeyAncestryStructure();
        checkOrderedRecordsIntegrity(manager.handleFindAncestor(null, false,
                false, keys[0]), Arrays.copyOfRange(sampleData, 0, 7));
    }

    protected Key[] createKeyAncestryStructure() {
        Key k0 = getKeys(manager.insert("0", sampleData[0]))[0];
        Key k1 = getKeys(manager.insert(k0, "1", sampleData[1]))[0];
        Key k2 = getKeys(manager.insert(k0, "2", sampleData[2]))[0];
        Key k3 = getKeys(manager.insert(k1, "3", sampleData[3]))[0];
        Key k4 = getKeys(manager.insert(k1, "4", sampleData[4]))[0];
        Key k5 = getKeys(manager.insert(k2, "5", sampleData[5]))[0];
        Key k6 = getKeys(manager.insert(k2, "6", sampleData[6]))[0];
        return new Key[] {k0, k1, k2, k3, k4, k5, k6};
    }
    */

    public void test_handleQuery() {
        Key[] rootKeys = getKeys(manager.insert("president",
                Arrays.copyOfRange(sampleData, 0, 20)));
        getKeys(manager.insert(rootKeys[1], "president",
                Arrays.copyOfRange(sampleData, 20, sampleData.length)));

        // No Transaction.
        Dataset request1 = manager.handleQuery(null, false, false,
                    new Query("president"));
        checkRecordsIntegrity(request1, sampleData, sampleDataComp);

        // Transaction (error - multiple Entity groups).
        Transaction txn2 = manager.getNewTransaction();
        Dataset data2 = manager.handleQuery(txn2, false, false, new Query("president"));
        txn2.rollback();
        assertEquals("Query across multiple Entity groups yields an error",
                     "Invalid, incomplete, or malformed argument of type: " +
                     "QueryArgument", data2.getErrorMessage());

        // Transaction (no error).
        Transaction txn3 = manager.getNewTransaction();
        Dataset request3 = manager.handleQuery(txn3, false, false,
                    new Query("president", rootKeys[1]).addFilter(
                            YEAR, Query.FilterOperator.GREATER_THAN, 1800));
        txn3.commit();
        checkRecordsIntegrity(request3, Arrays.copyOfRange(
                sampleData, 20, sampleData.length), sampleDataComp);

        // Inactive Transaction.
        Transaction txn4 = manager.getNewTransaction();
        txn4.commit();
        Dataset data4 = manager.handleQuery(txn4, false, false, new Query("president"));
        assertEquals("Query with an inactive Transaction yields an error",
                     "Invalid, incomplete, or malformed argument of type: " +
                     "QueryArgument", data4.getErrorMessage());

        // Key mapping.
        Dataset request5 = manager.handleQuery(null, true, false,
                    new Query("president").addFilter(ID,
                            Query.FilterOperator.LESS_THAN, 20));
        checkMappedRecordsIntegrity(request5,
                Arrays.copyOfRange(sampleData, 0, 20), rootKeys);

        // Keys-only records.
        Dataset request6 = manager.handleQuery(null, false, true,
                    new Query("president").addFilter(ID,
                            Query.FilterOperator.LESS_THAN, 20));
        checkKeyRecordsIntegrity(extractKeys(request6), rootKeys);
    }

    /* TEST ENTITY - DATASET CONVERSION */
    public void test_entityToDataset() {
        Entity entity = new Entity("president");
        entity.setProperty("a", "1");
        entity.setProperty("b", 2);
        entity.setProperty("c", Arrays.asList("x", "y", "z"));

        Dataset dataset = GaeDataManager.entityToDataset(entity);
        assertEquals("Converted attribute a", "1", dataset.getString("a"));
        assertEquals("Converted attribute b", 2, dataset.getInt("b"));
        assertEquals("Converted attribute c", Arrays.asList("x", "y", "z"),
                dataset.getStringList("c"));
    }

    public void test_datasetToEntity_noKeyInDataset() {
        Entity entity1 = GaeDataManager.datasetToEntity("prez", sampleData[41]);
        assertEquals("Converted attribute " + ID, 41, entity1.getProperty(ID));
        assertEquals("Converted attribute " + YEAR, 1989,
                entity1.getProperty(YEAR));
        assertEquals("Converted attribute " + FIRSTN, "George",
                entity1.getProperty(FIRSTN));
        assertEquals("Converted attribute " + MI, Arrays.asList("H", "W"),
                entity1.getProperty(MI));
        assertEquals("Converted attribute " + LASTN, "Bush",
                entity1.getProperty(LASTN));
        assertEquals("Converted Kind", "prez", entity1.getKind());

        // Only bother testing converted properties that differ from above.
        Key key = manager.allocateKeys("prez", 1)[0];
        Entity entity2 = GaeDataManager.datasetToEntity(key, "prez",
                sampleData[41]);
        assertEquals("Converted parent Key", key, entity2.getKey().getParent());
    }

    public void test_datasetToEntity_keyInDataset() throws Exception {
        Key key = manager.allocateKeys("president", 1)[0];

        // Key correctly in Dataset.
        sampleData[1].set(GaeDataManager.KEY_PROPERTY, key);
        Entity entity1 = GaeDataManager.datasetToEntity(sampleData[1]);
        assertEquals("Converted Key", key, entity1.getKey());

        // String-ified Key correctly in Dataset.
        sampleData[2].set(GaeDataManager.KEY_PROPERTY,
                GaeDataManager.keyToString(key));
        Entity entity2 = GaeDataManager.datasetToEntity(sampleData[2]);
        assertEquals("Converted Key", key, entity2.getKey());

        // Inappropriate value set as Key.
        sampleData[3].set(GaeDataManager.KEY_PROPERTY, 3);
        try {
            GaeDataManager.datasetToEntity(sampleData[3]);
            fail("Exception not thrown");
        } catch (GaeDataManager.GaeException e) {
            assertEquals("entity3 error message", "Key property (_key_) is not" +
                         " of type Key or String: java.lang.Integer",
                         e.getMessage());
        }

        // No value set as Key.
        try {
            GaeDataManager.datasetToEntity(sampleData[4]);
            fail("Exception not thrown");
        } catch (GaeDataManager.GaeException e) {
            assertEquals("entity3 error message", "Dataset must contain a Key " +
                         "attribute _key_", e.getMessage());
        }
    }

    public void test_fillEntity() {
        Dataset dataset = new Dataset(GaeDataManager.KEY_PROPERTY, "key",
                "a", "1", "b", 2, "b", 3);
        Entity entity = new Entity("test");

        GaeDataManager.fillEntity(dataset, entity);
        assertFalse("Converted Key attribute",
                entity.hasProperty(GaeDataManager.KEY_PROPERTY));
        assertEquals("Converted attribute a", "1", entity.getProperty("a"));
        assertEquals("Converted attribute b", Arrays.asList(2, 3),
                entity.getProperty("b"));
    }

    public void test_stringToKey() {
        Key key = manager.allocateKeys("president", 1)[0];
        String goodKey = GaeDataManager.keyToString(key);
        String badKey = key.toString();

        // Convert a good Key.
        Key converted = GaeDataManager.stringToKey(goodKey);
        assertEquals("Correct, lossless conversion", key, converted);

        // Convert a bad Key.
        converted = GaeDataManager.stringToKey(badKey);
        assertEquals("Bad key is null", null, converted);
    }

    public void test_addErrorData() {
        Dataset data0 = GaeDataManager.addErrorData(null,
                GaeDataManager.ErrType.BAD_ARG, "oops", null);
        assertEquals("null arguments", "Invalid, incomplete, or malformed " +
                     "argument of type: oops", data0.getErrorMessage());

        Dataset data1 = new Dataset();
        GaeDataManager.addErrorData(data1,
                GaeDataManager.ErrType.BAD_ARG, "oops1", null);
        assertEquals("null arguments", "Invalid, incomplete, or malformed " +
                     "argument of type: oops1", data1.getErrorMessage());

        Exception cause = new Exception("oops!");
        Dataset data2 = GaeDataManager.addErrorData(null,
                GaeDataManager.ErrType.BAD_ARG, "oops2", cause);
        assertEquals("with cause", cause, data2.getErrorData()[0].get("cause"));

        Dataset data3 = GaeDataManager.addErrorData(null,
                GaeDataManager.ErrType.DS_FAILURE, "oops3", null);
        assertEquals("ds_failure", "Datastore failure. oops3",
                     data3.getErrorMessage());

        Dataset data4 = GaeDataManager.addErrorData(null,
                GaeDataManager.ErrType.TXN_INACTIVE, "oops4", null);
        assertEquals("txn_inactive", "Transaction is inactive. oops4",
                     data4.getErrorMessage());

        Dataset data5 = GaeDataManager.addErrorData(null,
                GaeDataManager.ErrType.BAD_ARG, "oops5", null);
        assertEquals("bad_arg", "Invalid, incomplete, or malformed argument of type: oops5",
                     data5.getErrorMessage());
    }

    /* HELPER METHODS */

    // Extract an array of Keys from the provided Dataset
    protected Key[] extractKeys(Dataset request) {
        return request.getList("record").toArray(new Key[0]);
    }

    // Returns whether or not the provided Dataset is empty.
    protected boolean isEmptyDataset(Dataset dataset) {
        return dataset.keySet().isEmpty();
    }

    // Make sure the passed Key arrays are equal.
    protected void checkKeyRecordsIntegrity(Key[] recordKeys, Key[] refKeys) {
        assertEquals("Correct number of keys found", refKeys.length,
                recordKeys.length);
        for (int i=0; i<refKeys.length; i++) {
            assertEquals("Correct key found (" + i + ")", refKeys[i],
                    recordKeys[i]);
        }
    }

    // Make sure the mapped records results align with the provided reference
    // records and keys.
    protected void checkMappedRecordsIntegrity(Dataset records,
            Dataset[] reference, Key[] keys) {
        assertTrue("No nested record Datasets", !records.containsKey("record"));
        assertEquals("Record count", reference.length,
                records.keySet().size());
        for (int i=0; i<reference.length; i++) {
            String keyString = GaeDataManager.keyToString(keys[i]);
            checkRecordIntegrity(records.getDataset(keyString), reference[i]);
        }
    }

    // Make sure an unordered record result Dataset aligns with the provided
    // reference records.
    protected void checkRecordsIntegrity(Dataset records, Dataset[] reference,
            DatasetComparator comparator) {
        checkRecordsIntegrity(records, reference, comparator, false);
    }
    protected void checkRecordsIntegrity(Dataset records, Dataset[] reference,
            DatasetComparator comparator, boolean keepKey) {
        Dataset[] recordArr = records.getList("record").toArray(new Dataset[0]);
        Arrays.sort(recordArr, comparator);
        Arrays.sort(reference, comparator);
        assertEquals("Record count", reference.length, recordArr.length);
        for (int i=0; i<reference.length; i++) {
            checkRecordIntegrity(recordArr[i], reference[i], keepKey);
        }
    }

    // Make sure an ordered record result Dataset aligns with the provided
    // reference records.
    protected void checkOrderedRecordsIntegrity(Dataset records,
            Dataset[] reference) {
        checkOrderedRecordsIntegrity(records, reference, false);
    }
    protected void checkOrderedRecordsIntegrity(Dataset records,
            Dataset[] reference, boolean keepKey) {
        Dataset[] recordArr = records.getList("record").toArray(new Dataset[0]);
        assertEquals("Record count", reference.length, recordArr.length);
        for (int i=0; i<reference.length; i++) {
            checkRecordIntegrity(recordArr[i], reference[i], keepKey);
        }
    }

    // Make sure a single Dataset record has the same fields as the provided
    // reference record.
    protected void checkRecordIntegrity(Dataset record, Dataset reference) {
        checkRecordIntegrity(record, reference, false);
    }
    protected void checkRecordIntegrity(Dataset record, Dataset reference,
            boolean keepKey) {
        Set<String> referenceKeys = reference.keySet();
        Set<String> recordKeys = new HashSet<String>();
        recordKeys.addAll(record.keySet());
        if (!keepKey) {
            assertTrue("Record contains a Datastore Key",
                    recordKeys.remove(GaeDataManager.KEY_PROPERTY));
        }
        assertEquals("Record contains all expected keys", referenceKeys,
                recordKeys);
        for (String key : recordKeys) {
            Object[] refValues = reference.getList(key).toArray();
            Object[] recValues = record.getList(key).toArray();
            assertEquals("Record key (" + key + ") value count",
                    refValues.length, recValues.length);
            Arrays.sort(refValues);
            Arrays.sort(recValues);
            for (int i=0; i<recValues.length; i++) {
                checkObjectIntegrity(recValues[i], refValues[i], key);
            }
        }
    }

    // Makes sure that two record values are equal, accounting for the fact
    // that the Datastore stores numbers differently.
    protected void checkObjectIntegrity(Object recObj, Object refObj,
            String key) {
        if (refObj instanceof Integer || refObj instanceof Short) {
            refObj = ((Number)refObj).longValue();
        } else if (refObj instanceof Float) {
            refObj = ((Float)refObj).doubleValue();
        }
        assertEquals("Record integrity (" + key + ")", refObj, recObj);
    }

    // Comparator class used for sorting and comparing unordered Dataset
    // records.
    protected class DatasetComparator implements Comparator<Dataset> {
        protected String[] compKeys;
        public DatasetComparator(String ... compKeys) {
            this.compKeys = compKeys;
        }
        @SuppressWarnings("unchecked")
        public int compare(Dataset d1, Dataset d2) {
            for (String compKey : compKeys) {
                int val = ((Comparable)d1.get(compKey)).compareTo(
                        d2.get(compKey));
                if (val != 0) return val;
            }
            return 0;
        }
    }

    // Helps create the dummy Datastore environment used for testing.
    protected class TestEnvironment implements ApiProxy.Environment {
        public String getAppId() {
            return "test";
        }
        public String getVersionId() {
            return "1.0";
        }
        public String getEmail() {
            throw new UnsupportedOperationException();
        }
        public boolean isLoggedIn() {
            throw new UnsupportedOperationException();
        }
        public boolean isAdmin() {
            throw new UnsupportedOperationException();
        }
        public String getAuthDomain() {
            throw new UnsupportedOperationException();
        }
        public String getRequestNamespace() {
            return "";
        }
        public Map<String, Object> getAttributes() {
            return new HashMap<String, Object>();
        }
    }

    protected Key[] getKeys(Dataset data) {
        return data.getList("record").toArray(new Key[0]);
    }
}
