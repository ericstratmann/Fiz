/* IPhonePhoneToolbarSection.js --
 *
 * This file provides Javascript functions needed to implement the
 * PhoneToolbarSection class for an iPhone.
 * One Fiz.IPhonePhoneToolbarSection Javascript object gets created for each 
 * PhoneToolbarSection Java object created on an iPhone.  
 * Methods on the Javascript object are invoked for changing the color of the 
 * button when clicked.
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
 * Creates an IPhonePhoneToolbarSection object and then initializes handlers for
 * the buttons in the toolbar according to requirements for the iPhone.
 * @param id                       Id for the {@code <div>} element
 *                                 that represents this section.
 */
Fiz.IPhonePhoneToolbarSection = function(id) {
    this.id = id;
    var footer = document.getElementById(id);
    if (footer){
        var buttons = footer.getElementsByClassName("toolbarButton");
        for (var i = 0, j = buttons.length; i < j; i++){
            var button = buttons[i];
            var buttonId = button.getAttribute("id");
            //Extract image and label names from the id...
            label = buttonId.substring(0, buttonId.indexOf("-toolbarButton-"));
            image = buttonId.substring(buttonId.indexOf("-toolbarButton-") + 15);
            this.addHandlers(button, image, label);
        }
    }
}

/**
 * Initializes handlers for the buttons in the toolbar according to 
 * requirements for the iPhone.
 * @param button                   HTML element corresponding to the 
 *                                 {@code div} element that contains this
 *                                 button.
 * @param image                    The name of the image file (without the
 *                                 extension) that is being displayed on this
 *                                 button in the inactive state.
 * @param label                    The label that is being displayed on this
 *                                 button. The label and the the image
 *                                 name are used to assign javascript handlers
 *                                 to various HTML elements contained in this
 *                                 button.                                                              
 */
Fiz.IPhonePhoneToolbarSection.prototype.addHandlers = function(button, image, label){
    button.addEventListener("touchstart", function(){
        Fiz.changeImage(image + label, image + "-active.png");
        Fiz.addClass(document.getElementById("td" + image + label), "active"); 
    }, false);
    button.addEventListener("click", function(){
        Fiz.changeImage(image + label, image + ".png");
        Fiz.removeClass(document.getElementById("td" + image + label), "active"); 
    }, false);
}