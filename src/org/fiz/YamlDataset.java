/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

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
     * This method is only for the use of the static constructors
     * below.
     * @param contents             Main HashMap for the dataset,
     *                             created by the YAML parser.
     * @param fileName             Name of the file from which this dataset
     *                             was read, or null if none.
     */
    private YamlDataset(HashMap<String,Object> contents, String fileName) {
        super(contents, fileName);
    }

    /**
     * Create a dataset from a YAML input string.
     * @param s                    String in YAML format
     * @throws SyntaxError         <code> s</code> does not contain
     *                             well-formed YAML
     */
    public static YamlDataset newStringInstance(String s)
            throws SyntaxError {
        try {
            Object yamlInfo = Yaml.load(s);
            return checkAndConvert(yamlInfo, null);
        }
        catch (YamlException e) {
            throw new SyntaxError(null, e.getMessage());
        }
    }

    /**
     * Create a dataset from information contained in a YAML file.
     * @param fileName             Name of a file in YAML format
     * @return                     New YamlDataset object containing contents
     *                             of {@code fileName}
     * @throws FileNotFoundError   The file doesn't exist or can't be read
     * @throws SyntaxError         The file does not contain well-formed YAML
     */
    public static YamlDataset newFileInstance(String fileName)
            throws FileNotFoundError, SyntaxError {
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

        return checkAndConvert(yamlInfo, fileName);
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
            name = StringUtil.addExtension(name, ".yml");
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
                && (dataset.get(name) instanceof Dataset == false)
                && (dataset.get(name) instanceof DSArrayList == false)) {
                maxLength = name.length();
            }
        }

        // Make a second pass through all of the entries to print them.
        for (Object nameObject: names) {
            String name = (String) nameObject;
            Object value = dataset.get(name);
            writer.append(prefix);
            writer.append(escapeYamlChars(name));
            if (value instanceof Dataset) {
                writer.append(":\n");
                String childIndent = otherPrefix + "    ";
                writeSubtree(((Dataset) value).map, writer, childIndent,
                        childIndent);
            } else if (value instanceof DSArrayList) {
                ArrayList<Object> list = (DSArrayList <Object>) value;
                writer.append(":\n");
                for (int i = 0; i < list.size(); i++) {
                    Object o = list.get(i);
                    if (o instanceof Dataset) {
                        writeSubtree(((Dataset) o).map, writer, otherPrefix + "  - ",
                                     otherPrefix + "    ");
                    } else {
                        writer.append(otherPrefix + "  - ");
                        writer.append(o.toString());
                        writer.append("\n");
                    }
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
     * This method is invoked when writing YAML to handle names and values that
     * may include special characters that require quoting.
     * @param value                Raw name or value for a YAML element.
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
            // anywhere within a name or value.
            for (int i = 0; i < value.length(); i++) {
                c = value.charAt(i);
                if ((c == '&') || (c == '!') || (c == '@') || (c == ',')
                        || (c == '`') || (c == '#') || (c == ':')
                        || (c <= 0x1f)) {
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
     * it is parsed.  The method handles 2 situations: HashMaps are turned
     * into Datasets and ArrayLists into DSArrayLists
     * @param yamlInfo             YAML object to check and convert.  This
     *                             is supposed to be a HashMap; if it isn't,
     *                             it's because the YAML source contained
     *                             list values, which are illegal for us.
     * @param fileName             Name of the file from which the dataset
     *                             was read, for null if none.  Used for error
     *                             messages.
     */

    @SuppressWarnings("unchecked")
    protected static YamlDataset checkAndConvert(Object yamlInfo, String fileName) {
        HashMap<String,Object> hash;
        if (yamlInfo instanceof HashMap == false) {
            return null;
        } else {
            hash = (HashMap) yamlInfo;
        }
        YamlDataset dest = new YamlDataset(new HashMap<String,Object>(), fileName);
        for (Map.Entry<String,Object> pair : hash.entrySet()) {
            String key = pair.getKey();
            Object value = pair.getValue();
            if (value instanceof HashMap) {
                Dataset child = checkAndConvert(value, fileName);
                dest.set(key, child);
            } else if (value instanceof ArrayList) {
                ArrayList<Object> list = (ArrayList<Object>) value;
                for (Object value2 : list) {
                    if (value2 instanceof HashMap) {
                        Dataset child = checkAndConvert(value2, fileName);
                        list.set(list.indexOf(value2), child);
                    } else {
                        checkAndConvert(value2, fileName);
                    }
                }
                dest.set(key, new DSArrayList<Object>((ArrayList<Object>) value));
            } else if (value == null) {
                // This seems to happen if a hash is specified with
                // no members (e.g., " child:" on one line, and no
                // following lines containing members).  Create an empty
                // Dataset for this.
                dest.set(key, new Dataset());
            } else {
                dest.set(key, value);
            }
        }
        return dest;
    }
}
