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
 * @param format    (Chart.Format) Object used to format the title
 */
Fiz.Chart.Title = function (ctx, title, format) {
	this.ctx = ctx;
	this.title = title;
	this.format = format;
};


/*
 * Draws the title on the chart, moving and rotating the canvas if necessary.
 */
Fiz.Chart.Title.prototype.render = function(box) {
	if (this.title === null || this.title === undefined) {
		return;
	}
	
	var format = new Fiz.Chart.Format(this.ctx, this.format);
	this.ctx.save();
	this.moveAndOrient(box);
//	this.ctx.strokeRect(0, 0, box.width, box.height);
	var x = box.width/2 - format.width(this.title)/2;
	var y = format.height(this.title);
	this.ctx.translate(x, y * 1.5);
	format.draw(this.title);
	this.ctx.restore();
};

/*
 * Returns the width in pixels required to draw the title. Since titles
 * are always written parallel to the closest edge, this is based on the 
 * height of the text, not the width.
 *
 * @return          (Integer) Width in pixels required
 */
Fiz.Chart.Title.prototype.sizeRequired = function () {
	if (this.title === undefined || this.title === null) {
		return 0;
	}
	var format = new Fiz.Chart.Format(this.ctx, this.format);
	
	// 2 is for aesthetic reasons to allow some padding
	return format.height(this.title) * 2;
};

/**
 * Translates and rotates the canvas as necessary so we can pretend we are
 * drawing the title on top of the chart. The left and right sides are drawn
 * sideways, so they must be rotated. The bottom needs to be translated up or
 * else we will draw the title too low.
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
