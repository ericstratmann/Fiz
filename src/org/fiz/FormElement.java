package org.fiz;

/**
 * FormElement is the base class for controls such as text entries that
 * allow input values to be specified by the user.  FormElement objects
 * provide the basic building blocks for FormSections.  The following
 * properties are supported for all FormElements (individual FormElements
 * also have their own specific properties that they support):
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  Also gives the name
 */
public abstract class FormElement {
    // The following variable holds the dataset describing the FormElement,
    // which was passed to the constructors as its {@code properties}
    // argument.  This dataset must contain at least an {@code id} value.
    protected Dataset properties;

    // Value of the {@code id} constructor property.
    protected String id;

    /**
     * Construct a FormElement from a set of properties that define the
     * configuration of the FormElement.
     * @param properties           Dataset whose values are used to configure
     *                             the FormElement.  See the documentation
     *                             for individual FormElement subclasses for
     *                             information about the properties supported
     *                             by that class.  See above for the
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
     * string); this case the ForElement can override this method to perform
     * whatever translations are needed.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method should add one or
     *                             more values to that dataset, representing
     *                             the information managed by this element.
     */
    public void collect(ClientRequest cr, Dataset in, Dataset out) {
        out.set(id, in.get(id));
    }

    public String getId() {
        return id;
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this element and appends it to the Html
     * object associated with {@code cr}.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML will be
     *                             appended to {@code cr.getHtml()}.
     * @param data                 Data returned by the data request for
     *                             the overall form, or an empty dataset
     *                             if there is no overall data (the form
     *                             is to be empty initially).
     */
    public abstract void html(ClientRequest cr, Dataset data);

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
}
