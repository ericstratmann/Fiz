// DocumentFixture.js --
//
// This file provides a dummy partial implementation of the Document
// DOM class and the {@code document} browser object, for use in
// tests.  There's just enough functionality here to test existing
// Javascript code.
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

/**
 * Constructor for Document objects.
 */
function Document() {
    // The following Object is used by getElementById to map from an
    // id to an Element.
    this.ids = new Object();

    // The following Array holds new Elements created by calling the
    // createElement function.
    this.createdElements = [];

    // If a test places one or more Elements in the following array,
    // then they are used as the return value(s) from the next call(s)
    // to document.createElement().
    this.newElements = [];

    // Create a few sample Elements.
    this.ids.element1 = new Element({innerHTML: "text1"});
    this.ids.element2 = new Element({innerHTML: "text2"});

    // Create standard properties.
    this.location = {href: "/a/b/c"};
    this.body = new Element({tagName: "body"});
    this.body.firstChild = new Element({tagName: "p", id: "paragraph1"});
}

/**
 * Create a new element.
 * @param id                       The element will be accessible via this
 *                                 identifier.
 * @param contents                 Object containing other properties for the
 *                                 element.
 * @return                         The new element.
 */
Document.prototype.addElementWithId = function(id, contents) {
    var element = new Element(contents);
    element.id = id;
    this.ids[id] = element;
    return element;
}

// The following functions provide dummy implementations of standard
// DOM functions;  see the DOM documentation for details on how they
// are supposed to behave.

Document.prototype.getElementById = function(id) {
    if (this.ids[id] !== undefined) {
        return this.ids[id];
    }

    return null;
}

Document.prototype.getElementsByTagName = function(tag) {
    var children = [];
    for (var i in this.ids) {
        if (tag == this.ids[i].tagName) {
            children.push(this.ids[i]);
        }
    }
    return children;
}

Document.prototype.createElement = function(tagName) {
    var element;
    if (this.newElements.length > 0) {
        // Return a pre-created element provided by the test.
        jsunit.log += "document.createElement(" + tagName + ")\n";
        element = this.newElements.shift();
    } else {
        // Create a new Element.
        element = new Element({tagName: tagName});
    }
    this.createdElements.push(element);
    return element;
}
