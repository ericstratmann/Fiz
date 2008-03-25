package org.fiz;
import java.io.*;
import java.util.*;

import org.ho.yaml.Yaml;
import org.ho.yaml.exception.YamlException;

/**
 * YamlDataset allows YAML documents to be accessed using the standard
 * Dataset mechanisms.
 */

public class  YamlDataset extends Dataset {
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

    // Noone should ever try to actually create a YamlDataset; just
    // invoke the static methods.
    private YamlDataset() {
    }

    /**
     * Creates a dataset from a YAML input string.
     * @param s                    String in YAML format
     * @throws SyntaxError         <code> s</code> does not contain
     *                             well-formed YAML
     * @throws UnnamedValueError   The YAML contains a list of string values
     */
    public static Dataset newStringInstance(String s)
            throws SyntaxError, UnnamedValueError {
        try {
            Object yamlInfo = Yaml.load(s);
            checkAndConvert(yamlInfo, null);
            return new Dataset((HashMap) yamlInfo, null);
        }
        catch (YamlException e) {
            throw new SyntaxError(null, e.getMessage());
        }
    }

    /**
     * Creates a dataset from information contained in a YAML file.
     * @param fileName             Name of a file in YAML format
     * @return                     New YamlDataset object containing contents
     *                             of <code>fileName</code>
     * @throws FileNotFoundError   The file doesn't exist or can't be read
     * @throws SyntaxError         The file does not contain well-formed YAML
     * @throws UnnamedValueError   The YAML contains a list of string values
     */
    public static Dataset newFileInstance(String fileName)
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
        checkAndConvert(yamlInfo, fileName);
        return new Dataset((HashMap) yamlInfo, fileName);
    }

    /**
     * This method is called to scan a YAML dataset immediately after
     * it is parsed.  The method handles 2 situations:
     * 1. We don't currently support list values (lists of sub-datasets
     *    are OK, just not list values).  If any list values are found,
     *    generate a syntax error.
     * 2. JYaml converts string values to Integer or Double types whenever
     *    they have the appropriate syntax.  We need to return all values
     *    as strings, so this method converts all of the non-string values
     *    back to strings.
     * @param yamlInfo             YAML object to check and convert.  This
     *                             is supposed to be a HashMap; if it isn't,
     *                             it's because the YAML source contained
     *                             list values, which are illegal for us.
     * @param fileName             Name of the file from which the data set
     *                             was read, for null if none.  Used for error
     *                             messages.
     */

    @SuppressWarnings("unchecked")
    protected static void checkAndConvert(Object yamlInfo, String fileName)
            throws SyntaxError {
        if (!(yamlInfo instanceof HashMap)) {
            throw new UnnamedValueError(fileName);
        }
        HashMap dataset = (HashMap) yamlInfo;
        for (Map.Entry<String,Object> pair :
                ((HashMap<String,Object>) dataset).entrySet()) {
            Object value = pair.getValue();
            if (value instanceof HashMap) {
                checkAndConvert(value, fileName);
            } else if (value instanceof ArrayList) {
                ArrayList list = (ArrayList) value;
                for (Object value2 : list) {
                   checkAndConvert(value2, fileName);
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
}
