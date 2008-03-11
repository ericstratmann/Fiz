/**
 * The Css class is used internally by Fiz to manage dynamically generated
 * stylesheet files.  When a .css file is requested, this class generates
 * it by expanding a template.  It also caches the results to speed up
 * future accesses.
 *
 * Note: this class must safely handle concurrent execution by multiple
 * threads.
 */

package org.fiz;
import java.io.*;
import java.util.HashMap;
import javax.servlet.http.*;

class Css {
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
    public static synchronized String getStylesheet(String name) {
        // See if the stylesheet is already in our cache.
        String result = cache.get(name);
        if (result != null) {
            return result;
        }

        // Not cached; find the template and expand it.
        StringBuilder template = Util.readFileFromPath(name, "CSS", path);
        StringBuilder expandedCss = new StringBuilder(template.length());
        Template.expand(template, Config.getDataset("css"), expandedCss,
                Template.SpecialChars.NONE);
        String css = expandedCss.toString();
        cache.put(name, css);
        return css;
    }

    /**
     * This method is invoked by the Dispatcher to handle requests for
     * dynamically generated stylesheet files.  It finds the requested
     * file and returns it via the <code>response</code> argument.
     * @param request              Information about the HTTP request.
     * @param response             Use this to return the stylesheet.
     * @param fileName             Name of the stylesheet (the portion
     *                             of the URL after "/fiz/CSS/".  Must
     *                             not have a leading "/".
     * @throws IOException         Thrown if there is a problem transmitting
     *                             the stylesheet.
     */
    public static void handleRequest(HttpServletRequest request,
            HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("text/css");
        response.getWriter().write(getStylesheet(fileName));
    }

    /**
     * Returns the search path for stylesheets.
     * @return                     Array of directories searched for
     *                             stylesheets.
     */
    public static synchronized String[] getPath() {
        return path.clone();
    }
}
