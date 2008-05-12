// ElementFixture.js --
//
// This file provides a dummy partial implementation of the Element
// DOM class for use in tests.  There's just enough functionality here
// to test existing Javascript code.

/**
 * Constructor for Element objects.
 * @param body                     (optional) If specified, provides
 *                                 innerHtml for the element.
 */
function Element(body) {
    if (body == undefined) {
        this.innerHTML = "none";
    } else {
        this.innerHTML = body;
    }
}