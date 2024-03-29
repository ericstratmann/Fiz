/* Copyright (c) 2008-2010 Stanford University
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

import java.util.*;

/**
 * The Template class substitutes values from a Dataset into a template
 * string to produce a new string through a process called "expansion".
 * The goal for templates is to provide a simple mechanism for handling
 * the most common kinds of substitution; more complex forms, such as
 * iteration or recursion, are left to Java code.  Each character from
 * the template is copied to the output string except for the following
 * patterns, which cause substitutions:
 * @<i>name</i>             Copy the contents of the dataset element named
 *                          <i>name</i> to the output.  <i>name</i>
 *                          consists of all the standard Unicode identifier
 *                          characters following the {@code @}. It is an error
 *                          if the name doesn't exist in the dataset.
 * @(<i>name</i>)           Copy the contents of the data value named
 *                          <i>name</i> to the output.  <i>name</i>
 *                          consists of all the characters following the "("
 *                          up to the next ")".  {@code @} can be used
 *                          between the parentheses to perform substitutions
 *                          on the name; for example, {@code @(@foo)} finds
 *                          dataset element {@code foo}, uses its value as the
 *                          name of another dataset element, and substitutes
 *                          the value of that element into the output.  It is
 *                          an error if the name doesn't exist in the dataset.
 * @<i>name</i>?{...}       Default: if <i>name</i> exists in the dataset
 *                          and has a nonzero length, copy its contents
 *                          to the output and skip over {@code ?{...}}.
 *                          Otherwise, perform template expansion on the
 *                          text between the braces.
 * @<i>name</i>?{<i>t1</i>|<i>t2</i>}    Choice: if <i>name</i> exists in the
 *                          dataset perform template expansion on <i>t1</i>.
 *                          Otherwise perform template expansion on <i>t2</i>.
 * {{...}}                  Conditional substitution: normally the information
 *                          between the braces is processed just like the rest
 *                          of the template, except that the braces are not
 *                          copied to the output.  However, if there is a
 *                          data reference for which the name doesn't exist
 *                          then the information between the braces skipped:
 *                          nothing is copied to the output.
 * @@                       Append "@" to the output.
 * @{                       Append "{" to the output.
 * @}                       Append "}" to the output.
 * @*                       Any other occurrence of "@" besides those
 *                          described above is illegal and results in an error.
 * When a dataset element is substituted into a template, special characters
 * in the value will be escaped according to the method name. For example, if
 * HTML encoding has been specified, such as by calling expandHtml,
 * {@code <} characters will be translated to the entity reference
 * {@code &lt;}.  Translation occurs only for values coming from the
 * dataset, not for characters in the template itself;  you should ensure
 * that template characters already obey the output encoding rules.
 * Translation can be disabled for dataset values by invoking one of the the
 * methods ending in "Raw".
 *
 * When invoking template expansion, you can also provide data values using
 * one or more Objects instead of (or in addition to) a Dataset.  In this
 * case, you use numerical specifiers such as {@code @3} to refer to the
 * values: {@code @1} refers to the string value of the first object,
 * {@code @2} refers to the string value of the second object, and so on.
 * If the @ starts with a number, the name ends at the first non-numeric
 * character. E.g, "@2b" refers to the second object followed by a "b".
 * Use @(2b) to refer to a dataset value with key "2b"
 */

public class Template {
    /**
     * MissingValueError is thrown when a dataset element required by a
     * template does not exist.
     */
    public static class MissingValueError extends Error {
        String name;
        CharSequence template;

        /**
         * Constructs a MissingValueError
         * @param name        Value which could not be found
         * @param template    Template which was being expanded
         */
        public MissingValueError(String name, CharSequence template) {
            this.name = name;
            this.template = template;
        }

        public String getMessage() {
            return "missing value \"" + name + "\" " + "in template \"" + template + "\"";
        }
    }

    /**
     * SyntaxError is thrown when there is an incorrect construct in a
     * template, such as an {@code @} followed by an unrecognized character.
     */
    public static class SyntaxError extends Error {
        /**
         * Constructs a SyntaxError with a given message.
         * @param message          Detailed information about the problem
         */
        public SyntaxError(String message) {
            super(message);
        }
    }

    /**
     * Instances of this enum indicate how to escape special characters
     * in data values incorporated into template output.
     */
    protected enum SpecialChars {
        /**
         * The output will be used in HTML, so replace special HTML
         * characters such as {@code <} with HTML entities such as
         * {@code &lt;}.
         */
        HTML,

        /**
         * The output will be used as Javascript code; assume that all
         * substituted data values will be used in Javascript strings,
         * so use backslashes to quote special characters.
         */
        JS,

        /**
         * The output will be used as part of a URL, so use {@code %xx}
         * encoding for any characters that aren't permitted in URLs.
         */
        URL,

        /** Don't perform any transformations on the data values. */
        NONE}

    /**
     * A cache of all parsed templates. Keys are template strings.
     */
    protected static Map<CharSequence, ParsedTemplate> parsedTemplates =
        Collections.synchronizedMap(new HashMap<CharSequence, ParsedTemplate>());

    /**
     * A ParsedTemplate is an efficient representation of a template after it has
     * been parsed, so that we do not need to reparse a template every time it is
     * used.
     */
    protected static class ParsedTemplate {
        // List of fragments that make up the template
        protected ArrayList<Fragment> fragments;

        /**
         * Creates a new ParsedTemplate with no fragments.
         */
        protected ParsedTemplate() {
            this.fragments = new ArrayList<Fragment>();
        }

        /**
         * Creates a new ParsedTemplate with the given fragments.
         * @param fragments   List of fragments describing the template
         */
        protected ParsedTemplate(ArrayList<Fragment> fragments) {
            this.fragments = fragments;
        }

        /**
         * Expands a cached template, substituting values and quoting data.
         * @param info         Information describing the current expansion
         */
        public void expand(ExpandInfo info) {
            for (Fragment fragment : fragments) {
                fragment.expand(info);
            }
        }

        /**
         * Appends the fragment to this cache's list of fragments.
         * @param fragment    Describes part of this template
         */
        protected void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        /**
         * Checks all IdFragments and returns whether they are all defined
         * in the context of the current expansion (i.e., all @ variables
         * have a corresponding value passed in).
         * @param info         Information describing the current expansion
         * @return             Whether all IdFragments are defined
         */
        protected boolean checkAllIdsDefined(ExpandInfo info) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof IdFragment) {
                    IdFragment id = (IdFragment) fragment;
                    if(id.findValue(info, false) == null) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * A template is parsed into a list of {@code Fragment} objects. Expanding
     * each of these in a row is equivalent to expanding the template.
     */
    protected static interface Fragment {
        /**
         * Evaluate the fragment and add its value to the output
         * @param info         Information describing the current expansion
         */
        public abstract void expand(ExpandInfo info);
    }

    /**
     * Represents plain text in a template. No substition is done on this
     * text.
     */
    protected static class TextFragment implements Fragment {
        String text;

        /**
         * Creates a new TextFragment
         * @param text       String this fragment is representing
         */
        public TextFragment(String text) {
            this.text = text;
        }

        /**
         * Evaluate the fragment and add its value to the output
         * @param info         Information describing the current expansion
         */
        public void expand(ExpandInfo info) {
            info.out.append(text);
        }
    }

    /**
     * Represents one @ variable, such as @foo, @1, or @(@foo)
     */
    protected static class IdFragment implements Fragment {
        String name;
        // If the name is a number, such as "5", index represents the integer
        // version of the number. Otherwise the index is -1.
        int index;
        // If the @ is the form of @(...), this object represents the parsed
        // version of the text inside the parens. Otherwise this is null
        ParsedTemplate parens;

        /**
         * Creates a new IdFragment
         * @param name   Name of the @ variable. When the template is evaluated
         *               it'll get expanded with a value from a dataset
         */
        public IdFragment(String name) {
            this.name = name;
            try {
                index = Integer.parseInt(this.name);
            } catch (NumberFormatException e) {
                index = -1;
            }
            this.parens = null;
        }

        /**
         * Creates a new IdFragment corresponding to text inside parens: @(...)
         * @param parens  Parsed representation of the text in the parens. When
         *                the template is evaluated, this will be expanded and
         *                used as the name of a value to substitute
         */
        public IdFragment(ParsedTemplate parens) {
            this.parens = parens;
        }

        /**
         * Evaluate the fragment and add its value to the output
         * @param info         Information describing the current expansion
         */
        public void expand(ExpandInfo info) {
            addValue(info, findValue(info, true));
        }

        /**
         * Finds a value from a dataset or indexed data
         * @param info         Information describing the current expansion
         * @param required     If true, throws a MissingValueError if the
         *                     request value cannot be found
         * @return             The value from the dataset or indexed data
         */
        public Object findValue(ExpandInfo info, boolean required) {
            String tmpName = this.name;
            int tmpIndex = this.index;

            if (parens != null) {
                int outLen = info.out.length();
                SpecialChars oldQuoting = info.quoting;
                info.quoting = SpecialChars.NONE;

                parens.expand(info);

                tmpName = info.out.substring(outLen);
                info.quoting = oldQuoting;
                info.out.setLength(outLen);
                try {
                    tmpIndex = Integer.parseInt(tmpName);
                } catch (NumberFormatException e) {
                    tmpIndex = -1;
                }
            }

            Object value = null;
            // If there is indexed data in this expansion and the name is an
            // integer, use an indexed value if there is one corresponding to
            // the name.
            if (tmpIndex > 0 && info.indexedData != null) {
                if (tmpIndex <= info.indexedData.length) {
                    Object tmp = info.indexedData[tmpIndex-1];
                    if (tmp != null) {
                        value = tmp;
                    }
                }
            } else {
                if (info.data != null) {
                    value = info.data.check(tmpName);
                }
            }

            if (value != null || !required) {
                return value;
            }

            throw new MissingValueError(tmpName, info.template);
        }
    }

    /**
     * Represents part of a template in the form of @id?{...}
     */
    protected static class DefaultFragment implements Fragment {
        // id in @id?(frag)
        IdFragment id;
        // frag in @id?(frag)
        ParsedTemplate defaultOption;

        /**
         * Creates a DefaultFragment
         * @param id              The @ variable. If it exists, it is added to
         *                        the output. Otherwise, {@code fragment} is.
         * @param defaultOption   Added to the output if {@code id} does not exist
         */
        public DefaultFragment(IdFragment id, ParsedTemplate defaultOption) {
            this.id = id;
            this.defaultOption = defaultOption;
        }

        /**
         * Evaluate the fragment and add its value to the output
         * @param info         Information describing the current expansion
         */
        public void expand(ExpandInfo info) {
            Object value = id.findValue(info, false);
            if (valueExists(value)) {
                addValue(info, value);
            } else {
                defaultOption.expand(info);
            }
        }
    }

    /**
     * Represents part of a template in the form @id{t1|t2}.
     */
    protected static class ChoiceFragment implements Fragment {
        // id in @id{t1, t2}
        IdFragment id;
        // t1 in @id{t1, t2}
        ParsedTemplate first;
        // t2 in @id{t1, t2}
        ParsedTemplate second;
        /**
         * Creates a new ChoiceFragment
         * @param id      If id exists, first is expanded to the output,
         *                otherwise second is
         * @param first   Added to the output if {@code id} exists
         * @param second  Added to the output if {@code id} does not exist
         */
        public ChoiceFragment(IdFragment id, ParsedTemplate first, ParsedTemplate second) {
            this.id = id;
            this.first = first;
            this.second = second;
        }

        /**
         * Evaluate the fragment and add its value to the output
         * @param info         Information describing the current expansion
         */
        public void expand(ExpandInfo info) {
            Object value = id.findValue(info, false);
            if(valueExists(value)) {
                first.expand(info);
            } else {
                second.expand(info);
            }
        }
    }

    /**
     * Represents part of a template in the form {{...}}.  The only tricky
     * thing about this fragment is that we try to generate cleaner output
     * by collapsing spaces around fragments that are skipped. For example,
     * consider the following cases:
     * 1.    xyz {{&#064;a}} abc
     * 2.    {{&#064;a}} {{&#064;b}}{{&#064;c}}{{&#064;d}}
     * 3.    abc [{{&#064;a}} xyz]
     * In case 1, if &#064;a doesn't exist then there will only be one space in
     * the output.  In case 2, there will be a space in the output only if
     * &#064;a exists and at least one of @b, @c, and @d exists.  In case 3
     * there will be no spaces between the brackets if @a doesn't exist.
     */
    protected static class ConditionalFragment implements Fragment {
        // Parsed representation of text inside brackets
        ParsedTemplate contents;

        // Copies of constructor arguments; see the constructor for details..
        boolean borrowedSpace;
        boolean precedingSpaceCollapsible;
        boolean tryCollapsingBefore;

        /**
         * Creates a new ConditionalFragment.
         * @param contents          Added to the output if all @ variables
         *                          in it exist. Otherwise, nothing is added
         *                          to the output.
         * @param borrowedSpace     True means there was a collapsible space
         *                          character in the template just after this
         *                          fragment, but we moved it inside the
         *                          fragment instead.  Thus, if this fragment
         *                          ends up getting skipped we have
         *                          automatically collapsed spaces properly
         *                          without any additional work.
         * @param precedingSpaceCollapsible
         *                          True means there was a space character
         *                          in the template immediately preceding this
         *                          fragment, which is a candidate for
         *                          collapsing.  If this character doesn't
         *                          get collapsed by a preceding fragment
         *                          (e.g. @a in case 2 above), then it can
         *                          be collapsed by a subsequent fragment,
         *                          such as @d in case 2 above (assuming
         *                          neither @b or @c exist).
         * @param tryCollapsingBefore   True means that this fragment was at
         *                          the end of the template, or was followed by
         *                          something like ']', so it would be
         *                          appropriate to collapse a space
         *                          preceding the element, if one is available
         *                          during template expansion.
         */
        public ConditionalFragment(ParsedTemplate contents,
                boolean borrowedSpace, boolean precedingSpaceCollapsible,
                boolean tryCollapsingBefore) {
            this.contents = contents;
            this.borrowedSpace = borrowedSpace;
            this.precedingSpaceCollapsible = precedingSpaceCollapsible;
            this.tryCollapsingBefore = tryCollapsingBefore;
        }

        /**
         * Evaluate the fragment and add its value to the output
         * @param info         Information describing the current expansion
         */
        public void expand(ExpandInfo info) {
            // Figure out what we will do in terms of collapsing spaces
            // if this fragment ends up getting skipped. It's easy
            // if we are going to collapse out a space after the fragment;
            // in this case borrowedSpace is true and there's nothing
            // else for us to do.  Collapsing a space before the fragment
            // is trickier; we can do this only if the characters after
            // the fragment in the template have the right form (indicated
            // by precedingSpaceCollapsible), and if there is a space
            // before the fragment that is suitable for collapsing (it
            // must have come from the template rather than from a prior
            // substitution; info.lastCollapsibleSpace indicates this).
            boolean collapseBefore = false;
            int outLen = info.out.length();
            if (outLen > 0) {
                if ((precedingSpaceCollapsible) &&
                        (info.out.charAt(outLen-1) == ' ')) {
                    info.lastCollapsibleSpace = outLen-1;
                }
                // If we end up skipping this element, is there a space
                // preceding the element that we should collapse?  Note:
                // there could have been several other fragments between
                // the collapsible space and the current fragment, as long
                // as all of the intervening fragments were conditionals
                // that ended up being skipped (e.g. @d in case 2 above).
                collapseBefore = (!borrowedSpace && tryCollapsingBefore &&
                        ((outLen-1) == info.lastCollapsibleSpace));
            }

            if (contents.checkAllIdsDefined(info) == false) {
                if (collapseBefore) {
                    info.out.setLength(outLen-1);
                    // We used up the collapsible space.
                    info.lastCollapsibleSpace = -1;
                }
                return;
            }

            contents.expand(info);
        }
    }

    // Holds information used during expansion
    protected static class ExpandInfo {
        // Output is appended here
        protected StringBuilder out;
        // String representation of the template we are expanding
        // (Used in error messages)
        protected CharSequence template;
        // Style of quoting to use
        protected SpecialChars quoting;
        // If we are expanding for a SQL query, values are added here instead
        // of being substituted in the template
        protected ArrayList<String> sqlParameters;
        // Dataset of values to substitute
        protected Dataset data;
        // Used for sections
        protected ClientRequest cr;
        // Indexed data to substitute
        protected Object[] indexedData;
        // Index of the last space character that can be collapsed by
        // ConditionalFragments.
        protected int lastCollapsibleSpace = -1;

        protected ExpandInfo(StringBuilder out, CharSequence template,
                             SpecialChars quoting, ArrayList<String> sqlParameters,
                             ClientRequest cr, Dataset data, Object ... indexedData) {
            if (out == null) {
                out = new StringBuilder();
            }
            this.out = out;
            this.template = template;
            this.quoting = quoting;
            this.sqlParameters = sqlParameters;
            this.data = data;
            this.cr = cr;
            this.indexedData = indexedData;
        }
    }


    // The following class is used internally to pass information between
    // methods while parsing templates.  Among other things, it provides a
    // vehicle for returning multiple results from a method.
    protected static class ParseInfo {
        CharSequence template;     // The template being processed.
        int end;                   // Modified by methods such as
                                   // parseName to hold the index of the
                                   // character just after the last one
                                   // processed by the method.  For example,
                                   // if the method processed "@(name)"
                                   // then this value will give the index
                                   // of the next character after the ")".
        StringBuilder text;        // When parsing plain text (e.g., not a @),
                                   // text is appended here before being turned
                                   // into a fragment.
        ParsedTemplate parse;      // Fragments are appended to this cache.
        /**
         * Construct a ParseInfo object.
         * @param template         Template to be expanded.
         */
        public ParseInfo(CharSequence template) {
            this.template = new StringBuilder(template);
            this.text = new StringBuilder();
            this.parse = new ParsedTemplate();
        }
    }

    // No constructor: this class only has a static methods.
    private Template() {}


    /**
     * Checks whether a value "exists" in the context of expanding a
     * DefaultFragment or ChoiceFragment, such as in @value?{...}.
     * See the comments for the class for a more detailed description.
     * @param value           The "foo" of @foo?{...}
     * @return                Whether the value exists and is non-empty
     */
    protected static boolean valueExists(Object value) {
        return value instanceof Section || (value != null &&
                                            value.toString().length() > 0);
    }

    /**
     * Adds a value to the ouput, escaping it if necessary
     * @param info         Information describing the current expansion.
     *                     info.quoting determines what sort of quoting to do
     * @param value        String which is quoted and added to output
     */
    protected static void addValue(ExpandInfo info, Object value) {
        if (value instanceof Section) {
            if (info.cr == null) {
                throw new InternalError("Cannot expand section without a " +
                                        "client request");
            }
            ((Section) value).render(info.cr);
            return;
        }

        String val = value.toString();
        if (info.quoting == SpecialChars.HTML) {
            Html.escapeHtmlChars(val, info.out);
        } else if (info.quoting == SpecialChars.JS) {
            Html.escapeStringChars(val, info.out);
        } else if (info.quoting == SpecialChars.URL) {
            Html.escapeUrlChars(val, info.out);
        } else if (info.quoting == SpecialChars.NONE) {
            info.out.append(val);
        } else if (info.sqlParameters != null) {
            info.sqlParameters.add(val);
            info.out.append("?");
        } else {
            throw new InternalError("unknown quoting value in " +
                                    "Template.quoteString");
        }
    }

    /**
     * Expands a template
     * @param out             Output is appended here. If null, a new StringBuilder
     *                        is created
     * @param template        Template to expand
     * @param quoting         Style of quoting used on substituted values
     * @param data            Provides data to be substituted into the template.
     * @param indexedData     One or more objects, whose values can be
     *                        referred to in the template with
     *                        numerical specifiers such as {@code @1}.
     *                        Null values may be supplied to indicate
     *                        "no object with this index".
     * @return                The expanded template
     */
    protected static StringBuilder expand(StringBuilder out, CharSequence template,
                     SpecialChars quoting, Dataset data, Object ... indexedData) {
        return expand(out, template, quoting, null, null, data, indexedData);
    }

    /**
     * Expands a template
     * Returns a parsed version of the template, creating it if necessary.
     * @param out             Output is appended here. If null, a new StringBuilder
     *                        is created
     * @param template        Template to expand
     * @param quoting         Style of quoting used on substituted values
     * @param sqlParameters   If not null, substituted values are appended here
     * @param data            Provides data to be substituted into the template.
     * @param indexedData     One or more objects, whose values can be
     *                        referred to in the template with
     *                        numerical specifiers such as {@code @1}.
     *                        Null values may be supplied to indicate
     *                        "no object with this index".
     * @return                The expanded template
     */
    protected static StringBuilder expand(StringBuilder out, CharSequence template,
                     SpecialChars quoting, ArrayList<String> sqlParameters,
                     ClientRequest cr, Dataset data, Object ... indexedData) {
        ParsedTemplate parsed = parsedTemplates.get(template);
        if (parsed == null) {
            ParseInfo info = new ParseInfo(template);
            parseTo(info, 0);
            parsed = info.parse;
            parsedTemplates.put(template, parsed);
        }

        ExpandInfo info = new ExpandInfo(out, template, quoting, sqlParameters, cr, data, indexedData);
        parsed.expand(info);
        return info.out;
    }

    /**
     * Substitute data (including sections) into a template string, using HTML
     * conventions for escaping special characters in substituted values. HTML
     * is appeneded to the ClientRequest's output.
     * @param cr                   Output is appended to the client request
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendToClientRequest(ClientRequest cr, CharSequence template,
                                  Dataset data, Object ... indexedData) {
        expand(cr.getHtml().getBody(), template, SpecialChars.HTML, null, cr,
               data, indexedData);
    }

    /**
     * Substitute data into a template string, using HTML conventions
     * for escaping special characters in substituted values.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the result of the
     *                             expansion.
     */
    public static String expandHtml(CharSequence template, Dataset data,
            Object ... indexedData) throws SyntaxError {
        return expand(null, template, SpecialChars.HTML, data,
                      indexedData).toString();
    }

    /**
     * Substitute data into a template string, using HTML conventions
     * for escaping special characters in substituted values.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the results of the
     *                             expansion.
     */
    public static String expandHtml(CharSequence template,
            Object ... indexedData) throws MissingValueError, SyntaxError {
        return expand(null, template, SpecialChars.HTML, null,
                      indexedData).toString();
    }

    /**
     * Substitute data into a template string, using HTML conventions
     * for escaping special characters in substituted values.
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendHtml(StringBuilder out, CharSequence template,
                                  Dataset data, Object... indexedData)
            throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.HTML, data, indexedData);
    }

    /**
     * Substitute data into a template string, using HTML conventions
     * for escaping special characters in substituted values.
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendHtml(StringBuilder out, CharSequence template,
                                  Object... indexedData)
            throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.HTML, null, indexedData);
    }

    /**
     * Substitute data into a template string, using Javascript string
     * conventions for escaping special characters in substituted values.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the result of the
     *                             expansion.
     */
    public static String expandJs(CharSequence template, Dataset data,
            Object ... indexedData)
            throws MissingValueError, SyntaxError {
        return expand(null, template, SpecialChars.JS, data,
                      indexedData).toString();
    }

    /**
     * Substitute data into a template string, using Javascript string
     * conventions for escaping special characters in substituted values.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the results of the
     *                             expansion.
     */
    public static String expandJs(CharSequence template,
            Object ... indexedData) throws MissingValueError, SyntaxError {
        return expand(null, template, SpecialChars.JS, null,
                                          indexedData).toString();
    }

    /**
     * Substitute data into a template string, using Javascript string
     * conventions for escaping special characters in substituted values.
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendJs(StringBuilder out,
            CharSequence template, Dataset data, Object... indexedData)
            throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.JS, data, indexedData);
    }

    /**
     * Substitute data into a template string, using Javascript string
     * conventions for escaping special characters in substituted values.
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendJs(StringBuilder out,
            CharSequence template, Object... indexedData)
            throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.JS, null, indexedData);
    }

    /**
     * Substitute data into a template string, using URL encoding for
     * escaping special characters in substituted values.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the result of the
     *                             expansion.
     */
    public static String expandUrl(CharSequence template, Dataset data,
            Object ... indexedData)
            throws MissingValueError, SyntaxError {
        return expand(null, template, SpecialChars.URL, data,
                      indexedData).toString();
    }

    /**
     * Substitute data into a template string, using URL encoding for
     * escaping special characters in substituted values.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the results of the
     *                             expansion.
     */
    public static String expandUrl(CharSequence template,
            Object ... indexedData) throws MissingValueError, SyntaxError {
        return expand(null, template, SpecialChars.URL, null,
                                          indexedData).toString();
    }

    /**
     * Substitute data into a template string, using URL encoding for
     * escaping special characters in substituted values.
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendUrl(StringBuilder out, CharSequence template,
            Dataset data, Object... indexedData)
            throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.URL, data, indexedData);
    }

    /**
     * Substitute data into a template string, using URL encoding for
     * escaping special characters in substituted values.
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendUrl(StringBuilder out, CharSequence template,
            Object... indexedData) throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.URL, null, indexedData);
    }

    /**
     * Substitute data into a template string (no escaping of special
     * characters in substituted values).
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the result of the
     *                             expansion.
     */
    public static String expandRaw(CharSequence template, Dataset data,
            Object ... indexedData)
            throws MissingValueError, SyntaxError {
        return expand(null, template, SpecialChars.NONE, data,
                      indexedData).toString();
    }

    /**
     * Substitute data into a template string (no escaping of special
     * characters in substituted values).
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     * @return                     A String containing the results of the
     *                             expansion.
     */
    public static String expandRaw(CharSequence template,
            Object ... indexedData) throws MissingValueError, SyntaxError {
        return expand(null, template, SpecialChars.NONE, null,
                      indexedData).toString();
    }

    /**
     * Substitute data into a template string (no escaping of special
     * characters in substituted values).
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendRaw(StringBuilder out, CharSequence template,
            Dataset data, Object... indexedData)
            throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.NONE, data, indexedData);
    }

    /**
     * Substitute data into a template string (no escaping of special
     * characters in substituted values).
     * @param out                  The results of expansion are appended here.
     * @param template             Contains text to be copied to
     *                             {@code out} plus substitution
     *                             specifiers such as {@code @foo}.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     *                             Null values may be supplied to indicate
     *                             "no object with this index".
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static void appendRaw(StringBuilder out, CharSequence template,
            Object... indexedData)
            throws MissingValueError, SyntaxError {
        expand(out, template, SpecialChars.NONE, null, indexedData);
    }

    /**
     * Substitute data into a template SQL query and return the expanded
     * result.  Because of the way variables are handled in SQL queries,
     * variable values are not substituted directly into the query.  Instead,
     * each substitution causes a "?" character to appear in the output query.
     * The variable values are collected in a separate ArrayList for the
     * caller, which will then invoke a JDBC method to attach them to the
     * SQL statement for the query.
     * @param template             Contains an SQL query that may contain
     *                             substitution specifiers such as
     *                             {@code @foo}.
     * @param data                 Provides data to be substituted into the
     *                             template.
     * @param sqlParameters        For each substitution in {@code template},
     *                             the value for the substitution is appended
     *                             to this ArrayList and "?" is appended to
     *                             the result string.  The caller will pass
     *                             {@code sqlParameters} to JDBC when it
     *                             invokes the SQL statement.
     * @return                     The result produced by expanding the
     *                             template.
     * @throws MissingValueError   A required data value couldn't be found.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    public static String expandSql(CharSequence template, Dataset data,
            ArrayList<String> sqlParameters)
            throws MissingValueError, SyntaxError {
        return expand(null, template, null, sqlParameters, null, data).toString();
    }

    /**
     * Parses a template until one of the delimiters is matched.
     * @param info                 Contains information about the template
     *                             being parsed.  This method appends
     *                             framents to info.parse. Info.end is set to
     *                             the index of the first character that matches
     *                             ones of the delimiters or the length of the
     *                             template if none are matched.
     * @param start                Begin parsing at this position
     * @param delimiters           List of delimiters that marks the end of the
     *                             template
     */
    protected static void parseTo(ParseInfo info, int start, String ... delimiters) {
        boolean foundEnd = false;
        int len = info.template.length();

        int i;
        for (i = start; !foundEnd && i != len; ) {
            char c = info.template.charAt(i);
            char next = 0;
            if (i + 1 != len) {
                next = info.template.charAt(i+1);
            }

            if (c == '@') {
                parseAtSign(info, i+1);
                i = info.end;
            } else if (c == '{' && next == '{') {
                parseBraces(info, i+2);
                i = info.end;
            } else if (foundDelimiter(info.template, i, delimiters)) {
                foundEnd = true;
            } else {
                info.text.append(c);
                i++;
            }
        }

        flushText(info);
        info.end = i;
    }

    /**
     * This method is invoked to process the part of a template immediately
     * following an "@".
     * @param info                 Contains information about the template
     *                             being parsed.  This method appends
     *                             framents to info.parse. Info.end is set
     *                             to the index of the first character following
     *                             the @-specifier (e.g. for {@code @foo+bar}
     *                             info.end will refer to the {@code +} and for
     *                             {@code @abc d} info.end will refer to the "d".
     * @param start                Index of the character immediately after
     *                             the {@code @}.
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    protected static void parseAtSign(ParseInfo info, int start) {
        if (start >= info.template.length()) {
            throw new SyntaxError("dangling \"@\" in template \"" +
                    info.template + "\"");
        }
        char c = info.template.charAt(start);
        if (Character.isUnicodeIdentifierStart(c) || Character.isDigit(c)) {
            flushText(info);
            if (Character.isDigit(c)) {
                info.end = StringUtil.numberEnd(info.template, start);
            } else {
                info.end = StringUtil.identifierEnd(info.template, start);
            }
            String name = info.template.subSequence(start,
                    info.end).toString();
            if ((info.end < info.template.length())
                    && (info.template.charAt(info.end) == '?'))  {
                parseChoice(info, name, info.end+1);
            } else {
                info.parse.addFragment(new IdFragment(name));
            }
        } else if (c == '(') {
            flushText(info);
            parseParenName(info, start+1);
        } else if (c == '@' || c == '{' || c == '}') {
            info.text.append(c);
            info.end = start + 1;
        } else {
            throw new SyntaxError("invalid sequence \"@" + c
                    + "\" in template \"" + info.template + "\"");
        }
    }

    /**
     * This method is invoked to parse substitutions that start with
     * {@code @name?}.
     * @param info                 Overall information about the template
     *                             being parsed. This method appends fragments
     *                             to info.parse and sets info.end to the index
     *                             of the first character after the end of
     *                             this substitution.
     * @param name                 The name of the variable (everything
     *                             between the "@" and the "?").
     * @param start                Index in info.template of the character
     *                             just after the "?".
     * @throws SyntaxError         The template contains an illegal construct.
     */
    protected static void parseChoice(ParseInfo info, String name,
            int start) throws SyntaxError {

        flushText(info);
        CharSequence template = info.template;
        IdFragment nameFragment = new IdFragment(name);

        if ((start >= info.template.length()) || (template.charAt(start) != '{')) {
            throw new SyntaxError("missing \"{\" after \"?\" " +
                    "in template \"" + template + "\"");
        }

        ParsedTemplate oldCache = info.parse;
        ParsedTemplate cache1 = new ParsedTemplate();
        info.parse = cache1;
        parseTo(info, start + 1, "|", "}");
        if (info.end >= info.template.length()) {
            throw new SyntaxError("incomplete @...?{...} substitution " +
                    "in template \"" + template + "\"");
        }

        if (info.template.charAt(info.end) == '|') {
            ParsedTemplate cache2 = new ParsedTemplate();
            info.parse = cache2;

            parseTo(info, info.end + 1, "}");

            if (info.end == info.template.length()) {
                throw new SyntaxError("incomplete @...?{...} substitution " +
                        "in template \"" + template + "\"");
            }
            oldCache.addFragment(new ChoiceFragment(nameFragment, cache1, cache2));
            info.end++;
        } else {
            oldCache.addFragment(new DefaultFragment(nameFragment, cache1));
            info.end++;
        }


        info.parse = oldCache;
    }

    /**
     * This method is invoked to parse parenthesized names, such as
     * {@code @(@first+@second)}.
     * @param info                 Contains information about the template
     *                             being parsed.  This method appends a
     *                             fragment to info.parse. Info.end will
     *                             be set to the index of the character just
     *                             after the closing parenthesis.
     * @param start                Index of the character immediately after
     *                             the "@(".
     * @throws SyntaxError         The template contains an illegal construct
     *                             such as {@code @+}.
     */
    protected static void parseParenName(ParseInfo info, int start)
            throws SyntaxError {


        ParsedTemplate oldCache = info.parse;
        info.parse = new ParsedTemplate();

        parseTo(info, start, ")");

        if (info.end >= info.template.length()) {
            throw new SyntaxError("missing \")\" for \"@(\" in template \""
                                  + info.template + "\"");
        }
        oldCache.addFragment(new IdFragment(info.parse));
        info.end++;
        info.parse = oldCache;
    }

    /**
     * This method is invoked to parse a portion of a template that
     * lies between double curly braces.
     * @param info                 Contains information about the template
     *                             being parsed.  This method appends a
     *                             fragment to info.parse. Info.end will be set
     *                             to the index of the character just after the
     *                             closing braces.
     * @param start                Index of the character immediately after
     *                             the "{{".
     * @throws SyntaxError         The template is illegal, e.g. it doesn't
     *                             contain closing braces.
     */
    protected static void parseBraces(ParseInfo info, int start)
            throws SyntaxError {
        flushText(info);
        ParsedTemplate oldCache = info.parse;
        info.parse = new ParsedTemplate();

        parseTo(info, start, "}}");

        if (info.end >= info.template.length()) {
            throw new SyntaxError("unmatched \"{{\" in template \""
                                  + info.template + "\"");
        }

        // Collect information that can be used to collapse spaces around
        // the conditional element if the conditional element ends up
        // being skipped. First, identify the characters on either side
        // of the {{...}}.
        int indexBeforeBraces = start - 3;
        char charBefore;
        if (indexBeforeBraces >= 0) {
            charBefore = info.template.charAt(indexBeforeBraces);
        } else {
            // The {{ is at the beginning of the template; pretend
            // the character before {{ is '<', which behaves the same
            // as the start of the template.
            charBefore = '<';
        }
        int end = info.end +2;
        char charAfter;
        if (end < info.template.length()) {
            charAfter = info.template.charAt(end);
        } else {
            // The }} is at the end of the template; pretend the
            // character after }} is '>', which behaves the same
            // as the end of the template.
            charAfter = '>';
        }

        // If there is a space after the {{...}} that can be collapsed
        // then just "move" it inside the {{...}}.  In this case there's
        // no extra work to be done if the conditional element ends up
        // being skipped.
        boolean borrowedSpace = false;
        boolean tryCollapsingBefore = false;
        if ((charAfter == ' ') && ((charBefore == ' ') || (charBefore == '[')
                || (charBefore == '<') || (charBefore == '{')
                || (charBefore == '(') || (charBefore == '\"')
                || (charBefore == '\''))) {
            ArrayList<Fragment> frags = info.parse.fragments;
            Fragment last = null;
            if (frags.size() > 0) {
                last = frags.get(frags.size()-1);
            }

            if (last instanceof TextFragment) {
                ((TextFragment) last).text += " ";
            } else {
                frags.add(new TextFragment(" "));
            }
            borrowedSpace = true;
            end++;
        } else {
            tryCollapsingBefore = (charAfter == ']')
                    || (charAfter == '>') || (charAfter == '}')
                    || (charAfter == ')') || (charAfter == '\"')
                    || (charAfter == '\'');
        }

        oldCache.addFragment(new ConditionalFragment(info.parse, borrowedSpace,
                (charBefore == ' '), tryCollapsingBefore));
        info.end = end;
        info.parse = oldCache;
    }

    /**
     * Determines whether the current position in a string marks the beginning
     * of one of the delimiters.
     *
     * @param template        This string is searched for delimiters
     * @param start           Current position in the string
     * @param delimiters      List of delimiters to check for in {@code str}
     */
    protected static boolean foundDelimiter(CharSequence template, int start,
                                            String ... delimiters) {
        int charsLeft = template.length() - start;
        int i;
        for (String delimiter : delimiters) {
            int len = delimiter.length();
            for (i = 0; i < len && i < charsLeft; i++) {
                if (template.charAt(start + i) != delimiter.charAt(i)) {
                        break;
                }
            }
            if (i == len) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates and appends a TextFragment to the current list of fragments.
     * @param info            Contains information about the template being
     *                        parsed.
     */
    protected static void flushText(ParseInfo info) {
        if (info.text.length() > 0) {
            info.parse.addFragment(new TextFragment(info.text.toString()));
        }
        info.text.setLength(0);
    }

}
