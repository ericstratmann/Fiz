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

// ChartAxisTest.js --
//
// Jsunit tests for ChartAxis.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartAxis.js");
include("static/fiz/ChartSeries.js");
include("ChartPlotFixture.js");
include("CanvasFixture.js");

ChartAxisTest = {};

ChartAxisTest.isAxis = function () {
    this.axis.type = "x";
    assertEqual(true, this.axis.isXAxis(), "is x");
    
    this.axis.type = "y";
    assertEqual(false, this.axis.isXAxis(), "is not x");
};

ChartAxisTest.setUp = function () {
    this.axis = new Fiz.Chart.Axis({});
};

ChartAxisTest.test_addPlot = function () {
    var plot1 = new Fiz.Chart.Plot([]);
    this.axis.addPlot(plot1);
    assertEqual(plot1, this.axis.plots[0], "plot1");
    
    var plot2 = new Fiz.Chart.Plot([]);
    this.axis.addPlot(plot2);
    assertEqual(plot2, this.axis.plots[1], "plot2");
};

ChartAxisTest.test_setDiscreteness_xAxis = function () {
    this.axis.type = "x";

    var plot = new Fiz.Chart.Plot([]);
    plot.discrete = true;

    this.axis.setDiscreteness(plot);
    assertEqual(true, this.axis.discrete, "true");
    

    this.axis.discrete = undefined;
    plot.discrete = false;
    this.axis.setDiscreteness(plot);
    assertEqual(false, this.axis.discrete, "false");
};

ChartAxisTest.test_setDiscreteness_yAxis = function () {
    this.axis.type = "y";

    var plot = new Fiz.Chart.Plot([]);
    plot.discrete = true;
    this.axis.setDiscreteness(plot)
    assertEqual(false, this.axis.discrete);
};

ChartAxisTest.test_setDiscreteness_discreteMismatch = function () {
    this.axis.type = "x";

    var plot1 = new Fiz.Chart.Plot([]);
    plot1.discrete = true;
    this.axis.addPlot(plot1);

    try {
        var plot2 = new Fiz.Chart.Plot([]);
        plot2.discrete = false;
        this.axis.addPlot(plot2);
    } catch (e) {
        assertEqual("Axis.addPlot: Plots disagree on discreteness", e, "exception");
        return;
    }

    assertEqual(true, false, "Exception not throw");
};

ChartAxisTest.test_setLabels_discrete = function () {
    this.axis.getDiscreteAxisValues = chartLogFunction("discrete");

    this.axis.type = "x";
    this.axis.discrete = true;
    
    plot = {a: "foo"};
    this.axis.setLabels(plot);
    assertEqual("discrete(a: foo)\n", jsunit.log);
    
};

ChartAxisTest.test_setLabels_continuous = function () {

    this.axis.getContinuousAxisValues = chartLogFunction("continuous");
    plot = {b: "bar"};

    this.axis.type = "x";
    this.axis.discrete = false;
    this.axis.setLabels(plot);
    assertEqual("continuous(b: bar)\n", jsunit.log);
    
    jsunit.log = "";
    this.axis.type = "y";
    this.axis.discrete = true;
    this.axis.setLabels(plot);
    assertEqual("continuous(b: bar)\n", jsunit.log);
};

ChartAxisTest.test_getDiscreteAxisValues = function () {
    var plot = {series: [{data: [[1, 2], [2, 3]]}]};

    var labels = this.axis.getDiscreteAxisValues(plot);
    assertEqual("1", labels[0], "0");
    assertEqual("2", labels[1], "1");
};

// UPTOHERE

ChartAxisTest.test_getContinuousAxisValues_number = function () {
    this.axis.min = 0;
    this.axis.max = 10;
    this.axis.config.ticks = 5;

    var ticks = this.axis.getContinuousAxisValues();
    assertEqual(3, ticks.length, "length");
    assertEqual(0, ticks[0], "0");
    assertEqual(5, ticks[1], "1");
    assertEqual(10, ticks[2], "2");
};

ChartAxisTest.test_getContinuousAxisValues_array = function () {
    this.axis.config.ticks = [1,2,5];
    assertEqual(this.axis.config.ticks, this.axis.getContinuousAxisValues());
};

/* rmin rmax
ChartAxisTest.test_getContinuousAxisValues_log = function () {
    this.axis.config.scale = "log";
    this.axis.config.ticks = null;
    this.axis.min = 1;
    this.axis.max = 100;
    this.axis.config.logValue = 10;

    var ticks = this.axis.getContinuousAxisValues();
    assertEqual(3, ticks.length, "length");
    assertEqual(1, ticks[0], "0");
    assertEqual(10, ticks[1], "1");
    assertEqual(100, ticks[2], "2");
};
*/
//todo linear 

ChartAxisTest.test_generateTickMarks_test1 = function () {
    var ticks = this.axis.generateTickMarks(0, 4);
    assertEqual(5, ticks.length, "length");
    assertEqual(0, ticks[0], "0");
    assertEqual(2, ticks[2], "2");
    assertEqual(4, ticks[4], "4");
    var ticks = this.axis.generateTickMarks(0, 4);
};
ChartAxisTest.test_generateTickMarks_test2 = function () {
    var ticks = this.axis.generateTickMarks(0, 7);
    assertEqual(4, ticks.length, "length");
    assertEqual(0, ticks[0], "0");
    assertEqual(4, ticks[2], "2");
    assertEqual(6, ticks[3], "3");
};

/*
ChartAxisTest.test_generateTickMarks_test3 = function () {
    var ticks = this.axis.generateTickMarks(0, 16);
    assertEqual(4, ticks.length, "length");
    assertEqual(0, ticks[0], "0");
    assertEqual(10, ticks[2], "2");
    assertEqual(15, ticks[3], "3");
};
*/
ChartAxisTest.test_generateTickMarks_test4 = function () {
    var ticks = this.axis.generateTickMarks(50, 120);
    assertEqual(4, ticks.length, "length");
    assertEqual(50, ticks[0], "0");
    assertEqual(70, ticks[1], "1");
    assertEqual(110, ticks[3], "3");
};

/*
ChartAxisTest.test_generateTickMarks_test5 = function () {
    var ticks = this.axis.generateTickMarks(0.3, 0.6);
    assertEqual(7, ticks.length, "length");
    assertEqual(0.3, ticks[0], "0");
    assertEqual(0.35, ticks[1], "1");
    assertEqual(0.55, ticks[5], "5");
};
*/
ChartAxisTest.test_setMinAndMax_linear = function () {
    var plot1 = new Fiz.Chart.Plot();
    var plot2 = new Fiz.Chart.Plot();
    plot1.setMinAndMax(1, 6);
    plot2.setMinAndMax(0, 4);
    var axis = new Fiz.Chart.Axis({scale: "linear"});
    axis.setMinAndMax(plot1);
    axis.setMinAndMax(plot2);
    assertEqual(0, axis.min, "min");
    assertEqual(6, axis.max, "max");
};

ChartAxisTest.test_setMinAndMax_bothAxes = function () {
    var plot1 = new Fiz.Chart.Plot();
    plot1.setMinAndMax(-3, -1, 2, 5);
    var axis1 = new Fiz.Chart.Axis({scale: "linear"}, undefined, "x");
    axis1.setMinAndMax(plot1);
    assertEqual(0, axis1.min, "x axis min");
    assertEqual(5, axis1.max, "x axis max");

    var axis2 = new Fiz.Chart.Axis({scale: "linear"}, undefined, "y");
    axis2.setMinAndMax(plot1);
    assertEqual(-3, axis2.min, "y axis min");
    assertEqual(0, axis2.max, "y axis min");
    
};

/*
ChartAxisTest.test_setMinAndMax_log = function () {
    var plot1 = new Fiz.Chart.Plot();
    plot1.setMinAndMax(33, 55);
    var axis = new Fiz.Chart.Axis({scale: "log", logValue: 5});
    axis.fixValues = logFunction("fixValues");
    axis.setMinAndMax(plot1);
    assertEqual(25, axis.min, "min");
    assertEqual(55, axis.max, "max");
    assertEqual("fixValues()\n", jsunit.log);
};
*/

ChartAxisTest.test_fixValues = function () {
    this.axis.min = 10;
    this.axis.max = 100;
    this.axis.config.logValue = 10;
    
    this.axis.config.scale = "linear";
    this.axis.fixValues();
    assertEqual(10, this.axis.min, "linear min");
    assertEqual(100, this.axis.max, "linear max");

    /*
    this.axis.config.scale = "log";
    this.axis.fixValues();
    assertEqual(1, this.axis.min, "log min");
    assertEqual(2, this.axis.max, "log max");
*/
};

var lTCC = function (val) {
    jsunit.log += "logicalToChartCoords(" + val + ")\n";
    return  val * 2;
};

/*
ChartAxisTest.test_positionOf_oneVal = function () {
    this.axis.logicalToChartCoords = lTCC;
    this.axis.config.scale = "linear";
    assertEqual(10, this.axis.positionOf(5), "result");
    assertEqual("logicalToChartCoords(5)\n", jsunit.log, "jsunit.log");
};

ChartAxisTest.test_positionOf_array = function () {
    this.axis.logicalToChartCoords = lTCC;
    this.axis.config.scale = "linear";
    
    var result = this.axis.positionOf([5, 3]);
    assertEqual(2, result.length, "length");
    assertEqual(10, result[0], "0");
    assertEqual(16, result[1], "1");
    assertEqual("logicalToChartCoords(5)\nlogicalToChartCoords(8)\n",
                jsunit.log, "jsunit.log");
};

ChartAxisTest.test_positionOf_log = function () {
    this.axis.logicalToChartCoords = lTCC;
    this.axis.config.scale = "log";
    this.axis.config.logValue = 10;
    assertEqual(4, this.axis.positionOf(100), "result");
    assertEqual("logicalToChartCoords(2)\n", jsunit.log, "jsunit.log");
};

ChartAxisTest.test_logcalToChartCoords = function () {
    this.axis.min = -5;
    this.axis.max = 10;
    this.axis.size = 15;
    ssertEqual(10, this.axis.logicalToChartCoords(5));
};
*/

ChartAxisTest.test_zero = function () {
    this.axis.min = -5;
    this.axis.max = 15;
    this.axis.size = 20;
    this.axis.config.scale = "linear";

    assertEqual(5, this.axis.zero(), "linear");
    
    this.axis.min = 1;
    this.axis.max = 10;
    this.axis.size = 20;
    this.axis.config.scale = "log";
    assertEqual(0, this.axis.zero(), "log");
};
