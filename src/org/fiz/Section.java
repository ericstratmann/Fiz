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
 *
 * Sections are invoked to render a page in three phases.  In the first
 * phase each section indicates the data it needs to render itself by
 * specifying one or more DataRequests.  In the second phase all of the
 * requests are processed, potentially in parallel; this happens
 * transparently to the Sections.  In the third phase each Section
 * is asked to generate HTML for itself, using the results of the
 * DataRequests.
 */

public abstract class Section {
    // The following variable contains overall configuration properties
    // for the Section.  It is set by the Section constructor, and may
    // be null.
    protected Dataset properties = null;

    /**
     * This method is invoked during the first phase of rendering a page,
     * in case the Section needs to create custom requests of its own (as
     * opposed to requests already provided for it by the Interactor).
     * If so, this method creates the requests and passes them to
     * {@code cr.addDataRequest}.  This method provides a default
     * implementation that does nothing, which is appropriate for most
     * Sections.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void addDataRequests(ClientRequest cr) {
        // By default, do nothing.
    }

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
    public abstract void render(ClientRequest cr);
}
