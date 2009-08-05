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

Fiz.Chart.Line = function (data) {

	this.series = [];
	this.barWidth = 0;
	this.discrete = true;
	
	for (var i = 0; i < arguments.length; i += 1) {
		if (arguments[i].length !== undefined) {
			this.series[i] = new Fiz.Chart.Series(arguments[i]);
		}
	}
	
	this.config = {
		name: null,
		axis: ["bottom", "left"],
		opacity: 0.8,
		nameFormat: ["1em 'helvetiker'", "black"],
		display: true,

//

		area: false
    }

	for (var name in this.config) {
		this.set(name, this.config[name]);
	}
};

Fiz.Chart.Line.prototype = new Fiz.Chart.Plot();

Fiz.Chart.Line.prototype.draw = function (ctx) {
	ctx.save();
	ctx.globalAlpha = this.config.opacity;
	
	ctx.lineWidth = 3;
	ctx.lineCap = "round";


	var first = true;
	var points = [];
	var yVals = [];
	var vals;

	var stacked = this.stack();

	ctx.save();

	for (var i = this.series.length - 1; i >= 0; i--) {
		ctx.save();
		ctx.fillStyle = this.series[i].config["color"] || this.config["color"];
		ctx.strokeStyle = this.series[i].config["color"] || this.config["color"];
		ctx.beginPath();
		var lastX = this.xAxis.size * 0.5 / this.xAxis.labels.length;
		var lastY = stacked[i][0];
		ctx.moveTo(lastX, lastY);
		for (var j = 0; j < this.xAxis.labels.length; j++) {
//			Fiz.Chart.drawShape(ctx, lastX, lastY, "circle", "green", 5);
			if (j + 1 !== this.xAxis.labels.length) {
				var x = this.xAxis.size * (1.5 + j) / this.xAxis.labels.length;
				var y = stacked[j + 1][i];
			}
			ctx.lineTo(x, y);
			lastX = x;
			lasyY = y;
			
		}

		ctx.lineTo(lastX, this.yAxis.zero());
		ctx.lineTo(this.xAxis.size * 0.5 / this.xAxis.labels.length, this.yAxis.zero());
		ctx.fill();
		ctx.restore();
	}
	ctx.restore();
};

