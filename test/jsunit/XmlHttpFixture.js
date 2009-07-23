// XmlHttpFixture.js --
//
// This file provides a dummy implementation of an XMLHTTP object
// for use in testing.
//
// Copyright (c) 2009 Stanford University
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