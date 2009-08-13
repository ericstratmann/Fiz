/* FizTest.js --
 *
 * Jsunit tests for Fiz.js, organized in the standard fashion.
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
 * ACTION OF CONTRACT, NEGLIGENCE OR otherFields TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

include("static/fiz/Fiz.js");

FizTest = {};

FizTest.test_addBulletinMessage_clearOldBulletin = function() {
    var bulletin = document.addElementWithId("bulletin", {tagName: "div",
            innerHTML: "old contents"});
    Fiz.clearOldBulletin = true;
    Fiz.addBulletinMessage("Message #1");
    assertEqual("Message #1", bulletin.innerHTML, "bulletin innerHTML");
    assertEqual(false, Fiz.clearOldBulletin, "Fiz.clearOldBulletin");
};

FizTest.test_addClass = function() {
    var target = document.addElementWithId("target", {className: ''});
    Fiz.addClass(target, "first_class");
    assertEqual("first_class", target.className);
    Fiz.addClass(target, "second_class");
    assertEqual("first_class second_class", target.className);
    Fiz.addClass(target, "first_class");
    assertEqual("first_class second_class", target.className);
}

FizTest.test_addEvent_firefox = function() {
    var firefox = { addEventListener: logFunction("addEventListener") };
    Fiz.addEvent(firefox, "mouseover", "callback", false);
    assertEqual("addEventListener(mouseover, \n" +
            "function (event) {\n" +
            "    callback.call(thisObj, event);\n" +
            "}\n" +
            ", false)\n", jsunit.log, "firefox");
}

FizTest.test_addEvent_ie = function() {
    jsunit.log = "";
    var ie = { attachEvent: logFunction("attachEvent") };
    Fiz.addEvent(ie, "mouseover", "callback", false);
    assertEqual("attachEvent(onmouseover, \n" +
            "function () {\n" +
            "    callback.call(thisObj, window.event);\n" +
            "}\n)\n", jsunit.log, "ie");
}

FizTest.test_addEvent_old = function() {
    jsunit.log = "";
    var old = {};
    window.event = "test";
    Fiz.addEvent(old, "mouseover", logFunction("callback"), false);
    old.onmouseover();
    assertEqual("callback(test)\n", jsunit.log, "old");
}

FizTest.test_addBulletinMessage_createBulletin = function() {
    Fiz.addBulletinMessage("Message #1");
    assertEqual("className:   bulletin\n" +
            "id:          bulletin\n" +
            "innerHTML:   Message #1\n" +
            "nextSibling: paragraph1\n" +
            "parentNode:  unknown element\n" +
            "style:\n" +
            "    display: \"\"\n" +
            "tagName:     div\n",
            document.body.firstChild.toString(),
            "contents of first element in body");
};

FizTest.test_addBulletinMessage_bulletinExists = function() {
    var bulletin = document.addElementWithId("bulletin", {tagName: "div",
            display: "none", innerHTML: "old contents"});
    Fiz.addBulletinMessage("Message #1");
    assertEqual("old contentsMessage #1", bulletin.innerHTML,
            "updated innerHTML");
}

FizTest.test_cancelBubble = function() {
    var ff_event = {
            stopPropagation: logFunction("stopPropogation"),
            cancelBubble: false
    };
    Fiz.cancelBubble(ff_event);
    assertEqual(false, ff_event.cancelBubble, "Cancel bubble");
    assertEqual("stopPropogation()\n", jsunit.log, "Stop propagation")

    var ie_event = {
        cancelBubble: false
    };
    Fiz.cancelBubble(ie_event);
    assertEqual(true, ie_event.cancelBubble, "Cancel bubble");
    assertEqual("stopPropogation()\n", jsunit.log, "Stop propagation")
}

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

FizTest.test_findAbsolutePosition = function() {
    var superParent = document.addElementWithId("superParent",
            {offsetLeft: 0, offsetTop: 0, offsetParent: null});
    var parent = document.addElementWithId("parent",
            {offsetLeft: 15, offsetTop: 12, offsetParent: superParent});
    var child = document.addElementWithId("child",
            {offsetLeft: 32, offsetTop: 2, offsetParent: parent});

    assertEqual("0,0", Fiz.findAbsolutePosition(superParent).join(","), "super parent");
    assertEqual("15,12", Fiz.findAbsolutePosition(parent).join(","), "parent");
    assertEqual("47,14", Fiz.findAbsolutePosition(child).join(","), "child");
}

FizTest.test_getKeyCode = function() {
    var ff_event = {which: 13};
    assertEqual(13, Fiz.getKeyCode(ff_event), "e.which");

    var ie_event = {keyCode: 42};
    assertEqual(42, Fiz.getKeyCode(ie_event), "e.keyCode");
}

FizTest.test_getText = function() {
    var innerText = document.addElementWithId("innerText", {innerText: "inner"});
    assertEqual("inner", Fiz.getText(innerText), "innerText");

    var textContent = document.addElementWithId("textContent", {textContent: "text"});
    assertEqual("text", Fiz.getText(textContent), "textContent");
}

FizTest.test_removeClass = function() {
    var target = document.addElementWithId("target", {className: ''});
    Fiz.addClass(target, "first_class");
    Fiz.addClass(target, "second_class");
    Fiz.addClass(target, "third_class");

    Fiz.removeClass(target, "fake_class");
    assertEqual("first_class second_class third_class", target.className);
    Fiz.removeClass(target, "second_class");
    assertEqual("first_class third_class", target.className);
    Fiz.removeClass(target, "first_class");
    assertEqual("third_class", target.className);
    Fiz.removeClass(target, "third_class");
    assertEqual("", target.className);
}

FizTest.test_setText = function() {
    var textContent = document.addElementWithId("textContent", {textContent: "text"});
    Fiz.setText(textContent, "text_changed");
    assertEqual("text_changed", textContent.textContent, "textContent");

    var innerText = document.addElementWithId("innerText", {innerText: "inner"});
    Fiz.setText(innerText, "inner_changed");
    assertEqual("inner_changed", innerText.innerText, "innerText");
}