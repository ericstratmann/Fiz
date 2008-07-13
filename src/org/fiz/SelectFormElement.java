package org.fiz;

import java.util.*;

/**
 * The SelectFormElement class allows users to select one of more values
 * from a predefined menu of choices.  This class supports several
 * variations on that general theme:
 *   * The choices can be defined either statically in the properties
 *     of the form element or dynamically as the result of a data request.
 *     The {@code choiceRequest} property selects between these two options.
 *     In either case, the choices are defined by a collection of nested
 *     datasets, each containing {@code name} and {@code value} values.
 *     The {@code name} value specifies what to display in the menu, and
 *     the {@code value} value is the internal form used in incoming and
 *     outgoing data requests.  If {@code name} is omitted then
 *     {@code value} is displayed in the menu.
 *   * The choices can be displayed either with a drop-down menu or with
 *     a scrolling list; the {@code height} option controls this aspect.
 *   * The form element can permit either a single selection or multiple
 *     selections; the {@code multiple} option controls this.
 *  The SelectFormElement class supports the following properties:
 *   choice:         (optional) Default location for the choice datasets, if
 *                   neither the {@code choiceRequest} nor the
 *                   {@code choiceName} property is specified.
 *   choiceRequest:  (optional) If this property is specified, then it is the
 *                   name of a data request whose result will contain the
 *                   choices.  If this property is omitted then the choices
 *                   must be present in the properties.
 *   choiceName:     (optional) The name of the nested datasets (either in
 *                   the properties or in the result of the data request
 *                   specified by {@code choiceRequest}) containing the
 *                   choices; defaults to {@code choice}.
 *   class:          (optional) Class attribute to use for the {@code div}
 *                   containing the form element; defaults to
 *                   "SelectFormElement".
 *   height:         (optional) If this property is specified with a value
 *                   greater than 1, then the form element will be displayed
 *                   as a scrollable list with this many elements visible at
 *                   once; otherwise the form element will be displayed as
 *                   a drop-down menu.
 *   help:           (optional) Help text for this form element.  If this
 *                   property is omitted the form will look for help text
 *                   in the {@code help} configuration dataset.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  This is used as the name
 *                   for the data value in query and update requests and
 *                   also as the {@code name} attribute for the HTML form
 *                   element.
 *   multiple:       (optional) If specified, this property must have the
 *                   value {@code multiple}, which means that multiple
 *                   selections will be permitted.  In this case, the
 *                   value of this form element is specified in data requests
 *                   with one nested dataset for each specified value;
 *                   each dataset will have a name given by the {@code id}
 *                   property, and it will contain a single element named
 *                   {@code value}, which holds that value.
 *   label:          (optional) Template for label to display next to the
 *                   FormElement to identify the element for the user.
 */

public class SelectFormElement extends FormElement {
    // If the {@code choiceRequest} property is specified for this form
    // element, then the following variable provides a reference to the
    // corresponding DataRequest.
    DataRequest choiceRequest = null;

    // Value of the {@code multiple} property.
    String multiple;

    /**
     * Construct an SelectFormElement from a set of properties that define its
     * configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public SelectFormElement(Dataset properties) {
        super(properties);
        multiple = this.properties.check("multiple");
        if ((multiple != null) && !multiple.equals("multiple")) {
            throw new InternalError("\"multiple\" property for " +
                    "SelectFormElement has illegal value \"" + multiple +
                    "\"");
        }
    }

    /**
     * Invoked by FormSection when a form has been posted: prepares data
     * for inclusion in the update request for the form.  .
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
        if (multiple == null) {
            out.set(id, in.get(id));
        } else {
            // Must return multiple values:
            for (Dataset d : in.getChildren(id)) {
                out.addChild(id, d);
            }
        }
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
    public void html(ClientRequest cr, Dataset data,
            StringBuilder out) {
        // Create a HashSet that keeps track of the initially selected values.
        HashSet<String> initialSelections = new HashSet<String>();
        if (multiple == null) {
            String initialValue = data.check(id);
            if (initialValue != null) {
                initialSelections.add(initialValue);
            }
        } else {
            ArrayList<Dataset> children = data.getChildren(id);
            for (Dataset d : children) {
                initialSelections.add(d.get("value"));
            }
        }

        String choiceName = properties.check("choiceName");
        ArrayList<Dataset> choices;
        if (choiceName == null) {
            choiceName = "choice";
        }
        if (choiceRequest != null) {
            Dataset responseData = choiceRequest.getResponseData();
            if (responseData == null) {
                // The request to get the choices failed; display an error
                // message and use an empty list of choices.
                cr.addErrorsToBulletin(choiceRequest.getErrorData());
                choices = new ArrayList<Dataset>();
            } else {
                choices = responseData.getChildren(choiceName);
            }
        } else {
            choices = properties.getChildren(choiceName);
        }
        cr.getHtml().includeCssFile("SelectFormElement.css");
        Template.expand("\n<!-- Start SelectFormElement @id -->\n" +
                "<select name=\"@id\" " +
                "class=\"@class?{SelectFormElement}\" " +
                "{{size=\"@height\"}} {{multiple=\"@multiple\"}}>\n",
                properties, out);
        for (Dataset choice: choices) {
            String selected = null;
            if (initialSelections.contains(choice.get("value"))) {
                selected = "selected";
            }
            Template.expand("  <option {{selected=\"@1\"}} " +
                    "value=\"@value\">@name?{@value}</option>\n",
                    choice, out, selected);
        }
        out.append("</select>\n<!-- End SelectFormElement @id -->\n");
    }

    /**
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code cr.registerDataRequest} for the request given by
     * the {@code choiceRequest} property, if that property has been
     * specified..
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param formRequest          Not used.
     */
    @Override
    public void registerRequests(ClientRequest cr, String formRequest) {
        String query = properties.check("choiceRequest");
        if (query != null) {
            choiceRequest = cr.registerDataRequest(query);
        }
    }
}
