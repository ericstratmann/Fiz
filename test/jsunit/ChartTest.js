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

// ChartTest.js --
//
// Jsunit tests for Chart.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartSeries.js");
include("ChartFormatFixture.js");
include("ChartPlotFixture.js");
include("ChartTitleFixture.js");
include("ChartLegendFixture.js");
include("ChartTicksFixture.js");
include("ChartAxisFixture.js");
include("CanvasFixture.js");

ChartTest = {};

ChartTest.setUp = function () {
    this.canvas = new Canvas();
    this.ctx = this.canvas.getContext('getElementById');
    document.addElementWithId("chart", this.canvas);
    this.chart = new Fiz.Chart('chart');
};


ChartTest.test_constructor_badID = function () {
    try {
        var chart = new Fiz.Chart('bad');
    } catch (e) {
        assertEqual("Chart: Cannot find canvas element with ID: bad", e);
        return;
    }

    assertEqual(false, true, "Expected exception not throw");
};

ChartTest.test_removePlot = function () {
    plot = {a: 5};
    this.chart.plots = [plot];
    this.chart.removePlot(plot);
    assertEqual(0, this.chart.plots.length);
};

ChartTest.test_removePlot_invalidPlot = function () {
    try {
        this.chart.removePlot({});
    } catch (e) {
        assertEqual("Chart.removePlot: Plot not found", e);
        return;
    }

    assertEqual(true, false, "Exception not throw");
};


ChartTest.test_addPlot = function () {
    plot = {a: 5};
    plot2 = {b: 6};
    this.chart.addPlot(plot);
    this.chart.addPlot(plot2);
    assertEqual(plot, this.chart.plots[0]);
    assertEqual(plot2, this.chart.plots[1]);
};

ChartTest.test_set = function () {
    this.chart.set("legendTitle", "foo");
    assertEqual("foo", this.chart.legendConfig.title);

    this.chart.set("title", "bar");
    assertEqual("bar", this.chart.config.title);

    this.chart.set("xAxisTitle", "baz");
    assertEqual("baz", this.chart.xAxisConfig.title);
};

ChartTest.test_get = function () {
    this.chart.legendConfig.title = "foo";
    assertEqual("foo", this.chart.get("legendTitle"));

    this.chart.config.title = "bar";
    assertEqual("bar", this.chart.get("title"));
};

ChartTest.test_drawShape_diamond = function () {
    Fiz.Chart.drawShape(this.ctx, 1, 1, "diamond", "green", 5);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(1, 1)\nbeginPath()\n" +
                "fillStyle = green\nrotate(0.7853981633974483)\nmoveTo(-2.5, -2.5)\n" +
                "lineTo(2.5, -2.5)\nlineTo(2.5, 2.5)\nlineTo(-2.5, 2.5)\n" +
                "fill()\nrestore()\n", jsunit.log);

    jsunit.log = "";

    Fiz.Chart.drawShape(this.ctx, 3, 2, "diamond", "black", 4);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(3, 2)\nbeginPath()\n" +
                "fillStyle = black\nrotate(0.7853981633974483)\nmoveTo(-2, -2)\n" +
                "lineTo(2, -2)\nlineTo(2, 2)\nlineTo(-2, 2)\n" +
                "fill()\nrestore()\n", jsunit.log);
};

ChartTest.test_drawShape_square = function () {
    Fiz.Chart.drawShape(this.ctx, 1, 1, "square", "green", 5);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(1, 1)\nbeginPath()\n" +
                "fillStyle = green\nmoveTo(-2.5, -2.5)\nlineTo(2.5, -2.5)\nlineTo(2.5, 2.5)\n" +
                "lineTo(-2.5, 2.5)\nfill()\nrestore()\n", jsunit.log);
    jsunit.log = "";

    Fiz.Chart.drawShape(this.ctx, 0, -1, "square", "yellow", 2);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(0, -1)\nbeginPath()\n" +
                "fillStyle = yellow\nmoveTo(-1, -1)\nlineTo(1, -1)\nlineTo(1, 1)\n" +
                "lineTo(-1, 1)\nfill()\nrestore()\n", jsunit.log);
};

ChartTest.test_drawShape_circle = function () {
    Fiz.Chart.drawShape(this.ctx, 1, 1, "circle", "green", 6);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(1, 1)\nbeginPath()\n" +
                "fillStyle = green\narc(0, 0, 4, 0, 6.283185307179586, true)\nfill()\n" +
                "restore()\n", jsunit.log);
    jsunit.log = "";

    Fiz.Chart.drawShape(this.ctx, 4, 2, "circle", "cyan", 3);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(4, 2)\nbeginPath()\n" +
                "fillStyle = cyan\narc(0, 0, 2, 0, 6.283185307179586, true)\nfill()\n" +
                "restore()\n", jsunit.log);
};

ChartTest.test_drawShape_triangle = function () {
    Fiz.Chart.drawShape(this.ctx, 1, 1, "triangle", "green", 5);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(1, 1)\nbeginPath()\n" +
                "fillStyle = green\nmoveTo(-2.5, 2.2)\nlineTo(2.5, 2.2)\nlineTo(0, -2.2)\n" +
                "lineTo(-2.5, 2.2)\nfill()\nrestore()\n", jsunit.log);

    jsunit.log = "";

    Fiz.Chart.drawShape(this.ctx, 2, 3, "triangle", "blue", 3);
    assertEqual("save()\nglobalAlpha = 1\ntranslate(2, 3)\nbeginPath()\n" +
                "fillStyle = blue\nmoveTo(-1.5, 1.3)\nlineTo(1.5, 1.3)\nlineTo(0, -1.3)\n" +
                "lineTo(-1.5, 1.3)\nfill()\nrestore()\n", jsunit.log);
};

ChartTest.test_drawShape_invalidShape = function () {
    try {
        Fiz.Chart.drawShape(this.ctx, 1, 1, "foo", "green", 5);
    } catch (e) {
        assertEqual("Chart.drawShape: Invalid shape: foo", e);
        return;
    }

    assertEqual(true, false, "Exception not thrown");
};

ChartTest.test_drawLineTo_noBorder = function () {
    Fiz.Chart.drawLineTo(this.ctx, [], 1, 2);
    assertEqual("lineWidth = 1\nstrokeStyle = black\nlineTo(1, 2)\n",
                jsunit.log);
};

ChartTest.test_drawLineTo_withBorder = function () {
    Fiz.Chart.drawLineTo(this.ctx, [5, "green"], 0, 1);
    assertEqual("lineWidth = 5\nstrokeStyle = green\nlineTo(0, 1)\n",
                jsunit.log);
};

ChartTest.test_drawRect_basic = function () {
    Fiz.Chart.drawLineTo = chartLogFunction("lineTo");
    Fiz.Chart.drawRect(this.ctx, 1, 2);
    assertEqual("save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(0, 0)\n" +
                "lineTo(ctx, 0, 1, 0)\nlineTo(ctx, 0, 1, 2)\n" +
                "lineTo(ctx, 0, 0, 2)\nlineTo(ctx, 0, 0, 0)\nstroke()\n" +
                "restore()\n", jsunit.log);
};

ChartTest.test_findConfigObject = function () {
    var result = this.chart.findConfigObject("legendTitle");
    assertEqual(this.chart.legendConfig, result[0], "legendTitle 0");
    assertEqual("title", result[1], "legendTitle 1");

    var result = this.chart.findConfigObject("title");
    assertEqual(this.chart.config, result[0], "main 0");
    assertEqual("title", result[1], "main 1");
};

ChartTest.test_setColors = function () {
    var plot1 = new Fiz.Chart.Plot([1], [2]);
    var plot2 = new Fiz.Chart.Plot([3]);
    plot2.getSeries(0).set("color", "teal");
    this.chart.addPlot(plot1);
    this.chart.addPlot(plot2);
    this.chart.setColors();
    assertEqual("blue", plot1.series[0].get("color"));
    assertEqual("red", plot1.series[1].get("color"));
    assertEqual("teal", plot2.series[0].get("color"));
};

ChartTest.test_drawBorder = function () {
    this.chart.config.border = null;
    this.chart.sections = {};
    this.chart.sections.left = {};
    this.chart.sections.top = {};
    this.chart.canvasWidth = 500;
    this.chart.canvasHeight = 500;
    this.chart.config.borderWidth = 50;
    this.chart.config.borderColor = "blue";
    this.chart.sections.left.size = this.chart.sections.top.size = 10;

    var tmp = Fiz.Chart.drawRect;
    Fiz.Chart.drawRect = chartLogFunction("drawRect");
    this.chart.drawBorder();

    assertEqual("drawRect(ctx, 500, 500, 50,blue,inside, white)\n" +
                "translate(50, 50)\n", jsunit.log);

    Fiz.Chart.drawRect = tmp;
}

ChartTest.test_drawBorder_chartSizes = function () {
    var tmp = Fiz.Chart.drawRect;
    Fiz.Chart.drawRect = function(){};
    this.chart.config.borderWidth = 5;
    this.chart.canvasWidth = 100;
    this.chart.canvasHeight = 50;

    this.chart.drawBorder();

    Fiz.Chart.drawRect = tmp;

    assertEqual(90, this.chart.canvasWidth, "width");
    assertEqual(40, this.chart.canvasHeight, "height");

    assertEqual("translate(5, 5)\n", jsunit.log, "jsunit.log");
}

ChartTest.test_drawPlotBorder = function() {
    this.chart.config.plotBorder = null;
    this.chart.sections = {};
    this.chart.sections.left = {};
    this.chart.sections.top = {};
    this.chart.sections.left.size = this.chart.sections.top.size = 10;
    this.chart.plotArea.width = 20;
    this.chart.plotArea.height = 30;
    this.chart.config.plotBorderWidth = 5;
    this.chart.config.plotBorderColor = "blue";

    Fiz.Chart.drawRect = chartLogFunction("drawRect");

    this.chart.drawPlotBorder();

    assertEqual("save()\ntranslate(10, 10)\n" +
                "drawRect(ctx, 20, 30, 5,blue,outside, undefined)\n" +
                "restore()\n", jsunit.log);
};

ChartTest.test_getVisibleSeries = function () {
    this.chart.getVisiblePlots = function () {
        return [{series: [1, 2]}, {series: [4]}];
    };

    var series = this.chart.getVisibleSeries();
    assertEqual(2, series[1]);
    assertEqual(4, series[2]);
};

ChartTest.test_getVisiblePlots = function () {
    this.chart.plots.push({config: {display: false}});
    this.chart.plots.push({config: {display: true, a:1}});

    assertEqual(1, this.chart.getVisiblePlots()[0].config.a);
};

ChartTest.test_fixAxisSizes = function () {
    this.chart.plotArea.width = 10;
    this.chart.plotArea.height = 20;

    this.chart.axes = [{}, {}, {}, {}];

    this.chart.fixAxisSizes();

    assertEqual(10, this.chart.axes[0].size, "axis 0 size");
    assertEqual(20, this.chart.axes[0].pSize, "axis 0 pSize");
    assertEqual(10, this.chart.axes[1].pSize, "axis 1 pSize");
    assertEqual(20, this.chart.axes[1].size, "axis 1 size");
    assertEqual(10, this.chart.axes[2].size, "axis 2 size");
    assertEqual(20, this.chart.axes[2].pSize, "axis2 pSize");
    assertEqual(10, this.chart.axes[3].pSize, "axis 3 pSize");
    assertEqual(20, this.chart.axes[3].size, "axis 3 size");
};

ChartTest.test_drawPlots = function () {
    this.chart.axes = [];
    var axis1 = new Fiz.Chart.Axis();
    axis1.xAxis = false;
    var plot1 = {type: "a"};
    var plot2 = {type: "b"};
    plot1.drawPlots = function() {jsunit.log += "drawPlots1()\n" };
    plot2.drawPlots = function() {jsunit.log += "drawPlots2()\n" };
    var axis2 = new Fiz.Chart.Axis();
    axis2.plots = [plot1, plot2]
    axis2.xAxis = true;

    this.chart.axes = [axis1, axis2];
    this.chart.sections.left.size = 10;
    this.chart.sections.top.size = 20;
    this.chart.rotateCanvasForPlots = chartLogFunction("rotateCanvas");

    this.chart.drawPlots();

    assertEqual("save()\ntranslate(10, 20)\n" +
                "rotateCanvas(1)\ndrawPlots1()\n" +
                "drawPlots2()\nrestore()\n", jsunit.log);
};

ChartTest.test_sortPlotsByType = function () {
    var plot1 = {type: "a"};
    var plot2 = {type: "b"};
    var plot3 = {type: "a"};

    var list = this.chart.sortPlotsByType([plot1, plot2, plot3]);

    assertEqual(plot1, list[0][0], "type a first");
    assertEqual(plot2, list[1][0], "type b first");
    assertEqual(plot3, list[0][1], "type a second");
};

ChartTest.test_rotateCanvasForPlots_top = function() {
    this.chart.plotArea.height = 1;
    this.chart.rotateCanvasForPlots(2);
    assertEqual("transform(1, 0, 0, -1, 0, 1)\n" +
                "rotate(3.141592653589793)\n" +
                "transform(-1, 0, 0, 1, 0, -1)\n", jsunit.log);
};

ChartTest.test_rotateCanvasForPlots_right = function() {
    this.chart.plotArea.height = 2;
    this.chart.plotArea.width = 4;
    this.chart.rotateCanvasForPlots(3);
    assertEqual("transform(1, 0, 0, -1, 0, 2)\n" +
                "translate(0, 2)\n" +
                "rotate(-1.5707963267948966)\n" +
                "transform(1, 0, 0, -1, 0, 0)\n" +
                "transform(-1, 0, 0, 1, 2, -4)\n", jsunit.log);
};

ChartTest.test_rotateCanvasForPlots_bottom = function() {
    this.chart.plotArea.height = 3;
    this.chart.rotateCanvasForPlots(0);
    assertEqual("transform(1, 0, 0, -1, 0, 3)\n", jsunit.log);
};

ChartTest.test_rotateCanvasForPlots_left = function() {
    this.chart.plotArea.height = 4;
    this.chart.plotArea.width = 5;
    this.chart.rotateCanvasForPlots(1);
    assertEqual("transform(1, 0, 0, -1, 0, 4)\n" +
                "translate(5, 0)\n" +
                "rotate(1.5707963267948966)\n" +
                "transform(1, 0, 0, -1, 0, 5)\n", jsunit.log);
};
