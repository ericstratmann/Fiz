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
 * Describes a format used to draw text. Currently, this includes the font and
 * color. Given a string, this object can return its width or height, or draw
 * it on the canvas.
 *
 * @param ctx       (Context) Context to draw on
 * @param font      (String) Font of the text
 * @param color     (String) Color of the text
 */
Fiz.Chart.Format = function (ctx, font) {
	this.ctx = ctx;
	this.font = font[0];
	this.color = font[1];
};

/**
 * Returns the width in pixels the text would take if drawn on the canvas.
 *
 * @param text      (String) Text to take the width of
 * @return          (Integer) Width the text would take if drawn
 */
Fiz.Chart.Format.prototype.width = function(text) {
	this.save();
	var size = this.ctx.measureText(text).width;
	this.restore();
	return size;
};

/**
 * Returns the width in height the text would take if drawn on the canvas.
 *
 * @param text      (String) Text to take the height of
 * @return          (Integer) Height the text would take if drawn
 */
Fiz.Chart.Format.prototype.height = function(text) {
	this.save();
	var size = this.ctx.measureText("M").width * 1;
	this.restore();
	return size;
};


/**
 * Draws the given text on the canvas with this object's format. The color of
 * the text can be overriden if passed in as an option paramater.
 *
 * @param text      (String) Text to draw on the canvas
 * @param color     Optional. (String) Color of the text
 */
Fiz.Chart.Format.prototype.draw = function(text, color) {
	color = color || this.color;
	this.save();
	this.ctx.fillStyle = color;
	this.ctx.fillText(text, 0, 0);
	this.restore();
};

/**
 * Private function to save the old font. This is a workaround the way we
 * currently do text, where the old font is not restored after a
 * context.restore().A
 */
Fiz.Chart.Format.prototype.save = function() {
	this.oldFont = this.ctx.font;
	this.ctx.font = this.font;
}

/**
 * Private function to restore the old font.
 */
Fiz.Chart.Format.prototype.restore = function() {
	this.ctx.font = this.oldFont;
};
