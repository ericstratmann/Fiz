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

/* drawlinto/lineto
ChartTest.test_drawRect_side = function () {
    var border = new Fiz.Chart.Border(this.ctx, 10, undefined, "inside");
    Fiz.Chart.drawLineTo = chartLogFunction("lineTo");
    Fiz.Chart.drawRect(this.ctx, 3, 4);
    assertEqual("save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(0, 0)\n" + 
                "drawLineTo(ctx, 0, 3)\ndrawLineTo(ctx, 3, 4)\ndrawLineTo(ctx, 0, 4)\n" +
                "drawLineTo(ctx, 0, 0)\nstroke()\nrestore()\n", jsunit.log);
};


ChartTest.test_drawRect_withSelectBorder = function () {
    var border = new Fiz.Chart.Border(this.ctx, 10, "blue", "middle",
                                       [true, false, true, false]);
    Fiz.Chart.drawLineTo = chartLogFunction("lineTo");

    assertEqual("save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(-4, 2)\n" + 
                "drawLineTo(4, 2)\nmoveTo(4, 6)\ndrawLineTo(1, 6)\n" +
                "moveTo(1, 2)\nstroke()\nrestore()\n", jsunit.log);
};

ChartTest.test_drawRect_withBackGround = function () {
    var border = new Fiz.Chart.Border(this.ctx, 10);
    Fiz.Chart.drawLineTo = chartLogFunction("lineTo");
    Fiz.Chart.drawRect(this.ctx, 1, 2, 3, 4, "black");
    assertEqual("save()\nfillStyle = black\nfillRect(1, 2, 3, 4)\nrestore()\n" +
                "save()\nglobalAlpha = 1\nbeginPath()\nmoveTo(-4, 2)\n" +
                "drawLineTo(4, 2)\ndrawLineTo(4, 6)\ndrawLineTo(1, 6)\n" +
                "drawLineTo(1, 2)\nstroke()\nrestore()\n", jsunit.log);
};
*/
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

/*
ChartTest.test_drawBorders_sideUndefined = function () {
    this.chart.config.border = [2, "green"];
    this.chart.config.background = "blue";
    this.chart.config.plotBorder = [4, "white"];
    this.chart.config.plotBackground = "green";
    this.chart.sections = {};
    this.chart.sections.left = {};
    this.chart.sections.top = {};
    this.chart.sections.left.size = this.chart.sections.top.size = 10;
    this.chart.plotArea = {};
    this.chart.plotArea.width = this.chart.plotArea.height = 20;
    
    this.chart.drawBorders();
    
    assertEqual("drawRect(ctx, 500, 500, 2,green,inside, blue)\nsave()\n" + 
                "translate(10, 10)\ndrawRect(ctx, 20, 20, 4,white,outside" +
                ", green)\nrestore()\n", jsunit.log);
};

ChartTest.test_drawBorders = function () {
    this.chart.config.border = [1, "red", "outside"];
    this.chart.config.background = "blue";
    this.chart.config.plotBorder = [2, "blue", "inside"];
    this.chart.config.plotBackground = "green";
    this.chart.sections = {};
    this.chart.sections.left = {};
    this.chart.sections.top = {};
    this.chart.sections.left.size = this.chart.sections.top.size = 10;
    this.chart.plotArea = {};
    this.chart.plotArea.width = this.chart.plotArea.height = 20;
    
    this.chart.drawBorders();
    
    assertEqual("drawRect(ctx, 500, 500, 1,red,outside, blue)\nsave()\n" + 
                "translate(10, 10)\ndrawRect(ctx, 20, 20, 2,blue,inside" +
                ", green)\nrestore()\n", jsunit.log);
};
*/

/*
ChartTest.test_registerLayout = function () {
    this.chart.calculateSize = chartLogFunction();
    this.chart.axes=[{config:{}},{config:{}},{config:{}},{config:{}}];
    
    this.chart.registerLayout();
    assertEqual(true, this.chart.sections.top[0] instanceof Fiz.Chart.Title,
                "top0");
    assertEqual(true, this.chart.sections.top[1] instanceof Fiz.Chart.Title,
                "top1");
    assertEqual(true, this.chart.sections.top[2] instanceof Fiz.Chart.Ticks,
                "top2");
    assertEqual(true, this.chart.sections.left[0] instanceof Fiz.Chart.Title,
                "left0");
    assertEqual(true, this.chart.sections.left[1] instanceof Fiz.Chart.Ticks,
                "left1");
    assertEqual(true, this.chart.sections.bottom[0] instanceof Fiz.Chart.Title,
                "bottom0");
    assertEqual(true, this.chart.sections.bottom[1] instanceof Fiz.Chart.Ticks,
                "bottom1");
    assertEqual(true, this.chart.sections.right[0] instanceof Fiz.Chart.Legend,
                "right0");
    assertEqual(true, this.chart.sections.right[1] instanceof Fiz.Chart.Title,
                "right1");
    assertEqual(true, this.chart.sections.right[2] instanceof Fiz.Chart.Ticks,
                "right2");

    assertEqual("calculateChartSize()\n", jsunit.log);
};
*/
/*
ChartTest.test_calculateChartSize = function () {
    this.chart.sections = {};
    this.chart.sections.top = [];
    this.chart.sections.right = [];
    this.chart.sections.left = [];
    this.chart.sections.bottom = [];
    this.chart.config.plotBorderWidth = 5;
    this.chart.canvasWidth = 500;
    this.chart.canvasHeight = 500;
    this.chart.sections.top.push(new Fiz.Chart.Title());
    this.chart.sections.top.push(new Fiz.Chart.Legend());
    this.chart.sections.right.push(new Fiz.Chart.Title());
    
    this.chart.calculateChartSize();

    assertEqual(445, this.chart.plotArea.width, "width");
    assertEqual(430, this.chart.plotArea.height, "height");
};

ChartTest.test_calculateChartSize_badPrototype = function () {
    Object.prototype.foo = ["foo"];
    this.chart.sections = {};
    this.chart.sections.top = [];
    this.chart.sections.right = [];
    this.chart.sections.left = [];
    this.chart.sections.bottom = [];
    this.chart.sections.top.push(new Fiz.Chart.Title());
    this.chart.sections.top.push(new Fiz.Chart.Legend());
    this.chart.sections.right.push(new Fiz.Chart.Title());

    try {
        this.chart.calculateChartSize();
    } catch (e) {
        delete Object.prototype.foo;
        assertEqual(false, true, "Exception thrown");
        return;
    }
    delete Object.prototype.foo;
};
/*
ChartTest.test_renderSections = function () {
};


ChartTest.test_resolveAxes = function () {
};

ChartTest.test_createAxis = function () {
};
*/

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

