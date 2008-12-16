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

    // The following variable refers to the data request for this section,
    // or null if there is none.

    protected DataRequest dataRequest = null;

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
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code cr.registerDataRequest} for each of the
     * DataRequests needed by this section to gather data to be displayed.
     * This default implementation checks for a {@code request} configuration
     * property and uses it to create a request, if it exists.  A pointer
     * to the request is stored in the {@code dataRequest} member of the
     * Section object.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void registerRequests(ClientRequest cr) {
        if (properties != null) {
            dataRequest = cr.registerDataRequest(properties, "request");
        }
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and append it to the Html
     * object associated with {@code request}.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML should get appended to
     *                             {@code request.getHtml()}.
     */
    public abstract void html(ClientRequest cr);
}
