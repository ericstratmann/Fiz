/* Copyright (c) 2009 Stanford University
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
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

// FizCommonTest.js --
//
// Jsunit tests for FizCommon.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");

FizCommonTest = {};

FizCommonTest.test_arrayIndexOf = function () {
    assertEqual(1, Fiz.arrayIndexOf([1,2,3], 2), "found1");
    assertEqual(2, Fiz.arrayIndexOf([1,2,"foo"], "foo"), "found2");
    assertEqual(-1, Fiz.arrayIndexOf([1,2,3], 4), "not found");
};

FizCommonTest.test_rand = function() {
    for (var i = 0; i < 100; i++) {
        var r = Fiz.rand(i % 5 + 1);
        assertEqual(true, r >= 0 && r < i % 5 + 1);
    }
};

FizCommonTest.test_isArray = function() {
    assertEqual(true, Fiz.isArray([1,2,3], "array"));
    assertEqual(false, Fiz.isArray("foo", "string"));
    assertEqual(false, Fiz.isArray({0: 1}, "object"));
};

FizCommonTest.test_deepCopy = function() {
    assertEqual(null, Fiz.deepCopy(null), "null");
    assertEqual(undefined, Fiz.deepCopy(undefined), "undefined");
    assertEqual(5, Fiz.deepCopy(5), "number");

    var a = [2, 3];
    assertEqual(2, Fiz.deepCopy(a)[0], "array 0");
    assertEqual(3, Fiz.deepCopy(a)[1], "array 1");
    assertEqual(2, Fiz.deepCopy(a).length, "array.length");
    assertEqual(false, Fiz.deepCopy(a) === a, "array true deep copy");

    assertEqual(5, Fiz.deepCopy({a: {b: 5}}).a.b , "nested");

    var ctx = {a: 2};
    assertEqual(ctx, Fiz.deepCopy({ctx: ctx}).ctx, "ctx special case");
};

FizCommonTest.test_getRelativeEventCoords_pageCoords_notIE = function() {
    var elem = {};
    FizCommonTest.setup_dom_object(elem);

    var event = {pageX: 30, pageY: 40};
    var coords = Fiz.getRelativeEventCoords(event, elem);
    assertEqual(21, coords.x, "X coordinate");
    assertEqual(23, coords.y, "Y coordinate");
};

FizCommonTest.test_getRelativeEventCoords_pageCoords_IEnonStrict = function() {
    var elem = {};
    FizCommonTest.setup_dom_object(elem);
    document.body.scrollLeft = 100;
    document.body.scrollTop = 0;

    var event = {clientX: 55, clientY: 37};
    var coords = Fiz.getRelativeEventCoords(event, elem);
    assertEqual(146, coords.x, "X coordinate");
    assertEqual(20, coords.y, "Y coordinate");
};

FizCommonTest.test_getRelativeEventCoords_pageCoords_IEstrict = function() {
    var elem = {};
    FizCommonTest.setup_dom_object(elem);
    document.documentElement = {scrollLeft: 10, scrollTop: 244};
    document.body.scrollLeft = 0;
    document.body.scrollTop = 0;

    var event = {clientX: 104, clientY: 20};
    var coords = Fiz.getRelativeEventCoords(event, elem);
    assertEqual(105, coords.x, "X coordinate");
    assertEqual(247, coords.y, "Y coordinate");
};

FizCommonTest.test_getRelativeEventCoords_clientLeftTop = function() {
    var elem = {};
    FizCommonTest.setup_dom_object(elem);
    elem.clientLeft = 0;
    elem.clientTop = 1;
    elem.offsetParent.clientLeft = 2;
    elem.offsetParent.clientTop = 3;
    elem.offsetParent.offsetParent.clientLeft = 4;
    elem.offsetParent.offsetParent.clientTop = 5;

    var event = {pageX: 30, pageY: 40};
    var coords = Fiz.getRelativeEventCoords(event, elem);
    assertEqual(15, coords.x, "X coordinate");
    assertEqual(14, coords.y, "Y coordinate");
};

FizCommonTest.test_getRelativeEventCoords_negativeBodyOffset = function() {
    var elem = {};
    FizCommonTest.setup_dom_object(elem);
    document.body.offsetLeft = -3;
    document.body.offsetTop = -2;

    var event = {pageX: 30, pageY: 40};
    var coords = Fiz.getRelativeEventCoords(event, elem);
    assertEqual(18, coords.x, "X coordinate");
    assertEqual(21, coords.y, "Y coordinate");
};

// Creates an artificial DOM object with some parentNode and offsetParent
// hierarchy.  Also assigns scroll and offset values to several elements.
FizCommonTest.setup_dom_object = function(elem) {
    elem.scrollLeft = 5;
    elem.scrollTop = 2;
    var parentNode1 = {scrollLeft: 0, scrollTop: 0};
    var parentNode2 = {scrollLeft: 1, scrollTop: 1};
    elem.parentNode = parentNode1;
    parentNode1.parentNode = parentNode2;
    parentNode2.parentNode = document.body;

    elem.offsetLeft = 0;
    elem.offsetTop = 0;
    var offsetParent1 = {offsetLeft: 15, offsetTop: 20};
    document.body.offsetLeft = 0;
    document.body.offsetTop = 0;
    elem.offsetParent = offsetParent1;
    offsetParent1.offsetParent = document.body;
    document.body.offsetParent = null;
};
