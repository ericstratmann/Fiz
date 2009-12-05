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
 * The EntryFormElement class implements one-line text entries for forms,
 * using an {@code <input>} HTML element. See {@link FormElement} for a list of
 * properties supported by this form element.
 */

public class EntryFormElement extends FormElement {
    /**
     * Construct an EntryFormElement from a set of properties that define its
     * configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public EntryFormElement(Dataset properties) {
        super(properties);
    }

    /**
     * Construct an EntryFormElement from an identifier and label.
     * @param id                   Value for the element's {@code id}
     *                             property.
     * @param label                Value for the element's {@code label}
     *                             property.
     */
    public EntryFormElement(String id, String label) {
        super(new Dataset ("id", id, "label", label));
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
        cr.getHtml().includeCssFile("EntryFormElement.css");
        Template.appendHtml(out, "<input type=\"text\" id=\"@id\" " +
                "name=\"@id\" class=\"@class?{EntryFormElement}\" " +
                "{{value=\"@1\"}} />", properties, data.checkString(id));
    }
}
