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

// ChartTitleTest.js --
//
// Jsunit tests for ChartTitle.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");
include("CanvasFixture.js");
include("static/fiz/Chart.js");
include("ChartFormatFixture.js");
include("static/fiz/ChartTitle.js");

ChartTitleTest = {};

ChartTitleTest.setUp = function () {
    this.canvas = new Canvas();
    this.ctx = this.canvas.getContext('2d');
    this.format = ["foo", "blue"];
    this.box = {
        width: 100,
        height: 50,
        side: "bottom"
    };
};

ChartTitleTest.test_render_noTitle = function() {
    var ts = new Fiz.Chart.Title(this.ctx, null, this.format);
    ts.render(this.box);
    assertEqual("", jsunit.log);
};

ChartTitleTest.test_render = function () {
    var ts = new Fiz.Chart.Title(this.ctx, "foo", this.format);
    ts.moveAndOrient = logFunction("moveAndOrient");
    ts.render(this.box);

    assertEqual("Format(foo, blue)\nsave()\nmoveAndOrient([object Object])\ntranslate(40, 30)\n" +
                "draw(foo)\nrestore()\n", jsunit.log);
};

ChartTitleTest.test_sizeRequired_noTitle = function () {
    var ts = new Fiz.Chart.Title(this.ctx, null, this.format);
    assertEqual(0, ts.sizeRequired());
};

ChartTitleTest.test_sizeRequired = function () {
    var ts = new Fiz.Chart.Title(this.ctx, "foo", this.format);
    assertEqual(40, ts.sizeRequired());
};

ChartTitleTest.test_moveAndOrient_left= function () {
    var ts = new Fiz.Chart.Title(this.ctx, "foo", this.format);
    this.box.side = "left";
    ts.moveAndOrient(this.box);
    assertEqual("rotate(-" + Math.PI/2 + ")\n", jsunit.log);
};

ChartTitleTest.test_moveAndOrient_bottom = function () {
    var ts = new Fiz.Chart.Title(this.ctx, "foo", this.format);
    this.box.side = "bottom";
    ts.moveAndOrient(this.box);
    assertEqual("translate(0, -50)\n", jsunit.log);
};
    
ChartTitleTest.test_moveAndOrient_right = function () {
    var ts = new Fiz.Chart.Title(this.ctx, "foo", this.format);
    this.box.side = "right";
    ts.moveAndOrient(this.box);
    assertEqual("rotate(-" + Math.PI/2 + ")\n", jsunit.log);
};
    
ChartTitleTest.test_moveAndOrient_top = function () {
    var ts = new Fiz.Chart.Title(this.ctx, "foo", this.format);
    this.box.side = "top";
    ts.moveAndOrient(this.box);
    assertEqual("", jsunit.log);
};
