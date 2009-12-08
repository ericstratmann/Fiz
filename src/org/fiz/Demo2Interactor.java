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

import org.apache.log4j.*;

/**
 * This interactor demonstrates several interesting Fiz sections with a
 * single page.
 */
public class Demo2Interactor extends Interactor {
    // The following variable is used for log4j-based logging.
    protected static Logger logger = Logger.getLogger(
            "org.fiz.Demo2Interactor");

    // Use this data manager for reading datasets from demo.yaml in the
    // demo directory.
    FileDataManager fileDataManager = new FileDataManager(
            Config.get("main", "home") + "/WEB-INF/demo");

    /**
     * Main URL entry point; displays a page containing some tabs, a table,
     * and a form.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void main(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Simple Fiz Demonstration");
        cr.getMainDataset().set("currentTabId", "students");
        Dataset students = fileDataManager.read("demo.yaml", "students");
        Section sections[] = {
                new TemplateSection("<h1>Your University Online</h1>\n"),
                new TabSection(new Dataset(),
                        new Dataset("id", "faculty", "text", "Faculty",
                                "url", "other?currentTabId=faculty"),
                        new Dataset("id", "students", "text", "Students",
                                "url", "main"),
                        new Dataset("id", "courses", "text", "Courses",
                                "url", "other?currentTabId=courses"),
                        new Dataset("id", "admin", "text", "Administration",
                                "url", "other?currentTabId=admin"),
                        new Dataset("id", "life", "text", "Campus Life",
                                "url", "other?currentTabId=life")),
                new TemplateSection("<h2>Current Students</h2>\n"),
                new TableSection(
                    new Dataset("data", students),
                    new Column("Name",
                            new Link("@last, @first", "student?id=@id")),
                    new Column("Student Id", "@id"),
                    new Column("Graduation Year", "@grad"),
                    new Column("GPA", "@gpa")),
                new TemplateSection("<h2>Enter New Student</h2>\n"),
                new FormSection(
                    new Dataset("postUrl", "post"),
                    new EntryFormElement(new Dataset("id", "last",
                            "label", "Last name:")),
                    new EntryFormElement(new Dataset("id", "first",
                            "label", "First name:")),
                    new EntryFormElement(new Dataset("id", "id",
                            "label", "Student id:")),
                    new EntryFormElement(new Dataset("id", "grad",
                            "label", "Graduation year:")),
                    new EntryFormElement(new Dataset("id", "gpa",
                            "label", "GPA:")))
        };
        cr.showSections(sections);
    }

    /**
     * This entry point is invoked to display details about a particular
     * student.  This functionality is currently unimplemented.  Query data:
     *     id:                     Identifier for the desired student.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void student(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Student Details");
        cr.showSections(
                new TemplateSection("<h1>Details for Student Id @id</h1>\n"),
                new TemplateSection("<p>This functionality has not yet " +
                        "been implemented.</p>")
        );
    }

    /**
     * This entry point is invoked for tabs other than "Students"; it just
     * displays an error message.  Query data:
     *     tab:                    Name of the desired tab.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void other(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Simple Fiz Demonstration");
        cr.showSections(
                new TemplateSection("<h1>Your University Online</h1>\n"),
                new TabSection(new Dataset(),
                        new Dataset("id", "faculty", "text", "Faculty",
                                "url", "other?currentTabId=faculty"),
                        new Dataset("id", "students", "text", "Students",
                                "url", "main"),
                        new Dataset("id", "courses", "text", "Courses",
                                "url", "other?currentTabId=courses"),
                        new Dataset("id", "admin", "text", "Administration",
                                "url", "other?currentTabId=admin"),
                        new Dataset("id", "life", "text", "Campus Life",
                                "url", "other?currentTabId=life")),
                new TemplateSection("<p>The contents of this tab have not " +
                        "yet been implemented.</p>\n")
        );
    }

    /**
     * This entry point is invoked when the "New Student" form is posted.
     * Right now it generates one of several error messages.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void post(ClientRequest cr) {
        FormSection form = new FormSection(
                new Dataset(),
                new EntryFormElement(new Dataset("id", "last",
                        "label", "Last name:")),
                new EntryFormElement(new Dataset("id", "first",
                        "label", "First name:")),
                new EntryFormElement(new Dataset("id", "id",
                        "label", "Student id:")),
                new EntryFormElement(new Dataset("id", "grad",
                        "label", "Graduation year:")),
                new EntryFormElement(new Dataset("id", "gpa",
                        "label", "GPA:")));
        boolean anyErrors = false;
        if (cr.getMainDataset().getString("last").length() == 0) {
            form.elementError (cr, "last", new Dataset("message",
                    "You must supply the student's last name."));
            anyErrors = true;
        };
        String gpa = cr.getMainDataset().getString("gpa");
        boolean gpaError = false;
        if (gpa.length() == 0) {
            form.elementError (cr, "gpa", new Dataset("message",
                    "You must supply the student's grade-point average.")
            );
            anyErrors = true;
        } else {
            try {
                double value = Double.parseDouble(gpa);
                if ((value < 0.0) || (value > 4.0)) {
                    gpaError = true;
                }
            }
            catch (NumberFormatException e) {
                gpaError = true;
            }
        }
        if (gpaError) {
            form.elementError(cr, "gpa", new Dataset("message",
                "The GPA must be a number between 0.0 and 4.0.")
            );
            anyErrors = true;
        }
        if (!anyErrors && (gpa.equals("4.0"))) {
            cr.addErrorsToBulletin((new Dataset("message",
                    "The response for the \"New Student\" form has " +
                    "not been implemented yet.")));
        }
    }
}
