package org.fiz;

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
 *                   in query and update requests and also as the {@code name}
 *                   attribute for the HTML input element.
 *   label:          (optional) HTML template for label to display next
 *                   to the form element to identify the element for the user.
 *                   If omitted, {@code id} is used as the label.
 *   value:          (required) The value to include in data requests when
 *                   this form element is selected.  Each RadioFormElement
 *                   in a related group has a different {@code value}
 *                   property.  This property must not contain any characters
 *                   that are special in HTML ({@code <>&"}).
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
        value = properties.get("value");
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
        cr.getHtml().includeCssFile("RadioFormElement.css");
        String actual = data.check(id);
        Template.expand("<div class=\"@class?{RadioFormElement}\">" +
                "<input type=\"radio\" name=\"@id\" id=\"@id.@value\" " +
                "value=\"@value\"", properties, out);
        if ((actual != null) && (actual.equals(value))) {
            out.append(" checked=\"checked\"");
        }
        out.append(" />");

        // Display extra information, if any was requested.
        String extra = properties.check("extra");
        if (extra != null) {
            out.append("<span class=\"extra\" onclick=\"");
            Html.escapeHtmlChars("getElementById(" +
                    "\"" + id + "." + value + "\").checked=true;", out);
            out.append("\">");
            Template.expand(extra, data, out);
            out.append("</span>");
        }
        out.append("</div>");
    }
}
