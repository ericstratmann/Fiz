// chat.js --
//
// Implements 2 classes used for testing server notification.

function ajaxRequest() {
    // Browser-specific: create the XMLHttpRequest request object.

    if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    try {
        return new ActiveXObject("Msxml2.XMLHTTP");
    } catch(e) {
        try {
            return new ActiveXObject("Microsoft.XMLHTTP");
        } catch(e) {
            return null;
        }
    }
}

function Source(id, index) {
    this.index = index;
    log("source" + index);
    this.element = document.getElementById(id);
    var __object = this;
    this.element.onkeyup = function() {__object.changed();};
    this.inProgress = 0;
    this.moreUpdates = 0;
}

Source.prototype.changed = function() {
    if (this.inProgress) {
        log("Skipping update: request in progress");
        this.moreUpdates = 1;
        return;
    }
    this.inProgress = 1;
    this.request = ajaxRequest();
    __object = this;
    this.request.onreadystatechange = function() {__object.ajaxResponse(this.request);};
    this.request.open("POST", "/fiz/chat/source?source=" + this.index);
    this.request.setRequestHeader("Content-type", "text/plain");
    this.request.send(this.element.value);
    log("source message sent: " + this.element.value);
}

Source.prototype.ajaxResponse = function() {
     if (this.request.readyState != 4) {
        // Request is still in progress.

        return;
    }
    log ("source AJAX response: " + this.request.responseText);
    this.request.onreadystatechange = null;
    if (this.request.status != 200) {
        log("HTTP Error " + this.request.status + ": "
                + this.request.statusText);
        return;
    }
    this.inProgress = 0;
    if (this.moreUpdates) {
        log("Updates came in during request");
        this.moreUpdates = 0;
        this.changed();
    }
}

function Watch(id, index) {
    this.index = index;
    this.generation = -1;
    this.element = document.getElementById(id);
    this.createNotifier();
}

Watch.prototype.createNotifier = function() {
    this.request = ajaxRequest();
    var __object = this;
    this.request.onreadystatechange = function() {__object.watchResponse(this.request);};
    this.request.open("POST", "/fiz/chat/watch?watch=" + this.index
            + "&generation=" + this.generation);
    this.request.setRequestHeader("Content-type", "text/plain");
    this.request.send("");
    log("waiting on generation " + this.generation);
}

Watch.prototype.watchResponse = function() {
     if (this.request.readyState != 4) {
        // Request is still in progress.

        return;
    }
    var text = this.request.responseText;
    log("watch AJAX response: " + text);
    this.request.onreadystatechange = null;
    if (0) {
        log("HTTP Error " + this.request.status + ": "
                + this.request.statusText);
        return;
    }
    var newline = text.indexOf(":");
    if (newline >= 0) {
        this.generation = text.substr(0, newline);
        this.element.value = text.substr(newline +1);
        this.createNotifier();
    }
}