package org.fiz;

/**
 * Sections are the basic units of Web pages.  A Web page consists of one
 * or more Sections.  For example, one Section might display a table and
 * another a form.  It is also possible for a Section to contain multiple
 * nested Sections.
 * <p>
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
            String result = properties.check("id");
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
            String result = properties.check("id");
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
     * object associated with {@code request}.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML should get appended to
     *                             {@code request.getHtml()}.
     */
    public abstract void html(ClientRequest cr);
}
