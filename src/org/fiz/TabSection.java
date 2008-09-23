package org.fiz;

/**
 * A TabSection displays a horizontal array of notebook-style tabs, of
 * which one is selected (and hence displayed slightly differently) at
 * any given time.  Clicking on an unselected tab causes a different
 * page to be displayed (presumably the the one for that tab) or causes
 * an Ajax request to be invoked.  A TabSection supports the following
 * constructor properties, which apply to the entire section:
 *   class:          (optional) Class attribute to use for the {@code <table>}
 *                   element containing the section.  Defaults to
 *                   {@code TabSection}.
 *   id:             (required) Used as the {@code id} attribute for the
 *                   {@code <table>} element containing the section.  Must
 *                   be unique in the page.
 *   request:        (optional) This property is either the name of a request
 *                   in the {@code dataRequests} configuration dataset or
 *                   a nested dataset containing arguments for a DataRequest.
 *                   If this property is specified than the data from its
 *                   response is used for expanding templates in the individual
 *                   tabs.
 *   style:          (optional) Selects one of several different ways of
 *                   displaying tabs; must correspond to the name of a
 *                   nested dataset in the {@code tabSections} configuration
 *                   dataset, which contains CSS information for
 *                   displaying tabs of that style.  Defaults to
 *                   {@code tabGray}.  For details on how to define new
 *                   tab styles, see the documentation in the
 *                   {@code tabSections} configuration dataset.
 * Each of the tabs in the section is also described by a Dataset, which
 * contains properties for that tab.  The following tab properties are
 * supported (all templates are expanded using the result of {@code request},
 * if any, plus the main dataset for the request):
 *   id:             (required) Unique identifier for this tab among all the
 *                   tabs in this TabSection; used to identify the selected
 *                   tab, among other things.
 *   text:           (optional) Template for HTML to display inside the tab;
 *                   may be simple text or something more complicated,
 *                   such as an {@code <img>} element.
 *   url:            (optional) Template for URL to display when this tab
 *                   is clicked.
 *   ajaxUrl:        (optional) If this property is specified, clicking on the
 *                   tab will invoke an Ajax request; the value of this
 *                   property is template for the URL of the Ajax request.
 *                   This property is ignored if {@code url} is specified.
 *   javascript:     (optional) Template for Javascript code to invoke when
 *                   the tab is clicked. This property is ignored if
 *                   {@code url} or {@code ajaxUrl} is specified.
 */
public class TabSection implements Section{
    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Dataset properties;
    protected String selected;
    protected Dataset[] tabs;

    // Cached copy of TabSection.css.
    static protected String cssTemplate = null;

    // DataRequest for this section, if there is one.
    protected DataRequest dataRequest = null;

    /**
     * Construct a TabSection.
     * @param properties           Contains configuration information that
     *                             applies to the entire section.
     * @param selected             Identifier for the tab that is selected;
     *                             must match the {@code id} property for one
     *                             of the tabs.
     * @param tabs                 One or more Datasets, each describing a
     *                             single tab.  The tabs will be displayed
     *                             in the order of these arguments.
     */
    public TabSection(Dataset properties, String selected, Dataset ... tabs) {
        this.properties = properties;
        this.selected = selected;
        this.tabs = tabs;
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
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();
        StringBuilder expanded = new StringBuilder();
        boolean anyJavascript = false;
        String sectionId = properties.get("id");

        // Collect the data that will be available for templates, including
        // the response to the section's request, if there was one.

        Dataset data;
        if (dataRequest == null) {
            data = cr.getMainDataset();
        } else {
            data = new CompoundDataset(dataRequest.getResponseOrAbort(),
                    cr.getMainDataset());
        }

        // Use the style information to generate CSS for the tabs.
        StringBuilder expandedCss = new StringBuilder();
        String style = properties.check("style");
        if (style == null) {
            style = "tabGray";
        }
        Dataset styleData = new CompoundDataset(
                Config.getDataset("tabSections").getChild(style),
                properties,
                Config.getDataset("css"));
        Template.expand(getTemplate(), styleData, expandedCss,
                Template.SpecialChars.NONE);
        html.includeCss(expandedCss);

        // Generate the HTML for the tabs.
        Template.expand("\n<!-- Start TabSection @id -->\n" +
                "<table id=\"@id\" class=\"@class?{TabSection}\" " +
                "cellspacing=\"0\">\n" +
                "  <tr>\n",
                properties, out);

        // The tabs are displayed in a single row containing four <td>
        // elements for each: one to create space before the tab, one for
        // the left edge of the tab, one for the main portion of the tab,
        // and one for the right edge of the tab.  There is one additional
        // spacer to the right of all of the tabs.
        for (Dataset tab: tabs) {
            String tabId = tab.get("id");
            String suffix = (tabId.equals(selected)) ?
                    "Selected" : "";
            tabId = sectionId + "." + tabId;

            // IE quirk: IE doesn't display borders around empty cells,
            // which causes problems for the side strips and the spacers.
            // There are ways to force IE to display the borders
            // ("border-collapse: collapse; empty-cells: show;") but these
            // cause other browsers to drop different parts of the borders.
            // So, the code below displays a 1x1 transparent image in each
            // empty cell to force IE to display borders.
            Template.expand("    <td class=\"spacer\">" +
                    "<img src=\"/fizlib/images/blank.gif\" alt=\"\" /></td>\n" +
                    "    <td class=\"left@1\">" +
                    "<img src=\"/fizlib/images/blank.gif\" alt=\"\" /></td>\n" +
                    "    <td class=\"mid@1\" id=\"@2\"><a",
                    out, Template.SpecialChars.NONE, suffix, tabId);

            // Generate the action for this tab (the contents of the href
            // attribute for the <a> element).
            String url = tab.check("url");
            if (url != null) {
                out.append(" href=\"");
                Template.expand(url, data, out, Template.SpecialChars.URL);
                out.append("\"");
            } else {
                if (!anyJavascript) {
                    html.includeJsFile("fizlib/TabSection.js");
                    anyJavascript = true;
                }
                out.append(" href=\"#\" onclick=\"");
                expanded.setLength(0);

                // For Javascript and AJAX actions we need to call
                // Javascript code to modify the DOM so that the new selected
                // will appear selected (no need for this in the URL case
                // because an entirely new page will be displayed).
                Template.expand("Fiz.TabSection.selectTab(\"@1\"); ",
                        expanded, Template.SpecialChars.JAVASCRIPT, tabId);
                Html.escapeHtmlChars(expanded, out);
                String ajaxUrl = tab.check("ajaxUrl");
                if (ajaxUrl != null) {
                    // AJAX action.
                    Ajax.invoke(cr, ajaxUrl, data, out);
                } else {
                    // Javascript action.
                    String javascript = tab.check("javascript");
                    if (javascript != null) {
                        expanded.setLength(0);
                        Template.expand(javascript, data, expanded,
                                Template.SpecialChars.JAVASCRIPT);
                        Html.escapeHtmlChars(expanded, out);
                    }
                }
                out.append("; return false;\"");
            }

            // Generate the rest of the <td> for this tab.  Browser quirk:
            // the text needs to be enclosed in an extra <div>; otherwise
            // padding specified for it gets lost.
            out.append("><div>");
            Template.expand(tab.get("text"), data, out);
            Template.expand("</div></a></td>\n" +
                    "    <td class=\"right@1\">" +
                    "<img src=\"/fizlib/images/blank.gif\" alt=\"\" /></td>\n",
                    out, Template.SpecialChars.NONE, suffix);
        }
        Template.expand("    <td class=\"rightSpacer\">" +
                "<img src=\"/fizlib/images/blank.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TabSection @id -->\n",
                properties, out);
    }

    /**
     * This method is invoked during the first phase of rendering a page;
     * it calls {@code cr.registerDataRequest} for each of the
     * DataRequests needed by this section to gather data to be displayed.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    @Override
    public void registerRequests(ClientRequest cr) {
        dataRequest = cr.registerDataRequest(properties, "request");
    }

    /**
     * Find the template used for generating CSS for TabSections (caches
     * the template file in memory for faster access).
     * @return                     The (unexpanded) contents of
     *                             TabSection.css.
     */
    protected static synchronized String getTemplate() {
        if (cssTemplate == null) {
            cssTemplate = Util.readFileFromPath("TabSection.css", "CSS",
                    Css.getPath()).toString();
        }
        return cssTemplate;
    }

    /**
     * This method explicitly sets the template used for generating CSS
     * for TabSections, overriding the default that comes from a file.
     * Intended primarily for testing.
     * @param template             New template to use for the CSS of all
     *                             future TabSections.  Null resets the
     *                             cached template so that it will be reloaded
     *                             from the standard disk file the next time
     *                             it is needed.
     */
    protected static synchronized void setTemplate(String template) {
        cssTemplate = template;
    }
}
