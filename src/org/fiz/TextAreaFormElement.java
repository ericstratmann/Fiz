package org.fiz;

/**
 * The TextAreaFormElement class implements multi-line text entries for forms,
 * using a {@code <textarea>} HTML element.  It supports the following
 * properties:
 *   class:          (optional) Class attribute to use for the {@code textarea}
 *                   element; defaults to "TextareaFormElement".
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  This is used as the name
 *                   for the data value in query and update requests and
 *                   also as the {@code name} attribute for the HTML element.
 *   label:          (optional) Template for label to display next to the
 *                   FormElement to identify the element for the user.
 *   rows:           (optional) Number of rows to display in the
 *                   {@code textarea} element.
 * Some browsers (e.g., IE) use CRLF sequences between lines in the
 * submitted form data while other browsers use only LF (newline).  This
 * class automatically translates incoming CRLF sequences to newline so that
 * applications don't need to worry about this difference between browsers.
 */

public class TextAreaFormElement extends FormElement {
    /**
     * Construct an EntryFormElement from a set of properties that define its
     * configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public TextAreaFormElement(Dataset properties) {
        super(properties);
    }

    /**
     * Construct an EntryFormElement from an identifier and label.
     * @param id                   Value for the element's {@code id}
     *                             property.
     * @param label                Value for the element's {@code label}
     *                             property.
     */
    public TextAreaFormElement(String id, String label) {
        super(new Dataset ("id", id, "label", label));
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  This method
     * translates CRLF sequences into LF (IE generates CRLF, while Firefox
     * and Safari generate LF only).
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method will add a value to
     *                             that dataset whose name is given by our
     *                             {@code id} property.
     */
    public void collect(ClientRequest cr, Dataset in, Dataset out) {
        String formValue = in.check(id);
        if (formValue != null) {
            out.set(id, formValue.replace("\r\n", "\n"));
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
    public void html(ClientRequest cr, Dataset data,
            StringBuilder out) {
        cr.getHtml().includeCssFile("TextAreaFormElement.css");
        Template.expand("<textarea name=\"@id\" " +
                "class=\"@class?{TextAreaFormElement}\" rows=\"@rows?{10}\">" +
                "{{@1}}</textarea>", properties, out, data.check(id));
    }
}
