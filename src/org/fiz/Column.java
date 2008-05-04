package org.fiz;

/**
 * A Column object represents one column in a TableSection.  This class
 * implements a few common forms of columns; subclasses implement additional
 * forms.  A Column implements the Formatter interface, which knows how to
 * generate HTML for a particular column in a particular row, given a
 * dataset containing data for the row.  In addition, a Column object can
 * generate HTML for the column header.
 *
 */
public class Column implements Formatter {
    // The following variables hold copies of constructor arguments;
    // see the constructors for details.
    protected String label;
    protected String template = null;
    protected Formatter formatter = null;

    /**
     * Construct a Column that expands a template and displays the
     * result.
     * @param label                Identifying string for this column;
     *                             displayed in the header row.
     * @param template             Template that is expanded in the
     *                             context of a row's data to generate
     *                             HTML for this column.
     */
    public Column(String label, String template) {
        this.label = label;
        this.template = template;
    }

    /**
     * Construct a Column that uses a given Formatter to display
     * the information in each row.
     * @param label                Identifying string for this column;
     *                             displayed in the header row.
     * @param formatter            Invoked to generate the HTML for
     *                             this column in each row of the table.
     */
    public Column(String label, Formatter formatter) {
        this.label = label;
        this.formatter = formatter;
    }

    /**
     * Generate HTML for the column header (everything that goes inside
     * the {@code <td>} element).
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param out                  HTML gets appended here.
     */
    public void headerHtml(ClientRequest cr, StringBuilder out) {
        Html.escapeHtmlChars(label, out);
    }

    /**
     * Generate HTML for this column in a particular row (everything that
     * goes inside the {@code <td>} element).
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param rowData              Data available for this row (typically
 *                                 includes row-specific data with a chain to
 *                                 the main dataset for {@code cr}.
     * @param out                  HTML gets appended here.
     */
    public void html(ClientRequest cr, Dataset rowData, StringBuilder out) {
        if (formatter != null) {
            formatter.html(cr, rowData, out);
        } else {
            Template.expand(template, rowData, out);
        }
    }
}
