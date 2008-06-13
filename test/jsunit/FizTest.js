// FizTest.js --
//
// Jsunit tests for Fiz.js, organized in the standard fashion.

include("Fiz.js");

test("Fiz_setBulletin_createBulletin", function() {
    document = new Document();
    Fiz.setBulletin("<div class=\"error\"> Message #1</div>");
    assertEqual("className:      bulletin\n" +
            "id:             bulletin\n" +
            "innerHTML:      <div class=\"error\"> Message #1</div>\n" +
            "tagName:        div\n",
            printDataset(document.createdElements[0]),
            "contents of bulletin");
    assertEqual("body.insertBefore(bulletin, paragraph1)\n", jsunit.log,
            "jsunit log");
});
test("Fiz_setBulletin_bulletinExists", function() {
    document = new Document();
    var bulletin = document.addElementWithId("bulletin", {tagName: "div"});
    Fiz.setBulletin("<div class=\"error\"> Message #1</div>");
    assertEqual("id:             bulletin\n" +
            "innerHTML:      <div class=\"error\"> Message #1</div>\n" +
            "tagName:        div\n",
            printDataset(bulletin),
            "contents of bulletin");
    assertEqual("", jsunit.log, "jsunit log");
});