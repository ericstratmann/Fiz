package org.fiz;

/**
 * A TableSection displays information in tabular form consisting of rows and
 * columns.  TableSections support the following constructor properties:
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML table that displays the TableSection.
 *   emptyTemplate:  (optional) If the table is empty ({@code request} returns
 *                   no records), this template will be expanded (using
 *                   the main dataset) and displayed in a single row in the
 *                   table.  If this property is not specified then a default
 *                   value will be supplied; specify this property with an
 *                   empty value if you want an empty table to simply appear
 *                   empty.
 *   errorTemplate:  (optional) If {@code request} returns an error, this
 *                   template will be expanded using the main dataset and
 *                   displayed in a single row in the table.  If this
 *                   property is omitted, then a default is supplied.
 *   id:             (optional) Used as the {@code id} attribute for the
 *                   HTML table that displays the TableSection.
 *   request:        (required) Name of the DataRequest that will supply
 *                   data to display in the TableSection; the response to
 *                   this request must contain one {@code record} child for
 *                   each row of the table.
 */
public class TableSection implements Section {
    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Dataset properties;
    protected Column[] columns;

    // Source of data for the rows of the table:
    protected DataRequest dataRequest;

    /**
     * Construct a TableSection.
     * @param properties           Contains configuration information
     *                             for the table; see description above.
     * @param columns              The remaining arguments describe the
     *                             columns of the table, in order from
     *                             left to right.
     */
    public TableSection(Dataset properties, Column ... columns) {
        this.properties = properties;
        this.columns = columns;
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and appends it to the Html
     * object associated with {@code clientRequest}.
     * @param clientRequest        Information about the request being
     *                             processed; HTML will be appended to
     *                             {@code clientRequest.getHtml()}.
     */
    public void html(ClientRequest clientRequest) {
        Html html = clientRequest.getHtml();
        StringBuilder out = html.getBody();
        Dataset mainDataset = clientRequest.getDataset();

        // Start.
        if (properties.check("class") == null) {
            html.includeCss("fiz/css/TableSection.css");
        }
        Template.expand("\n<!-- Start TableSection {{@id}} -->\n" +
                "<table {{id=\"@id\"}} class=\"@class?{TableSection}\" " +
                "cellspacing=\"0\">\n",
                properties, out);

        // Header row.
        out.append("  <tr class=\"header\">\n");
        for (int i = 0; i < columns.length; i++) {
            printTd(i, out);
            columns[i].headerHtml(out, clientRequest);
            out.append("</td>\n");
        }
        out.append("  </tr>\n");

        // Body rows.
        Dataset response = dataRequest.getResponseData();
        if (response == null) {
            // The request generated an error.  Display information about
            // the error in a single row.
            out.append("  <tr class=\"error\">\n    <td colspan=\"");
            out.append(columns.length);
            out.append("\">");
            Dataset errorDataset = dataRequest.getErrorData();
            errorDataset.setChain(mainDataset);
            Template.expand(Util.getErrorTemplate(properties),
                    errorDataset, out);
            out.append("</td>\n  </tr>\n");
        } else {
            Dataset[] rows = response.getChildren("record");
            if (rows.length == 0) {
                // The table is empty.  Display a single row containing
                // information about that fact.
                out.append("  <tr class=\"empty\">\n    <td colspan=\"");
                out.append(columns.length);
                out.append("\">");
                String template = properties.check("emptyTemplate");
                if (template == null) {
                    template = "There are no records to display";
                }
                Template.expand(template, mainDataset, out);
                out.append("</td>\n  </tr>\n");
            } else {
                // Normal case: there are records to display.
                boolean odd = true;
                for (Dataset rowData : rows) {
                    out.append("  <tr class=\"");
                    out.append(odd ? "odd\">\n" : "even\">\n");
                    odd = !odd;
                    for (int i = 0; i < columns.length; i++) {
                        printTd(i, out);
                        columns[i].html(rowData, out, clientRequest);
                        out.append("</td>\n");
                    }
                    out.append("  </tr>\n");
                }
            }
        }

        // End.
        Template.expand("</table>\n<!-- End TableSection {{@id}} -->\n",
                properties, out);
    }

    /**
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code clientRequest.registerDataRequest} for each of the
     * DataRequests needed by this section to gather data to be displayed.
     * @param clientRequest        Information about the request being
     *                             processed.
     */
    public void registerRequests(ClientRequest clientRequest) {
        dataRequest = clientRequest.registerDataRequest(
                properties.get("request"));
    }

    /**
     * Generates the {@code <td>} for a table element, with a "left" or
     * "right" class to mark the elements on the ends of the row
     * @param column               Index of this column (0 refers to the
     *                             leftmost column).
     * @param out                  HTML is appended here.
     */
    protected void printTd(int column, StringBuilder out) {
        if (column == 0) {
            out.append("    <td class=\"left\">");
        } else if (column == (columns.length-1)) {
            out.append("    <td class=\"right\">");
        } else {
            out.append("    <td>");
        }
    }
}
