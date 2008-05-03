package org.fiz;

/**
 * A Column object represents one column in a TableSection.  This class
 * implements a few common forms of columns; subclasses implement additional
 * forms.  A Column's main responsibility is to display all of the elements
 * any given column.  It is invoked once for each row in the table; it
 * extracts information related to the column from a dataset containing
 * information about the entire row and uses that information to generate
 * HTML for the particular column.  A Column object must also generate
 * HTML for the column header.
 */
public class Column {
    // The following variables hold copies of constructor arguments;
    // see the constructors for details.
    protected String label;
    protected String template;

    /**
     * Construct a simple Column that displays a particular field
     * in textual form.
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
        Template.expand(template, rowData, out);
    }
}
