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
