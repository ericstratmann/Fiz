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
import java.io.*;
import java.util.*;

/**
 * The PathCache class implements path-based lookups, where a file is
 * located by searching a list of directories.  A PathCache object
 * also caches successful lookups so that it can perform future
 * lookups more quickly.
 */

public class PathCache {
    // The array below gives the names of zero or more directories in which
    // to search for files.
    protected String[] path;

    // The following hash table maps from a name passed to {@code find}
    // to the name of the corresponding file, which is in one of the
    // directories in the path.
    protected HashMap<String,String> lookupCache
            = new HashMap<String,String>();

    /**
     * Construct a PathCache to manage a path consisting of one or more
     * directories.
     * @param path                 One or more directories, which will be
     *                             searched in order by the {@code find}
     *                             method.  Directory names should use
     *                             slashes as path separators, and should
     *                             not end in slash.
     */
    public PathCache(String ... path) {
        this.path = path.clone();
    }

    /**
     * Discard all cached information, so that it will be refetched from
     * disk the next time is needed.  Typically invoked during debugging
     * sessions to flush caches on every request.
     */
    public void clearCache() {
        lookupCache.clear();
    }

    /**
     * Locate a file by  searching through all of the entries in the path,
     * stopping as soon as an existing file is found.  If this file has
     * already been looked up successfully in the past, the previous result
     * is returned without searching the path again.
     * @param name                 Name of desired file; may be a multi-
     *                             level path (use "/" as separator char).
     * @return                     Path to actual file, consisting of
     *                             one of the directory names in the path,
     *                             followed by a slash, followed by
     *                             {@code name}.
     * @throws FileNotFoundError   The file didn't exist in any of the
     *                             directories in the path.
     */
    public String find(String name) {
        String result = lookupCache.get(name);
        if (result != null) {
            // We've already looked up this file; use the existing
            // information.
            return result;
        }

        // Each iteration through the following loop checks one
        // directory in the path.
        StringBuilder fullName = new StringBuilder(50);
        for (String directory : path) {
            fullName.setLength(0);
            fullName.append(directory);
            fullName.append("/");
            fullName.append(name);
            result = fullName.toString();
            if ((new File(result)).exists()) {
                lookupCache.put(name, result);
                return result;
            }
        }

        // File not found; generate an error.
        throw FileNotFoundError.newPathInstance(name, null, path);
    }
}
