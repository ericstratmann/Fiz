package org.fiz;

/**
 * FormElement is the base class for controls such as text entries that
 * allow users to input data.  FormElement objects provide the basic
 * building blocks for FormSections.  The following properties are
 * supported for all FormElements (individual FormElements support
 * additional properties specific to that FormElement class):
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  For most FormElements
 *                   this is also the name for the data value in query
 *                   and update requests, and it is also used as the name
 *                   for the HTML form element.
 *   label:          (optional) Template for label to display next to the
 *                   FormElement to identify the element for the user.
 *                   If omitted {@code id} is used as the label.
 */
public abstract class FormElement implements Formatter {
    // The following variable holds the dataset describing the FormElement,
    // which was passed to the constructors as its {@code properties}
    // argument.  This dataset must contain at least an {@code id} value.
    protected Dataset properties;

    // Value of the {@code id} constructor property.
    protected String id;

    /**
     * Construct a FormElement from a set of properties that define its
     * configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the FormElement.  See the documentation
     *                             for individual FormElement subclasses for
     *                             information about the properties supported
     *                             by those classes.  See above for the
     *                             properties supported by all FormElement
     *                             objects.
     */
    public FormElement(Dataset properties) {
        this.properties = properties;
        id = properties.get("id");
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  Normally this
     * consists of reading from {@code in} the value whose name is the same
     * as this element's id and copying it to {@code out}.  This method
     * provides that behavior as a default.  However, in some situations
     * the posted data has to be translated for use in the update request
     * (e.g., perhaps a time value was split across several different controls
     * for editing but has to be returned to the data manager in a single
     * string); in this case the FormElement can override this method to
     * perform whatever translations are needed.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method will add one or
     *                             more values to that dataset, representing
     *                             the information managed by this element.
     */
    public void collect(ClientRequest cr, Dataset in, Dataset out) {
        out.set(id, in.get(id));
    }

    /**
     * Returns the identifier for this form element.
     * @return                     The {@code id} property that was specified
     *                             when the ForElement was constructed.
     */
    public String getId() {
        return id;
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for the element.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             global dataset).
     * @param out                  Generated HTML is appended here.
     */
    @Override
    public abstract void html(ClientRequest cr, Dataset data,
            StringBuilder out);

    /**
     * This method is invoked by FormSection to generate HTML to display
     * adds the label for this FormElement.  This default implementation
     * generates the label using a template provided in the {@code label}
     * property, or just outputs the id if no template was specified.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             main dataset).
     * @param out                  Generated HTML is appended here.
     */
    public void labelHtml(ClientRequest cr, Dataset data,
            StringBuilder out) {
        String template = properties.check("label");
        if (template != null) {
            Template.expand(template, data, out);
        } else {
            Html.escapeHtmlChars(id, out);
        }
    }

    /**
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code cr.registerDataRequest} for each of the
     * DataRequests needed by this form element to provide initial values
     * for data it will display.  The enclosing FormSection already
     * registers an overall request for the entire form {@code formRequest}
     * and makes that data available to each of the elements; normally that
     * is sufficient.  Hence this method provides a default, which is to do
     * nothing.  If a FormElement needs additional data it can override
     * this message to request it.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param formRequest          Name of the overall request that the
     *                             form will issue to retrieve initial values,
     *                             or null if the form is supposed to start
     *                             off empty.
     */
    public void registerRequests(ClientRequest cr, String formRequest) {
        // Do nothing.
    }

    /**
     * When erroneous form data is entered by the user, this method
     * indicates whether the erroneous data was managed by this particular
     * form element.  The caller can use this information to display an
     * error message next to the form element where the user entered the
     * bad data.
     * @param culprit              The name of a value in the form's update
     *                             request (generated by the {@code collect}
     *                             methods of all the form elements.
     * @return                     If this form element's {@code collect}
     *                             method creates a value named
     *                             {@code culprit} in its output dataset
     *                             then true is returned; otherwise false is
     *                             returned.
     */
    public boolean responsibleFor(String culprit) {
        return (culprit.equals(id));
    }
}
