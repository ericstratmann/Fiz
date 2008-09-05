package org.fiz;

import java.util.*;

/**
 * The StringUtil class defines a collection of generally-useful methods
 * for manipulating strings.
 */
public final class StringUtil {
    // No constructor: this class only has a static methods.
    private StringUtil() {}

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
     * Returns a message containing all of the information available
     * for one were errors; intended for logs or for examination by
     * developers to track down problems.  Use {@code errorMessage}
     * instead of this method if you want a message suitable for presentation
     * to a user.
     * @param errors               One or more datasets, each containing
     *                             information about an error; this method
     *                             looks for standard values in the datasets
     *                             such as {@code message} and {@code code}.
     * @return                     If the request hasn't completed, or if it
     *                             completed successfully, then null is
     *                             returned.  Otherwise the return value is a
     *                             string containing all of the information
     *                             available for the error(s) that have been
     *                             recorded for the request.
     */
    public static String detailedErrorMessage(Dataset... errors) {
        StringBuilder result = new StringBuilder();
        for (Dataset error: errors) {
            // Use a newline to separate information for different errors.
            if (result.length() != 0) {
                result.append('\n');
            }

            // Generate a header line containing the message, if there is one.
            String message = error.check("message");
            if (message != null) {
                result.append(message);
            } else {
                result.append("error");
            }

            // Add all of the other values in the error dataset to the
            // message. If any of the values include newline characters,
            // add indentation to the additional lines.
            String prefix = ":\n  ";
            String prefix2 = "\n  ";
            String indent = "\n               ";
            ArrayList<String> names = new ArrayList<String>();
            names.addAll(error.keySet());
            Collections.sort(names);
            for (Object nameObject : names) {
                String name = (String) nameObject;
                if (name.equals("message")) {
                    continue;
                }
                String value = error.check(name);
                if (value == null) {
                    continue;
                }
                result.append(prefix);
                result.append(String.format("%-12s %s", (name + ":"),
                        value.replace("\n", indent)));
                prefix = prefix2;
            }
        }
        return result.toString();
    }

    /**
     * Returns a human-readable message describing one or more errors,
     * each described by a dataset.
     * @param errors               One or more datasets, each containing
     *                             information about an error; this method
     *                             looks for standard values in the datasets
     *                             such as {@code message} and {@code code}.
     * @return                     If the request hasn't completed, or if it
     *                             completed successfully, then null is
     *                             returned.  Otherwise the return value is a
     *                             description of the problem(s), intended for
     *                             presentation to users (as opposed to
     *                             system maintainers).
     */
    public static String errorMessage(Dataset... errors) {
        // Handle the simple case of a single error with a "message" value.
        if (errors.length == 1) {
            String message = errors[0].check("message");
            if (message != null) {
                return message;
            }
        }

        // Generate a message with one line for each error.
        StringBuilder message = new StringBuilder();
        for (Dataset error: errors) {
            // Use newline as a separator between messages.
            if (message.length() != 0) {
                message.append('\n');
            }

            // Ideally, there is a "message" value in the error; if so, it is
            // just what we're looking for.
            String value = error.check("message");
            if (value != null) {
                message.append(value);
            } else {
                // Next choice is a "code" value in the error; this is pretty terse,
                // but better than nothing.
                value = error.check("code");
                if (value != null) {
                    message.append("error ");
                    message.append(value);
                } else {
                    // No reasonable information is available.
                    message.append("unknown error");
                }
            }
        }
        return message.toString();
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
        StringBuilder builder = new StringBuilder(joinedLength(values,
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
     * Concatenates all of the values in an array of objects.
     * @param values                   Objects whose string values should be
     *                                 concatenated
     * @param separator                Use this as a separator between the
     *                                 values.
     * @return                         Concatenation of all the values
     *                                 in {@code values}, with
     *                                 {@code separator} between adjacent
     *                                 values.
     */
    public static String join(Object[] values, CharSequence separator) {
        StringBuilder builder= new StringBuilder();
        CharSequence prefix = "";
        for (int i = 0; i < values.length; i++) {
            builder.append(prefix);
            builder.append(values[i].toString());
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
     * If the first character in a string is uppercase, convert it to
     * lowercase.
     * @param s                    Input string
     * @return                     The return value is identical to {@code s}
     *                             except that its first character is
     *                             converted to lowercase if it was previously
     *                             uppercase.
     */
    public static String lcFirst(String s) {
        if (s.length() == 0) {
            return s;
        }
        char first = s.charAt (0);
        if (!Character.isUpperCase(first)) {
            return s;
        }
        return Character.toLowerCase(first) + s.substring(1);
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
}
