package org.fiz;

/**
 * This interactor implements a simple Fiz application used as an
 * example on the Fiz Wiki.
 */
public class Demo3Interactor extends Interactor {
    FormSection form = new FormSection(
        new Dataset("postUrl", "post"),
        new EntryFormElement(new Dataset("id", "name",
                "label", "Name:")),
        new EntryFormElement(new Dataset("id", "street1",
                "label", "Address:")),
        new EntryFormElement(new Dataset("id", "street2",
                "label", "")),
        new EntryFormElement(new Dataset("id", "city",
                "label", "City:")),
        new SelectFormElement(new Dataset("id", "state",
                "label", "State:", "choiceRequest", "getStates")),
        new EntryFormElement(new Dataset("id", "zip",
                "label", "Zip code:")));

    /**
     * Displays a page containing a simple form.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void main(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Simple Fiz Form");
        cr.addDataRequest("getStates", new FileDataManager().newReadRequest(
                "demo/states.yaml", "states"));
        Section sections[] = {
            new TemplateSection("<h1>Enter your data below</h1>\n"),
            form,
            new TemplateSection("<div id=postedInfo></div>\n")
        };
        cr.showSections(sections);
    }

    /**
     * This method is invoked when the form is posted; it simply prints
     * the form contents.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void post(ClientRequest cr) {
        Dataset data = form.collectFormData(cr);
        if (validate(cr, data)) {
            cr.updateElement("postedInfo", Template.expand(
                    "<p>Posted form data: @name, @street1, {{@street2,}} " +
                    "@city, @state  @zip</p>\n", data));
        }
    }

    /**
     * Validate the data posted for a form.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Incoming data collected from the form.
     * @return                     True means the form data is OK; false
     *                             means there is a problem, and the form
     *                             has been updated with error information.
     */
    protected boolean validate(ClientRequest cr, Dataset data) {
        boolean success = true;

        // Make sure that values were provided for required fields.
        for (String id : new String[] {"name", "street1", "city",
                "state", "zip"}) {
            if (data.get(id).length() == 0) {
                form.elementError(cr, id,
                        "You must provide a value for this field");
                success = false;
            }
        }

        // Make sure that the ZIP code includes exactly 5 decimal digits.
        String zip = data.get("zip");
        if (zip.length() > 0) {
            if (zip.length() != 5) {
                form.elementError(cr, "zip",
                        "Zip code must consist of 5 decimal digits");
                success = false;
            }
            for (int i = 0; i < 5; i++) {
                if (!Character.isDigit(zip.charAt(i))) {
                    form.elementError(cr, "zip",
                            "Zip code must consist of 5 decimal digits");
                    success = false;
                }
            }
        }
        return success;
    }
}