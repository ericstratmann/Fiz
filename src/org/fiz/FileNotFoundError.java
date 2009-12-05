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
 * FileNotFoundError is thrown by various Fiz methods when a requested
 * file doesn't exist or can't be opened, and the situation is such that
 * the caller is unlikely to handle the problem: the most likely outcome is
 * that we want to abort the request being processed.  Using an Error for
 * this means that the ancestor methods don't need to declare an exception.
 */

public class FileNotFoundError extends Error {
    /**
     * Constructor for FileNotFoundError objects.
     * @param fileName             Name of the file that couldn't be opened.
     * @param type                 Type of the file, such as "dataset";
     *                             will appear in the error message.  Null
     *                             means type isn't known.
     * @param message              Information about the problem; typically
     *                             the getMessage() value from an exception.
     */
    public FileNotFoundError(String fileName, String type, String message) {
        super("couldn't open " + ((type != null) ? (type + " ") : "")
                + "file \"" + fileName + "\": "
                + StringUtil.extractInnerMessage(message));
    }

    /**
     * Constructor for FileNotFoundError objects.  This constructor is
     * intended primarily for internal use by newPathInstance.
     * @param message              Information about the problem.
     */
    public FileNotFoundError(String message) {
        super(message);
    }

    /**
     * This method creates a FileNotFoundError object when a path lookup
     * has failed; it includes the path in the error message.
     * @param fileName             Name of the file that couldn't be opened
     * @param type                 Type of the file such as "dataset";
     *                             will appear in the error message.  Null
     *                             means type isn't known.
     * @param path                 Directories that were searched for the file..
     * @return                     An Error object ready to throw.
     */
    public static FileNotFoundError newPathInstance(String fileName,
            String type, String[] path) {
        return new FileNotFoundError("couldn't find "
                + ((type != null) ? (type + " ") : "")
                + "file \"" + fileName + "\" in path (\""
                + StringUtil.join(path, "\", \"") + "\")");
    }
}
