// FizTest.js --
//
// Jsunit tests for Fiz.js, organized in the standard fashion.
//
// Copyright (c) 2009 Stanford University
// Permission to use, copy, modify, and distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

include("static/fiz/Fiz.js");

FizTest = {};

FizTest.test_addBulletinMessage_clearOldBulletin = function() {
    var bulletin = document.addElementWithId("bulletin", {tagName: "div",
            innerHTML: "old contents"});
    Fiz.clearOldBulletin = true;
    Fiz.addBulletinMessage("error", "Message #1");
    assertEqual("", bulletin.innerHTML, "bulletin innerHTML");
    assertEqual(false, Fiz.clearOldBulletin, "Fiz.clearOldBulletin");
};

FizTest.test_addBulletinMessage_createBulletin = function() {
    Fiz.addBulletinMessage("error", "Message #1");
    assertEqual("className:   bulletin\n" +
            "firstChild:  div#1\n" +
            "id:          bulletin\n" +
            "lastChild:   div#1\n" +
            "nextSibling: paragraph1\n" +
            "parentNode:  unknown element\n" +
            "style:\n" +
            "    display: \"\"\n" +
            "tagName:     div\n",
            document.body.firstChild.toString(),
            "contents of first element in body");
    assertEqual("Child #0:\n" +
            "    className:  error\n" +
            "    innerHTML:  Message #1\n" +
            "    parentNode: bulletin\n" +
            "    style:\n" +
            "    tagName:    div\n",
            document.body.firstChild.printChildren(),
            "children of bulletin");
};

FizTest.test_addBulletinMessage_bulletinExists = function() {
    var bulletin = document.addElementWithId("bulletin", {tagName: "div"});
    Fiz.addBulletinMessage("error", "Message #1");
    assertEqual("Child #0:\n" +
            "    className:  error\n" +
            "    innerHTML:  Message #1\n" +
            "    parentNode: bulletin\n" +
            "    style:\n" +
            "    tagName:    div\n",
            bulletin.printChildren(),
            "contents of bulletin");
};

FizTest.test_clearBulletin = function() {
    Fiz.clearOldBulletin = true;
    var bulletin = document.addElementWithId("bulletin",
            {innerHTML: "sample contents"});
    Fiz.clearBulletin();
    assertEqual("id:        bulletin\n" +
            "innerHTML: \"\"\n" +
            "style:\n" +
            "    display: none\n",
            bulletin.toString(),
            "contents of bulletin");
    assertEqual(false, Fiz.clearOldBulletin, "Fiz.clearOldBulletin");
};

FizTest.test_clearBulletinBeforeNextAdd = function() {
    Fiz.clearOldBulletin = false;
    Fiz.clearBulletinBeforeNextAdd();
    assertEqual(true, Fiz.clearOldBulletin, "Fiz.clearOldBulletin");
};
