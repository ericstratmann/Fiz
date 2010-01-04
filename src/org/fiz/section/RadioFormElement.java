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

/**
 * The RadioFormElement class is used for "select one of several"
 * applications in forms.  The choices are represented by several
 * RadioFormElements, each representing one of the choices.  A
 * RadioFormElement displays a "selected" indicator followed by
 * arbitrary text or HTML (determined by the {@code extra} property).
 * RadioFormElements support the following properties:
 *   class:          (optional) Class attribute to use for the {@code <div>}
 *                   containing this element; defaults to RadioFormElement.
 *   extra:          (optional) HTML template for additional information
 *                   to display to the right of the form element.
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name that identifies a group of related
 *                   RadioFormElements; must be unique among all ids for
 *                   the page.  This is used as the name for the data value
 *                   in input and output datasets and also as the {@code name}
 *                   attribute for the HTML input element.
 *   label:          (optional) HTML template for label to display next
 *                   to the form element to identify the element for the user.
 *                   If omitted, {@code id} is used as the label.
 *   value:          (required) The value to include in data requests when
 *                   this form element is selected.  Each RadioFormElement
 *                   in a related group has a different {@code value}
 *                   property.  This property must not contain any characters
 *                   that are special in HTML ({@code <>&"}).
 *
 * RadioFormElement automatically sets the following {@code class} attributes
 * for use in CSS:
 *   extra:          The {@code <div>} containing {@code extra}.
 */
public class RadioFormElement extends FormElement {
    // The following variable contains the {@code value} property.
    protected String value;

    /**
     * Construct a RadioFormElement from a set of properties that define
     * its configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public RadioFormElement(Dataset properties) {
        super(properties);
        value = properties.getString("value");
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
        cr.getHtml().includeCssFile("RadioFormElement.css");
        String actual = data.checkString(id);
        Template.appendHtml(out, "<div class=\"@class?{RadioFormElement}\">" +
                "<input type=\"radio\" name=\"@id\" id=\"@(id)_@value\" " +
                "value=\"@value\"", properties);
        if ((actual != null) && (actual.equals(value))) {
            out.append(" checked=\"checked\"");
        }
        out.append(" />");

        // Display extra information, if any was requested.
        String extra = properties.checkString("extra");
        if (extra != null) {
            out.append("<span class=\"extra\" onclick=\"");
            Html.escapeHtmlChars("getElementById(" +
                    "\"" + id + "." + value + "\").checked=true;", out);
            out.append("\">");
            Template.appendHtml(out, extra, data);
            out.append("</span>");
        }
        out.append("</div>");
    }
}
