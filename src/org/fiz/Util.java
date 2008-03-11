/**
 * The Util class defines miscellaneous methods that are generally useful
 * and don't fit anywhere else.
 */

package org.fiz;
import java.io.*;
import javax.servlet.http.*;

public class Util {
    /**
     * Find the next non-space character in a string.
     * @param s                        String to search
     * @param index                    First character to check
     * @return                         Returns the index in <code>s</code>
     *                                 of the next non-space character at or
     *                                 after <code>index</code>, or the
     *                                 string length if there are no more
     *                                 non-space characters.
     */
    public static int skipSpaces(CharSequence s, int index) {
        while (index < s.length() && (s.charAt(index) == ' ')) {
            index++;
        }
        return index;
    }

    /**
     * Utility method used by <code>join</code>: computes the total space
     * needed to join strings, in order to avoid reallocation in the
     * StringBuilder used for the result.
     * @param values                   Strings to concatenate
     * @param separator                Separator between values.
     * @return                         Total number of characters needed to
     *                                 hold all the strings in
     *                                 <code>values</code>, with
     *                                 <code>separator</code> between adjacent
     *                                 values.
     */
    public static int joinedLength(CharSequence[] values,
            CharSequence separator) {
        int length = 0, separatorLength = 0;
        for (int i = 0; i < values.length; i++) {
            length += values[i].length() + separatorLength;
            separatorLength = separator.length();
        }
        return length;
    }

    /**
     * Concatenates all of the strings in an iterable.
     * @param values                   Strings to concatenate
     * @param separator                Use this as a separator between the
     *                                 strings.
     * @return                         Concatenation of all the strings
     *                                 in <code>values</code>, with
     *                                 <code>separator</code> between adjacent
     *                                 values.
     */
    public static String join(Iterable values, CharSequence separator) {
        StringBuilder builder= new StringBuilder();
        CharSequence prefix = "";
        for (Object o: values) {
            builder.append(prefix);
            builder.append(o.toString());
            prefix = separator;
        }
        return builder.toString();
    }

    /**
     * Concatenates all of the strings in an array.
     * @param values                   Strings to concatenate
     * @param separator                Use this as a separator between the
     *                                 strings.
     * @return                         Concatenation of all the strings
     *                                 in <code>values</code>, with
     *                                 <code>separator</code> between adjacent
     *                                 values.
     */
    public static String join(CharSequence[] values, CharSequence separator) {
        StringBuilder builder= new StringBuilder(joinedLength(values,
                separator));
        CharSequence prefix = "";
        for (int i = 0; i < values.length; i++) {
            builder.append(prefix);
            builder.append(values[i]);
            prefix = separator;
        }
        return builder.toString();
    }

    /**
     * Given a string and a separator character, break the string up
     * into the substrings that fall between separators.  Leading spaces
     * are removed from each substring.
     * @param s                       Input string
     * @param separator               Substrings are terminated by this
     *                                character
     * @return                        An array containing the substrings
     */
    public static String[] split(String s, char separator) {
        int current, length;
        char c;

        // Count the substrings.
        for (current = 0, length = 0; ; length++) {
            int sep = s.indexOf(separator, current);
            if (sep < 0) {
                break;
            }
            current = sep+1;
        }

        // The following code checks for a non-empty substring after the
        // last separator; this allows us to return an empty array for a
        // string that is empty or consists entirely of spaces.
        current = skipSpaces(s, current);
        if (current < s.length()) {
            length++;
        }

        // Each iteration through the following loop extracts one substring.
        String[] result = new String[length];
        current = 0;
        for (int i = 0; i < length; i++) {
            current = skipSpaces(s, current);
            int next = s.indexOf(separator, current);
            String spec;
            if (next >= 0) {
                result[i] = s.substring(current, next);
                current = next+1;
            } else {
                result[i] = s.substring(current);
            }
        }
        return result;
    }

    /**
     * Searches a string starting at a given location to find the first
     * character that is not a valid identifier character.
     * @param s                       String to search
     * @param start                   Index of first character to consider
     * @return                        Returns the index of the first character
     *                                at or after <code>start</code> that
     *                                cannot be part of an identifier or is
     *                                past the end of the string.
     */
    public static int identifierEnd(CharSequence s, int start) {
        int i;
        for (i = start; i < s.length(); i++) {
            if (!Character.isUnicodeIdentifierPart(s.charAt(i))) {
                break;
            }
        }
        return i;
    }

    /**
     * Given a file name, this method checks to see if the last element
     * in <code>path</code> contains an extension (i.e., it ends with
     * a construct such as ".java", ".jx", etc.).  If so the extension
     * is returned.
     * @param path                    A filename such as "/a/b/foo.html";
     *                                the file need not actually exist.
     * @return                        If <code>path</code> contains an
     *                                extension it is returned (including the
     *                                ".").  If there is no extension null is
     *                                returned.
     */
    public static String fileExtension(CharSequence path) {
        // Work backwards from the end of the string to see if we find a
        // "." before a "/".

        int length = path.length();
        for (int i = length -1; i >= 0; i--) {
            char c = path.charAt(i);
            if (c == '.') {
                if (i == (length-1)) {
                    // There is a "dot", but there is nothing after it.
                    return null;
                }
                return path.subSequence(i, length).toString();
            }
            if ((c == '/') || (c == '\\')) {
                return null;
            }
        }
        return null;
    }

    /**
     * Given a file name without an extension, this checks to see if there
     * exists a file with this base name and one of several possible
     * extensions.
     * @param base                    The base file name, which should not
     *                                already have an extension.
     * @param extensions              Extensions to try (must contain ".")
     *                                in order.
     * @return                        The name (base plus extension) of the
     *                                first file that exists.  If none of
     *                                extensions exist null is returned.
     */
    public static String findFileWithExtension(String base,
            String... extensions) {
        int baseLength = base.length();
        StringBuilder fullName = new StringBuilder(baseLength + 6);
        fullName.append(base);
        for (String extension : extensions) {
            fullName.setLength(baseLength);
            fullName.append(extension);
            if ((new File(fullName.toString())).exists()) {
                return fullName.toString();
            }
        }
        return null;
    }

    /**
     * This method re-creates the full URI for an incoming request,
     * including the query string.
     * @param request                 Information about the request.
     * @return                        The URI for the request, including
     *                                query string (if there was one).
     */
    public static String getUriAndQuery(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null) {
            return uri;
        }
        return uri + "?" + query;
    }

    /**
     * Copy all of the data from one stream to another, stopping when the
     * end of the input stream is reached.
     * @param in                      Read from this stream.
     * @param out                     Write to this stream
     */
    public static void copyStream(Reader in, Writer out) throws IOException {
        while (true) {
            int next = in.read();
            if (next == -1) {
                break;
            }
            out.write(next);
        }
    }

    /**
     * Deletes a given file/directory and, in the case of a directory,
     * all of its descendents.
     * @param name                    Name of the file or directory to delete.
     * @return                        Returns true if the deletion was
     *                                completed successfully, false otherwise.
     */
    public static boolean deleteTree(String name) {
        File file = new File(name);
        if (file.isDirectory()) {
            for (String child : file.list()) {
                deleteTree(name + "/" + child);
            }
        }
        return file.delete();
    }

    /**
     * Return the contents of a file as the response to an HTTP request.
     * @param input                   Identifies the file to return.
     * @param response                The file is delivered via this object.
     */
    public static void respondWithFile(File input,
            HttpServletResponse response) throws IOException {
        response.setContentLength((int) input.length());
        FileReader in = new FileReader(input);
        copyStream(in, response.getWriter());
        in.close();
    }

    /**
     * Given an exception message of the form "fileName (message)", extract
     * and return just the portion in parentheses.
     * @param input                   The original exception message
     * @param fileName                Name of a file, which may appear
     *                                at the start of the message.
     * @return                        If <code>input</code> has the form
     *                                "fileName (message)", just the inner
     *                                message is returned; otherwise
     *                                <code>input</code> is returned.
     */
    public static String extractInnerMessage(String input, String fileName) {
        int inputLength = input.length();
        int nameLength = fileName.length();
        if (input.startsWith(fileName) && (input.charAt(nameLength) == ' ')
                && (input.charAt(nameLength+1) == '(')
                && (input.charAt(inputLength-1) == ')')) {
            return input.substring(nameLength+2, inputLength-1);
        }
        return input;
    }

    /**
     * Read a file and return its contents in a StringBuilder object.
     * @param fileName                Name of the file to read.
     * @return                        Contents of the file.
     * @throws FileNotFoundException  The file could not be opened.
     * @throws IOError                An error happened while reading the
     *                                file.
     */
    public static StringBuilder readFile(String fileName)
            throws FileNotFoundException {
        try {
            FileReader reader = new FileReader(fileName);
            StringBuilder result = new StringBuilder(
                    (int) (new File(fileName)).length());
            char[] buffer = new char[1000];
            while (true) {
                int length = reader.read(buffer, 0, buffer.length);
                if (length < 0) {
                    break;
                }
                result.append(buffer, 0, length);
            }
            reader.close();
            return result;
        }
        catch (FileNotFoundException e) {
            throw e;
        }
        catch (IOException e) {
            throw IOError.getFileInstance(fileName, e.getMessage());
        }
    }

    /**
     * Searches a collection of directories for a file and reads in the first
     * file found.
     * @param fileName                Name of the file to read.
     * @param type                    Type of the file, such as "dataset"
     *                                or "template"; used only to generate a
     *                                better error message  if the file can't
     *                                be found.  Null needs the file doesn't
     *                                have a meaningful type.
     * @param path                    One or more directories in which to
     *                                search for the file.
     * @return                        Contents of the first file found.
     * @throws FileNotFoundError      None of the directories in
     *                                <code>path</code> contained the file.
     */
    public static StringBuilder readFileFromPath(String fileName, String type,
            String... path) throws FileNotFoundError {
        for (int i = 0; i < path.length; i++) {
            try {
                return readFile(path[i] + "/" + fileName);
            }
            catch (FileNotFoundException e) {
                // No template in this directory; go on to the next.
                continue;
            }
        }
        throw FileNotFoundError.getPathInstance(fileName, type, path);
    }
}