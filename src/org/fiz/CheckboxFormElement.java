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
 * The CheckboxFormElement class allows boolean values to be entered in
 * forms using an {@code <input type="checkbox">} HTML element.  It
 * supports the following properties:
 *   class:          (optional) Class attribute to use for the <div>
 *                   containing this element; defaults to CheckboxFormElement.
 *   extra:          (optional) HTML template for additional information
 *                   to display to the right of the checkbox.
 *   falseValue:     (optional) String that appears in query and update
 *                   requests if the form element is not checked.  Defaults
 *                   to {@code false}.
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  This is used as the name
 *                   for the data value in input and output datasets and
 *                   also as the {@code name} attribute for the HTML input
 *                   element.
 *   label:          (optional) Template for label to display next to the
 *                   checkbox to identify the element for the user.
 *   trueValue:      (optional) String that appears in query and update
 *                   requests if the form element is checked.  Defaults
 *                   to {@code true}.
 *
 * CheckboxFormElement automatically sets the following {@code class}
 * attributes for use in CSS:
 *   extra:          The {@code <div>} containing {@code extra}.
 */
public class CheckboxFormElement extends FormElement {
    // The following variables contain the external values (the values
    // used when communicating with data managers) used to represent
    // true (checked) and false values of the checkbox.
    protected String trueValue;
    protected String falseValue;

    /**
     * Construct a CheckboxFormElement from a set of properties that define
     * its configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public CheckboxFormElement(Dataset properties) {
        super(properties);
        trueValue = properties.check("trueValue");
        if (trueValue == null) {
            trueValue = "true";
        }
        falseValue = properties.check("falseValue");
        if (falseValue == null) {
            falseValue = "false";
        }
    }

    /**
     * Construct a CheckboxFormElement from an identifier and label.
     * @param id                   Value for the element's {@code id}
     *                             property.
     * @param label                Value for the element's {@code label}
     *                             property.
     */
    public CheckboxFormElement(String id, String label) {
        this(new Dataset ("id", id, "label", label));
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  This method
     * translates between the representation required in the HTML
     * {@code <input>} element and the representation used in the data
     * manager.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method will add a value to
     *                             that dataset whose name is given by our
     *                             {@code id} property and whose value is
     *                             either our {@code trueValue} property or
     *                             our {@code falseValue} property.
     */
    public void collect(ClientRequest cr, Dataset in, Dataset out) {
        String formValue = in.check(id);
        if ((formValue != null) && formValue.equals("true")) {
            out.set(id, trueValue);
        } else {
            out.set(id, falseValue);
        }
    }

    /**
     * This method is invoked to generate HTML for this form element.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             global dataset).
     * @param out                  Generated HTML is appended here.
     */
    @Override
    public void render(ClientRequest cr, Dataset data,
            StringBuilder out) {
        cr.getHtml().includeCssFile("CheckboxFormElement.css");
        String value = data.check(id);
        Template.appendHtml(out, "<div class=\"@class?{CheckboxFormElement}\">" +
                "<input type=\"checkbox\" id=\"@id\" name=\"@id\" " +
                "value=\"true\"", properties);
        if ((value != null) && (value.equals(trueValue))) {
            out.append(" checked=\"checked\"");
        }
        out.append(" />");

        // Display extra information, if any was requested.
        String extra = properties.check("extra");
        if (extra != null) {
            out.append("<span class=\"extra\" onclick=\"");
            Html.escapeHtmlChars("el=getElementById(\"" + id + "\"); " +
                    "el.checked=!el.checked;", out);
            out.append("\">");
            Template.appendHtml(out, extra, data);
            out.append("</span>");
        }
        out.append("</div>");
    }
}
