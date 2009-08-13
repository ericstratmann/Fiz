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
 * supports the following properties (additional properties applicable to all
 * form elements may be found in {@link FormElement}):
 *   duplicate:      (optional) This property is used in cases where the
 *                   user is entering a new password and is asked to type
 *                   it twice.  This property specifies the id of the primary
 *                   password form element and will make sure that the value of
 *                   this form element matches the value of the primary form
 *                   element before the form is submitted.
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
        String duplicate = properties.check("duplicate");
        if (duplicate != null) {
            addValidator(new Dataset("type", "duplicate", 
                    "otherFields", duplicate,
                    "errorMessage", "Password does not match"));
        }
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
        Template.appendHtml(out, "<input type=\"password\" id=\"@id\" " +
                "name=\"@id\" class=\"@class?{PasswordFormElement}\" />",
                properties);
    }
}
