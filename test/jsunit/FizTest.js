// FizTest.js --
//
// Jsunit tests for Fiz.js, organized in the standard fashion.

include("fizlib/Fiz.js");

FizTest = {};

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
    var bulletin = document.addElementWithId("bulletin",
            {innerHTML: "sample contents"});
    Fiz.clearBulletin();
    assertEqual("id:        bulletin\n" +
            "innerHTML: \"\"\n" +
            "style:\n" +
            "    display: none\n",
            bulletin.toString(),
            "contents of bulletin");
};
