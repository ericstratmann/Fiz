/*
 * The Template class provides facilities that substitute values from a
 * Dataset into a template string to produce a new string through a process
 * called "expansion".  Each character from the template is copied to the
 * output string except for the following patterns, which cause substitutions:
 * @name                    Copy the contents of the dataset element named
 *                          <code>name</code> to the output.  <code>name</code>
 *                          consists of all the standard Unicode identifier
 *                          characters following the "@".  It is an error
 *                          if the name doesn't exist in the dataset.
 * @(name)                  Copy the contents of the data value named
 *                          <code>name</code> to the output.  <code>name</code>
 *                          consists of all the  characters following the "("
 *                          up to the next ")".  "@" can be used between the
 *                          parentheses to perform substitutions on the name;
 *                          for example, "@(@foo)" finds dataset element "foo",
 *                          uses its value as the name of another dataset
 *                          element, and copies the value of that element
 *                          to the output.  It is an error if the name
 *                          doesn't exist in the dataset.
 * @@                       Append "@" to the output.
 * @{                       Append "{" to the output.
 * @}                       Append "}" to the output.
 * @*                       Any other occurrence of "@" besides those
 *                          described above is illegal and results in an error.
 * {...}                    Conditional substitution: normally the information
 *                          between the braces is processed just like the rest
 *                          of the template (except that the braces are not
 *                          copied to the output).  However, if there is a
 *                          data reference for which the name doesn't exist
 *                          then the information between the braces skipped:
 *                          nothing is copied to the output.  Furthermore, if
 *                          there are space characters next to either of the
 *                          curly braces then one of the spaces may be
 *                          removed to avoid reduncant spaces (enclose the
 *                          space in {} to keep this from happening.
 * Encodings: when a dataset element is substituted into a template,
 * special characters in the value will be escaped according to an Encoding
 * enum supplied during expansion.  For example, if HTML encoding has been
 * specified, "<" characters will be translated to the entity reference
 * "&lt;".  Translation occurs only for values coming from the dataset, not
 * for characters in the template itself;  you should ensure that template
 * characters already obey the output encoding rules.  Translation can be
 * disabled for dataset values by specifying NONE as the encoding.
 */

package org.fiz;

public class Template {
    /**
     * SyntaxError is thrown when there is an incorrect construct in a
     * template, such as an "@" followed by unrecognized character.
     */
    public static class SyntaxError extends Error {
        /**
         * @param message          Detailed information about the problem
         */
        public SyntaxError(String message) {
            super(message);
        }
    }

    /**
     * Instances of this enum indicate how to escape special characters
     * in data values incorporated into template output:
     * <p>
     * HTML: the output will be used in HTML, so replace special HTML
     * characters such as "<" with HTML entities such as "&lt;".
     * <p>
     * URL: the output will be used as part of a URL, so use %xx encoding
     * for all of the characters that aren't are permitted in URLs.
     * <p>
     * NONE: don't perform any transformations on the data values.
     */
    public enum Encoding {HTML, URL, NONE}

    // The following class is used internally to pass information between
    // methods.  Among other things, it provides a vehicle for returning
    // additional results from a method.
    protected static class ParseInfo {
        CharSequence template;     // The template being processed.
        Dataset data;              // Source of data for expansions.
        StringBuilder out;         // Output information is appended here.
        Encoding encoding;         // How to transform special characters.
        boolean conditional;       // True means we are parsing information
                                   // between curly braces, so don't get upset
                                   // if data values can't be found.
        boolean missingData;       // True means that a data value requested
                                   //  between curly braces couldn't be found.
        int end;                   // Modified by methods such as
                                   // expandName to hold the index of the
                                   // character just after the last one
                                   // processed by the method.  For example,
                                   // if the method processed "@(name)"
                                   // then this value will give the index
                                   // of the next character after the ")".
        int lastDeletedSpace;      // Used in collapsing space characters
                                   // around braces to handle cumulative
                                   // situations like "<{@a} {@b} {@c}>":
                                   // if all three sets embraces drop out,
                                   // we want to eliminate exactly 2 spaces.
                                   // This field holds the index into the
                                   // template of the last space character
                                   // we collapsed out, or -1 if none.
    }

    /**
     * Substitute data into a template string, appending the result to a
     * StringBuilder.
     * @param template             Contains text to be copied to
     *                             <code>out</code> plus substitution
     *                             specifiers, as described above.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param out                  The expanded template is appended here.
     * @param encoding             Determines whether (and how) special
     *                             characters in data values are quoted.
     * @throws Dataset.MissingValueError
     *                             A data value requested outside {}
     *                             couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as "@+".
     */
    public static void expand(CharSequence template, Dataset data, StringBuilder out,
            Encoding encoding) throws Dataset.MissingValueError, SyntaxError{
        ParseInfo info = new ParseInfo();
        info.template = template;
        info.data = data;
        info.out = out;
        info.encoding = encoding;
        info.conditional = false;
        info.missingData = false;
        info.lastDeletedSpace = -1;

        for (int i = 0; i < template.length(); ) {
            char c = template.charAt(i);
            if (c == '@') {
                expandAtSign(info, i+1);
                i = info.end;
            } else if (c == '{') {
                expandBraces(info, i+1);
                i = info.end;
            } else {
                out.append(c);
                i++;
            }
        }
    }

    /**
     * Substitute data into a template string, appending the result to a
     * StringBuilder.  In this method the encoding defaults to HTML.
     * @param template             Contains text to be copied to
     *                             <code>out</code> plus substitution
     *                             specifiers, as described above.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param out                  The expanded template is appended here.
     * @throws Dataset.MissingValueError
     *                             A data value requested outside {}
     *                             couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as "@+".
     */
    public static void expand(CharSequence template, Dataset data,
            StringBuilder out) throws Dataset.MissingValueError, SyntaxError {
        expand(template, data, out, Encoding.HTML);
    }

    /**
     * This method is invoked to process the part of a template immediately
     * following an "@".
     * @param info                 Contains information about the template
     *                             being expanded.  This method modifies
     *                             info.out, info.missingInfo, and info.end.
     *                             Info.end is set to the index of the first
     *                             character following the @-specifier (e.g.
     *                             for "@foo+bar" info.end will refer to the
     *                             "+" and for "@{abc" info.end will refer
     *                             to the "a".
     * @param start                Index of the character immediately after
     *                             the "@".
     * @throws Dataset.MissingValueError
     *                             Thrown if a data value couldn't be found
     *                             and info.conditional is false.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as "@+".
     */
    protected static void expandAtSign(ParseInfo info, int start)
            throws Dataset.MissingValueError {
        char c = info.template.charAt(start);
        if (Character.isUnicodeIdentifierStart(c)) {
            info.end = Util.identifierEnd(info.template, start);
            appendValue(info,
                    info.template.subSequence(start, info.end).toString());
        } else if (c == '(') {
            expandParenName(info, start+1);
        } else if ((c == '@') || (c == '{') || (c == '}')) {
            info.out.append(c);
            info.end = start+1;
        } else {
            throw new SyntaxError("invalid sequence \"@" + c
                    + "\" in template \"" + info.template + "\"");
        }
    }

    /**
     * This method is invoked to expand parenthesized names, such as
     * @(@first+@second).
     * @param info                 Contains information about the template
     *                             being expanded.  This method modifies
     *                             info.out, info.missingInfo, and info.end.
     *                             Info.end will be set to the index of
     *                             the character just after the closing
     *                             parenthesis.
     * @param start                Index of the character immediately after
     *                             the "@(".
     * @throws Dataset.MissingValueError
     *                             Thrown if a data value couldn't be found
     *                             and info.conditional is false.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as "@+".
     */
    protected static void expandParenName(ParseInfo info, int start)
            throws Dataset.MissingValueError, SyntaxError {
        // Note: we use info.out to collect the name of the dataset value,
        // then erase this from info.out before appending the value.
        int oldEnd = info.out.length();

        // While we are collecting the name, we don't want to do any
        // special-character handling.  To accomplish this, temporarily
        // change the encoding (then change it back before processing the
        // final data value).
        Encoding oldEncoding = info.encoding;
        info.encoding = Encoding.NONE;

        CharSequence template = info.template;
        for (int i = start; i < template.length(); ) {
            char c = template.charAt(i);
            if (c == '@') {
                expandAtSign(info, i+1);
                i = info.end;
            } else if (c == ')') {
                info.end = i+1;
                String name = info.out.substring(oldEnd);
                info.out.setLength(oldEnd);
                info.encoding = oldEncoding;
                appendValue(info, name);
                return;
            } else {
                info.out.append(c);
                i++;
            }
        }
        throw new SyntaxError("missing \")\" for \"@(\" in template \""
                + info.template + "\"");
    }

    /**
     * Given the name of a dataset element, this method appends the value
     * of that element to the output string.
     * @param info                 Contains information about the template
     *                             being expanded.  This method modifies
     *                             info.out and info.missingInfo.
     * @param name                 Name of the desired dataset element.
     * @throws Dataset.MissingValueError
     *                             Thrown if the data value couldn't be found
     *                             and info.conditional is false.
     */
    protected static void appendValue(ParseInfo info, String name)
            throws Dataset.MissingValueError {
        String value;
        if (info.conditional) {
            value = info.data.check(name);
            if (value == null) {
                info.missingData = true;
                return;
            }
        } else {
            value = info.data.get(name);
        }
        if (info.encoding == Encoding.HTML) {
            Html.escapeHtmlChars(value, info.out);
        } else if (info.encoding == Encoding.URL) {
            Html.escapeUrlChars(value, info.out);
        } else {
            info.out.append(value);
        }
    }

    /**
     * This method is invoked to expand a portion of a template that
     * lies between curly braces.
     * @param info                 Contains information about the template
     *                             being expanded.  This method modifies
     *                             info.out, info.missingInfo, and info.end.
     *                             Info.end will be set to the index of
     *                             the character just after the closing
     *                             brace.
     * @param start                Index of the character immediately after
     *                             the "{".
     *                             and info.conditional is false.
     * @throws SyntaxError         The template is typical, e.g. it doesn't
     *                             contain a close-brace.
     */
    protected static void expandBraces(ParseInfo info, int start)
            throws SyntaxError {
        info.conditional = true;
        info.missingData = false;
        int oldEnd = info.out.length();
        CharSequence template = info.template;

        for (int i = start; i < template.length(); ) {
            char c = template.charAt(i);
            if (c == '@') {
                expandAtSign(info, i+1);
                i = info.end;
            } else if (c == '}') {
                info.end = i+1;
                if (info.missingData) {
                    // Some of the required data was not available, so
                    // undo all of the output we have generated.  Furthermore,
                    // if omitting. the material embraces results in redundant
                    // spaces, then eliminate the space character on one side
                    // of the braces.

                    char next;
                    if (info.end < template.length()) {
                        next = template.charAt(info.end);
                    } else {
                        // The "}" is at the end of the template, so pretend
                        // the next character is a close-delimiter.
                        next = '>';
                    }
                    if ((info.lastDeletedSpace < (start-2))
                            && (template.charAt(start-2) == ' ')
                            && ((next == ' ') || (next == ']')
                            || (next == '>') || (next == '}')
                            || (next == ')') || (next == '\"')
                            || (next == '\''))) {
                        // There is a space before the "{" that hasn't already
                        // been deleted, and the character after the "}"
                        // is either a space, a close-delimiter such as ">",
                        // or the end of the template.  Erase the space before
                        // the "{".
                        info.lastDeletedSpace = start-2;
                        oldEnd--;
                    } else {
                        char prev;
                        if (start >= 2) {
                            prev = template.charAt(start-2);
                        } else {
                            // The "{" is at the beginning of the template,
                            // so pretend the previous character is an
                            // open-delimiter.
                            prev = '<';
                        }
                        if ((next == ' ') && ((prev == '[')
                                || (prev == '<') || (prev == '{')
                                || (prev == '(') || (prev == '\"')
                                || (prev == '\''))) {
                            // There is a space after the "}" and the
                            // character before the "{" is and open-delimiter
                            // such as "<" or the beginning of the string.
                            // Skip over the trailing space.
                            info.lastDeletedSpace = info.end;
                            info.end++;
                        }
                    }
                    info.out.setLength(oldEnd);
                }
                info.conditional = false;
                return;
            } else {
                info.out.append(c);
                i++;
            }
        }
        throw new SyntaxError("missing \"}\" in template \""
                + info.template + "\"");
    }
}
