package org.fiz;
import java.lang.management.*;
import java.util.*;

import org.apache.commons.fileupload.*;
import org.apache.log4j.*;

/**
 * This Interactor displays a collection of pages that illustrate the
 * facilities of Fiz.
 */

public class DemoInteractor extends Interactor {
    // The following variable is used for log4j-based logging.
    protected Logger logger = Logger.getLogger("org.fiz.DemoInteractor");

    // The following beans allow us to collect execution time information
    // from all of the various garbage collectors.
    List<GarbageCollectorMXBean> beans =
            ManagementFactory.getGarbageCollectorMXBeans();

    // Total time spent in garbage collection since the last time
    // performance statistics were reset.
    long baseGcMs = 0;

    // Total number of garbage collector indications since the last time
    // performance statistics were reset.
    int baseGcInvocations = 0;

    // Use this data manager for reading datasets from demo.yaml in the
    // demo directory.
    FileDataManager fileDataManager = new FileDataManager(
            Config.get("main", "home") + "/WEB-INF/demo");

    // Datasets that supply results for raw data requests.
    Dataset formData = new Dataset("name", "Bill", "age", "41",
            "height", "73", "weight", "195",
            "saying", "Line 1\nLine 2\n<Line 3>\n", "fruit", "grape",
            "notify", "all", "state", "California", "mascot", "Spartans");
    Dataset fruitInfo = new Dataset(
            "fruit", new Dataset("name", "Apple", "value", "Apple"),
            "fruit", new Dataset("name", "Banana", "value", "banana"),
            "fruit", new Dataset("name", "Grape", "value", "grape"),
            "fruit", new Dataset("name", "Kiwi", "value", "kiwi"),
            "fruit", new Dataset("name", "Peach", "value", "peach"),
            "fruit", new Dataset("name", "Pear", "value", "pear"),
            "fruit", new Dataset("name", "Raspberry", "value", "raspberry"),
            "fruit", new Dataset("name", "Strawberry", "value", "strawberry"),
            "fruit", new Dataset("name", "Watermelon", "value", "watermelon"));

    FormSection sideBySideForm = new FormSection(
            new Dataset("id", "form1", "request", "getFormData",
                    "postUrl", "postSideBySide"),
            new EntryFormElement(new Dataset("id", "name",
                    "label", "Name:", "help", "Enter customer name here")),
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
                    "choiceRequest", "getFruits",
                    "multiple", "multiple",
                    "height", "5",
                    "choiceName", "fruit")),
            new TextAreaFormElement(new Dataset("id", "saying",
                    "label", "Favorite saying:")),
            new UploadFormElement("upload", "File to upload:"),
            new HiddenFormElement("mascot")
            );

    FormSection verticalForm = new FormSection(
            new Dataset("id", "form2", "request", "getFormData",
                    "layout", "vertical", "postUrl", "postVertical"),
            new EntryFormElement(new Dataset("id", "name",
                    "label", "Name:", "help", "Enter employee name here")),
            new EntryFormElement(new Dataset("id", "age",
                    "label", "Age:")),
            new EntryFormElement(new Dataset("id", "state",
                    "label", "Home state:")),
            new CheckboxFormElement(new Dataset("id", "citizen",
                    "extra", "U.S. citizen")),
            new SelectFormElement(new Dataset("id", "fruit",
                    "label", "Favorite fruit:",
                    "choiceRequest", "getFruits",
                    "choiceName", "fruit")),
            new TextAreaFormElement(new Dataset("id", "saying",
                    "label", "Favorite saying:", "rows", "3"))
            );

    /**
     * Displays a page illustrating usage of the CompoundSection class.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void compound(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("CompoundSection Demo");
        CompoundSection section = new CompoundSection (
                new Dataset("borderFamily", "/fizlib/images/borderBlueFilled",
                        "background", "#f1f5fb"),
                new TemplateSection ("<p>Here is some sample text " +
                        "to fill the body of this compound section.</p>\n"),
                new TemplateSection ("<p>This is a second paragraph, " +
                        "which follows the first.</p>\n")
        );
        CompoundSection section2 = new CompoundSection (
                new Dataset("borderFamily", "/fizlib/images/borderGrayFilled",
                        "background", "#f5f5f5"),
                new TemplateSection ("<p>This text provides the body " +
                        "of the second compound section.  I will type " +
                        "and type and type some more, in order to " +
                        "provide lots of text for this section, " +
                        "so that it overflows onto several lines.</p>\n")
        );
        cr.showSections(
                new TemplateSection("<h1>Compound Section</h1>\n"),
                section,
                new TemplateSection("<br>"),
                section2);
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
        cr.addDataRequest("getFormData", RawDataManager.newRequest(formData));
        cr.addDataRequest("getFruits", RawDataManager.newRequest(fruitInfo));
        cr.showSections(
                new TemplateSection("<h1>Side-By-Side Form</h1>\n"),
                sideBySideForm,
                new TemplateSection("<h1>Vertical Form</h1>\n"),
                verticalForm);
    }

    /**
     * Returns an empty page; used as a control case for benchmarking.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void noop(ClientRequest cr) {
        // Don't do anything.
    }

    /**
     * Displays a page illustrating the TabSection class.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void tabSection(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("TabSection Demo");
        cr.showSections(
                new TemplateSection("<h1>Sample TabSection</h1>\n"),
                new TabSection(new Dataset(),
                        new Dataset("id", "t1", "text", "Inventory",
                                "url", "tabSection?currentTabId=t1"),
                        new Dataset("id", "t2", "text", "Orders",
                                "url", "tabSection?currentTabId=t2"),
                        new Dataset("id", "t3", "text", "Accounts",
                                "ajaxUrl", "ajaxTab?tab=t3"),
                        new Dataset("id", "t4", "text", "Shippers",
                                "javascript",
                                "document.getElementById(\"text\").innerHTML " +
                                "= 'You clicked on tab \"t4\", which caused " +
                                "this text to be updated by Javascript.'"),
                        new Dataset("id", "t5", "text", "Suppliers",
                                "url", "tabSection?currentTabId=t5")),
                new TemplateSection("<p id=\"text\">The currently selected " +
                        "tab has id \"@currentTabId?{??}\"</p>\n"));
    }

    /**
     * Invoked by some of the tabs in {@code tabSection}.  Modifies
     * the text displayed underneath the tabs.
     * @param cr
     */
    public void ajaxTab(ClientRequest cr) {
        cr.updateElement("text", "You clicked on tab \"" +
                cr.getMainDataset().get("tab") +
                "\", which caused this text to be updated via AJAX.");
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
        cr.addDataRequest("people", fileDataManager.newReadRequest("demo.yaml",
                "people"));
        cr.addDataRequest("noData", fileDataManager.newReadRequest("demo.yaml",
                "empty"));
        cr.addDataRequest("error", fileDataManager.newReadRequest("demo.yaml",
                "bogus.nonexistent"));
        cr.addDataRequest("cart", fileDataManager.newReadRequest("demo.yaml",
                "shoppingCart"));
        cr.showSections(
                new TemplateSection("<h1>TableSection Demos</h1>\n"),

                new TemplateSection("<h2>Basic table:</h2>\n"),
                new TableSection(
                    new Dataset("request", "people"),
                    new Column("Name", "@name"),
                    new Column("Age", "{{@age}}"),
                    new Column("Height", "{{@height}}"),
                    new Column("Weight", "{{@weight}}"),
                    new Column("Social Security Number", "{{@ssn}}")),

                new TemplateSection("<h2>Table with no data:</h2>\n"),
                new TableSection(
                    new Dataset("request", "noData"),
                    new Column("Name", "@name"),
                    new Column("Age", "{{@age}}"),
                    new Column("Height", "{{@height}}"),
                    new Column("Weight", "{{@weight}}"),
                    new Column("Social Security Number", "{{@ssn}}")),

                new TemplateSection("<h2>Error in data request:</h2>\n"),
                new TableSection(
                    new Dataset("request", "error"),
                    new Column("Name", "@name"),
                    new Column("Age", "{{@age}}"),
                    new Column("Height", "{{@height}}"),
                    new Column("Weight", "{{@weight}}"),
                    new Column("Social Security Number", "{{@ssn}}")),

                new TemplateSection("<h2>Simple shopping cart:</h2>\n"),
                new TableSection(
                    new Dataset("request", "cart", "noHeader", "true",
                            "class", "shoppingCart", "lastRowClass", "last"),
                    new Column("Item", "@item"),
                    new Column("Price", "@price"))
        );
    }

    /**
     * Displays a page containing statistics from all the performance timers.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void stats(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Timer Statistics");
        ArrayList<Dataset> stats = Timer.getStatistics(1.0e06, "%.3f");
        Collections.sort(stats, new DatasetComparator("average",
                DatasetComparator.Type.FLOAT,
                DatasetComparator.Order.DECREASING));
        Dataset statsDataset = new Dataset();
        for (Dataset d : stats) {
            statsDataset.addChild("record", d);
        }
        cr.addDataRequest("stats", RawDataManager.newRequest(statsDataset));
        cr.showSections(
                new TemplateSection("<h1>Timer Statistics</h1>\n"),
                new TableSection(
                    new Dataset("request", "stats"),
                    new Column("Timer", "@name"),
                    new Column("Count", "@intervals"),
                    new Column("Avg (ms)", "@average"),
                    new Column("Min (ms)", "@minimum"),
                    new Column("Max (ms)", "@maximum"),
                    new Column("Std Dev. (ms)", "@standardDeviation"))
        );
        long gcMs = -baseGcMs;
        int gcInvocations = -baseGcInvocations;
        for (GarbageCollectorMXBean bean: beans) {
            gcMs += bean.getCollectionTime();
            gcInvocations += bean.getCollectionCount();
        }
        StringBuilder body = html.getBody();
        body.append(String.format("<p>\nGarbage collector invocations: %d" +
                "<br>Garbage collector execution time: %dms</p>",
                gcInvocations, gcMs));
        Link link = new Link(new Dataset("text", "Clear statistics",
                "ajaxUrl", "/fiz/demo/ajaxClearStats"));
        body.append("<p>");
        link.html(cr, cr.getMainDataset(), body);
    }

    /**
     * Displays a page demonstrating the TreeSection class.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void tree(ClientRequest cr) {
        Html html = cr.getHtml();
        html.clear();
        html.setTitle("TreeSection Demo");
        Dataset main = cr.getMainDataset();
        String edgeFamily = main.check("edgeFamily");
        if (edgeFamily == null) {
            edgeFamily = "treeSolid";
        }
        cr.showSections(
                new TemplateSection("<h1>TreeSection Demo</h1>\n" +
                        "<div id=\"p1\"><p>Current edge style: " +
                        "@edgeStyle?{treeSolid}</p></div>\n" +
                        "<p><a href=\"tree?edgeFamily=treeSolid\">Change " +
                        "edge style to treeSolid</a><br />\n" +
                        "<a href=\"tree?edgeFamily=treeDotted\">Change " +
                        "edge style to treeDotted</a><br />\n" +
                        "<a href=\"tree?edgeFamily=treeNoLines\">Change " +
                        "edge style to treeNoLines</a></p>\n"),
                new TreeSection(
                    new Dataset("id", "tree1", "requestFactory",
                            "DemoInteractor.treeRequest", "edgeFamily",
                            edgeFamily))
        );
    }

    /**
     * Creates a DataRequest that returns the contents of a node in the
     * TreeSection demonstration.
     * @param name                 Name of the parent node whose children
     *                             are needed.
     * @return                     DataRequest whose response contains
     *                             information about all of the children of
     *                              by gone for that I the I{@code name}.
     */
    public static DataRequest treeRequest(String name) {
        Dataset data = YamlDataset.newStringInstance(
                "top:\n" +
                "    record:\n" +
                "        - text:       Alice\n" +
                "        - text:       Bill\n" +
                "          name:       bill\n" +
                "          expandable: 1\n" +
                "        - text:       Carol\n" +
                "          name:       carol\n" +
                "          expandable: 1\n" +
                "        - text:       David\n" +
                "bill:\n" +
                "    record:\n" +
                "        - text:       Ellen\n" +
                "          name:       ellen\n" +
                "          expandable: 1\n" +
                "        - text:       Frank\n" +
                "        - text:       Grace\n" +
                "carol:\n" +
                "    record:\n" +
                "        - text:       Harry\n" +
                "        - text:       Ian\n" +
                "        - text:       Juliet\n" +
                "ellen:\n" +
                "    record:\n" +
                "        - text:       Kurt\n" +
                "        - text:       Leslie\n" +
                "        - text:       Michael\n" +
                "          name:       top\n" +
                "          expandable: 1"

        );
        if (name.length() == 0) {
            return RawDataManager.newRequest(data.getChild("top"));
        }
        return RawDataManager.newRequest(data.getChild(name));
    }

    public void ajaxClearStats(ClientRequest cr) {
        Timer.resetAll();
        Timer.measureNoopTime();
        long gcMs = 0;
        int gcInvocations = 0;
        for (GarbageCollectorMXBean bean: beans) {
            gcMs += bean.getCollectionTime();
            gcInvocations += bean.getCollectionCount();
        }
        baseGcMs = gcMs;
        baseGcInvocations = gcInvocations;
        cr.redirect("stats");
    }

    public void postSideBySide(ClientRequest cr) {
        Dataset main = cr.getMainDataset();
        logger.info("Posted dataset:\n" + main.toString());
        FileItem upload = cr.getUploadedFile("upload");
        if (upload != null) {
            if (upload.getSize() > 200) {
                logger.info("Contents of uploaded file:\n" +
                        upload.getString().substring(0, 200) + " ...");
            } else {
                logger.info("Contents of uploaded file:\n" +
                        upload.getString());
            }
        } else {
            logger.info("No file upload with this submission");
        }
        Dataset formData = sideBySideForm.collectFormData(cr);
        if (formData.get("state").length() == 0) {
            sideBySideForm.displayErrors(cr, new Dataset("culprit", "name",
                    "message",
                    "User doesn't exist; this is a very long message " +
                    "just to see what happens.  Does the line wrap, or " +
                    "doesn't it?  In order to find out, there needs to be " +
                    "lots and lots of text in the error message.  Also, " +
                    "here are some special characters: <&>!\"\""));
        } else {
            sideBySideForm.displayErrors(cr, new Dataset("culprit", "name",
                    "message", "User doesn't exist"),
                    new Dataset("message",
                    "An error occurred that isn't associated " +
                    "with a particular form element, so this " +
                    "message should appear in the bulletin."));
        }
    }

    public void postVertical(ClientRequest cr) {
        Dataset main = cr.getMainDataset();
        logger.info("Posted dataset:\n" + main.toString());
        verticalForm.displayErrors(cr, new Dataset("culprit", "fruit",
                "message", "You have chosen an exceptionally bad " +
                "tasting fruit; please pick another."));
    }
}
