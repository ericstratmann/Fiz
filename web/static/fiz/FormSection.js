/* FormSection.js --
 *
 * This file provides Javascript functions needed to implement the
 * FormSection class.  One Fiz.FormSection Javascript object gets created
 * for each FormSection Java object.  Methods on the Javascript object
 * are invoked for functions such as submitting the form and displaying
 * error messages.
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

// The following lines are used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js
// Fiz:include static/fiz/Ajax.js

/**
 * Create a FormSection object.
 * @param id                       Id for the {@code <form>} element
 *                                 that represents the form.
 */
Fiz.FormSection = function(id) {
    this.id = id;

    // The following property stores ids for all of the form elements that
    // have been modified to display error information.
    this.errorElements = new Array();
}

// The following variable refers to the most recent Fiz.FormSection
// to be submitted.  It is used to handle the subsequent response.
Fiz.FormSection.currentForm = null;

/**
 * If there are any element-specific error messages for the form element with
 * {@code id}, undisplay them.
 * @param id                       Identifies a particular form element; used
 *                                 as the base of various HTML element ids.
 */
Fiz.FormSection.prototype.clearElementError = function(id) {
    for (var i in this.errorElements) {
        if (this.errorElements[i] == id) {
            this.errorElements.splice(i, 1);
            break;
        }
    }
    this.clearElementErrorDisplay(id);
}

/**
 * If there are any element-specific error messages displayed in this
 * form, undisplay them.
 */
Fiz.FormSection.prototype.clearElementErrors = function() {
    while (this.errorElements.length > 0) {
        var id = this.errorElements[0];
        this.clearElementErrorDisplay(id);
        this.errorElements.shift();
    }

    // Clear the error elements array
    this.errorElements = [];
}

/**
 * Clears the error-specific error messages displayed in this form. Invoked
 * by clearElementError and clearElementErrors.
 * @param id                       Identifies a particular form element; used
 *                                 as the base of various HTML element ids.
 */
Fiz.FormSection.prototype.clearElementErrorDisplay = function(id) {
    var div = document.getElementById(id + "_diagnostic");
    div.style.display = "none";

    // Also, remove the "formError" class from the main row.
    var row = document.getElementById(id);
    row.className = "";
}

/**
 * This function is invoked as an Ajax response to display an error message
 * in the diagnostic row for the form element whose id is {@code id}, and
 * also add class "formError" to the main row for the element.
 * @param id                       Identifies a particular form element; used
 *                                 as the base of various HTML element ids.
 * @param html                     HTML to use as the body of the diagnostic
 *                                 {@code <div>} for the form element.
 */
Fiz.FormSection.prototype.elementError = function(id, html) {
    this.errorElements.push(id);
    var div = document.getElementById(id + "_diagnostic");
    div.style.display = "";
    div.innerHTML = html;
    var row = document.getElementById(id);
    row.className = "formError";
}

/**
 * This method is invoked just before a form is submitted.  It creates
 * the iframe that will be used to receive the form response, and also
 * performs other bookkeeping functions.
 * @return                         Always true, so the form posting will
 *                                 proceed normally.
 */
Fiz.FormSection.prototype.submit = function() {
    // Tricky stuff: the target for a form refers to an invisible
    // iframe.  When the form is submitted, the response goes to that iframe
    // rather than replacing the main window contents.  This makes
    // form submits roughly equivalent to Ajax requests, except that
    // they can handle file uploads whereas Ajax does not.  However, in
    // order to avoid unwanted browser behavior the iframe doesn't get
    // created until now, just before the form is submitted, and it gets
    // deleted below, as soon as the response has been received.  If we
    // don't do this, and the "Back" button is used to return to a page
    // where a form was previously submitted, the browser will reissue
    // the form submit (it thinks it needs the results to fill the iframe).
    // Different browsers exhibit this unpleasant behavior in slightly
    // different ways, but this solution, making the iframe as transient
    // as possible, seems to work on all of them.
    Fiz.FormSection.currentForm = this;
    var div = document.getElementById(this.id + "_target");
    div.innerHTML = "<iframe name=\"" + this.id + "_iframe\"></iframe>";
    var form = document.getElementById(this.id);
    form.target = this.id + "_iframe";

    // If a page id has been defined, return it as part of the form by
    // supplying the value for a hidden form element.
    if (Fiz.pageId) {
        var element = document.getElementById(this.id + "_fizPageId");
        if (element != null) {
            element.value = Fiz.pageId;
        }
    }
    return true;
}

/**
 * This function is invoked by the HTML returned by a form response to the
 * invisible iframe for the form.  This method will delete the iframe and eval
 * its script argument.
 * @param script                   A Javascript script that should be
 *                                 eval-ed in the frame containing the
 *                                 original {@code <form>} element.
 *                                 All of the server's desired actions are
 *                                 encoded in the script.
 */
Fiz.FormSection.handleResponse = function(script) {
    // It isn't safe to handle the form response right now, while the nested
    // iframe is on the call stack .  Do all the work from a timer event,
    // which will have its own call stack.
    setTimeout(function() {
            Fiz.FormSection.currentForm.handleResponse2(script);
        }, 0);
}

/**
 * This method is invoked as a timer handler to complete the handling of
 * a form response.
 * @param script                   A Javascript script to eval.
 */
Fiz.FormSection.prototype.handleResponse2 = function(script) {
    // Clear old error information before handling the form response.
    this.clearElementErrors();
    Fiz.clearBulletin();

    var div = document.getElementById(this.id + "_target");
    div.innerHTML = "";
    eval(script);
}