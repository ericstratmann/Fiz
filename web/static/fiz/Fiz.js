/** Fiz.js --
 *
 * This Javascript file is included by every page generated with Fiz.
 * It provides widely-needed functions that don't fit anywhere else.
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
 * wasn't already.  Then it appends a new div with class {@code className}
 * and contents {@code html} to the bulletin div.
 * @param className                Class to use for the div holding the new
 *                                 message.
 * @param html                     HTML for the new message.  This will become
  *                                the innerHTML of a new div inside the
  *                                bulletin.
 */
Fiz.addBulletinMessage = function(className, html) {
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
    var message = document.createElement("div");
    message.className = className;
    message.innerHTML = html;
    bulletin.appendChild(message);
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

Fiz.addClass = function(elem, className)
{
	if(undefined == elem.className) {
		elem.className = '';
	}
	if(elem.className.match(className) == null) {
		// Trim off the white spaces
		elem.className = elem.className.replace(/^\s+|\s+$/g, '');
		
		// Split at whitespace characters
		var classes = (elem.className == '' ?
				[] : elem.className.split(/\s+/));
				
		// Add the new class
		classes.push(className);
		
		// ... And finally, put it all together again
 		elem.className = classes.join(' ');
 	}
}

Fiz.removeClass = function(elem, className)
{
	if(undefined == elem.className) {
		elem.className = '';
	}
	elem.className = elem.className.replace(className, '');
}

Fiz.findPos = function(obj)
{
	var curleft = 0;
	var curtop = 0;
	if(obj.offsetParent) {
		do {
			curleft += obj.offsetLeft;
			curtop += obj.offsetTop;
		} while(obj = obj.offsetParent);
 	}
	return [curleft,curtop];
}

Fiz.cancelBubble = function(e)
{
	var e;
	if (!e) { e = window.event; }
	e.cancelBubble = true;
 	if (e.stopPropagation) { e.stopPropagation(); }
}

Fiz.getKeyCode = function(e) {
	if (window.event) {
		return e.keyCode;
	} else {
		return e.which;
	}
}