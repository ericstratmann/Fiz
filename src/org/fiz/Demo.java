package org.fiz;

/**
 * This Interactor displays a collection of pages that illustrate the
 * facilities of Fiz.
 */

public class Demo extends Interactor {
    public void peopleTable(ClientRequest clientRequest) {
        clientRequest.getHtml().setTitle("TableSection Demo");
        clientRequest.showSections(
                new TemplateSection("<h1>Sample TableSection</h1>\n"),
                new TableSection(
                    new Dataset("request", "demo.getPeople"),
                    new Column("Name", "@name"),
                    new Column("Age", "{{@age}}"),
                    new Column("Height", "{{@height}}"),
                    new Column("Weight", "{{@weight}}"),
                    new Column("Social Security Number", "{{@ssn}}")
        ));
    }
}
