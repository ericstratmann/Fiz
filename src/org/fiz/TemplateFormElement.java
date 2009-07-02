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
     * @param out                  Generated HTML is appended here.
     */
    @Override
    public void render(ClientRequest cr, Dataset data,
            StringBuilder out) {
        Template.expand(properties.get("template"), data, out);
    }

    /**
     * This method is invoked by FormSection to generate HTML for this
     * element's label.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             main dataset).
     * @param out                  Generated HTML is appended here.
     * @return                     True is normally returned; false means
     *                             no label should be displayed and the
     *                             result of the {@code html} method should
     *                             span both the label and control areas
     *                             for this element.
     */
    @Override
    public boolean renderLabel(ClientRequest cr, Dataset data,
            StringBuilder out) {
        String span = properties.check("span");
        if ((span != null) && (span.equals("true"))) {
            return false;
        }
        return super.renderLabel(cr, data, out);
    }
}
