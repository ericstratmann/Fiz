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
    protected String id;

    /**
     * Construct a simple Column that displays a particular field
     * in textual form.
     * @param label                Identifying string for this column;
     *                             displayed in the header row.
     * @param id                   Name of the data field to display in this
     *                             column.  For each row, the field by
     *                             this name is extracted from the record
     *                             for the row and displayed as text
     *                             in this column.
     */
    public Column(String label, String id) {
        this.label = label;
        this.id = id;
    }

    /**
     * Generate HTML for the column header (everything that goes inside
     * the {@code <td>} element).
     * @param out                  HTML gets appended here.
     * @param clientRequest        Provides information about the overall
     *                             request being processed, in case it is
     *                             needed.
     */
    public void headerHtml(StringBuilder out, ClientRequest clientRequest) {
        Html.escapeHtmlChars(label, out);
    }

    /**
     * Generate HTML for this column in a particular row (everything that
     * goes inside the {@code <td>} element).
     * @param rowData              Data available for this row (typically
     *                             includes row-specific data with a chain to
     *                             the main dataset for {@code clientRequest}.
     * @param out                  HTML gets appended here
     * @param clientRequest        Provides information about the overall
     *                             request being processed, in case it is
     *                             needed.
     */
    public void html(Dataset rowData, StringBuilder out,
            ClientRequest clientRequest) {
        Html.escapeHtmlChars(rowData.get(id), out);
    }
}
