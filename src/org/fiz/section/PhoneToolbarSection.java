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

import java.util.*;

/**
 * A PhoneToolbarSection is designed to be used for designing web 
 * applications on a phone. This section creates a set of buttons at 
 * the bottom of the screen, which can be used to navigate within the
 * application. A PhoneToolbarSection supports the following constructor 
 * properties:
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML div that contains the PhoneToolbarSection.
 *                   Defaults to {@code toolbarSection}.  
 *                   Any change to this property will over-ride the default 
 *                   css for the PhoneToolbarSection.           
 *   id:             (optional) Used as the {@code id} attribute for the
 *                   HTML div that contains the PhoneToolbarSection.
 *                   Defaults to "{@code footer}".        
 *   buttons:        (required) Supplies data for the toolbar. The dataset
 *                   must contain one child for each button of the toolbar. 
 *                   The child must have the following entries: 
 *                   
 *                   -> 'label': The text to display on the
 *                      button
 *                   -> 'image': The image name (without the extension)
 *                      to display above the text on the button. When a button 
 *                      is pressed, the image is changed to another 
 *                      one with the same shape but a different color.
 *                   
 *                   Touching a button can do one of the following three
 *                   things. The presence of these entries is examined in the
 *                   order given below and the first such entry is attached
 *                   to the button as an '{@code href}' or '{@code onclick}'
 *                   handler.
 *
 *                   -> 'url': The link to go to when the button is 
 *                       touched/clicked. 
 *                   -> 'ajaxUrl': If this property is specified, clicking on 
 *                      the button will invoke an Ajax request. This 
 *                      property is ignored if '{@code url}' is specified.
 *                   -> 'javascript': If this property specified, clicking on
 *                      the button will cause the value of this property to be 
 *                      invoked as Javascript code. This property is ignored 
 *                      if '{@code url}' or '{@code ajaxUrl}' are specified.
 *
 *                   To display an image 'x.png' on the button, the dataset 
 *                   must contain the entry 'x' for the 'image' field.
 *                   The images must be kept in that standard images 
 *                   directory of Fiz. For every image 'x.png', there must be
 *                   an image called 'x-active.png', which will be displayed
 *                   when the button is pressed. All image sizes are 27x27
 *                   pixels. To add more images, add the corresponding png 
 *                   file to the standard image directory. 
 *
 * PhoneToolbarSection automatically sets the following {@code class} and 
 * {@code id} attributes. These can be used to select objects in the 
 * associated CSS and JS files (some elements may have more than one class).
 * There is an implicit assumption that there won't be two buttons with exactly
 * the same label and the same image.:
 *   td<image><label>:              {@code id} attribute for the {@code td} 
 *                                  containing a button with the image <image> 
 *                                  and the label <label>. May be used to
 *                                  define changes in the button's background
 *                                  when it is pressed.
 *
 *   toolbarButton:                 {@code class} attribute for the {@code div} 
 *                                  element containing a button. There will be \
 *                                  one such container for each button.
 *
 *   <label>-toolbarButton-<image>: {@code id} attribute for the {@code div} 
 *                                  element containing a button with the image
 *                                  <image> and the label <label>. 
 *                                  There will be one such container for each 
 *                                  button. May be used to events on that
 *                                  button.
 *
 *   <image><label>:                {@code id} attribute for the {@code div}
 *                                  element containing the image for a button 
 *                                  with the image <image> and the label 
 *                                  <label>.
 *                                  There will be one such element inside every
 *                                  button. May be used to define changes in
 *                                  the image when the button is pressed.                               
 */

public class PhoneToolbarSection extends Section {

	/**
     * Construct a PhoneToolbarSection.
     * @param properties           Contains configuration information
     *                             for the section; see description above.
     */
    public PhoneToolbarSection(Dataset properties) {
        this.properties = properties;
        if (!properties.containsKey("buttons")) {
            throw new org.fiz.InternalError("PhoneToolbarSection constructor " +
                    " invoked without a \"buttons\" property");
        }        
    }

    @Override
    public void render(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();

        //Choose CSS and JS files depending upon the device
        String device;
        try{
            device = (String) cr.getMainDataset().get("device");
        } catch(org.fiz.Dataset.MissingValueError e){
            device = "Default";
        }
        String jsFile = "static/fiz/" + device + "PhoneToolbarSection.js";
        html.includeJsFile(jsFile);
        cr.evalJavascript("new Fiz." + device + 
                "PhoneToolbarSection(\"@id?{footer}\");\n", 
                    properties);
        
        String cssFile = device + "PhoneToolbarSection.css";
        if (!properties.containsKey("class")) {
            html.includeCssFile(cssFile);
        }

        //Begin section...
        Template.appendHtml(out, 
                "\n<!-- Start PhoneToolbarSection {{@id}} -->\n" +
                "<div {{id=\"@id?{footer}\"}} " +
                    "class=\"@class?{toolbarSection}\">\n   <table><tr>\n",
                properties);
        Dataset buttons = properties.getDataset("buttons");

        //Add buttons...
        ArrayList<Dataset> rows = buttons.getDatasetList("record");
        for (int i = 0; i < rows.size(); i++) {
            Template.appendHtml(out, 
                    "  <td id=\"td@image@label\">", rows.get(i));

            if (rows.get(i).checkString("url") != null) {
                // Normal href.
                Template.appendHtml(out, "<a href=\"@url\"", rows.get(i));
            } else {
                StringBuilder expandedJs;
                String ajaxUrl = rows.get(i).checkString("ajaxUrl"); 
                if (ajaxUrl != null) {
                    // AJAX invocation.
                    expandedJs = new StringBuilder();
                    Ajax.invoke(cr, ajaxUrl, null, expandedJs);
                } else {
                    // Javascript code.
                    String javascript = rows.get(i).checkString("javascript");
                    expandedJs = new StringBuilder(javascript.length());
                    Template.appendJs(expandedJs, javascript);
                }
                Template.appendHtml(out, 
                        "<a href=\"#\" onclick=\"@1 return false;\"",
                        expandedJs);
            }

            Template.appendHtml(out, "><div class=\"toolbarButton\" " + 
                    "id=\"@label-toolbarButton-@image\"" + 
                    "><div>" + 
                    "<img id=\"@image@label\" alt=\"@label\" " +
                        "src=\"/static/fiz/images/@image.png\"/>" + 
                    "</div>" + 
                    "<span>@label</span></div>" + 
                    "</a></td>\n", rows.get(i));
        }
        // End.
        Template.appendHtml(out,
                "   </tr></table>\n</div>\n<!-- End PhoneToolbarSection {{@id}} -->\n",
                properties);
    }
}