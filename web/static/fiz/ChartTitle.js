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
 * A Title object represents a title in the chart and is responsible for
 * rendering it. This includes both the title for the graph and axis
 * titles. It implements the section interface.
 *
 * @param ctx       (Context) Canvas context
 * @param title     (String) Text of the title
 * @param font      (String) Font used to render the chart
 * @param color     (String) Color used to draw the chart
 */
Fiz.Chart.Title = function (ctx, title, font, color) {
    this.ctx = ctx;
    this.title = title;
    this.font = font;
    this.color = color;
};

/*
 * Draws the title on the chart, moving and rotating the canvas if necessary
 * The title is drawn in the middle of its containing box, which is twice its
 * height.
 *
 * @param box       Object describing the containing box's width, height, and
 *                  what side it is being drawn on (top, right, bottom, or
 *                  left).
 */
Fiz.Chart.Title.prototype.render = function(box) {
    if (this.title === null || this.title === undefined) {
        return;
    }

    var format = new Fiz.Chart.Format(this.ctx, this.font, this.color);
    this.ctx.save();
    this.moveAndOrient(box);
//  this.ctx.strokeRect(0, 0, box.width, box.height); // debug
    var x = box.width/2 - format.width(this.title)/2;
    var y = format.height(this.title);
    this.ctx.translate(x, y * 1.5);
    format.draw(this.title);
    this.ctx.restore();
};

/*
 * Returns the height in pixels required to draw the title. Since titles
 * are always written parallel to the closest edge, this is based on the
 * height of the text. A title requires twice the size of its height.
 *
 * @return          (Integer) Height in pixels required
 */
Fiz.Chart.Title.prototype.sizeRequired = function () {
    if (this.title === undefined || this.title === null) {
        return 0;
    }
    var format = new Fiz.Chart.Format(this.ctx, this.font, this.color);

    return format.height(this.title) * 2;
};

/**
 * Translates and rotates the canvas as necessary so we can pretend we are
 * drawing the title on top of the chart. The left and right sides are drawn
 * sideways, so they must be rotated. The bottom needs to be translated up or
 * else we will draw the title too low.
 *
 * @param box       Object describing what side the title is beign drawn on (top,
 *                  right, bottom, or left).
 */
Fiz.Chart.Title.prototype.moveAndOrient = function (box) {
    var side = box.side;
    if (side === "left") {
        this.ctx.rotate(-Math.PI/2);
    } else if (side === "bottom") {
        this.ctx.translate(0, -box.height);
    } else if (side === "right") {
        this.ctx.rotate(-Math.PI/2);
    }
};
