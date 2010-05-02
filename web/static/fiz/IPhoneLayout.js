/* IPhoneLayout.js --
 *
 * This file contains the javascript required to add basic functionalities for
 * an iPhone based view.
 *
 * Copyright (c) 2010 Stanford University
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

// The following lines are used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js

/**
 * Constructs a new IPhoneLayout object.
 */
Fiz.IPhoneLayout = function(){
    var $i = this;
    this.disableScrollOnBody();
    window.addEventListener("load", $i.hideURLBar, false);
    window.addEventListener("orientationchange", $i.hideURLBar, false);
}

/**
 * This function removes the default action associated with the 'touchmove'
 * event. It prevents the user from scrolling the body on the iPhone by
 * dragging a finger over it. This makes sure that the toolbar stays at the
 * bottom of the screen.
 */
Fiz.IPhoneLayout.prototype.disableScrollOnBody = function() {
    document.body.addEventListener("touchmove", function(e) {
        e.preventDefault();
    }, false);
}
/**
 * This function hides the URL bar in Safari on an iPhone. This helps to
 * increase the area available to the application.
 */
Fiz.IPhoneLayout.prototype.hideURLBar = function() {
    setTimeout(function() {
        window.scrollTo(0, 1);
    }, 0);
}