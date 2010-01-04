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
 * Sections are the basic units of Web pages.  A Web page consists of one
 * or more Sections.  For example, one Section might display a table and
 * another a form.  It is also possible for a Section to contain multiple
 * nested Sections.
 */

public abstract class Section {
    // The following variable contains overall configuration properties
    // for the Section.  It is set by the Section constructor, and may
    // be null.
    public Dataset properties = null;

    /**
     * Return the {@code id} property for this section, or null if no
     * such property exists.
     * @return                     See above.
     */
    public String checkId() {
        if (properties != null) {
            String result = properties.checkString("id");
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Return the {@code id} property for this section, or throw an error
     * if it doesn't exist.
     * @return                     The value of the {@code id} configuration
     *                             property for the section.
     */
    public String getId() {
        if (properties != null) {
            String result = properties.checkString("id");
            if (result != null) {
                return result;
            }
        }
        throw new InternalError(this.getClass().getSimpleName() +
                " object has no id");
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and appends it to the Html
     * object associated with {@code request}.  It may also generate
     * Javascript and other information related to this section.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML, Javascript,
     *                             etc. should get added to
     *                             {@code request.getHtml()}.
     */
    public void render(ClientRequest cr) {
        render(cr, cr.getMainDataset());
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and appends it to the Html
     * object associated with {@code request}.  It may also generate
     * Javascript and other information related to this section.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML, Javascript,
     *                             etc. should get added to
     *                             {@code request.getHtml()}.
     * @pararm data                Data available for use in generating HTML.
     */
    public void render(ClientRequest cr, Dataset data) {
        render(cr);
    }
}
