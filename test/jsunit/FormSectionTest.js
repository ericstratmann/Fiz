// FormSectionTest.js --
//
// Jsunit tests for FormSection.js, organized in the standard fashion.

include("fizlib/Fiz.js");
include("fizlib/FormSection.js");

// Following is a replacement for the Ajax constructor; it just logs
// information about its arguments.

Fiz.Ajax = function(url, data) {
    jsunit.log += "Fiz.Ajax url: " + url + ", data:\n" +
            printDataset(data, "    ");
}

test("FormSection_post", function() {
    document = new Document();
    document.addElementWithId("form16", {length: 17, elements: [
        {tagName: "input",    type: "button",     name: "b1", value: "b1v"},
        {tagName: "input",    type: "checkbox",   name: "c1", value: "c1v",
            checked: false},
        {tagName: "input",    type: "checkbox",   name: "c2", value: "c2v",
            checked: true},
        {tagName: "input",    type: "file",       name: "f1", value: "f1v"},
        {tagName: "input",    type: "hidden",     name: "h1", value: "h1v"},
        {tagName: "input",    type: "image",      name: "i1"},
        {tagName: "input",    type: "password",   name: "p1", value: "p1v"},
        {tagName: "input",    type: "radio",      name: "r1", value: "r1_a",
            checked: true},
        {tagName: "input",    type: "radio",      name: "r1", value: "r1_b",
            checked: false},
        {tagName: "input",    type: "reset",      name: "rs1", value: "rs1v"},
        {tagName: "input",    type: "submit",     name: "s1", value: "s1v"},
        {tagName: "input",    type: "text",       name: "t1", value: "t1v"},
        {tagName: "button",   type: "???",        name: "b2", value: "b2v"},
        {tagName: "select",   type: "select-one", name: "s2", value: "s2v"},
        {tagName: "select",   type: "select-multiple", name: "s3",
            value: "s3v", options: [
            {selected: false, value: "Arizona"},
            {selected: true,  value: "California"},
            {selected: true,  value: "Nevada"},
            {selected: false, value: "Oregon"},
            {selected: true,  value: "Washington"}]},
        {tagName: "textarea", type: "textarea",   name: "t2", value: "t2v"},
        {tagName: "unknown",  type: "unknown",    name: "xx", value: "999"}
    ]});
    var form = new Fiz.FormSection("form16", "/a/b/c");
    form.post();
    assertEqual("Fiz.Ajax url: /a/b/c, data:\n" +
            "    c2: c2v\n" +
            "    h1: h1v\n" +
            "    p1: p1v\n" +
            "    r1: r1_a\n" +
            "    s2: s2v\n" +
            "    s3:\n" +
            "      - value: California\n" +
            "      - value: Nevada\n" +
            "      - value: Washington\n" +
            "    t1: t1v\n" +
            "    t2: t2v\n",
            jsunit.log, "jsunit.log");
});

test("FormSection_elementError", function() {
    document = new Document();
    var div1 = document.addElementWithId("div1.diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var div2 = document.addElementWithId("div2.diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    var form = new Fiz.FormSection("form16", "/a/b/c");

    form.elementError("div1", "error1");
    assertEqual("error1", div1.innerHTML,
            "div1.innerHTML after first call");
    assertEqual("", div1.style.display,
            "div1.style.display after first call");
    assertEqual("1", form.visibleDiagnostics.length,
            "form.visibleDiagnostics length");
    assertEqual("div1.diagnostic", form.visibleDiagnostics[0].id,
            "form.visibleDiagnostics[0] after first call");

    // Make a second call on a different element, and make sure that
    // visibleDiagnostics records both elements.
    form.elementError("div2", "error2");
    assertEqual("error1",div1.innerHTML,
            "div1.innerHTML after second call");
    assertEqual("2", form.visibleDiagnostics.length,
            "form.visibleDiagnostics length");
    assertEqual("div1.diagnostic", form.visibleDiagnostics[0].id,
            "form.visibleDiagnostics[0] after first call");
    assertEqual("div2.diagnostic", form.visibleDiagnostics[1].id,
            "form.visibleDiagnostics[1] after first call");
});

test("FormSection_clearElementErrors", function() {
    document = new Document();
    var div1 = document.addElementWithId("div1.diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var div2 = document.addElementWithId("div2.diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    var form = new Fiz.FormSection("form16", "/a/b/c");

    form.elementError("div1", "error1");
    form.elementError("div2", "error2");
    form.clearElementErrors();
    assertEqual("none", div1.style.display,
            "div1.style.display after call");
    assertEqual("none", div2.style.display,
            "div2.style.display after call");
    assertEqual("0", form.visibleDiagnostics.length,
            "form.visibleDiagnostics.length after call");
});