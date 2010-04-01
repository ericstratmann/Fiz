/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

/*
 * This file contains useful functions that are of general use, but not
 * required by every page. The should only be included when needed.
 */


/*
 * If a value is present in an array, return its key. Otherwise, return -1.
 *
 * @param array     An array
 * @param value     A value that might be in the array
 * @return          The index of the value or -1 if not found
 */
Fiz.arrayIndexOf = function (array, value) {
    for (var i = 0; i < array.length; i++) {
        if (array[i] === value) {
            return i;
        }
    }

    return -1;
};

/*
 * Returns a random integer 0 <= r < n.
 *
 * @param n         Max value of the random integer (non-inclusive)
 * @return          An inter r such that 0 <= r < n
 */
Fiz.rand = function(n) {
    return Math.floor(Math.random() * n);
};

/*
 * Checks if an object is an array
 *
 * @param obj       Object to rest
 * @return          Whether the object is an array
 */

Fiz.isArray = function(obj) {
    return obj && obj.constructor === Array;
};

/**
 * Recursivly copies an object such that no memory is shared between the two
 * copies. The behavior for copying an object that has a loop (an object which
 * points to another object higher up) is undefined. The function has a special
 * case for variables with the name "ctx": they are not copied and point to the
 * old variable.
 *
 * @param oldObj    Object to copy
 * @return          Copy of the object passed in
 */
Fiz.deepCopy = function (oldObj) {
    if (oldObj === null) {
        return null;
    }
    var newObj = {};
    if (typeof(oldObj) === 'object') {
        if (Fiz.isArray(oldObj)) {
            var newObj = new Array();
        }
        for (var i in oldObj) {
            if (typeof(oldObj[i]) === 'object') {
                if (i !== "ctx") {
                    newObj[i] = Fiz.deepCopy(oldObj[i]);
                } else {
                    newObj[i] = oldObj[i];
                }
            } else {
                newObj[i] = oldObj[i];
            }
        }
    } else {
        newObj = oldObj;
    }
    return newObj;
};

/*
 * Given a mouse event, determines the x and y position of the mouse relative to
 * the specified element.  Compatible with Firefox, Safari, Chrome, Opera, IE7,
 * and IE8.  Some code taken from Danny Goodman's "Javascript and DHTML
 * Cookbook, 2nd Edition" and www.howtocreate.co.uk.
 *
 * @param event         The mouse event from which to get coordinates.
 * @param elem          The element in whose coordinate system the mouse
 *                      location is desired.
 * @return              A javascript object with fields 'x' and 'y',
 *                      corresponding to location of the mouse relative to the
 *                      specified element.
 */
Fiz.getRelativeEventCoords = function(event, elem) {
    var coords = {};
    var docBody = document.body;
    var docElem = document.documentElement;

    // Set coords to contain the location of the mouse within the entire page.
    if (event.pageX != undefined) { // Firefox, Safari, Chrome, Opera
        coords.x = event.pageX;
        coords.y = event.pageY;
    } else { // IE
        coords.x = event.clientX;
        coords.y = event.clientY;
        // If the document body has any scrolling offset, it will be the
        // scrolling offset for the whole page.  If the document body does not
        // have any scrolling offset, the document element may still have
        // scrolling offset for the whole page.
        if (docBody && (docBody.scrollLeft || docBody.scrollTop)) {
            // Non-Strict Mode
            coords.x += docBody.scrollLeft;
            coords.y += docBody.scrollTop;
        } else if (docElem && (docElem.scrollLeft || docElem.scrollTop)) {
            // Strict Mode
            coords.x += docElem.scrollLeft;
            coords.y += docElem.scrollTop;
        }
    }

    // Subtract the page-wide element coordinates from the page-wide mouse
    // coordinates.
    var hasClientLeftTop = (elem.clientLeft != undefined); // Needed for IE.
    var currElem = elem;
    do {
        var isBody = (currElem == docBody);
        coords.x -= (isBody ? Math.abs(currElem.offsetLeft) :
            currElem.offsetLeft) + (hasClientLeftTop ? currElem.clientLeft : 0);
        coords.y -= (isBody ? Math.abs(currElem.offsetTop) :
            currElem.offsetTop) + (hasClientLeftTop ? currElem.clientTop : 0);
    } while (currElem = currElem.offsetParent);

    // Add any scrolling offsets between the element and the document body.
    currElem = elem;
    while (currElem != docBody) {
        coords.x += currElem.scrollLeft;
        coords.y += currElem.scrollTop;
        currElem = currElem.parentNode;
    }

    return coords;
};
