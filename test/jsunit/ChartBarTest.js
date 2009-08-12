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

/*
ChartBarTest.test_draw = function () {
    xAxis = new Fiz.Chart.Axis(["a", "b"]);
    yAxis = new Fiz.Chart.Axis();

    var bar = new Fiz.Chart.Bar([["a", 1], ["b", 2]], [["a", 3], ["b", 5]]);
    
    properties = {
        barWidth: 5,
        i: 1,
        allWidth: 10,
        barSpacer: 3
    };

    bar.draw(xAxis, yAxis, this.ctx, properties);
    assertEqual("save()\ntranslate(3, 0)\nsave()\ndraw(5, 18, 5, -28, undefined)\n" + 
                "restore()\nsave()\ndraw(5, 14, 5, 4, undefined)\nrestore()\n" +
                "save(15, 16, 5, -26, undefined)\nrestore()\nsave()\n" + 
                "draw(15, 10, 5, 6, undefined)\nrestore()\nrestore()\n", jsunit.log);
};
*/

/*
ChartBarTest.test_drawPlots = function() {
    var plots = [{}, {}];
    plots[0].draw = chartLogFunction();
    plots[0].xAxis = new Fiz.Chart.Axis();
    plots[1].draw = chartLogFunction();
    plots[1].xAxis = new Fiz.Chart.Axis();

    var bar = new Fiz.Chart.Bar();
    bar.drawPlots(plots, ctx, {});
};
*/
