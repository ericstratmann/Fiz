/**
 * The Css class is used internally by Fz to manage dynamically generated
 * stylesheet files.  When a .css file is requested, this class generates
 * it by expanding a template.  It also caches the results to speed up
 * future accesses.
 */

package org.fiz;
import java.util.HashMap;

public class Css {
    // The following hash table maps from the string name of a .css file
    // to a string containing the dynamically generated contents of the file.
    protected HashMap<String,String> cache = new HashMap<String,String>();

    /**
     * Returns the contents of a dynamically generated stylesheet.
     * @param name                 Path for the stylesheet.  This is the
     *                             the remainder of the URI after "fiz/css";
     *                             it can be a multi-level path but is
     *                             typically a simple filename such as foo.css.
     *                             The path must exist as a template file
     *                             in a CSS template directory.
     * @return                     The expanded stylesheet corresponding to
     *                             <code>name</code>.
     */
    public String getStylesheet(String name) {
        // First, see if the stylesheet is already in the cache.
        String result = cache.get(name);
        if (result != null) {
            return result;
        }

        // Not cached; find the template and expand it.
        return null;
    }
}
