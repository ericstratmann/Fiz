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


ChartAxisTest.setUp = function () {
    this.axis = new Fiz.Chart.Axis({});
};

ChartAxisTest.test_isXAxis = function () {
    this.axis.type = "x";
    assertEqual(true, this.axis.isXAxis(), "is x");

    this.axis.type = "y";
    assertEqual(false, this.axis.isXAxis(), "is not x");
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


ChartAxisTest.test_getContinuousAxisValues_noTicks = function () {
    this.axis.config.ticks = false;

    assertEqual(0, this.axis.getContinuousAxisValues().length);
};

ChartAxisTest.test_getContinuousAxisValues_logTicks = function () {
    this.axis.config.ticks = true;
    this.axis.config.scale = "log";
    this.axis.generateLogTickMarks = chartLogFunction("logTicks");
    this.axis.getContinuousAxisValues();
    assertEqual("logTicks()\n", jsunit.log);
};

ChartAxisTest.test_getContinuousAxisValues_linearTicks = function () {
    this.axis.config.ticks = "true";
    this.axis.config.scale = "linear";
    this.axis.generateTickMarks = chartLogFunction("linearTicks");
    this.axis.getContinuousAxisValues();
    assertEqual("linearTicks()\n", jsunit.log);
};

ChartAxisTest.test_getContinuousAxisValues_badScale = function () {
    this.axis.config.ticks = "true";
    this.axis.config.scale = "badzxc";
    try {
        this.axis.getContinuousAxisValues();
        assertEqual(true, false, "Exception not thrown");
    } catch (e) {
        assertEqual("Axis.getContinuousAxisValues: bad config.scale value", e);
    }
};

ChartAxisTest.test_getNSigFigs = function () {
    assertEqual(15, this.axis.getNSigFigs(150, 2), "more digits than sigfigs");
    assertEqual(83, this.axis.getNSigFigs(8.3, 2), "less digits than sigfigs");
    assertEqual(515, this.axis.getNSigFigs(515, 3), "equal digits as sigfigs");
};

ChartAxisTest.test_generateLogTickMarks = function () {

};

ChartAxisTest.test_generateLogTickMarks = function () {
    this.axis.config.scale = "log";
    this.axis.min = 1;
    this.axis.max = 105;
    this.axis.config.logBase = 10;

    var ticks = this.axis.generateLogTickMarks();
    assertEqual(3, ticks.length, "length");
    assertEqual(1, ticks[0], "0");
    assertEqual(10, ticks[1], "1");
    assertEqual(100, ticks[2], "2");
};

ChartAxisTest.test_generateTickMarks_test1 = function () {
    this.axis.min = 0;
    this.axis.max = 4;
    var ticks = this.axis.generateTickMarks();
    assertEqual("0,1,2,3,4", ticks.toString());
};

ChartAxisTest.test_generateTickMarks_test2 = function () {
    this.axis.min = 0;
    this.axis.max = 7;
    var ticks = this.axis.generateTickMarks();
    assertEqual("0,2,4,6", ticks.toString());
    assertEqual(4, ticks.length, "length");
};

ChartAxisTest.test_generateTickMarks_test3 = function () {
    this.axis.min = 0;
    this.axis.max = 16;
    var ticks = this.axis.generateTickMarks();
    assertEqual("0,5,10,15", ticks.toString());
};

ChartAxisTest.test_generateTickMarks_test4 = function () {
    this.axis.min = 50;
    this.axis.max = 120;
    var ticks = this.axis.generateTickMarks();
    assertEqual("50,70,90,110", ticks.toString());
};

ChartAxisTest.test_generateTickMarks_test5 = function () {
    this.axis.min = 0.3;
    this.axis.max = 0.6;
    var ticks = this.axis.generateTickMarks();
    assertEqual("0.3,0.4,0.5,0.6", ticks.toString());
};

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
    var axis1 = new Fiz.Chart.Axis({scale: "linear"}, null, undefined, "x");
    axis1.setMinAndMax(plot1);
    assertEqual(0, axis1.min, "x axis min");
    assertEqual(5, axis1.max, "x axis max");

    var axis2 = new Fiz.Chart.Axis({scale: "linear"}, null, undefined, "y");
    axis2.setMinAndMax(plot1);
    assertEqual(-3, axis2.min, "y axis min");
    assertEqual(0, axis2.max, "y axis max");

};

ChartAxisTest.test_setMinAndMax_log = function () {
    var plot1 = new Fiz.Chart.Plot();
    plot1.setMinAndMax(33, 55);
    var axis = new Fiz.Chart.Axis({scale: "log", logBase: 5});
    axis.setMinAndMax(plot1);
    assertEqual(25, axis.min, "min");
    assertEqual(55, axis.max, "max");
};

var lTCC = function (val) {
    jsunit.log += "logicalToChartCoords(" + val + ")\n";
    return  val * 2;
};

ChartAxisTest.test_logcalToChartCoords = function () {
    this.axis.min = -5;
    this.axis.max = 10;
    this.axis.size = 15;
    this.axis.config.scale = "linear";

    this.axis.getScaledNumber = function(n) {
        jsunit.log += "getScaled(" + n + ")\n";
        return n * 3;
    };

    assertEqual(10, this.axis.logicalToChartCoords(5), "value");
    assertEqual("getScaled(-5)\ngetScaled(10)\ngetScaled(5)\n", jsunit.log,
                "jsunit.log");
};

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
