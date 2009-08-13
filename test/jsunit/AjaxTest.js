/* AjaxTest.js --
 *
 * Jsunit tests for Ajax.js, organized in the standard fashion.
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
include("static/fiz/Ajax.js");
include("XmlHttpFixture.js");

AjaxTest = {};

Fiz.clearBulletin = function() {
    jsunit.log += "clearBulletin();";
}
Fiz.addBulletinMessage = function(message) {
    jsunit.log += "addBulletinMessage(\"" + message + "\");";
}
window.XMLHttpRequest = true;

AjaxTest.test_constructor_sendRequest_urlParameterOnly = function() {
    new Fiz.Ajax("/a/b");
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/fiz; " +
            "charset=utf-8)\n" +
            "send(message: main.())\n",
            jsunit.log, "jsunit.log");
};

AjaxTest.test_constructor_sendRequest_propertiesParameter = function() {
    ajax = new Fiz.Ajax({url: "/a/b"});
    assertEqual("/a/b", ajax.url, "url parameter");
};

AjaxTest.test_constructor_cantFindXmlHttp = function() {
    window.XMLHttpRequest = null;
    new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    assertEqual("clearBulletin();addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: couldn't create " +
            "XMLHttpRequest object</div>\");",
            jsunit.log, "jsunit.log");
    window.XMLHttpRequest = true;
};

AjaxTest.test_constructor_sendRequestWithData = function() {
    new Fiz.Ajax({url: "/a/b", data: {name: "Alice", age: 28}});
    assertEqual("open(method: POST, url: /a/b, async: undefined)\n" +
            "setRequestHeader(name: Content-type, value: text/fiz; " +
            "charset=utf-8)\n" +
            "send(message: main.(4.name5.Alice\n" +
            "3.age2.28))\n", jsunit.log, "jsunit.log");
};

AjaxTest.test_constructor_setOnChangeHandler = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.xmlhttp.onreadystatechange();
    assertEqual("clearBulletin();addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: HTTP error 400: " +
            "sample status message</div>\");",
            jsunit.log, "jsunit.log");
};

AjaxTest.test_constructor_setClearBulletin = function() {
    Fiz.clearOldBulletin = false;
    var ajax = new Fiz.Ajax("/a/b");
    assertEqual(true, Fiz.clearOldBulletin, "Fiz.clearOldBulletin");
};

AjaxTest.test_stateChange_requestNotComplete = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.xmlhttp.readyState = 2;
    ajax.stateChange();
    assertEqual("", jsunit.log, "jsunit.log");
};

AjaxTest.test_stateChange_clearOnReadyStateChange = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.xmlhttp.readyState = 4;
    ajax.stateChange();
    assertEqual("true", (ajax.xmlhttp.onreadystatechange == null).toString(),
            "onreadystatechange == null");
};

AjaxTest.test_stateChange_httpError = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.status = 400;
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: HTTP error 400: " +
            "sample status message</div>\");",
            jsunit.log, "jsunit.log");
};

AjaxTest.test_stateChange_errorInResponseJavascript = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "x(;";
    ajax.stateChange();
    assertEqual("clearBulletin();" +
            "addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: error in Javascript " +
            "response (web/static/fiz/Ajax.js(eval):1): SyntaxError: " +
            "syntax error</div>\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
};

AjaxTest.test_stateChange_successfulEval = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "alert('Test');";
    ajax.stateChange();
    assertEqual("alert(message: Test)\n", jsunit.log, "jsunit.log");
};

AjaxTest.test_stateChange_evalAction_syntaxError = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "x(;";
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: error in Javascript " +
            "response (web/static/fiz/Ajax.js(eval):1): " +
            "SyntaxError: syntax error</div>\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
};

AjaxTest.test_stateChange_exceptionWithLineNumber = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "x(;";
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: error in Javascript response " +
            "(web/static/fiz/Ajax.js(eval):1): SyntaxError: " +
            "syntax error</div>\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
};

AjaxTest.test_stateChange_exceptionWithoutLineNumber = function() {
    var ajax = new Fiz.Ajax({url: "/a/b"}, {name: "Alice", age: 28});
    jsunit.log = "";
    ajax.xmlhttp.responseText = "throw 'test exception';";
    ajax.stateChange();
    assertEqual("clearBulletin();addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: error in Javascript " +
            "response: test exception</div>\");",
            jsunit.log.replace(/\.js#\d*\(/, ".js("), "jsunit.log");
};

AjaxTest.test_error = function() {
    document = new Document();
    var handler = new Object();
    var ajax = new Fiz.Ajax({url: "/a/b"}, {});
    jsunit.log = "";
    ajax.error("message");
    assertEqual("clearBulletin();addBulletinMessage(" +
            "\"<div class=\"bulletinError\">" +
            "Error in Ajax request for /a/b: message</div>\");",
            jsunit.log, "jsunit.log");
};

AjaxTest.test_serialize = function() {
    var result = Fiz.Ajax.serialize({a: 14, b: "test string",
            object: {name: "Alice", age: 44},
            array: [{name: "Bill"}, {name: "Carol"}, {name: "David"}],
            empty: [],
            last: 1234.56});
    assertEqual("(1.a2.14\n" +
            "1.b11.test string\n" +
            "6.object(4.name5.Alice\n" +
            "3.age2.44)\n" +
            "5.array(4.name4.Bill)(4.name5.Carol)(4.name5.David)\n" +
            "4.last7.1234.56)", result);
};

AjaxTest.test_serialize_undefinedObject = function() {
    var result = Fiz.Ajax.serialize(undefined);
    assertEqual("()", result);
};

AjaxTest.test_serialize_authAndPageId = function() {
    Fiz.auth = "auth100";
    Fiz.pageId = "pageId99";
    var result = Fiz.Ajax.serialize({name: "Alice"});
    assertEqual("(4.name5.Alice\n" +
            "8.fiz_auth7.auth100\n" +
            "10.fiz_pageId8.pageId99)", result);
    Fiz.auth = null;
    Fiz.pageId = null;
};
