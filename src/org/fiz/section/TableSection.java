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

package org.fiz.section;

import org.fiz.*;
import java.util.*;

/**
 * A TableSection displays information in tabular form consisting of rows and
 * columns.  TableSections support the following constructor properties:
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML table that displays the TableSection.
 *   emptyTemplate:  (optional) If the table is empty ({@code data} is an
 *                   empty dataset), this template will be expanded (using
 *                   the main dataset) and displayed in a single row in the
 *                   table.  If this property is not specified then a default
 *                   value will be supplied; specify this property with an
 *                   empty value if you want an empty table to simply appear
 *                   empty.
 *   errorStyle:     (optional) If {@code data} contains an error then
 *                   this property contains the name of a template in the
 *                   {@code styles} dataset, which is expanded with the
 *                   error data and the main dataset.  The resulting HTML
 *                   is displayed as the body of the table.  In addition,
 *                   if there exists a template in the {@code styles} dataset
 *                   with the same name followed by "-bulletin", it is expanded
 *                   and the resulting HTML is displayed in the bulletin.
 *                   Defaults to "TableSection.error".
 *   id:             (optional) Used as the {@code id} attribute for the
 *                   HTML table that displays the TableSection.
 *   noHeader:       (optional) If this property is defined, then the
 *                   header row for the table will not be displayed, even
 *                   if some of the columns offer header information.
 *                   Regardless of this property, the header row will be
 *                   omitted if none of the columns generate header info.
 *   data:           (required) Supplies data for the table; The dataset
 *                   must contain one {@code record} child
 *                   for each row of the table; when rendering a row, the
 *                   child dataset for that row will be passed to each of the
 *                   Column objects.
 *
 * TableSection automatically sets the following {@code class} attributes
 * for use in CSS (some elements may have more than one class):
 *   header:         The {@code <tr>} for the table's header row.
 *   odd:            The {@code <tr>} for the first non-header row in the
 *                   table, and every other row after that.
 *   even:           The {@code <tr>} for second non-header row in the
 *                   table, and every other row after that.
 *   last:           The {@code <tr>} for the last row in the table.
 *   left:           The leftmost {@code <td>} for each row.
 *   right:          The rightmost {@code <td>} for each row.
 *   error:          If an error occurs in the data request for the table
 *                   then the first row of the table (which displays the
 *                   error message) will have this class.
 *   empty:          If there is no data for the table then the first row
 *                   of the table (which displays {@code emptyTemplate})
 *                   will have this class.
 */
public class TableSection extends Section {
    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Section[] columns;

    /**
     * Construct a TableSection.
     * @param properties           Contains configuration information
     *                             for the table; see description above.
     * @param columns              The remaining arguments describe the
     *                             columns of the table, in order from
     *                             left to right.  For each data record
     *                             in {@code data} one row will
     *                             be generated in the table; each column's
     *                             {@code html} method will be invoked to
     *                             generate the HTML between the {@code <td>}
     *                             and the {@code </td>}) for that column.
     *                             The dataset passed to the {@code html}
     *                             methods will include the information for
     *                             the row as well as the main dataset for the
     *                             client request.  If a column is also a
     *                             Column then it will also be invoked to
     *                             generate header HTML for the column.
     */
    public TableSection(Dataset properties, Section ... columns) {
        this.properties = properties;
        this.columns = columns;
        if (!properties.containsKey("data")) {
            throw new org.fiz.InternalError("TableSection constructor invoked " +
                    "without a \"data\" property");
        }
    }

    @Override
    public void render(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();
        Dataset mainDataset = cr.getMainDataset();

        // The following dataset will eventually include the data from
        // the current row plus the main dataset.
        CompoundDataset dataForRow = new CompoundDataset(null, mainDataset);

        // Start.
        if (!properties.containsKey("class")) {
            html.includeCssFile("TableSection.css");
        }
        Template.appendHtml(out, "\n<!-- Start TableSection {{@id}} -->\n" +
                "<table {{id=\"@id\"}} class=\"@class?{TableSection}\" " +
                "cellspacing=\"0\">\n",
                properties);

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
                Section f = columns[col];
                if (f instanceof Column) {
                    ((Column) f).renderHeader(cr);
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
        Dataset data = properties.getDataset("data");
        if (data.getErrorData() != null) {
            // The request generated an error.  Display information about
            // the error in a single row.
            out.append("  <tr class=\"error\">\n    <td colspan=\"");
            out.append(columns.length);
            out.append("\">");
            Dataset[] errors = data.getErrorData();
            errors[0].set("sectionType", "table");
            cr.showErrorInfo(properties.checkString("errorStyle"),
                    "TableSection.error", errors[0]);
            out.append("</td>\n  </tr>\n");
        } else {
            ArrayList<Dataset> rows = data.getDatasetList("record");
            if (rows.size() == 0) {
                // The table is empty.  Display a single row containing
                // information about that fact.
                out.append("  <tr class=\"empty\">\n    <td colspan=\"");
                out.append(columns.length);
                out.append("\">");
                String template = properties.checkString("emptyTemplate");
                if (template == null) {
                    template = "There are no records to display";
                }
                Template.appendHtml(out, template, mainDataset);
                out.append("</td>\n  </tr>\n");
            } else {
                // Normal case: there are records to display.
                for (int i = 0; i < rows.size(); i++) {
                    dataForRow.setComponent(0, rows.get(i));
                    out.append("  <tr class=\"");
                    if (i == (rows.size()-1)) {
                        out.append("last ");
                    }
                    if ((i & 1) != 0) {
                        out.append("odd\">\n");
                    } else {
                        out.append("even\">\n");
                    }
                    for (int col = 0; col < columns.length; col++) {
                        printTd(col, out);
                        columns[col].render(cr, dataForRow);
                        out.append("</td>\n");
                    }
                    out.append("  </tr>\n");
                }
            }
        }

        // End.
        Template.appendHtml(out,
                "</table>\n<!-- End TableSection {{@id}} -->\n",
                properties);
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
