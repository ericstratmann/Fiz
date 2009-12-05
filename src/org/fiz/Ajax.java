/* Copyright (c) 2009 Stanford University
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
        Template.appendUrl(out, urlTemplate, data);
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
        Template.appendUrl(javascript, urlTemplate, indexedData);
        javascript.append("\"");
        javascript.append("});");
        return javascript.toString();
    }
}
