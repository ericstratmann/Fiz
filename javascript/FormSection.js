/** FormSection.js --
 *
 * This file provides Javascript functions needed to implement the
 * FormSection class.  One Fiz.FormSection Javascript object gets created
 * for each FormSection Java object.  Methods on the Javascript object
 * are invoked for functions such as submitting the form and displaying
 * error messages.
 */

// The following line is used by Fiz to manage Javascript dependencies.
// Fiz:include Ajax.js

/**
 * Create a FormSection object.
 * @param id                       Id for the {@code <form>} element
 *                                 that represents the form.
 * @param postUrl                  Make an Ajax request to this URL when
 *                                 the form is posted.
 */
Fiz.FormSection = function(id, postUrl) {
    this.id = id;
    this.postUrl = postUrl;

    // The following property references the <div> element that currently
    // displays a diagnostic message (null means none).
    this.visibleDiagnostic = null;
}

/**
 * This function is invoked automatically by the browser when the
 * form corresponding to this object is submitted (e.g., by clicking
 * on a submit input).  It issues an Ajax request to the URL for this
 * form, providing all of the form data in the Ajax message.
 */
Fiz.FormSection.prototype.post = function() {
    var form = document.getElementById(this.id);
    var data = new Object();

    // Iterate over all the form elements, collecting data from the elements
    // that have data associated with them (e.g., buttons and submit elements
    // don't have any data).
    for (var i = 0; i < form.length; i++) {
        var element = form.elements[i];
        var type = element.type;
        if ((type == "checkbox") || (type == "radio")) {
            if (element.checked) {
                data[element.name] = element.value;
            }
        } else if ((type == "hidden") || (type == "password") ||
                (type == "select-one") || (type == "text") ||
                (type == "textarea")) {
            data[element.name] = element.value;
        } else if (type == "select-multiple") {
            // Iterate through all of the options and create an array
            // containing one entry for each option that is selected.
            var value = [];
            for (var j = 0, length = element.options.length;
                    j < length; j++) {
                var option = element.options[j];
                if (option.selected) {
                    value.push({value: option.value});
                }
            }
            data[element.name] = value;
        }
    }

    new Fiz.Ajax(this.postUrl, data);
}

/**
 * This function is invoked as an Ajax response to display an error message
 * in the diagnostic row for the form element whose id is {@code id}.  If
 * there was previously an error message displayed for a different element,
 * that message is made invisible.
 * @param id                       Identifies a particular form element; used
 *                                 as the base of various HTML element ids.
 * @param html                     HTML to use as the body of the diagnostic
 *                                 {@code <div>} for the form element.
 */
Fiz.FormSection.prototype.elementError = function(id, html) {
    var div = document.getElementById(id + ".diagnostic");
    div.style.display = "";
    div.innerHTML = html;
    if (this.visibleDiagnostic && (this.visibleDiagnostic != div)) {
        this.visibleDiagnostic.style.display = "none";
    }
    this.visibleDiagnostic = div;
}

/**
 * If there is an element-specific error message displayed in this
 * form, undisplay it.
 */
Fiz.FormSection.prototype.clearElementError = function() {
    if (this.visibleDiagnostic) {
        this.visibleDiagnostic.style.display = "none";
        this.visibleDiagnostic = null;
    }
}