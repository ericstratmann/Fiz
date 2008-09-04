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
        manager.insert("states", new Dataset("name", "California",
                "capital", "Sacramento", "id", "442"));
        out = manager.findWithSql("SELECT * FROM states;");
    }
    public void test_clearTable_SqlError() {
        boolean gotException = false;
        try {
            manager.clearTable("bogus");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in clearTable: Table 'test.bogus' " +
                    "doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
    public void test_delete_SqlError() {
        boolean gotException = false;
        try {
            manager.delete("bogus", "first", "Alice");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in delete: Table 'test.bogus' doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
    public void test_find_SqlError() {
        boolean gotException = false;
        try {
            Dataset out = manager.find("bogus", "first", "Alice");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in find: Table 'test.bogus' doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
    public void test_findWithSql_SqlError() {
        boolean gotException = false;
        try {
            manager.findWithSql("SELECT * FROM bogusTable;");
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in findWithSql: Table 'test.bogustable' doesn't exist",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
    public void test_insert_SqlError() {
        boolean gotException = false;
        try {
            manager.insert("people", new Dataset("id", "1"));
        }
        catch (SqlError e) {
            assertEquals("exception message",
                    "SQL error in insert: Duplicate entry '1' for key 1",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
    public void test_startRequests_findWithSqlRequest() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "findWithSql",
                "sql", "SELECT last FROM people WHERE state = 'California';"));
        assertEquals("first response",
                "record:\n" +
                "  - last: Adams\n" +
                "  - last: Collins\n",
                request.getResponseData().toString());
    }
    public void test_startRequests_bogusRequestName() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "sql",
                "request", "bogusRequest"));
        request.start();
        assertEquals("error dataset",
                "message: unknown request \"bogusRequest\" for " +
                "SqlDataManager; must be findWithSql\n",
                request.getErrorData()[0].toString());
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
                "Table 'test.bogustable' doesn't exist",
                request.getErrorData()[0].check("message"));
    }

    public void test_update() {
        manager.updateWithSql(
                "INSERT INTO people (first, last, state, age) " +
                "VALUES ('Edward', 'Eckstein', 'Wyoming', 22);",
                "INSERT INTO people (first, last, state, age) " +
                "VALUES ('Frank', 'Eckstein', 'Montana', 24);");
        manager.update ("people", "last", "Eckstein", new Dataset(
                "state", "Mississippi", "age", "40", "bogus1", "99"));
        Dataset out = manager.findWithSql("SELECT first, last, state, age " +
                "FROM people WHERE last = 'Eckstein';");
        assertEquals("modified rows", "record:\n" +
                "  - age:   40\n" +
                "    first: Edward\n" +
                "    last:  Eckstein\n" +
                "    state: Mississippi\n" +
                "  - age:   40\n" +
                "    first: Frank\n" +
                "    last:  Eckstein\n" +
                "    state: Mississippi\n", out.toString());
    }
    public void test_update_SqlError() {
        boolean gotException = false;
        try {
            manager.update ("people", "bogus", "bogus", new Dataset(
                    "state", "Mississippi"));
        }
        catch (SqlError e) {
            assertEquals("error message",
                    "SQL error in update: Unknown column 'bogus' in " +
                    "'where clause'",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_updateWithSql_multipleUpdates() {
        manager.updateWithSql(
                "INSERT INTO people (first, last, state) " +
                "VALUES ('David', 'Williams', 'Oregon');",
                "UPDATE people SET state = 'Utah' WHERE first = 'David';");
        Dataset out = manager.findWithSql("SELECT first, last, state " +
                "FROM people WHERE first = 'David';");
        assertEquals("retrieved row", "record:\n" +
                "    first: David\n" +
                "    last:  Williams\n" +
                "    state: Utah\n", out.toString());
    }
    public void test_updateWithSql_SqlError() {
        boolean gotException = false;
        try {
            manager.updateWithSql("UPDATE bogusTable;");
        }
        catch (SqlError e) {
            TestUtil.assertSubstring("first part of exception message",
                    "SQL error in updateWithSql: You have an error in " +
                    "your SQL syntax",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_updateWithSql_withTemplate() {
        manager.updateWithSql(
                "UPDATE people SET state = @state WHERE first = @first;",
                new Dataset("state", "West Virginia", "first", "David"));
        Dataset out = manager.findWithSql("SELECT first, last, state " +
                "FROM people WHERE first = 'David';");
        assertEquals("retrieved row", "record:\n" +
                "    first: David\n" +
                "    last:  Williams\n" +
                "    state: West Virginia\n", out.toString());
    }
    public void test_updateWithSql_withTemplate_SqlError() {
        boolean gotException = false;
        try {
            manager.updateWithSql("UPDATE @table;",
                    new Dataset("table", "bogusTable"));
        }
        catch (SqlError e) {
            TestUtil.assertSubstring("first part of exception message",
                    "SQL error in updateWithSql: You have an error in " +
                    "your SQL syntax",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
        Dataset out = manager.findWithSql(
                "SELECT 'xyzzy' id, id FROM states WHERE name = 'California'");
        assertEquals("retrieved rows", "record:\n" +
                "    id:        xyzzy\n" +
                "    \"states:id\": 442\n", out.toString());
    }
    public void test_getResults_secondDupHasNoTableName() {
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
}
