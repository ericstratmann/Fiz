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
 * The UploadFormElement class implements form {@code <input>} elements
 * of type "file", which are used to upload files from the browser to the
 * server. See {@link FormElement} for a list of properties supported by 
 * this form element.
 * 
 * Note: unlike other form elements, there is no "data value" associated with
 * UploadFormElements (the form element always starts out empty, and the
 * {@code collect} method does nothing.  Is up to higher-level application
 * code to handle the incoming file data, for example by calling
 * ClientRequest.saveUploadedFile.
 */
public class UploadFormElement extends FormElement {
    /**
     * Construct an UploadFormElement from a set of properties that define its
     * configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public UploadFormElement(Dataset properties) {
        super(properties);
    }

    /**
     * Construct an UploadFormElement from an identifier and label.
     * @param id                   Value for the element's {@code id}
     *                             property.
     * @param label                Value for the element's {@code label}
     *                             property.
     */
    public UploadFormElement(String id, String label) {
        super(new Dataset ("id", id, "label", label));
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  For this kind
     * of form element there's no "data" (the file contents are handled
     * separately)
     * @param cr                   Ignored.
     * @param in                   Ignored.
     * @param out                  Ignored.
     */
    @Override
    public void collect(ClientRequest cr, Dataset in, Dataset out) {
        // Do nothing.
    }

    /**
     * This method is invoked to generate HTML for this form element.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             global dataset).  Ignored.
     */
    @Override
    public void render(ClientRequest cr, Dataset data) {
        StringBuilder out = cr.getHtml().getBody();
        cr.getHtml().includeCssFile("UploadFormElement.css");
        Template.appendHtml(out, "<input type=\"file\" id=\"@id\" " +
                "name=\"@id\" class=\"@class?{UploadFormElement}\" />",
                properties);
    }
}
