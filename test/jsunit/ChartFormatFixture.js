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

// ChartFormatFixture.js --
//
// This file provides a dummy implementation of a Chart.Format object
// for use in testing.

Fiz.Chart.Format = function (ctx, font) {
    jsunit.log += "Format(" + font[0] + ", " + font[1] + ")\n";
    this.ctx = ctx;
    this.font = font[0]
    this.color = font[1];
};

Fiz.Chart.Format.prototype.width = function(text) {
//  jsunit.log += "width(text: " + text + "\n";
    if (text !== null) {
        return 20;
    } else {
        return 0;
    }
};

Fiz.Chart.Format.prototype.height = function(text) {
//  jsunit.log += "height(text: " + text + "\n";
    if (text !== null) {
        return 20;
    } else {
        return 0;
    }
};

Fiz.Chart.Format.prototype.draw = logFunction("draw");
