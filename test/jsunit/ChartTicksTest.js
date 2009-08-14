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

// ChartTicksTest.js --
//
// Jsunit tests for ChartTicks.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartTicks.js");
include("CanvasFixture.js");
include("ChartFormatFixture.js");
include("ChartAxisFixture.js");

ChartTicksTest = {};

ChartTicksTest.setUp = function () {
    this.axis = new Fiz.Chart.Axis();
    this.axis.config = {};
    this.canvas = new Canvas();
    this.ctx = this.canvas.getContext('2d');
    this.ticks = new Fiz.Chart.Ticks(this.ctx, this.axis);
};

ChartTicksTest.test_render_discrete = function () {
    this.axis.discrete = true;
    this.axis.xAxis = true;
    this.ticks.drawDiscreteAxis = logFunction("drawDiscreteAxis");
    this.ticks.render(3);
    assertEqual("drawDiscreteAxis(3)\n", jsunit.log);
};

ChartTicksTest.test_render_continuous = function () {
    this.axis.discrete = true;
    this.axis.type = "y";
    this.ticks.drawContinuousAxis = logFunction("drawContinuousAxis");
    this.ticks.render(5);
    assertEqual("drawContinuousAxis(5)\n", jsunit.log);
    jsunit.log = "";

    this.axis.discrete = false;
    this.axis.type = "x";
    this.ticks.drawContinuousAxis = logFunction("drawContinuousAxis");
    this.ticks.render(6);
    assertEqual("drawContinuousAxis(6)\n", jsunit.log);
};

ChartTicksTest.test_drawDiscreteAxis = function () {
    this.ticks.moveAndOrient = logFunction("moveAndOrient");
    this.ticks.drawDiscreteLabels = logFunction("drawDiscreteLabels");
    this.ticks.drawDiscreteGridLines = logFunction("drawDiscreteGridLines");
    this.ticks.drawDiscreteAxis({height: 10});

    assertEqual("save()\nmoveAndOrient([object Object])\ntranslate(10, 0)\n" +
                "drawDiscreteLabels()\ndrawDiscreteGridLines()\nrestore()\n",
                jsunit.log);
};

ChartTicksTest.test_drawDiscreteLabels = function () {
    this.axis.labels = ["a", "b"];
    this.axis.size = 10;
    this.axis.config.barSpacer = 5;
    this.ticks.drawLabel = logFunction("drawLabel");
    this.ticks.drawDiscreteLabels();

    assertEqual("save()\ntranslate(0, -3.75)\ndrawLabel(a)\nrestore()\n" +
                "save()\ntranslate(0, -6.25)\ndrawLabel(b)\nrestore()\n",
                jsunit.log);
};

ChartTicksTest.test_drawDiscreteGridLines = function () {
    this.axis.labels = ["a", "b", "c"];
    this.axis.config.barSpacer = 2;
    this.axis.size = 5;
    this.ticks.drawMajorGridLine = logFunction("drawMajorGridLine");
    this.ticks.drawDiscreteGridLines();

    assertEqual("save()\ntranslate(0, -2)\ndrawMajorGridLine()\nrestore()\n" +
                "save()\ntranslate(0, -3)\ndrawMajorGridLine()\nrestore()\n",
                jsunit.log);

};

ChartTicksTest.test_drawContinuousAxis = function () {
    this.ticks.moveAndOrient = logFunction("moveAndOrient");
    this.ticks.drawMajorGridLine = logFunction("drawMajorGridLine");
    this.ticks.drawMinorTicks = logFunction("drawMinorTicks");

    this.ticks.drawTickMark = logFunction("drawTickMark");
    this.ticks.drawLabel = logFunction("drawLabel");
    this.axis.labels = [1, 2, 3];
    this.axis.size = 6;
    this.axis.config.majorGrid = true;

    this.ticks.drawContinuousAxis({height: 10});

    assertEqual("save()\nmoveAndOrient([object Object])\ntranslate(10, 0)\n" +
                "save()\ntranslate(0, -2)\ndrawMinorTicks(2)\n" +
                "drawTickMark(1)\nFormat(,, undefined)\ndrawLabel(1)\nrestore()\nsave()\n" +
                "translate(0, -4)\ndrawMajorGridLine()\ndrawMinorTicks(4)\n" +
                "drawTickMark(2)\nFormat(,, undefined)\ndrawLabel(2)\nrestore()\nsave()\n" +
                "translate(0, -6)\ndrawMinorTicks(6)\ndrawTickMark(3)\n" +
                "Format(,, undefined)\ndrawLabel(3)\nrestore()\nrestore()\n", jsunit.log);
};

ChartTicksTest.test_sizeRequired_discrete = function () {
    this.axis.discrete = true;
    this.axis.config.labelFont = "1em ariel";
    this.axis.config.labelColor = "black";
    this.axis.labels = ["a", "bc"];
    assertEqual(60, this.ticks.sizeRequired());
};

ChartTicksTest.test_sizeRequired_continuous = function () {
    this.axis.discrete = false;
    this.axis.config.tickLength = 5;
    this.axis.config.labelFont = "1em ariel";
    this.axis.config.labelColor = "black";
    this.axis.labels = ["a", "bc"];
    assertEqual(60, this.ticks.sizeRequired());
};

ChartTicksTest.test_maxSizeLabel = function () {
    this.axis.config.labelFont = "foo";
    this.axis.side = 0;
    assertEqual(20, this.ticks.maxSizeLabel(), "bottom or top");

    this.axis.labels = ["a", "bcd", "fg"];
    this.axis.side = 1;
    assertEqual(20, this.ticks.maxSizeLabel(), "left or right");
};


ChartTicksTest.test_drawTickMark = function () {
    this.axis.config.tickSide = "outside";
    this.axis.config.tickLength = 5;
    this.ticks.drawTickMark(5);

    assertEqual("save()\nbeginPath()\nmoveTo(0, 0)\nlineTo(-5, 0)\n" +
                "stroke()\nrestore()\n", jsunit.log);
};

ChartTicksTest.test_drawTickMark_dontDraw = function () {
    this.axis.config.tickSide = "inside";
    this.axis.config.tickLength = 5;
    this.ticks.drawTickMark(0);

    assertEqual("", jsunit.log);
};

ChartTicksTest.test_drawMajorGridLine_dontDraw = function () {
    this.axis.config.majorGridWidth = 0;

    this.ticks.drawMajorGridLine();
    assertEqual("", jsunit.log);
};

ChartTicksTest.test_drawMajorGridLine = function () {
    this.axis.config.majorGrid = true;
    this.axis.pSize = 10;

    this.ticks.drawMajorGridLine();
    /* drawLineTo
    assertEqual("save()\nbeginPath()\nmoveTo(0, 0)\nlineTo(10, 0)\n" +
                "closePath()\nstroke()\nrestore()\n", jsunit.log);
*/
};

ChartTicksTest.test_drawMinorTicks = function () {
    this.axis.config.tickSide = "inside";
    this.axis.config.minorTicks = 3;
    this.axis.config.minorTickLength = 4;
    this.axis.size = 6;
    this.axis.labels = [1, 2];
    this.axis.max = 6;
    this.axis.min = 4;

    this.ticks.drawMinorTicks(5);

    assertEqual("save()\nbeginPath()\nmoveTo(0, -1)\nlineTo(4, -1)\n" +
                "stroke()\nrestore()\n", jsunit.log);
};

ChartTicksTest.test_moveAndOrient = function () {
    this.axis.size = 10;
    this.axis.oSide = 0;

    this.ticks.moveAndOrient({side: "top"});
    assertEqual("rotate(" + Math.PI/2 + ")\n", jsunit.log, "top");
    jsunit.log = "";

    this.ticks.moveAndOrient({side: "left"});
    assertEqual("", jsunit.log, "left");
    jsunit.log = "";

    this.ticks.moveAndOrient({side: "bottom"});
    assertEqual("rotate(-" + Math.PI/2 + ")\ntransform(1, 0, 0, -1, 0, 0)\n",
                jsunit.log, "top");
    jsunit.log = "";

    this.ticks.moveAndOrient({side: "right", height: 5});
    assertEqual("rotate(" + Math.PI + ")\ntranslate(-5, 0)\n" +
                "transform(1, 0, 0, -1, 0, 0)\n", jsunit.log, "top");
};

ChartTicksTest.test_drawLabel = function () {

};

ChartTicksTest.test_translateForTicks_inside = function () {
    this.axis.config.tickSide = "outside";
    this.axis.config.tickLength = 20;
    this.ticks.translateForTicks();
    assertEqual("translate(-20, 0)\n", jsunit.log);
};


ChartTicksTest.test_translateForTicks_middle = function () {
    this.axis.config.tickSide = "middle";
    this.axis.config.tickLength = 10;
    this.ticks.translateForTicks();
    assertEqual("translate(-5, 0)\n", jsunit.log);
};


ChartTicksTest.test_translateForTicks_outside = function () {
    this.axis.config.tickSide = "inside";
    this.axis.config.tickLength = 30;
    this.ticks.translateForTicks();
    assertEqual("", jsunit.log);
};
