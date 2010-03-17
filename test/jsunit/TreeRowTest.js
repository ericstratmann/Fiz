/* TreeRowTest.js --
 *
 * Jsunit tests for TreeRow.js, organized in the standard fashion.
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
include("static/fiz/TreeRow.js");

TreeRowTest = {};

TreeRowTest.test_constructor = function() {
    var row = new Fiz.TreeRow("id 44", "html #1", "html #2");
    assertEqual("expandedHtml: html #2, id: id 44, unexpandedHtml: html #1",
            printObject(row), "object properties");
};

TreeRowTest.test_expand = function() {
    // Create elements for the test to manipulate.
    document = new Document();
    var childDiv = document.addElementWithId("tree_44_childDiv",
            {innerHTML: "original  HTML"});
    var childRow = document.addElementWithId("tree_44_childRow",
            {style: {display: "none"}});

    var treeRow = new Fiz.TreeRow("tree_44", "html #1", "html #2");
    // Stub out the "replace" function.
    treeRow.replace = function(html) {
        jsunit.log += "TreeRow_replace(\"" + html + "\")\n";
    }

    treeRow.expand("new HTML");
    assertEqual("TreeRow_replace(\"html #2\")\n", jsunit.log, "jsunit log");
    assertEqual("new HTML", childDiv.innerHTML,
            "new HTML for child div");
    assertEqual("", childRow.style.display,
            "new display style for child row");
};

TreeRowTest.test_unexpand = function() {
    // Create elements for the test to manipulate.
    document = new Document();
    var childRow = document.addElementWithId("tree_44_childRow",
            {style: {display: "original"}});

    var treeRow = new Fiz.TreeRow("tree_44", "html #1", "html #2");
    // Stub out the "replace" function.
    treeRow.replace = function(html) {
        jsunit.log += "TreeRow_replace(\"" + html + "\")\n";
    }

    treeRow.unexpand();
    assertEqual("TreeRow_replace(\"html #1\")\n", jsunit.log, "jsunit log");
    assertEqual("none", childRow.style.display,
            "new display style for child row");
};

TreeRowTest.test_replace = function() {
    // Dummy up the row that will get replaced.
    var parent = document.addElementWithId("parent", {});
    var oldRow = document.addElementWithId("tree_44", {parentNode: parent});
    parent.appendChild(oldRow);

    // Pre-create a paragraph with a nested table of a form appropriate
    // for the "replace" method to parse.
    var paragraph = new Element({nodeName: "P"});
    var table =  new Element({nodeName: "TABLE"});
    var body =  new Element({nodeName: "TBODY"});
    var row =  new Element({nodeName: "TR", innerHTML: "sample HTML"});
    paragraph.appendChild(new Element({nodeName: "#text"}));
    paragraph.appendChild(table);
    table.appendChild(new Element({nodeName: "#text"}));
    table.appendChild(body);
    body.appendChild(new Element({nodeName: "#text"}));
    body.appendChild(row);
    document.newElements = [paragraph];

    var treeRow = new Fiz.TreeRow("tree_44", "html #1", "html #2");
    treeRow.replace("replacement");
    assertEqual("document.createElement(p)\n", jsunit.log, "jsunit log");
    assertEqual("<table>replacement</table>",
            document.createdElements[0].innerHTML,
            "HTML for temporary table");
    assertEqual("sample HTML",
            parent.firstChild.innerHTML, "new contents of table row");
};
