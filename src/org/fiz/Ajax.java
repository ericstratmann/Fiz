package org.fiz;

/**
 * The Ajax class provides facilities for managing Ajax requests from
 * the browser.
 */
public class Ajax {
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
    public static void invoke(ClientRequest cr, CharSequence urlTemplate,
            Dataset data, StringBuilder out) {
        cr.setAuthToken();
        cr.getHtml().includeJsFile("static/fiz/Ajax.js");

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
     * @return                     Javascript code that will invoke the Ajax
     *                             request using the class Fiz.Ajax.
     */
    public static String invoke(ClientRequest cr,
            CharSequence urlTemplate, Dataset data) {
        StringBuilder javascript = new StringBuilder(urlTemplate.length()
                + 40);
        invoke(cr, urlTemplate, data, javascript);
        return javascript.toString();
    }

    /**
     * Generate Javascript code that will invoke an Ajax request.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param urlTemplate          Template for the URL to be invoked
     *                             via Ajax.  Must refer to an Interactor
     *                             method.
     * @param indexedData          One or more objects, whose values can be
     *                             referred to in the template with
     *                             numerical specifiers such as {@code @1}.
     * @return                     Javascript code that will invoke the Ajax
     *                             request using the class Fiz.Ajax.
     */
    public static String invoke(ClientRequest cr, CharSequence urlTemplate,
            Object ... indexedData) {
        cr.setAuthToken();
        cr.getHtml().includeJsFile("static/fiz/Ajax.js");
        StringBuilder javascript = new StringBuilder(urlTemplate.length()
                + 40);

        // Quoting is tricky:
        //   * Must first use URL quoting while expanding the URL template.
        //   * Would normally apply string quoting next (because the URL
        //     will be passed in a Javascript string) but we can skip this
        //     because URL quoting has already quoted all the characters
        //     that are special in strings.
        javascript.append("void new Fiz.Ajax({url: \"");
        Template.expand(urlTemplate, javascript, Template.SpecialChars.URL,
                indexedData);
        javascript.append("\"");
        javascript.append("});");
        return javascript.toString();
    }
}
