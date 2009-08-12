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

// ChartAxisFixture.js --
//
// This file provides a dummy implementation of a Chart.Axis object
// for use in testing.

Fiz.Chart.Axis = function (labels) {
    this.labels = labels;
    this.size = 20;
};

Fiz.Chart.Axis.prototype.positionOf = function (n) {
    if (typeof n === "number") {
        return n * 2;
    } else {
        var arr = [];
        for (var i = 0; i < n.length; i++) {
            arr[i] = n[i] * 2;
        }
        return arr;
    }
}

Fiz.Chart.Axis.prototype.zero = function () {
    return 30;
};
