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

// ChartLegendTest.js --
//
// Jsunit tests for ChartLegend.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/FizCommon.js");
include("CanvasFixture.js");
include("static/fiz/Chart.js");
include("ChartFormatFixture.js");
include("static/fiz/ChartLegend.js");
include("static/fiz/ChartPlot.js");

ChartLegendTest = {};

ChartLegendTest.setUp = function () {
    this.canvas = new Canvas();
    this.ctx = this.canvas.getContext("2d");
    this.series = [];
    this.series.push({config: {name: "foo", color: "green", nameFormat:
                               ["1em foo", "blue"]}});
    this.series.push({config: {name: "bar", color: "blue", nameFormat:
                               ["1em bar", "green"]}});
    this.box = {
        width: 100,
        height: 50
    };
};

ChartLegendTest.test_render_doNotShow = function () {
    var config = { display: false };
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series);
    legend.render(this.box);
    assertEqual("", jsunit.log);
};

ChartLegendTest.test_render_noSeries = function () {
    var config = { display: true };
    var legend = new Fiz.Chart.Legend(this.ctx, config, []);
    legend.render(this.box);
    assertEqual("", jsunit.log);
};

//not implemented yet
/*
ChartLegendTest.test_render_middle = function () {
    //todo fix
    var config = { display: true, legendLocation: [10, 15]};
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series)
    legend.render(this.box);
    assertEqual("save()\ntranslate(0, -100)\nsave()\ntranslate(0, 0)\n" + 
                "fillRect(15, 0, 15, 15)\ntranslate(45, 10)\n" + 
                "draw(foo, green)\nrestore()\nsave()\ntranslate(0, 30)\n" + 
                "fillRect(15, 0, 15, 15)\ntranslate(45, 10)\ndraw(bar, blue)\n" +
                "restore()\nrestore()\n", jsunit.log);
};
*/

ChartLegendTest.test_render_right = function () {
    var config = { display: true, location: "right" };
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series)
    legend.render(this.box);
    assertEqual("save()\ntranslate(0, -100)\nFormat(1em foo, blue)\nsave()\ntranslate(0, 0)\n" + 
                "fillStyle = green\nglobalAlpha = 1\nfillRect(15, 0, 15, 15)\ntranslate(45, 10)\n" + 
                "draw(foo, green)\nrestore()\nFormat(1em bar, green)\nsave()\ntranslate(0, 30)\n" + 
                "fillStyle = blue\nglobalAlpha = 1\nfillRect(15, 0, 15, 15)\ntranslate(45, 10)\ndraw(bar, blue)\n" +
                "restore()\nrestore()\n", jsunit.log);
};

ChartLegendTest.test_render_legendFormat = function () {
    this.series = [({config: {name: "foo", color: "green"}})];
    var config = { display: true, location: "right", nameFormat: ["bar", "green"]};
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series)
    legend.render(this.box);
    assertEqual("save()\ntranslate(0, -100)\nFormat(bar, green)\nsave()\ntranslate(0, 0)\n" + 
                "fillStyle = green\nglobalAlpha = 1\nfillRect(15, 0, 15, 15)\ntranslate(45, 10)\n" + 
                "draw(foo, green)\nrestore()\nrestore()\n", jsunit.log);
};

ChartLegendTest.test_render_defaultFormat = function () {
    this.series = [({config: {name: "foo", color: "green"}})];
    var config = { display: true, location: "right"};
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series)
    legend.render(this.box);
    assertEqual("save()\ntranslate(0, -100)\nFormat(1em helteviker, black)\nsave()\ntranslate(0, 0)\n" + 
                "fillStyle = green\nglobalAlpha = 1\nfillRect(15, 0, 15, 15)\ntranslate(45, 10)\n" + 
                "draw(foo, green)\nrestore()\nrestore()\n", jsunit.log);
};

ChartLegendTest.test_render_multipleFormat = function () {
    delete this.series[1].config.nameFormat;
    var config = { display: true, location: "right", nameFormat: ["bar", "green"]};
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series)
    legend.render(this.box);
    assertEqual("save()\ntranslate(0, -100)\nFormat(1em foo, blue)\nsave()\ntranslate(0, 0)\n" + 
                "fillStyle = green\nglobalAlpha = 1\nfillRect(15, 0, 15, 15)\ntranslate(45, 10)\n" + 
                "draw(foo, green)\nrestore()\nFormat(bar, green)\nsave()\ntranslate(0, 30)\n" + 
                "fillStyle = blue\nglobalAlpha = 1\nfillRect(15, 0, 15, 15)\ntranslate(45, 10)\ndraw(bar, blue)\n" +
                "restore()\nrestore()\n", jsunit.log);
};

ChartLegendTest.test_render_withOpacity = function () {
    this.series = [({config: {name: "foo", color: "green", opacity: 0.8}})];
    var config = { display: true, location: "right", nameFormat: ["bar", "green"]};
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series)
    legend.render(this.box);
    assertEqual("save()\ntranslate(0, -100)\nFormat(bar, green)\nsave()\ntranslate(0, 0)\n" + 
                "fillStyle = green\nglobalAlpha = 0.8\nfillRect(15, 0, 15, 15)\ntranslate(45, 10)\n" + 
                "draw(foo, green)\nrestore()\nrestore()\n", jsunit.log);
};

ChartLegendTest.test_sizeRequired_inMiddle = function () {
    var config = { display: true, location: [0, 0] };
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series);
    assertEqual(0, legend.sizeRequired());
};

ChartLegendTest.test_sizeRequired_noSeries = function () {
    var config = { location: "right" };
    var legend = new Fiz.Chart.Legend(this.ctx, config, []);
    assertEqual(0, legend.sizeRequired());
};

ChartLegendTest.test_sizeRequired = function () {
    var config = { location: "right" };
    var legend = new Fiz.Chart.Legend(this.ctx, config, this.series);
    assertEqual(78, legend.sizeRequired());
};
