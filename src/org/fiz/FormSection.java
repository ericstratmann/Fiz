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
 *                   initial values to display in the FormSection.  If
 *                   this property is omitted the form will initially
 *                   be empty.
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
            // TODO: seems weird to indirect through "record" to get data.
            if (response.containsKey("record")) {
                response = response.getChild("record");
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
            out.append("    <tr>\n      <td class=\"label\">");
            element.labelHtml(cr, data, out);
            out.append("</td>\n      <td class=\"control\">");
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

        // Close off the table, add the submit button if desired, and
        // finish up the form.
        out.append("  </table>\n");
        String buttonTemplate = Config.getDataset("formButtons").get(
                buttonStyle);
        if (buttonTemplate.length() > 0) {
            Template.expand(buttonTemplate, new CompoundDataset(
                    properties, mainDataset), out);
            out.append("\n");
        }
        Template.expand("</form>\n" +
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
     *                             an error.  This method will handle the
     *                             error by scheduling various Ajax responses
     *                             to display error information in the form;
     *                             the error is thrown to abort any
     *                             remaining processing of the request.
     */
    public Dataset post(ClientRequest cr, String requestName)
            throws PostError {
        Dataset main = cr.getMainDataset();
        DataRequest request = new DataRequest(requestName,  main);

        // Give each of the form elements a chance to collect and transform
        // the data for which it is responsible.  Include this information
        // in the request.
        Dataset postData = request.getRequestData().createChild("data");
        for (FormElement element : elements) {
            element.collect(cr, main, postData);
        }

        // Issue the request and wait for it to complete.
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

                    // Generate the HTML.
                    String templateName = properties.check(
                            "elementErrorStyle");
                    if (templateName == null) {
                        templateName = "formElement";
                    }
                    Template.expand(Config.get("errors", templateName),
                            new CompoundDataset(errorData, main), html);

                    // Invoke a Javascript method, passing it information about
                    // the form element plus the HTML.
                    cr.ajaxEvalAction("form_@formId.elementError(" +
                            "\"@elementId\", \"@html\");",
                            new Dataset("formId", properties.get("id"),
                            "elementId", element.getId(),
                            "html", html.toString()));
                    cr.setBulletinError(new Dataset("message",
                            "Some of the input fields are invalid; " +
                            "see details below"));
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
}
