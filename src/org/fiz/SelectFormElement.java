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

import java.util.*;

/**
 * The SelectFormElement class allows users to select one of more values
 * from a predefined menu of choices.  This class supports several
 * variations on that general theme:
 *   * The choices can be defined either statically in the properties
 *     of the form element or dynamically as the result of a data request.
 *     The {@code choiceRequest} property selects between these two options.
 *     In either case, the choices are defined by a collection of nested
 *     datasets, each containing {@code name} and {@code value} values.
 *     The {@code name} value specifies what to display in the menu, and
 *     the {@code value} value is the internal form used in incoming and
 *     outgoing data requests.  If {@code name} is omitted then
 *     {@code value} is displayed in the menu.
 *   * The choices can be displayed either with a drop-down menu or with
 *     a scrolling list; the {@code height} option controls this aspect.
 *   * The form element can permit either a single selection or multiple
 *     selections; the {@code multiple} option controls this.
 *  The SelectFormElement class supports the following properties
 *  (additional properties applicable to all form elements may be found in
 *  {@link FormElement}):
 *   choice:         (optional) Default location for the choice datasets, if
 *                   neither the {@code choiceRequest} nor the
 *                   {@code choiceName} property is specified.
 *   choiceRequest:  (optional) Name of a DataRequest whose result will
 *                   contain the choices.  If this property is omitted
 *                   then the choices must be present in the properties.
 *   choiceName:     (optional) The name of the nested datasets (either in
 *                   the properties or in the result of the data request
 *                   specified by {@code choiceRequest}) containing the
 *                   choices; defaults to {@code choice}.
 *   height:         (optional) If this property is specified with a value
 *                   greater than 1, then the form element will be displayed
 *                   as a scrollable list with this many elements visible at
 *                   once; otherwise the form element will be displayed as
 *                   a drop-down menu.
 *   multiple:       (optional) If specified, this property must have the
 *                   value {@code multiple}, which means that multiple
 *                   selections will be permitted.  In this case, the
 *                   value of this form element is specified in data requests
 *                   with one nested dataset for each specified value;
 *                   each dataset will have a name given by the {@code id}
 *                   property, and it will contain a single element named
 *                   {@code value}, which holds that value.
 */

public class SelectFormElement extends FormElement {
    // Value of the {@code multiple} property.
    String multiple;

    /**
     * Construct an SelectFormElement from a set of properties that define its
     * configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public SelectFormElement(Dataset properties) {
        super(properties);
        multiple = this.properties.check("multiple");
        if ((multiple != null) && !multiple.equals("multiple")) {
            throw new InternalError("\"multiple\" property for " +
                    "SelectFormElement has illegal value \"" + multiple +
                    "\"");
        }
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method will add one or
     *                             more values to that dataset, representing
     *                             the information managed by this element.
     */
    public void collect(ClientRequest cr, Dataset in, Dataset out) {
        if (multiple == null) {
            out.set(id, in.get(id));
        } else {
            // Must return multiple values:
            for (Dataset d : in.getChildren(id)) {
                out.addChild(id, d);
            }
        }
    }

    /**
     * This method is invoked to generate HTML for this form element.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             global dataset).
     */
    @Override
    public void render(ClientRequest cr, Dataset data) {
        StringBuilder out = cr.getHtml().getBody();
        // Create a HashSet that keeps track of the initially selected values.
        HashSet<String> initialSelections = new HashSet<String>();
        if (multiple == null) {
            String initialValue = data.check(id);
            if (initialValue != null) {
                initialSelections.add(initialValue);
            }
        } else {
            ArrayList<Dataset> children = data.getChildren(id);
            for (Dataset d : children) {
                initialSelections.add(d.get("value"));
            }
        }

        String choiceName = properties.check("choiceName");
        ArrayList<Dataset> choices;
        if (choiceName == null) {
            choiceName = "choice";
        }
        String choiceRequestName = properties.check("choiceRequest");
        if (choiceRequestName != null) {
            DataRequest request = cr.getDataRequest(choiceRequestName);
            Dataset responseData = request.getResponseData();
            if (responseData == null) {
                // The request to get the choices failed; display an error
                // message and use an empty list of choices.
                cr.addErrorsToBulletin(request.getErrorData());
                choices = new ArrayList<Dataset>();
            } else {
                choices = responseData.getChildren(choiceName);
            }
        } else {
            choices = properties.getChildren(choiceName);
        }
        cr.getHtml().includeCssFile("SelectFormElement.css");
        Template.appendHtml(out, "\n<!-- Start SelectFormElement @id -->\n" +
                "<select id=\"@id\" name=\"@id\" " +
                "class=\"@class?{SelectFormElement}\" " +
                "{{size=\"@height\"}} {{multiple=\"@multiple\"}}>\n",
                properties);

        String allChoices = "";
        for (int i = 0; i < choices.size(); i++) {
            Dataset choice = choices.get(i);

            String selected = null;
            if (initialSelections.contains(choice.get("value"))) {
                selected = "selected";
            }
            Template.appendHtml(out, "  <option {{selected=\"@1\"}} " +
                    "value=\"@value\">@name?{@value}</option>\n",
                    choice, selected);

            if (i > 0) {
                allChoices += ",";
            }
            allChoices += choice.get("value");
        }
        out.append("</select>\n<!-- End SelectFormElement @id -->\n");
        addValidator(new Dataset("type", "value", "valid", "allChoices"));
    }
}
