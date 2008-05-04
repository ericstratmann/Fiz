package org.fiz;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * The Util class defines miscellaneous methods that are generally useful
 * and don't fit anywhere else.
 */

public class Util {
    // No constructor: this class only has a static methods.
    private Util() {}

    /**
     * Find the next non-space character in a string.
     * @param s                        String to search
     * @param start                    Index of first character to check
     * @return                         Returns the index in {@code s}
     *                                 of the next non-space character at or
     *                                 after {@code start}, or the
     *                                 string length if there are no more
     *                                 non-space characters.
     */
    public static int skipSpaces(CharSequence s, int start) {
        while ((start < s.length()) && (s.charAt(start) == ' ')) {
            start++;
        }
        return start;
    }

    /**
     * Find the next non-space character in a string.
     * @param s                        String to search
     * @param start                    Index of first character to check
     * @param end                      Index of character just after last
     *                                 one to check.
     * @return                         Returns the index in {@code s}
     *                                 of the next non-space character at or
     *                                 after {@code start}, or {@code end}
     *                                 if there are no non-space characters
     *                                 before {@code end}.
     */
    public static int skipSpaces(CharSequence s, int start, int end) {
        while ((start < end) && (s.charAt(start) == ' ')) {
            start++;
        }
        return start;
    }

    /**
     * Utility method used by {@code join}: computes the total space
     * needed to join strings, in order to avoid reallocation in the
     * StringBuilder used for the result.
     * @param values                   Strings to concatenate
     * @param separator                Separator between values.
     * @return                         Total number of characters needed to
     *                                 hold all the strings in
     *                                 {@code values}, with
     *                                 {@code separator} between adjacent
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
     *                                 in {@code values}, with
     *                                 {@code separator} between adjacent
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
     *                                 in {@code values}, with
     *                                 {@code separator} between adjacent
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
     *                                 in {@code values}, with
     *                                 {@code separator} between adjacent
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
     *                                at or after {@code start} that
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
     *                                in the specified range are whitespace
     *                                characters, false if any are not.
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
     * @return                        If {@code s} has no more than
     *                                {@code maxChars} characters then the
     *                                result is {@code s}.  Otherwise, the
     *                                result consists of the first
     *                                characters of {@code s}, plus " ...",
     *                                for a total of {@code maxChars}
     *                                characters.
     */
    public static String excerpt (String s, int maxChars) {
        if (s.length() <= maxChars) {
            return s;
        }
        return s.substring(0, maxChars-3) + "...";
    }

    /**
     * This method is used to produce a shortened form of strings that
     * are too long, typically for use in error messages.
     * @param s                       Input string
     * @param maxChars                Maximum number of characters to return
     *                                result
     * @return                        If {@code s} has no more than
     *                                {@code maxChars} characters then the
     *                                result is {@code s}.  Otherwise, the
     *                                result consists of the first
     *                                characters of {@code s}, plus " ...",
     *                                for a total of {@code maxChars}
     *                                characters.
     */
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
     * This method is invoked to ensure that a blank line appears between
     * sections in a string being built up.  If {@code s} is not empty,
     * a newline is appended to it; if the previous last line did not end
     * in a newline, then an additional newline is appended.
     * @param s                    String under construction.
     */
    public static void addBlankLine(StringBuilder s) {
        int length = s.length();
        if (length == 0) {
            return;
        }
        if (s.charAt(length-1) != '\n') {
            s.append("\n\n");
        } else {
            s.append('\n');
        }
    }

    /**
     * Given a file name, this method checks to see if {@code path} ends
     * with an extension such as ".java", ".yml", etc.
     * @param path                    A file name such as {@code /a/b/foo.html};
     *                                the file need not actually exist.
     * @return                        If {@code path} ends with an
     *                                extension the return value is the index
     *                                in {@code path} of the extension's
     *                                initial ".".  Otherwise the return
     *                                value is -1.
     */
    public static int findExtension(CharSequence path) {
        // Work backwards from the end of the string to see if we find a
        // "." before a "/".

        int length = path.length();
        for (int i = length -1; i >= 0; i--) {
            char c = path.charAt(i);
            if (c == '.') {
                if (i == (length-1)) {
                    // There is a "dot", but there is nothing after it.
                    return -1;
                }
                return i;
            }
            if ((c == '/') || (c == '\\')) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Given a file name, this method checks to see if {@code path} ends
     * with an extension such as ".java", ".yml", etc.  If so the
     * extension is returned.
     * @param path                    A file withname such as {@code /a/b/foo.html};
     *                                the file need not actually exist.
     * @return                        If {@code path} ends with an
     *                                extension it is returned (including the
     *                                {@code .}).  If there is no extension
     *                                null is returned.
     */
    public static String fileExtension(CharSequence path) {
        int index = findExtension(path);
        if (index >= 0) {
            return path.subSequence(index, path.length()).toString();
        }
        return null;
    }

    /**
     * Adds an extension to a file name if it doesn't already have one.
     * @param path                 Input file name
     * @param extension            Extension to add to {@code path} if
     *                             {@code path} doesn't have an extension
     *                             of its own.  Should start with a ".".
     * @return                     If {@code path} already contains an
     *                             extension then it is returned as-is.
     *                             Otherwise, {@code extension} is
     *                             added to the end of {@code path} to
     *                             form the return value.
     */
    public static String addExtension(String path, String extension) {
        if (fileExtension(path) != null) {
            return path;
        }
        return path + extension;
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
     * This method re-creates the full URL for an incoming request,
     * including the query string.
     * @param request                 Information about the request.
     * @return                        The URL for the request, including
     *                                query string (if there was one).
     */
    public static String getUrlWithQuery(HttpServletRequest request) {
        String url = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null) {
            return url;
        }
        return url + "?" + query;
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
     * @return                        If {@code input} has the form
     *                                "path (message)", just the inner
     *                                message is returned; otherwise
     *                                {@code input} is returned.
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
     *                                better error message if the file can't
     *                                be found.  Null means the file doesn't
     *                                have a meaningful type.
     * @param path                    One or more directories in which to
     *                                search for the file.
     * @return                        Contents of the first file found.
     * @throws FileNotFoundError      None of the directories in
     *                                {@code path} contained the file.
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

    /**
     * Given the name of a class and various other information, load the
     * class and construct an instance of it.
     * @param className            Name of the desired class.  If this
     *                             name doesn't exist as a class, and it
     *                             contains no "." characters, then the
     *                             name will also be searched for in a
     *                             list of packages determined by configuration
     *                             information.
     * @param requiredType         If this is non-null, then the class must
     *                             be a subclass of this or implement this
     *                             interface.
     * @param constructorArgs      Arguments to pass to the constructor; the
     *                             class must contain a constructor compatible
     *                             with these arguments.
     * @return                     The return value is a new instance of
     *                             the class.
     */
    public static Object newInstance(String className, String requiredType,
            Object... constructorArgs) {

        // Look up the class.
        Class<?> cl = null;
        try {
            cl = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
        }
        searchPackages: if ((cl == null) && (className.indexOf('.') < 0)) {
            // Couldn't find the given name; try prepending various package
            // names provided by configuration information.
            Dataset config = Config.getDataset("main");
            String path = config.check("searchPackages");
            if (path == null) {
                break searchPackages;
            }
            for (String packageName : split(path, ',')) {
                try {
                    cl = Class.forName(packageName + "."  + className);
                    break searchPackages;
                }
                catch (ClassNotFoundException e) {
                }
            }
        }
        if (cl == null) {
            throw new ClassNotFoundError(className);
        }

        // Make sure that class has the right type.
        if (requiredType != null) {
            Class<?> desiredClass;
            try {
                desiredClass = Class.forName(requiredType);
            }
            catch (ClassNotFoundException e) {
                throw new ClassNotFoundError(requiredType);
            }
            if (!desiredClass.isAssignableFrom(cl)) {
                throw new InstantiationError(className,
                        "class isn't a subclass of " + requiredType);
            }
        }

        // Find a constructor with the right signature and create an instance.
        Constructor constructor;
        try {
            Class<?>[] argClasses = new Class<?>[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                argClasses[i] = constructorArgs [i].getClass();
            }
            constructor = cl.getConstructor(argClasses);
        }
        catch (Exception e) {
            throw new InstantiationError(className,
                    "couldn't find appropriate constructor ("
                    + e.getMessage() + ")");
        }
        try {
            return constructor.newInstance(constructorArgs);
        }
        catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new InstantiationError(className,
                    "exception in constructor: " + cause.getMessage());
        }
    }

    /**
     * Returns a template that can be expanded in the context of an
     * error response to a DataRequest to produce an HTML error message.
     * @param properties           If non-null, and if it contains an
     *                             {@code errorTemplate} value, that value
     *                             is used as the template.
     * @return                     Returns the {@code errorTemplate} value
     *                             from {@code properties}, if it exists,
     *                             otherwise returns the {@code errorTemplate}
     *                             value from the main dataset, if it exists,
     *                             otherwise returns a default value.
     */
    public static String getErrorTemplate(Dataset properties) {
        String template = null;
        if (properties != null) {
            template = properties.check("errorTemplate");
        }
        if (template == null) {
            template = Config.getDataset("main").check("errorTemplate");
        }
        if (template == null) {
            template = "Error: @message";
        }
        return template;
    }

    /**
     * Determine whether a URL is "complete" (ready to be used as-is) or
     * just consists of the Fiz-relative portion of the URL.  The URL
     * is considered to be complete if it starts with a slash, or if
     * it contains a colon before the first slash.  For example,
     * {@code /a/b/c} and {@code http://www.company.com/x/y} are both
     * complete, but {@code demo/link} is not complete.
     * @param url                  URL to check for completeness.
     * @return                     True if {@code url} is complete, false
     *                             if it isn't.
     */
    public static boolean urlComplete(CharSequence url) {
        if (url.charAt(0) == '/') {
            return true;
        }
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '/') {
                return false;
            }
            if (c == ':') {
                return true;
            }
        }
        return false;
    }
}