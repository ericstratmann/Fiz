package org.fiz;

import java.sql.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * SqlDataManager provides access to SQL databases using JDBC.
 * <p>
 * The configuration dataset for a SqlDataManager may contain any of the
 * following values:
 *   driverClass:      (required) Name of the class that implements the
 *                      JDBC driver for the target database, such as
 *                      {@code com.mysql.jdbc.Driver} for MySQL.
 *   serverUrl:        (required) JDBC URL that can be used to open a
 *                     connection to the server, such as
 *                     {@code jdbc:mysql://localhost:3306/dbName} for
 *                     database {@code dbName} in a local MySQL server
 *                     listening on the default port.
 *   password:         (required) Password associated with {@code user}.
 *   user:             (required) Name of the user under which database
 *                     operations should be performed.
 * <p>
 * DataRequests for SqlDataManager must contain a {@code request} value
 * that selects an operation to perform; it must be one of the values
 * listed below.  Based on {@code request}, additional DataRequest values
 * are required or supported:
 *   findWithSql:      Issue a SQL query and returned a dataset containing
 *                     one nested dataset (named {@code data}) for each
 *                     record returned.
 *       sql:          (required) The SQL query to issue.
 */

public class SqlDataManager extends DataManager {
    // The following variable holds the URL describing this connection.
    protected String serverUrl;

    // The following variable refers to a Connection that we use to
    // communicate with the database server.
    protected Connection connection;

    // One instance of the following type is created for each table in the
    // database associated with this connection.  It contains metadata
    // needed to implement various operations on the table.
    protected static class TableInfo {
        HashSet<String> columnNames;
                                   // Names of all of the columns in
                                   // this table.
    }

    // The following variable keeps track of all the tables in the database
    // for this connection; each entry maps from a table name to an object
    // containing information about the table.
    protected HashMap<String,TableInfo> tables = null;

    // The following variable is used for log4j-based logging.
    protected static Logger logger = Logger.getLogger(
            "org.fiz.SqlDataManager");

    /**
     * Construct a SqlDataManager using a dataset containing configuration
     * parameters.
     * @param config               Parameters for this data manager;
     *                             see top-level class documentation
     *                             for supported values.
     */
    public SqlDataManager(Dataset config) {
        serverUrl = config.get("serverUrl");
        try {
            Class.forName(config.get("driverClass"));
        }
        catch (ClassNotFoundException e) {
            throw new InternalError("SqlDataManager couldn't load driver " +
                    "class \"" + config.get("driverClass") + "\"");
        }
        try {
            connection = DriverManager.getConnection(serverUrl,
                    config.get("user"), config.get("password"));
            logger.info("SQLDataManager connected to \"" + serverUrl + "\"");
        }
        catch (SQLException e) {
            throw new InternalError ("SqlDataManager couldn't create " +
                    "connection with server at \"" + serverUrl +
                    "\": " + e.getMessage());
        }
        collectMetadata();
    }

    /**
     * Remove all of the rows from a table.
     * @param table                Name of the table to be emptied.
     */
    public void clearTable(String table) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM " + table);
            statement.close();
        }
        catch (SQLException e) {
            throw new SqlError(e, "in clearTable");
        }
    }

    /**
     * Delete all of the rows in a table that have a particular value in a
     * particular column.
     * @param tableName            Name of the table to modify.
     * @param column               Name of the column whose value selects the
     *                             row(s) to be deleted.
     * @param value                Delete all rows that have this value
     *                             in the column given by {@code column}.
     */
    public void delete(String tableName, String column, String value) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + tableName + " WHERE " +
                    column + " = ?");
            statement.setString(1, value);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e) {
            throw new SqlError(e, "in delete");
        }
    }

    /**
     * Find all of the rows in a table that have a particular value in a
     * particular column.
     * @param tableName            Name of the table in which to search.
     * @param column               Name of a column in the table.
     * @param value                Return all rows that have this value in
     *                             the column given by {@code column}.
     * @return                     A dataset containing one child named
     *                             {@code record} for each record returned
     *                             by the query.
     */
    public Dataset find(String tableName, String column, String value) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM " + tableName + " WHERE " +
                    column + " = ?;");
            statement.clearParameters();
            statement.setString(1, value);
            ResultSet rs = statement.executeQuery();
            Dataset result = getResults(rs);
            statement.close();
            return result;
        }
        catch (SQLException e) {
            throw new SqlError(e, "in find");
        }

    }

    /**
     * Retrieve records from the database using a raw SQL string for
     * the query.
     * @param sql                  SQL query that will return zero or more
     *                             records.
     * @return                     A dataset containing one child named
     *                             {@code record} for each record returned
     *                             by the query.
     * @throws SqlError            The database server reported a problem.
     */
    public Dataset findWithSql(String sql) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            Dataset result = getResults(rs);
            statement.close();
            return result;
        }
        catch (SQLException e) {
            throw new SqlError(e, "in findWithSql");
        }
    }

    /**
     * Retrieve records from the database using a template for an SQL query.
     * @param template             Template for an SQL query that will return
     *                             zero or more records.
     * @param data                 Contains data values to substitute into
     *                             {@code template}.
     * @return                     A dataset containing one child named
     *                             {@code record} for each record returned
     *                             by the query.
     * @throws SqlError            The database server reported a problem.
     */
    public Dataset findWithSql(String template, Dataset data) {
        try {
            ArrayList<String> parameters = new ArrayList<String>();
            String sql = Template.expandSql(template, data, parameters);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.clearParameters();
            int i = 1;
            for (String value : parameters) {
                statement.setString(i, value);
                i++;
            }
            ResultSet rs = statement.executeQuery();
            Dataset result = getResults(rs);
            statement.close();
            return result;
        }
        catch (SQLException e) {
            throw new SqlError(e, "in findWithSql");
        }
    }

    /**
     * Add a row to an existing table, where the data for a new row is
     * specified by a Dataset.
     * @param tableName            Name of the table in which the new rows
     *                             are to be created.
     * @param row                  Contains values for the new row (any values
     *                             that do not correspond to columns in
     *                             {@code tableName} are ignored).
     * @throws SqlError            The database server reported a problem.
     */
    public void insert(String tableName, Dataset row) {
        try {
            ArrayList<String> names = getValidColumns(tableName, row);

            // Create the SQL query string, with ?'s as placeholders
            // for variables.
            StringBuffer sql = new StringBuffer();
            sql.setLength(0);
            sql.append("INSERT INTO ");
            sql.append(tableName);
            sql.append(" (");
            sql.append(StringUtil.join(names, ", "));
            sql.append(") VALUES (");
            String field = "?";
            for (int i = 0, length = names.size(); i < length; i++) {
                sql.append(field);
                field = ", ?";
            }
            sql.append(");");

            // Create the statement and supply values for the ?'s.
            PreparedStatement statement = connection.prepareStatement(
                    sql.toString());
            statement.clearParameters();
            int i = 1;
            for (String name : names) {
                statement.setString(i, row.get(name));
                i++;
            }
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e) {
            throw new SqlError(e, "in insert");
        }
    }

    /**
     * This method is invoked by DataRequest.startRequests to process
     * one or more requests for this data manager.  The requests are
     * processed synchronously, so they are all complete before this
     * method returns.
     * @param requests             DataRequest objects describing the
     *                             requests to be processed.
     */
    @Override
    public void startRequests(Collection<DataRequest> requests) {
        for (DataRequest request : requests) {
            Dataset parameters = request.getRequestData();
            String operation = null;
            try {
                operation = parameters.get("request");
                if (operation.equals("findWithSql")) {
                    request.setComplete(findWithSql(parameters.get("sql")));
                } else {
                    request.setError(new Dataset("message",
                            "unknown request \"" + operation +
                            "\" for SqlDataManager; must be findWithSql"));
                }
            }
            catch (Dataset.MissingValueError e) {
                request.setError(new Dataset("message",
                        "SqlDataManager " +
                        ((operation != null) ? ("\"" + operation + "\" ") : "") +
                        "request didn't contain required " +
                        "parameter \"" + e.getMissingKey() + "\""));
            }
            catch (SqlError e) {
                String message = "SQL error in SqlDataManager \"" +
                        operation + "\" request: " +
                        e.getCause().getMessage();
                logger.error(message);
                request.setError(new Dataset("message", message));
            }
            catch (Error e) {
                String message = "internal error in SqlDataManager" +
                        operation + "\" request: " +
                        ((operation != null) ?
                        ("\"" + operation + "\" ") : "") +
                        "request: " + e.getMessage();
                logger.error(message);
                request.setError(new Dataset("message", message));
            }
        }
    }

    /**
     * Modify one or more rows in a particular table, where the rows to be
     * modified are those with a particular value in a particular column.
     * @param tableName            Name of the table to be modified.
     * @param column               Name of the column that will be checked
     *                             to select the rows to be modified.
     * @param value                All rows whose {@code column} value
     *                             equals this will be modified.
     * @param newValues            Contains new values to store in all
     *                             of the rows matching {@code column} and
     *                             {@code value}.  Any value whose name
     *                             does not represent a column in
     *                             {@code table} is ignored.
     */
    public void update(String tableName, String column, String value,
            Dataset newValues) {
        try {
            ArrayList<String> names = getValidColumns(tableName, newValues);

            // Create the SQL query string, with ?'s as placeholders
            // for variables.
            StringBuffer sql = new StringBuffer();
            sql.setLength(0);
            sql.append("UPDATE ");
            sql.append(tableName);
            sql.append (" SET ");
            String separator = "";
            for (String name : names) {
                sql.append(separator);
                sql.append(name);
                sql.append(" = ?");
                separator = ", ";
            }
            sql.append(" WHERE ");
            sql.append(column);
            sql.append(" = ?;");

            // Create the statement and supply values for the ?'s.
            PreparedStatement statement = connection.prepareStatement(
                    sql.toString());
            statement.clearParameters();
            int i = 1;
            for (String name : names) {
                statement.setString(i, newValues.get(name));
                i++;
            }
            statement.setString(i, value);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e) {
            throw new SqlError(e, "in update");
        }
    }

    /**
     * Make a database update (i.e., no return data) using one or more
     * raw SQL statements.
     * @param sqlStatements        Any number of SQL statements.
     */
    public void updateWithSql(String ... sqlStatements) {
        try {
            Statement statement = connection.createStatement();
            for (String sql : sqlStatements) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
            statement.close();
        }
        catch (SQLException e) {
            throw new SqlError(e, "in updateWithSql");
        }
    }

    /**
     * Make a database update (i.e., no return data) using a template for
     * an SQL statement.
     * @param template             Template for an SQL statement (all the
     *                             usual "@" facilities of templates).
     * @param data                 Contains values to substitute into
     *                             {@code template}.
     */
    public void updateWithSql(String template, Dataset data) {
        try {
            ArrayList<String> parameters = new ArrayList<String>();
            PreparedStatement statement = connection.prepareStatement(
                    Template.expandSql(template, data, parameters));
            statement.clearParameters();
            int i = 1;
            for (String value : parameters) {
                statement.setString(i, value);
                i++;
            }
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e) {
            throw new SqlError(e, "in updateWithSql");
        }
    }

    /**
     * This method is used internally to read information about the database's
     * tables and save it for use later.
     */
    protected void collectMetadata() {
        try {
            // Collect metadata about all of the tables in the database.
            tables = new HashMap<String,TableInfo>();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet tableResults = md.getTables(null, null, null, null);
            StringBuffer message = new StringBuffer();
            while (tableResults.next()) {
                String tableName = tableResults.getString("TABLE_NAME");
                message.setLength(0);
                message.append("table \"" + tableName + "\" has columns ");
                ResultSet columnResults = md.getColumns(null, null,
                        tableName, null);
                TableInfo info = new TableInfo();
                info.columnNames = new HashSet<String>();
                String separator = "";
                while (columnResults.next()) {
                    String columnName = columnResults.getString("COLUMN_NAME");
                    info.columnNames.add(columnName);
                    message.append(separator);
                    message.append(columnName);
                    separator = ", ";
                }
                tables.put(tableName, info);
                logger.info(message);
            }
        }
        catch (SQLException e) {
            throw new SqlError(e, "while reading database metadata");
        }
    }

    /**
     * Generate a Dataset containing all of the information in a ResultSet.
     * @param rs                   ResultSet, typically returned by a JDBC
     *                             query.
     * @return                     A Dataset containing one child named
     *                             {@code record} for each result row, with
     *                             values containing the columns for that
     *                             row.  The names for the leaf values are
     *                             the database column names unless the
     *                             same column name is used multiple times,
     *                             in which case names of the form
     *                             {@code table:column} are used for the
     *                             ambiguous column.
     * @throws SQLException        An error occurred while querying the
     *                             ResultSet.
     */
    protected Dataset getResults(ResultSet rs) throws SQLException {
        // Collect the column names, which are the same for all rows
        // of the result.  If the same column name appears more than once
        // (e.g. because we are seeing combined information from several
        // tables), change the ambiguous column names to table:column.
        ResultSetMetaData info = rs.getMetaData();
        int columnCount = info.getColumnCount();
        String names[] = new String[columnCount];

        // The following variable maps from a simple column name to the
        // index of a column that already has that name.
        HashMap<String,Integer> otherColumn = new HashMap<String,Integer>();
        for (int i = 1; i <= columnCount; i++) {
            String name = info.getColumnName(i);
            names[i-1] = name;
            Integer dup = otherColumn.get(name);
            if (dup != null) {
                // We have duplicate column names.  First, fix up the
                // other column by this name (if there are more than 2
                // columns with the same name we will fix up the first column
                // multiple times, but this doesn't happen very often).
                String tableName = info.getTableName(dup);
                if (tableName.length() > 0) {
                    names[dup-1] = tableName + ":" + name;
                }

                // Now fix up the new column.
                tableName = info.getTableName(i);
                if (tableName.length() > 0) {
                    names[i-1] = tableName + ":" + name;
                }
            }
            otherColumn.put(name, i);
        }

        // Generate a child Dataset for each row of the result.
        Dataset result = new Dataset();
        while (rs.next()) {
            Dataset row = new Dataset();
            for (int i = 1; i <= columnCount; i++) {
                String value = rs.getString(i);
                row.set(names[i-1], (value != null)?  value : "");
            }
            result.addChild("record", row);
        }
        return result;
    }

    /**
     * This method is used before making updates to filter out names
     * that do not correspond to columns in a particular table.
     * @param tableName            Name of a table in the database.
     * @param values               Dataset containing candidate values for
     *                             creating a new row in the table or updating
     *                             an existing row.
     * @return                     The return value is an ArrayList containing
     *                             all of the top-level names from
     *                             {@code values} that are column names in
     *                             {@code tableName}.
     */
    protected ArrayList<String> getValidColumns(String tableName,
            Dataset values) {
        TableInfo info = tables.get(tableName);
        if (info == null) {
            throw new InternalError("no database table named \"" +
                    tableName + "\"");
        }
        ArrayList<String> names = new ArrayList<String>();
        for (String name : values.keySet()) {
            if (info.columnNames.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }
}
