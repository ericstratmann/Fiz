/** Fiz.js --
 *
 * This Javascript file is included by every page generated with Fiz.
 * It provides widely-needed functions that don't fit anywhere else.
 */

// Create the overall Fiz container; all Fiz classes live within
// this object; that way, people can also use other toolkits such as
// Prototype without worrying about name conflicts.
Fiz = Object();

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
}
