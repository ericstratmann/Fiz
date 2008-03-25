package org.fiz;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * The Util class defines miscellaneous methods that are generally useful
 * and don't fit anywhere else.
 */

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
     * Concatenates all of the strings in an Enumeration.
     * @param values                   Strings to concatenate
     * @param separator                Use this as a separator between the
     *                                 strings.
     * @return                         Concatenation of all the strings
     *                                 in <code>values</code>, with
     *                                 <code>separator</code> between adjacent
     *                                 values.
     */
    public static String join(Enumeration values, CharSequence separator) {
        StringBuilder builder= new StringBuilder();
        CharSequence prefix = "";
        while (values.hasMoreElements()) {
            builder.append(prefix);
            builder.append(values.nextElement().toString());
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
     * Determines whether a string value consists entirely of whitespace.
     * @param s                       String to check.
     * @return                        Returns true if all of the characters
     *                                in s are whitespace characters, false
     *                                if any are not.
     */
    public static boolean isWhitespace(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether a set of characters consists entirely of whitespace.
     * @param ch                      Character array containing data to check.
     * @param start                   Index of first character to check.
     * @param length                  Total number of characters to check.
     * @return                        Returns true if all of the characters
     *                                are whitespace characters, false
     *                                if any are not.
     */
    public static boolean isWhitespace(char[] ch, int start, int length) {
        int end = start + length;
        for (int i = start; i < end; i++) {
            if (!Character.isWhitespace(ch[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is used to produce a shortened form of strings that
     * are too long, typically for use in error messages.
     * @param s                       Input string
     * @param maxChars                Maximum number of characters to return
     *                                result
     * @return                        If s has no more than maxChars characters
     *                                than the result is s.  Otherwise, the
     *                                result consists of the first
     *                                characters of s, plus " ...".
     */
    public static String excerpt (String s, int maxChars) {
        if (s.length() <= maxChars) {
            return s;
        }
        return s.substring(0, maxChars-3) + "...";
    }
    public static String excerpt (StringBuilder s, int maxChars) {
        if (s.length() <= maxChars) {
            return s.toString();
        }
        return s.substring(0, maxChars-3) + "...";
    }

    /**
     * Allocate a new character array and initialize it with the contents
     * of a string.
     * @param s                       String that determines the length and
     *                                contents of the character array.
     * @return                        Character array with contents equal
     *                                to s.
     */
    public static char[] newCharArray(String s) {
        char[] ch = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            ch[i] = s.charAt(i);
        }
        return ch;
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
     * Given an exception message of the form "path (message)", extract
     * and return just the portion in parentheses.
     * @param input                   The original exception message
     * @return                        If <code>input</code> has the form
     *                                "path (message)", just the inner
     *                                message is returned; otherwise
     *                                <code>input</code> is returned.
     */
    public static String extractInnerMessage(String input) {
        int inputLength = input.length();
        if (input.charAt(inputLength-1) != ')') {
            return input;
        }
        int firstParen = input.indexOf('(');
        if ((firstParen > 2) && (input.charAt(firstParen-1) == ' ')) {
            return input.substring(firstParen+1, inputLength-1);
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
            throw IOError.newFileInstance(fileName, e.getMessage());
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
        throw FileNotFoundError.newPathInstance(fileName, type, path);
    }
}