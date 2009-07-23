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
