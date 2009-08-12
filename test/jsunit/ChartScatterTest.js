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

// ChartScatterTest.js --
//
// Jsunit tests for ChartScatter.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartPlot.js");
include("static/fiz/ChartSeries.js");
include("static/fiz/ChartScatter.js");
include("ChartAxisFixture.js");

ChartScatterTest = {};

ChartScatterTest.setUp = function () {
    this.canvas = new Canvas();
    this.ctx = this.canvas.getContext('2d');
};
ChartScatterTest.test_draw = function () {
    xAxis = new Fiz.Chart.Axis();
    yAxis = new Fiz.Chart.Axis();

    var scatter = new Fiz.Chart.Scatter([[1,2], [2, 3]]);
    scatter.xAxis = xAxis;
    scatter.yaXis = yAxis;
    Fiz.Chart.drawShape = chartLogFunction("drawShape");
    scatter.set("color", "blue");
    /*
    scatter.draw(this.ctx);
    assertEqual("save()\ndrawShape(ctx, 2, 16, circle, blue, 4)\n" + 
                "drawShape(ctx, 4, 14, circle, blue, 4)\n" + 
                "restore()\n", jsunit.log);
*/
};
