package org.fiz;
import java.io.*;
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
 *   - Values can have two forms:
 *     - A string
 *     - A list of one or more nested datasets, each of which contains
 *       additional key-value pairs).
 *   - Datasets can be created from a variety of sources, such as
 *     XML and YAML files (see classes such as XmlDataset and
 *     YamlDataset for details).
 */

public class Dataset implements Cloneable {
    /**
     * Instances of this enum are passed to Dataset.lookup to indicate
     * what sort of value is desired as a result.
     */
    public enum DesiredType {
        /**
         * The caller is only interested in string values.
         */
        STRING,

        /**
         * The caller is only interested in nested datasets.
         */
        DATASET,

        /**
         * The caller is interested in both string values and nested
         * datasets.
         */
        ANY
    }

    /**
     * For methods such as lookup and newFileInstanceFromPath where
     * there may be more than one matching result, this enum indicates
     * how many of them should be returned.
     */
    public enum Quantity {
        /**
         * Return only the first matching result.
         */
        FIRST_ONLY,

        /**
         * Return all matching results.
         */
        ALL
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

    // If this dataset originated in a file the following variable contains
    // the name of the file; otherwise it is null.  Used for error messages.
    protected String fileName;

    // Used to simulate I/O errors during testing.
    protected boolean generateIoException = false;

    /**
     * Construct an empty dataset.
     */
    public Dataset() {
        map = new HashMap();
    }

    /**
     * Construct a dataset from keys and values passed as arguments.
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
     * Construct a dataset from keys and values passed as arguments,
     * where each value can be either a string or a nested dataset.d
     * @param keysAndValues        An even number of argument objects;
     *                             the first argument of each pair must be
     *                             a string key and the second argument of
     *                             each pair must be a value for that key
     *                             (either a String or a Dataset).  To
     *                             create multiple nested datasets with the
     *                             same name, use multiple key/value pairs
     *                             with the same key.
     */
    @SuppressWarnings("unchecked")
    public Dataset(Object... keysAndValues) {
        map = new HashMap();
        int last = keysAndValues.length - 2;
        for (int i = 0; i <= last; i += 2) {
            String key = (String) keysAndValues[i];
            Object value = keysAndValues[i+1];
            if (value instanceof String) {
                map.put(key, (String) value);
            } else {
                addChild(key, (Dataset) value);
            }
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
        String extension = StringUtil.fileExtension(fileName);
        if (extension == null) {
            String newName = Util.findFileWithExtension(fileName,
                    ".yaml", ".yml", ".xml");
            if (newName == null) {
                throw new FileNotFoundError(fileName, "dataset",
                        "couldn't find a file with a supported extension");
            }
            fileName = newName;
            extension = StringUtil.fileExtension(fileName);
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
     * @param quantity             If {@code name} exists in more than
     *                             one directory in {@code path}, this
     *                             parameter indicates whether to return the
     *                             first dataset found or a CompoundDataset
     *                             containing all of them.
     * @return                     A new Dataset object.
     */
    public static Dataset newFileInstanceFromPath(String name,
            String[] path, Quantity quantity) {
        ArrayList<Dataset> datasets = null;
        for (String directory : path) {
            String fullName = directory + "/" + name;
            try {
                Dataset current = Dataset.newFileInstance(fullName);
                if (quantity == Quantity.FIRST_ONLY) {
                    return current;
                }

                // Build up a list of all the datasets that were found.
                if (datasets == null) {
                    datasets = new ArrayList<Dataset>(path.length);
                }
                datasets.add(current);
            }
            catch (FileNotFoundError e) {
                // There isn't a relevant dataset in this directory;
                // skip it and go on to the next directory.
                continue;
            }
        }
        if (datasets == null) {
            throw FileNotFoundError.newPathInstance(name, "dataset", path);
        }
        if (datasets.size() == 1) {
            return datasets.get(0);
        }
        return new CompoundDataset(datasets);
    }

    /**
     * Create a new dataset from a serialized dataset of the sort generated
     * by {@code serialize}.
     * @param source               Contains a serialized dataset starting at
     *                             the first character.  If the serialized
     *                             dataset ends before the end of
     *                             {@code source}, the information after
     *                             the serialized data set is ignored.
     * @return                     A dataset whose contents match the
     *                             serialized dataset in {@code source}.
     */
    public static Dataset newSerializedInstance(CharSequence source) {
        Dataset d = new Dataset();
        d.addSerializedData(source, 0);
        return d;
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
     * Read a serialized dataset and add its contents to the current dataset.
     * If there are conflicts between values in the serialized dataset
     * and this dataset (e.g., a name in one dataset has a string value, and
     * in the other dataset its value is a nested dataset) the values
     * from the serialized data set replaces the existing values.
     * @param source               Contains a serialized dataset, in the
     *                             syntax generated by {@code serialize}.
     * @param start                Index in {@code source} of the "("
     *                             character that starts the encoded dataset.
     * @return                     Index of the character just after the
     *                             ")" that ends a serialized dataset.
     */
    protected int addSerializedData(CharSequence source, int start) {
        IntBox end = new IntBox();
        int i = start;
        int length = source.length();

        if ((i >= length) || (source.charAt(i) != '(')) {
            throw new SyntaxError(null,
                    "serialized dataset didn't start with \"(\"");
        }
        i++;

        // Each iteration through the following loop parses one top-level
        // element for the dataset, which can be either a string value
        // a single nested dataset, or a list of nested datasets.
        while (i < length) {
            // See if we have reached the end of this dataset.
            if (source.charAt(i) == ')') {
                return i+1;
            }

            // Get the name of the next element.
            String name = getEncodedString(source, i, end);
            i = end.value;

            // See if this is a simple value or nested dataset(s).
            if (i >= length) {
                throw new SyntaxError(null,
                        "no value for element \"" + name + "\" in " +
                        "serialized dataset");
            }
            if (source.charAt(i) != '(') {
                // String value.
                set(name, getEncodedString(source, i, end));
                i = end.value;
            } else {
                // Nested dataset(s).  At the start of each iteration
                // {@code i} refers to the "(" that starts the next child
                // dataset.
                while (true) {
                    Dataset child = new Dataset();
                    i = child.addSerializedData(source, i);
                    addChild(name, child);
                    if ((i >= length) || (source.charAt(i) != '(')) {
                        break;
                    }
                }
            }
            if ((i < length) && (source.charAt(i) == '\n')) {
                i++;
            }
        }
        throw new SyntaxError(null,
                "serialized dataset not terminated by \")\"");
    }

    /**
     * Read a serialized dataset and add its contents to the current dataset.
     * If there are conflicts between values in the serialized dataset
     * and this dataset (e.g., a name in one dataset has a string value, and
     * in the other dataset its value is a nested dataset) the values
     * from the serialized data set replaces the existing values.
     * @param source               Contains a serialized dataset, in the
     *                             syntax generated by {@code serialize}.
     */
    protected void addSerializedData(CharSequence source) {
        addSerializedData(source, 0);
    }

    /**
     * Given a key, returns the value associated with that key, or null
     * if the key is not defined.  This method is identical to
     * {@code get} except that it does not generate an exception
     * if the key is undefined or has the wrong type.
     * @param key                  Name of the desired value.
     * @return                     Value associated with {@code key}, or
     *                             null if {@code key} doesn't exist or if
     *                             it corresponds to a nested dataset.
     */
    public String check(String key) {
        return (String) lookup(key, DesiredType.STRING, Quantity.FIRST_ONLY,
                null);
    }

    /**
     * Given a key, returns the first nested dataset with that name,
     * or null if there is no such child.  This method is identical to
     * {@code getChild} except that it does not generate an exception
     * if there is no child by the given name.
     * @param key                  Name of the desired child dataset.
     * @return                     First child dataset with {@code key}
     *                             as name, or null if there is no such
     *                             child.
     */
    public Dataset checkChild(String key) {
        return (Dataset) lookup(key, DesiredType.DATASET, Quantity.FIRST_ONLY,
                null);
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
        return result;
    }

    /**
     * Indicates whether a key exists in the dataset.
     * @param key                  Name of the desired value.
     * @return                     True if the key exists in the top
     *                             level of the dataset, false otherwise.
     */
    public boolean containsKey(String key) {
        if (map.get(key) != null) {
            return true;
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
     * @param dataset              Dataset to associate with {@code key}.
     * @return                     Same as the {@code dataset} argument.
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
     * @return                     Dataset corresponding to {@code path}.
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
     * @return                     Dataset corresponding to {@code path}.
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
     * does nothing.
     * @param key                  Name of a value in the top level of the
     *                             dataset (not a path).
     */
    public void delete(String key) {
        map.remove(key);
    }

    /**
     * Given a path in a dataset, removes the corresponding value (or
     * nested dataset) if it exists.  If the path is not defined then
     * this method does nothing.
     * @param path                 A sequence of keys separated by dots.
     */
    public void deletePath(String path) {
        ParentInfo info = lookupParent(path, false);
        if (info != null) {
            info.parentMap.remove(info.lastName);
        }
    }

    /**
     * Treat the current dataset as a template and create a new dataset
     * by expanding the current dataset in the context of an auxiliary
     * dataset containing values.  The template data set is expanded as
     * follows.  For each <i>name</i>,<i>value</i> pair in the current
     * dataset (and its nested children), an identical
     * <i>name</i>,<i>value</i> pair is created in the result dataset
     * except for the following special cases:
     *   - If <i>value</i> starts with {@code @} then the remainder of
     *     the value is used as the path name of an entry in the auxiliary
     *     dataset; the value from the auxiliary dataset is used for the
     *     result dataset in place of <i>value</i>.
     *   - If <i>value</i> starts with {@code @@} then it is not treated as
     *     the name of an auxiliary value; <i>value</i> is passed through
     *     to the request dataset, except that the two {@code @} characters
     *     are collapsed into a single {@code @}.
     * @param aux                  Values from this dataset are used to
     *                             handle "@" references in the current
     *                             dataset.
     * @return                     A new dataset created from the current
     *                             dataset with substitutions from
     *                             {@code aux}.
     */
    public Dataset expand(Dataset aux) {
        Dataset result = new Dataset();
        for (String key : keySet()) {
            String value = check(key);
            if (value != null) {
                // This entry is a string value.  Perform substitution on it.
                if ((value.length() < 1) || (value.charAt(0) != '@')) {
                    // Choice #1: template value doesn't start with '@'; copy
                    // the template name and value through to the result.
                    result.set(key, value);
                } else if ((value.length() >= 2) && (value.charAt(1) == '@')) {
                    // Choice #2: template value starts with '@@'; convert
                    // the '@@' to '@' and copy through to the result.
                    result.set(key, value.substring(1));
                } else {
                    // Choice #3: template value starts with '@': use the
                    // value (excluding the '@') as the name of a value in
                    // {@code aux} and copy that through to the request
                    // dataset.
                    result.set(key, aux.getPath(value.substring(1)));
                }
            } else {
                // This entry consists of one or more nested datasets;
                // expand each of the nested dataset.
                for (Dataset child : getChildren(key)) {
                    result.addChild(key, child.expand(aux));
                }
            }
        }
        return result;
    }

    /**
     * Given a key, returns the value associated with that key.  This is
     * a single-level lookup: the key must be defined in the top level of
     * the dataset.
     * @param key                  Name of the desired value.
     * @return                     Value associated with {@code key}.
     * @throws MissingValueError   Thrown if {@code key} can't be found
     *                             or doesn't refer to a string value.
     */
    public String get(String key) throws MissingValueError {
        Object value = lookup(key, DesiredType.STRING, Quantity.FIRST_ONLY,
                null);
        if (value == null) {
            throw new MissingValueError(key);
        }
        return (String) value;
    }

    /**
     * Return the first nested dataset within the current dataset that
     * has a given name.
     * @param key                  Name of the desired child; must be a
     *                             top-level child within the current dataset.
     * @return                     A Dataset providing access to the child.
     * @throws MissingValueError   Thrown if {@code key} is not defined
     *                             at the top level of the current dataset
     *                             or that refers to a string value.
     */
    public Dataset getChild(String key) throws MissingValueError {
        Object child = lookup(key, DesiredType.DATASET, Quantity.FIRST_ONLY,
                null);
        if (child == null) {
            throw new MissingValueError(key);
        }
        return (Dataset) child;
    }

    /**
     * Traverse a hierarchical sequence of keys to find a nested dataset.
     * @param path                 A sequence of keys separated by dots.
     * @return                     The (first) nested dataset matching
     *                             {@code path}
     * @throws MissingValueError   Thrown if {@code path} is not defined
     *                             as a nested dataset within the current
     *                             dataset.
     */
    public Dataset getChildPath(String path) throws MissingValueError {
        Object child = lookupPath(path, DesiredType.DATASET,
                Quantity.FIRST_ONLY, null);
        if (child == null) {
            throw new MissingValueError(path);
        }
        return (Dataset) child;
    }

    /**
     * Find all of the nested datasets with a given name.
     * @param key                  Name of the desired child; must be a
     *                             top-level child within the current dataset
     * @return                     An ArrayList of Datasets, one for each
     *                             child dataset corresponding to
     *                             {@code key}; the ArrayList will be empty
     *                             if there are no children corresponding
     *                             to {@code key}.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Dataset> getChildren(String key) {
        Object children = lookup(key, DesiredType.DATASET,
                Quantity.ALL, null);
        if (children == null) {
            return new ArrayList<Dataset>();
        }
        return (ArrayList<Dataset>) children;
    }

    /**
     * Find all of the nested datasets that correspond to a hierarchical path.
     * @param path                 Path to the desired descendent (s); must
     *                             be a sequence of keys separated by dots.
     * @return                     An array of Datasets, one for each
     *                             descendant dataset corresponding to
     *                             {@code path}; the array will be empty
     *                             if there are no children corresponding to
     *                             {@code path}.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Dataset> getChildrenPath(String path) {
        Object children = lookupPath(path, DesiredType.DATASET,
                Quantity.ALL, null);
        if (children == null) {
            return new ArrayList<Dataset>();
        }
        return (ArrayList<Dataset>) children;
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
     * Traverse a hierarchical sequence of keys to find a string value.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, "a.b.c" refers to a value
     *                             "c" contained in a nested dataset "b"
     *                             contained in a dataset "a" contained
     *                             in the current dataset
     * @return                     Value associated with {@code path}
     * @throws MissingValueError   Thrown if there is no value at
     *                             {@code path}.
     */

    public String getPath(String path) throws MissingValueError {
        Object child = lookupPath(path, DesiredType.STRING,
                Quantity.FIRST_ONLY, null);
        if (child == null) {
            throw new MissingValueError(path);
        }
        return (String) child;
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
     * This is a general-purpose method to find the value(s) associated
     * with a given key, intended primarily for use by other methods such
     * as {@code get} and {@code getChildren}.  The value(s) must be
     * present in the top level of the dataset (i.e., {@code key} is not
     * a path; use lookupPath if it is).
     * @param key                  Name of the desired value.
     * @param wanted               Indicates what kind of value is desired
     *                             (string, nested dataset, or either).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @return                     The return value is null if no matching
     *                             value is found.  If {@code quantity} is
     *                             {@code FIRST_ONLY} then the return
     *                             value is a String or Dataset; otherwise
     *                             the return value is an ArrayList, each of
     *                             whose members is a String or Dataset.
     */
    public Object lookup(String key, DesiredType wanted, Quantity quantity) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return collectResults(value, wanted, quantity, null);
    }

    /**
     * This is a general-purpose method to find the value(s) associated
     * with a given key, intended primarily for use by other methods such
     * as {@code get} and {@code getChildren}.  The value(s) must be
     * present in the top level of the dataset (i.e., {@code key} is not
     * a path; use lookupPath if it is).
     * @param key                  Name of the desired value.
     * @param wanted               Indicates what kind of value is desired
     *                             (string, nested dataset, or either).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @param out                  If {@code quantity} is {@code ALL} and
     *                             this argument is non-null then the
     *                             matching values are appended to this
     *                             rather than creating a new ArrayList,
     *                             and the return value will be {@code out}.
     * @return                     The return value is null if no matching
     *                             value is found.  If {@code quantity} is
     *                             {@code FIRST_ONLY} then the return
     *                             value is a String or Dataset; otherwise
     *                             the return value is an ArrayList, each of
     *                             whose members is a String or Dataset.
     */
    public Object lookup(String key, DesiredType wanted, Quantity quantity,
            ArrayList<Object> out) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return collectResults(value, wanted, quantity, out);
    }

    /**
     * This is a general-purpose method to find one or more values associated
     * with a hierarchical path, intended primarily for use by other methods
     * such as {@code getPath} and {@code getPathChildren}.  There can be
     * multiple values assisted with a single path if some of the elements
     * of the path refer to nested datasets.  For example, if the element
     * {@code b} in the path {@code a.b.c} refers to 3 nested datasets
     * then there could be 3 values corresponding to {@code a.b.c}.  These
     * values need not necessarily be the same type: some could be string
     * values and others could be nested datasets.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, {@code a.b.c} refers to a value
     *                             {@code c} contained in a nested dataset
     *                             {@code b} contained in a dataset {@code a}
     *                             contained in the current dataset.
     * @param wanted               Indicates what kind of value is desired
     *                             (string, nested dataset, or either).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @return                     The return value is null if no matching
     *                             values are found.  Otherwise, if
     *                             {@code quantity} is {@code FIRST_ONLY}
     *                             then the return value is a String or
     *                             Dataset; otherwise the return value is
     *                             an ArrayList, each of whose members is
     *                             a String or Dataset.
     */
    public Object lookupPath(String path, DesiredType wanted,
            Quantity quantity) {
        return lookupPathHelper(path, 0, map, wanted, quantity, null);
    }

    /**
     * This is a general-purpose method to find one or more values associated
     * with a hierarchical path, intended primarily for use by other methods
     * such as {@code getPath} and {@code getPathChildren}.  There can be
     * multiple values assisted with a single path if some of the elements
     * of the path refer to nested datasets.  For example, if the element
     * {@code b} in the path {@code a.b.c} refers to 3 nested datasets
     * then there could be 3 values corresponding to {@code a.b.c}.  These
     * values need not necessarily be the same type: some could be string
     * values and others could be nested datasets.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, {@code a.b.c} refers to a value
     *                             {@code c} contained in a nested dataset
     *                             {@code b} contained in a dataset {@code a}
     *                             contained in the current dataset.
     * @param wanted               Indicates what kind of value is desired
     *                             (string, nested dataset, or either).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @param out                  If {@code quantity} is {@code ALL} and
     *                             this argument is non-null then the
     *                             matching values are appended to this
     *                             rather than creating a new ArrayList,
     *                             and the return value will be {@code out}.
     * @return                     The return value is null if no matching
     *                             values are found.  Otherwise, if
     *                             {@code quantity} is {@code FIRST_ONLY}
     *                             then the return value is a String or
     *                             Dataset; otherwise the return value is
     *                             an ArrayList, each of whose members is
     *                             a String or Dataset.
     */
    public Object lookupPath(String path, DesiredType wanted,
            Quantity quantity, ArrayList<Object> out) {
        return lookupPathHelper(path, 0, map, wanted, quantity, out);
    }

    /**
     * Generate a compact string representation for the dataset.
     * This form is intended for efficient transmission and storage;
     * it is not intended to be human readable.  The only code that
     * should attempt to understand the structure of this information is
     * the addSerializedData method of this class.
     * @param out                  A description of the dataset is
     *                             appended here.
     */
    public void serialize(StringBuilder out) {
        // Here is the grammar for the serialized representation of
        // a dataset.  The basic idea is to use run-length encoding,
        // where a string is represented as {@code length.contents},
        // where the contents of the string are preceded by its length
        // in characters and a "." to delimit the length.
        // <dataset> = "("<element>("\n"<element)*")"
        // <element> = <name><value>
        // <name> = <encodedString>
        // encodedString> = <length>"."<contents>
        // <value> = <encodedString> | (<dataset>)+
        //
        // Here's an example of a dataset containing a string value
        // {@code age} and a list of nested datasets named {@code children},
        // each with a {@code name} element.
        // (3.age2.24
        // 8.children(4.name5.Alice)(4.name3.Bob)(4.name5.Carol))
        serializeSubtree(map, out);
    }

    /**
     * Generate a compact string representation for the dataset.
     * This form is intended for efficient transmission and storage;
     * it is not intended to be human readable.  The only code that
     * should attempt to understand the structure of this information is
     * the addSerializedData method of this class.
     * @return                     The serialized representation of the
     *                             dataset.
     */
    public String serialize() {
        StringBuilder out = new StringBuilder();
        serializeSubtree(map, out);
        return out.toString();
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
     * Generate a Javascript description of the database contents, in the form
     * of a Javascript Object literal enclosed in braces.
     * @param out                  The Javascript is appended to this
     *                             StringBuilder.
     */
    public void toJavascript(Appendable out) {
        try {
            javascriptForSubtree(map, out);
        }
        catch (IOException e) {
            throw new IOError(e.getMessage());
        }
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
     * This method is used by both lookup and lookupPath to process values
     * that match the key or path.  This method determines whether they match
     * the caller's desired type(s), handles the creation of new Datasets from
     * HashMaps, and collects multiple return values into an ArrayList, if
     * desired.
     * @param value                Value from a HashMap that has the right
     *                             name.  Can be either a String, HashMap,
     *                             or ArrayedList.
     * @param wanted               The kinds of objects that are of interest.
     * @param quantity             How many objects are of interest.
     * @param out                  If {@code quantity} is {@code ALL} and
     *                             this argument is non-null, then matching
     *                             results are appended here; otherwise a
     *                             new ArrayList is created to hold matching
     *                             results.
     * @return                     If {@code value} doesn't match
     *                             {@code wanted} then null is returned.
     *                             Otherwise, if {@code quantity} is
     *                             {@code ALL} than a single String or
     *                             Dataset is returned.  Otherwise an
     *                             ArrayList is returned with all of the
     *                             matching values.
     */
    protected Object collectResults(Object value, DesiredType wanted,
            Quantity quantity, ArrayList<Object> out) {
        if (value instanceof String) {
            if (wanted == DesiredType.DATASET) {
                return null;
            }
            if (quantity == Quantity.FIRST_ONLY) {
                return value;
            }
            if (out == null) {
                out = new ArrayList<Object>(5);
            }
            out.add(value);
            return out;
        }

        // The value is either a HashMap or an ArrayList.
        if (wanted == DesiredType.STRING) {
            return null;
        }
        if (quantity == Quantity.FIRST_ONLY) {
            if (value instanceof ArrayList) {
                value = ((ArrayList) value).get(0);
            }
            return new Dataset((HashMap) value, fileName);
        }
        if (out == null) {
            out = new ArrayList<Object>(5);
        }
        if (value instanceof ArrayList) {
            for (Object element : (ArrayList) value) {
                out.add(new Dataset((HashMap) element, fileName));
            }
        } else {
            out.add(new Dataset((HashMap) value, fileName));
        }
        return out;
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
     * Extract a run-length encoded string from the input, and return it
     * @param source               Source string.  The characters starting
     *                             at {@code start} must consist of a
     *                             decimal integer followed by a "."
     *                             followed by the string, whose length is
     *                             given by the integer.
     * @param start                Index within {@code source} of the first
     *                             character in the run-length encoded
     *                             string.
     * @param end                  Used to return the index in {@code source}
     *                             of the first character after the string.
     * @return                     The extracted string.
     *
     */
    protected static String getEncodedString(CharSequence source, int start,
            IntBox end) {
        // Convert everything up to the next "." into a decimal integer.
        int i = start;
        int length = 0;
        int sourceLength = source.length();
        while (true) {
            if (i >= sourceLength) {
                throw new SyntaxError(null,
                    "missing \".\" in serialized dataset");
            }
            char c = source.charAt(i);
            if (c == '.') {
                i++;
                break;
            }
            length = length*10 + (c - '0');
            i++;
        }

        // Use the length to extract the actual string value.
        if (length < 0) {
            throw new SyntaxError(null,
                    "serialized dataset has improper length \"" +
                    source.subSequence(start, i-1) + "\"");
        }
        if ((i + length) > sourceLength) {
            throw new SyntaxError(null,
                    "unexpected end of serialized dataset");
        }
        end.value = i + length;
        return source.subSequence(i, i + length).toString();
    }

    /**
     * This recursive method does all the real work for the
     * {@code toJavascript} method, generating a Javascript description
     * of the contents of a dataset.
     * @param map                  HashMap that holds the dataset subtree
     *                             to be written.
     * @param out                  The Javascript for the dataset gets
     *                             appended here in the form of an Object
     *                             literal (enclosed in braces).
     */
    @SuppressWarnings("unchecked")
    protected void javascriptForSubtree(HashMap map, Appendable out)
            throws IOException {
        out.append('{');
        String prefix = "";
        for (Object nameObject: map.keySet()) {
            String name = (String) nameObject;
            out.append(prefix);
            out.append(name);
            out.append(": ");
            Object value = map.get(name);
            if (value instanceof HashMap) {
                javascriptForSubtree((HashMap) value, out);
            } else if (value instanceof ArrayList) {
                out.append('[');
                ArrayList<HashMap> list = (ArrayList <HashMap>) value;
                String listPrefix = "";
                for (int i = 0; i < list.size(); i++) {
                    out.append(listPrefix);
                    javascriptForSubtree(list.get(i), out);
                    listPrefix = ", ";
                }
                out.append(']');
            } else {
                out.append('"');
                Html.escapeStringChars(value.toString(), out);
                out.append('"');
            }
            prefix = ", ";
        }
        out.append('}');
    }

    /**
     * Given a path, find the hash table that contains the element named in
     * the path (if there is one) and return it along with the final
     * name in the past.  This method is used internally by several other
     * methods, such as createPath and deletePath.
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
    protected ParentInfo lookupParent(String path, boolean create) {
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
     * This recursive method does all of the work of the {@code lookupPath}
     * method.  See the documentation for {@code lookupPath} for info
     * on the results produced.  This method is called recursively for
     * each element in {@code path}.
     * @param path                 Dot-separated collection of element names,
     *                             indicating the desired values.
     * @param start                The caller has already processed the
     *                             portion of {@code path} up to this
     *                             index.  The next element name starts at
     *                             this index.
     * @param dataset              Nested dataset in which to start searching:
     *                             the element in {@code path} starting at
     *                             index {@code start} will be looked up in
     *                             this dataset.
     * @param wanted               The kind of values that are desired.
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @param results              If {@code quantity} is {@code ALL} and
     *                             this argument is non-null then the
     *                             matching values are appended to this
     *                             rather than creating a new ArrayList,
     *                             and the return value will be
     *                             {@code results}.
     * @return                     The return value is null if no matching
     *                             values are found.  Otherwise, if
     *                             {@code quantity} is {@code FIRST_ONLY} then
     *                             the return value is a String or Dataset.
     *                             Otherwise the return value is an ArrayList,
     *                             each of whose members is a String or Dataset.
     */
    @SuppressWarnings("unchecked")
    protected Object lookupPathHelper(String path, int start, HashMap dataset,
            DesiredType wanted, Quantity quantity, ArrayList<Object> results) {
        int length = path.length();
        int dot = path.indexOf('.', start);
        if (dot == -1) {
            dot = length;
        }
        String key = path.substring(start, dot);
        Object nextObject = dataset.get(key);
        if (nextObject == null) {
            return null;
        }
        if (dot >= length) {
            // We've reached the end of the path; add the value(s) to
            // the result (if they match {@code wanted}) and return.
            return collectResults(nextObject, wanted, quantity, results);
        }

        // If we get here it means there are more path elements to look up.
        // Make a recursive call for each nested dataset in the current value.
        dot++;
        if (nextObject instanceof HashMap) {
            return lookupPathHelper(path, dot, (HashMap) nextObject, wanted,
                    quantity, results);
        } else if (nextObject instanceof ArrayList) {
            Object returnValue = null;
            ArrayList list = (ArrayList) nextObject;
            for (int i = 0, end = list.size(); i < end; i++) {
                Object nestedResult = lookupPathHelper(path, dot,
                        (HashMap) list.get(i), wanted, quantity, results);
                if (nestedResult != null) {
                    if (quantity == Quantity.FIRST_ONLY) {
                        // We only need one result, and we have it.  No
                        // need to search additional nested datasets.
                        return nestedResult;
                    } else {
                        // The result is an ArrayList; save it to use for
                        // future results (it's possible that the next call
                        // allocated it).
                        results = (ArrayList<Object>) nestedResult;
                        returnValue = nestedResult;
                    }
                }
            }
            return returnValue;
        } else {
            return null;
        }
    }

    /**
     * This method does most of the work of{@code serialize}.  It
     * appends the serialized representation of the (nested?) dataset
     * given by {@code map} to {@code out}.
     * @param map                  Dataset to be serialized.
     * @param out                  Where to place the serialized
     *                             representation.
     */
    @SuppressWarnings("unchecked")
    protected void serializeSubtree(HashMap map, StringBuilder out) {
        // See the serialize method for documentation on the syntax of
        // the serialized representation.
        out.append('(');
        String prefix = "";
        for (Object nameObject: map.keySet()) {
            String name = (String) nameObject;
            out.append(prefix);
            out.append(name.length());
            out.append('.');
            out.append(name);
            Object value = map.get(name);
            if (value instanceof HashMap) {
                serializeSubtree((HashMap) value, out);
            } else if (value instanceof ArrayList) {
                ArrayList<HashMap> list = (ArrayList <HashMap>) value;
                for (int i = 0; i < list.size(); i++) {
                    serializeSubtree(list.get(i), out);
                }
            } else {
                String s = value.toString();
                out.append(s.length());
                out.append('.');
                out.append(s);
            }
            prefix = "\n";
        }
        out.append(')');
    }
}
