/** TabSection.js --
 *
 * This file provides Javascript functions needed to implement the
 * TabSection class.
 */

// The following line is used by Fiz to manage Javascript dependencies.
// Fiz:include fizlib/Fiz.js

Fiz.TabSection = new Object();

/**
 * Given the id of a tab within a TabSection, modify the HTML for the
 * TabSection so that the tab becomes selected.
 * @param id                       {@code id} attribute for the middle <td>
 *                                 element of the tab to select (the one
 *                                 that contains the tab's text).
 */
Fiz.TabSection.selectTab = function(id) {
    var sibling;
    var tab = document.getElementById(id);

    // First, go through all of the sibling <td> elements and deselect
    // any tabs that are currently selected.
    for (sibling = tab.parentNode.firstChild; sibling != null;
            sibling = sibling.nextSibling) {
        if (sibling.className == "leftSelected") {
            sibling.className = "left";
        } else if (sibling.className == "midSelected") {
            sibling.className = "mid";
        } else if (sibling.className == "rightSelected") {
            sibling.className = "right";
        }
    }

    // Finally, modify the class for this element and its adjacent siblings
    // to reflect that they are selected.
    tab.className = "midSelected";
    for (sibling = tab.previousSibling; sibling != null;
            sibling = sibling.previousSibling) {
        if (sibling.className == "left") {
            sibling.className = "leftSelected";
            break;
        }
    }
    for (sibling = tab.nextSibling; sibling != null;
            sibling = sibling.nextSibling) {
        if (sibling.className == "right") {
            sibling.className = "rightSelected";
            break;
        }
    }
}
