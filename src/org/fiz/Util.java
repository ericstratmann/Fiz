/**
 * The Util class defines miscellaneous methods that are generally useful
 * and don't fit anywhere else.
 */

package org.fiz;
import javax.servlet.http.HttpServletRequest;

public class Util {

    // The following array is used by urlEncoding to map from character
    // values 0-255 to the corresponding URL-encoded values.
    final static String[] urlCodes = {
    "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
    "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
    "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
    "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
      "+", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
    "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
    "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
    "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
    "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
    "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
    "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
    "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
    "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
    "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
    "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
    "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
    "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
    "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
    "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
    "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
    "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
    "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
    "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
    "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
    "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
    "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
    "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
    "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
    "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
    "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
    "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
    "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
  };

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
     * This method is invoked to replace characters that are special in
     * HTML (<, >, &, ") with HTML entity references (&lt;, &gt;, &amp;,
     * and &quot;, respectively); this allows arbitrary data to be
     * included in HTML without accidentally invoking special HTML
     * behavior.
     * @param s                       Input string; may contain arbitrary
     *                                characters
     * @param out                     The contents of <code>s</code> are
     *                                copied here, replacing special characters
     *                                with entity references
     */
    public static void escapeHtmlChars(String s, StringBuilder out) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"') {
                out.append("&quot;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '<') {
                out.append("&lt;");
            } else {
                out.append(c);
            }
        }
    }

    /**
     * This method transforms a string into a form that may be used in
     * URLs (such as for query values).  It does this by replacing
     * unusual characters with %xx-sequences as defined by RFC1738.  It
     * also converts non-ASCII characters to UTF-8 before encoding, as
     * recommended in  http://www.w3.org/International/O-URL-code.html.
     * @param s                       Input string; may contain arbitrary
     *                                characters
     * @param out                     The contents of <code>s</code> are
     *                                copied here after converting to UTF-8
     *                                and converting nonalphanumeric
     *                                characters to %xx sequences.
     */
    public static void escapeUrlChars(String s, StringBuilder out) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'a') && (c <= 'z') || (c >= 'A') && (c <= 'Z')
                    || (c >= '0') && (c <= '9') || (c == '.') || (c == '-')) {
                out.append(c);
            } else if (c <= 0x7f) {
                out.append(urlCodes[c]);
            } else if (c <= 0x7ff) {
                out.append(urlCodes[0xc0 | (c >> 6)]);
                out.append(urlCodes[0x80 | (c & 0x3f)]);
            } else {
                out.append(urlCodes[0xe0 | (c >> 12)]);
                out.append(urlCodes[0x80 | ((c >> 6) & 0x3f)]);
                out.append(urlCodes[0x80 | (c & 0x3f)]);
            }
      }
    }

    /**
     * This method transforms a string into a form that may be used safely
     * in a string literal for Javascript and many other languages.  For
     * example, if <code>s</code> is a\x"z then it gets escaped to
     * a\\x\"z so that the original value will be regenerated when
     * Javascript evaluates the string literal.
     * @param s                       Value that is to be encoded in a string
     *                                literal.
     * @param out                     A converted form of <code>s</code>
     *                                is copied here; if this information
     *                                is used in a Javascript string, it
     *                                will evaluate to exactly the characters
     *                                in  <code>s</code>.
     */
    public static void escapeStringChars(String s, StringBuilder out) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c <= '\037') {
                if (c == '\n') {
                    out.append("\\n");
                } else if (c == '\t') {
                    out.append("\\t");
                } else {
                    out.append(String.format("\\x%02x", (int) c));
                }
            } else if (c == '\\') {
                out.append("\\\\");
            } else if (c == '\"') {
                out.append("\\\"");
            } else if (c == '\177') {
                out.append("\\x3f");
            } else {
                out.append(c);
            }
        }
    }

    /**
     * This method re-creates the full URI for incoming request,
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