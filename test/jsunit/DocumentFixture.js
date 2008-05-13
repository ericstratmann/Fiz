// DocumentFixture.js --
//
// This file provides a dummy partial implementation of the Document
// DOM class and the {@code document} browser object, for use in
// tests.  There's just enough functionality here to test existing
// Javascript code.

/**
 * Constructor for Document objects.
 */
function Document() {
    // The following Object is used by getElementById to map from an
    // id to an Element.
    this.ids = new Object();

    // Create a few sample Elements.
    this.ids.element1 = new Element("text1");
    this.ids.element2 = new Element("text2");

    // Create standard properties.
    this.location = {href: "/a/b/c"};
}

// The following functions provide dummy implementations of standard
// DOM functions;  see the DOM documentation for details on how they
// are supposed to behave.
Document.prototype.getElementById = function(id) {
    return this.ids[id];
}