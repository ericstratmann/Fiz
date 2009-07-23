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
 * The CompoundFormElement class creates a form element consisting of
 * any number of other form elements.  Furthermore, the component form
 * elements are composed using a template that can provide additional
 * HTML to arrange and decorate the form elements.
 *   class:          (optional) Class attribute to use for the <div>
 *                   containing this element; defaults to CompoundFormElement.
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  Used for managing errors
 *                   related to this form element.
 *   label:          (optional) Template for label to display next to the
 *                   checkbox to identify the element for the user.
 *   request:        (optional) Name of a DataRequest, which must have been
 *                   created by the caller and registered in the ClientRequest
 *                   by calling ClientRequest.addDataRequest.  The results of
 *                   the request will be combined with the data provided by
 *                   the form and used both for expanding {@code template}
 *                   and for passing to the component elements when they are
 *                   generating HTML.
 *   template:       (optional) HTML template that arranges and decorates the
 *                   component form elements.  Substitutions such as
 *                   {@code @3} cause the HTML for a component form
 *                   element to be substituted at that point ({@code @1}
 *                   refers to the first component).  Non-numerical
 *                   substitutions refer to form data, including the results
 *                   of {@code request}.  If this property is omitted then
 *                   the component elements are rendered in order (equivalent
 *                   to a template of {@code @1@2@3...}).
 */
public class CompoundFormElement extends FormElement {
    // The component FormElements that make up this element:
    protected FormElement[] components;

    /**
     * Construct a CompoundFormElement from a set of properties that define
     * its configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     * @param components           Any number of FormElements, which make
     *                             up this element.
     */
    public CompoundFormElement(Dataset properties,
            FormElement ... components) {
        super(properties);
        this.components = components;
    }

    /**
     * This method is invoked during the first phase of rendering a page,
     * in case the FormElement needs to create custom requests of its own
     * (as opposed to requests already provided for it by the Interactor).
     * If so, this method creates the requests and passes them to
     * {@code cr.addDataRequest}.  This method just invokes the corresponding
     * method in each of its components.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param empty                True means no request was provided to
     *                             the enclosing form, which means that the
     *                             form should start off empty.
     */
    public void addDataRequests(ClientRequest cr, boolean empty) {
        for (FormElement component : components) {
            component.addDataRequests(cr, empty);
        }
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  This method
     * makes recursive calls to the {@code collect} methods of all of
     * the component elements.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param in                   The main dataset for the request;
     *                             contains at least the form's post data
     *                             and the query values from the request URL.
     * @param out                  Dataset that will be sent to the data
     *                             manager; this method will add one or
     *                             more values to that dataset, representing
     *                             all the information managed by this
     *                             element and its components.
     * @throws FormSection.FormDataException
     *                             Thrown if a form element finds the
     *                             submitted form data to be invalid.
     */
    @Override
    public void collect(ClientRequest cr, Dataset in, Dataset out)
            throws FormSection.FormDataException {
        for (FormElement component : components) {
            component.collect(cr, in, out);
        }
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for the element, including all its components.
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
        // If we have our own data request, combine its output with the
        // incoming data from the form.
        String requestName = properties.check("request");
        if (requestName != null) {
            DataRequest request = cr.getDataRequest(requestName);
            Dataset responseData = request.getResponseData();
            if (responseData == null) {
                // The request to get the choices failed; display an error
                // message and carry on without the additional data.
                cr.addErrorsToBulletin(request.getErrorData());
            } else {
                data = new CompoundDataset(responseData, data);
            }
        }

        // Handle the case where there is no template.
        String template = properties.check("template");
        if (template == null) {
            for (FormElement component : components) {
                component.render(cr, data, out);
            }
            return;
        }

        // There exists a template.  First, generate HTML for each of the
        // component FormElements.  Then use the template to combine those
        // results.
        Object[] componentHtml = new Object[components.length];
        int outLength = out.length();
        for (int i = 0; i <components.length; i++) {
            // Use {@code out} to generate the HTML for each component;
            // once the HTML has been saved in a separate string, truncate
            // {@code out} back to its original length again.
            components[i].render(cr, data, out);
            componentHtml[i] = out.substring(outLength);
            out.setLength(outLength);
        }
        // When expanding the template, don't do any special character
        // processing on the component HTML: those strings are already
        // completely valid HTML (don't want to quote <> etc.).
        // However, information coming from {@code data} must still be
        // quoted.
        Template.ParseInfo info = new Template.ParseInfo(template, out,
                Template.SpecialChars.HTML, data, componentHtml);
        info.indexedQuoting = Template.SpecialChars.NONE;
        Template.expandRange(info, 0, template.length());
    }

    /**
     * When erroneous form data is entered by the user, this method
     * indicates whether the erroneous data is managed by this particular
     * form element or any of its components.
     * @param culprit              The name of a value in the form's update
     *                             request (generated by the {@code collect}
     *                             methods of all the form elements.
     * @return                     If this form element's {@code collect}
     *                             method creates a value named
     *                             {@code culprit} in its output dataset
     *                             then true is returned; otherwise false is
     *                             returned.
     */
    @Override
    public boolean responsibleFor(String culprit) {
        for (FormElement component : components) {
            if (component.responsibleFor(culprit)) {
                return true;
            }
        }
        return false;
    }
}
