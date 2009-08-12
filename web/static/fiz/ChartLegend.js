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
 * A Legend object renders the legend of the chart, which displays the name
 * of each series and its color. For each series, the legend displays a box
 * (or shape for appropriate types of series) with the series' color and the
 * series name to the right of it. It implements the Section interface.
 * 
 * @param ctx       (Context) Canvas context
 * @param config    Legend configuration object
 * @param series    (Array<Chart.Series>) An array of all visible series.
 */
Fiz.Chart.Legend = function (ctx, config, series) {
    this.ctx = ctx;
    this.config = config;
    this.series = series;
};

/*
 * Draws the legend on the chart.
 * 
 * @param box       Bounding box for the section.
 */
Fiz.Chart.Legend.prototype.render = function (box) {
    if (this.config.display === "false" || this.config.display === false) {
        return;
    }

    if (this.series.length === 0) {
        return;
    }
    
    this.ctx.save();
    if (this.config.location === "right") {
        this.ctx.translate(0, -box.width); // move to the left edge
    }

    // Currently only incremented by one each loop, but could be more in the
    // future if we allow multiple lines per name
    var linesUsed = 0;

    var format;
    for (var i = 0; i < this.series.length; i++) {
        if (this.series[i].config.nameFormat) {
            format = new Fiz.Chart.Format(this.ctx, this.series[i].config.nameFormat);
        } else if (this.config.nameFormat) {
            format = new Fiz.Chart.Format(this.ctx, this.config.nameFormat);
        } else {
            format = new Fiz.Chart.Format(this.ctx, ["1em helteviker", "black"]);
        }
        var lineHeight = format.height("x") * 1.5; // random string
        var serie = this.series[i];
        this.ctx.save();
        this.ctx.translate(0, linesUsed * lineHeight);
        this.ctx.fillStyle = serie.config.color;
        this.ctx.globalAlpha = serie.config.opacity || 1;

        this.ctx.fillRect(lineHeight/2, 0, lineHeight/2, lineHeight/2); // square
        this.ctx.translate(lineHeight * 1.5, 10);
        format.draw(serie.config.name, serie.config.color);
        linesUsed++;
        this.ctx.restore();
    }

    this.ctx.restore();
};

/*
 * Returns the width in pixels required to draw the legend. This is the width
 * of the longest name plus the width of the box and empty space. If the
 * legend will be drawn in the chart, the size is 0.
 *
 * @return     (Int) Width in pixels required
 */
Fiz.Chart.Legend.prototype.sizeRequired = function () {
    if (this.config.display === "false" || this.config.display === false ||
        this.config.location !== "right" || this.series.length === 0) {
        return 0;
    }

    var max = 0;
    for (i = 0; i < this.series.length; i++) {
        var format = new Fiz.Chart.Format(this.ctx,
                     this.series[i].config.nameFormat);
        var len = format.width(this.series[i].config.name) * 1.2;
        max = Math.max(max, len);
    }
    
    var boxSpace = format.height("x") * 2.7;
    return max + boxSpace
};
