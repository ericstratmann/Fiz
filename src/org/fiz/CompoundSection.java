package org.fiz;

/**
 * A CompoundSection is a Section that contains one or more other Sections.
 * CompoundSections support the following constructor properties:
 *   background:     (optional) Specifies a background color to use for
 *                   the interior of this section (everything inside the
 *                   border).  Defaults to transparent.
 *   borderBase:     (optional) Specifies the base name for a collection of
 *                   images that will be used to display a border around this
 *                   section.  If this option has the value {@code x}, then
 *                   there must exist images named {@code x-nw.gif},
 *                   {@code x-n.gif}, {@code x-ne.gif}, {@code x-e.gif},
 *                   {@code x-se.gif}, {@code x-s.gif}, {@code x-sw.gif},
 *                   and {@code x-w.gif}; {@code x-nw.gif} displays the
 *                   upper left corner of the border, {@code x-n.gif} will be
 *                   stretched to cover the top of the border, and so on.
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML table or div that contains the CompoundSection.
 *   id:             (optional)  Used as the {@code id} attribute for the
 *                   HTML table or div that contains the CompoundSection.
 *                   Used to find the section in Javascript, e.g. to make it
 *                   visible or invisible.  Must be unique among all id's
 *                   for the page.
 */
public class CompoundSection implements Section {
    // The following variables are copies of the constructor arguments by
    // the same names.  See the constructor documentation for details.
    protected Dataset properties;
    protected Section[] children;

    /**
     * Construct a CompoundSection.
     * @param properties           Contains configuration information for
     *                             the section.
     * @param children             Any number of Sections, which will be
     *                             displayed inside this section
     */
    public CompoundSection(Dataset properties, Section ... children) {
        this.properties = properties;
        this.children = children;
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
        // If there is a border for this section, then the section gets
        // rendered as a 3x3 table, with the outer cells containing the
        // border and the inner cell containing the children's sections.
        // If there is no border then the section is rendered in a <div>.

        // Render the portion of the container that comes before the children.
        StringBuilder out = cr.getHtml().getBody();
        Template.expand("\n<!-- Start CompoundSection {{@id}} -->\n",
                properties, out);
        String borderBase = properties.check("borderBase");
        if (borderBase != null) {
            Template.expand("<table {{id=\"@id\"}} {{class=\"@class\"}} " +
                    "cellspacing=\"0\">\n" +
                    "  <tr style=\"line-height: 0px;\">\n" +
                    "    <td><img src=\"@borderBase-nw.gif\" alt=\"\" />" +
                    "</td>\n" +
                    "    <td style=\"background-image: " +
                    "url(@borderBase-n.gif); background-repeat: repeat-x;\">" +
                    "</td>\n" +
                    "    <td><img src=\"@borderBase-ne.gif\" alt=\"\" />" +
                    "</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"background-image: " +
                    "url(@borderBase-w.gif); background-repeat: repeat-y;\">" +
                    "</td>\n" +
                    "    <td class=\"compoundBody\" " +
                    "{{style=\"background: @background;\"}}>\n",
                    properties, out);
        } else {
            Template.expand("<div {{id=\"@id\"}} {{class=\"@class\"}} " +
                    "{{style=\"background: @background;\"}}>\n",
                    properties, out);
        }

        // Give the children a chance to render themselves.
        for (Section child: children) {
            child.html(cr);
        }

        // Render the portion of the container that comes after the children.
        if (borderBase != null) {
            Template.expand("    </td>\n" +
                    "    <td style=\"background-image: " +
                    "url(@borderBase-e.gif); background-repeat: repeat-y;\">" +
                    "</td>\n" +
                    "  </tr>\n" +
                    "  <tr style=\"line-height: 0px;\">\n" +
                    "    <td><img src=\"@borderBase-sw.gif\" alt=\"\" />" +
                    "</td>\n" +
                    "    <td style=\"background-image: " +
                    "url(@borderBase-s.gif); background-repeat: repeat-x;\">" +
                    "</td>\n" +
                    "    <td><img src=\"@borderBase-se.gif\" alt=\"\" />" +
                    "</td>\n" +
                    "  </tr>\n" +
                    "</table>\n", properties, out);
        } else {
            out.append("</div>\n");
        }
        Template.expand("<!-- End CompoundSection {{@id}} -->\n",
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
        // No data is needed for this section, but we need to give each
        // of our children a chance to register its requests.
        for (Section child: children) {
            child.registerRequests(cr);
        }
    }
}
