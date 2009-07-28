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
 * A TabSection displays a horizontal array of notebook-style tabs, of
 * which one is selected (and hence displayed slightly differently) at
 * any given time.  Clicking on an unselected tab causes a different
 * page to be displayed (presumably the the one for that tab) or causes
 * an Ajax request to be invoked.  A TabSection supports the following
 * constructor properties, which apply to the entire section:
 *   class:          (optional) Class attribute to use for the {@code <table>}
 *                   element containing the section.  Defaults to
 *                   {@code TabSection}.
 *   id:             (optional) Used as the {@code id} attribute for the
 *                   {@code <table>} element containing the section.  Must
 *                   be unique in the page; defaults to {@code tabs}.
 *   request:        (optional) This property is the name of a request
 *                   registered by the caller with ClientRequest.addDataRequest.
 *                   If this property is specified than the data from its
 *                   response is used for expanding templates in the individual
 *                   tabs.
 *   selector:       (optional) Name of an entry in the main dataset, whose
 *                   value identifies the selected tab by giving its
 *                   {@code id}.  Defaults to {@code currentTabId}.
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
 *
 * TabSection automatically sets the following {@code class} attributes
 * for use in CSS:
 *   left:           A {@code <td>} element containing the left portion of
 *                   each non-selected tab.
 *   mid:            A {@code <td>} element containing the middle portion of
 *                   each non-selected tab.
 *   right:          A {@code <td>} element containing the right portion of
 *                   each non-selected tab.
 *   leftSelected:   A {@code <td>} element containing the left portion of
 *                   the selected tab.
 *   midSelected:    A {@code <td>} element containing the middle portion of
 *                   the selected tab.
 *   rightSelected:  A {@code <td>} element containing the right portion of
 *                   the selected tab.
 *   spacer:         A {@code <td>} element that separates adjacent tabs.
 *   rightSpacer:    A {@code <td>} element to the right of the rightmost tab.
 */
public class TabSection extends Section{
    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Dataset[] tabs;

    // Cached copy of TabSection.css.
    static protected String cssTemplate = null;

    /**
     * Construct a TabSection.
     * @param properties           Contains configuration information that
     *                             applies to the entire section.
     * @param tabs                 One or more Datasets, each describing a
     *                             single tab.  The tabs will be displayed
     *                             in the order of these arguments.
     */
    public TabSection(Dataset properties, Dataset ... tabs) {
        this.properties = properties;
        this.tabs = tabs;
        if (properties.check("id") == null) {
            properties.set("id", "tabs");
        }
    }

    /**
     * Discard all cached information (such as the CSS template), so that
     * it will be refetched from disk the next time is needed.  Typically
     * invoked during debugging sessions to flush caches on every request.
     */
    public static synchronized void clearCache() {
        cssTemplate = null;
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
    public void render(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();
        StringBuilder javascript = new StringBuilder();
        boolean anyJavascript = false;
        String sectionId = properties.get("id");

        // Get information about which tab is selected.
        String selected = null;
        String selector = properties.check("selector");
        if (selector == null) {
            selector = "currentTabId";
        }
        selected = cr.getMainDataset().check(selector);

        // Collect the data that will be available for templates, including
        // the response to the section's request, if there was one.

        Dataset data;
        String requestName = properties.check("request");
        if (requestName == null) {
            data = cr.getMainDataset();
        } else {
            data = new CompoundDataset(
                    cr.getDataRequest(requestName).getResponseOrAbort(),
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
        Template.appendRaw(expandedCss, getTemplate(), styleData);
        html.includeCss(expandedCss);

        // Generate the HTML for the tabs.
        Template.appendHtml(out, "\n<!-- Start TabSection @id -->\n" +
                "<table id=\"@id\" class=\"@class?{TabSection}\" " +
                "cellspacing=\"0\">\n" +
                "  <tr>\n",
                properties);

        // The tabs are displayed in a single row containing four <td>
        // elements for each: one to create space before the tab, one for
        // the left edge of the tab, one for the main portion of the tab,
        // and one for the right edge of the tab.  There is one additional
        // spacer to the right of all of the tabs.
        for (Dataset tab: tabs) {
            String tabId = tab.get("id");
            String suffix = ((selected != null) && (tabId.equals(selected))) ?
                    "Selected" : "";
            tabId = sectionId + "_" + tabId;

            // IE quirk: IE doesn't display borders around empty cells,
            // which causes problems for the side strips and the spacers.
            // There are ways to force IE to display the borders
            // ("border-collapse: collapse; empty-cells: show;") but these
            // cause other browsers to drop different parts of the borders.
            // So, the code below displays a 1x1 transparent image in each
            // empty cell to force IE to display borders.
            Template.appendHtml(out, "    <td class=\"spacer\">" +
                    "<img src=\"/static/fiz/images/blank.gif\" alt=\"\" /></td>\n" +
                    "    <td class=\"left@1\">" +
                    "<img src=\"/static/fiz/images/blank.gif\" alt=\"\" /></td>\n" +
                    "    <td class=\"mid@1\" id=\"@2\"><a",
                    suffix, tabId);

            // Generate the action for this tab (the contents of the href
            // attribute for the <a> element).
            String url = tab.check("url");
            if (url != null) {
                out.append(" href=\"");
                Template.appendUrl(out, url, data);
                out.append("\"");
            } else {
                if (!anyJavascript) {
                    html.includeJsFile("static/fiz/TabSection.js");
                    anyJavascript = true;
                }
                out.append(" href=\"#\" onclick=\"");
                javascript.setLength(0);

                // For Javascript and AJAX actions we need to call
                // Javascript code to modify the DOM so that the new selected
                // tab will appear selected (no need for this in the URL case
                // because an entirely new page will be displayed).
                Template.appendJavascript(javascript, "Fiz.TabSection.selectTab(\"@1\"); ",
                        tabId);
                String ajaxUrl = tab.check("ajaxUrl");
                if (ajaxUrl != null) {
                    // AJAX action.
                    Ajax.invoke(cr, ajaxUrl, data, javascript);
                } else {
                    // Javascript action.
                    String jsTemplate = tab.check("javascript");
                    if (jsTemplate != null) {
                        Template.appendJavascript(javascript, jsTemplate, data);
                    }
                }
                Html.escapeHtmlChars(javascript, out);
                out.append("; return false;\"");
            }

            // Generate the rest of the <td> for this tab.  Browser quirk:
            // the text needs to be enclosed in an extra <div>; otherwise
            // padding specified for it gets lost.
            out.append("><div>");
            Template.appendHtml(out, tab.get("text"), data);
            Template.appendHtml(out, "</div></a></td>\n" +
                    "    <td class=\"right@1\">" +
                    "<img src=\"/static/fiz/images/blank.gif\" alt=\"\" /></td>\n",
                    suffix);
        }
        Template.appendHtml(out, "    <td class=\"rightSpacer\">" +
                "<img src=\"/static/fiz/images/blank.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TabSection @id -->\n",
                properties);
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
