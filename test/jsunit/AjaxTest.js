// AjaxTest.js --
//
// Jsunit tests for Ajax.js, organized in the standard fashion.

include("fizlib/Fiz.js");
include("fizlib/Ajax.js");
include("XmlHttpFixture.js");

Fiz.clearBulletin = function() {
    jsunit.log += "clearBulletin();";
}
Fiz.addBulletinMessage = function(className, message) {
    jsunit.log += "addBulletinMessage(\"" + className + "\", \"" +
            message + "\");";
}

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
    ajax = new Fiz.Ajax({url: "/a/b"});
    assertEqual("/a/b", ajax.url, "url parameter");
});
test("Ajax_cantFindXmlHttp", function() {
    window.XMLHttpRequest = null;
    new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: couldn't create " +
            "XMLHttpRequest object\");",
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
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: HTTP error 400: " +
            "sample status message\");",
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
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: HTTP error 400: " +
            "sample status message\");",
            jsunit.log, "jsunit.log");
});
test("stateChange_errorInResponseJavascript", function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "x(;";
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: error in Javascript " +
            "response (web/fizlib/Ajax.js(eval):1): SyntaxError: " +
            "syntax error\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});
test("stateChange_successfulEval", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "alert('Test');";
    ajax.stateChange();
    assertEqual("alert(message: Test)\n", jsunit.log, "jsunit.log");
});
test("stateChange_evalAction_syntaxError", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "x(;";
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: error in Javascript " +
            "response (web/fizlib/Ajax.js(eval):1): " +
            "SyntaxError: syntax error\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});
test("stateChange_exceptionWithLineNumber", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "x(;";
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: error in Javascript response " +
            "(web/fizlib/Ajax.js(eval):1): SyntaxError: " +
            "syntax error\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});
test("stateChange_exceptionWithoutLineNumber", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "throw 'test exception';";
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: error in Javascript " +
            "response: test exception\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});

test("errorr", function() {
    document = new Document();
    var handler = new Object();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {});
    jsunit.log = "";
    ajax.error("message");
    assertEqual("clearBulletin();addBulletinMessage(\"bulletinError\", " +
            "\"Error in Ajax request for /a/b: message\");",
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
