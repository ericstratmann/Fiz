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
include("static/fiz/ChartPlot.js");
include("static/fiz/ChartSeries.js");

ChartPlotTest = {};

ChartPlotTest.setUp = function () {
    this.plot = new Fiz.Chart.Plot();
    this.data = [[1, 5], [0, 4], [2, 2]];

    this.series = new Fiz.Chart.Series(this.data);
    this.plot.series = [];
    this.plot.series[0] = this.series;

};


ChartPlotTest.test_hasDiscreteXAxis = function () {
    this.plot.discrete = true;
    assertEqual(true, this.plot.hasDiscreteXAxis(), "true");

    this.plot.discrete = false;
    assertEqual(false, this.plot.hasDiscreteXAxis(), "false");
};

ChartPlotTest.test_getBoundingBox = function () {
    this.plot.discrete = false;
    var series = {data: [[3, 4], [2, 6], [-1, 5]]};
    this.plot.series[1] = series;

    var boundary = this.plot.getBoundingBox();
    assertEqual(7, boundary.minY, "min y");
    assertEqual(10, boundary.maxY, "max y");
    assertEqual(0, boundary.minX, "min x");
    assertEqual(2, boundary.maxX, "max x");
};

ChartPlotTest.test_findVal = function () {
    assertEqual(4, this.plot.findVal(this.series, 0), "found");
    assertEqual(undefined, this.plot.findVal(this.series, 10), "not found");
};

ChartPlotTest.test_set = function () {
    this.plot.config = {};
    this.plot.set("foo", "bar");
    assertEqual("bar", this.plot.config.foo, "plot");
    assertEqual("bar", this.plot.series[0].config.foo, "series");
};

ChartPlotTest.test_get = function () {
    this.plot.config = {};
    this.plot.config.foo = "bar";
    assertEqual("bar", this.plot.get("foo"));
};

ChartPlotTest.test_getSeries = function () {
    assertEqual(this.series, this.plot.getSeries(0));
};

ChartPlotTest.test_drawPlots = function () {
    var plot1 = {};
    plot1.draw = chartLogFunction("plot1draw");

    var plot2 = {};
    plot2.draw = chartLogFunction("plot2draw");

    var canvas = new Canvas();
    var ctx = canvas.getContext('getElementById');

    this.plot.drawPlots([plot1, plot2], ctx);

    assertEqual("plot1draw(ctx)\nplot2draw(ctx)\n", jsunit.log);
};

ChartPlotTest.test_addSeries = function () {
    var series = new Fiz.Chart.Series();
    series.set("foo", "bar");

    this.plot.config = {};
    this.plot.config.foo = "baz";
    this.plot.config.x = "y";

    this.plot.addSeries(series);

    assertEqual(series, this.plot.series[1], "series object");
    assertEqual("bar", series.config.foo, "predefined property");
    assertEqual("y", series.config.x, "new property");
};
