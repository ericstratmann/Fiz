package org.fiz;
import java.util.*;
import org.apache.log4j.*;

/**
 * This Interactor displays a collection of pages that illustrate the
 * facilities of Fiz.
 */

public class Demo extends Interactor {
    // The following variable is used for log4j-based logging.
    protected Logger logger = Logger.getLogger("org.fiz.Demo");

    FormSection sideBySideForm =new FormSection(
            new Dataset("id", "form1", "request", "demo.getFormData",
                    "postUrl", "ajaxSideBySidePost"),
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

    FormSection verticalForm =new FormSection(
            new Dataset("id", "form2", "request", "demo.getFormData",
                    "layout", "vertical", "postUrl", "ajaxVerticalPost"),
            new EntryFormElement(new Dataset("id", "name2",
                    "label", "Name:")),
            new EntryFormElement(new Dataset("id", "age2",
                    "label", "Age:")),
            new EntryFormElement(new Dataset("id", "state2",
                    "label", "Home state:")),
            new CheckboxFormElement(new Dataset("id", "citizen2",
                    "extra", "U.S. citizen")),
            new SelectFormElement(new Dataset("id", "fruit2",
                    "label", "Favorite fruit:",
                    "choiceRequest", "demo.getFruits",
                    "choiceName", "fruit")),
            new TextAreaFormElement(new Dataset("id", "saying2",
                    "label", "Favorite saying:", "rows", "3"))
            );

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
                new TemplateSection("<h1>Side-By-Side Form</h1>\n"),
                sideBySideForm,
                new TemplateSection("<h1>Vertical Form</h1>\n"),
                verticalForm);
    }

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
     * Displays a page containing statistics about Interactor invocations.
     * class.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void stats(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Interactor Statistics");
        ArrayList<Dataset> stats = ((Dispatcher) cr.getServlet()).
                getInteractorStatistics();
        Collections.sort(stats, new DatasetComparator("averageMs",
                DatasetComparator.Type.FLOAT,
                DatasetComparator.Order.DECREASING));
        Dataset statsDataset = new Dataset();
        for (Dataset d : stats) {
            statsDataset.addChild("record", d);
        }
        cr.showSections(
                new TemplateSection("<h1>Interactor Statistics</h1>\n"),
                new TableSection(
                    RawDataManager.setRequest(new Dataset(), "request",
                            statsDataset),
                    new Column("Class/Method", "@name"),
                    new Column("# Invocations", "@invocations"),
                    new Column("Average (ms)", "@averageMs"),
                    new Column("Std Dev. (ms)", "@standardDeviationMs"))
        );
        StringBuilder body = html.getBody();
        Link link = new Link(new Dataset("text", "Clear statistics",
                "ajaxUrl", "/fiz/demo/ajaxClearStats"));
        body.append("<p>");
        link.html(cr, cr.getMainDataset(), body);
    }

    public void ajaxClearStats(ClientRequest cr) {
        ((Dispatcher) cr.getServlet()).clearInteractorStatistics();
        cr.ajaxRedirectAction("stats");
    }

    public void ajaxSideBySidePost(ClientRequest cr) {
        Dataset main = cr.getMainDataset();
        logger.info("Posted dataset:\n" + main.toString());
        sideBySideForm.post(cr, (main.get("state").length() == 0) ?
                "demo.formError1" : "demo.formError2");

    }

    public void ajaxVerticalPost(ClientRequest cr) {
        Dataset main = cr.getMainDataset();
        logger.info("Posted dataset:\n" + main.toString());
        verticalForm.post(cr, "demo.formError3");
    }
}
