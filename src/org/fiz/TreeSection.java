package org.fiz;

import java.util.*;

/**
 * A TreeSection displays data in a hierarchical form where subtrees
 * can be expanded or unexpanded by the user, and indentation and
 * graphics are used to show the nesting structure.  TreeSections
 * support the following constructor properties:
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML element that contains the TreeSection.  Defaults
 *                   two {@code TreeSection}.
 *   edgeStyle:      (optional) Determines how to display the lines along
 *                   the left edge of the tree, which connect parents to their
 *                   children.  The value forms the base name for a family
 *                   of images.  If the value is {@code x}, then there
 *                   must exist images {@code x-line.gif}, {@code x-last.gif},
 *                   {@code x-plus.gif}, {@code x-minus.gif}, and
 *                   {@code x-leaf.gif}.  See the images themselves and
 *                   the generated HTML for details on how the images are
 *                   used.  Fiz has built-in support for styles
 *                   {@code treeSolid}, {@code treeDotted}, and
 *                   {@code treeNoLines}, but applications can define
 *                   additional styles.  Defaults to {@code treeSolid}.
 *   id:             (required) Used as the {@code id} attribute for the
 *                   HTML element that contains the section.  Must be
 *                   unique among all id's for the page.
 *   leafStyle:      (optional) Style to use for displaying leaves of the
 *                   tree (which cannot be expanded); overridden by a
 *                   {@code style} value in records returned by
 *                   {@code requests}.  Defaults to {@code leaf}.
 *   nodeStyle:      (optional) Style to use for displaying nodes that
 *                   have children and hence can be expanded; overridden
 *                   by a {@code style} value in records returned by
 *                   {@code requests}.  Defaults to {@code node}.
 *   request:        (required) Specifies a DataRequest that will return
 *                   information about the children of a node in the tree.
 *                   The constructor value can be either the name of a
 *                   request in the {@code dataRequests} configuration
 *                   dataset or a nested dataset containing the request's
 *                   arguments directly.  Before invoking the request,
 *                   this class will add an additional argument {@code name}
 *                   containing the name of the node whose children are needed
 *                   (a name of "" refers to the root node of the tree).
 *                   TODO: document values expected in the result.
 */
public class TreeSection implements Section {
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
        Dataset mainDataset = cr.getMainDataset();
        String edgeStyle = properties.check("edgeStyle");
        if (edgeStyle == null) {
            edgeStyle = "treeSolid";
        }
        if (!properties.containsKey("class")) {
            html.includeCssFile("TreeSection.css");
        }
        Template.expand("\n<!-- Start TreeSection @id -->\n" +
                "<table cellspacing=\"0\" class=\"@class?{TreeSection}\" " +
                "id=\"@id\">\n", properties, out);
        childrenHtml(cr,
                dataRequest.getResponseOrAbort().getChildren("record"),
                properties.get("id"), edgeStyle, out);
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
     * of the children of a particular node.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML will be
     *                             appended to {@code cr.getHtml()}.
     * @param children             Contains one dataset for each child
     *                             to display, in the order they should be
     *                             displayed.
     * @param baseId               Used to generate ids for the rows of
     *                             the table; rows will have ids
     *                             {@code baseId.0}, {@code baseId.1}, etc.
     * @param edgeStyle            Used to select a family of images for
     *                             displaying the lines along the left
     *                             edge of the tree.
     * @param out                  Generated HTML is appended here.  Only
     *                             {@code <tr>} elements are generated here;
     *                             the {@code <table>} and {@code </table>}
     *                             elements must be generated by the caller.
     */
    protected void childrenHtml(ClientRequest cr, ArrayList<Dataset> children,
            String baseId, String edgeStyle, StringBuilder out) {
        for (int i = 0; i < children.size(); i++) {
            Dataset child = children.get(i);

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
            // together (if desired) and a box displaying plus or minus
            // for nodes.  The right cell displays icons and name for this
            // child (or whatever it is specified by the template for
            // this child's style).
            Template.expand("  <tr id=\"@1.@2\">\n", out, baseId, i);
            if (i == (children.size()-1)) {
                // Last child: display an L-shaped line as the background
                // for the cell.
                Template.expand("    <td class=\"left\" " +
                        "style=\"background-image: url(/fizlib/images" +
                        "/@1-last.gif); background-repeat: no-repeat;\"",
                        out, edgeStyle);
            } else {
                // Not the last child: display a vertical line, stretching
                // to cover the cell from top to bottom.
                Template.expand("    <td class=\"left\" " +
                        "style=\"background-image: url(/fizlib/images/" +
                        "@1-line.gif); background-repeat: repeat-y;\"",
                        out, edgeStyle);
            }

            // Now display one of 2 images in the left cell: a plus if the
            // cell is expandable, or an empty image otherwise (needed to
            // set the size for the cell).
            if (expandable) {
                Template.expand(" onclick=\"@1\"><img src=" +
                        "\"/fizlib/images/@2-plus.gif\"></td>\n",
                        out, Ajax.invoke(cr, "ajaxTreeUpdate?name=@name",
                        child), edgeStyle);
            } else {
                Template.expand("><img src=\"/fizlib/images/@1-leaf.gif\">" +
                        "</td>\n",
                        out, edgeStyle);
            }

            // Render the cell on the right, using a template selected by
            // the style.
            // TODO: figure out consistent naming scheme for configuration files: use class names verbatim?
            out.append("    <td class=\"right\">");
            Template.expand(Config.get("treeSection", style), child, out);
            out.append("    </td>\n");
            out.append("  </tr>\n");
        }
    }
}
