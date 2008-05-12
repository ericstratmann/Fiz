// AjaxUnit.js --
//
// Jsunit tests for Ajax.js, organized in the standard fashion.

include ("Ajax.js");
include ("XmlHttpFixture.js");

window.XMLHttpRequest = true;
test("Ajax_sendRequest_urlParameterOnly", function() {
    new Fiz.Ajax("/a/b");
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/plain; " +
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
test("Ajax_sendRequest", function() {
    new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/plain; " +
            "charset=utf-8)\n" +
            "send(message: 3.age2.28\n" +
            "4.name5.Alice\n" +
            ")\n",
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
            "processing responseText (javascript/Ajax.js(eval):1): " +
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
            "processing responseText (javascript/Ajax.js(eval):1): " +
            "SyntaxError: syntax error)\n",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
});
test("stateChange_errorAction", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"error\", " +
            "properties: {age: 24,  message: \"something broke\"}}]";
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: " +
            "something broke)\n", jsunit.log, "jsunit.log");
});
test("stateChange_exceptionWithLineNumber", function() {
    document = new Document();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "actions = [{type: \"eval\", " +
            "javascript: \"x(;\"}]";
    ajax.stateChange();
    assertEqual("alert(message: Error in Ajax request for /a/b: error " +
            "processing responseText (javascript/Ajax.js(eval):1): " +
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
    ajax.error({message: "earthquake"});
    assertEqual("alert(message: Error in Ajax request for /a/b: earthquake)\n",
            jsunit.log, "jsunit.log");
});

test("serialize", function() {
    var result = Fiz.Ajax.serialize({a: 14, b: "test string",
            object: {name: "Alice", age: 44},
            array: [{name: "Bill"}, {name: "Carol"}, {name: "David"}],
            last: 1234.56});
    assertEqual("5.array(4.name4.Bill\n" +
            ")(4.name5.Carol\n" +
            ")(4.name5.David\n" +
            ")\n" +
            "4.last7.1234.56\n" +
            "6.object(3.age2.44\n" +
            "4.name5.Alice\n" +
            ")\n" +
            "1.a2.14\n" +
            "1.b11.test string\n", result);
});
test("serialize_undefinedObject", function() {
    var result = Fiz.Ajax.serialize(undefined);
    assertEqual("", result);
});
