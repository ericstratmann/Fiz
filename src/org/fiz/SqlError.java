package org.fiz;
import java.sql.*;

/**
 * SqlError is thrown by Fiz classes that manipulate the database directly.
 * They typically represent an internal error of some sort (i.e., there is no
 * recovery).  SqlErrors are usually created from SQLExceptions in order to
 * abort the current request without all the nested callers having to declare
 * exception handlers.
 */
public class SqlError extends Error {

    /**
     * Construct an SqlError object from an SQLException object.
     * @param e                    The SQLException that is the underlying
     *                             cause of the problem.
     * @param context              Describes the activity during which
     *                             the error occurred, such as "while
     *                             reading metadata" or
     *                             "in SqlDataManager.updateWithSql".
     */
    public SqlError(SQLException e, String context) {
        super("SQL error " + context + ": " +
                StringUtil.lcFirst(e.getMessage()), e);
    }
}
