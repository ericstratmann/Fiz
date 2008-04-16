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

public class Dataset {
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
         * nested data sets, all of which will be returned.
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
     * MissingValueError is thrown when methods such as <code>get</code>
     * cannot find the requested key in the dataset.
     */
    public static class MissingValueError extends Error {
        /**
         * Construct a MissingValueError with a message describing the
         * key that was not found.
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
     * <code>get</code> has the wrong type (e.g., a string value was
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

    // The following field holds the contents of the dataset.  Keys
    // are strings, and values can have any of the following types:
    // String:                 simple value
    // HashMap:                nested dataset
    // ArrayList:              list of nested datasets; each element of
    //                         the list is a HashMap.
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
     * Private constructor, used by checkVaglue, newFileInstance, and other
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
     *                             of <code>fileName</code>
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
     *                             <code>path</code> (a different extension
     *                             may be chosen for each directory).
     * @param path                 Directories to search for the dataset.
     * @param pathHandling         If <code>name</code> exists in more than
     *                             one directory in <code>path</code>, this
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
     * Given a key, returns the value associated with that key, or null
     * if the key is not defined.  This method is identical to
     * <code>get</code> except that it does not generate an exception
     * if the key is undefined or has the wrong type.
     * @param key                  Name of the desired value
     * @return                     Value associated with <code>key</code>,
     *                             or null if <code>key</code> doesn't
     *                             exist or if it corresponds to a nested
     *                             dataset.
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
     * Given a key, returns the value associated with that key.  This is
     * a single-level lookup: the key must be defined in the top level of
     * the dataset.
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
            throw new MissingValueError(path);
        }
        return (String) child;
    }

    /**
     * This is a general-purpose method to find the value associated with a
     * given key, intended primarily for use by other methods such as
     * <code>get</code> and <code>getChildren</code>.  The value must be
     * present in the top level of the dataset (i.e., key is not a path;
     * use lookupPath if it is).  This method searches both the dataset
     * and its chain.
     * @param key                  Name of the desired value
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     If <code>key</code> exists and its value
     *                             matches <code>desiredResult</code> then
     *                             it is returned as a String, Dataset,
     *                             or Dataset[] for <code>desiredResult</code>
     *                             values of STRING, DATASET, and DATASETS,
     *                             respectively.  If <code>key</code>
     *                             doesn't exist then null is returned.
     * @throws WrongTypeError      Thrown if <code>key</code> is defined but
     *                             its value doesn't correspond to
     *                             <code>desiredResult</code>.
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
     * <code>getPath</code> and <code>getPathChildren</code>.  This method
     * searches both the dataset and its chain.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, "a.b.c" refers to a value
     *                             "c" contained in a nested dataset "b"
     *                             contained in a data set "a" contained
     *                             in the current dataset
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     If the desired value exists and matches
     *                             <code>desiredResult</code> then
     *                             it is returned as a String, Dataset,
     *                             or Dataset[] for <code>desiredResult</code>
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
                throw new WrongTypeError(wrongTypeMessage(
                        path.substring(0, dot), DesiredType.DATASET,
                        currentObject));
            }
        }

        // At this point we have found the final value.
        return checkValue(path, wanted, currentObject);
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
     * Returns the dataset's chain (the value passed to the last call to
     * setChain).
     * @return                     Next Dataset to search for values not found
     *                             in the Dataset (null if none).
     */
    public Dataset getChain() {
        return chain;
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
     * Removes an entry (or nested dataset) from the top level of a dataset.
     * If there is no value with the given <code> key</code> then the method
     * does nothing.
     * @param key                  Name of a value in the top level of the
     *                             dataset (not a path).
     */
    public void remove(String key) {
        map.remove(key);
    }

    /**
     * Creates an entry in the top level of a dataset that consists of a
     * nested dataset.  If the key already exists in the dataset then its
     * previous contents are replaced (regardless of whether they
     * represent a string value or nested dataset).
     * @param key                  Name of a value in the top level of the
     *                             dataset (not a path).
     */
    @SuppressWarnings("unchecked")
    public Dataset setChild(String key) {
        HashMap childMap = new HashMap();
        map.put(key, childMap);
        return new Dataset(childMap, null);
    }

    /**
     * Makes one dataset a child of another (from now on, elements in the
     * child dataset can be referenced either using the child or using the
     * parent).
     * @param key                  Name of a value in the top level of the
     *                             dataset (not a path).  If this key is
     *                             already defined, the existing value will
     *                             be discarded
     * @param child                Existing dataset, which will become a
     *                             child of <code>this</code>.
     */
    @SuppressWarnings("unchecked")
    public void setChild(String key, Dataset child) {
        map.put(key, child.map);
    }

    /**
     * Given an existing dataset, adds it to <code>this</code> as a top-level
     * child.  However, if there already exist one or more top-level children
     * by the same name then the new child is added to them to form a list,
     * with the new child at the end of the list.
     * @param key                  Name of a value in the top level of the
     *                             dataset (not a path).  If this key is
     *                             already defined as a string value, the
     *                             existing value will be discarded.
     * @param child                Existing dataset, which will become a
     *                             child of <code>this</code>.
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
     * of the dataset (intended primarily for testing).
     * @return                     Pretty-printed string.
     */
    public String toString() {
        StringBuilder out = new StringBuilder();
        prettyPrint(map, out, "");
        return out.toString();
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
     * This recursive method does all of the real work for toString().
     * @param dataset              Nested dataset to pretty-print.
     * @param out                  Pretty-printed output gets appended here.
     * @param indent               String to prefix to each line of
     *                             output; provides appropriate indentation
     *                             for the nesting level of this dataset.
     */
    @SuppressWarnings("unchecked")
    protected static void prettyPrint(HashMap dataset, StringBuilder out,
            String indent) {
        ArrayList names = new ArrayList();
        names.addAll(dataset.keySet());
        Collections.sort(names);
        for (Object nameObject : names) {
            String name = (String) nameObject;
            Object value = dataset.get(name);
            if (value instanceof HashMap) {
                out.append(String.format("%s%s:\n", indent, name));
                prettyPrint((HashMap) value, out, indent + "  ");
            } else if (value instanceof ArrayList) {
                ArrayList<HashMap> list = (ArrayList <HashMap>) value;
                for (int i = 0; i < list.size(); i++) {
                    out.append(String.format("%s%s[%d]:\n", indent, name, i));
                    prettyPrint(list.get(i), out, indent + "  ");
                }
            } else {
                out.append(String.format("%s%s: %s\n", indent, name,
                        value.toString()));
            }
        }
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
     *                             <code>name</code>.
     * @return                     If <code>value</code>  matches
     *                             <code>wanted</code> then it is returned
     *                             as a String, Dataset, or Dataset[]
     *                             for <code>wanted</code> values of STRING,
     *                             DATASET, and DATASETS, espectively.
     * @throws WrongTypeError      Thrown if <code>value</code> doesn't
     *                             correspond to  <code>wanted</code>.
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
     * Invoked when other methods encounter values that have the wrong
     * type (e.g., expected a string value but found a nested dataset);
     * generates an appropriate message to use in a WrongTypeError exception.
     * @param name                 Name for the entity that had the wrong
     *                             type (a top-level key or a hierarchical
     *                             path)
     * @param wanted               The type of entity that was desired
     * @param got                  The object that was encountered, which
     *                             didn't match <code> wanted</code>
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
