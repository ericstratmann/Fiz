package org.fiz;

/**
 * A TemplateSection is a simple form of Section that generates HTML from
 * a single template.  TemplateSections support the following constructor
 * properties:
 * request:          (optional) Name of a DataRequest that will supply
 *                   data for use by {@code template}.
 * template:         (required) Template that will generate HTML for the
 *                   section.  If {@code request} is specified then the
 *                   template is expanded in the context of the response
 *                   to that request (chained to the main dataset);
 *                   Otherwise the template is expanded in the context of
 *                   the main dataset.
 */
public class TemplateSection implements Section {
    // The following variables hold values for the properties that define
    // the section; see above for definitions.
    protected String request = null;
    protected String template;

    // Source of data for the section:
    protected DataRequest dataRequest = null;

    /**
     * Construct a TemplateSection from a dataset containing properties.
     * @param properties           Contains configuration information
     *                             for the section; see description above.
     */
    public TemplateSection(Dataset properties) {
        request = properties.check("request");
        template = properties.get("template");
    }

    /**
     * Construct a TemplateSection from a template string.  The section
     * will not issue any data requests.
     * @param template             Value of the {@code template} property for
     *                             the section.
     */
    public TemplateSection(String template) {
        this.template = template;
    }

    /**
     * Construct a TemplateSection given values for the {@code template} and
     * {@code request} properties.
     * @param request              Value of the {@code request} property for
     *                             the section.
     * @param template             Value of the {@code template} property for
     *                             the section.
     */
    public TemplateSection(String request, String template) {
        this.request = request;
        this.template = template;
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and appends it to the Html
     * object associated with {@code cr}.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML will be
     *                             appended to {@code cr.getHtml()}.
     */
    @Override
    public void html(ClientRequest cr) {
        Dataset data;
        if (dataRequest != null) {
            // TODO: must handle errors.
            data = dataRequest.getResponseData();
            data.setChain(cr.getMainDataset());
        } else {
            data = cr.getMainDataset();
        }
        Template.expand(template, data, cr.getHtml().getBody());
    }

    /**
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code cr.registerDataRequest} for the
     * DataRequest needed by this section (if any).
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    @Override
    public void registerRequests(ClientRequest cr) {
        if (request != null) {
            dataRequest = cr.registerDataRequest(request);
        }
    }
}
