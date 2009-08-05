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

/**
 * A Border object can be used to draw lines and rectangles with a given border.
 * It supports setting the width, color, todo.
 *
 * @param ctx       (Context) Context to draw on
 * @param width     (Int) Width of the line
 * @param color     (String) Color of the line
 * @param side      (String) One of inside, normal, or outside
 * @param which     (Array<Boolean>) Which sides to draw on, in the order of 
 *                  bottom, left, top, right
 */
Fiz.Chart.Border = function (ctx, width, color, side, which) {
	this.ctx = ctx;
	this.width = width;
	this.color = color;
	this.side = side;
	this.which = which;
};

