package org.fiz;
import java.lang.management.*;
import java.util.*;

import org.apache.commons.fileupload.*;
import org.apache.log4j.*;
import org.fiz.SqlDataManager.SqlRequest;

/**
 * This Interactor displays a collection of pages that illustrate the
 * facilities of Fiz.
 */

public class FormInteractor extends Interactor {
    // The following variable is used for log4j-based logging.
    protected Logger logger = Logger.getLogger("org.fiz.FormInteractor");

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

    // Lazily instantiated: Data manager for connecting to SQL database
    // to authenticate users
    SqlDataManager sqlDataManager = new SqlDataManager(new Dataset(
            "serverUrl","jdbc:mysql://localhost",
            "user", "root",
            "password", "",
            "driverClass", "com.mysql.jdbc.Driver"));
    
    Dataset formData = new Dataset("name", "Bill", "age", "41",
            "height", "73", "weight", "195",
            "saying", "Line 1\nLine 2\n<Line 3>\n", "fruit", "grape",
            "notify", "all", "state", "California", "mascot", "Spartans");
    
    private static Dataset autocompleteData = new Dataset(
            "name", new Dataset("choice", "George Washington"),
            "name", new Dataset("choice", "John Adams"),
            "name", new Dataset("choice", "Thoms Jefferson"),
            "name", new Dataset("choice", "James Madison"),
            "name", new Dataset("choice", "James Monroe"),
            "name", new Dataset("choice", "John Quincy Adams"),
            "name", new Dataset("choice", "Andrew Jackson"),
            "name", new Dataset("choice", "Martin Van Buren"),
            "name", new Dataset("choice", "William Henry Harrison"),
            "name", new Dataset("choice", "John Tyler"),
            "name", new Dataset("choice", "James Knox Polk"),
            "name", new Dataset("choice", "Zachary Taylor"),
            "name", new Dataset("choice", "Millard Fillmore"),
            "name", new Dataset("choice", "Franklin Pierce"),
            "name", new Dataset("choice", "James Buchanan"),
            "name", new Dataset("choice", "Abraham Lincoln"),
            "name", new Dataset("choice", "Andrew Johnson"),
            "name", new Dataset("choice", "Ulysses S. Grant"),
            "name", new Dataset("choice", "Rutherford B. Hayes"),
            "name", new Dataset("choice", "James Garfield"),
            "name", new Dataset("choice", "Chester Arthur"),
            "name", new Dataset("choice", "Grover Cleveland"),
            "name", new Dataset("choice", "Benjamin Harrison"),
            "name", new Dataset("choice", "Grover Cleveland"),
            "name", new Dataset("choice", "William McKinley"),
            "name", new Dataset("choice", "Theodore Roosevelt"),
            "name", new Dataset("choice", "William Howard Taft"),
            "name", new Dataset("choice", "Woodrow Wilson"),
            "name", new Dataset("choice", "Warren Harding"),
            "name", new Dataset("choice", "Calvin Coolidge"),
            "name", new Dataset("choice", "Herbert Hoover"),
            "name", new Dataset("choice", "Franklin D. Roosevelt"),
            "name", new Dataset("choice", "Harry S. Truman"),
            "name", new Dataset("choice", "Dwight D. Eisenhower"),
            "name", new Dataset("choice", "John F. Kennedy"),
            "name", new Dataset("choice", "Lyndon Johnson"),
            "name", new Dataset("choice", "Richard Nixon"),
            "name", new Dataset("choice", "Gerald Ford"),
            "name", new Dataset("choice", "James Carter"),
            "name", new Dataset("choice", "Ronald Reagan"),
            "name", new Dataset("choice", "George H.W. Bush"),
            "name", new Dataset("choice", "William J. Clinton"),
            "name", new Dataset("choice", "George W. Bush"),
            "name", new Dataset("choice", "Barack H. Obama"));

    FormSection testForm = new FormSection(
            new Dataset("id", "form1",
                    "request", "getFormData",
                    "postUrl", "postForm"),
            new PasswordFormElement(new Dataset("id", "password",
                    "label", "Password:")),
            new PasswordFormElement(new Dataset("id", "password2",
                    "label", "Retype password:",
                    "duplicate", "password")),
            new EntryFormElement(new Dataset("id", "validate",
                    "label", "Length:",
                    "required", "true",
                    "validator", new Dataset("type", "length",
                            "min", "5", "max", "8"))),
            new EntryFormElement(new Dataset("id", "validate2",
                    "label", "Length 2:",
                    "required", "true",
                    "validator", new Dataset("type", "length",
                            "min", "5", "max", "8"))),
            new EntryFormElement(new Dataset("id", "validate3",
                    "label", "Range:",
                    "required", "true",
                    "validator", new Dataset("type", "Range",
                            "min", "5", "max", "8", "includeMax", "false"))),
            new CheckboxFormElement(new Dataset("id", "citizen",
                    "label", "Checkbox:")),
            new RadioFormElement(new Dataset("id", "notify",
                    "label", "Radio:", "value", "all", "extra",
                    "One")),
            new RadioFormElement(new Dataset("id", "notify",
                    "value", "orders", "extra",
                    "Two")),
            new RadioFormElement(new Dataset("id", "notify",
                    "value", "none", "extra",
                    "Three")),
            new AutocompleteFormElement(new Dataset("id", "name",
            "label", "Name:",
            "help", "Enter customer name here",
            "requestFactory", "FormInteractor.autocompleteRequest")),
            new DateFormElement(new Dataset("id", "expiration",
                    "label", "Expiration date:",
                    "attachPosition", "bottom",
                    "dateFormat", "m-d-Y",
                    "exclude", "Saturday, 1/12/2005, 7/23," +
                    "8/13/2010:9/11/2010, " +
                    ":6/24/2009, 9/3/2011:"))
            );

    public static DataRequest autocompleteRequest(String query) {
        query = query.toLowerCase();

        Dataset matches = new Dataset();
        for(Dataset data : autocompleteData.getChildren("name")) {
            String dataName = data.get("choice").toLowerCase();
            if(dataName.substring(0, Math.min(query.length(),
                    dataName.length())).equals(query)) {
                matches.addChild("record", data);
            }
        }

        return RawDataManager.newRequest(matches);
    }

    public void formSection(ClientRequest cr) {
        cr.addDataRequest("getFormData", RawDataManager.newRequest(formData));

        Html html = cr.getHtml();
        html.setTitle("FormSection Demo");
        html.includeCssFile("demo/form.css");
        cr.setPageProperty("formInfo", "Zip code: 94301");

        cr.showSections(
                new TemplateSection("<h1>Test Form</h1>\n"),
                testForm);
    }

    /**
     * Returns an empty page; used as a control case for benchmarking.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void noop(ClientRequest cr) {
        // Don't do anything.
    }

    public void register(ClientRequest cr) {
        
    }
        
    public void postForm(ClientRequest cr) {
        Dataset formData = testForm.collectFormData(cr);
        cr.redirect("register");
    }
    
    public void coolStuff(ClientRequest cr) {
        
    }
}
