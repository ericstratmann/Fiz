package org.fiz;

import java.util.*;

/**
 * A TreeSection displays data in a hierarchical form where subtrees
 * can be expanded or unexpanded by the user, and indentation and
 * graphics are used to show the nesting structure.  TreeSections
 * support the following constructor properties:
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML element that contains the TreeSection.  Defaults
 *                   to {@code TreeSection}.
 *   edgeStyle:      (optional) Determines how to display the lines along
 *                   the left edge of the tree, which connect parents to their
 *                   children.  The value forms the base name for a family
 *                   of images.  If the value is {@code x}, then there
 *                   must exist images {@code x-line.gif}, {@code x-leaf.gif},
 *                   {@code x-plus.gif}, and {@code x-minus.gif}.  See the
 *                   images themselves and the generated HTML for details
 *                   on how the images are used.  Fiz has built-in support
 *                   for styles {@code treeSolid}, {@code treeDotted}, and
 *                   {@code treeNoLines}, but applications can define
 *                   additional styles.  Defaults to {@code treeSolid}.
 *   id:             (required) Used as the {@code id} attribute for the
 *                   HTML element that contains the section.  Must be
 *                   unique among all id's for the page.
 *   leafStyle:      (optional) Style to use for displaying leaves of the
 *                   tree (which cannot be expanded); overridden by a
 *                   {@code style} value in records returned by
 *                   {@code request}.  Defaults to {@code leaf}.
 *   nodeStyle:      (optional) Style to use for displaying nodes that
 *                   have children and hence can be expanded; overridden
 *                   by a {@code style} value in records returned by
 *                   {@code request}.  Defaults to {@code node}.
 *   request:        (required) Specifies a DataRequest that will return
 *                   information about the tree.  The constructor value
 *                   can be either the name of a template in the
 *                   {@code dataRequests} configuration dataset or a nested
 *                   dataset containing the request's arguments directly.
 *                   The values expected in the request's response are
 *                   described below.  This request will be used as-is to
 *                   fetch the top level of the tree, which is displayed
 *                   initially.  When a node in the tree is expanded, the same
 *                   request is used to fetch the children of the node, except
 *                   that the {@code name} parameter in the request is set to
 *                   the {@code name} value for the node.
 *
 * The response to {@code request} consists of a dataset with a {@code record}
 * child for each node at the current level.  The TreeSection will use the
 * following elements, if they are present in a record:
 *   expandable      A value of 1 means this node can be expanded (it has
 *                   children).  If this value is absent, or has any value
 *                   other than 1, then this is a leaf node.
 *   name            Must be provided if the node is expandable; this value
 *                   is used with the {@code request} constructor property
 *                   to fetch the node's children.
 *   style           If present, specifies the style(s) to use for displaying
 *                   this node.  See below for more information on styles.
 * TODO: describe how styles are used.
 */
public class TreeSection implements DirectAjax, Section {
    // The following variables are copies of constructor arguments.  See
    // the constructor documentation for details.
    protected Dataset properties;

    // Source of initial information to display in the section.
    protected DataRequest dataRequest = null;

    /**
     * Construct a TreeSection.
     * @param properties           Contains configuration information
     *                             for the section; see description above.
     */
    public TreeSection(Dataset properties) {
        this.properties = properties;

        // Generate an error if there is no {@code request} property.
        properties.get("request");
    }

    /**
     * This method is an Ajax entry point, invoked to expand an element
     * in a TreeSection.
     * @param cr                   Overall information about the client
     *                             request being serviced;  there must
     *                             be {@code id} and {@code name}
     *                             values in the main dataset, which
     *                             identify the HTML element to be updated
     *                             and the data manager's name for the
     *                             contents of that element.
     */
    public static void ajaxExpand(ClientRequest cr) {
        // Retrieve state information about the row and the overall section.
        Dataset rowInfo = cr.getReminder("TreeSection.row");
        Dataset sectionInfo = cr.getReminder("TreeSection");

        // Invoke a data request to fetch information about the children of
        // the element being expanded, then generate a <table> that will
        // display the children.
        DataRequest request = cr.registerDataRequest(sectionInfo, "request");
        request.addParameter("name", rowInfo.get("name"));

        StringBuilder html = new StringBuilder();
        String id = rowInfo.get("id");
        Template.expand("<table cellspacing=\"0\" " +
                "class=\"@class?{TreeSection}\" " +
                "id=\"@1\">\n", sectionInfo, html, id);
        childrenHtml(cr, sectionInfo,
                request.getResponseOrAbort().getChildren("record"),
                id, html);
        html.append("</table>\n");

        // Generate a Javascript method invocation for the browser to
        // invoke, which will instantiate the HTML.
        StringBuilder javascript = new StringBuilder(html.length() + 50);
        Template.expand("Fiz.ids[\"@1\"].expand(\"@2\");", javascript,
                Template.SpecialChars.JAVASCRIPT, id, html);
        cr.ajaxEvalAction(javascript);
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
        String id = properties.get("id");
        Reminder reminder = new Reminder(id, "TreeSection");
        reminder.addFromDataset(properties, "class", "edgeStyle", "id",
                "leafStyle", "nodeStyle", "request");
        reminder.flush(cr);
        if (!properties.containsKey("class")) {
            html.includeCssFile("TreeSection.css");
        }
        html.includeJsFile("fizlib/TreeRow.js");
        Template.expand("\n<!-- Start TreeSection @id -->\n" +
                "<table cellspacing=\"0\" class=\"@class?{TreeSection}\" " +
                "id=\"@id\">\n", properties, out);
        childrenHtml(cr, properties,
                dataRequest.getResponseOrAbort().getChildren("record"),
                properties.get("id"), out);
        out.append("</table>\n" +
                "<!-- End TreeSection @id -->\n");
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
     * This method generates rows for an HTML table that will display all
     * of the children of a particular node.  It is invoked both from the
     * {@code html} method and from {@code ajaxExpand}, which is a static
     * method.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML will be
     *                             appended to {@code cr.getHtml()}.
     * @param properties           Dataset containing configuration
     *                             information for this section.
     * @param children             Contains one dataset for each child
     *                             to display, in the order they should be
     *                             displayed.
     * @param baseId               Used to generate ids for the rows of
     *                             the table; rows will have ids
     *                             {@code baseId.0}, {@code baseId.1}, etc.
     * @param out                  Generated HTML is appended here.  Only
     *                             {@code <tr>} elements are generated here;
     *                             the {@code <table>} and {@code </table>}
     *                             elements must be generated by the caller.
     */
    protected static void childrenHtml(ClientRequest cr, Dataset properties,
            ArrayList<Dataset> children, String baseId, StringBuilder out) {
        // The following variable is used to generate an alternate (expanded)
        // style for each row.  It is declared here so that it can be reused
        // for each of the rows (minimize garbage collection).
        StringBuilder expandedRow = new StringBuilder();

        String edgeStyle = properties.check("edgeStyle");
        if (edgeStyle == null) {
            edgeStyle = "treeSolid";
        }
        for (int i = 0; i < children.size(); i++) {
            Dataset child = children.get(i);
            Boolean lastElement = (i == (children.size()-1));
            String rowId = baseId + "_" + i;

            // Is this child a node (expandable) or a leaf?
            Boolean expandable = false;
            String value = child.check("expandable");
            if ((value != null) && (value.equals("1"))) {
                expandable = true;
            }

            // Compute the style to use for this child.
            String style = child.check("style");
            if (style == null) {
                style = (expandable ? properties.check("leafStyle")
                        : properties.check("nodeStyle"));
                if (style == null) {
                    style = expandable ? "node" : "leaf";
                }
            }

            // Each child is drawn as a table row with two cells.  The
            // left cell contains lines connecting all of the children
            // together vertically (if desired) and a box displaying plus
            // or minus for nodes (initially plus).  The right cell displays
            // the icon(s) and name for this child (or whatever is specified
            // by the template for this child's style).
            int rowStart = out.length();
            Template.expand("  <tr id=\"@1\">\n", out, rowId);
            String repeat = "repeat-y";
            if (lastElement) {
                // Don't extend the vertical line for the last element:
                // it should have an "L" shape.
                repeat = "no-repeat";
            }
            Template.expand("    <td class=\"left\" " +
                    "style=\"background-image: url(/fizlib/images/" +
                    "@1-line.gif); background-repeat: @2;\"",
                    out, edgeStyle, repeat);
            int midPoint = out.length();

            // Now display one of 2 images in the left cell: a plus if the
            // element is expandable, or a horizontal line if this is a
            // leaf node.
            Template.expand(" onclick=\"@1\"><img src=" +
                    "\"/fizlib/images/@2-@3.gif\"></td>\n",
                    out, Ajax.invoke(cr, "/fiz/TreeSection/ajaxExpand", null,
                    properties.get("id"), rowId), edgeStyle,
                    (expandable ? "plus": "leaf"));

            // Render the cell on the right, using a template selected by
            // the style.
            // TODO: figure out consistent naming scheme for configuration files: use class names verbatim?
            out.append("    <td class=\"right\">");
            Template.expand(Config.get("treeSection", style), child, out);
            out.append("    </td>\n");
            out.append("  </tr>\n");

            // If this element is expandable then do some additional things
            // to support expansion and unexpansion of this element.
            if (expandable) {
                // Create a reminder with information we will need during Ajax
                // requests to expand this child.
                Reminder reminder = new Reminder(rowId, "TreeSection.row",
                        "id", rowId, "name", child.get("name"));
                reminder.flush(cr);

                // Next, create a Javascript object holding two copies of
                // the HTML for the table row: the one we just rendered (used
                // when the element is unexpanded), and an alternate version
                // to use when the element is expanded.
                expandedRow.setLength(0);
                expandedRow.append(out.substring(rowStart, midPoint));
                Template.expand(" onclick=\"Fiz.ids['@1'].unexpand();\">" +
                        "<img src=\"/fizlib/images/@2-minus.gif\"></td>\n",
                        expandedRow, rowId, edgeStyle);
                expandedRow.append("    <td class=\"right\">");
                Template.expand(Config.get("treeSection", style + "-expanded"),
                        child, expandedRow);
                expandedRow.append("    </td>\n");
                expandedRow.append("  </tr>\n");
                cr.includeJavascript(Template.expand("Fiz.ids[\"@1\"] = " +
                        "new Fiz.TreeRow(\"@1\", \"@2\", \"@3\");\n",
                        Template.SpecialChars.JAVASCRIPT, rowId,
                        out.substring(rowStart), expandedRow));

                // Finally, add an additional row to the table we are
                // generating, which will hold the children of this element
                // if/when the element is expanded.  For now, this row is
                // invisible.  We add this row now because we have information
                // now that is needed to generate the row (lastEement), and
                // that information won't be readily available when it's
                // time to expand the element.
                Template.expand("  <tr id=\"@(1)_childRow\" " +
                        "style=\"display:none\">\n", out, rowId);
                if (!lastElement) {
                    Template.expand("    <td style=\"background-image: " +
                            "url(/fizlib/images/@1-line.gif); " +
                            "background-repeat: repeat-y;\">",
                            out, edgeStyle);
                } else {
                    out.append("    <td>");
                }
                Template.expand("</td>\n    <td><div class=\"nested\" " +
                        "id=\"@(1)_childDiv\"></div></td>\n", out, rowId);
                out.append("  </tr>\n");
            }
        }
    }
}
