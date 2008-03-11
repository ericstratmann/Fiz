/**
 * Dataset defines general-purpose mechanisms for accessing hierarchical
 * information stored in key-value form, such as XML and YAML documents,
 * HashMaps, and so on.  Datasets are used to hold a variety of data
 * in Fiz, such as query values from URLs, configuration options,
 * and data coming from data managers.  A dataset has the following
 * general properties:
 *   - Each dataset consists of zero or more key-value pairs
 *   - Keys are arbitrary strings (but it's usually convenient to limit
 *     them to standard identifier characters)
 *   - Values can have three forms:
 *     - A string
 *     - A nested dataset (containing additional key-value pairs)
 *     - A list of nested data sets, each containing additional
 *       key-value pairs
 *     - Datasets can be implemented in a variety of different ways
 *       (nested hash tables, XML documents, etc.) but their contents
 *       have the logical structure described above.  Some datasets,
 *       like those defined by this base class, support only a single
 *       level (no nested data sets).
 * In addition to the values it stores internally, each dataset can
 * contain a reference to another dataset.  The term "chain" refers to the
 * referenced dataset as well as any dataset it references, and so on.
 * If a desired value doesn't exist in a dataset, its chain is also
 * searched for the value.
 *
 * This base implementation of Dataset supports a single level
 * structure described by an array of strings.
 */

package org.fiz;
import java.util.HashMap;

public class Dataset {
    /**
     * Instances of this enum are passed to the lookup method to indicate
     * what sort of value is desired as a result:
     * <p>
     * STRING: the caller expects the name to refer to a string value
     * <p>
     * DATASET: the caller expects the name to refer to a single nested
     * dataset;  if it actually refers to a list of datasets, the first
     * dataset in the list will be returned
     * <p>
     * DATASETS: the caller expects the name to refer to zero or more nested
     * data sets, all of which should be returned
     */
    public enum DesiredType {STRING, DATASET, DATASETS}

    /**
     * Instances of this enum are passed to getFileInstanceFromPath
     * to indicate how to handle the case where the dataset exists
     * in multiple directories of the path:
     * <p>
     * CHAIN: chain all of the datasets together, so that their contents
     * combined with those from earlier directories in the path getting
     * priority.
     * <p>
     * FIRST_ONLY: use only the first dataset found and ignore any others.
     */
    public enum PathHandling {CHAIN, FIRST_ONLY}

    /**
     * MissingValueError is thrown when methods such as <code>get</code>
     * cannot find the requested key in the dataset.
     */
    public static class MissingValueError extends Error {
        /**
         * Constructor for MissingValueError.
         * @param missingKey       Name of the key that was not found;
         *                         used to generate a message in the
         *                         exception
         */
        public MissingValueError(String missingKey) {
            super("couldn't find dataset element \"" + missingKey + "\"");
        }
    }

    /**
     * SyntaxError is thrown when the input used to create a Dataset
     * is malformed in some way that makes the information useless.
     */
    public static class SyntaxError extends Error {
        /**
         * Constructor for SyntaxError.
         * @param fileName         If the dataset input came from a file,
         *                         this gives the file name; null means
         *                         the input didn't come from a file
         * @param message          Detailed information about the problem
         *                         (null if no details are available)
         */
        public SyntaxError(String fileName, String message) {
            super("syntax error in dataset input"
                    + ((fileName != null) ? (" file \"" + fileName + "\"") : "")
                    + ((message != null) ? (": " + message) : ""));
        }
    }

    /**
     * UnsupportedFormat is thrown when getFileInstance is passed a
     * file name with an unrecognized extension.
     */
    public static class UnsupportedFormatError extends Error {
        /**
         * Constructor for UnsupportedFormatError.
         * @param fileName         Name of the file whose extension was
         *                         not recognized
         */
        public UnsupportedFormatError(String fileName) {
            super("couldn't recognize format of dataset file \""
                    + fileName + "\"");
        }
    }

    /**
     * WrongTypeError is thrown when the value found by a method such as
     * <code>get</code> has the wrong type (e.g., a string value was
     * needed but a nested dataset was found).
     */
    public static class WrongTypeError extends Error {
        /**
         * Constructor for WrongTypeError.
         * @param message          Message to use for the exception
         */
        public WrongTypeError(String message) {
            super(message);
        }
    }

    // Holds the key-value mappings for the dataset.  Basic Datasets
    // support only a single level: no nested datasets.
    protected HashMap<String,String> map;

    // The following field points to another dataset, which is searched
    // (along with its chain) for any values not found in the dataset.
    // Null means there is no chain for the dataset.
    protected Dataset chain;

    /**
     * Creates an empty dataset.
     */
    public Dataset() {
        map = new HashMap<String,String>();
    }

    /**
     * Creates a dataset from string values passed as arguments.
     * @param keysAndValues        Array whose elements consist of
     *                             alternating keys and values for
     *                             initializing the Dataset; must
     *                             contain an even number of elements
     */
    public Dataset(String... keysAndValues) {
        map = new HashMap<String,String>();
        int last = keysAndValues.length - 2;
        for (int i = 0; i <= last; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i+1]);
        }
    }

    /**
     * Creates a Dataset from information contained in a file.
     * @param fileName             Name of a dataset file in a supported
     *                             format; the format is inferred from the
     *                             file's extension (e.g. ".yaml" means the
     *                             file is in YAML format).  If the name
     *                             doesn't contain an extension, this method
     *                             tries all supported extensions until it
     *                             finds a dataset file that exists.
     * @return                     New Dataset object containing contents
     *                             of <code>fileName</code>
     */
    public static Dataset getFileInstance(String fileName) {
        String extension = Util.fileExtension(fileName);
        if (extension == null) {
            String newName = Util.findFileWithExtension(fileName,
                    ".yaml", ".yml");
            if (newName == null) {
                throw new FileNotFoundError(fileName, "dataset",
                        "couldn't find a file with a supported extension");
            }
            fileName = newName;
            extension = Util.fileExtension(fileName);
        }
        if (extension.equals(".yml") || extension.equals(".yaml")) {
            return YamlDataset.getFileInstance(fileName);
        }
        throw new UnsupportedFormatError(fileName);
    }

    /**
     * Creates a Dataset from information in one or more files found by
     * searching a collection of directories.
     * @param name                 Name of the desired dataset file.  If
     *                             the name has no extension, this method
     *                             tries all of the supported extensions in
     *                             order and stops with the first file that
     *                             exists.  The search for an extension
     *                             happens separately for each directory in
     *                             <code>path</code> (a different extension
     *                             may be chosen for each directory).
     * @param path                 Directories to search for the dataset.
     * @param pathHandling         If <code>name</code> exists in more than
     *                             one directory in <code>path</code>, this
     *                             parameter indicates whether to use just the
     *                             first dataset found or chain them together.
     * @return                     A new Dataset object.
     */
    public static Dataset getFileInstanceFromPath(String name,
            String[] path, PathHandling pathHandling) {
        Dataset first = null, last = null;
        for (int i = 0; i < path.length; i++) {
            String fullName = path[i] + "/" + name;
            try {
                Dataset current = Dataset.getFileInstance(fullName);
                if (pathHandling == PathHandling.FIRST_ONLY) {
                    return current;
                }

                // Add the new dataset to the end of the chain of
                // datasets that will form the result.
                if (first == null) {
                    first = last = current;
                } else {
                    last.setChain(current);
                    last = current;
                }
            }
            catch (FileNotFoundError e) {
                // There isn't a relevant dataset in this directory;
                // skip it and go on to the next directory.
                continue;
            }
        }
        if (first == null) {
            throw FileNotFoundError.getPathInstance(name, "dataset", path);
        }
        return first;
    }

    /**
     * Given a particular key, returns the value associated with that
     * key, or null if the key is not defined.  This method is identical
     * to <code>get</code> except that it does not generate an exception
     * if the key is undefined.
     * @param key                  Name of the desired value
     * @return                     Value associated with <code>key</code>,
     *                             or null if <code>key</code> doesn't
     *                             exist.
     * @throws WrongTypeError      Thrown if <code>key</code> corresponds
     *                             to a nested dataset rather than a string
     *                             value.e, since
     *                             we don't support nested datasets)
     */
    public String check(String key) throws WrongTypeError {
        Object child = lookup(key, DesiredType.STRING);
        if ((child == null) && (chain != null)) {
            return chain.check(key);
        }
        return (String) child;
    }

    /**
     * Indicates whether a particular key exists in the dataset.
     * @param key                  Name of the desired value
     * @return                     True if the key exists, false otherwise
     */
    public boolean containsKey(String key) {
        return (map.get(key) != null);
    }

    /**
     * Given a particular key, returns the value associated with that
     * key, if there is one.  This is a single-level lookup: the key and
     * value must be present in the top level of the dataset.
     * @param key                  Name of the desired value
     * @return                     Value associated with <code>key</code>
     * @throws MissingValueError   Thrown if <code>key</code> can't be found
     * @throws WrongTypeError      Thrown if <code>key</code> corresponds
     *                             to a nested dataset rather than a string
     *                             value.e, since
     *                             we don't support nested datasets)
     */
    public String get(String key) throws MissingValueError, WrongTypeError {
        Object child = lookup(key, DesiredType.STRING);
        if (child == null) {
            if (chain != null) {
                return chain.get(key);
            }
            throw new MissingValueError(key);
        }
        return (String) child;
    }

    /**
     * Create a new Dataset corresponding to a nested dataset within the
     * current dataset.
     * @param key                  Name of the desired child; must be a
     *                             top-level child within the current dataset
     * @return                     A Dataset providing access to the child
     * @throws MissingValueError   Thrown if <code>key</code> is not defined
     *                             at the top level of the current dataset
     * @throws WrongTypeError      Thrown if <code>key</code> is defined but
     *                             corresponds to a string value rather than
     *                             a nested dataset
     */
    public Dataset getChild(String key)
            throws MissingValueError, WrongTypeError {
        Object child = lookup(key, DesiredType.DATASET);
        if (child == null) {
            if (chain != null) {
                return chain.getChild(key);
            }
            throw new MissingValueError(key);
        }
        return (Dataset) child;
    }

    /**
     * Generate an array of Datasets corresponding to all of the
     * children by a given name.
     * @param key                  Name of the desired child; must be a
     *                             top-level child within the current dataset
     * @return                     An array of Datasets, one for each child
     *                             dataset corresponding to <code>key</code>;
     *                             the array will be empty if there are no
     *                             children corresponding to
     *                             <code>key</code>
     * @throws WrongTypeError      Thrown if <code>key</code> refers to a
     *                             string value rather than nested datasets
     */
    public Dataset[] getChildren(String key) throws WrongTypeError {
        Object children = lookup(key, DesiredType.DATASETS);
        if (children == null) {
            if (chain != null) {
                return chain.getChildren(key);
            }
            return new Dataset[0];
        }
        return (Dataset[]) children;
    }

    /**
     * Traverses a hierarchical sequence of keys to find a string value.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, "a.b.c" refers to a value
     *                             "c" contained in a nested dataset "b"
     *                             contained in a data set "a" contained
     *                             in the current dataset
     * @return                     Value associated with <code>path</code>
     * @throws MissingValueError   Thrown if one of the keys in
     *                             <code>path</code> doesn't exist
     * @throws WrongTypeError      Thrown if one of the keys in
     *                             <code>path</code> has the wrong type
     *                             (nested dataset vs. string value)
     */

    public String getPath(String path)
            throws MissingValueError, WrongTypeError {
        Object child = lookupPath(path, DesiredType.STRING);
        if (child == null) {
            if (chain != null) {
                return chain.getPath(path);
            }
            throw new MissingValueError(path);
        }
        return (String) child;
    }

    /**
     * This is a general-purpose method to find the value associated with a
     * given key, intended primarily for use by other methods such as
     * <code>get</code> and <code>getChildren</code>.  The value must be
     * present in the top level of the dataset (i.e., key is not a path;
     * use lookupPath if it is).  This method searches only the dataset,
     * not its chain.
     * @param key                  Name of the desired value
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     If <code>key</code> exists and its value
     *                             matches <code>desiredResult</code> then
     *                             it is returned as a String, YamlDataset,
     *                             or YamlDataset[] for <code>desiredResult</code>
     *                             values of STRING, DATASET, and DATASETS,
     *                             respectively.  If <code>key</code>
     *                             doesn't exist then null is returned.
     * @throws WrongTypeError      Thrown if <code>key</code> is defined but
     *                             its value doesn't correspond to
     *                             <code>desiredResult</code>.
     */
    public Object lookup(String key, DesiredType wanted)
            throws WrongTypeError {
        String child = map.get(key);
        if (child == null) {
            return null;
        }

        // If we found a value it must be a string; that's all we support.
        if (wanted == DesiredType.STRING) {
            return child;
        }
        throw new WrongTypeError("wrong type for dataset key \"" + key
                + "\": expected nested dataset, found string value");
    }

    /**
     * This is a general-purpose method to find the value associated with a
     * hierarchical path, intended primarily for use by other methods such as
     * <code>getPath</code> and <code>getPathChildren</code>.  This method
     * searches only the dataset, not its chain.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, "a.b.c" refers to a value
     *                             "c" contained in a nested dataset "b"
     *                             contained in a data set "a" contained
     *                             in the current dataset
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     If desired value exists and matches
     *                             <code>desiredResult</code> then
     *                             it is returned as a String, YamlDataset,
     *                             or YamlDataset[] for <code>desiredResult</code>
     *                             values of STRING, DATASET, and DATASETS,
     *                             respectively.  If the dataset doesn't
     *                             contain a value corresponding to
     *                             <code>path</code> then null is returned.
     * @throws WrongTypeError      Thrown if <code>path</code> is defined but
     *                             its value doesn't correspond to
     *                             <code>desiredResult</code>
     */

    public Object lookupPath(String path, DesiredType wanted)
            throws WrongTypeError {
        // This class doesn't support hierarchical data sets, so if path
        // contains more than one key we will generate an error.

        int dot = path.indexOf('.');
        if (dot == -1) {
            return map.get(path);
        }
        String key = path.substring(0, dot);
        if (map.get(key) == null) {
            return null;
        }
        throw new WrongTypeError("wrong type for dataset element \"" + key
                + "\": expected nested dataset, found string value");
    }

    /**
     * Specifies another dataset, which should be searched (along with its
     * chain) whenever a desired key or path cannot be found in the
     * Dataset.
     * @param chain                Additional dataset to search, or null
     *                             to clear any existing chain
     */
    public void setChain(Dataset chain) {
        this.chain = chain;
    }

    /**
     * @return                     Next Dataset to search for values not found
     *                             in the Dataset (null if none).
     */
    public Dataset getChain() {
        return chain;
    }

    /**
     * Returns the number of values stored in this Dataset.
     * @return                     Number of values in the Dataset; if
     *                             the dataset is hierarchical, only
     *                             top-level values are counted
     */
    public int size() {
        return map.size();
    }
}
