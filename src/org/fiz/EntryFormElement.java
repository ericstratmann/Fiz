package org.fiz;

/**
 * The EntryFormElement class implements one-line text entries for forms,
 * using an {@code <input>} HTML element.  It supports the following
 * properties:
 *   class:          (optional) Class attribute to use for the {@code input}
 *                   element; defaults to "EntryFormElement".
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  This is used as the name
 *                   for the data value in input and output datasets and
 *                   also as the {@code name} attribute for the HTML input
 *                   element.
 *   label:          (optional) Template for label to display next to the
 *                   FormElement to identify the element for the user.
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
     * @param out                  Generated HTML is appended here.
     */
    @Override
    public void render(ClientRequest cr, Dataset data,
            StringBuilder out) {
        cr.getHtml().includeCssFile("EntryFormElement.css");
        Template.appendHtml(out, "<input type=\"text\" name=\"@id\" " +
                "class=\"@class?{EntryFormElement}\" {{value=\"@1\"}} />",
                properties, data.check(id));
    }
}
