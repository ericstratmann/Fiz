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
 *     The choices are defined by a collection of nested
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
 *   choiceData:     (required) Dataset containing the list of choices. See
 *                   above for a description.
 *   choiceName:     (optional) The name of the nested datasets in
 *                   {@code choiceData} containing the choices; defaults to
 *                   {@code choice}.
 *   height:         (optional) If this property is specified with a value
 *                   greater than 1, then the form element will be displayed
 *                   as a scrollable list with this many elements visible at
 *                   once; otherwise the form element will be displayed as
 *                   a drop-down menu.
 *   multiple:       (optional) If specified, this property must have the
 *                   value {@code multiple}, which means that multiple
 *                   selections will be permitted.  In this case, the
 *                   value of this form element is specified in data requests
 *                   using a list of values under the name given by the
 *                  {@code id} property.
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
        multiple = this.properties.checkString("multiple");
        if ((multiple != null) && !multiple.equals("multiple")) {
            throw new InternalError("\"multiple\" property for " +
                    "SelectFormElement has illegal value \"" + multiple +
                    "\"");
        }
        validatorData = new ValidatorData(id);
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
            out.set(id, in.getString(id));
        } else {
            // Must return multiple values:
            for (String s : in.getStringList(id)) {
                out.add(id, s);
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
            String initialValue = data.checkString(id);
            if (initialValue != null) {
                initialSelections.add(initialValue);
            }
        } else {
            for (String s: data.getStringList(id)) {
                initialSelections.add(s);
            }
        }

        Dataset choiceData = properties.getDataset("choiceData");
        if (choiceData.getErrorData() != null) {
            cr.addErrorsToBulletin(choiceData.getErrorData());
            choiceData = new Dataset();;
        }

        cr.getHtml().includeCssFile("SelectFormElement.css");
        Template.appendHtml(out, "\n<!-- Start SelectFormElement @id -->\n" +
                "<select id=\"@id\" name=\"@id\" " +
                "class=\"@class?{SelectFormElement}\" " +
                "{{size=\"@height\"}} {{multiple=\"@multiple\"}}>\n",
                properties);

        String choiceName = properties.checkString("choiceName");
        if (choiceName == null) {
            choiceName = "choice";
        }
        ArrayList<Dataset> choices = choiceData.getDatasetList(choiceName);
        String allChoices = "";
        for (int i = 0; i < choices.size(); i++) {
            Dataset choice = choices.get(i);

            String selected = null;
            if (initialSelections.contains(choice.getString("value"))) {
                selected = "selected";
            }
            Template.appendHtml(out, "  <option {{selected=\"@1\"}} " +
                    "value=\"@value\">@name?{@value}</option>\n",
                    choice, selected);

            if (i > 0) {
                allChoices += ",";
            }
            allChoices += choice.getString("value");
        }
        out.append("</select>\n<!-- End SelectFormElement @id -->\n");

        addValidator(new Dataset("type", "SelectFormElement.validateIn",
                "valid", allChoices,
                "multiple", multiple != null && multiple.equals("multiple") ?
                        "multiple" : "false"));
    }

    /**
     * Validates that the value of {@code id} is contained in the list of valid
     * inputs. The following properties are supported:
     *
     *   valid                      Comma separated list of valid inputs
     *   multiple                   If "multiple" is set, the validator will
     *                              run once for each value in the formData
     *                              for {@code id}.
     *
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateIn(String id, Dataset properties,
            Dataset formData) {
        String allValues = properties.getString("valid");

        if (properties.getString("multiple").equals("multiple")) {
            for (String value : formData.getStringList(id)) {
                String error = FormValidator.validateIn(id,
                        new Dataset("valid", allValues),
                        new Dataset(id, value));
                if (error != null) {
                    return error;
                }
            }
        } else {
            return FormValidator.validateIn(id, new Dataset(
                    "valid", allValues), formData);
        }
        return null;
    }

}
