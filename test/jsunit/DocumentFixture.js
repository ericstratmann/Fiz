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

    // The following Array holds new Elements created by calling the
    // createElement function.
    this.createdElements = [];

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
    return this.ids[id];
}

Document.prototype.createElement = function(tagName) {
    var element = new Element({tagName: tagName});
    this.createdElements.push(element);
    return element;
}