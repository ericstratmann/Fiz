/* TreeRow.js --
 *
 * This file implements the TreeRow class, which is used to implement
 * TreeSections.
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

/**
 * Create a TreeRow object, which is used to expand and unexpand
 * elements of a TreeSection.
 * @param id                       Id for the {@code <td>} element that
 *                                 contains this element.
 * @param unexpandedHtml           Contents of the row when it is not
 *                                 expanded.
 * @param expandedHtml             Contents of the row when it is expanded
 *                                 (not including the children of this element,
 *                                 which will be added to the end of the row).
 */
Fiz.TreeRow = function(id, unexpandedHtml, expandedHtml){
    this.id = id;
    this.unexpandedHtml = unexpandedHtml;
    this.expandedHtml = expandedHtml;
}

/**
 * Replace the contents of the row for this element so that it is displayed
 * in the "expanded" style.  This method also fills in the body of an
 * additional table row containing the children of this node.
 * @param html                     HTML for all of the children of this row.
 */
Fiz.TreeRow.prototype.expand = function(html) {
    this.replace(this.expandedHtml);

    // Find the <div> that must contain all the child information, and
    // replace its body with the HTML given to us.
    var childDiv = document.getElementById(this.id + "_childDiv");
    childDiv.innerHTML = html;

    // Find the table row containing the child information and make it
    // visible.
    var childRow = document.getElementById(this.id + "_childRow");
    childRow.style.display = "";
}

/**
 * Replace the contents of the row for this element so that it is displayed
 * in the "unexpanded" style, and undisplay all the children.
 */
Fiz.TreeRow.prototype.unexpand = function() {
    this.replace(this.unexpandedHtml);
    var childRow = document.getElementById(this.id + "_childRow");
    childRow.style.display = "none";
}

/**
 * Replace the contents of the row for this element with given HTML.
 * @param html                     New HTML for the row, starting with the
 *                                 {@code <tr>} element.
 * @return                         The {@code <tr>} element for the new row.
 */
Fiz.TreeRow.prototype.replace = function(html) {
    // Note: as of 10/2008, IE 7 doesn't permit assignment of innerHTML
    // for <table> or <tr> elements.  Thus we have to create a <p> element,
    // then put the new row in a table inside the <p>.  After this is done,
    // we move the row to the main document, replacing the original row.

    var temp = document.createElement("p");
    temp.innerHTML = "<table>" + html + "</table>";

    // Find the new row, which is buried inside the temporary table.
    for (var newRow = temp.firstChild; newRow != null;
            newRow = newRow.nextSibling) {
        if (newRow.nodeName == "TABLE") {
            newRow = newRow.firstChild;
        }
        if (newRow.nodeName == "TBODY") {
            newRow = newRow.firstChild;
        }
        if (newRow.nodeName == "TR") {
            var oldRow = document.getElementById(this.id);
            oldRow.parentNode.replaceChild(newRow, oldRow);
            return newRow;
        }
    }
}
