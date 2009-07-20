package org.fiz;

/**
 * This interactor influence a collection of pages used for testing the
 * performance of Fiz.  For details, view the individual pages in a
 * browser.
 */
public class PerfInteractor extends Interactor {
    // The following variables are used to count iterations of the
    // form submission latency test.
    protected int submitCount = 0;
    protected final int MAX_SUBMIT_COUNT = 100;

    // Starting time for a performance measurement.
    long startTime;

    public void main(ClientRequest cr) {
        Html html = cr.getHtml();
        html.setTitle("Fiz Performance Tools");
        cr.showSections(
                new TemplateSection(
                        "<h1>Form Submission Latency</h1>\n" +
                        "<p>To measure the round-trip latency for " +
                        "submitting a simple form, click the submit " +
                        "button below.</p>\n"),
                new FormSection(
                        new Dataset("id", "submitPerf",
                        "postUrl", "postSubmitPerf"),
                        new EntryFormElement("name", "Name:"),
                        new EntryFormElement("age", "Age:")),
                new TemplateSection("<div>Average round-trip time: " +
                        "<span id=\"submitMs\"></span></div>\n")
        );
    }

    public void postSubmitPerf(ClientRequest cr) {
        String name = cr.getMainDataset().get("name");
        if (submitCount == 0) {
            startTime = System.nanoTime();
        }
        submitCount++;
        if (submitCount < MAX_SUBMIT_COUNT) {
            cr.evalJavascript("Fiz.ids.submitPerf.submit();\n" +
                    "document.getElementById(\"submitPerf\").submit()");
        } else {
            double average = (System.nanoTime() - startTime)/
                    (1000000.0 * (MAX_SUBMIT_COUNT-1));
            cr.evalJavascript(Template.expandJavascript(
                    "document.getElementById(\"submitMs\").innerHTML = " +
                    "\"@1 ms\";\n", String.format("%.2f", average)));
            submitCount = 0;
        }
    }
}
