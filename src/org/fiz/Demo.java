package org.fiz;
import org.apache.log4j.*;

/**
 * This Interactor displays a collection of pages that illustrate the
 * facilities of Fiz.
 */

public class Demo extends Interactor {
    // The following variable is used for log4j-based logging.
    protected Logger logger = Logger.getLogger("org.fiz.Demo");

    FormSection demoForm =new FormSection(
            new Dataset("id", "form1", "request", "demo.getFormData"),
            new EntryFormElement(new Dataset("id", "name",
                    "label", "Name:")),
            new EntryFormElement(new Dataset("id", "age",
                    "label", "Age:")),
            new EntryFormElement(new Dataset("id", "state",
                    "label", "Home state:")),
            new CompoundFormElement(new Dataset("id", "expiration",
                    "label", "Expiration date:", "template",
                    "<table cellspacing=\"0\"><tr><td>Month:&nbsp</td>" +
                    "<td>@1</td><td>&nbsp&nbsp Year:&nbsp</td>" +
                    "<td>@2</td></tr></table>"),
                    new SelectFormElement(YamlDataset.newStringInstance(
                        "id: expireMonth\n" +
                        "choice:\n" +
                        "  - value:  January\n" +
                        "  - value:  February\n" +
                        "  - value:  March\n" +
                        "  - value:  April\n" +
                        "  - value:  May\n" +
                        "  - value:  June\n" +
                        "  - value:  July\n" +
                        "  - value:  August\n" +
                        "  - value:  September\n" +
                        "  - value:  October\n" +
                        "  - value:  November\n" +
                        "  - value:  December\n")),
                    new SelectFormElement(YamlDataset.newStringInstance(
                        "id: expireYear\n" +
                        "choice:\n" +
                        "  - value:  2008\n" +
                        "  - value:  2009\n" +
                        "  - value:  2010\n" +
                        "  - value:  2011\n" +
                        "  - value:  2012\n" +
                        "  - value:  2013\n" +
                        "  - value:  2014\n"))),
            new PasswordFormElement(new Dataset("id", "password",
                    "label", "Password:")),
            new PasswordFormElement(new Dataset("id", "password2",
                    "label", "Retype password:", "duplicate", "password")),
            new CheckboxFormElement(new Dataset("id", "citizen",
                    "label", "U.S. citizen:", "extra",
                    "(you can click here too)")),
            new RadioFormElement(new Dataset("id", "notify",
                    "label", "Email opt-out:", "value", "all", "extra",
                    "Wants to receive all communications")),
            new RadioFormElement(new Dataset("id", "notify",
                    "value", "orders", "extra",
                    "Send only order status messages")),
            new RadioFormElement(new Dataset("id", "notify",
                    "value", "none", "extra",
                    "Send no messages of any form")),
            new TemplateFormElement(new Dataset("id", "heading",
                    "span", "true", "template",
                    "<div class=\"heading\">Optional Information</div>")),
            new SelectFormElement(new Dataset("id", "fruit",
                    "label", "Favorite fruits:",
                    "choiceRequest", "demo.getFruits",
                    "multiple", "multiple",
                    "height", "5",
                    "choiceName", "fruit")),
            new TextAreaFormElement(new Dataset("id", "saying",
                    "label", "Favorite saying:"))
            );

    /**
     * Displays a page with various demonstrations of the TableSection
     * class.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void tableSection(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("TableSection Demos");
        html.includeCssFile("demo/shoppingCart.css");
        cr.showSections(
                new TemplateSection("<h1>TableSection Demos</h1>\n"),

                new TemplateSection("<h2>Basic table:</h2>\n"),
                new TableSection(
                    new Dataset("request", "demo.getPeople"),
                    new Column("Name", "@name"),
                    new Column("Age", "{{@age}}"),
                    new Column("Height", "{{@height}}"),
                    new Column("Weight", "{{@weight}}"),
                    new Column("Social Security Number", "{{@ssn}}")),

                new TemplateSection("<h2>Table with no data:</h2>\n"),
                new TableSection(
                    new Dataset("request", "demo.noData"),
                    new Column("Name", "@name"),
                    new Column("Age", "{{@age}}"),
                    new Column("Height", "{{@height}}"),
                    new Column("Weight", "{{@weight}}"),
                    new Column("Social Security Number", "{{@ssn}}")),

                new TemplateSection("<h2>Error in data request:</h2>\n"),
                new TableSection(
                    new Dataset("request", "demo.error"),
                    new Column("Name", "@name"),
                    new Column("Age", "{{@age}}"),
                    new Column("Height", "{{@height}}"),
                    new Column("Weight", "{{@weight}}"),
                    new Column("Social Security Number", "{{@ssn}}")),

                new TemplateSection("<h2>Simple shopping cart:</h2>\n"),
                new TableSection(
                    new Dataset("request", "demo.getCart", "noHeader", "true",
                            "class", "shoppingCart", "lastRowClass", "last"),
                    new Column("Item", "@item"),
                    new Column("Price", "@price"))
        );
    }

    /**
     * Displays a page illustrating the FormSection class.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void formSection(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("FormSection Demo");
        html.includeCssFile("demo/form.css");
        cr.showSections(
                new TemplateSection("<h1>FormSection Demo</h1>\n"),
                demoForm);
    }

    public void ajaxPost(ClientRequest cr) {
        Dataset main = cr.getMainDataset();
        logger.info("Posted dataset:\n" + main.toString());
        demoForm.post(cr, (main.get("state").length() == 0) ?
                "demo.formError1" : "demo.formError2");

    }
}
