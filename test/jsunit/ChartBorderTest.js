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

include("static/fiz/Fiz.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartBorder.js");
include("CanvasFixture.js");

ChartBorderTest = {};

/*
ChartBorderTest.setUp = function () {
	this.canvas = new Canvas();
	this.ctx = this.canvas.getContext('2d');
};

ChartBorderTest.test_draw_basic = function () {
	var border = new Fiz.Chart.Border(this.ctx, 10);
	border.drawLineTo = logFunction("lineTo");
	border.draw(1, 2, 3, 4);
	assertEqual("save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(-4, 2)\n" + 

				"stroke()\nrestore()\n", jsunit.log);
};

ChartBorderTest.test_draw_side = function () {
	var border = new Fiz.Chart.Border(this.ctx, 10, undefined, "inside");
	border.drawLineTo = logFunction("drawLineTo");
	border.draw(1, 2, 3, 4);
	assertEqual("save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(1, 7)\n" + 
				"drawLineTo(-1, 7)\ndrawLineTo(-1, 1)\ndrawLineTo(6, 1)\n" +
				"drawLineTo(6, 7)\nstroke()\nrestore()\n", jsunit.log);
};


ChartBorderTest.test_draw_withSelectBorder = function () {
	var border = new Fiz.Chart.Border(this.ctx, 10, "blue", "normal",
									   [true, false, true, false]);
	border.drawLineTo = logFunction("drawLineTo");
	border.draw(1, 2, 3, 4);
	assertEqual("save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(-4, 2)\n" + 
				"drawLineTo(4, 2)\nmoveTo(4, 6)\ndrawLineTo(1, 6)\n" +
				"moveTo(1, 2)\nstroke()\nrestore()\n", jsunit.log);
};

ChartBorderTest.test_draw_withBackGround = function () {
	var border = new Fiz.Chart.Border(this.ctx, 10);
	border.drawLineTo = logFunction("drawLineTo");
	border.draw(1, 2, 3, 4, "black");
	assertEqual("save()\nfillStyle = black\nfillRect(1, 2, 3, 4)\nrestore()\n" +
				"save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(-4, 2)\n" +
				"drawLineTo(4, 2)\ndrawLineTo(4, 6)\ndrawLineTo(1, 6)\n" +
				"drawLineTo(1, 2)\nstroke()\nrestore()\n", jsunit.log);
};

ChartBorderTest.test_drawLineTo_noBorder = function () {
	var border = new Fiz.Chart.Border(this.ctx);
	border.drawLineTo(1, 2);
	assertEqual("lineWidth = 1\nstrokeStyle = black\nlineTo(1, 2)\n",
				jsunit.log);
};

ChartBorderTest.test_drawLineTo_withBorder = function () {
	var border = new Fiz.Chart.Border(this.ctx, 5, "green");
	border.drawLineTo(1, 2);
	assertEqual("lineWidth = 5\nstrokeStyle = green\nlineTo(1, 2)\n",
				jsunit.log);
};
*/
