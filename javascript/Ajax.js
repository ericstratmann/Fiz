/** Ajax.js --
 *
 * This file implements the browser side of Ajax support in Fiz.  This
 * is not a general-purpose implementation of Ajax; it is designed
 * to work with the Ajax class in Fiz:
 *   * Each Ajax request can include a dataset containing parameter
 *     information.  The dataset is transmitted using the POST message
 *     using a custom Fiz format (see the {@code readInputData} method
 *     of the Ajax class for details.  Parameters can also be supplied
 *     using query values attached to the URL.
 *   * Each Ajax response consists of Javascript code that, when evaluated,
 *     will create a Javascript array named {@code Actions}.  Each element
 *     of this array is a Javascript Object whose properties specify an
 *     action for the browser to take.  The {@code type} property specifies
 *     the kind of action, and other properties provide additional parameters
 *     depending on {@code type}.  The action types currently supported are:
 *       update:       Replace the value of a DOM element.  The {@code id}
 *                     property gives the id of the element, and the
 *                     {@code html} property contains the new HTML for the
 *                     element.
 *       eval:         Execute Javascript code.  The {@code javascript}
 *                     property contains the code to evaluate.
 *       redirect:     Change the page displayed in the window to the one
 *                     given by {@code url}.
 *       error:        Register an error using whatever mechanism was
 *                     defined for the request.  The {@code properties}
 *                     property contains a dataset (nested Objects and Arrays)
 *                     with information about the error.  All errors have
 *                     at least a {@code message} value in {@code properties}
 *                     which contains a human-readable message.  Other
 *                     values may be available on a case-by-case basis.
 *     * The result of an Ajax request is determined entirely by the server.
 *       The initiating Javascript code provides information for the request
 *       but it does not tell how to handle the response;  the server
 *       determines that by creating {@code update}, {@code eval}, and
 *       {@code error} actions in the response.  If Javascript code wishes
 *       to update a particular {@code <div>} it might include the id for
 *       the {@code <div>} in the Ajax request, but it is up to the server
 *       to decide whether to honor that id or specify something else in the
 *       response.  The initiating code has control only over error handling.
 */

// Create the overall Fiz container if it doesn't already exist.
try {Fiz} catch (e) {Fiz = Object();}

/**
 * Create an Ajax object and initiate a server request.  This function
 * returns when the request has been initiated, but before it has been
 * completed.  When the response is eventually received, the actions
 * described in the response will be executed.  The following values are
 * supported in {@code properties}:
 *   url:                          (required) Send the request to this URL.
 *   errorHandler:                 (optional) If an error occurs the
 *                                 {@code ajaxError} method will be invoked
 *                                 on this object, with a single parameter
 *                                 consisting of an Object whose properties
 *                                 provide information about the error.  If
 *                                 this property is omitted than a default
 *                                 error method is invoked, which displays
 *                                 a pop-up dialog.  The error method may be
 *                                 invoked before this function returns, for
 *                                 some kinds of errors.
 * @param properties               Object whose properties describe the
 *                                 request.  See above for supported values.
 *                                 Or, this parameter can be a string
 *                                 whose value is the {@code url} property,
 *                                 in which case all other properties are
 *                                 considered to be unspecified.
 * @param data                     (optional) Object whose contents will be
 *                                 sent in the request as parameters.  May
 *                                 contain nested objects and arrays of
 *                                 objects in addition to string values.
 *                                 This turns into a Dataset on the Fiz side.
 *                                 If this argument is omitted then an empty
 *                                 message is sent, producing an empty
 *                                 dataset in the server.
 */
Fiz.Ajax = function(properties, data) {
    if ((typeof properties) == "string") {
        this.url = properties;
        this.errorHandler = undefined;
    } else {
        this.url = properties.url;
        this.errorHandler = properties.errorHandler;
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
                this.error({message: "couldn't create XMLHttpRequest object"});
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
    this.xmlhttp.setRequestHeader("Content-type",
            "text/plain; charset=utf-8");
    this.xmlhttp.send(Fiz.Ajax.serialize(data));
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
        this.error({message: "HTTP error " + this.xmlhttp.status + ": "
                + this.xmlhttp.statusText});
        return;
    }

    // The response consists of Javascript code that will set a variable
    // {@code actions}.  That variable consists of an array, each of whose
    // elements is an object describing a single action to take.  Walk
    // through the array, carrying out the actions in order.
    try {
        eval(this.xmlhttp.responseText);
        for (var i = 0, length = actions.length; i < length; i++) {
            var action = actions[i];
            var type = action.type;
            if (type == "update"){
                var target = document.getElementById(action.id);
                if (target != null) {
                    target.innerHTML = action.html;
                } else {
                    throw "nonexistent element \"" + action.id +
                          "\" in update action";
                }
            } else if (type == "eval") {
                eval(action.javascript);
            } else if (type == "redirect") {
                document.location.href = action.url;
            } else if (type == "error") {
                this.error(action.properties);
            } else {
                throw "unknown response action \"" + op + "\"";
            }
        }
    } catch (e) {
        var where = "";
        if (e.fileName && e.lineNumber) {
            where = " (" + e.fileName + ":" + e.lineNumber + ")";
        }
        this.error({message: "error processing responseText" +
                where + ": " + e});
    }
}

/**
 * Private: this function is invoked when an error occurs during an Ajax
 * request.  It reports the error and aborts the request.
 * @param properties               Object containing information about the
 *                                 error.  Must have at least a
 *                                 {@code message} property with a
 *                                 human-readable description of the problem.
 */
Fiz.Ajax.prototype.error = function(properties) {
    if (this.errorHandler) {
        this.errorHandler.ajaxError(properties);
    } else {
        alert("Error in Ajax request for " + this.url + ": " +
                properties.message);
    }
}

/**
 * Translate a dataset-like object into a string suitable for transport to
 * Fiz.  See Ajax.java for details on the syntax of this format.
 * @param object                   Object consisting of a hierarchical
 *                                 collection of scalar properties, nested
 *                                 objects, and arrays of nested objects.
 * @return                         String representing {@code object}.
 */
Fiz.Ajax.serialize = function(object) {
    // Iterate over all of the values in the object.  Each value can be
    // either an array of nested objects, a single nested object, or a
    // scalar value.
    var result = "";
    for (var name in object) {
        result += name.length + "." + name;
        var value = object[name];
        if (value instanceof Array) {
            for (var i = 0, length = value.length; i < length; i++) {
                result += "(" + Fiz.Ajax.serialize(value[i]) + ")";
            }
            result += "\n";
        } else if ((typeof value) == "object") {
            result += "(" + Fiz.Ajax.serialize(value) + ")\n";
        } else {
            // Simple scalar value.
            value = value.toString();
            result += value.length + "." + value + "\n";
        }
    }
    return result;
}
