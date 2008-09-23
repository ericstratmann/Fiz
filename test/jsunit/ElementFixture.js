// ElementFixture.js --
//
// This file provides a dummy partial implementation of the Element
// DOM class for use in tests.  There's just enough functionality here
// to test existing Javascript code.

/**
 * Constructor for Element objects.
 * @param contents                 (optional) If specified, consists of
 *                                 an Object all of whose properties will
 *                                 be copied to the new element.
 */
function Element(contents) {
    if (contents != undefined) {
        for (var name in contents) {
            this[name] = contents[name];
        }
    }
    if (this.style == undefined) {
        this.style = new Object();
    }
}

/**
 * This function looks for some sort of identifying information about
 * the element and returns that.  It is used by tests to test element
 * identity.  The return value is the element's id if that exists,
 * otherwise a serial number based on the order in which the element
 * was created.
 * @return                         Id or other identifying info for this
 *                                 element.
 */
Element.prototype.getId = function() {
    if (this.id != null) {
        return this.id;
    }
    for (var i = 0, length = document.createdElements.length; i < length;
            i++) {
        if (document.createdElements[i] == this) {
            return ((this.tagName != null) ? this.tagName : "") + "#" + i;
        }
    }
    return "unknown element";
}

/**
 * Return a string that describes the information in this element;
 * the result only describes this particular element, not its children.
 * @return                         String that describes the contents of
 *                                 this element.
 */
Element.prototype.toString = function() {
    return printDataset(this);
}

/**
 * Generates a string describing all of the children of a particular element.
 * @return                         See above.
 */
Element.prototype.printChildren = function() {
    var result = "";
    for (var child = this.firstChild, i = 0; child != null;
            child = child.nextSibling, i++) {
        result += "Child #" + i + ":\n";
        result += printDataset(child, "    ");
    }
    return result;
}

// The following functions provide dummy versions of standard DOM functions;
// see the DOM documentation for details on how they are supposed to behave.
// The functions below may attempt to replicate part or all of the
// functionality of the official DOM versions, or they may just log
// information about their parameters.

Element.prototype.appendChild = function(element) {
    element.parentNode = this;
    if (!this.lastChild) {
        this.firstChild = this.lastChild = element;
        return;
    }
    element.previousSibling = this.lastChild;
    this.lastChild.nextSibling = element;
    this.lastChild = element;
}

Element.prototype.insertBefore = function(element, successor) {
    element.parentNode = this;
    if (successor == null) {
        appendChild(element);
        return;
    }

    // Make sure that successor is one of our children.
    for (var child = this.firstChild; child != successor;
            child = child.nextSibling) {
        if (!child) {
            throw new Error("Element.insertBefore couldn't find successor");
        }
    }

    // Insert the element.
    if (!successor.previousSibling) {
        this.firstChild = element;
    } else {
        successor.previousSibling.nextSibling = element;
        element.previousSibling = successor.previousSibling;
    }
    successor.previousSibling = element;
    element.nextSibling = successor;
}

Element.prototype.scrollIntoView = function() {
    jsunit.log += this.getId() + ".scrollIntoView()\n";
}

Element.prototype.setAttribute = function(name, value) {
    this[name] = value;
}