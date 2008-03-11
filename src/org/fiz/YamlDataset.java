
/**
 * YamlDataset allows YAML documents to be accessed using the standard
 * Dataset mechanisms.
 */

package org.fiz;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ho.yaml.Yaml;
import org.ho.yaml.exception.YamlException;

public class YamlDataset extends Dataset {
    // The following field holds the key-value mappings for the Dataset.
    // It is created by JYaml; keys are strings, and values can have
    // type String for simple values, HashMap for nested Datasets,
    // and ArrayList for nested lists.
    protected HashMap yamlMap;

    // If this YAML originated in a file the following variable contains the
    // name of the file; otherwise it is null.

    protected String fileName;

    /**
     * UnnamedValueError is thrown if we encounter unnamed values in the
     * YAML, such as a list of values with no names.
     */
    public static class UnnamedValueError extends Error {
        /**
         * Constructor for UnnamedValueError.
         * @param fileName         If the dataset input came from a file,
         *                         this gives the file name; null means
         *                         the input didn't come from a file
         */
        public UnnamedValueError(String fileName) {
            super("YAML dataset contains sequence(s) with unnamed values"
                    + ((fileName != null)
                    ? (" (file \"" + fileName + "\")")
                    : ""));
        }
    }

    /**
     * Creates a dataset from a YAML input string.
     * @param s                    String in YAML format
     * @throws SyntaxError         <code> s</code> does not contain
     *                             well-formed YAML
     */
    public YamlDataset(String s) throws SyntaxError, UnnamedValueError {
        try {
            Object yamlInfo = Yaml.load(s);
            if (!(yamlInfo instanceof HashMap)) {
                throw new UnnamedValueError(null);
            }
            yamlMap = (HashMap) yamlInfo;
            checkAndConvert(yamlMap);
        }
        catch (YamlException e) {
            throw new SyntaxError(null, e.getMessage());
        }
    }

    /**
     * Private constructor, used by getFileInstance and other methods.
     * @param yamlInfo             The internal representation of the YAML
     *                             file as returned by a JYaml "load" method
     * @param fileName             If the YAML was read from a file this
     *                             gives the file name (if known); otherwise
     *                             this is null
     * @throws UnnamedValueError   The top level of the YAML consists of a
     *                             sequence with unnamed values
     */
    private YamlDataset(Object yamlInfo, String fileName)
            throws UnnamedValueError {
        if (!(yamlInfo instanceof HashMap)) {
            throw new UnnamedValueError(fileName);
        }
        yamlMap = (HashMap) yamlInfo;
        checkAndConvert(yamlMap);
        this.fileName = fileName;
    }

    /**
     * Creates a YamlDataset from information contained in a file.
     * @param fileName             Name of a file in YAML format
     * @return                     New YamlDataset object containing contents
     *                             of <code>fileName</code>
     * @throws FileNotFoundError   The file doesn't exist or can't be read
     * @throws SyntaxError         The file does not contain well-formed YAML
     * @throws UnnamedValueError   The top level of the YAML consists of a
     *                             sequence with unnamed values
     */
    public static YamlDataset getFileInstance(String fileName)
            throws FileNotFoundError, SyntaxError, UnnamedValueError {
        Object yamlInfo;

        // Note: if we pass the file name to JYaml and let it open the
        // file, JYaml appears to leave the file open (as of 1/2008).
        // To avoid this problem, pass JYaml an open file, which we
        // can then close.
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
            yamlInfo = Yaml.load(in);
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundError(fileName, "dataset",
                    e.getMessage());
        }
        catch (YamlException e) {
            throw new SyntaxError(fileName, e.getMessage());
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (Exception e) { /* Ignore errors during the close */ }
        }
        return new YamlDataset(yamlInfo, fileName);
    }

    public boolean containsKey(String key) {
        return (yamlMap.get(key) != null);
    }

    public Object lookup(String key, DesiredType wanted)
            throws WrongTypeError {
        Object child = yamlMap.get(key);
        if (child == null) {
            return null;
        }
        return checkValue(key, wanted, child);
    }

    public Object lookupPath(String path, DesiredType wanted)
            throws WrongTypeError {
        int startIndex = 0;
        int length = path.length();
        Object currentObject = yamlMap;
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
                return null;
            }
            startIndex = dot+1;
            if (startIndex >= length) {
                break;
            }

            // This is not the last key; make sure that the current object
            // is a nested dataset.
            if (currentObject instanceof ArrayList) {
                // The child consists of a list of values; take the first one
                // (if it is a HashMap)
                Object listElement = ((ArrayList) currentObject).get(0);
                if (listElement instanceof HashMap) {
                    currentObject = listElement;
                }
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
     * Returns the number of values stored in this Dataset.
     * @return                     Number of values in this Dataset; if
     *                             the dataset is hierarchical, only
     *                             top-level values are counted
     */
    public int size() {
        return yamlMap.size();
    }

    /**
     * This method is needed because JYaml converts string values to
     * Integer or Double types whenever they have the appropriate syntax.
     * We need to return all values as strings, so this method is
     * invoked immediately after parsing YAML; it scans the dataset's
     * hierarchy and converts all of the non-string values back to strings.
     * @param dataset              Dataset to check and convert
     */

    @SuppressWarnings("unchecked")
    protected static void checkAndConvert(HashMap dataset) {
        for (Map.Entry<String,Object> pair :
                ((HashMap<String,Object>) dataset).entrySet()) {
            Object value = pair.getValue();
            if (value instanceof HashMap) {
                checkAndConvert((HashMap) value);
            } else if (value instanceof ArrayList) {
                ArrayList list = (ArrayList) value;
                for (Object value2 : list) {
                    if (value2 instanceof HashMap) {
                        checkAndConvert((HashMap) value2);
                    }
                }
            } else {
                // String value: see if it needs to be converted to a string.
                Class valueClass = value.getClass();
                if ((valueClass == Integer.class)
                        || (valueClass == Double.class)
                        || (valueClass == Float.class)
                        || (valueClass == Boolean.class)) {
                    pair.setValue(value.toString());
                }
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
     *                             <code>path</code>.
     * @return                     If <code>value</code>  matches
     *                             <code>wanted</code> then it is returned
     *                             as a String, YamlDataset, or YamlDataset[]
     *                             for <code>desiredResult</code> values of
     *                             STRING, DATASET, and DATASETS, espectively.
     * @throws WrongTypeError      Thrown if <code>value</code> doesn't
     *                             correspond to  <code>wanted</code>.
     */

    protected final Object checkValue(String name, DesiredType wanted,
            Object value) throws WrongTypeError {
        if (wanted == DesiredType.STRING) {
            if (value instanceof String) {
                return value;
            }
            throw new WrongTypeError(wrongTypeMessage(name, wanted, value));
        } else {
            if (value instanceof HashMap) {
                if (wanted == DesiredType.DATASET) {
                    return new YamlDataset(value, fileName);
                }
                YamlDataset[] result = new YamlDataset[1];
                result[0] = new YamlDataset(value, fileName);
                return result;
            } else if (value instanceof ArrayList) {
                if (wanted == DesiredType.DATASET) {
                    // The value consists of a list of values; take the first
                    // one (only if it is a HashMap)

                    Object child2 = ((ArrayList) value).get(0);
                    if (child2 instanceof HashMap) {
                        return new YamlDataset(child2, fileName);
                    }
                } else {
                    ArrayList a = (ArrayList) value;
                    YamlDataset[] result = new YamlDataset[a.size()];
                    for (int i = 0; i < result.length; i++) {
                        Object listElement = a.get(i);
                        if (!(listElement instanceof HashMap)) {
                            throw new WrongTypeError(wrongTypeMessage(name,
                                    wanted, listElement));
                        }
                        result[i] = new YamlDataset(a.get(i), fileName);
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
            String value = got.toString();
            if (value.length() < 20) {
                gotType = "string value \"" + value + "\"";
            } else {
                gotType = "string value \"" + value.substring(0, 15)
                        + " ...\"";
            }
        }
        return "wrong type for dataset element \"" + name + "\": expected "
                + ((wanted == DesiredType.STRING) ? "string value"
                : "nested dataset") + " but found " + gotType;
    }
}
