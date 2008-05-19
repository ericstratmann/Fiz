package org.fiz;

/**
 * A FormSection displays a collection of text entries and/or other controls
 * that allow the user to specify a collection of related values and then
 * submit the result.  FormSections support the following constructor
 * properties:
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML table that displays the FormSection.
 *                   Defaults to {@code FormSection}.
 *   id:             (optional) Used as the {@code id} attribute for the
 *                   HTML table that displays the FormSection.
 *   updateRequest:  (required) Name of a DataRequest to invoke when the
 *                   form is posted.
 *   queryRequest:   (optional) Name of the DataRequest that will supply
 *                   initial values to display in the FormSection.  If
 *                   this property is omitted and the form will initially
 *                   be empty.
 */
public class FormSection implements Section {
    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Dataset properties;
    protected FormElement[] elements;

    // Overall source of initial values to display in the form, or null
    // if the form is to be empty initially.  Note that individual elements
    // of the form can also request data on their own.
    protected DataRequest dataRequest = null;

    /**
     * Construct a FormSection.
     * @param properties           Contains configuration information
     *                             for the table; see description above.
     * @param elements             The remaining arguments describe the
     *                             elements to appear in the form.
     */
    public FormSection(Dataset properties, FormElement ... elements) {
        this.properties = properties;
        this.elements = elements;
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and appends it to the Html
     * object associated with {@code cr}.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML will be
     *                             appended to {@code cr.getHtml()}.
     */
    @Override
    public void html(ClientRequest cr) {

    }

    /**
     * Interactors invoke this method when a form is posted.  This
     * method processes the incoming post data, invokes the appropriate
     * update requests, and handles any errors that occur along the way.
     * @param cr                   Overall information about the client
     *                             request being serviced; must be an
     *                             Ajax request.
     */
    public void post(ClientRequest cr) {

    }

    /**
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code cr.registerDataRequest} for each of the
     * DataRequests needed by this section to gather data to be displayed.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    @Override
    public void registerRequests(ClientRequest cr) {
        String query = properties.check("queryRequest");
        if (query != null) {
            dataRequest = cr.registerDataRequest(
                properties.get("request"));
        }
        for (FormElement element : elements) {
            element.registerRequests(cr, query);
        }
    }
}
