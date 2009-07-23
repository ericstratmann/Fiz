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
 * The HiddenFormElement class implements form elements whose values do
 * not appear on the screen and cannot be edited directly by the user,
 * using an {@code <input>} HTML element.  These elements are used to
 * hold values that are manipulate with special editors built out of
 * Javascript; they are also used in situations where the server wants to
 * pass data through the form, back to itself when the form is submitted,
 * but the user does not need to modify the data.  HiddenFormElements support
 * the following properties:
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  This is used as the name
 *                   for the data value in input and output datasets and
 *                   also as the {@code name} attribute for the HTML input
 *                   element.
 */

public class HiddenFormElement extends FormElement {
    /**
     * Construct an HiddenFormElement from an identifier.
     * @param id                   Value for the element's {@code id}
     *                             property.
     */
    public HiddenFormElement(String id) {
        super(new Dataset ("id", id));
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
        Template.appendHtml(out, "<input type=\"hidden\" name=\"@id\" " +
                "{{value=\"@1\"}} />",
                properties, data.check(id));
    }
}
