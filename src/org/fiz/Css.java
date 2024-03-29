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
import java.util.HashMap;

/**
 * The Css class manages dynamically generated stylesheet files.  When a
 * .css file is requested, this class generates it by expanding a template.
 * It also caches the results to speed up future accesses.  The class is
 * designed for concurrent execution by multiple threads handling requests
 * in parallel while sharing a collection of stylesheet files.
 */

public class Css {
    // The following hash table maps from the string name of a .css file
    // to a string containing the dynamically generated contents of the file.
    protected static HashMap<String,String> cache
            = new HashMap<String,String>();

    // The array below gives the names of zero or more directories in which
    // to search for stylesheet templates.  See the init documentation
    // for more information.
    protected static String[] path = new String[] {"."};

    /**
     * Reinitializes the stylesheet cache: deletes any information in the
     * cache and sets the search path of directories in which to look for
     * stylesheet templates.
     * @param path                 One or more directories in which to search
     *                             for templates.  The directories are searched
     *                             in order, using the first template found.
     */
    public static synchronized void init(String... path) {
        cache.clear();
        Css.path = path.clone();
    }

    /**
     * Discards all cached information, so that it will be refetched from
     * disk the next time is needed.  Typically invoked during debugging
     * sessions to flush caches on every request.
     */
    public static synchronized void clearCache() {
        cache.clear();
    }

    /**
     * Returns the current path being used for finding stylesheet files.
     * @return                     The directories supplied in the most
     *                             recent call to {@code init}.
     */
    public static synchronized String[] getPath() {
        return path;
    }

    /**
     * Returns the contents of a dynamically generated stylesheet.
     * @param name                 Path for the stylesheet.  This is the
     *                             the remainder of the URL after "fiz/css";
     *                             it can be a multi-level path but is
     *                             typically a simple filename such as foo.css.
     *                             The path must exist as a template file
     *                             in a CSS template directory.
     * @return                     The expanded stylesheet corresponding to
     *                             <code>name</code>.
     */
    public static synchronized String getStylesheet(String name) {
        // See if the stylesheet is already in our cache.
        String result = cache.get(name);
        if (result != null) {
            return result;
        }

        // Not cached; find the template and expand it.
        StringBuilder template = Util.readFileFromPath(name, "CSS", path);
        StringBuilder expandedCss = new StringBuilder(template.length());
        Template.appendRaw(expandedCss, template, Config.getDataset("css"));
        String css = expandedCss.toString();
        cache.put(name, css);
        return css;
    }

    /**
     * Returns the search path for stylesheets.
     * @return                     Array of directories searched for
     *                             stylesheets.
     */
    public static synchronized String[] getSearchPath() {
        return path.clone();
    }
}
