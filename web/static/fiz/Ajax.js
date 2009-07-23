/* Ajax.js --
 *
 * This file implements the browser side of Ajax support in Fiz.  This
 * is not a general-purpose implementation of Ajax; it is designed
 * to work with the Ajax class in Fiz:
 *   * Each Ajax request can include a dataset containing parameter
 *     information.  The dataset is transmitted using the POST message
 *     using a custom Fiz format (see the {@code readInputData} method
 *     of the Ajax class for details.  Parameters can also be supplied
 *     using query values attached to the URL.
 *   * An Ajax response consists of Javascript code that is evaluated
 *     in the browser.
 *   * The result of an Ajax request is determined entirely by the server.
 *     The initiating Javascript code provides information for the request
 *     but it has no control over handling of response;  the server
 *     determines that with the code it returns.  For example, if
 *     Javascript code wishes to update a particular {@code <div>} it
 *     might include the id for the {@code <div>} in the Ajax request, but
 *     it is up to the server to decide whether to use that id in its
 *     response.
 *   * If unexpected errors occurred while handling an Ajax request,
 *     error information is displayed in the bulletin.
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

/**
 * Create an Ajax object and initiate a server request.  This function
 * returns when the request has been initiated, but before it has been
 * completed.  When the response is eventually received, the actions
 * described in the response will be executed.  The following values are
 * supported in {@code properties}:
 *   url:                          (required) Send the request to this URL.
 *   data:                         (optional) Object whose contents will be
 *                                 sent in the request as parameters.  May
 *                                 contain nested objects and arrays of
 *                                 objects in addition to string values.
 *                                 Each top-level value becomes an entry
 *                                 in the main dataset on the server.
 * @param properties               Object whose properties describe the
 *                                 request.  See above for supported values.
 *                                 Or, this parameter can be a string
 *                                 whose value is the {@code url} property,
 *                                 in which case all other properties get
 *                                 default values.
 */
Fiz.Ajax = function(properties) {
    if ((typeof properties) == "string") {
        this.url = properties;
    } else {
        this.url = properties.url;
        this.data = properties.data;
    }
    this.xmlhttp = null;           // XMLHTTP object for controlling the
                                   // request.

    // Create the XMLHTTP object that will be used for transport; try
    // several different ways, since different browsers implement it
    // differently.
    if (window.XMLHttpRequest) {
        this.xmlhttp = new XMLHttpRequest();
    } else {
        try {
            this.xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
        } catch(e) {
            try {
                this.xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
            } catch(e) {
                this.error("couldn't create XMLHttpRequest object");
                return;
            }
        }
    }

    // The code below creates a function that, when invoked, will call
    // stateChange on this object.
    var _targetObject = this;
    this.xmlhttp.onreadystatechange = function() {
        _targetObject.stateChange();
    }

    // Send out the request.
    this.xmlhttp.open("POST", this.url);
    this.xmlhttp.setRequestHeader("Content-type", "text/fiz; charset=utf-8");
    var postData = "";
    postData += "main.";
    postData += Fiz.Ajax.serialize(this.data);
    this.xmlhttp.send(postData);
    Fiz.clearBulletinBeforeNextAdd();
}

/**
 * Private: this function is invoked by the browser's XMLHTTP object when
 * the state of a request has changed.  There are multiple possible
 * state changes, but the only one we care about here is completion.
 * Once the response has been received, this function will carry out the
 * action(s) described by the response.
 */
Fiz.Ajax.prototype.stateChange = function() {
    if (this.xmlhttp.readyState != 4) {
        // Uninteresting state change; wait for the request to complete.
        return;
    }

    // The following statement is needed to prevent memory leaks in
    // IE versions 6 and 7.
    this.xmlhttp.onreadystatechange = null;

    if (this.xmlhttp.status != 200) {
        this.error( "HTTP error " + this.xmlhttp.status + ": "
                + this.xmlhttp.statusText);
        return;
    }

    // The response consists of Javascript code that will carry out the
    // server's wishes.
    try {
        eval(this.xmlhttp.responseText);
    } catch (e) {
        var where = "";
        if (e.fileName && e.lineNumber) {
            where = " (" + e.fileName + ":" + e.lineNumber + ")";
        }
        this.error("error in Javascript response" + where + ": " + e);
    }
}

/**
 * Private: this function is invoked when an error occurs during an Ajax
 * request.  It reports the error to the user, using the bulletin.
 * @param message                  Human-readable HTML describing the problem.
 */
Fiz.Ajax.prototype.error = function(message) {
    Fiz.clearBulletin();
    Fiz.addBulletinMessage("bulletinError",
            "Error in Ajax request for " + this.url + ": " + message);
}

/**
 * Private: translate a dataset-like object into the form of a serialized
 * dataset, which can then be sent to Fiz.  See the {@code serialize}
 * method in Dataset.java for details on the syntax of this format.
 * @param object                   Object consisting of a hierarchical
 *                                 collection of scalar properties, nested
 *                                 objects, and arrays of nested objects.
 * @return                         String representing {@code object}.
 */
Fiz.Ajax.serialize = function(object) {
    // Iterate over all of the values in the object.  Each value can be
    // either an array of nested objects, a single nested object, or a
    // scalar value.
    var result = "(";
    var prefix = "";
    for (var name in object) {
        var value = object[name];
        if (value instanceof Array) {
            var length = value.length;

            // Don't emit anything for empty arrays of nested datasets.
            if (length > 0) {
                result += prefix + name.length + "." + name;
                for (var i = 0; i < length; i++) {
                    result += Fiz.Ajax.serialize(value[i]);
                }
            }
        } else if ((typeof value) == "object") {
            result += prefix + name.length + "." + name +
                    Fiz.Ajax.serialize(value);
        } else {
            // Simple scalar value.
            value = value.toString();
            result += prefix + name.length + "." + name +
                    value.length + "." + value;
        }
        prefix = "\n";
    }

    // If an authentication token is available, include that in the request
    // (is used by the server to prevent request forgeries).
    if (Fiz.auth) {
        result += prefix + "8.fiz_auth" + Fiz.auth.length + "." + Fiz.auth;
    }
    // If a page identifier has been assigned, include that in the request
    // (it will allow the server to access saved data for this page).
    if (Fiz.pageId) {
        result += prefix + "10.fiz_pageId" + Fiz.pageId.length + "." +
                  Fiz.pageId;
    }
    result += ")";
    return result;
}
