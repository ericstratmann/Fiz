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

import java.io.Serializable;
import java.util.*;

import org.fiz.FormSection.FormDataException;

/**
 * FormElement is the base class for controls such as text entries that
 * allow users to input data.  FormElement objects provide the basic
 * building blocks for FormSections.  The following properties are
 * supported for all FormElements (individual FormElements support
 * additional properties specific to that FormElement class):
 *   class:          (optional) Class attribute to use for the {@code input}
 *                   element; defaults to the class name of the FormElement.
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
 *   required:       (optional) If set to true, will require that a non-empty
 *                   value will be returned by the form element
 *   validator:      (optional) One or more nested datasets, each describing one
 *                   validation to perform on this form element. All validator
 *                   datasets must include a {@code type} attribute. Other valid
 *                   properties depend on the validator being used. If a single
 *                   word is specified for the type, Fiz will look inside
 *                   {@link FormValidator} for a validation method of the form
 *                   validateType. Otherwise, Fiz will use the type as the
 *                   fully qualified class name and method for the validator.
 *                   Multiple nested data sets may be used to attach multiple
 *                   validators to a form element. See {@link FormValidator} for
 *                   additional usage and implementation information, including
 *                   the method prototype for implementing custom validators.
 */
public abstract class FormElement implements Formatter, DirectAjax {    
    // An object of the following class is stored as a page property if this
    // form element contains validation: it holds data that we will need later
    // on to process Ajax requests to validate the value of the form element
    protected static class ValidatorData implements Serializable {
        // The following variables are just copies of configuration properties
        // for the parent form section, or null if no such property.
        protected String parentId;
        protected String elementErrorStyle;

        // The following give information about the form element and its 
        // collection of validators
        protected String id;
        protected List<Dataset> validators;
        
        // Constructs a new object for holding various pieces of information
        // needed for a form to validate itself. It also provides information to
        // render error messages if any errors occur.
        public ValidatorData(String id) {
            this.id = id;
            this.validators = new ArrayList<Dataset>();
        }
    }

    // Value of the {@code id} constructor property.
    protected String id;
        
    // The following variable holds the dataset describing the FormElement,
    // which was passed to the constructors as its {@code properties}
    // argument.  This dataset must contain at least an {@code id} value.
    protected Dataset properties;

    // Contains information about the validators used on this form element
    protected ValidatorData validatorData = null;

    // Contains a reference to the FormSection that contains this form element
    protected FormSection parentForm = null;
    
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
        
        // Sets up the "required" validator
        if ("true".equals(properties.check("required"))) {
            addValidator(new Dataset("type", "required"));
        }

        for (Dataset validator : properties.getChildren("validator")) {
            addValidator(validator);
        }
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
        // By default, do nothing
    }

    /**
     * This method adds a validator to the form element.
     * @param validator            Dataset containing information about the
     *                             validator
     */
    public void addValidator(Dataset validator) {
        // Package the information needed to validate a form element up
        // a page property
        if (validatorData == null) {
            validatorData = new ValidatorData(id);
        }
        validatorData.validators.add(validator);
    }

    /**
     * This method is an Ajax entry point, invoked to validate a form element.
     * @param cr                    Overall information about the client
     *                              request being serviced; there must be an
     *                              {@code elementsToValidate} value, which is
     *                              a comma-separated list of form elements
     *                              needing to be validated, in the main dataset 
     *                              along with a {@code formData} object which
     *                              contains key-value pairs for all relevant
     *                              form elements.
     */
    public static void ajaxValidate(ClientRequest cr) {
        Dataset main = cr.getMainDataset();

        String[] elementsToValidate = 
            StringUtil.split(main.get("elementsToValidate"), ',');
        Dataset formData = main.getChild("formData");

        // We validate each of the form elements listed in
        // {@code elementsToValidate}
        for (String elementId : elementsToValidate) {
            ValidatorData validatorData = 
                (ValidatorData) cr.getPageProperty(elementId + "_validation");
              
            try {
                validate(validatorData, formData);
                
                // No errors, clear the error messages on the form element
                // (if any)
                cr.evalJavascript(
                        "Fiz.ids.@1.clearElementError(\"@(1)_@2\");\n",
                        validatorData.parentId, elementId);
            } catch (FormSection.FormDataException e) {
                FormSection.elementError(cr, validatorData.parentId,
                        elementId, validatorData.elementErrorStyle,
                        e.getMessages());
            }
        }
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
     * @return                     The {@code id} property used to uniquely
     *                             identify the FormElement on the page.
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
     * This method generates the JavaScript on the page needed to perform
     * AJAX-based validation on the FormElement. This method is automatically
     * invoked by the FormSection after {@code render} is called in the final
     * steps of preparing the page for output.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void renderValidators(ClientRequest cr) {
        if (validatorData == null) {
            return;
        }
        
        // Enable the auth token for handling Ajax requests
        cr.setAuthToken();
        
        // Validation of this form element may require data from other form
        // elements so we need to create a list of other form elements to 
        // include when sending back data to be validated.
        Set<String> otherIds = new TreeSet<String>();
        otherIds.add(id);
        for (Dataset validator : validatorData.validators) {
            String otherFields = validator.check("otherFields");
            if (otherFields == null) {
                continue;
            }
            String[] ids = StringUtil.split(otherFields, ',');
            for (String includeId : ids) {
                otherIds.add(includeId);
    
                // Attach a validate callback to the included form element so
                // that any changes in the included form element will also fire
                // off a validation event on this form element
                cr.evalJavascript("Fiz.FormElement" +
                        ".attachValidator(\"@1\", \"@2\", \"@3\");\n",
                        includeId, id, otherFields);
            }
        }
    
        // Finally, attach the validate callback to the current form element
        cr.evalJavascript("Fiz.FormElement." +
                "attachValidator(\"@1\", \"@1\", \"@2\");\n",
                id, StringUtil.join(otherIds, ","));
        cr.setPageProperty(id + "_validation", validatorData);
        cr.getHtml().includeJsFile("static/fiz/FormElement.js");
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

    /**
     * Changes the {@code id} of the FormElement.
     * @param id                   The {@code id} property used to uniquely
     *                             identify the FormElement on the page.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * This method associates a FormElement with a specific FormSection. This
     * parent-child relationship allows functions like validate to make changes
     * (like displaying error messages) to the parent section. This method is
     * automatically invoked by a FormSection on each of its FormElements.
     * @param parent              {@code FormSection} containing this form
     *                            element
     */
    public void setParentForm(FormSection parent) {
        this.parentForm = parent;
        if (validatorData != null) {
            validatorData.parentId = parent.checkId();
            validatorData.elementErrorStyle = parent.checkElementErrorStyle();
        }
    }

    /**
     * This method validates the current value contained in the FormElement when
     * the validator is invoked. This method is automatically invoked by
     * the FormSection before the {@code collect} call in the final steps of
     * preparing the returned form data.
     * @param in                   Dataset containing the relevant form elements
     *                             and their corresponding values
     * @throws FormDataException   Generated if validation fails and contains
     *                             the error messages for ALL failed validators
     */
    public void validate(Dataset in) throws FormDataException {
        if (validatorData != null) {
            FormElement.validate(validatorData, in);
        }
    }

    /**
     * This function validates the form element identified by
     * {@code validatorData.id}. This function is used by both the AJAX-based
     * and submit-based validation mechanisms.
     * @param validatorData        Contains information about the validators,
     *                             stylings, and form being validated
     * @param in                   Dataset containing the relevant form elements
     *                             and their corresponding values
     * @throws FormDataException   Generated if validation fails and contains
     *                             the error messages for ALL failed validators
     */
    public static void validate(ValidatorData validatorData,
            Dataset in) throws FormDataException {
        FormSection.FormDataException e = null;
        for (Dataset validator : validatorData.validators) {
            String method = validator.get("type");
            
            // If there is no period in the validator type, we assume the
            // developer is specifying a built-in validator type 
            if (method.indexOf('.') == -1) {
                method = "FormValidator.validate" 
                    + StringUtil.ucFirst(method);
            }
                        
            // Gets either the error message if the validation fails or null
            // representing the validation succeeded
            String result = (String) Util.invokeStaticMethod(
                    method, validatorData.id, validator, in);
    
            if (result != null) {
                if (e == null) {
                    e = new FormSection.FormDataException(
                            validatorData.id, result);
                } else {
                    e.addMessage(validatorData.id, result);
                }
            }
        }
        
        if (e != null) {
            throw e;
        }
    }
}
