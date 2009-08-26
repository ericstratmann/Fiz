// RatingSectionTest.js --
//
// Jsunit tests for RatingSectionTest.js, organized in the standard fashion.
//
// Copyright (c) 2009 Stanford University
//
// Permission to use, copy, modify, and distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");
include("static/fiz/RatingSection.js");

RatingSectionTest = {};

DefaultID = "testRS";

// A dummy implementation of Fiz.Ajax, for testing purposes.
Fiz.Ajax = function(properties) {
    Fiz.ids[DefaultID].ajaxSentUrl = properties.url;
    Fiz.ids[DefaultID].ajaxSentData = properties.data;
}

// A dummy implementation of Fiz.getRelativeEventCoords, for testing purposes.
Fiz.getRelativeEventCoords = function(event, id) {
    return event;
}

RatingSectionTest.setUp = function() {
    this.ratingSection = document.addElementWithId(DefaultID);
    
    this.offRating = document.addElementWithId(DefaultID + "_offRating");
    this.offRating.clientWidth = 300;
    this.offRating.clientHeight = 70;
    
    this.onRating = document.addElementWithId(DefaultID + "_onRating");
    
    this.unratedMsg = document.addElementWithId(DefaultID + "_unratedMsg");
    this.unratedMsg.clientHeight = 35;
    
    this.sampleImg = document.addElementWithId(DefaultID + "_offImg1");
    this.sampleImg.clientWidth = 60;
    this.sampleImg.clientHeight = 70;
}

RatingSectionTest.test_constructor_overview = function() {
    var tempSetOfficial = Fiz.RatingSection.prototype.setOfficialRating;
    Fiz.RatingSection.prototype.setOfficialRating = logFunction("setOfficial");
    var ratingSection = new Fiz.RatingSection(DefaultID, 5, 0.25, -1, false, 
        true, "/x/ajaxY?a=b");
    
    // Stored properties on the RatingSection object.
    assertEqual(5, ratingSection.numImages, "Cached numImages");
    assertEqual(0.25, ratingSection.granularity, "Cached granularity");
    assertEqual(false, ratingSection.readOnly, "Cached readOnly");
    assertEqual("/x/ajaxY?a=b", ratingSection.ajaxUrl, "Cached ajaxUrl");
    assertEqual(0.25, ratingSection.zeroCutoff, "Fixed zeroCutoff");
    
    assertEqual(this.ratingSection, ratingSection.elemRatingSection, 
        "Cached ratingSection <div>");
    assertEqual(this.offRating, ratingSection.elemOffRating, 
        "Cached offRating <div>");
    assertEqual(this.onRating, ratingSection.elemOnRating, 
        "Cached onRating <div>");
    assertEqual(this.unratedMsg, ratingSection.elemUnratedMsg, 
        "Cached unratedMsg <div>");
    
    assertEqual(300, ratingSection.fullWidth, "Cached fullWidth");
    assertEqual(70, ratingSection.fullHeight, "Cached fullHeight");
    
    // Results of element resizing.
    assertEqual("300px", this.ratingSection.style.width, 
        "Set ratingSection Width");
    assertEqual("70px", this.ratingSection.style.height, 
        "Set ratingSection Height");
    assertEqual("300px", this.unratedMsg.style.width, 
        "Set unratedMsg Width");
        
    // Ensure that the font size and padding were set correctly.
    assertEqual("35px", this.unratedMsg.style.fontSize, 
        "Set unratedMsg Font Size");
    assertEqual("17.5px", this.unratedMsg.style.paddingTop, 
        "Set unratedMsg Padding Top");
    
    // Ensure that officialRating and shownRating are initialized correctly.
    assertEqual(undefined, this.officialRating, "Default officialRating");
    assertEqual(undefined, this.shownRating, "Default shownRating");
    
    // Ensure that setOfficialRating() was called with the correct argument.
    assertEqual("setOfficial(-1)\n", jsunit.log,
        "Function Calls");
    
    // Reset the method prototype.
    Fiz.RatingSection.prototype.setOfficialRating = tempSetOfficial;
}

RatingSectionTest.test_constructor_sampleImg = function() {
    // Alter the dimensions of the test image.
    this.sampleImg.clientWidth = 70;
    this.sampleImg.clientHeight = 80;
    
    var tempSetOfficial = Fiz.RatingSection.prototype.setOfficialRating;
    Fiz.RatingSection.prototype.setOfficialRating = logFunction("setOfficial");
    var ratingSection = new Fiz.RatingSection(DefaultID, 5, 1, 1, false, false,
        "");
    
    // Ensure that the cached width and height were taken from the sample image.
    assertEqual(350, ratingSection.fullWidth, "Cached fullWidth");
    assertEqual(80, ratingSection.fullHeight, "Cached fullHeight");
    
    // Construct a RatingSection with no images, and ensure that the sample 
    // image is not used.
    ratingSection = new Fiz.RatingSection(DefaultID, 0, 1, 1, false, false, "");
    assertEqual(300, ratingSection.fullWidth, "Cached fullWidth");
    assertEqual(70, ratingSection.fullHeight, "Cached fullHeight");
    
    // Reset the method prototypes.
    Fiz.RatingSection.prototype.setOfficialRating = tempSetOfficial;
}

RatingSectionTest.test_constructor_noAutoSizeText = function() {
    var tempSetOfficial = Fiz.RatingSection.prototype.setOfficialRating;
    Fiz.RatingSection.prototype.setOfficialRating = logFunction("setOfficial");
    new Fiz.RatingSection(DefaultID, 2, 1, 1, false, false, "");
    
    // Ensure that the unrated message was not auto-sized.
    assertEqual(undefined, this.unratedMsg.style.fontSize, 
        "unratedMsg Font Size not set");
    assertEqual(undefined, this.unratedMsg.style.paddingTop, 
        "unratedMsg Padding Top not set");
    
    // Reset the method prototypes.
    Fiz.RatingSection.prototype.setOfficialRating = tempSetOfficial;
}

RatingSectionTest.test_calculateRating = function() {
    var ratingSection = new Fiz.RatingSection(DefaultID, 2, 0.25, -1, false, 
        false, "");
    var event = {};
    
    // Calculate an ordinary rating, granularity > 0.
    event.x = 200;
    assertEqual(1.5, ratingSection.calculateRating(event), "Rating X=200, G>0");
    
    // Calculate a rating barely large enough to avoid rounding to zero.
    event.x = 10;
    assertEqual(0.25, ratingSection.calculateRating(event), "Rating X=10, G>0");
    
    // Calculate a rating barely small enough to get rounded to zero.
    event.x = 9;
    assertEqual(0, ratingSection.calculateRating(event), "Rating X=9, G>0");
    
    // Calculate an ordinary rating, but with granularity = 0.
    ratingSection.granularity = 0;
    event.x = 63;
    assertEqual(0.42, ratingSection.calculateRating(event), "Rating X=63, G=0");
}

RatingSectionTest.test_clickHandler = function() {
    var ratingSection = new Fiz.RatingSection(DefaultID, 2, 1, -1, true, false,
        "");
    ratingSection.setOfficialRating = logFunction("setOfficial");
    Fiz.ids[DefaultID] = ratingSection;
    var event = {};
    
    // Ensure that a read-only RatingSection takes no action.
    ratingSection.clickHandler(event);
    assertEqual("", jsunit.log, "Function Calls when read-only");
    assertEqual(undefined, ratingSection.ajaxSentUrl, 
        "Ajax sent URL when read-only");
    
    // Ensure that a RatingSection with no ajaxUrl doesn't send an Ajax request.
    ratingSection.readOnly = false;
    ratingSection.clickHandler(event);
    assertEqual("setOfficial(-1)\n", jsunit.log, 
        "Function Calls when ajaxUrl is empty");
    assertEqual(undefined, ratingSection.ajaxSentUrl,
        "Ajax sent URL when ajaxUrl is empty");
    jsunit.log = "";
    
    // Ensure that a RatingSection with an ajaxUrl sends an Ajax request.
    ratingSection.ajaxUrl = "/x/ajaxY";
    ratingSection.clickHandler(event);
    assertEqual("setOfficial(-1)\n", jsunit.log,
        "Function Calls when an Ajax request should send");
    assertEqual("/x/ajaxY", ratingSection.ajaxSentUrl,
        "Ajax sent URL when an Ajax request should send");
    assertEqual(-1, ratingSection.ajaxSentData.rating,
        "Ajax sent rating when an Ajax request should send");
}

RatingSectionTest.test_mouseMoveHandler = function() {
    var ratingSection = new Fiz.RatingSection(DefaultID, 2, 1, -1, true, false,
        "");
    ratingSection.setShownRating = logFunction("setShown");
    ratingSection.calculateRating = function(event) { return event; };
    var event = "test";
    
    // Ensure that a read-only RatingSection takes no action.
    ratingSection.mouseMoveHandler(event);
    assertEqual("", jsunit.log, "Function Calls when read-only");
    
    // Ensure that the event is handled when passed explicitly (non-IE).
    ratingSection.readOnly = false;
    ratingSection.mouseMoveHandler(event);
    assertEqual("setShown(test)\n", jsunit.log, 
        "Function Calls when event passed explicitly (non-IE)");
    jsunit.log = "";
}

RatingSectionTest.test_mouseOutHandler_setup = function() {
    var ratingSection = new Fiz.RatingSection(DefaultID, 2, 1, -1, true, false,
        "");
    ratingSection.setShownRating = logFunction("setShown");
    var event = {relatedTarget: {parentNode: {parentNode: {}}}};
    
    // Ensure that a read-only RatingSection takes no action.
    ratingSection.mouseOutHandler(event);
    assertEqual("", jsunit.log, "Function Calls when read-only");
    
    // Ensure that the event is handled when passed explicitly (non-IE).
    ratingSection.readOnly = false;
    ratingSection.mouseOutHandler(event);
    assertEqual("setShown(-1)\n", jsunit.log,
        "Function Calls when event passed explicitly (non-IE).");
    jsunit.log = "";
}

RatingSectionTest.test_mouseOutHandler_relatedElementCheck = function() {
    var ratingSection = new Fiz.RatingSection(DefaultID, 2, 1, -1, false, false,
        "");
    ratingSection.setShownRating = logFunction("setShown");
    var event = {};
    
    // Ensure that event.relatedTarget is checked, and that no action is taken
    // when the related element is a grandchild of the RatingSection element.
    event.relatedTarget = {parentNode: {parentNode: this.ratingSection}};
    ratingSection.mouseOutHandler(event);
    assertEqual("", jsunit.log, "Function Calls when event.relatedTarget " +
        "is set, related element is a grandchild element");
    
    // Ensure that event.toElement is checked, and that no action is taken when
    // the related element is a child of the RatingSection element.
    event.toElement = {parentNode: this.ratingSection};
    event.relatedTarget = undefined;
    ratingSection.mouseOutHandler(event);
    assertEqual("", jsunit.log, "Function Calls when event.toElement is " +
        "set, related element is a child element");
    
    // Ensure that no action is taken when the related element is the 
    // RatingSection element.
    event.toElement = this.ratingSection;
    ratingSection.mouseOutHandler(event);
    assertEqual("", jsunit.log, "Function Calls when the related element is " +
        "the RatingSection element");
    
    // Ensure that action is taken when the related element has no accessible 
    // fields.
    event.relatedTarget = {};
    event.toElement = {};
    ratingSection.mouseOutHandler(event);
    assertEqual("setShown(-1)\n", jsunit.log, 
        "Function Calls when the related element has no accessible fields");
}

RatingSectionTest.test_setOfficialRating = function() {
    var ratingSection = new Fiz.RatingSection(DefaultID, 2, 1, -1, false, false,
        "");
    ratingSection.setShownRating = logFunction("setShown");
    
    // Ensure that setShownRating is called when the official and shown ratings
    // disagree.
    ratingSection.setOfficialRating(1.5);
    assertEqual(1.5, ratingSection.officialRating, "Set officialRating to 1.5");
    assertEqual("setShown(1.5)\n", jsunit.log, 
        "Function Calls when the official and shown ratings disagree");
    jsunit.log = "";
    
    // Ensure that setShownRating is not called when the official and shown 
    // ratings agree.
    ratingSection.setOfficialRating(-1);
    assertEqual(-1, ratingSection.officialRating, "Set officialRating to -1");
    assertEqual("", jsunit.log, 
        "Function Calls when the official and shown ratings agree");
}

RatingSectionTest.test_setShownRating = function() {
    var ratingSection = new Fiz.RatingSection(DefaultID, 2, 1, -1, false, false,
        "");
    
    // Ensure that the unrated message is hidden when the shown rating is >= 0.
    ratingSection.setShownRating(0.6);
    assertEqual(0.6, ratingSection.shownRating, "Set shownRating to 0.6");
    assertEqual("none", this.unratedMsg.style.display, 
        "Unrated message hidden");
    assertEqual("90px", this.onRating.style.width, "Set onRating width to 90");
    
    // Ensure that the unrated message is shown when the shown rating is < 0;
    ratingSection.setShownRating(-1);
    assertEqual(-1, ratingSection.shownRating, "Set shownRating to -1");
    assertEqual("block", this.unratedMsg.style.display, 
        "Unrated message shown");
    assertEqual("0px", this.onRating.style.width, "Set onRating width to 0");
}
