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
 *   lastRowClass:   (optional) If this property is defined, its value will
 *                   be used as the class for the bottom row in the table.
 *                   This can be used to display the bottom row differently,
 *                   e.g. for totals.
 *   noHeader:       (optional) If this property is defined, then the
 *                   header row for the table will not be displayed, even
 *                   if some of the columns offer header information.
 *                   Regardless of this property, the header row will be
 *                   omitted if none of the columns generate header info.
 *   request:        (required) Name of the DataRequest that will supply
 *                   data to display in the TableSection; the response to
 *                   this request must contain one {@code record} child for
 *                   each row of the table.
 */
public class TableSection implements Section {
    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Dataset properties;
    protected Formatter[] columns;

    // Source of data for the rows of the table:
    protected DataRequest dataRequest;

    /**
     * Construct a TableSection.
     * @param properties           Contains configuration information
     *                             for the table; see description above.
     * @param columns              The remaining arguments describe the
     *                             columns of the table, in order from
     *                             left to right.  Each argument can be
     *                             either a Column (in which case it knows
     *                             how to display a column header as well as
     *                             information for each row of the table)
     *                             or just a Formatter (in which case there
     *                             will be no header for this column).
     */
    public TableSection(Dataset properties, Formatter ... columns) {
        this.properties = properties;
        this.columns = columns;
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and appends it to the Html
     * object associated with {@code cr}.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML will be
     *                             appended to {@code cr.getHtml()}.
     */
    @Override
    public void html(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();
        Dataset mainDataset = cr.getMainDataset();
        String lastRowClass = properties.check("lastRowClass");

        // Start.
        if (!properties.containsKey("class")) {
            html.includeCssFile("TableSection.css");
        }
        Template.expand("\n<!-- Start TableSection {{@id}} -->\n" +
                "<table {{id=\"@id\"}} class=\"@class?{TableSection}\" " +
                "cellspacing=\"0\">\n",
                properties, out);

        // Header row.
        if (!properties.containsKey("noHeader")) {
            int oldLength = out.length();
            boolean anyHeaders = false;
            out.append("  <tr class=\"header\">\n");

            // Each iteration through the following loop generates the header
            // for one column.  While doing this, see if any of the columns
            // produce actual headers (if not, we will omit the entire
            // header row).
            for (int col = 0; col < columns.length; col++) {
                printTd(col, out);
                int headerStart = out.length();
                Formatter f = columns[col];
                if (f instanceof Column) {
                    ((Column) f).headerHtml(cr, out);
                }
                if (out.length() > headerStart) {
                    anyHeaders = true;
                }
                out.append("</td>\n");
            }
            if (anyHeaders) {
                out.append("  </tr>\n");
            } else {
                // None of the columns had a header to display; omit the
                // entire header row.
                out.setLength(oldLength);
            }
        }

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
                for (int i = 0; i < rows.length; i++) {
                    Dataset rowData = rows[i];
                    out.append("  <tr class=\"");
                    if ((i == (rows.length-1)) && (lastRowClass != null)) {
                        out.append(lastRowClass);
                        out.append("\">\n");
                    } else if ((i & 1) != 0) {
                        out.append("odd\">\n");
                    } else {
                        out.append("even\">\n");
                    }
                    for (int col = 0; col < columns.length; col++) {
                        printTd(col, out);
                        columns[col].html(cr, rowData, out);
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
     * it calls {@code cr.registerDataRequest} for each of the
     * DataRequests needed by this section to gather data to be displayed.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    @Override
    public void registerRequests(ClientRequest cr) {
        dataRequest = cr.registerDataRequest(
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
