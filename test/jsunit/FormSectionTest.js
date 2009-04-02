// FormSectionTest.js --
//
// Jsunit tests for FormSection.js, organized in the standard fashion.

include("fizlib/Fiz.js");
include("fizlib/FormSection.js");

// Following is a replacement for the Ajax constructor; it just logs
// information about some of its arguments.

Fiz.Ajax = function(properties) {
    jsunit.log += "Fiz.Ajax url: " + properties.url + ", data:\n" +
            printDataset(properties.data, "    ");
}

test("FormSection_clearElementErrors", function() {
    document = new Document();
    var row1 = document.addElementWithId("div1", {className: "undefined"});
    var div1 = document.addElementWithId("div1_diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var row2 = document.addElementWithId("div2", {className: "undefined"});
    var div2 = document.addElementWithId("div2_diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    var form = new Fiz.FormSection("form16");

    form.elementError("div1", "error1");
    form.elementError("div2", "error2");
    form.clearElementErrors();
    assertEqual("none", div1.style.display,
            "div1.style.display after call");
    assertEqual("", row1.className, "row1.className after call");
    assertEqual("none", div2.style.display,
            "div2.style.display after call");
    assertEqual("", row2.className, "row2.className after call");
    assertEqual("", form.errorElements.join(", "),
            "form.errorElements after call");
});

test("FormSection_elementError", function() {
    document = new Document();
    var row1 = document.addElementWithId("div1", {className: "undefined"});
    var div1 = document.addElementWithId("div1_diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var row2 = document.addElementWithId("div2", {className: "undefined"});
    var div2 = document.addElementWithId("div2_diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    var form = new Fiz.FormSection("form16");

    form.elementError("div1", "error1");
    assertEqual("div1", form.errorElements.join(", "),
            "form.errorElements after first call");
    assertEqual("", div1.style.display,
            "div1.style.display after first call");
    assertEqual("error1", div1.innerHTML,
            "div1.innerHTML after first call");
    assertEqual("formError", row1.className,
            "row.className after first call");

    // Make a second call on a different element, and make sure that
    // errorElements records both elements.
    form.elementError("div2", "error2");
    assertEqual("div1, div2", form.errorElements.join(", "),
            "form.errorElements after second call");
});

test("FormSection_submit", function() {
    document = new Document();
    var form1 = document.addElementWithId("form1", {target: "undefined"});
    var div1 = document.addElementWithId("form1_target", {innerHTML: "xxx"});
    var form = new Fiz.FormSection("form1");

    assertEqual(true, form.submit(), "return value");
    assertEqual(form, Fiz.FormSection.currentForm,
            "Fiz.FormSection.currentForm");
    assertEqual("<iframe name=\"form1_iframe\"></iframe>",
            div1.innerHTML, "HTML for iframe");
    assertEqual("form1_iframe", form1.target, "form1.target");
});

test("FormSection_handleResponse", function() {
    Fiz.ids.form1 = new Fiz.FormSection("formxx");
    Fiz.ids.form1.handleResponse2 = function(script) {
        window.testLog = "handleResponse2 argument: " + script;
    }
    Fiz.FormSection.currentForm = Fiz.ids.form1;
    Fiz.FormSection.handleResponse("window.xyzzy += 3;");
    jsunit.setTimeoutArg();
    assertEqual("handleResponse2 argument: window.xyzzy += 3;",
            window.testLog, "log information");
});

test("FormSection_handleResponse2", function() {
    document = new Document();
    var div1 = document.addElementWithId("form1_target", {innerHTML: "xxx"});
    var form = new Fiz.FormSection("form1");
    window.xyzzy = 44;
    form.handleResponse2("window.xyzzy += 3;");
    assertEqual("", div1.innerHTML, "HTML for iframe");
    assertEqual("47", window.xyzzy, "incremented variable contents");
});