package org.fiz;

/**
 * The Ajax class provides facilities for managing Ajax requests from
 * the browser.
 */
public class Ajax {
    /**
     * SyntaxError is thrown by readInputData and its descendents when
     * incoming Ajax data has improper structure.
     */
    public static class SyntaxError extends Error {
        /**
         * Construct a SyntaxError with a given message.
         * @param message          Detailed information about the problem
         */
        public SyntaxError(String message) {
            super(message);
        }
    }

    // The following class is used to return 2 values from getString.
    protected static class GetStringResult {

        public String value;       // String extracted from the input.
        public int end;            // Index of the character just after
                                   // the last one returned in {@code value}.
    }

    /**
     * Generate Javascript code that will invoke an Ajax request.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param urlTemplate          Template for the URL to be invoked
     *                             via Ajax.  Must refer to an Interactor
     *                             method.
     * @param data                 Data to use while expanding urlTemplate.
     *                             May be null if there are no substitutions
     *                             in the template.
     * @param out                  Javascript code gets appended here; when
     *                             executed, it will invoke the Ajax request
     *                             using the class Fiz.Ajax.
     */
    public static void invoke(ClientRequest cr,
            CharSequence urlTemplate, Dataset data, StringBuilder out) {
        cr.getHtml().includeJsFile("fizlib/Ajax.js");

        // Quoting is tricky:
        //   * Must first use URL quoting while expanding the URL template.
        //   * Would normally apply string quoting next (because the URL
        //     will be passed in a Javascript string) but we can skip this
        //     because URL quoting has already quoted all the characters
        //     that are special in strings.
        out.append("void new Fiz.Ajax(\"");
        Template.expand(urlTemplate, data, out,
                Template.SpecialChars.URL);
        out.append("\");");
    }

    /**
     * Generate Javascript code that will invoke an Ajax request.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param urlTemplate          Template for the URL to be invoked
     *                             via Ajax.  Must refer to an Interactor
     *                             method.
     * @param data                 Data to use while expanding urlTemplate.
     *                             May be null if there are no substitutions
     *                             in the template.
     * @return                     Javascript code that will invoke the Ajax
     *                             request using the class Fiz.Ajax.
     */
    public static StringBuilder invoke(ClientRequest cr,
            CharSequence urlTemplate, Dataset data) {
        StringBuilder javascript = new StringBuilder(urlTemplate.length()
                + 40);
        invoke(cr, urlTemplate, data, javascript);
        return javascript;
    }

    /**
     * Parse the message from an incoming Ajax request and add all of
     * its data to a dataset.  The POST data for an Ajax request
     * represents a dataset using a special-purpose format that is easy
     * to generate from Javascript and easy to parse here.  The basic
     * idea is to use run-length encoding, where a string is represented
     * as {@code length.contents}, where the contents of the string are
     * preceded by its length in characters and a "." to delimit
     * the length. Here is the grammar for the representation:
     * <pre>{@code
     * <dataset> = <element>*
     * <element> = <name><value>\n
     * <name> = <encodedString>
     * <encodedString> = <length>.<contents>
     * <value> = <encodedValue> | (<dataset>)+
     * }</pre>
     * Here's an example of a dataset containing a string value {@code age}
     * and a list of nested datasets named {@code children}, each with
     * a {@code name} element.
     * <pre>
     * 3.age2.24
     * 8.children(4.name5.Alice
     * )(4.name3.Bob
     * )(4.name5.Carol
     * )
     * </pre>
     * @param source               Encoded input string; typically the
     *                             POST data from an incoming Ajax request.
     * @param out                  Decoded data (including nested datasets)
     *                             are added to the contents of this
     *                             dataset.  Top-level string values replace
     *                             existing values by the same name;
     *                             top-level datasets are added to {@code out}
     *                             with {@code Dataset.addChild}.
     * @throws SyntaxError         {@code source} did not have the required
     *                             format.
     */
    public static void readInputData(CharSequence source, Dataset out)
            throws SyntaxError {
        getDataset(source, 0, '\0', out);
    }

    /**
     * This method does most of the work of {@code readInputData}.
     * It recursively reads a single dataset.
     * @param source               Encoded input string.
     * @param start                Index of the first character of this
     *                             dataset (the character after the "(",
     *                             if there is one).
     * @param term                 ")" means the dataset to read must be
     *                             terminated by ")" (i.e., it's a nested
     *                             dataset).  0 means the dataset to read
     *                             runs to the end of the string.
     * @param out                  All the information parsed from
     *                             {@code source} is added to this dataset.
     * @return                     Index of the character just after the
     *                             last one in this dataset (either the
     *                             end of the string or the character just
     *                             after the terminating ")").
     */
    protected static int getDataset(CharSequence source, int start, char term,
            Dataset out) {
        GetStringResult result = new GetStringResult();
        int i = start;
        int length = source.length();

        // Each iteration through the following loop parses one top-level
        // element for the dataset, which can be either a string value
        // a single nested dataset, or a list of nested datasets.
        while (i < length) {
            // See if we have reached the end of this dataset.
            if (source.charAt(i) == term) {
                return i+1;
            }

            // Get the name of the next element.
            getString(source, i, result);
            i = result.end;
            String name = result.value;

            // See if this is a simple value or nested dataset(s).
            if (i >= length) {
                throw new SyntaxError("syntax error in Ajax data input: " +
                        "no value for element \"" + name + "\"");
            }
            if (source.charAt(i) != '(') {
                // String value.
                getString(source, i, result);
                i = result.end;
                out.set(name, result.value);
            } else {
                // Nested dataset(s).  At the start of each iteration
                // {@code i} refers to the "(" that starts the next child
                // dataset.
                while (true) {
                    Dataset child = new Dataset();
                    i = getDataset(source, i+1, ')', child);
                    out.addChild(name, child);
                    if ((i >= length) || (source.charAt(i) != '(')) {
                        break;
                    }
                }
            }
            if ((i >= length) || (source.charAt(i) != '\n')) {
                throw new SyntaxError("syntax error in Ajax data input: " +
                        "no newline after element \"" + name + "\"");
            }
            i++;
        }
        if (term != 0) {
            throw new SyntaxError("syntax error in Ajax data input: " +
                    "missing \")\"");
        }
        return i;
    }

    /**
     * Extract a run-length encoded string from the input, and return it
     * @param source               Source string.  The characters starting
     *                             at {@code start} must consist of a
     *                             decimal integer followed by a "."
     *                             followed by the string, whose length is
     *                             given by the integer.
     * @param start                Index within {@code source} of the first
     *                             character in the run-length encoded
     *                             string.
     * @param result               Used to return the extracted string and
     *                             the location in {@code source} of the
     *                             first character after the string.
     */
    protected static void getString(CharSequence source, int start,
            GetStringResult result) {
        // Convert everything up to the next "." into a decimal integer.
        int i = start;
        int length = 0;
        int sourceLength = source.length();
        while (true) {
            if (i >= sourceLength) {
                throw new SyntaxError("syntax error in Ajax data input: " +
                        "missing \".\"");
            }
            char c = source.charAt(i);
            if (c == '.') {
                break;
            }
            length = length*10 + (c - '0');
            i++;
        }

        // Use the length to extract the actual string value.
        if (length < 0) {
            throw new SyntaxError("syntax error in Ajax data input: improper " +
                    "length field \"" + source.subSequence(start, i) + "\"");
        }
        i++;
        if ((i + length) > sourceLength) {
            throw new SyntaxError("syntax error in Ajax data input: unexpected " +
                    "end of input");
        }
        result.value = source.subSequence(i, i + length).toString();
        result.end = i + length;
    }
}
