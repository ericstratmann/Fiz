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

/*
 * A scatter object is a plot using for drawing a scatter plot. Both of its axes
 * are continuous.
 */
Fiz.Chart.Scatter = function () {
    this.series = [];
    this.barWidth = 0;
    this.discrete = false;
    

    // todo: better isarray check
    for (var i = 0; i < arguments.length; i += 1) {
        if (arguments[i].length !== undefined) {
            this.series[i] = new Fiz.Chart.Series(arguments[i]);
        }
    }

    this.config = {
        name: null,
        nameFormat: ["1em 'helvetiker'", "black"],
        axis: ["bottom", "left"],
        opacity: 0.5,
        showInLegend: true,
        radius: 4,
        allowRemove: true,
        display: true,
        scale: "linear"
    };

    for (var name in this.config) {
        this.set(name, this.config[name]);
    }
};

Fiz.Chart.Scatter.prototype = new Fiz.Chart.Plot();

Fiz.Chart.Scatter.prototype.draw = function (ctx) {
    ctx.save();
    this.stack();
    for (var i = 0; i < this.series[0].data.length; i++) {
        var yVal = this.yAxis.positionOf(this.series[0].data[i][1]);
        var x = parseInt(this.series[0].data[i][0], 10);
        var xVal = this.xAxis.positionOf(x);
        Fiz.Chart.drawShape(ctx, xVal, this.yAxis.size - yVal, this.series[0].config.shape || "circle",
                            this.series[0].config.color, this.series[0].config.radius);
    }

    ctx.restore();
};
