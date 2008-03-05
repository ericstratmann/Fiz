/**
 * The Util class defines miscellaneous methods that are generally useful
 * and don't fit anywhere else.
 */

package org.fiz;
import javax.servlet.http.HttpServletRequest;

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
    public static int skipSpaces(String s, int index) {
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
    public static int joinedLength(String[] values, String separator) {
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
    public static String join(Iterable values, String separator) {
        StringBuilder builder= new StringBuilder();
        String prefix = "";
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
    public static String join(String[] values, String separator) {
        StringBuilder builder= new StringBuilder(joinedLength(values,
                separator));
        String prefix = "";
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
    public static int identifierEnd(String s, int start) {
        int i;
        for (i = start; i < s.length(); i++) {
            if (!Character.isUnicodeIdentifierPart(s.charAt(i))) {
                break;
            }
        }
        return i;
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
}