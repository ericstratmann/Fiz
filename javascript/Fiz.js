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
 * Displays an error or advisory message in the "bulletin" area at the
 * top of the page.  Also creates the bulletin if it doesn't already exist
 * (i.e., if there is no element with id "bulletin"), and scrolls the
 * page if necessary to make the bulletin visible.
 * @param html                     HTML to display in the bulletin.  This
 *                                 will replace the innerHTML of the
 *                                 element with id "bulletin".
 */
Fiz.setBulletin = function(html) {
    var bulletin = document.getElementById("bulletin");
    if (!bulletin) {
        // This page doesn't already include a bulletin; create a new
        // bulletin <div> as the first element in the <body>.
        bulletin = document.createElement("div");
        bulletin.setAttribute("id", "bulletin");
        bulletin.className = "bulletin";
        document.body.insertBefore(bulletin, document.body.firstChild);
    }
    bulletin.innerHTML = html;
    bulletin.scrollIntoView();
}
