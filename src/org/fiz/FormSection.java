package org.fiz;

/**
 * A FormSection displays a collection of text entries and/or other controls
 * that allow the user to input data and then submit the result.  FormSections
 * support the following constructor properties:
 *   buttonStyle:    (optional) The name of a style for the form's submit
 *                   button: the value is the name of a template in the
 *                   {@code formButtons} configuration dataset.  Defaults to
 *                   {@code standard}; use the value {@code none} if you
 *                   don't want a submit button to appear.
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML table that displays the FormSection.
 *                   Defaults to {@code FormSection}.
 *   elementErrorStyle:
 *                   (optional) When the {@code post} method encounters an
 *                   error in its data request and is displaying an error
 *                   message next to the culprit form element, this value
 *                   specifies a template in the {@code errors} dataset,
 *                   which is expanded with the terror data and the main
 *                   dataset to produce the HTML to display.  Defaults to
 *                   "formElement".
 *   errorStyle:     (optional) If an error occurs in {@code request} then
 *                   this property contains the name of a template in the
 *                   {@code errors} dataset, which is expanded with the
 *                   error data and the main dataset.  The resulting HTML
 *                   is displayed in place of the FormSection unless
 *                   {@code errorStyle} starts with "bulletin", in which
 *                   case the resulting HTML is displayed in the bulletin.
 *                   Defaults to "formSection".
 *   id:             (required) Used as the {@code id} attribute for the
 *                   HTML form element that displays the FormSection and
 *                   for various other purposes.  Must be unique among all
 *                   id's for the page.
 *   postUrl:        (optional) When the form is submitted an Ajax request
 *                   is sent to this URL; defaults to "ajaxPost".  The
 *                   caller must ensure that this URL is implemented by an
 *                   Interactor.
 *   request:        (optional) Name of the DataRequest that will supply
 *                   initial values to display in the FormSection.  If the
 *                   responds to the request contains a {@code data}
 *                   nested dataset, then the contents of that nested dataset
 *                   will be used to provide the form's initial data.  If
 *                   there is no {@code data} nested dataset then the
 *                   top-level contents of the results are used for the
 *                   form's initial data.  If this property is omitted the
 *                   form will initially be empty.
 */
public class FormSection implements Section {
    /**
     * PostError is thrown when the {@code post} method detects an error
     * in its DataRequest.
     */
    public static class PostError extends HandledError {
        /**
         * Construct a PostError with the error data returned by the failed
         * DataRequest.
         * @param errorData        Dataset describing the error.
         */
        public PostError(Dataset errorData) {
            super(errorData);
        }
    }

    /**
     * Form elements throw FormDataException in their {@code collect} methods
     * when they detect a problem with incoming form data.
     */
    public static class FormDataException extends Exception {
        /**
         * Construct a FormDataException with a given message.
         * @param message          Message describing the problem.
         */
        public FormDataException(String message) {
            super(message);
        }
    }

    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Dataset properties;
    protected FormElement[] elements;

    // Source of initial values to display in the form, or null if the form
    // is to be empty initially.  Note that individual elements of the form
    // can request additional data on their own.
    protected DataRequest dataRequest = null;

    // Style to use for the form's buttons; read from the "buttons"
    // property.
    protected String buttonStyle;

    // The {@code help} configuration dataset:
    protected Dataset helpConfig;

    // Nested dataset within {@code helpConfig} whose name matches our
    // id (contains help text specific to this form).  Null means no
    // such dataset.
    protected Dataset nestedHelp;

    /**
     * Construct a FormSection.
     * @param properties           Contains configuration information
     *                             for the form; see description above.
     * @param elements             The remaining arguments describe the
     *                             elements to appear in the form.
     */
    public FormSection(Dataset properties, FormElement ... elements) {
        this.properties = properties;
        this.elements = elements;
        buttonStyle = properties.check("buttonStyle");
        if (buttonStyle == null) {
            buttonStyle = "standard";
        }
        helpConfig = Config.getDataset("help");
        nestedHelp = (Dataset) helpConfig.lookup(properties.get("id"),
                Dataset.DesiredType.DATASET, Dataset.Quantity.FIRST_ONLY);
    }

    /**
     * Invoke the {@code collect} methods on each of the FormElements in this
     * form to collect and translate the form's posted data.  This method
     * assumes that submitted data from the form is available in the main
     * dataset for {@code cr}.
     * @param cr                   Overall information about the client
     *                             request being serviced.  Used for
     *                             handling validation errors.
     * @return                     A Dataset containing the submitted data
     *                             from the form, potentially translated
     *                             by the {@code collect} methods.
     * @throws PostError           Thrown if any of the {@code collect}
     *                             methods generated an error.  In this case
     *                             AJAX actions will have been generated to
     *                             display information about the error.
     */
    public Dataset collectFormData(ClientRequest cr) {
        // Give each of the form elements a chance to collect and transform
        // the data for which it is responsible.
        Dataset postData = new Dataset();
        Dataset main = cr.getMainDataset();
        Dataset errorData = null;
        for (FormElement element : elements) {
            try {
                element.collect(cr, main, postData);
            }
            catch (FormDataException e) {
                // The data for this form element was invalid; generate
                // AJAX actions to display an error message.
                String id = element.getId();
                errorData = new Dataset("message", e.getMessage(),
                        "culprit", id);
                elementError(cr, errorData, id);
            }
        }

        // If there were any errors generated by the form elements,
        // throw an error containing information about the last of them.
        if (errorData != null) {
            throw new PostError(errorData);
        }
        return postData;
    }

    /**
     * When an error is detected in form data and the problem can be traced
     * to a particular form element, this method is invoked to generate
     * AJAX actions to display information about the error next to the form
     * element.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param errorData            Dataset containing information about the
     *                             error.
     * @param id                   Identifier for the form element
     *                             responsible for the error; typically the
     *                             same as the {@code culprit} value in
     *                             {@code errorData}.
     */
    public void elementError(ClientRequest cr, Dataset errorData, String id) {
        // Generate  HTML for the error message.
        StringBuilder html = new StringBuilder(100);
        String templateName = properties.check(
                "elementErrorStyle");
        if (templateName == null) {
            templateName = "formElement";
        }
        Template.expand(Config.get("errors", templateName),
                new CompoundDataset(errorData, cr.getMainDataset()), html);

        // Invoke a Javascript method, passing it information about
        // the form element plus the HTML.
        cr.ajaxEvalAction("form_@formId.elementError(" +
                "\"@elementId\", \"@html\");",
                new Dataset("formId", properties.get("id"),
                "elementId", id, "html", html.toString()));
        cr.setBulletinError(new Dataset("message",
                "Some of the input fields are invalid; " +
                "see details below."));
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
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();
        Dataset mainDataset = cr.getMainDataset();

        // Create a dataset that provides access to the initial values
        // for the form elements.
        Dataset data;
        if (dataRequest != null) {
            Dataset response = dataRequest.getResponseData();
            if (response == null) {
                // An error occurred in the request.
                Template.expand("\n<!-- Start FormSection @id -->\n",
                        properties, out);
                cr.showErrorInfo(properties.check("errorStyle"),
                        "formSection", dataRequest.getErrorData());
                Template.expand("\n<!-- End FormSection @id -->\n",
                        properties, out);
                return;
            }
            if (response.containsKey("data")) {
                response = response.getChild("data");
            }
            data = new CompoundDataset(response, mainDataset);
        } else {
            data = cr.getMainDataset();
        }

        if (!properties.containsKey("class")) {
            html.includeCssFile("FormSection.css");
        }
        Template.expand("\n<!-- Start FormSection @id -->\n" +
                "<form id=\"@id\" class=\"@class?{FormSection}\" " +
                "action=\"javascript: form_@id.post();\" method=\"post\">\n" +
                "  <table cellspacing=\"0\">\n",
                properties, out);

        // Each iteration of the following loop renders one row of the
        // table, displaying one FormElement.
        Dataset diagnosticInfo = new Dataset();
        for (FormElement element : elements) {
            Template.expand("    <tr {{title=\"@1\"}}>\n", out,
                    getHelpText(element));
            int startingLength = out.length();
            out.append("      <td class=\"label\">");
            if (!element.labelHtml(cr, data, out)) {
                // This element requests that we not display any label and
                // instead let the control span both columns.  Erase the
                // information for this row and regenerate the row with
                // a single column.
                out.setLength(startingLength);
                out.append("      <td class=\"control\" " +
                        "colspan=\"2\">");
            } else {
                out.append("</td>\n      <td class=\"control\">");
            }
            element.html(cr, data, out);

            // Create an extra <div> underneath the control.  Initially
            // this is empty and invisible, but it may be used later to
            // display an error message pertaining to this form element.
            diagnosticInfo.set("id", element.getId());
            Template.expand("<div id=\"@id.diagnostic\" " +
                    "class=\"diagnostic\" style=\"display:none\">" +
                    "</div></td>\n" +
                    "    </tr>\n", diagnosticInfo, out);
        }

        // Add the submit button if desired, and finish up the form.
        String buttonTemplate = Config.getDataset("formButtons").get(
                buttonStyle);
        if (buttonTemplate.length() > 0) {
            out.append("    <tr>\n      <td class=\"submit\" " +
                    "colspan=\"2\">");
            Template.expand(buttonTemplate, new CompoundDataset(
                    properties, mainDataset), out);
            out.append("</td>\n    </tr>\n");
        }
        Template.expand("  </table>\n</form>\n" +
                "<!-- End FormSection @id -->\n",
                properties, out);

        // Generate a Javascript object containing information about the form.
        html.includeJavascript("window.form_@id = new Fiz.FormSection(" +
                "\"@id\", \"@postUrl?{ajaxPost}\");\n", properties);
        html.includeJsFile("FormSection.js");
    }

    /**
     * Interactors invoke this method when a form is posted.  This
     * method invokes {@code requestName}, including in the request the data
     * from the form, and handles errors that occur along the way (e.g.,
     * if there is an error related to data in the form then an Ajax
     * response will be generated to display an error message in the form).
     * @param cr                   Overall information about the client
     *                             request being serviced; must be an
     *                             Ajax request.
     * @param requestName          Name of the DataRequest to invoke
     *                             using the form's data; must be defined
     *                             in the {@code dataRequests} configuration
     *                             dataset.
     * @return                     The dataset containing the result from
     *                             {@code requestName}.
     * @throws PostError           Thrown if {@code requestName} returns
     *                             an error or if formed elements found some
     *                             of the submitted data to be invalid.  This
     *                             method will handle the error by scheduling
     *                             various Ajax responses to display error
     *                             information in the form;  the error is
     *                             thrown to abort any remaining processing
     *                             of the request.
     */
    public Dataset post(ClientRequest cr, String requestName)
            throws PostError {
        // Collect the form's data, issue a request to the data manager,
        // and wait for it to complete.
        DataRequest request = new DataRequest(requestName,
                cr.getMainDataset());
        request.getRequestData().createChild("data", collectFormData(cr));
        Dataset responseData = request.getResponseData();
        if (responseData != null) {
            return responseData;
        }

        // An error occurred while processing the request.  If the error
        // can be attributed to a particular form element, display an error
        // message next to the culprit.  Otherwise display the error message
        // in the bulletin.
        Dataset errorData = request.getErrorData();
        String culprit = errorData.check("culprit");
        StringBuilder html = new StringBuilder(100);
        boolean foundCulprit = false;
        if (culprit != null) {
            for (FormElement element : elements) {
                if (element.responsibleFor(culprit)) {
                    elementError(cr, errorData, element.getId());
                    foundCulprit = true;
                    break;
                }
            }
        }
        if (!foundCulprit) {
            cr.setBulletinError(errorData);

            // If there are any element-specific messages currently displayed
            // because of a previous post, undisplay them.
            cr.ajaxEvalAction("form_@formId.clearElementError();",
                    new Dataset("formId", properties.get("id")));
        }
        throw new PostError(errorData);
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
        String query = properties.check("request");
        if (query != null) {
            dataRequest = cr.registerDataRequest(query);
        }
        for (FormElement element : elements) {
            element.registerRequests(cr, query);
        }
    }

    /**
     * Try to find help text to display for a form element, checking in
     * three places: first, a {@code help} property on the form element;
     * second, a property {@code x.y} in the help configuration dataset,
     * where {@code x} is the {@code id} property of the FormSection and
     * {@code y} is the {@code id} property of the FormElement; and third,
     * a property in the help configuration dataset was named is the
     * value of the form element's {@code id} property.
     * @param element              A FormElement in this form.
     * @return                     The help text for the form element,
     *                             or null if none could be found.
     */
    protected String getHelpText(FormElement element) {
        String result = element.checkProperty("help");
        if (result != null) {
            return result;
        }
        String id = element.getId();
        if (nestedHelp != null) {
            result = nestedHelp.check(id);
            if (result != null) {
                return result;
            }
        }
        return helpConfig.check(id);
    }
}
