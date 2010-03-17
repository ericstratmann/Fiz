/* TabSectionTest.js --
 *
 * Jsunit tests for TabSection.js, organized in the standard fashion.
 *
 * Copyright (c) 2009-2010 Stanford University
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
 * ACTION OF CONTRACT, NEGLIGENCE OR TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

include("static/fiz/Fiz.js");
include("static/fiz/TabSection.js");

// Utility function: scans all the children of a node and returns a string
// containing their classes.
function getChildClasses(element) {
    var result = "";
    var separator = "";
    for (var child = element.firstChild; child != null;
            child = child.nextSibling) {
        result += separator + child.className;
        separator = ", ";
    }
    return result;
};

TabSectionTest = {};

TabSectionTest.test_selectTab = function() {
    document = new Document();
    var parent = new Element();
    parent.appendChild(new Element({className: "left"}));
    parent.appendChild(new Element({className: "left"}));
    parent.appendChild(new Element({className: "leftSelected"}));
    parent.appendChild(new Element({className: "rightSelected"}));
    parent.appendChild(new Element({className: "right"}));
    parent.appendChild(new Element({className: "left"}));
    parent.appendChild(new Element({className: "left"}));
    parent.appendChild(new Element({className: "left"}));
    parent.appendChild(new Element({className: ""}));
    parent.appendChild(document.addElementWithId("999", {className: "mid"}));
    parent.appendChild(new Element({className: ""}));
    parent.appendChild(new Element({className: "right"}));
    parent.appendChild(new Element({className: "mid"}));
    parent.appendChild(new Element({className: "midSelected"}));
    parent.appendChild(new Element({className: "midSelected"}));
    Fiz.TabSection.selectTab("999");
    assertEqual("left, left, left, right, right, left, left, " +
            "leftSelected, , midSelected, , rightSelected, mid, mid, mid",
            getChildClasses(parent), "new sibling classes");
};

TabSectionTest.test_selectTab_alreadySelected = function() {
    document = new Document();
    var parent = new Element();
    parent.appendChild(new Element({className: "leftSelected"}));
    parent.appendChild(document.addElementWithId("999", {className: "mid"}));
    parent.appendChild(new Element({className: "rightSelected"}));
    Fiz.TabSection.selectTab("999");
    assertEqual("leftSelected, midSelected, rightSelected",
            getChildClasses(parent), "new sibling classes");
};

TabSectionTest.test_selectTab_noSiblingsToSelect = function() {
    document = new Document();
    var parent = new Element();
    parent.appendChild(document.addElementWithId("999", {className: "mid"}));
    Fiz.TabSection.selectTab("999");
    assertEqual("midSelected",
            getChildClasses(parent), "new sibling classes");
};
