package org.fiz;
import java.util.*;

/**
 * DocTranslator objects are used by the FizDoc doclet to convert
 * Fiz comments from a form that looks good in code to a form that
 * looks good in Javadoc.  Here are examples of what you can do:
 *  - You can use asterisks or hyphens to generate bulleted lists,
 *    like this:
 *    <pre>
 *      - First point goes here.
 *        More text for the first point.
 *      - Second point, just one line.
 *      * Asterisks and hyphens are interchangeable.
 *    </pre>
 *  - You can generate definition lists, like this:
 *    <pre>
 *      term1:     definition for term1, which
 *                 can have multiple lines
 *      term2 -    hyphens work just as well as colons
 *      term3      or you can omit them;  the key is that
 *                 there must be at least two consecutive
 *                 spaces between the term and its
 *                 definition.
 *    </pre>
 * - If you have a {@code <pre>} block, where the {@code <pre>} and
 *   {@code </pre>} are on lines by themselves, then the lines in between,
 *   as well as the {@code <pre>} and {@code </pre>} lines, are passed
 *   along verbatim without modification by this doclet, except that HTML
 *   special characters within the {@code <pre>} are automatically quoted.
 *   This means you can include HTML in the block and have it appear verbatim
 *   in the output.
 * - Blank lines get turned into paragraph breaks with {@code <p>}.
 */

public class DocTranslator {
    // The following enum is used to classify comment lines.
    protected enum LineType {
        /**
         * The line consists of a bullet ("*" or "-") followed by
         * whitespace followed by text.
         */
        BULLET,

        /**
         * The line consists of a term being defined, optionally
         * followed by ":" or " -", followed by more than one space
         * character, followed by text.
         */
        DEFINITION,

        /**
         * The line consists of only {@literal <pre>}.
         */
        PRE,

        /**
         * The line consists only of {@literal </pre>}.
         */
        END_PRE,

        /**
         * The line is blank.
         */
        BLANK,

        /**
         * None of the above (just text).
         */
        NORMAL,

        /**
         * There is no line: we have reached the end of the input.  In
         * this case bulletIndent will be less than zero also.
         */
        EOF}

    // The raw comment block that we are processing:
    String input;

    // Translated info gets appended here:
    StringBuilder output;

    // The following variables are set by the classify method to
    // describe the current line:
    int lineStart;                 // Index within input of the first
                                   // character of the current line.
    LineType type;                 // Overall structure of the line.
    String term = "";              // If type is DEFINITION, holds the
                                   // text of the term being defined.
    int bulletIndent;              // For types BULLET and DEFINITION,
                                   // gives the index within the current
                                   // line of the first character of the
                                   // bullet or term.  Otherwise it is the
                                   // same as textIndent.  -1 means we have
                                   // reached the end of the input.
    int textIndent;                // Index within the current line of the
                                   // first character of text (ignoring
                                   // bullet or definition term)
    int lineEnd;                   // Index within input of the character
                                   // that ends this line (carriage return,
                                   // newline, or end of input).
    int nextLine;                  // Index within input of the first
                                   // character of the next line.

    // List of all the standard Javadoc tags: if the term for a definition
    // starts with one of the strings, we don't treat it as a definition
    // after all, since that would defeat the normal Javadoc tag mechanism.
    ArrayList<String> standardTags = null;

    /**
     * Constructor for DocTranslators.
     * @param input                String to be translated.  This is the
     *                             "raw" comment text from the code, which
     *                             means that Javadoc has already removed
     *                             the leading " *" from each line.
     */
    public DocTranslator(String input) {
        this.input = input;
        nextLine = 0;
        output = new StringBuilder();
        if (standardTags == null) {
            standardTags = new ArrayList<String>();
            standardTags.add("@author ");
            standardTags.add("@deprecated ");
            standardTags.add("@exception ");
            standardTags.add("@param ");
            standardTags.add("@return ");
            standardTags.add("@see ");
            standardTags.add("@serial ");
            standardTags.add("@serialField ");
            standardTags.add("@serialData ");
            standardTags.add("@throws ");
            standardTags.add("@version ");
        }
    }

    /**
     * Given a raw comment block from Java code, translate various
     * code-friendly constructs to Javadoc-friendly forms.
     * @return                     Reformatted comment block suitable for
     *                             further processing by Javadoc.
     */
     public String translate() {
        translateLines(0);
        return output.toString();
    }

    /**
     * This method provides the main recursive driver for translating
     * comments.  It advances to the next line, and continues processing
     * lines until it encounters one whose basic indentation is less than
     * <code>indent</code>.  When it returns, the current line has not
     * yet been processed.
     * @param indent               Only process lines that are indented at
     *                             least this much.
     */
    protected void translateLines(int indent) {
        classify(nextLine);
        while (true) {
            if (bulletIndent < indent) {
                return;
            }
            if (type == LineType.PRE) {
                handlePre();
            } else if (type == LineType.BULLET) {
                translateBullets();
            } else if (type == LineType.DEFINITION) {
                translateDefinitions();
            } else if (type == LineType.BLANK) {
                output.append("<p>\n");
                classify(nextLine);
            } else {
                output.append(input, lineStart, nextLine);
                classify(nextLine);
            }
        }
    }

    /**
     * Scans a line of input and sets a collection of fields in the object
     * to describe the line.  For example, this method figures out if the
     * line looks like a bullet "- foo" and, if so, it figures out the
     * indentation of the bullet and the text that follows.
     * @param start                Index within the input string of the line's
     *                             first character.
     */
    protected void classify(int start) {
        char c;
        int i, left;
        lineStart = start;
        term = "";

        // See if we have reached the end of the input string.
        if (start == input.length()) {
            nextLine = lineEnd = start;
            type = LineType.EOF;
            bulletIndent = -1;
            return;
        }

        // Find the end of the line.
        for (i = start; ; i++) {
            if (i >= input.length()) {
                // No line termination for this line; it ends at the end
                // of the input.
                lineEnd = nextLine = i;
                break;
            }
            c = input.charAt(i);
            if ((c == '\r') || (c == '\n')) {
                lineEnd = i;
                nextLine = i+1;
                if ((c == '\r') && (nextLine < input.length())
                        && (input.charAt(nextLine) == '\n')) {
                    nextLine++;
                }
                break;
            }
        }

        // Skip over leading spaces to find the left edge of the line's
        // real content, then check for a blank line.
        left = StringUtil.skipSpaces(input, start, lineEnd);
        textIndent = left - start;
        if (left >= lineEnd) {
            type = LineType.BLANK;
            return;
        }

        // Check for <pre> and </pre>.
        bulletIndent = left - start;
        if (input.startsWith("<pre>", left)) {
            type = LineType.PRE;
            return;
        }
        if (input.startsWith("</pre>", left)) {
            type = LineType.END_PRE;
            return;
        }

        // See if there is a bullet character.
        c = input.charAt(left);
        if (((c == '-') || (c == '*')) && ((left+1) < lineEnd)
                && (input.charAt(left+1) == ' ')) {
            textIndent = StringUtil.skipSpaces(input, left+2, lineEnd) - start;
            type = LineType.BULLET;
            return;
        }

        // See if this line matches a definition.  This is the case if
        // there's a run of at least two space characters (not preceded
        // by a "." or ";") later in the line.  Also, don't treat this as a
        // definition if the term has the form "@foo": these are Javadoc
        // tags and will be handled elsewhere.
        defCheck: for (i = left+1; (i+1) < lineEnd; i++) {
            if ((input.charAt(i) == ' ') && (input.charAt(i+1) == ' ')
                    && (input.charAt(i-1) != '.')
                    && (input.charAt(i-1) != ';')) {
                // Make sure that what we are seeing isn't a Javadoc tag.
                if (input.charAt(left) == '@') {
                    for (String tag : standardTags) {
                        if (input.startsWith(tag, left)) {
                            // This is a Javadoc tag; don't treat the line
                            // as a definition.
                            break defCheck;
                        }
                    }
                }

                // We have a definition here.  Extract the definition term
                // (and remove a trailing ":" or " -" if it exists).
                c = input.charAt(i-1);
                if (c == ':') {
                    term = input.substring(left, i-1);
                } else if ((c == '-') && (input.charAt(i-2) == ' ')) {
                    term = input.substring(left, i-2);
                } else {
                    term = input.substring(left, i);
                }
                textIndent = StringUtil.skipSpaces(input, i+2, lineEnd) - start;
                type = LineType.DEFINITION;
                return;
            }
        }

        // No definition; this must be a normal text line.
        type = LineType.NORMAL;
    }

    /**
     * This method is invoked when we encounter a line with a bullet
     * and a level of indentation greater than previous lines (i.e. the start
     * of a new bulleted list).  This method will process all of the lines
     * in the bulleted list at the current indentation level, making
     * recursive calls to handle more deeply nested constructs within the
     * list.  Translated information gets appended to "output".  When this
     * method returns the current line corresponds to the first line after
     * the bulleted list, which has not yet been processed.
     */
    protected void translateBullets() {
        // Insert HTML for the list header and the beginning of the first
        // element.
        int indent = bulletIndent;
        output.append(input, lineStart, lineStart + bulletIndent);
        output.append("<ul><li>");
        output.append(input, lineStart + textIndent, nextLine);

        // Each iteration through the following loop handles the remainder
        // of one bullet and potentially starts another.
        while (true) {
            // Make a recursive call to handle any additional lines in this
            // bullet (including nested bullets).
            translateLines(indent+1);

            // Insert HTML to end the current bullet element.
            appendToLastLine("</li>");

            // Are there more bullets in the current bullet list (i.e., same
            // level of indentation)?
            if ((type != LineType.BULLET) || (bulletIndent != indent)) {
                // We're done with this bulleted list.
                appendToLastLine("</ul>");
                break;
            }

            // Start the next bullet.
            output.append(input, lineStart, lineStart + bulletIndent);
            output.append("<li>");
            output.append(input, lineStart + textIndent, nextLine);
        }
    }

    /**
     * This method is invoked when we discovered the first line in a new
     * definition list (lines such as "term:  definition").  This method
     * will process all of the lines in the definition list, including
     * recursively handling other constructs nested within the list.  It
     * appends translated info to "output".  When this method returns,
     * the current line is the first line after the end of the definition
     * list, which has not yet been translated.
     */
    protected void translateDefinitions() {
        // Use a table to get clean layout.  Insert HTML for the main
        // table structure and the beginnings of the first row (the
        // text for the definition may span multiple lines of input).
        int indent = bulletIndent;
        output.append(input, lineStart, lineStart + bulletIndent);
        output.append("<table cellspacing=\"0\"><tr><td class=\"fizterm\">");
        output.append(term);
        output.append("</td><td class=\"fizdef\">");
        output.append(input, lineStart + textIndent, nextLine);

        // Each iteration through the following loop handles the remainder
        // of one definition and potentially starts another.
        while (true) {
            // Make a recursive call to handle any additional lines in this
            // definition (including nested bullets and definitions).
            translateLines(indent+1);

            // Insert HTML to end the current bullet element.
            appendToLastLine("</td></tr>");

            // Are there more definitions in the current definitions list
            // (i.e., same level of indentation)?
            if ((type != LineType.DEFINITION) || (bulletIndent != indent)) {
                // We're done with this bulleted list.
                appendToLastLine("</table>");
                break;
            }

            // Start the next definition.
            output.append(input, lineStart, lineStart + bulletIndent);
            output.append("<tr><td class=\"fizterm\">");
            output.append(term);
            output.append("</td><td class=\"fizdef\">");
            output.append(input, lineStart + textIndent, nextLine);
        }
    }

    /**
     * This method is invoked when we encounter a line containing
     * {@literal <pre>}.  This method processes all of the lines up until
     * the next {@literal </pre>} line, passing them through to "output"
     * with no translations except converting HTML special characters to
     * entities.  When this method returns, the current line is the first
     * line after the end of the definition list, which has not yet been
     * translated.
     */
    protected void handlePre() {
        int indent = textIndent;
        output.append(input, lineStart+indent, nextLine);
        while (true) {
            classify(nextLine);
            if (type == LineType.END_PRE) {
                break;
            }
            if (type == LineType.EOF) {
                // No </pre> before the end of the comment block; this is
                // bogus, but there's not much we can do.
                return;
            }

            // In addition to quoting HTML special characters, remove
            // leading spaces up to the level of the <pre> tag.

            int thisIndent = indent;
            if (bulletIndent < thisIndent) {
                thisIndent = bulletIndent;
            }
            for (int i = lineStart+thisIndent; i < nextLine; i++) {
                // Convert HTML special characters to entities so that
                // they appear verbatim in the output.
                char c = input.charAt(i);
                if (c == '<') {
                    output.append("&lt;");
                } else if (c == '>') {
                    output.append("&gt;");
                } else if (c == '&') {
                    output.append("&amp;");
                } else {
                    output.append(c);
                }
            }
        }
        output.append(input, lineStart+textIndent, nextLine);
        classify(nextLine);
    }

    /**
     * Insert a string in the output buffer at the end of the last line in
     * the output buffer (but before the terminating characters for that
     * line, such as newline).
     * @param s                    Text to insert.
     */
    protected void appendToLastLine(String s) {
        // Skip back over the terminating character(s) of the last line,
        // if there are any.
        int index = output.length();
        if (index > 0) {
            if (output.charAt(index-1) == '\n') {
                index--;
            }
            if ((index > 0) && (output.charAt(index-1) == '\r')) {
                index--;
            }
        }

        // Insert the text.
        output.insert(index, s);
    }
}
