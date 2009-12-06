/* TabSection.js --
 *
 * This file provides Javascript functions needed to implement the
 * TabSection class.
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

// The following line is used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js

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
