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
 * A RatingSection displays a series of rating images (stars, a progress bar, 
 * etc).  Users can select a rating by clicking on these images; upon clicking,
 * the selected rating is sent to a specified URL via Ajax.  When no rating has
 * been selected, a special "unrated" message is displayed.  RatingSections
 * support the following constructor properties:
 *   ajaxUrl:           (optional) A template for a URL to which an Ajax 
 *                      request will be sent every time a user selects a rating.
 *                      The template will be expanded using both the main
 *                      dataset and the result of any specified data request.  
 *                      The ClientRequest received by the {@code ajaxUrl} method
 *                      will have an additional "rating" key in its main 
 *                      dataset, with the associated value being the user's 
 *                      rating.  For instance, if {@code ajaxUrl} is set to 
 *                      "/x/ajaxY?a=b", then the dataset received by the method
 *                      for "ajaxY" will have {a:b, rating:[user rating]}.  Note
 *                      that the method (page) name must begin with "ajax".
 *   autoSizeText:      (optional) A boolean value determining whether or not 
 *                      the {@code unratedMessage}, the message shown when no 
 *                      rating has been selected, should be automatically sized 
 *                      to fit on one line passing through the center of the 
 *                      RatingSection.  If set as false, you may choose to 
 *                      customize the {@code <div>} containing the unrated 
 *                      message using CSS.  The unrated message text is located
 *                      in a {@code <div>} with class "unratedMsg", lying within
 *                      the RatingSection {@code <div>} whose id can be 
 *                      specified.  Thus, a CSS rule such as: 
 *                      <pre>
 *                          #[my-specified-id] .unratedMsg { ... }
 *                      </pre>
 *                      will give you CSS access to the unrated message text.
 *                      {@code autoSizeText} defaults to true.
 *   class:             (optional) Used as the {@code class} attribute for
 *                      the HTML div that contains the RatingSection.
 *   granularity:       (optional) Specifies the fineness of the rating 
 *                      measurements.  Ratings will be rounded to the nearest
 *                      granularity.  For instance, if {@code granularity} is 
 *                      set to 0.5, all ratings will be rounded to the nearest 
 *                      0.5.  If set to zero, no rounding will occur and the 
 *                      resulting ratings will be as fine-grained as the pixels.
 *                      {@code granularity} defaults to 1.0.  
 *   id:                (optional) Used as the id attribute for the {@code 
 *                      <div>} containing the RatingSection.
 *   imageFamily:       (optional) Specifies the partial URL of the images to 
 *                      display.  If {@code imageFamily} is specified as 
 *                      "/x/y/z.gif", then the following two images must exist:
 *                      - /x/y/z-on.gif:     The image to display when the
 *                                           rating value is selected.
 *                      - /x/y/z-off.gif:    The image to display when the 
 *                                           rating value is not selected.
 *                      Both of these images should have the same dimensions. 
 *                      Note that the size of the RatingSection will vary based
 *                      on the size of these images and the number of images 
 *                      specified - no image scaling will occur. {@code 
 *                      imageFamily} defaults to 
 *                      "/static/fiz/images/goldstar.png", images taken and/or
 *                      modified from the Crystal Icons Project.
 *   initRating:        (optional) Used as the initial rating for the 
 *                      RatingSection.  This value will be displayed when the 
 *                      RatingSection first loads.  If specified, any value 
 *                      for {@code initRatingKey} will be ignored.  If no 
 *                      initial rating is specified by either {@code initRating}
 *                      or {@code initRatingKey}, the RatingSection will 
 *                      initially display the {@code unratedMessage} and a
 *                      rating of zero.
 *   initRatingKey:     (optional) The dataset key to use for retrieval of an 
 *                      initial rating.  Both the ClientRequest's main dataset
 *                      and the DataRequest's response dataset will be searched.
 *                      For instance, if a {@code request} is specified with 
 *                      response containing {myrating : 3.2}, then setting 
 *                      {@code initRatingKey} to "myrating" will cause the 
 *                      initial rating to be 3.2.
 *   numImages:         (required) Specifies the number of rating images to 
 *                      display, and determines the maximum rating a user can 
 *                      select.  For instance, if the rating images are stars 
 *                      and {@code numImages} is set to 5, then five stars will
 *                      be displayed with the maximum user rating being 5.
 *   readOnly:          (optional) If set as true, then no user rating can be 
 *                      specified and the RatingSection will always show its 
 *                      initial rating.  {@code readOnly} defaults to false.
 *   request:           (optional) The name of a DataRequest whose result can 
 *                      supply data for any of the specified templates ({@code 
 *                      ajaxUrl}, {@code initRatingKey}, {@code 
 *                      unratedMessage}).  The request is created by the caller
 *                      and registered in the ClientRequest by calling 
 *                      ClientRequest.addDataRequest.
 *   unratedMessage:    (optional) A message to display on top of the 
 *                      RatingSection when no rating has been specified or 
 *                      selected.  {@code unratedMessage} defaults to "Not Yet 
 *                      Rated".
 *
 * RatingSection automatically sets the following {@code class} attributes
 * for use in CSS:
 *   offRating:         The {@code <div>} containing the "off" rating images.
 *   onRating:          The {@code <div>} containing the "on" rating images.
 *   unratedMsg:        The {@code <div>} containing the text of the {@code 
 *                      unratedMessage}.
 */
public class RatingSection extends Section implements DirectAjax {
    // The following are used as default values for various optional constructor
    // configurations.
    protected static final String DEFAULT_AJAX_URL = "";
    protected static final String DEFAULT_IMAGE_FAMILY = 
        "/static/fiz/images/goldstar24.png";
    protected static final String DEFAULT_INIT_RATING = "-1";
    protected static final String DEFAULT_UNRATED_MESSAGE = "Not Yet Rated";
    
    // Suffixes for files specified by the image family.
    protected static final String ON_SUFFIX = "-on";
    protected static final String OFF_SUFFIX = "-off";
    
    // A cache of the RatingSection's id.
    protected String id = null;
    
    /**
     * Construct a RatingSection.
     * @param properties            Contains configuration information for the 
     *                              RatingSection; see description above.
     */
    public RatingSection(Dataset properties) {
        this.properties = properties;
    }

    /**
     * Sets the rating of the RatingSection by emitting Javascript to that 
     * effect.  If the RatingSection has not already been rendered, no action 
     * will be taken.
     * 
     * @param cr                    Overall information about the client request
     *                              being serviced.
     * @param rating                The new rating for the RatingSection.
     */
    public void setRating(ClientRequest cr, double rating) {
        if (id == null) return;
        cr.evalJavascript("Fiz.ids.@1.setOfficialRating(@2);", id, "" + rating);
    }

    /**
     * Sets whether the RatingSection is read-only by emitting Javascript to 
     * that effect.  If the RatingSection has not already been rendered, no 
     * action will be taken.
     * 
     * @param cr                    Overall information about the client request
     *                              being serviced.
     * @param readOnly              If true, the RatingSection will be read-
     *                              only.  If false, it will be editable.
     */
    public void setReadOnly(ClientRequest cr, boolean readOnly) {
        if (id == null) return;
        cr.evalJavascript("Fiz.ids.@1.readOnly = @2;", id, "" + readOnly);
    }
    
    /**
     * This method is invoked during the final phase of rendering a page; it 
     * generates the necessary HTML and Javascript to make the RatingSection
     * function in the browser.
     * @param cr                    Overall information about the client request
     *                              being serviced.
     */
    @Override
    public void render(ClientRequest cr) {
        Html html = cr.getHtml();
        StringBuilder out = html.getBody();
        
        int numImages = Integer.parseInt(properties.getString("numImages"));
        
        id = properties.containsKey("id") ? 
                properties.getString("id") : cr.uniqueId("rating");

        // Collect the data that will be available for user-specified templates, 
        // including the response to the section's request, if there was one.
        Dataset templateData;
        String requestName = properties.checkString("request");
        if (requestName != null) {
            DataRequest dataRequest = cr.getDataRequest(requestName);
            Dataset response = dataRequest.getResponseOrAbort();
            if (response.containsKey("record")) {
                response = response.getDataset("record");
            }
            templateData = new CompoundDataset(response, cr.getMainDataset());
        } else {
            templateData = cr.getMainDataset();
        }
        
        // Include the necessary authentication tokens and Javascript files.
        cr.setAuthToken();
        html.includeJsFile("static/fiz/RatingSection.js");
        
        // Include the CSS file for RatingSections, assuming the user hasn't
        // opted to do otherwise by specifying their own class.
        if (!properties.containsKey("class")) {
            html.includeCssFile("RatingSection.css");
        }

        // Determine the image family and "unrated" message to use in the 
        // appended HTML.
        String imageFamily = DEFAULT_IMAGE_FAMILY;
        if (properties.containsKey("imageFamily")) {
            imageFamily = properties.getString("imageFamily");
        }
        String unratedMsg = DEFAULT_UNRATED_MESSAGE;
        if (properties.containsKey("unratedMessage")) {
            unratedMsg = Template.expandHtml(properties.getString("unratedMessage"), 
                    templateData);
        }
        
        // Append the HTML for the RatingSection.
        Template.appendHtml(out, "\n<!-- Start RatingSection @1 -->\n" +
                "<div id=\"@1\" class=\"@class?{RatingSection}\" " +
                    "onmousemove=\"@2\" onmouseout=\"@3\" onclick=\"@4\">\n" +
                "  <div id=\"@(1)_offRating\" class=\"offRating\">\n    ", 
                properties, id,
                Template.expandJs("Fiz.ids.@1.mouseMoveHandler(event);", id),
                Template.expandJs("Fiz.ids.@1.mouseOutHandler(event);", id),
                Template.expandJs("Fiz.ids.@1.clickHandler(event);", id));
        for (int i=0; i<numImages; i++) {
            Template.appendHtml(out, 
                    "<img id=\"@(1)_offImg@3\" src=\"@2\" alt=\"Off@3\"></img>",
                    id, StringUtil.addSuffix(imageFamily, OFF_SUFFIX), i + 1);
        }
        Template.appendHtml(out, "\n  </div>\n" +
                "  <div id=\"@(1)_onRating\" class=\"onRating\">\n    ", id);
        for (int i=0; i<numImages; i++) {
            Template.appendHtml(out,  
                    "<img id=\"@(1)_onImg@3\" src=\"@2\" alt=\"On@3\"></img>",
                    id, StringUtil.addSuffix(imageFamily, ON_SUFFIX), i + 1);
        }
        Template.appendHtml(out, "\n  </div>\n" +
                "  <div id=\"@(1)_unratedMsg\" class=\"unratedMsg\">" +
                      "@2?{Not Yet Rated}</div>\n" +
                "</div>\n" + 
                "<!-- End RatingSection @1 -->\n", id, unratedMsg);
        
        // Determine the Ajax URL and initial rating to use in the evaluated 
        // Javascript.
        String ajaxUrl = DEFAULT_AJAX_URL;
        if (properties.containsKey("ajaxUrl")) {
            ajaxUrl = Template.expandUrl(properties.getString("ajaxUrl"), 
                    templateData);
        }
        String initRating = DEFAULT_INIT_RATING;
        if (properties.containsKey("initRating")) {
            initRating = properties.getString("initRating");
        } else if (properties.containsKey("initRatingKey")) {
            initRating = templateData.getString(properties.getString("initRatingKey"));
        }
        
        // Create the initial Javascript to execute when the page is loaded.
        cr.evalJavascript(Template.expandJs("Fiz.ids.@1 = " +
                "new Fiz.RatingSection(\"@1\", @numImages, @granularity?{1}, " +
                "@2, @readOnly?{false}, @autoSizeText?{true}, \"@3\");\n", 
                properties, id, initRating, ajaxUrl));
    }
}
