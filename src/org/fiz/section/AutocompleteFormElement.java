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

package org.fiz.section;

import org.fiz.*;
import java.io.Serializable;

/**
 * The AutocompleteFormElement helps users fill in a field by providing
 * suggestions or options for completing what the user has already typed. It
 * supports the following properties (additional properties applicable to all
 * form elements may be found in {@link FormElement}):
 *   dataFactory:       (required) Identifies a factory method that takes a
 *                      single String argument and returns a dataset that
 *                      contains possible autocomplete choices.
 *                      Must have the form {@code class.method}, where
 *                      {@code class} is a Java class name and {@code method}
 *                      is a static method in the class.  The values expected in
 *                      the dataset are described below.  The string
 *                      argument to the factory method is current user input
 *                      for which we are trying to autocomplete.
 *   choiceName:        (optional) Identifies the field in each record returned
 *                      by the Ajax which contains the value to be used for
 *                      the autocompletion choice. Defaults to "choice."
 *
 * The dataset generated from {@code dataFactory} consists of one {@code record}
 * child for each autocomplete choice. Each autocomplete choice will be a
 * dataset containing one key-value pair where the key is specified by the
 * {@code choiceName} parameter and the value will be shown in the autocomplete
 * dropdown.
 *
 * AutocompleteFormElement automatically sets the following {@code class}
 * attributes for use in CSS:
 *   dropdown:       The {@code <div>} containing the autocomplete dropdown.
 *   highlight:      The {@code <li>} containing the currently highlighted
 *                   choice in the dropdown.
 */
public class AutocompleteFormElement extends FormElement implements DirectAjax {
    // One object of the following class is stored as a page property
    // for each AutocompleteFormElement in a page: it holds data that we
    // will need later on to process Ajax requests to request autocomplete
    // information for the form field.
    protected static class PageProperty implements Serializable {
        // The following variables are just copies of configuration properties
        // for the section, or null if no such property.
        protected String id;
        protected String dataFactory;
        protected String choiceName;

        public PageProperty(String id, String dataFactory, String choiceName) {
            this.id = id;
            this.dataFactory = dataFactory;
            this.choiceName = choiceName;
        }
    }

    // Reference to the page state for this section, stored as a page
    // property named {@code AutocompleteFormElement-id}, where {@code id}
    // is the id attribute for the section..
    protected PageProperty pageProperty;

    /**
     * Construct an AutocompleteFormElement from a set of properties that define
     * its configuration.
     *
     * @param properties        Dataset whose values are used to configure the
     *                          element. See the class documentation above for
     *                          a list of supported values.
     */
    public AutocompleteFormElement(Dataset properties) {
        super(properties);
        String choiceName = properties.checkString("choiceName");
        if (choiceName == null) {
            choiceName = "choice";
        }
        pageProperty = new PageProperty(properties.getString("id"),
                properties.getString("dataFactory"), choiceName);
    }

    /**
     * This method is an Ajax entry point, invoked to fill in the menu
     * containing candidate completions of the current text.
     * @param cr                    Overall information about the client
     *                              request being serviced; there must
     *                              be a {@code id} value, which identifies the
     *                              AutocompleteFormElement, and a
     *                              {@code userInput} value, which is the value
     *                              for which we are trying to autocomplete, in
     *                              the main dataset.
     */
    public static void ajaxQuery(ClientRequest cr) {
        Dataset main = cr.getMainDataset();
        PageProperty pageProperty = (PageProperty)
                cr.getPageProperty(main.getString("id"));

        String query = main.getString("userInput");

        Dataset data = (Dataset) Util.invokeStaticMethod(
                pageProperty.dataFactory, query);

        if (data.getDatasetList("record").size() > 0) {
            StringBuilder html = new StringBuilder();
            Template.appendHtml(html, "<ul id=\"@(1)_choices\">",
                    pageProperty.id);
            for(Dataset dataValue : data.getDatasetList("record")) {
                String value = dataValue.getString(pageProperty.choiceName);

                // Extracts the position of the query to highlight it
                int queryIndex = value.toLowerCase()
                        .indexOf(query.toLowerCase());
                String js1 = Template.expandJs(
                        "Fiz.ids.@1.selectChoice(this, true)", pageProperty.id);
                String js2 = Template.expandJs(
                        "Fiz.ids.@1.highlightChoice(this)", pageProperty.id);

                if (queryIndex == -1) {
                    Template.appendHtml(html, "<li onclick=\"@1\" " +
                            "onmouseover=\"@2\">@3</li>", js1, js2,value
                    );
                } else {
                    Template.appendHtml(html, "<li onclick=\"@1\" " +
                            "onmouseover=\"@2\">@3<strong>@4</strong>@5</li>",
                            js1, js2,
                            value.substring(0, queryIndex),
                            value.substring(queryIndex, query.length()),
                            value.substring(queryIndex + query.length(),
                                    value.length())
                    );
                }
            }
            html.append("</ul>");
            cr.updateElement(pageProperty.id + "_dropdown", html.toString());
            cr.evalJavascript("Fiz.ids.@1.showDropdown();\n",
                    pageProperty.id);
        } else {
            cr.updateElement(pageProperty.id + "_dropdown", "");
            cr.evalJavascript("Fiz.ids.@1.hideDropdown(true);\n",
                    pageProperty.id);
        }

    }

    /**
     * This method is invoked to generate HTML for this form element.
     *
     * @param cr                Overall information about the client
     *                          request being serviced.
     * @param data              Data for the form (a CompoundDataset
     *                          including both form data, if any, and
     *                          the main dataset).
     */
    @Override
    public void render(ClientRequest cr, Dataset data) {
        StringBuilder out = cr.getHtml().getBody();
        cr.setPageProperty(pageProperty.id, pageProperty);
        cr.setAuthToken();

        Template.appendHtml(out,
                "\n<!-- Start AutocompleteFormElement @id -->\n" +
                "<div class=\"@class?{AutocompleteFormElement}\" " +
                "id=\"@(id)_container\">\n", properties);
        Template.appendHtml(out,
                "  <input type=\"text\" id=\"@id\" name=\"@id\" " +
                "onkeyup=\"Fiz.ids.@id.refreshMenu()\" " +
                "onkeydown=\"Fiz.ids.@id.captureKeydown(event)\" " +
                "onblur=\"Fiz.ids.@id.hideDropdown()\" " +
                "{{value=\"@1\"}} />\n" +
                "  <div id=\"@(id)_dropdown\" class=\"dropdown\" " +
                "onmouseover=\"Fiz.ids.@id.keepOpen = true\" " +
                "onmouseout=\"Fiz.ids.@id.keepOpen = false\"></div>\n",
                properties, data.checkString(id));
        out.append("</div>\n");
        Template.appendHtml(out, "<!-- End AutocompleteFormElement @id -->\n",
                properties);

        // Generate a Javascript object containing information about the form.
        cr.evalJavascript("Fiz.ids.@id = new Fiz.AutocompleteFormElement(" +
                "\"@id\");\n", properties);
        cr.getHtml().includeCssFile("AutocompleteFormElement.css");
        cr.getHtml().includeJsFile("static/fiz/AutocompleteFormElement.js");
    }
}
