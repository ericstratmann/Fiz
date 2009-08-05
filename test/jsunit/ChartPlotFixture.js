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

// ChartPlotFixture.js --
//
// This file provides a dummy implementation of a Chart.Plot object
// for use in testing.

Fiz.Chart.Plot = function (data, data2) {
	this.series = [];
	this.series[0] = new Fiz.Chart.Series(data);
	if (data2 !== undefined) {
		this.series[1] = new Fiz.Chart.Series(data2);
	}
		
	this.config = {};
	this.config.display = true;
}


Fiz.Chart.Plot.prototype.getBarWidth = function () {
	return 1;
}

Fiz.Chart.Plot.prototype.hasDiscreteXAxis = function () {
	return this.discrete !== undefined ? this.discrete : false;
}

Fiz.Chart.Plot.prototype.getBoundingBox = function () {
	var boundary = {};
	boundary.minY = 0;
	boundary.maxY = 10;
	boundary.maxX = 0;
	boundary.maxX = 5;

	return boundary;
};
Fiz.Chart.Plot.prototype.findVal = function (series, key) {
};

Fiz.Chart.Plot.prototype.set = function(name, value) {
	this.config[name] = value;

	for (var i in this.series) {
		if (this.series.hasOwnProperty(i)) {
//			if (this.series[i].config[name] === undefined) {
			this.series[i].config[name] = value;
//			}
		}
	}
};

Fiz.Chart.Plot.prototype.get = function(name) {
	return this.config[name];
};

Fiz.Chart.Plot.prototype.getSeries = function(i) {
	return this.series[i];
}

Fiz.Chart.Plot.prototype.draw = function(xAxis, yAxis, ctx, properties) {
	jsunit.log += "draw()";
};

Fiz.Chart.Plot.prototype.setMinAndMax = function (minY, maxY, minX, maxX) {
	this.minX = minX;
	this.maxX = maxX;
	this.minY = minY;
	this.maxY = maxY;
};

Fiz.Chart.Plot.prototype.getBoundingBox = function () {
	return {
		minX: this.minX,
		maxX: this.maxX,
		minY: this.minY,
		maxY: this.maxY
	};
};
