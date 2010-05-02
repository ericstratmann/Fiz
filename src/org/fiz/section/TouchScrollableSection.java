/* Copyright (c) 2010 Stanford University
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

package org.fiz.section;

import org.fiz.*;

/**
 * A TouchScrollableSection is designed to be used for designing web
 * applications on a phone with a touch screen. This section creates a
 * scrollable area which forms the main body of the application.
 * This may contain other sections inside it. A TouchScrollableSection supports
 * the following constructor properties:
 *
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML div that acts the inner (scrollable) part for
 *                   this section. The outer {@code div} contaning this
 *                   section is automatically given a {@code class} attribute
 *                   equal to this value + "Container".
 *                   Defaults to {@code touchScrollableSection}. Thus, the outer
 *                   div has a default class
 *                   {@code touchScrollableSectionContainer}
 *                   Any change to this property will over-ride the default CSS
 *                   for this section.
 *
 *   id:             (optional) Used as the {@code id} attribute for
 *                   the HTML div that acts the inner (scrollable) part for
 *                   this section. The outer {@code div} contaning this
 *                   section is automatically given an {@code id} attribute
 *                   equal to this value + "Container".
 *                   Defaults to {@code touchScrollableSectionContent} + a
 *                   unique integer, eg.: {@code touchScrollableSectionContent1}.
 *                   Thus, the outer div has a default id
 *                   {@code touchScrollableSectionContent1Container}
 */

public class TouchScrollableSection extends Section{

    protected Section[] children;

    /**
     * Construct a TouchScrollableSection.
     * @param properties           Contains configuration information for
     *                             the section.
     * @param children             Any number of Sections, which will be
     *                             displayed inside this section
     */
    public TouchScrollableSection(Dataset properties, Section ... children) {
        this.properties = properties;
        this.children = children;
    }

    @Override
    public void render(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();

        //Choose CSS and JS files depending upon the device
        String device = (String) cr.getMainDataset().checkString("device");
        if (device == null){
            device = "Default";
        }

        String jsFile = "static/fiz/" + device + "TouchScrollableSection.js";
        html.includeJsFile(jsFile);

        String cssFile = device + "TouchScrollableSection.css";
        String sectionClass = properties.checkString("class");
        if (sectionClass == null) {
            sectionClass = "touchScrollableSection";
            html.includeCssFile(cssFile);
        }

        String sectionId = properties.checkString("id");
        if (sectionId == null) {
            sectionId = cr.uniqueId("touchScrollableSectionContent");
        }

        cr.evalJavascript("new Fiz." + device +
                "TouchScrollableSection(\"" + sectionId + "Container\"," +
                " \"" + sectionId + "\");\n");

     // Render the portion of this section that comes before the children.
        Template.appendHtml(out,
                "\n<!-- Start TouchScrollableSection -->\n" +
                "<div id=\"" + sectionId + "Container\" " +
                    "class=\"" + sectionClass + "Container\">\n" +
                "  <div id=\"" + sectionId + "\" " +
                    "class=\"" + sectionClass + "\">\n");

        // Render the children...
        for (Section child: children) {
            child.render(cr);
        }

        //End section..
        out.append("  </div>\n</div>\n" +
                "<!-- End TouchScrollableSection -->\n");
    }
}