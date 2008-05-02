package org.fiz;
import java.io.*;
import java.util.*;

import org.ho.yaml.Yaml;
import org.ho.yaml.exception.YamlException;

/**
 * YamlDataset allows YAML documents to be accessed using the standard
 * Dataset mechanisms.  A slightly restricted subset of YAML is supported
 * here: documents must not contain lists of values like the following:
 * <pre>
 * foo:
 *   - first
 *   - second
 *   - third
 * </pre>
 * List of hashes are OK: these translate into nested datasets.
 */

public class  YamlDataset extends Dataset {
    /**
     * UnnamedValueError is thrown if YamlDataset encounters unnamed values
     * in the YAML, such as a list of values with no names.
     */
    public static class UnnamedValueError extends Error {
        /**
         * Construct an UnnamedValueError within message including the
         * name of the YAML file where the problem occurred.
         * @param fileName         If the dataset input came from a file,
         *                         this gives the file name; null means
         *                         the input didn't come from a file.
         */
        public UnnamedValueError(String fileName) {
            super("YAML dataset contains sequence(s) with unnamed values"
                    + ((fileName != null)
                    ? (" (file \"" + fileName + "\")")
                    : ""));
        }
    }

    /**
     * This method is only for the use of the static constructors
     * below.
     * @param contents             Main HashMap for the dataset,
     *                             created by the YAML parser.
     * @param fileName             Name of the file from which this dataset
     *                             was read, or null if none.
     */
    private YamlDataset(HashMap contents, String fileName) {
        super(contents, fileName);
    }

    /**
     * Create a dataset from a YAML input string.
     * @param s                    String in YAML format
     * @throws SyntaxError         <code> s</code> does not contain
     *                             well-formed YAML
     * @throws UnnamedValueError   The YAML contains a list of string values
     */
    public static YamlDataset newStringInstance(String s)
            throws SyntaxError, UnnamedValueError {
        try {
            Object yamlInfo = Yaml.load(s);
            checkAndConvert(yamlInfo, null);
            return new YamlDataset((HashMap) yamlInfo, null);
        }
        catch (YamlException e) {
            throw new SyntaxError(null, e.getMessage());
        }
    }

    /**
     * Create a dataset from information contained in a YAML file.
     * @param fileName             Name of a file in YAML format
     * @return                     New YamlDataset object containing contents
     *                             of <code>fileName</code>
     * @throws FileNotFoundError   The file doesn't exist or can't be read
     * @throws SyntaxError         The file does not contain well-formed YAML
     * @throws UnnamedValueError   The YAML contains a list of string values
     */
    public static YamlDataset newFileInstance(String fileName)
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
        return new YamlDataset((HashMap) yamlInfo, fileName);
    }

    /**
     * Convert a dataset back to YAML format.  This method can be used
     * on any dataset, even those that didn't originally come from YAML.
     * @param d                    Dataset to convert.
     * @return                     A string describing the contents of the
     *                             dataset using YAML syntax.
     */
    public static String writeString(Dataset d) {
        try {
            StringWriter out = new StringWriter();
            writeSubtree(d.map, out, "", "");
            if (d.generateIoException) {
                // We get here only during testing.
                throw new IOException("error simulated");
            }
            return out.toString();
        }
        catch (IOException e) {
            throw new IOError(e.getMessage());
        }
    }

    /**
     * Convert the dataset back to YAML format.
     * @return                     A string describing the contents of the
     *                             dataset using YAML syntax.
     */
    public String toString() {
        return YamlDataset.writeString(this);
    }

    /**
     * Create a file in YAML format describing the contents of a dataset.
     * This method can be used on any dataset, even one that didn't
     * originally come from YAML.
     * @param d                    Dataset to write to the file.
     * @param name                 Name of file; this file will be overwritten
     *                             if it already exists.  If the name doesn't
     *                             end with an extension, a ".yml" extension
     *                             is added.
     * @param comment              Optional text describing the meaning of
     *                             the file for humans who might stumble
     *                             across it.  If non-null, this is turned
     *                             into a YAML comment by adding appropriate
     *                             comment characters and then output at the
     *                             beginning of the file, before the generated
     *                             YAML.  May contain embedded newlines,
     *                             which will result in a multi-line comment.
     * @throws FileNotFoundError   The file couldn't be opened.
     * @throws IOError             An error occurred while writing or
     *                             closing the file.
     */
    public static void writeFile(Dataset d, String name, String comment) {
        FileWriter out;
        try {
            name = Util.addExtension(name, ".yml");
            out = new FileWriter(name);
        }
        catch  (IOException e) {
            throw new FileNotFoundError(name, "YAML dataset", e.getMessage());
        }
        try {
            if (comment != null) {
                out.append("# ");
                out.append(comment.replace("\n", "\n# "));
                out.append ("\n");
            }
            writeSubtree(d.map, out, "", "");
            out.close();
            if (d.generateIoException) {
                // We get here only during testing.
                throw new IOException("error simulated");
            }
        }
        catch (IOException e) {
            throw IOError.newFileInstance(name, e.getMessage());
        }
    }

    /**
     * Create a file in YAML format describing the contents of the dataset.
     * @param name                 Name of file; this file will be overwritten
     *                             if it already exists.  If the name doesn't
     *                             end with an extension, a ".yml" extension
     *                             is added.
     * @param comment              Optional text describing the meaning of
     *                             the file for humans who might stumble
     *                             across it.  If non-null, this is turned
     *                             into a YAML comment by adding appropriate
     *                             comment characters and then output at the
     *                             beginning of the file, before the generated
     *                             YAML.  May contain embedded newlines,
     *                             which will result in a multi-line comment.
     * @throws FileNotFoundError   The file couldn't be opened.
     * @throws IOError             An error occurred while writing or
     *                             closing the file.
     */
    public void writeFile(String name, String comment) {
        YamlDataset.writeFile(this, name, comment);
    }

    /**
     * This recursive method provides most of the functionality for writing
     * datasets in YAML format.  Each invocation is responsible for writing
     * a subtree of the full dataset.
     * @param dataset              HashMap that holds the dataset subtree
     *                             to be written.
     * @param writer               Append YAML information here.
     * @param firstPrefix          Output this string before the name of
     *                             the first child of dataset; contains
     *                             spaces for indentation and also a "-" if
     *                             this dataset is part of a list in the
     *                             parent.
     * @param otherPrefix          Output this string before the name of
     *                             children after the first; contains
     *                             spaces for indentation.
     * @throws IOException         Thrown by writer if it encounters a
     *                             problem.
     */
    @SuppressWarnings("unchecked")
    protected static void writeSubtree(HashMap dataset, Writer writer,
            String firstPrefix, String otherPrefix) throws IOException {
        ArrayList names = new ArrayList();
        names.addAll(dataset.keySet());
        Collections.sort(names);
        String prefix = firstPrefix;

        // Find the length of the longest name, so that we can format
        // the output in two columns.
        int maxLength = 0;
        for (Object nameObject: names) {
            String name = (String) nameObject;
            if ((name.length() > maxLength)
                    && (dataset.get(name) instanceof String)) {
                maxLength = name.length();
            }
        }

        // Make a second pass through all of the entries to print them.
        for (Object nameObject: names) {
            String name = (String) nameObject;
            Object value = dataset.get(name);
            writer.append(prefix);
            writer.append(name);
            if (value instanceof HashMap) {
                writer.append(":\n");
                String childIndent = otherPrefix + "    ";
                writeSubtree((HashMap) value, writer, childIndent,
                        childIndent);
            } else if (value instanceof ArrayList) {
                ArrayList<HashMap> list = (ArrayList <HashMap>) value;
                writer.append(":\n");
                for (int i = 0; i < list.size(); i++) {
                    writeSubtree(list.get(i), writer, otherPrefix + "  - ",
                            otherPrefix + "    ");
                }
            } else {
                writer.append(": ");
                int padding = maxLength - name.length();
                if (padding > 0) {
                    writer.append(String.format("%" +  padding + "s", ""));
                }
                writer.append(escapeYamlChars(value.toString()));
                writer.append("\n");
            }
            prefix = otherPrefix;
        }
    }

    /**
     * This method is invoked when writing YAML to handle values that
     * may include special characters that require quoting.
     * @param value                Raw value of a YAML element.
     * @return                     In most cases, this is the same as
     *                             {@code value}.  However, if {@code value}
     *                             would cause confusion to the YAML
     *                             parser in its current form (e.g., it
     *                             starts with a space or includes a newline)
     *                             the return value is a quoted form of
     *                             {@code value} that will be parsed by
     *                             YAML to return the original value.
     */
    protected static String escapeYamlChars(String value) {
        checkForIssues: {
            if (value.length() == 0) {
                break checkForIssues;
            }

            // Certain punctuation characters have special meaning to YAML
            // if they appear as the first character of a value.
            char c = value.charAt(0);
            if ((c == '[') || (c == '{') || (c == '*')
                    || (c == '|') ||(c == '>') || (c == '\'')
                    || (c == '\"') || (c == '%') || (c == ' ')) {
                break checkForIssues;
            }

            // Certain characters have special meaning to YAML if they appear
            // anywhere within a value.
            for (int i = 0; i < value.length(); i++) {
                c = value.charAt(i);
                if ((c == '&') || (c == '!') || (c == '@') || (c == ',')
                        || (c == '`') || (c == '#') || (c <= 0x1f)) {
                    break checkForIssues;
                }
            }

            // The value must not end with a space character.
            if (value.charAt(value.length()-1) == ' ') {
                break checkForIssues;
            }

            // The string is just fine as-is.
            return value;
        }

        // If we get here it means that there was something special
        // about the value, so the value must be quoted.  Enclose the value
        // in quotes and use backslashes for any special characters.
        StringBuilder out = new StringBuilder(value.length() + 5);
        out.append("\"");
        Html.escapeStringChars(value, out);
        out.append("\"");
        return out.toString();
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
     * @param fileName             Name of the file from which the dataset
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
            } else if (value == null) {
                // This seems to happen if a hash is specified with
                // no members (e.g., " child:" on one line, and no
                // following lines containing members).  Create an empty
                // HashMap for this.
                pair.setValue (new HashMap());
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
