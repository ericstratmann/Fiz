package org.fiz;
import java.sql.*;

/**
 * Junit tests for the SqlError class.
 */
public class SqlErrorTest extends junit.framework.TestCase {
    public void test_SqlError() {
        SQLException base = new SQLException("database barfed");
        Error e = new SqlError(base, "while running tests");
        assertEquals("exception message",
                "SQL error while running tests: database barfed",
                e.getMessage());
        assertEquals("cause", base, e.getCause());
    }
}
