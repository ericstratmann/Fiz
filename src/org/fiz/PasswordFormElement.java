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
 * The PasswordFormElement class implements one-line text entries for forms,
 * just like EntryFormElement except that the contents of the entry are
 * hidden by displaying them as "***" rather than actual text.  This class
 * is typically used for entries where passwords will be entered.  It
 * supports the following properties:
 *   class:          (optional) Class attribute to use for the {@code input}
 *                   element; defaults to "PasswordFormElement".
 *   duplicate:      (optional) This property is used in cases where the
 *                   user is entering a new password and is asked to type
 *                   it twice.  If this property is set it means this form
 *                   element is the duplicate password, and the value of
 *                   the property is the id for the primary password.  This
 *                   form element compares its value against the primary value
 *                   and generates a form error if they don't match.  If they
 *                   do match then this element does nothing: no data is
 *                   generated by the {@code collect} method (the primary
 *                   password entry will provide that data).
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  This is used as the name
 *                   for the data value in output datasets and also as the
 *                   {@code name} attribute for the HTML input element.
 *   label:          (optional) Template for label to display next to the
 *                   FormElement to identify the element for the user.
 * Password form elements always start off empty: unlike other form elements,
 * they cannot be initialized with data from a data request.
 */

public class PasswordFormElement extends FormElement {
    /**
     * Construct an PasswordFormElement from a set of properties that define its
     * configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public PasswordFormElement(Dataset properties) {
        super(properties);
    }

    /**
     * Construct an PasswordFormElement from an identifier and label.
     * @param id                   Value for the element's {@code id}
     *                             property.
     * @param label                Value for the element's {@code label}
     *                             property.
     */
    public PasswordFormElement(String id, String label) {
        super(new Dataset ("id", id, "label", label));
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  If the
     * {@code duplicate} property has not been specified then this
     * method simply copies from {@code in} two {@code out} the value
     * whose name is contained in the {@code id} property.  If
     * {@code duplicate} has been specified, then no copying occurs;
     * instead this method compares the two input values whose names are
     * given by the {@code id} and {@code duplicate} properties and generates
     * a form error if they don't match.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method will add one or
     *                             more values to that dataset, representing
     *                             the information managed by this element.
     * @throws FormSection.FormDataException
     *                             Thrown if the {@code duplicate} property
     *                             was specified and the passwords didn't
     *                             match.
     */
    public void collect(ClientRequest cr, Dataset in, Dataset out)
            throws FormSection.FormDataException {
        String duplicate = properties.check("duplicate");
        if (duplicate != null) {
            String first = in.check(id);
            String second = in.check(duplicate);
            if ((first == null) || (second == null) || !first.equals(second)) {
                throw new FormSection.FormDataException(
                        "the passwords are not the same");
            }
        } else {
            out.set(id, in.get(id));
        }
    }

    /**
     * This method is invoked to generate HTML for this form element.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             global dataset).  Ignored here.
     * @param out                  Generated HTML is appended here.
     */
    @Override
    public void render(ClientRequest cr, Dataset data,
            StringBuilder out) {
        cr.getHtml().includeCssFile("PasswordFormElement.css");
        Template.appendHtml(out, "<input type=\"password\" name=\"@id\" " +
                "class=\"@class?{PasswordFormElement}\" />", properties);
    }
}
