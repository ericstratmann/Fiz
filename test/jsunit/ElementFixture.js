// ElementFixture.js --
//
// This file provides a dummy partial implementation of the Element
// DOM class for use in tests.  There's just enough functionality here
// to test existing Javascript code.
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
    for (var i = 0, length = document.createdElements.length; i < length; i++) {
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
 * Returns whether an element has any children or not
 * @return                            See above
 */
Element.prototype.hasChildNodes = function() {
    return (this.firstChild);
}

/**
 * Returns an array that contains all of the children of a particular element.
 * @return                            See above
 */
Element.prototype.getChildNodes = function() {
    var children = [];
    for(var child = this.firstChild; child != null;
    child = child.nextSibling) {
        children.push(child);
    }
    return children;
}

/**
 * Generates a list of children of the current element that are of the type
 * specified. IMPORTANT: Element must include a "tagName" property for this
 * function to find it.
 * @param tag                            The type of element we are looking for
 */
Element.prototype.getElementsByTagName = function(tag) {
    var children = [];
    for (var child = this.firstChild; child != null;
    child = child.nextSibling) {
        if (tag == child.tagName) {
            children.push(child);
        }
    }
    return children;
}

/**
 * Generates a list of children of the current element that have the class
 * specified. IMPORTANT: Element must include a "class" property for this
 * function to find it.
 * @param class_name                      The class attribute of element we 
 *                                        are looking for
 */
Element.prototype.getElementsByClassName = function(class_name) {
    var children = [];
    for (var child = this.firstChild; child != null;
    child = child.nextSibling) {
        if (class_name == child["class"]) {
            children.push(child);
        }
    }
    return children;
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

Element.prototype.addEventListener = function(event, callbackFunction, capture){
    if(!this.listeners[event]){
        this.listeners[event] = new Array();
    }
    this.listeners[event].push(callbackFunction);
}

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

Element.prototype.removeChild = function(element) {
    for (var child = this.firstChild; child != null;
            child = child.nextSibling) {
        if (child == element) {
            if (!child.previousSibling) {
                this.firstChild = child.nextSibling;
            } else {
                child.previousSibling.nextSibling = child.nextSibling;
            }
            if (!child.nextSibling) {
                this.lastChild = child.previousSibling;
            } else {
                child.nextSibling.previousSibling = child.previousSibling;
            }
            return child;
        }
    }
    throw new Error("Element.removeChild could not find the child");
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

Element.prototype.replaceChild = function(newChild, oldChild) {
    for (var child = this.firstChild; child != null;
    child = child.nextSibling) {
        if (child == oldChild) {
            newChild.previousSibling = child.previousSibling;
            if (child.previousSibling == null) {
                this.firstChild = newChild;
            } else {
                child.previousSibling.nextSibling = newChild;
            }
            newChild.nextSibling = child.nextSibling;
            if (child.nextSibling == null) {
                this.lastChild = newChild;
            } else {
                child.nextSibling.previousSibling = newChild;
            }
            return;
        }
    }
}

Element.prototype.scrollIntoView = function() {
    jsunit.log += this.getId() + ".scrollIntoView()\n";
}

Element.prototype.setAttribute = function(name, value) {
    this[name] = value;
}

Element.prototype.getAttribute = function(name){
    return this[name];
}

Element.prototype.setAttributes = function(attributes) {
    for(var i in attributes) {
        this.setAttribute(i, attributes[i]);
    }
}