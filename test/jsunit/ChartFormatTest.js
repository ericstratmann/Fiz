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

// ChartFormatTest.js --
//
// Jsunit tests for ChartFormat.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartFormat.js");
include("CanvasFixture.js");

ChartFormatTest = {};

ChartFormatTest.setUp = function () {
    this.canvas = new Canvas();
    this.ctx = this.canvas.getContext("2d");
    this.format = new Fiz.Chart.Format(this.ctx, ["1em arial", "green"]);
};

ChartFormatTest.test_width = function () {
    this.format.save = chartLogFunction("save");
    this.format.restore = chartLogFunction("restore");

    assertEqual(6, this.format.width("foo"), "return");
    assertEqual("save()\nmeasureText(foo)\nrestore()\n", jsunit.log, "log");
};

ChartFormatTest.test_height = function () {
    this.format.save = chartLogFunction("save");
    this.format.restore = chartLogFunction("restore");
    
    assertEqual(2, this.format.height("foo"), "return");
    assertEqual("save()\nmeasureText(M)\nrestore()\n", jsunit.log);
};

ChartFormatTest.test_draw = function() {
    this.format.save = chartLogFunction("save");
    this.format.restore = chartLogFunction("restore");
    
    this.format.draw("foo");
    assertEqual("save()\nfillStyle = green\nfillText(foo, 0, 0)\nrestore()\n",
                jsunit.log);
};

ChartFormatTest.test_save = function () {
    this.ctx.font = "foo";
    jsunit.log = "";

    this.format.save();
    assertEqual(undefined, this.format.oldFont, "old font"); // bug in setter code
    assertEqual("font = 1em arial\n", jsunit.log, "cfx font");
};

ChartFormatTest.test_restore = function () {
    this.format.oldFont = "foo";
    this.format.restore();
    assertEqual("font = foo\n", jsunit.log, "cfx font");
};
