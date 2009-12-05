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

// ChartBarTest.js --
//
// Jsunit tests for ChartBar.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartPlot.js");
include("static/fiz/ChartSeries.js");
include("static/fiz/ChartBar.js");
include("ChartAxisFixture.js");

ChartBarTest = {};

ChartBarTest.setUp = function () {
    this.canvas = new Canvas();
    this.ctx = this.canvas.getContext('2d');
};

ChartBarTest.test_draw = function () {
    Fiz.Chart.drawRect = chartLogFunction("drawRect");
    xAxis = new Fiz.Chart.Axis(["a", "b"]);
    yAxis = new Fiz.Chart.Axis();

    var bar = new Fiz.Chart.Bar([["a", 1], ["b", 2]], [["a", 3], ["b", 5]]);
    bar.series[0].config.color = "blue";
    bar.series[1].config.color = "green";
    bar.xAxis = xAxis;
    bar.yAxis = yAxis;

    properties = {
        barWidth: 5,
        i: 1,
        allWidth: 10,
        barSpacer: 3
    };

    bar.draw(this.ctx, properties);
    assertEqual("save()\ntranslate(3, 0)\n" +
                "globalAlpha = 0.5\nsave()\ntranslate(5, -30)\n" +
                "drawRect(ctx, 5, 2, 1,black, blue, false,true,true,true)\n" +
                "restore()\nsave()\ntranslate(5, -28)\n" +
                "drawRect(ctx, 5, 6, 1,black, green, false,true,true,true)\n" +
                "restore()\nsave()\ntranslate(15, -30)\n" +
                "drawRect(ctx, 5, 4, 1,black, blue, false,true,true,true)\n" +
                "restore()\nsave()\ntranslate(15, -26)\n" +
                "drawRect(ctx, 5, 10, 1,black, green, false,true,true,true)\n" +
                "restore()\nrestore()\n", jsunit.log);

    jsunit.log = "";

    properties = {
        barWidth: 3,
        i: 0,
        allWidth: 11,
        barSpacer: 2
    };

    bar.draw(this.ctx, properties);
    assertEqual("save()\ntranslate(2, 0)\n" +
                "globalAlpha = 0.5\nsave()\ntranslate(0, -30)\n" +
                "drawRect(ctx, 3, 2, 1,black, blue, false,true,true,true)\n" +
                "restore()\nsave()\ntranslate(0, -28)\n" +
                "drawRect(ctx, 3, 6, 1,black, green, false,true,true,true)\n" +
                "restore()\nsave()\ntranslate(11, -30)\n" +
                "drawRect(ctx, 3, 4, 1,black, blue, false,true,true,true)\n" +
                "restore()\nsave()\ntranslate(11, -26)\n" +
                "drawRect(ctx, 3, 10, 1,black, green, false,true,true,true)\n" +
                "restore()\nrestore()\n", jsunit.log);

};

ChartBarTest.test_drawPlots = function() {
    var plots = [{}, {}];
    plots[0].draw = chartLogFunction("draw0");
    plots[0].xAxis = new Fiz.Chart.Axis(["a", "b"]);
    plots[1].draw = chartLogFunction("draw1");
    plots[1].xAxis = new Fiz.Chart.Axis(["a", "b"]);

    var bar = new Fiz.Chart.Bar();
    bar.drawPlots(plots, this.ctx, {barSpacer: 3});

    assertEqual("save()\n" +
                "draw0(ctx, allWidth: 8.5, barSpacer: 3, barWidth: 2.75, i: 0)\n" +
                "restore()\nsave()\n" +
                "draw1(ctx, allWidth: 8.5, barSpacer: 3, barWidth: 2.75, i: 1)\n" +
                "restore()\n", jsunit.log);
};
