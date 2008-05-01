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

public interface Section {
    /**
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code clientRequest.registerDataRequest} for each of the
     * DataRequests needed by this section to gather data to be displayed.
     * @param clientRequest        Information about the request being
     *                             processed.
     */
    public void registerRequests(ClientRequest clientRequest);

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and append it to the Html
     * object associated with {@code request}.
     * @param clientRequest        Information about the request being
     *                             processed; HTML should get appended to
     *                             {@code request.getHtml()}.
     */
    public void html(ClientRequest clientRequest);
}
