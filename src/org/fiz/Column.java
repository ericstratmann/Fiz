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

/**
 * A Column object represents one column in a TableSection.  This class
 * implements a few common forms of columns; subclasses implement additional
 * forms.  A Column implements the Section interface, which knows how to
 * generate HTML for a particular column in a particular row, given a
 * dataset containing data for the row.  In addition, a Column object can
 * generate HTML for the column header.
 *
 */
public class Column extends Section {
    // The following variables hold copies of constructor arguments;
    // see the constructors for details.
    protected String label;
    protected String template = null;
    protected Section section = null;

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
     * Construct a Column that uses a given Section to display
     * the information in each row.
     * @param label                Identifying string for this column;
     *                             displayed in the header row.
     * @param section            Invoked to generate the HTML for
     *                             this column in each row of the table.
     */
    public Column(String label, Section section) {
        this.label = label;
        this.section = section;
    }

    /**
     * Generate HTML for this column in a particular row (everything that
     * goes inside the {@code <td>} element).
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param rowData              Data available for this row (typically
     *                             a compound dataset that includes
     *                             row-specific data and the main dataset
     *                             for {@code cr}).
     */
    public void render(ClientRequest cr, Dataset rowData) {
        StringBuilder out = cr.getHtml().getBody();
        if (section != null) {
            section.render(cr, rowData);
        } else {
            Template.appendHtml(out, template, rowData);
        }
    }

    /**
     * Generate HTML for the column header (everything that goes inside
     * the {@code <td>} element).
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void renderHeader(ClientRequest cr) {
        Html.escapeHtmlChars(label, cr.getHtml().getBody());
    }
}
