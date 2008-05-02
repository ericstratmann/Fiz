package org.fiz;
import java.util.*;

/**
 * Dataset defines general-purpose mechanisms for accessing hierarchical
 * information stored in key-value form.  Datasets are used to hold a
 * variety of data in Fiz, such as query values from URLs, configuration
 * options, and data coming from data managers.  A dataset has the following
 * general properties:
 *   - A dataset consists of key-value pairs
 *   - Keys are arbitrary strings (but it's usually convenient to limit
 *     them to standard identifier characters)
 *   - Values can have three forms:
 *     - A string
 *     - A nested dataset (containing additional key-value pairs)
 *     - A list of nested datasets, each containing additional
 *       key-value pairs
 *   - Datasets can be created from a variety of sources, such as
 *     XML and YAML files (see classes such as XmlDataset and
 *     YamlDataset for details).
 * In addition to the values it stores internally, each dataset can
 * contain a reference to another dataset.  The term "chain" refers to the
 * referenced dataset as well as any dataset it references, and so on.
 * If a desired value doesn't exist in a dataset, its chain is also
 * searched for the value.
 */

public class Dataset implements Cloneable {
    /**
     * Instances of this enum are passed to Dataset.lookup to indicate
     * what sort of value is desired as a result.
     */
    public enum DesiredType {
        /**
         * The caller expects the name to refer to a string value.
         */
        STRING,

        /**
         * The caller expects the name to refer to a single nested
         * dataset;  if it actually refers to a list of datasets,
         * the first dataset in the list will be returned.
         */
        DATASET,

        /**
         * The caller expects the name to refer to zero or more
         * nested datasets, all of which will be returned.
         */
        DATASETS,

        /**
         * The caller is happy to accept any of the above types.
         */
        ANYTHING
    }

    /**
     * Instances of this enum are passed to Dataset.newFileInstanceFromPath
     * to indicate how to handle the case where the dataset exists
     * in multiple directories of the path.
     */
    public enum PathHandling {
        /**
         * Chain all of the datasets together so that their contents
         * combine, with those from earlier directories in the path
         * getting priority.
         */
        CHAIN,

        /**
         * Use only the first dataset found and ignore any others.
         */
        FIRST_ONLY
    }

    /**
     * MissingValueError is thrown when methods such as {@code get}
     * cannot find the requested key in the dataset.
     */
    public static class MissingValueError extends Error {
        // Name of the dataset element that could be found:
        protected String missingKey;

        /**
         * Construct a MissingValueError with a message describing the
         * key that was not found.
         * @param missingKey       Name of the key that was not found;
         *                         used to generate a message in the
         *                         exception
         */
        public MissingValueError(String missingKey) {
            super("couldn't find dataset element \"" + missingKey + "\"");
            this.missingKey = missingKey;
        }

        /**
         * This method can be used to retrieve the name of the
         * dataset element that caused the exception.
         * @return                 Key for the dataset value that wasn't
         *                         found.
         */
        public String getMissingKey() {
            return missingKey;
        }
    }

    /**
     * SyntaxError is thrown when the input used to create a Dataset
     * is malformed in some way that makes the information useless.
     */
    public static class SyntaxError extends Error {
        /**
         * Construct a SyntaxError with a message that includes the name of
         * the file in which the error occurred.
         * @param fileName         If the dataset input came from a file,
         *                         this gives the file name; null means
         *                         the input didn't come from a file
         * @param message          Detailed information about the problem
         *                         (null if no details are available)
         */
        public SyntaxError(String fileName, String message) {
            super("syntax error in dataset"
                    + ((fileName != null) ? (" (file \"" + fileName + "\")")
                    : "")
                    + ((message != null) ? (": " + message) : ""));
        }
    }

    /**
     * UnsupportedFormat is thrown when newFileInstance is passed a
     * file name with an unrecognized extension.
     */
    public static class UnsupportedFormatError extends Error {
        /**
         * Construct an UnsupportedFormatError with a message that identifies
         * the problem file.
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
     * {@code get} has the wrong type (e.g., a string value was
     * needed but a nested dataset was found).
     */
    public static class WrongTypeError extends Error {
        /**
         * Construct a WrongTypeError with a given message.
         * @param message          Message to use for the exception
         */
        public WrongTypeError(String message) {
            super(message);
        }
    }

    /**
     * UnknownFileFormatError is thrown when {@code writeFile} on a
     * Dataset for which there is no on-disk representation defined
     * (such as this class).
     */
    public static class UnknownFileFormatError extends Error {
        /**
         * Construct a UnknownFileFormatError.
         * @param message          Message to use for the exception
         */
        public UnknownFileFormatError(String message) {
            super(message);
        }
    }

    // The following class is used to return information from the
    // lookupParent method.
    protected class ParentInfo {
        public HashMap parentMap;  // HashMap corresponding to the portion
                                   // of the path up to its final element,
                                   // or null if there is no such HashMap.
        public String lastName;    // The final element in the path.

        public ParentInfo(HashMap parentMap, String lastName) {
            this.parentMap = parentMap;
            this.lastName = lastName;
        }
    }

    // The following field holds the contents of the dataset.  Keys
    // are strings, and values can have any of the following types:
    // String:                     simple value
    // HashMap:                    nested dataset
    // ArrayList:                  list of nested datasets; each element of
    //                             the list is a HashMap.
    // It would be better if this field could be declared as
    // HashMap<String,Object>; unfortunately that won't work, because
    // we use JYaml to read YAML datasets directly into the map and JYaml
    // declares its HashMaps without the <String,Object>.
    protected HashMap map;

    // The following field points to another dataset, which is searched
    // (along with its chain) for any values not found in this dataset.
    // Null means there is no chain for the dataset.
    protected Dataset chain;

    // If this dataset originated in a file the following variable contains
    // the name of the file; otherwise it is null.  Used for error messages.
    protected String fileName;

    // Used to simulate I/O errors during testing.
    protected boolean generateIoException = false;

    /**
     * Creates an empty dataset.
     */
    public Dataset() {
        map = new HashMap();
    }

    /**
     * Creates a dataset from keys and values passed as arguments.
     * @param keysAndValues        Alternating keys and values for
     *                             initializing the Dataset; there must be
     *                             an even number of arguments.
     */
    @SuppressWarnings("unchecked")
    public Dataset(String... keysAndValues) {
        map = new HashMap();
        int last = keysAndValues.length - 2;
        for (int i = 0; i <= last; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i+1]);
        }
    }

    /**
     * Private constructor, used by checkValue, newFileInstance, and other
     * methods.
     * @param contents             HashMap holding the contents of the
     *                             dataset.
     * @param fileName             If the dataset was read from a file this
     *                             gives the file name (if known); otherwise
     *                             this is null.
     */
    protected Dataset(HashMap contents, String fileName) {
        map = contents;
        this.fileName = fileName;
    }

    /**
     * Creates a Dataset from information in a file.
     * @param fileName             Name of a dataset file in a supported
     *                             format; the format is inferred from the
     *                             file's extension (e.g. ".yaml" means the
     *                             file is in YAML format).  If the name
     *                             doesn't contain an extension, this method
     *                             tries all supported extensions until it
     *                             finds a dataset file that exists.
     * @return                     New Dataset object containing contents
     *                             of {@code fileName}
     */
    public static Dataset newFileInstance(String fileName) {
        String extension = Util.fileExtension(fileName);
        if (extension == null) {
            String newName = Util.findFileWithExtension(fileName,
                    ".yaml", ".yml", ".xml");
            if (newName == null) {
                throw new FileNotFoundError(fileName, "dataset",
                        "couldn't find a file with a supported extension");
            }
            fileName = newName;
            extension = Util.fileExtension(fileName);
        }
        if (extension.equals(".yml") || extension.equals(".yaml")) {
            return YamlDataset.newFileInstance(fileName);
        }
        if (extension.equals(".xml")) {
            return XmlDataset.newFileInstance(fileName);
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
     *                             {@code path} (a different extension
     *                             may be chosen for each directory).
     * @param path                 Directories to search for the dataset.
     * @param pathHandling         If {@code name} exists in more than
     *                             one directory in {@code path}, this
     *                             parameter indicates whether to use just the
     *                             first dataset found or chain them together.
     * @return                     A new Dataset object.
     */
    public static Dataset newFileInstanceFromPath(String name,
            String[] path, PathHandling pathHandling) {
        Dataset first = null, last = null;
        for (int i = 0; i < path.length; i++) {
            String fullName = path[i] + "/" + name;
            try {
                Dataset current = Dataset.newFileInstance(fullName);
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
            throw FileNotFoundError.newPathInstance(name, "dataset", path);
        }
        return first;
    }

    /**
     * Given an existing dataset, adds it to {@code this} as a top-level
     * child.  However, if there already exist one or more top-level children
     * by the same name then the new child is added to them to form a list,
     * with the new child at the end of the list.
     * @param key                  Name of a value in the top level of the
     *                             dataset (not a path).  If this key is
     *                             already defined as a string value, the
     *                             existing value will be discarded.
     * @param child                Existing dataset, which will become a
     *                             child of {@code this}.
     */
    @SuppressWarnings("unchecked")
    public void addChild(String key, Dataset child) {
        Object existingValue = map.get(key);
        if (existingValue instanceof HashMap) {
            // There exists a single child by the given name; turn this
            // into a list of children.
            ArrayList list = new ArrayList(2);
            list.add(existingValue);
            list.add(child.map);
            map.put(key, list);
        } else if (existingValue instanceof ArrayList) {
            // There's already a list of children; just add the new one.
            ArrayList list = (ArrayList) existingValue;
            list.add(child.map);
        } else {
            // Either the value doesn't exist or it is a string value;
            // in either case, ignore it and use the new child as the value.
            map.put(key, child.map);
        }
    }

    /**
     * Given a key, returns the value associated with that key, or null
     * if the key is not defined.  This method is identical to
     * {@code get} except that it does not generate an exception
     * if the key is undefined or has the wrong type.
     * @param key                  Name of the desired value
     * @return                     Value associated with {@code key}, or
     *                             null if {@code key} doesn't exist or if
     *                             it corresponds to a nested dataset.
     */
    public String check(String key) {
        try {
            return (String) lookup(key, DesiredType.STRING);
        }
        catch (WrongTypeError e) {
            return null;
        }
    }

    /**
     * Delete all of the contents of a dataset, leaving the dataset
     * empty.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Generates and returns a "deep copy" of the dataset, such that
     * no modification to either dataset will be visible in the other.
     * @return                     A copy of the current dataset.
     */
    public Dataset clone() {
        Dataset result = new Dataset(cloneHelper(map, null), fileName);
        result.chain = this.chain;
        return result;
    }

    /**
     * Indicates whether a key exists in the dataset.
     * @param key                  Name of the desired value
     * @return                     True if the key exists in the top
     *                             level of the dataset, false otherwise
     */
    public boolean containsKey(String key) {
        if (map.get(key) != null) {
            return true;
        }
        if (chain != null) {
            return chain.containsKey(key);
        }
        return false;
    }

    /**
     * Copies all of the top-level values from another dataset into
     * this one, replacing any existing values with the same names.
     * Nested datasets and lists are cloned before copying so that the
     * datasets remain independent.
     * @param source              Dataset whose values are to be copied
     *                            into this one.
     */
    public void copyFrom(Dataset source) {
        cloneHelper(source.map, map);
    }

    /**
     * Returns a nested dataset if it exists, creates a new dataset
     * if the child doesn't exist.  If {@code key} corresponds to a
     * list of nested datasets than the first child in the list is returned.
     * If {@code key} corresponds to a string value then the string value
     * is replaced with a new empty dataset.
     * @param key                  Name of the desired child (in the
     *                             top-level dataset; not a path).
     * @return                     Dataset corresponding to {@code key}.
     */
    public Dataset createChild(String key) {
        return createChildInternal(map, key);
    }

    /**
     * Creates a nested dataset with a given name, overwriting any previous
     * value by that name.  The nested dataset will refer to {@code dataset},
     * which means that future changes to {@code dataset} will be visible
     * in this Dataset object.  If you don't want this behavior, clone
     * {@code dataset} before calling this method.
     * @param key                  Name of a nested dataset within the
     *                             top-level dataset (not a path).
     * @param dataset              Same as the {@code dataset} argument.
     */
    @SuppressWarnings("unchecked")
    public Dataset createChild(String key, Dataset dataset) {
        map.put(key, dataset.map);
        return new Dataset(dataset.map, fileName);
    }

    /**
     * If a path refers to a nested dataset, returns that nested dataset;
     * if the path already has a defined value that is a list or string
     * value, replaces the old value with a new empty dataset.  If the path
     * doesn't currently exist, creates a new empty dataset at the specified
     * location, filling in any missing parent datasets along the path.
     * @param path                 One or more keys, separated by dots.
     * @return                     Dataset corresponding to {@path}.
     */
    public Dataset createChildPath(String path) {
        ParentInfo info = lookupParent(path, true);
        return createChildInternal(info.parentMap, info.lastName);
    }

    /**
     * Creates a nested dataset whose location is given by path,
     * overwriting any previous value by that name.  The nested dataset
     * will refer to {@code dataset}, which means that future changes to
     * {@code dataset} will be visible in this Dataset object.  If you
     * don't want that behavior, clone {@code dataset} before calling
     * this method.
     * @param path                 One or more keys, separated by dots.
     * @param dataset              Contents for the nested dataset.
     * @return                     Dataset corresponding to {@path}.
     */
    @SuppressWarnings("unchecked")
    public Dataset createChildPath(String path, Dataset dataset) {
        ParentInfo info = lookupParent(path, true);
        info.parentMap.put(info.lastName, dataset.map);
        return new Dataset(dataset.map, fileName);
    }

    /**
     * Removes an entry (or nested dataset) from the top level of a dataset.
     * If there is no value with the given {@code key} then the method
     * does nothing.  This method operates only on the original dataset:
     * it does not search its chain.
     * @param key                  Name of a value in the top level of the
     *                             dataset (not a path).
     */
    public void delete(String key) {
        map.remove(key);
    }

    /**
     * Given a path in a dataset, removes the corresponding value (or
     * nested dataset) if it exists.  If the path is not defined then
     * this method does nothing.  This method operates only on the original
     * dataset: it does not search its chain.
     * @param path                 A sequence of keys separated by dots.
     */
    public void deletePath(String path) {
        ParentInfo info = lookupParent(path, false);
        if (info != null) {
            info.parentMap.remove(info.lastName);
        }
    }

    /**
     * Given a key, returns the value associated with that key.  This is
     * a single-level lookup: the key must be defined in the top level of
     * the dataset.
     * @param key                  Name of the desired value
     * @return                     Value associated with {@code key}
     * @throws MissingValueError   Thrown if {@code key} can't be found
     * @throws WrongTypeError      Thrown if {@code key} corresponds
     *                             to a nested dataset rather than a string
     *                             value.e, since
     *                             we don't support nested datasets)
     */
    public String get(String key) throws MissingValueError, WrongTypeError {
        Object child = lookup(key, DesiredType.STRING);
        if (child == null) {
            throw new MissingValueError(key);
        }
        return (String) child;
    }

    /**
     * Returns the dataset's chain (the value passed to the last call to
     * setChain).
     * @return                     Next Dataset to search for values not found
     *                             in the Dataset (null if none).
     */
    public Dataset getChain() {
        return chain;
    }

    /**
     * Create a new Dataset corresponding to a nested dataset within the
     * current dataset.
     * @param key                  Name of the desired child; must be a
     *                             top-level child within the current dataset
     * @return                     A Dataset providing access to the child
     * @throws MissingValueError   Thrown if {@code key} is not defined
     *                             at the top level of the current dataset
     * @throws WrongTypeError      Thrown if {@code key} is defined but
     *                             corresponds to a string value rather than
     *                             a nested dataset
     */
    public Dataset getChild(String key)
            throws MissingValueError, WrongTypeError {
        Object child = lookup(key, DesiredType.DATASET);
        if (child == null) {
            throw new MissingValueError(key);
        }
        return (Dataset) child;
    }

    /**
     * Traverse a hierarchical sequence of keys to find a nested dataset.
     * @param path                 A sequence of keys separated by dots.
     * @return                     A Dataset providing access to the child
     * @throws MissingValueError   Thrown if {@code key} is not defined
     *                             at the top level of the current dataset
     * @throws WrongTypeError      Thrown if {@code key} is defined but
     *                             corresponds to a string value rather than
     *                             a nested dataset
     */
    public Dataset getChildPath(String path)
            throws MissingValueError, WrongTypeError {
        Object child = lookupPath(path, DesiredType.DATASET);
        if (child == null) {
            throw new MissingValueError(path);
        }
        return (Dataset) child;
    }

    /**
     * Generate an array of Datasets corresponding to all of the
     * children by a given name.
     * @param key                  Name of the desired child; must be a
     *                             top-level child within the current dataset
     * @return                     An array of Datasets, one for each child
     *                             dataset corresponding to {@code key};
     *                             the array will be empty if there are no
     *                             children corresponding to {@code key}
     * @throws WrongTypeError      Thrown if {@code key} refers to a
     *                             string value rather than nested datasets
     */
    public Dataset[] getChildren(String key) throws WrongTypeError {
        Object children = lookup(key, DesiredType.DATASETS);
        if (children == null) {
            return new Dataset[0];
        }
        return (Dataset[]) children;
    }

    /**
     * Traverse a hierarchical sequence of keys to find a collection of
     * nested datasets with the same name.
     * @param path                 Path to the desired descendent (s); must
     *                             be a sequence of keys separated by dots.
     * @return                     An array of Datasets, one for each
     *                             descendant dataset corresponding to
     *                             {@code path}; the array will be empty
     *                             if there are no children corresponding to
     *                             {@code path}
     * @throws WrongTypeError      Thrown if {@code have} refers to a
     *                             string value rather than nested datasets
     */
    public Dataset[] getChildrenPath(String path) throws WrongTypeError {
        Object children = lookupPath(path, DesiredType.DATASETS);
        if (children == null) {
            return new Dataset[0];
        }
        return (Dataset[]) children;
    }

    /**
     * If this dataset was originally read from a file, this method
     * will provide the name of that file.
     * @return                     The name of the file from which the dataset
     *                             was originally read, or null if the dataset
     *                             was not created from a file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns a Set containing all of the top-level keys in the dataset.
     * @return                     All of the keys at the top level of
     *                             the dataset.
     */
    @SuppressWarnings("unchecked")
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * This is a general-purpose method to find the value associated with a
     * given key, intended primarily for use by other methods such as
     * {@code get} and {@code getChildren}.  The value must be
     * present in the top level of the dataset (i.e., key is not a path;
     * use lookupPath if it is).  This method searches both the dataset
     * and its chain.
     * @param key                  Name of the desired value
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     If {@code key} exists and its value
     *                             matches {@code desiredResult} then
     *                             it is returned as a String, Dataset,
     *                             or Dataset[] for {@code desiredResult}
     *                             values of STRING, DATASET, and DATASETS,
     *                             respectively.  If {@code key} doesn't
     *                             exist then null is returned.
     * @throws WrongTypeError      Thrown if {@code key} is defined but
     *                             its value doesn't correspond to
     *                             {@code desiredResult}.
     */
    public Object lookup(String key, DesiredType wanted)
            throws WrongTypeError {
        Object child = map.get(key);
        if (child != null) {
            return checkValue(key, wanted, child);
        }
        if (chain != null) {
            return chain.lookup(key, wanted);
        }
        return null;
    }

    /**
     * This is a general-purpose method to find the value associated with a
     * hierarchical path, intended primarily for use by other methods such as
     * {@code getPath} and {@code getPathChildren}.  This method searches
     * both the dataset and its chain.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, "a.b.c" refers to a value
     *                             "c" contained in a nested dataset "b"
     *                             contained in a dataset "a" contained
     *                             in the current dataset
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     If the desired value exists and matches
     *                             {@code desiredResult} then it is returned
     *                             as a String, Dataset, or Dataset[] for
     *                             {@code desiredResult} values of STRING,
     *                             DATASET, and DATASETS, respectively.  If
     *                             the dataset doesn't contain a value
     *                             corresponding to {@code path} then null
     *                             is returned.
     * @throws WrongTypeError      Thrown if {@code path} is defined but
     *                             its value doesn't correspond to
     *                             {@code desiredResult}
     */
    public Object lookupPath(String path, DesiredType wanted)
            throws WrongTypeError {
        int startIndex = 0;
        int length = path.length();
        Object currentObject = map;
        String key;

        // Each iteration through the following loop extracts the next
        // key from path and looks it up.
        while (true) {
            int dot = path.indexOf('.', startIndex);
            if (dot == -1) {
                dot = length;
            }
            key = path.substring(startIndex, dot);
            currentObject = ((HashMap) currentObject).get(key);
            if (currentObject == null) {
                // The current key doesn't exist.  If this was the first
                // element in the path then check chained datasets, if
                // any.  However, if we successfully located the first
                // element in the path then its contents override any
                // chained datasets so stop here.
                if ((startIndex == 0) && (chain != null)) {
                    return chain.lookupPath(path, wanted);
                }
                return null;
            }
            startIndex = dot+1;
            if (startIndex >= length) {
                break;
            }

            // This is not the last key; make sure that the current object
            // is a nested dataset.
            if (currentObject instanceof ArrayList) {
                // The child consists of a list of nested datasets; take
                // the first one.
                currentObject = ((ArrayList) currentObject).get(0);
            }
            if (!(currentObject instanceof HashMap)) {
                // This key refers to a string value but we need a
                // nested dataset.  Handle it the same as if the key
                // didn't exist.
                if ((startIndex == 0) && (chain != null)) {
                    return chain.lookupPath(path, wanted);
                }
                return null;
            }
        }

        // At this point we have found the final value.
        return checkValue(path, wanted, currentObject);
    }

    /**
     * Traverse a hierarchical sequence of keys to find a string value.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, "a.b.c" refers to a value
     *                             "c" contained in a nested dataset "b"
     *                             contained in a dataset "a" contained
     *                             in the current dataset
     * @return                     Value associated with {@code path}
     * @throws MissingValueError   Thrown if one of the keys in {@code path}
     *                             doesn't exist
     * @throws WrongTypeError      Thrown if one of the keys in {@code path}
     *                             has the wrong type (nested dataset vs.
     *                             string value)
     */

    public String getPath(String path)
            throws MissingValueError, WrongTypeError {
        Object child = lookupPath(path, DesiredType.STRING);
        if (child == null) {
            throw new MissingValueError(path);
        }
        return (String) child;
    }

    /**
     * Sets a value in the top level of a dataset.  If the value already
     * exists then it is replaced (even if the value represents a nested
     * dataset).
     * @param key                  Name of a value in the top-level of the
     *                             dataset (not a path).
     * @param value                New value to associate with the key.
     */
    @SuppressWarnings("unchecked")
    public void set(String key, String value) {
        map.put(key, value);
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
     * Returns the number of values stored in the top level of this Dataset.
     * @return                     Number of values in the Dataset; if
     *                             the dataset is hierarchical, only
     *                             top-level values are counted
     */
    public int size() {
        return map.size();
    }

    /**
     * Generates a nicely formatted string displaying the contents
     * of the dataset (intended primarily for testing).  By default
     * the dataset is formatted using YAML syntax.
     * @return                     Pretty-printed string.
     */
    public String toString() {
        return YamlDataset.writeString(this);
    }

    /**
     * Writes the contents of the dataset to a file on disk.  This particular
     * class doesn't have a defined disk format, so it throws an Error
     * @param name                 Name of file to write.
     * @param comment              Optional text describing the meaning of
     *                             the file for humans who might stumble
     *                             across it.  Null means no comment.
     */
    public void writeFile(String name, String comment) {
        throw new UnknownFileFormatError("class " +
                getClass().getSimpleName() +
                " doesn't know how to write datasets to files");
    }

    /**
     * This method is invoked by the lookup functions to make sure
     * that the value has the type desired by the caller; it also performs
     * conversions, such as creating a new Dataset for the value.
     * @param name                 Name for the value (either a single
     *                             key or a hierarchical path); used for
     *                             error messages
     * @param wanted               The type of value requested by the
     *                             code that called the lookup function
     * @param value                Dataset value corresponding to
     *                             {@code name}.
     * @return                     If {@code value} matches {@code wanted}
     *                             then it is returned as a String, Dataset,
     *                             or Dataset[] for {@code wanted} values
     *                             of STRING, DATASET, and DATASETS,
     *                             respectively.
     * @throws WrongTypeError      Thrown if {@code value} doesn't
     *                             correspond to {@code wanted}.
     */

    protected final Object checkValue(String name, DesiredType wanted,
            Object value) throws WrongTypeError {
        if (value instanceof String) {
            if ((wanted == DesiredType.STRING)
                    || (wanted == DesiredType.ANYTHING)) {
                return value;
            }
            throw new WrongTypeError(wrongTypeMessage(name, wanted, value));
        }
        if (wanted != DesiredType.STRING) {
            if (value instanceof HashMap) {
                if (wanted == DesiredType.DATASETS) {
                    Dataset[] result = new Dataset[1];
                    result[0] = new Dataset((HashMap) value, fileName);
                    return result;
                }
                return new Dataset((HashMap) value, fileName);
            } else if (value instanceof ArrayList) {
                if (wanted == DesiredType.DATASET) {
                    // The value consists of a list of values; take the first
                    // one (only if it is a HashMap)

                    Object child2 = ((ArrayList) value).get(0);
                    if (child2 instanceof HashMap) {
                        return new Dataset((HashMap) child2, fileName);
                    }
                } else {
                    ArrayList a = (ArrayList) value;
                    Dataset[] result = new Dataset[a.size()];
                    for (int i = 0; i < result.length; i++) {
                        Object listElement = a.get(i);
                        if (!(listElement instanceof HashMap)) {
                            throw new WrongTypeError(wrongTypeMessage(name,
                                    wanted, listElement));
                        }
                        result[i] = new Dataset((HashMap) a.get(i), fileName);
                    }
                    return result;
                }
            }
        }

        // We get here if value has an unrecognized type or if
        // desiredType is DATASET and child refers to a list of things that
        // aren't HashMaps.
        throw new WrongTypeError(wrongTypeMessage(name, wanted, value));
    }

    /**
     * This internal method does most of the work of the {@code clone}
     * method.  It returns a deep copy of {@code source}, calling itself
     * recursively to copy nested datasets.
     * @param source               Object to copy; contents must conform
     *                             to those expected for a dataset.
     * @param dest                 Copy everything from {@code source} to
     *                             this HashMap; if null, a new HashMap
     *                             is created and used as the destination
     * @return                     A clone of {@code source}.
     */
    @SuppressWarnings("unchecked")
    protected HashMap cloneHelper(HashMap source, HashMap dest) {
        if (dest == null) {
            dest = new HashMap();
        }

        // Each iteration through the following loop copies one key-value
        // pair from source to dest; it will invoke this method recursively
        // to copy the contents of nested datasets.
        for (Map.Entry<String,Object> pair :
                ((HashMap<String,Object>) source).entrySet()) {
            Object value = pair.getValue();
            if (value instanceof String) {
                dest.put(pair.getKey(), value);
            } else if (value instanceof HashMap) {
                dest.put(pair.getKey(), cloneHelper((HashMap) value, null));
            } else if (value instanceof ArrayList) {
                ArrayList sourceList = (ArrayList) value;
                ArrayList destList = new ArrayList(sourceList.size());
                for (Object value2 : sourceList) {
                    destList.add(cloneHelper((HashMap) value2, null));
                }
                dest.put(pair.getKey(), destList);
            }
        }
        return dest;
    }

    /**
     * This shared utility method does most of the real work for
     * createChild and createChildPath.
     * @param map                  HashMap containing the desired child
     *                             dataset.
     * @param key                  Name of the desired nested dataset.
     * @return                     Dataset corresponding to the child;
     *                             refers to an existing nested dataset, if
     *                             there was one, or to a newly created
     *                             nested dataset otherwise.
     */
    @SuppressWarnings("unchecked")
    protected Dataset createChildInternal(HashMap map, String key) {
        Object child = map.get(key);
        if (child instanceof HashMap) {
            return new Dataset((HashMap) child, fileName);
        }
        if (child instanceof ArrayList) {
            return new Dataset((HashMap) ((ArrayList) child).get(0), fileName);
        }
        child = new HashMap();
        map.put(key, child);
        return new Dataset((HashMap) child, fileName);
    }

    /**
     * Given a path, find the hash table that contains the element named in
     * the path (if there is one) and return it along with the final
     * name in the past.  This method is used internally by several other
     * methods, such as createPath and deletePath.  This method looks only
     * in the primary dataset; it does not examine the chain.
     * @param path                 A sequence of keys separated by dots.
     * @param create               True means this the method is being invoked
     *                             as part of a "create" operation: if any
     *                             of the ancestors of {@code path} don't
     *                             exist then they are created, overwriting
     *                             any string values they used to have).
     *                             False means just return a null if either
     *                             of these problems occurs.
     * @return                     ParentMap structure with information
     *                             corresponding to {@code path}, or null
     *                             if the parent doesn't exist.
     */
    @SuppressWarnings("unchecked")
    protected ParentInfo lookupParent(String path, boolean create)
            throws WrongTypeError {
        int startIndex = 0;
        int length = path.length();
        HashMap parent = map;
        Object child;
        String key;
        int dot;

        // Each iteration through the following loop extracts the next
        // key from path and looks it up.
        while (true) {
            dot = path.indexOf('.', startIndex);
            if (dot == -1) {
                break;
            }
            key = path.substring(startIndex, dot);
            child = parent.get(key);

            // Make sure that the current object is a nested dataset.
            if (child instanceof ArrayList) {
                // The child consists of a list of nested datasets; take
                // the first one.
                child = ((ArrayList) child).get(0);
            }
            if (!(child instanceof HashMap)) {
                // Child doesn't exist or has a string value.
                if (!create) {
                    return null;
                }
                child = new HashMap();
                parent.put(key, child);
            }
            parent = (HashMap) child;
            startIndex = dot+1;
        }

        // At this point we have found the parent.
        return new ParentInfo(parent, path.substring(startIndex));
    }

    /**
     * Invoked when other methods encounter values that have the wrong
     * type (e.g., expected a string value but found a nested dataset);
     * generates an appropriate message to use in a WrongTypeError exception.
     * @param name                 Name for the entity that had the wrong
     *                             type (a top-level key or a hierarchical
     *                             path)
     * @param wanted               The type of entity that was desired
     * @param got                  The object that was encountered, which
     *                             didn't match {@code wanted}
     * @return                     An appropriate message to use in a
     *                             WrongTypeError exception.
     */
    protected static String wrongTypeMessage(String name, DesiredType wanted,
            Object got) {
        String gotType;
        if (got instanceof HashMap) {
            gotType = "nested dataset";
        } else if (got instanceof ArrayList) {
            gotType = "list";
        } else {
            gotType = "string value \"" + Util.excerpt(got.toString(), 20)
                    + "\"";
        }
        return "wrong type for dataset element \"" + name + "\": expected "
                + ((wanted == DesiredType.STRING) ? "string value"
                : "nested dataset") + " but found " + gotType;
    }
}
