/** RatingSection.js --
 *
 * This file provides Javascript functions needed to implement the
 * RatingSection class.
 *
 * Copyright (c) 2009 Stanford University
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

// The following line is used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js
// Fiz:include static/fiz/FizCommon.js
// Fiz:include static/fiz/Ajax.js

/*
 * Constructor for the RatingSection.  Takes in a wide variety of information
 * needed by various Javascript functions throughout the existence of of the
 * RatingSection.
 *
 * @param id                The id of the RatingSection {@code <div>}.
 * @param numImages         The number of images displayed in the RatingSection
 *                          (also equal to the maximum possible user rating).
 * @param granularity       The fineness of the rating measurements.  If set to
 *                          zero, then the rating measurement is as fine as the
 *                          pixel grain.
 * @param initRating        The initial rating to display.
 * @param readOnly          If true, then the RatingSection will not respond to
 *                          any triggered events and will therefore be read-
 *                          only.  If false, the RatingSection will update its
 *                          display according to the mouse position and submit
 *                          a rating when the user clicks.
 * @param autoSizeText      If true, then the RatingSection will automatically
 *                          modify the font and positioning of the "unrated"
 *                          message to be centered within the RatingSection.
 *                          If false, the text will be unaltered.
 * @param ajaxUrl           The URL to which an Ajax request should be sent
 *                          upon the user selecting a rating.
 */
Fiz.RatingSection = function(id, numImages, granularity, initRating, readOnly,
        autoSizeText, ajaxUrl) {
    // Save constructor parameters in the object.
    this.numImages = numImages;
    this.granularity = granularity;
    this.readOnly = readOnly;
    this.ajaxUrl = ajaxUrl;
    this.zeroCutoff = 0.25; // The rating will be set to zero if the mouse is
                            // within the left-most 0.25 of the left-most grain.

    // Cache pointers to the key DOM elements.
    this.elemRatingSection = document.getElementById(id);
    this.elemOffRating = document.getElementById(id + "_offRating");
    this.elemOnRating = document.getElementById(id + "_onRating");
    this.elemUnratedMsg = document.getElementById(id + "_unratedMsg");

    // Cache the full width and height of the RatingSection.  In Opera as of
    // 8/2009, only an image's dimensions will always yield the right values.
    // The only exception is if the RatingSection placed in a scrolling element
    // with a width narrower than one image, in which case there (at this time)
    // is no work-around.
    var sampleImg = numImages <= 0 ? {clientWidth: 0, clientHeight: 0} :
        document.getElementById(id + "_offImg1");
    this.fullWidth = Math.max(this.elemOffRating.clientWidth,
        sampleImg.clientWidth * numImages);
    this.fullHeight = Math.max(this.elemOffRating.clientHeight,
        sampleImg.clientHeight);

    // Set the dimensions of the overarching RatingSection {@code <div>}, which
    // will have incorrect dimensions because it isn't absolutely positioned.
    // The unrated message {@code <div>} also won't have the correct dimensions.
    this.elemRatingSection.style.width = this.fullWidth + "px";
    this.elemRatingSection.style.height = this.fullHeight + "px";
    this.elemUnratedMsg.style.width = this.fullWidth + "px";

    // If specified, auto-size the unrated message to be centered within the
    // RatingSection.
    if (autoSizeText) {
        this.elemUnratedMsg.style.fontSize = (this.fullHeight / 2) + "px";
        this.elemUnratedMsg.style.paddingTop = ((this.fullHeight -
            this.elemUnratedMsg.clientHeight) / 2) + "px";
    }

    // Set up the official rating (the most recent submitted or assigned rating)
    // and the shown rating (the rating displayed on the RatingSection).
    this.officialRating = undefined;
    this.shownRating = undefined;

    // Set the initial rating to display.
    this.setOfficialRating(initRating);
}

/*
 * Calculates the rating based on the coordinates of the specified event.
 * Relies heavily on Fiz.getRelativeEventCoords to retrieve the relative
 * x position of the mouse within the RatingSection {@code <div>}.  If a
 * positive non-zero granularity has been specified, the calculated rating is
 * rounded to the nearest granularity (i.e. if granularity is set as 0.5, then
 * the rating will be rounded to the nearest 0.5).  Otherwise, the returned
 * rating will not be rounded.
 *
 * @param event                   The event whose coordinates will determine the
 *                                returned rating.
 * @return                        The calculated rating.
 */
Fiz.RatingSection.prototype.calculateRating = function(event) {
    // Calculate the relative x-position of the event.
    var eventX = Fiz.getRelativeEventCoords(event, this.elemRatingSection).x;
    var rating = -1;

    if (this.granularity > 0) {
        var rawIndex = (eventX * this.numImages) /
            (this.fullWidth * this.granularity);
        // If the mouse is within the left-most zeroCutoff (0.25) of the left-
        // most grain, round the rating to zero.
        var index = (rawIndex <= this.zeroCutoff) ? 0 : Math.ceil(rawIndex);
        rating = Math.min(index * this.granularity, this.numImages);
    } else {
        rating = (eventX * this.numImages) / this.fullWidth;
    }

    return rating;
}

/*
 * Called when the user clicks on the RatingSection.  Sets the currently shown
 * rating as the official rating, and sends that rating via Ajax to the
 * specified URL.
 *
 * @param event                    The triggered click event.
 */
Fiz.RatingSection.prototype.clickHandler = function(event) {
    if (this.readOnly) return;

    this.setOfficialRating(this.shownRating);
    if (this.ajaxUrl != "") {
        new Fiz.Ajax({url: this.ajaxUrl, data:
                {'rating': this.officialRating}});
    }
}

/*
 * Called when the user moves their mouse over the RatingSection.  Calculates
 * the rating based on the coordinates of the specified event, and sets that as
 * the shown rating.
 *
 * @param event                    The triggered mousemove event.
 */
Fiz.RatingSection.prototype.mouseMoveHandler = function(event) {
    if (this.readOnly) return;

    var rating = this.calculateRating(event);
    this.setShownRating(rating);
}

/*
 * Called when the user moves their mouse out of the RatingSection.  Updates
 * the currently shown rating to be the official rating.
 *
 * @param event                    The triggered mouseout or mouseleave event.
 */
Fiz.RatingSection.prototype.mouseOutHandler = function(event) {
    if (this.readOnly) return;

    // If the event was triggered by moving between elements within the
    // RatingSection, do nothing.
    var toElement = event.relatedTarget || event.toElement;
    try {
        if (toElement == this.elemRatingSection ||
            toElement.parentNode == this.elemRatingSection ||
            toElement.parentNode.parentNode == this.elemRatingSection) return;
    } catch (err) {}

    this.setShownRating(this.officialRating);
}

/*
 * Sets the "official" rating of the RatingSection: the official rating
 * represents the most recent rating submitted by the user, or the initial
 * rating if no ratings have been submitted.  If the official and shown ratings
 * are not equal, the shown rating is set to the official rating.  When a user's
 * mouse leaves a RatingSection, the shown rating reverts to the official
 * rating.
 *
 * @param rating                The new official rating.
 */
Fiz.RatingSection.prototype.setOfficialRating = function(rating) {
    this.officialRating = rating;

    if (this.officialRating != this.shownRating) {
        this.setShownRating(this.officialRating);
    }
}

/*
 * Sets the rating shown by the RatingSection; modifies the number or fraction
 * of on/off images to match the specified rating.  If the shown rating is less
 * than zero (implying no rating), then the "unrated" message is displayed.
 *
 * @param rating                The rating to display.
 */
Fiz.RatingSection.prototype.setShownRating = function(rating) {
    this.shownRating = rating;

    if (rating < 0) {
        // If there's no rating, show the unratedMessage and zero images.
        this.elemUnratedMsg.style.display = "block";
        rating = 0;
    } else {
        // If there is a rating, hide the unratedMessage.
        this.elemUnratedMsg.style.display = "none";
    }

    // Show the specified rating by updating the width of elemOnRating.
    var newRatingWidth = rating * this.fullWidth / this.numImages;
    this.elemOnRating.style.width = newRatingWidth + "px";
}
