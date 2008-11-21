// AjaxTest.js --
//
// Jsunit tests for Ajax.js, organized in the standard fashion.

include("fizlib/Fiz.js");
include("fizlib/Ajax.js");
include("XmlHttpFixture.js");

window.XMLHttpRequest = true;
test("Ajax_sendRequest_urlParameterOnly", function() {
    new Fiz.Ajax("/a/b");
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/fiz; " +
            "charset=utf-8)\n" +
            "send(message: )\n",
            jsunit.log, "jsunit.log");
});
test("Ajax_sendRequest_propertiesParameter", function() {
    ajax = new Fiz.Ajax({url: "/a/b", errorHandler: "test44"});
    assertEqual("/a/b", ajax.url, "url parameter");
    assertEqual("test44", ajax.errorHandler, "errorHandler parameter");
});
test("Ajax_cantFindXmlHttp", function() {
    window.XMLHttpRequest = null;
    new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    assertEqual("alert(message: Error in Ajax request for /a/b: " +
            "couldn't create XMLHttpRequest object)\n",
            jsunit.log, "jsunit.log");
});
window.XMLHttpRequest = true;
test("Ajax_sendRequestWithData", function() {
    new Fiz.Ajax({url: "/a/b", data: {name: "Alice", age: 28}});
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/fiz; " +
            "charset=utf-8)\n" +
            "send(message: main.(3.age2.28\n" +
            "4.name5.Alice))\n",
            jsunit.log, "jsunit.log");
});
test("Ajax_sendRequestWithReminder", function() {
    new Fiz.Ajax({url: "/a/b", reminders: "first reminder"});
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/fiz; " +
            "charset=utf-8)\n" +
            "send(message: reminder.first reminder)\n",
            jsunit.log, "jsunit.log");
});
test("Ajax_multipleReminders", function() {
    new Fiz.Ajax({url: "/a/b", reminders: ["reminder #1", "reminder #2",
            "reminder #3"]});
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/fiz; " +
            "charset=utf-8)\n" +
            "send(message: reminder.reminder #1reminder.reminder #2" +
            "reminder.reminder #3)\n",
            jsunit.log, "jsunit.log");
});
test("Ajax_setOnChangeHandler", function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.xmlhttp.onreadystatechange();
    assertEqual("alert(message: Error in Ajax request for /a/b: HTTP " +
            "error 400: sample status message)\n",
            jsunit.log, "jsunit.log");
});

test("stateChange_requestNotComplete", function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.xmlhttp.readyState = 2;
    ajax.stateChange();
    assertEqual("", jsunit.log, "jsunit.log");
});
test("stateChange_clearOnReadyStateChange", function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.xmlhttp.readyState = 4;
    ajax.stateChange();
    assertEqual("true", (ajax.xmlhttp.onreadystatechange == null).toString(),
            "onreadystatechange == null");
});
test("stateChange_httpError", function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: HTTP " +
            "error 400: sample status message)\n",
            jsunit.log, "jsunit.log");
});
test("stateChange_errorInResponseJavascript", function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "x(;";
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: error " +
            "processing responseText (javascript/fizlib/Ajax.js(eval):1): " +
            "SyntaxError: syntax error)\n",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});
test("stateChange_updateAction", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"update\", " +
            "id: \"element1\", html: \"new text\"}]";
    ajax.stateChange();
    assertEqual("", jsunit.log, "jsunit.log");
    assertEqual("new text", document.getElementById("element1").innerHTML,
            "innerHTML for updated element");
});
test("stateChange_updateAction_noSuchId", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"update\", " +
            "id: \"bogus\", html: \"new text\"}]";
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: error " +
        "processing responseText: nonexistent element \"bogus\" in " +
        "update action)\n", jsunit.log, "jsunit.log");
});
test("stateChange_evalAction", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"eval\", " +
            "javascript: \"alert('Test');\"}]";
    ajax.stateChange();
    assertEqual("alert(message: Test)\n", jsunit.log, "jsunit.log");
});
test("stateChange_evalAction_syntaxError", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"eval\", " +
            "javascript: \"x(;\"}]";
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: error " +
            "processing responseText (javascript/fizlib/Ajax.js(eval):1): " +
            "SyntaxError: syntax error)\n",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});
test("stateChange_redirectAction", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"redirect\", " +
            "url: \"/x/y/z\"}]";
    ajax.stateChange();
    assertEqual("/x/y/z", document.location.href, "document.location.href");
});
test("stateChange_errorAction", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"error\", " +
            "properties: {age: 24,  message: \"something broke\"}}]";
    ajax.stateChange();
    assertEqual("alert(message: something broke)\n", jsunit.log,
            "jsunit.log");
});
test("stateChange_exceptionWithLineNumber", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"eval\", " +
            "javascript: \"x(;\"}]";
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: error " +
            "processing responseText (javascript/fizlib/Ajax.js(eval):1): " +
            "SyntaxError: syntax error)\n",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});
test("stateChange_exceptionWithoutLineNumber", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"eval\", " +
            "javascript: \"throw 'test exception';\"}]";
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: error " +
            "processing responseText: test exception)\n",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});

test("error_externalHandler", function() {
    document = new Document();
    var handler = new Object();
    handler.ajaxError = function(properties) {
        jsunit.log += "ajaxHandler(properties.message: " +
                properties.message + ")\n";
    }
    var ajax = new Fiz.Ajax({url: "/a/b", errorHandler: handler}, {});
    jsunit.log = "";
    ajax.error({message: "earthquake"});
    assertEqual("ajaxHandler(properties.message: earthquake)\n",
            jsunit.log, "jsunit.log");
});
test("error_defaultHandler", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {});
    jsunit.log = "";
    ajax.error({message: "earthquake"}, false);
    assertEqual("alert(message: earthquake)\n",
            jsunit.log, "jsunit.log");
});

test("serialize", function() {
    var result = Fiz.Ajax.serialize({a: 14, b: "test string",
            object: {name: "Alice", age: 44},
            array: [{name: "Bill"}, {name: "Carol"}, {name: "David"}],
            empty: [],
            last: 1234.56});
    assertEqual("(5.array(4.name4.Bill)(4.name5.Carol)(4.name5.David)\n" +
            "4.last7.1234.56\n" +
            "6.object(3.age2.44\n" +
            "4.name5.Alice)\n" +
            "1.a2.14\n" +
            "1.b11.test string)", result);
});
test("serialize_undefinedObject", function() {
    var result = Fiz.Ajax.serialize(undefined);
    assertEqual("()", result);
});
