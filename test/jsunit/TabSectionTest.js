// TabSectionTest.js --
//
// Jsunit tests for TabSection.js, organized in the standard fashion.

include("fizlib/Fiz.js");
include("fizlib/TabSection.js");

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
}

test("TabSection_selectTab", function() {
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
});
test("TabSection_selectTab_alreadySelected", function() {
    document = new Document();
    var parent = new Element();
    parent.appendChild(new Element({className: "leftSelected"}));
    parent.appendChild(document.addElementWithId("999", {className: "mid"}));
    parent.appendChild(new Element({className: "rightSelected"}));
    Fiz.TabSection.selectTab("999");
    assertEqual("leftSelected, midSelected, rightSelected",
            getChildClasses(parent), "new sibling classes");
});
test("TabSection_selectTab_noSiblingsToSelect", function() {
    document = new Document();
    var parent = new Element();
    parent.appendChild(document.addElementWithId("999", {className: "mid"}));
    Fiz.TabSection.selectTab("999");
    assertEqual("midSelected",
            getChildClasses(parent), "new sibling classes");
});