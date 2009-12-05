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

/**
 * IOError is thrown by various Fiz methods when there are problems reading
 * or writing external data.  Typically, java.io.IOException exceptions are
 * turned into IOError's to make it easier to unwind the entire request
 * without having to declare exceptions at every level.
 */

public class IOError extends Error {
    /**
     * Construct an IOError object with a given message.
     * @param message              Information about the problem
     */
    public IOError(String message) {
        super(message);
    }

    /**
     * Creates an IOError object suitable for reporting exceptions
     * on a named file.
     * @param fileName             Name of the file for which the error occurred
     * @param details              Detailed information about the error;
     *                             typically e.getMessage() for an exception.
     * @return                     An Error object ready to throw.
     */
    public static IOError newFileInstance(String fileName, String details) {
        return new IOError("I/O error in file \"" + fileName + "\": "
                + StringUtil.extractInnerMessage(details));
    }
}
