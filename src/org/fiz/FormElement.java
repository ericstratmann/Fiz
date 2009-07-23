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
 * FormElement is the base class for controls such as text entries that
 * allow users to input data.  FormElement objects provide the basic
 * building blocks for FormSections.  The following properties are
 * supported for all FormElements (individual FormElements support
 * additional properties specific to that FormElement class):
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  For most FormElements
 *                   this is also the name for the data value in query
 *                   and update requests, and it is also used as the name
 *                   for the HTML form element.
 *   label:          (optional) Template for label to display next to the
 *                   FormElement to identify the element for the user.
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
     * This method is invoked during the first phase of rendering a page,
     * in case the FormElement needs to create custom requests of its own
     * (as opposed to requests already provided for it by the Interactor).
     * If so, this method creates the requests and passes them to
     * {@code cr.addDataRequest}.  This method provides a default
     * implementation that does nothing, which is appropriate for most
     * FormElements.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param empty                True means no request was provided to
     *                             the enclosing form, which means that the
     *                             form should start off empty.
     */
    public void addDataRequests(ClientRequest cr, boolean empty) {
        // Do nothing.
    }

    /**
     * Return the form element property given by {@code name} if it
     * exists; otherwise return null.
     * @param name                 Name of the desired property.
     * @return                     The property given by {@code name},
     *                             or null if it doesn't exist.
     */
    public String checkProperty(String name) {
        return properties.check(name);
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  Normally this
     * consists of checking for a value in {@code in} whose name is the same
     * as this element's id and copying it to {@code out} if it exists.
     * This method provides that behavior as a default.  However, in some
     * situations the posted data has to be translated for use in the update
     * request (e.g., perhaps a time value was split across several different
     * controls for editing but has to be returned to the data manager in a
     * single string); in this case the FormElement can override this method
     * to perform whatever translations are needed.  FormElements can also
     * use this method to perform data validation (though that usually happens
     * in the data managers).
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method will add one or
     *                             more values to that dataset, representing
     *                             the information managed by this element.
     * @throws FormSection.FormDataException
     *                             Thrown if the form element finds the
     *                             submitted form data to be invalid.
     */
    public void collect(ClientRequest cr, Dataset in, Dataset out)
            throws FormSection.FormDataException {
        String value = in.check(id);
        if (value != null) {
            out.set(id, value);
        }
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
    public abstract void render(ClientRequest cr, Dataset data,
            StringBuilder out);

    /**
     * This method is invoked by FormSection to generate HTML to display
     * the label for this FormElement.  This default implementation
     * generates the label using a template provided in the {@code label}
     * property.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             main dataset).
     * @param out                  Generated HTML is appended here.
     * @return                     True is always returned (false means
     *                             no label should be displayed and the
     *                             result of the {@code html} method should
     *                             span both the label and control areas
     *                             for this element).
     */
    public boolean renderLabel(ClientRequest cr, Dataset data,
            StringBuilder out) {
        String template = properties.check("label");
        if (template != null) {
            Template.appendHtml(out, template, data);
        }
        return true;
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
