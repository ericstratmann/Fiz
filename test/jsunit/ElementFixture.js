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
}

/**
 * This function looks for some sort of identifying information about
 * the element and returns that.  It is used by tests to test element
 * identity.  The return value is the element's id if that exists,
 * otherwise would look for something else useful.
 * @return                         Id or other identifying info for this
 *                                 element.
 */
Element.prototype.getId = function() {
    if (this.id != null) {
        return this.id;
    }
    if (this.tagName != null) {
        return this.tagName;
    }
    return "anonymous";
}

// The following functions provide dummy versions of standard DOM functions;
// see the DOM documentation for details on how they are supposed to behave.

Element.prototype.setAttribute = function(name, value) {
    this[name] = value;
}

Element.prototype.insertBefore = function(element, successor) {
    jsunit.log += this.getId() + ".insertBefore(" + element.getId() +
            ", " + ((successor == null) ? "end" : successor.getId()) +
            ")\n";
}

Element.prototype.scrollIntoView = function() {
    jsunit.log += this.getId() + ".scrollIntoView()\n";
}