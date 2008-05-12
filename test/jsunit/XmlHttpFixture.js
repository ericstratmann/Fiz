// XmlHttpFixture.js --
//
// This file provides a dummy implementation of an XMLHTTP object
// for use in testing.

function XMLHttpRequest() {
    // The following properties correspond to the official properties
    // supported by real XMLHttpRequest objects.
    this.onreadystatechange = "none";
    this.status = 200;
    this.statusText = "sample status message";
    this.readyState = 4;
    this.responseText = "actions = {};";
}

// The following methods provide gummy implementations of XMLHttpRequest
// methods: they simply log their arguments to {@code jsunit.log}.

XMLHttpRequest.prototype.open = function(method, url, async) {
    jsunit.log += "open(method: " + method + ", url: " + url + ", async: " +
            async + ")\n";
}
XMLHttpRequest.prototype.send = function(message) {
    jsunit.log += "send(message: " + message + ")\n";
}
XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
    jsunit.log += "setRequestHeader(name: " + name + ", value: " + value + ")\n";
}