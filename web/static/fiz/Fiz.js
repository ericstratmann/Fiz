/* Fiz.js --
 *
 * This Javascript file is included by every page generated with Fiz.
 * It provides widely-needed functions that don't fit anywhere else.
 *
 * Copyright (c) 2009 Stanford University
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

// Create the overall Fiz container; all Fiz classes live within
// this object; that way, people can also use other toolkits such as
// Prototype without worrying about name conflicts.
Fiz = Object();

// Some Fiz objects, such as FormSections, create a Javascript object
// that holds additional data related to an HTML element (Fiz.FormSection
// in the case of a FormSection).  When this happens, a reference to
// the Javascript object is stored as a property of the following
// object, using the id of the corresponding HTML element as the name
// of the Javascript object.
Fiz.ids = Object();

// The following variable identifies this page uniquely among all pages
// generated so far in the current session. This value is returned to
// the server in form posts and Ajax requests to connect with page
// properties for the page.  Null means the page doesn't have an id
// (there are no page properties defined for it yet).  This variable is
// set when the first page properties defined for the page, either during
// the regional rendering later on during an Ajax request or form post.
Fiz.pageId = null;

// The following variable holds an authentication token used to prevent
// CSRF attacks during form posts and Ajax requests.  The value is set
// by the Java method ClientRequest.setAuthToken; null means that
// method hasn't been invoked yet.
Fiz.auth = null;

// The following variable is set by Fiz.clearBulletinBeforeNextAdd.
Fiz.clearOldBulletin = false;

/**
 * Adds a message to the "bulletin" area at the top of the page.  First,
 * this function looks for a div with id {@code bulletin}, and creates one
 * if it doesn't already exist.  Then it makes that div visible, if it
 * wasn't already.  Finally, it appends {@code html} to the current
 * {@code innerHTML} for the bulletin div.
 * @param html                     HTML for the new message.  This will be
 *                                 appended to the bulletin's contents.
 */
Fiz.addBulletinMessage = function(html) {
    if (Fiz.clearOldBulletin) {
        Fiz.clearBulletin();
    }
    var bulletin = document.getElementById("bulletin");
    if (!bulletin) {
        // This page doesn't already include a bulletin; create a new
        // bulletin <div> as the first element in the <body>.
        bulletin = document.createElement("div");
        bulletin.setAttribute("id", "bulletin");
        bulletin.className = "bulletin";
        document.body.insertBefore(bulletin, document.body.firstChild);
    }
    bulletin.style.display = "";
    if (!bulletin.innerHTML) {
        // This check is needed primarily for testing, where there is
        // initially no innerHTML property.
        bulletin.innerHTML =  html;
    } else {
        bulletin.innerHTML = bulletin.innerHTML + html;
    }
}

/**
 * Remove everything from the bulletin and make the bulletin invisible.
 */
Fiz.clearBulletin = function() {
    var bulletin = document.getElementById("bulletin");
    if (bulletin) {
        bulletin.style.display = "none";
        bulletin.innerHTML = "";
    }
    Fiz.clearOldBulletin = false;
}

/**
 * This function sets a flag to clear any old bulletin contents in the
 * next call to addBulletinMessage.
 */
Fiz.clearBulletinBeforeNextAdd = function() {
    Fiz.clearOldBulletin = true;
}

/**
 * Adds {@code class} to {@code target}'s classes
 * @param target            (Element) See above
 * @param class             (String) See above
 */
Fiz.addClass = function(target, className)
{
	if (target.className.match(className) == null) {
	    target.className += (target.className == '')
	            ? className : ' ' +  className;
 	}
}

/**
 * Removes {@code class} from {@code target}'s classes
 * @param target            (Element) See above
 * @param class             (String) See above
 */
Fiz.removeClass = function(target, className)
{
    if (target.className.match(className) != null) {
        // Split at whitespace characters
        var classes = (target.className == '' ?
                [] : target.className.split(/\s+/));

        // Remove the class
        for (var i = classes.length - 1; i >= 0; i--) {
            if (classes[i] == className) {
                classes.splice(i, 1);
            }
        }

        // ... And finally, put it all together again
        target.className = classes.join(' ');
    }
}

/**
 * Finds the position of an element in a page relative to the top-left corner
 * of the page (the top-left position of the <html> element)
 * @param elem              (Element) See above
 * @param class             (String) See above
 */
Fiz.findAbsolutePosition = function(target)
{
	var curleft = 0;
	var curtop = 0;
	if (target.offsetParent) {
		do {
			curleft += target.offsetLeft;
			curtop += target.offsetTop;
		} while (target = target.offsetParent);
 	}
	return [curleft,curtop];
}

/**
 * Provides a cross-browser compatible way to stop the event {@code e} from
 * propogating further through the stack of DOM elements (stops the event from
 * bubbling up).
 * @param e                 (Event) See above
 */
Fiz.cancelBubble = function(e)
{
	if (!e) { e = window.event; }
	e.cancelBubble = true;
 	if (e.stopPropagation) { e.stopPropagation(); }
}

/**
 * Provides a cross-browser compatible way to fetch the key code value of the
 * event {@code e}.
 * @param e                 (Event) See above
 */
Fiz.getKeyCode = function(e)
{
	if (window.event) {
		return e.keyCode;
	} else {
		return e.which;
	}
}

/**
 * Provides a cross-browser compatible way to bind event listeners to
 * DOM elements.
 * @param target            (Element) Element to bind event to
 * @param type              (String) Event trigger (mouseover, click, etc...)
 * @param callback          (Function) Function to invoke when event occurs
 * @param capture           (Boolean) If set to true, all events of the
 *                          specified type will be dispatched to the registered
 *                          element before being dispatched to any elements
 *                          beneath it in the DOM tree
 */
Fiz.addEvent = function(target, type, callback, capture)
{
	if (target.addEventListener) {
		target.addEventListener(type, callback, capture);
	} else if (target.attachEvent) {
		// Internet Explorer
		target.attachEvent('on' + type, function() {
			callback(window.event);
		}, capture);
	} else {
		// Internet Explorer 5 for Mac and general case support
		target['on' + type] = function() {
			callback(window.event);
		};
	}
}

/**
 * Provides a cross-browser compatible way to retrieve the text value of
 * {@code target}.
 * @param target                    (Element) See above
 */
Fiz.getText = function(target)
{
    return target.innerText || target.textContent;
}

/**
 * Provides a cross-browser compatible way to set the text value of
 * {@code target}.
 * @param target                    (Element) See above
 * @param text                      (String) Text content we want to set
 */
Fiz.setText = function(target, text)
{
    if (target.innerText == undefined) {
        target.textContent = text;
    } else {
        target.innerText = text;
    }
}