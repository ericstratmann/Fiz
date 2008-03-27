package org.fiz;
import java.util.HashMap;

/**
 * The Config class is used internally by Fiz to manage configuration
 * datasets.  Config knows how to find datasets on disk and it caches
 * them in main memory for faster access.  The static methods of this
 * class are synchronized, so that they can be invoked by concurrent
 * threads.
 */

public class Config {
    // The following hash table maps from the string name of a dataset,
    // such as "css" or "main", to a Dataset containing the cached contents
    // of the dataset.
    protected static HashMap<String,Dataset> cache
            = new HashMap<String,Dataset>();

    // The array below gives the names of zero or more directories in which
    // to search for configuration datasets.  See the init documentation
    // for more information.
    protected static String[] path = new String[] {"."};

    // No constructor: this class only has a static methods.
    private Config() {}

    /**
     * Clears any information in the configuration dataset cache and sets
     * the directories in which to search for configuration data sets.
     * @param path                 One or more directories in which to search
     *                             for datasets.  If a given dataset exists
     *                             in multiple directories in the path, all
     *                             of the datasets will be chained together,
     *                             with datasets from earlier directories
     *                             taking precedence.
     */
    public static synchronized void init(String... path) {
        cache.clear();
        Config.path = path.clone();
    }

    /**
     * Given the name for a configuration dataset, this method finds the
     * file containing that dataset, loads it, and returns the corresponding
     * Dataset object.  Once loaded, datasets are cached to allow faster
     * access in the future.  An Error is thrown if the dataset cannot
     * be found.
     * @param name                 Name of the dataset.  This is typically
     *                             a file name with a single element, such as
     *                             "css" or "main", though it can be a
     *                             multi-level path.  The dataset file is
     *                             located by searching for a file with
     *                             this name and a recognized dataset
     *                             extension such as .yaml in the directories
     *                             containing configuration datasets.
     * @return                     Dataset corresponding to name.
     */
    public static synchronized Dataset getDataset(String name) {
        // First, see if we have this dataset cached already.
        Dataset result = cache.get(name);
        if (result != null) {
            return result;
        }

        // Not cached; find the mdataset and cache it.
        result = Dataset.newFileInstanceFromPath(name, path,
                Dataset.PathHandling.CHAIN);
        cache.put(name, result);
        return result;
    }

    /**
     * Returns the search path for configuration datasets.
     * @return                     Array of directories searched for
     *                             configuration datasets.
     */
    public static synchronized String[] getPath() {
        return path.clone();
    }
}
