package org.fiz;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * Junit tests for the SqlDataManager class.
 */

public class SqlDataManagerTest extends junit.framework.TestCase {
    protected static SqlDataManager manager = null;

    public void setUp() {
        if (manager == null) {
            SqlDataManager.logger.setLevel(Level.FATAL);
            DataManager.logger.setLevel(Level.ERROR);
            Config.init("test/testData/WEB-INF/config");
            manager = (SqlDataManager) DataManager.getDataManager("sql");

            // Initialize the "people" table; individual tests should
            // not modify the contents of this table.
            manager.clearTable("people");
            manager.updateWithSql("ALTER TABLE people AUTO_INCREMENT = 1;");
            Dataset in = YamlDataset.newStringInstance(
                    "record:\n" +
                    "  - first:  Alice\n" +
                    "    last:   Adams\n" +
                    "    state:  California\n" +
                    "    age:    24\n" +
                    "    weight: 115\n" +
                    "  - first:  Bob\n" +
                    "    last:   Brennan\n" +
                    "    state:  New York\n" +
                    "    age:    18\n" +
                    "    weight: 190\n" +
                    "  - first:  Carol\n" +
                    "    last:   Collins\n" +
                    "    state:  California\n" +
                    "    age:    32\n" +
                    "    weight: 130\n");
            for (Dataset row : in.getChildren("record")) {
                manager.insert("people", row);
            }
        }
    }

    // Utility method to close the current database connection.
    protected void closeConnection() {
        try {
            manager.connection.close();
            manager.reopens = 0;
        }
        catch (SQLException e) {
            // Ignore exceptions.
        }
    }

    public void test_sanityCheck() {
        Dataset out = manager.findWithSql("SELECT * FROM people;");
        assertEquals("retrieved rows",
                "record:\n" +
                "  - age:    24\n" +
                "    first:  Alice\n" +
                "    id:     1\n" +
                "    last:   Adams\n" +
                "    state:  California\n" +
                "    weight: 115\n" +
                "  - age:    18\n" +
                "    first:  Bob\n" +
                "    id:     2\n" +
                "    last:   Brennan\n" +
                "    state:  New York\n" +
                "    weight: 190\n" +
                "  - age:    32\n" +
                "    first:  Carol\n" +
                "    id:     3\n" +
                "    last:   Collins\n" +
                "    state:  California\n" +
                "    weight: 130\n", out.toString());
    }

    public void test_constructor_badClass() throws SQLException {
        boolean gotException = false;
        try {
            new SqlDataManager(new Dataset("serverUrl", "urlabc",
                    "user", "test", "password", "test",
                    "driverClass", "com.bogus.missing"));
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "SqlDataManager couldn't load driver class " +
                    "\"com.bogus.missing\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_constructor_cantConnectToServer() throws SQLException {
        boolean gotException = false;
        try {
            new SqlDataManager(new Dataset("driverClass",
                    "com.mysql.jdbc.Driver", "serverUrl",
                    "jdbc:mysql://localhost:3307/bogus",
                    "user", "u1", "password", "p1"));
        }
        catch (InternalError e) {
            TestUtil.assertSubstring("first part of exception message",
                    "SqlDataManager couldn't create connection with server " +
                    "at \"jdbc:mysql://localhost:3307/bogus\":",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_clearTable() {
        manager.insert("states", new Dataset("name", "Virginia"));
        manager.clearTable("states");
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "", out.toString());
    }
    public void test_clearTable_fatalError() {
        boolean gotException = false;
        try {
            manager.clearTable("bogus");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in clearTable: table 'test.bogus' " +
                    "doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_clearTable_retriableError() {
        manager.insert("states", new Dataset("name", "Virginia"));
        closeConnection();
        manager.clearTable("states");
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_delete() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        manager.insert("states", new Dataset("name", "California",
                "capital", "Palo Alto", "id", "443"));
        manager.insert("states", new Dataset("name", "Colorado",
                "capital", "Denver", "id", "999"));
        manager.delete("states", "name", "California");
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("table after deletion",
                "record:\n" +
                "    capital: Denver\n" +
                "    name:    Colorado\n", out.toString());
    }
    public void test_delete_fatalError() {
        boolean gotException = false;
        try {
            manager.delete("bogus", "first", "Alice");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in delete: table 'test.bogus' doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_delete_retriableError() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        manager.insert("states", new Dataset("name", "California",
                "capital", "Palo Alto", "id", "443"));
        manager.insert("states", new Dataset("name", "Colorado",
                "capital", "Denver", "id", "999"));
        closeConnection();
        manager.delete("states", "name", "California");
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("table after deletion",
                "record:\n" +
                "    capital: Denver\n" +
                "    name:    Colorado\n", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_find() {
        Dataset out = manager.find("people", "state", "California");
        assertEquals("retrieved rows", "record:\n" +
                "  - age:    24\n" +
                "    first:  Alice\n" +
                "    id:     1\n" +
                "    last:   Adams\n" +
                "    state:  California\n" +
                "    weight: 115\n" +
                "  - age:    32\n" +
                "    first:  Carol\n" +
                "    id:     3\n" +
                "    last:   Collins\n" +
                "    state:  California\n" +
                "    weight: 130\n", out.toString());
    }
    public void test_find_fatalError() {
        boolean gotException = false;
        try {
            Dataset out = manager.find("bogus", "first", "Alice");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in find: table 'test.bogus' doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_find_retriableError() {
        closeConnection();
        Dataset out = manager.find("people", "first", "Alice");
        assertEquals("retrieved rows", "record:\n" +
                "    age:    24\n" +
                "    first:  Alice\n" +
                "    id:     1\n" +
                "    last:   Adams\n" +
                "    state:  California\n" +
                "    weight: 115\n", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_findWithSql() {
        Dataset out = manager.findWithSql("SELECT first, last FROM people " +
                "WHERE state = 'California';");
        assertEquals("retrieved rows", "record:\n" +
                "  - first: Alice\n" +
                "    last:  Adams\n" +
                "  - first: Carol\n" +
                "    last:  Collins\n", out.toString());
    }
    public void test_findWithSql_fatalError() {
        boolean gotException = false;
        try {
            manager.findWithSql("SELECT * FROM bogusTable;");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in findWithSql: table 'test.bogustable' doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findWithSql_retriableError() {
        closeConnection();
        Dataset out = manager.findWithSql("SELECT first, last FROM people " +
                "WHERE state = 'California';");
        assertEquals("retrieved rows", "record:\n" +
                "  - first: Alice\n" +
                "    last:  Adams\n" +
                "  - first: Carol\n" +
                "    last:  Collins\n", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_findWithSql_withTemplate() {
        Dataset out = manager.findWithSql("SELECT first, last FROM people " +
                "WHERE state = @state AND first = @first;",
                new Dataset("table", "people", "state", "California",
                "first", "Alice"));
        assertEquals("retrieved rows", "record:\n" +
                "    first: Alice\n" +
                "    last:  Adams\n", out.toString());
    }
    public void test_findWithSql_withTemplate_retriableError() {
        closeConnection();
        Dataset out = manager.findWithSql("SELECT first, last FROM people " +
                "WHERE state = @state AND first = @first;",
                new Dataset("table", "people", "state", "California",
                "first", "Alice"));
        assertEquals("retrieved rows", "record:\n" +
                "    first: Alice\n" +
                "    last:  Adams\n", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_insert() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442",
                "bogus", "ignore this"));
        manager.insert("states", new Dataset("name", "Colorado"));
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("retrieved rows",
                "record:\n" +
                "  - capital: Sacramento\n" +
                "    name:    California\n" +
                "  - capital: \"\"\n" +
                "    name:    Colorado\n", out.toString());
    }
    public void test_insert_fatalError() {
        boolean gotException = false;
        try {
            manager.insert("people", new Dataset("id", "1"));
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in insert: duplicate entry '1' for key 1",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_insert_retriableError() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442",
                "bogus", "ignore this"));
        closeConnection();
        manager.insert("states", new Dataset("name", "Colorado"));
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("retrieved rows",
                "record:\n" +
                "  - capital: Sacramento\n" +
                "    name:    California\n" +
                "  - capital: \"\"\n" +
                "    name:    Colorado\n", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_startRequests_multipleRequests() {
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(new DataRequest(new Dataset(
                "manager", "sql",
                "request", "findWithSql",
                "sql", "SELECT first FROM people WHERE id = 1;")));
        requests.add(new DataRequest(new Dataset(
                "manager", "sql",
                "request", "findWithSql",
                "sql", "SELECT first FROM people WHERE id = 2;")));
        DataRequest.start(requests);
        assertEquals("first response",
                "record:\n" +
                "    first: Alice\n",
                requests.get(0).getResponseData().toString());
        assertEquals("second response",
                "record:\n" +
                "    first: Bob\n",
                requests.get(1).getResponseData().toString());
    }
    public void test_startRequests_clearTable() {
        manager.insert("states", new Dataset("name", "Alaska",
                "capital", "Juneau"));
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "clearTable",
                "table", "states"));
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "", out.toString());
    }
    public void test_startRequests_clearTable_error() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "clearTable",
                "table", "bogusTable"));
        assertEquals("error message",
                "SQL error in SqlDataManager \"clearTable\" request: " +
                "table 'test.bogustable' doesn't exist",
                request.getErrorMessage());
    }
    public void test_startRequests_delete() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        manager.insert("states", new Dataset("name", "California",
                "capital", "Palo Alto", "id", "443"));
        manager.insert("states", new Dataset("name", "Colorado",
                "capital", "Denver", "id", "999"));
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql", "request", "delete", "table", "states",
                "column", "name", "value", "California"));
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("table after deletion",
                "record:\n" +
                "    capital: Denver\n" +
                "    name:    Colorado\n", out.toString());
    }
    public void test_startRequests_find() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "find",
                "table", "people",
                "column", "state",
                "value", "California"));
        assertEquals("response",
                "record:\n" +
                "  - age:    24\n" +
                "    first:  Alice\n" +
                "    id:     1\n" +
                "    last:   Adams\n" +
                "    state:  California\n" +
                "    weight: 115\n" +
                "  - age:    32\n" +
                "    first:  Carol\n" +
                "    id:     3\n" +
                "    last:   Collins\n" +
                "    state:  California\n" +
                "    weight: 130\n",
                request.getResponseData().toString());
    }
    public void test_startRequests_findWithSql() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "findWithSql",
                "sql", "SELECT last FROM people WHERE state = 'California';"));
        assertEquals("response",
                "record:\n" +
                "  - last: Adams\n" +
                "  - last: Collins\n",
                request.getResponseData().toString());
    }
    public void test_startRequests_findWithSql_withTemplate() {
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager:    sql\n" +
                "request:    findWithSql\n" +
                "sql:        \"SELECT first FROM people WHERE state = @state;\"\n" +
                "queryData:\n" +
                "    state:  California\n"));
        assertEquals("response",
                "record:\n" +
                "  - first: Alice\n" +
                "  - first: Carol\n",
                request.getResponseData().toString());
    }
    public void test_startRequests_insert_noRecordDataset() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "insert",
                "table", "states",
                "name", "Illinois",
                "capital", "Springfield"));
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.find("states", "name", "Illinois");
        assertEquals("retrieved rows", "record:\n" +
                "    capital: Springfield\n" +
                "    id:      \"\"\n" +
                "    name:    Illinois\n", out.toString());
    }
    public void test_startRequests_insert_multipleRecords() {
        manager.clearTable("states");
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager:    sql\n" +
                "request:    insert\n" +
                "table:      states\n" +
                "record:\n" +
                "  - name:     Michigan\n" +
                "    capital:  Lansing\n" +
                "  - name:     Ohio\n" +
                "    capital:  Columbus\n" +
                "    id:       50\n"));
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "record:\n" +
                "  - capital: Lansing\n" +
                "    id:      \"\"\n" +
                "    name:    Michigan\n" +
                "  - capital: Columbus\n" +
                "    id:      50\n" +
                "    name:    Ohio\n", out.toString());
    }
    public void test_startRequests_update_noRecordDataset() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "update",
                "table", "states",
                "column", "name",
                "value", "California",
                "name", "Michigan",
                "capital", "Lansing"));
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "record:\n" +
                "    capital: Lansing\n" +
                "    id:      442\n" +
                "    name:    Michigan\n", out.toString());
    }
    public void test_startRequests_update_withRecord() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager:    sql\n" +
                "request:    update\n" +
                "table:      states\n" +
                "column:     name\n" +
                "value:      California\n" +
                "record:\n" +
                "  - name:     Michigan\n" +
                "    id:       991\n"));
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "record:\n" +
                "    capital: Sacramento\n" +
                "    id:      991\n" +
                "    name:    Michigan\n", out.toString());
    }
    public void test_startRequests_updateWithSql() {
        manager.clearTable("states");
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "updateWithSql",
                "sql", "INSERT INTO states (name, capital) " +
                "VALUES ('Texas', 'Austin');"));
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "record:\n" +
                "    capital: Austin\n" +
                "    id:      \"\"\n" +
                "    name:    Texas\n", out.toString());
    }
    public void test_startRequests_updateWithSql_withTemplate() {
        manager.clearTable("states");
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager:    sql\n" +
                "request:    updateWithSql\n" +
                "sql:        \"INSERT INTO states (name, capital) " +
                "VALUES (@name, @capital);\"\n" +
                "queryData:\n" +
                "  name:     Colorado\n" +
                "  capital:  Denver\n"));
        assertEquals("error message", null, request.getErrorMessage());
        assertEquals("response", "",
                request.getResponseData().toString());
        Dataset out = manager.findWithSql("SELECT * FROM states;");
        assertEquals("retrieved rows", "record:\n" +
                "    capital: Denver\n" +
                "    id:      \"\"\n" +
                "    name:    Colorado\n", out.toString());
    }
    public void test_startRequests_bogusRequestName() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "bogusRequest"));
        request.start();
        assertEquals("error message",
                "unknown request \"bogusRequest\" for " +
                "SqlDataManager; must be clearTable, delete, find, " +
                "findWithSql, insert, update, or updateWithSql",
                request.getErrorMessage());
    }
    public void test_startRequests_MissingValueError() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql"));
        request.start();
        assertEquals("error dataset",
                "message: SqlDataManager request didn't contain " +
                "required parameter \"request\"\n",
                request.getErrorData()[0].toString());
    }
    public void test_startRequests_SqlError() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql", "request", "findWithSql",
                "sql", "SELECT * FROM bogusTable"));
        request.start();
        assertEquals("error message",
                "SQL error in SqlDataManager \"findWithSql\" request: " +
                "table 'test.bogustable' doesn't exist",
                request.getErrorData()[0].check("message"));
    }

    public void test_update() {
        manager.clearTable("states");
        manager.updateWithSql(
                "INSERT INTO states (name, capital) " +
                "VALUES ('Virginia', 'Richmond');",
                "INSERT INTO states (name, capital) " +
                "VALUES ('California', 'Sacramento');");
        manager.update ("states", "capital", "Sacramento", new Dataset(
                "capital", "Berkeley", "bogus1", "99"));
        Dataset out = manager.findWithSql("SELECT name, capital " +
                "FROM states WHERE name = 'California';");
        assertEquals("modified row", "record:\n" +
                "    capital: Berkeley\n" +
                "    name:    California\n", out.toString());
    }
    public void test_update_fatalError() {
        boolean gotException = false;
        try {
            manager.update ("people", "bogus", "bogus", new Dataset(
                    "state", "Mississippi"));
        }
        catch (SqlError e) {
            assertEquals("error message",
                    "SQL error in update: unknown column 'bogus' in " +
                    "'where clause'",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_update_retriableError() {
        manager.clearTable("states");
        manager.updateWithSql(
                "INSERT INTO states (name, capital) " +
                "VALUES ('Virginia', 'Richmond');",
                "INSERT INTO states (name, capital) " +
                "VALUES ('California', 'Sacramento');");
        closeConnection();
        manager.update ("states", "capital", "Sacramento", new Dataset(
                "capital", "Berkeley", "bogus1", "99"));
        Dataset out = manager.findWithSql("SELECT name, capital " +
                "FROM states WHERE name = 'California';");
        assertEquals("modified rows", "record:\n" +
                "    capital: Berkeley\n" +
                "    name:    California\n", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_updateWithSql_multipleUpdates() {
        manager.clearTable("states");
        manager.updateWithSql(
                "INSERT INTO states (name, capital) " +
                "VALUES ('Michigan', 'Lansing');",
                "UPDATE states SET name = 'Illinois' WHERE name = 'Michigan';");
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("retrieved row", "record:\n" +
                "    capital: Lansing\n" +
                "    name:    Illinois\n", out.toString());
    }
    public void test_updateWithSql_fatalError() {
        boolean gotException = false;
        try {
            manager.updateWithSql("UPDATE bogusTable;");
        }
        catch (SqlError e) {
            TestUtil.assertSubstring("first part of exception message",
                    "SQL error in updateWithSql: you have an error in " +
                    "your SQL syntax",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_updateWithSql_multipleUpdates_retriableError() {
        manager.clearTable("states");
        closeConnection();
        manager.updateWithSql(
                "INSERT INTO states (name, capital) " +
                "VALUES ('Michigan', 'Lansing');",
                "UPDATE states SET name = 'Illinois' WHERE name = 'Michigan';");
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("retrieved row", "record:\n" +
                "    capital: Lansing\n" +
                "    name:    Illinois\n", out.toString());
        assertEquals("count of reopens", 1, manager.reopens);
    }

    public void test_updateWithSql_withTemplate() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "Georgia",
                "capital", "Atlanta"));
        manager.updateWithSql(
                "UPDATE states SET name = @state WHERE capital = @capital;",
                new Dataset("state", "West Virginia", "capital", "Atlanta"));
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("retrieved row", "record:\n" +
                "    capital: Atlanta\n" +
                "    name:    West Virginia\n", out.toString());
    }
    public void test_updateWithSql_withTemplate_fatalError() {
        boolean gotException = false;
        try {
            manager.updateWithSql("UPDATE @table;",
                    new Dataset("table", "bogusTable"));
        }
        catch (SqlError e) {
            TestUtil.assertSubstring("first part of exception message",
                    "SQL error in updateWithSql: you have an error in " +
                    "your SQL syntax",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_updateWithSql_withTemplate_retriableError() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "Georgia",
                "capital", "Atlanta"));
        closeConnection();
        manager.updateWithSql(
                "UPDATE states SET name = @state WHERE capital = @capital;",
                new Dataset("state", "West Virginia", "capital", "Atlanta"));
        Dataset out = manager.findWithSql("SELECT name, capital FROM states;");
        assertEquals("retrieved row", "record:\n" +
                "    capital: Atlanta\n" +
                "    name:    West Virginia\n", out.toString());
    }

    @SuppressWarnings("unchecked")
    public void test_collectMetadata() {
        manager.collectMetadata();
        SqlDataManager.TableInfo info = manager.tables.get("people");
        assertEquals("info exists for people table", true, info != null);
        ArrayList names = new ArrayList();
        names.addAll(info.columnNames);
        Collections.sort(names);
        assertEquals("column names for people table",
                "age, first, id, last, state, weight",
                StringUtil.join(names, ", "));

        info = manager.tables.get("states");
        assertEquals("info exists for states table", true, info != null);
        names = new ArrayList();
        names.addAll(info.columnNames);
        Collections.sort(names);
        assertEquals("column names for states table",
                "capital, id, name",
                StringUtil.join(names, ", "));
    }

    public void test_getResults_basics() {
        Dataset out = manager.findWithSql("SELECT first, last, age " +
                "FROM people WHERE state = 'California';");
        assertEquals("retrieved rows", "record:\n" +
                "  - age:   24\n" +
                "    first: Alice\n" +
                "    last:  Adams\n" +
                "  - age:   32\n" +
                "    first: Carol\n" +
                "    last:  Collins\n", out.toString());
    }
    public void test_getResults_duplicatedColumnName() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        Dataset out = manager.findWithSql("SELECT people.id, " +
                "states.id, capital FROM people JOIN states " +
                "WHERE people.state = states.name");
        assertEquals("retrieved rows", "record:\n" +
                "  - capital:   Sacramento\n" +
                "    \"people:id\": 1\n" +
                "    \"states:id\": 442\n" +
                "  - capital:   Sacramento\n" +
                "    \"people:id\": 3\n" +
                "    \"states:id\": 442\n", out.toString());
    }
    public void test_getResults_firstDupHasNoTableName() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        Dataset out = manager.findWithSql(
                "SELECT 'xyzzy' id, id FROM states WHERE name = 'California'");
        assertEquals("retrieved rows", "record:\n" +
                "    id:        xyzzy\n" +
                "    \"states:id\": 442\n", out.toString());
    }
    public void test_getResults_secondDupHasNoTableName() {
        manager.clearTable("states");
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        Dataset out = manager.findWithSql(
                "SELECT id, 'xyzzy' id FROM states WHERE name = 'California'");
        assertEquals("retrieved rows", "record:\n" +
                "    id:        xyzzy\n" +
                "    \"states:id\": 442\n", out.toString());
    }

    public void test_getValidColumns_nonexistentTable() {
        boolean gotException = false;
        try {
            manager.getValidColumns("bogusTable", new Dataset("a", "1"));
        }
        catch (InternalError e) {
            assertEquals("error message",
                    "no database table named \"bogusTable\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getValidColumns_checkColumns() {
        ArrayList<String> names = manager.getValidColumns("people",
                new Dataset("bogus1", "24", "first", "Alice",
                "last", "Brown", "bogus2", "35", "id", "14"));
        Collections.sort(names);
        assertEquals("valid columns", "first, id, last",
                StringUtil.join(names, ", "));
    }

    public void test_handleError_reopenConnection() throws SQLException {
        // Close the connection, then try to reopen it twice: the first
        // time will fail because we changed the user name to something
        // incorrect; the second time it should work.
        closeConnection();
        String oldUser = manager.user;
        manager.user = "bogusUser";
        boolean gotException = false;
        try {
                manager.handleError(new SQLException("fake exception"),
                        "in test");
        }
        catch (InternalError e) {
            assertEquals("error message",
                    "SqlDataManager.handleError couldn't reconnect with " +
                    "server at \"jdbc:mysql://localhost:3306/test\": " +
                    "Access denied for user 'bogusUser'@'localhost' " +
                    "(using password: YES)",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        assertEquals("connection open", true, manager.connection.isClosed());

        // Reset the username and try again..
        manager.user = oldUser;
        manager.handleError(new SQLException("fake exception"),
                "in test");
        assertEquals("connection open", false, manager.connection.isClosed());
    }
}
