/* FormSectionTest.js --
 *
 * Jsunit tests for FormSection.js, organized in the standard fashion.
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
include("static/fiz/FormElement.js");
include("static/fiz/FormSection.js");

// Following is a replacement for the Ajax constructor; it just logs
// information about some of its arguments.

Fiz.Ajax = function(properties) {
    jsunit.log += "Fiz.Ajax url: " + properties.url + ", data:\n" +
            printDataset(properties.data, "    ");
};

FormSectionTest = {};

FormSectionTest.setUp = function() {
    var form = document.addElementWithId("form1");
}

FormSectionTest.test_clearElementError = function() {
    var row1 = document.addElementWithId("row1", {className: "undefined"});
    var diag1 = document.addElementWithId("row1_diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var row2 = document.addElementWithId("row2", {className: "undefined"});
    var diag2 = document.addElementWithId("row2_diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    var form = new Fiz.FormSection("form1");

    form.elementError("row1", "error1");
    form.elementError("row2", "error2");
    form.clearElementError("row1");
    assertEqual("none", diag1.style.display,
            "diag1.style.display after call");
    assertEqual("", row1.className, "row1.className after call");
    assertEqual("", diag2.style.display,
            "diag2.style.display after call");
    assertEqual("formError", row2.className, "row2.className after call");
    assertEqual("row2", form.errorElements.join(", "),
            "form.errorElements after call");
}

FormSectionTest.test_clearElementErrors = function() {
    var row1 = document.addElementWithId("row1", {className: "undefined"});
    var diag1 = document.addElementWithId("row1_diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var row2 = document.addElementWithId("row2", {className: "undefined"});
    var diag2 = document.addElementWithId("row2_diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    var form = new Fiz.FormSection("form1");

    form.elementError("row1", "error1");
    form.elementError("row2", "error2");
    form.clearElementErrors();
    assertEqual("none", diag1.style.display,
            "diag1.style.display after call");
    assertEqual("", row1.className, "row1.className after call");
    assertEqual("none", diag2.style.display,
            "diag2.style.display after call");
    assertEqual("", row2.className, "row2.className after call");
    assertEqual("", form.errorElements.join(", "),
            "form.errorElements after call");
    assertEqual(0, form.errorElements.length, "error elements array");
};

FormSectionTest.test_clearElementErrorDisplay = function() {
    var row1 = document.addElementWithId("row1", {className: "undefined"});
    var diag1 = document.addElementWithId("row1_diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var form = new Fiz.FormSection("form1");

    form.elementError("row1", "error1");
    form.clearElementErrorDisplay("row1");
    assertEqual("none", diag1.style.display,
            "diag1.style.display after call");
    assertEqual("", row1.className, "row1.className after call");
};

FormSectionTest.test_elementError = function() {
    var row1 = document.addElementWithId("row1", {className: "undefined"});
    var diag1 = document.addElementWithId("row1_diagnostic",
        {style: {display: "none"}, innerHTML: "xxx"});
    var row2 = document.addElementWithId("row2", {className: "undefined"});
    var diag2 = document.addElementWithId("row2_diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    var form = new Fiz.FormSection("form1");

    form.elementError("row1", "error1");
    assertEqual("row1", form.errorElements.join(", "),
            "form.errorElements after first call");
    assertEqual("", diag1.style.display,
            "diag1.style.display after first call");
    assertEqual("error1", diag1.innerHTML,
            "diag1.innerHTML after first call");
    assertEqual("formError", row1.className,
            "row.className after first call");

    // Make a second call on a different element, and make sure that
    // errorElements records both elements.
    form.elementError("row2", "error2");
    assertEqual("row1, row2", form.errorElements.join(", "),
            "form.errorElements after second call");
};

FormSectionTest.test_submit = function() {
    var form1 = document.addElementWithId("form1", {target: "undefined"});
    var target1 = document.addElementWithId("form1_target", {innerHTML: "xxx"});
    var form = new Fiz.FormSection("form1");

    assertEqual(true, form.submit(), "return value");
    assertEqual(form, Fiz.FormSection.currentForm,
            "Fiz.FormSection.currentForm");
    assertEqual("<iframe name=\"form1_iframe\"></iframe>",
            target1.innerHTML, "HTML for iframe");
    assertEqual("form1_iframe", form1.target, "form1.target");
};

FormSectionTest.test_submit_setPageId = function() {
    document = new Document();
    var form1 = document.addElementWithId("form1", {target: "undefined"});
    var target1 = document.addElementWithId("form1_target", {innerHTML: "xxx"});
    var form = new Fiz.FormSection("form1");
    var pageId = document.addElementWithId("form1_fizPageId",
            {value: "undefined"});
    Fiz.pageId = "page44";

    assertEqual(true, form.submit(), "return value");
    assertEqual("page44", pageId.value, "page identifier");
};

FormSectionTest.test_submit_nonexistentPageIdElement = function() {
    document = new Document();
    var form1 = document.addElementWithId("form1", {target: "undefined"});
    var target1 = document.addElementWithId("form1_target", {innerHTML: "xxx"});
    var form = new Fiz.FormSection("form1");
    Fiz.pageId = "page44";

    assertEqual(true, form.submit(), "return value");
};

FormSectionTest.test_handleResponse = function() {
    Fiz.ids.form1 = new Fiz.FormSection("form1");
    Fiz.ids.form1.handleResponse2 = function(script) {
        window.testLog = "handleResponse2 argument: " + script;
    }
    Fiz.FormSection.currentForm = Fiz.ids.form1;
    Fiz.FormSection.handleResponse("window.xyzzy += 3;");
    jsunit.setTimeoutArg();
    assertEqual("handleResponse2 argument: window.xyzzy += 3;",
            window.testLog, "log information");
};

FormSectionTest.test_handleResponse2 = function() {
    var target1 = document.addElementWithId("form1_target", {innerHTML: "xxx"});
    var form = new Fiz.FormSection("form1");

    // Create a bulletin message and a form diagnostic so that we can make
    // sure they both get cleared.
    var bulletin = document.addElementWithId("bulletin",
            {innerHTML: "sample contents"});
    var row2 = document.addElementWithId("row2", {className: "undefined"});
    var diag2 = document.addElementWithId("row2_diagnostic",
            {style: {display: "none"}, innerHTML: "yyy"});
    form.elementError("row2", "error1");

    window.xyzzy = 44;
    form.handleResponse2("window.xyzzy += 3;");
    assertEqual("", target1.innerHTML, "HTML for iframe");
    assertEqual(47, window.xyzzy, "incremented variable contents");
};
