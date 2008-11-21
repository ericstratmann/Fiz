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
     * @param reminders            Any number of Reminders to include in the
     *                             Ajax requests.
     * @param out                  Javascript code gets appended here; when
     *                             executed, it will invoke the Ajax request
     *                             using the class Fiz.Ajax.
     */
    public static void invoke(ClientRequest cr, CharSequence urlTemplate,
            Dataset data, StringBuilder out, Reminder... reminders) {
        cr.getHtml().includeJsFile("fizlib/Ajax.js");

        // Quoting is tricky:
        //   * Must first use URL quoting while expanding the URL template.
        //   * Would normally apply string quoting next (because the URL
        //     will be passed in a Javascript string) but we can skip this
        //     because URL quoting has already quoted all the characters
        //     that are special in strings.
        out.append("void new Fiz.Ajax({url: \"");
        Template.expand(urlTemplate, data, out,
                Template.SpecialChars.URL);
        out.append("\"");
        if (reminders.length > 0) {
            out.append(", reminders: [");
            String separator = "";
            for (Reminder reminder : reminders) {
                out.append(separator);
                out.append(reminder.getJsReference());
                separator = ", ";
            }
            out.append("]");
        }
        out.append("});");
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
     * @param reminders            Any number of Reminders to include in the
     *                             Ajax requests.
     * @return                     Javascript code that will invoke the Ajax
     *                             request using the class Fiz.Ajax.
     */
    public static StringBuilder invoke(ClientRequest cr,
            CharSequence urlTemplate, Dataset data, Reminder... reminders) {
        StringBuilder javascript = new StringBuilder(urlTemplate.length()
                + 40);
        invoke(cr, urlTemplate, data, javascript, reminders);
        return javascript;
    }
}
