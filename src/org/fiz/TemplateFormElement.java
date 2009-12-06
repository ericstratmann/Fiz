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
 * The TemplateFormElement class creates a form element from an HTML
 * template.  This is used most often to create decorations in a form
 * such as headings.  This class supports the following properties:
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.
 *   label:          (optional) Template for label to display next to the
 *                   checkbox to identify the element for the user.
 *   span:           (optional) If this property has the value {@code true}
 *                   then {@code label} is ignored; no label will be generated
 *                   and instead the output from {@code template} will span
 *                   both the label and control areas for this form element.
 *                   Typically used to create headings within a form.
 *   template:       (required ) HTML template to generate the form element,
 *                   expanded in the context of the data provided by the form.
 */
public class TemplateFormElement extends FormElement {

    /**
     * Construct a TemplateFormElement from a set of properties that define
     * its configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public TemplateFormElement(Dataset properties) {
        super(properties);
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
        Template.appendHtml(out, properties.getString("template"), data);
    }

    /**
     * This method is invoked by FormSection to generate HTML for this
     * element's label.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             main dataset).
     * @return                     True is normally returned; false means
     *                             no label should be displayed and the
     *                             result of the {@code html} method should
     *                             span both the label and control areas
     *                             for this element.
     */
    @Override
    public boolean renderLabel(ClientRequest cr, Dataset data) {
        String span = properties.checkString("span");
        if ((span != null) && (span.equals("true"))) {
            return false;
        }
        return super.renderLabel(cr, data);
    }
}
