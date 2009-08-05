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

// Fiz:include static/fiz/Fiz.js 
// Fiz:include static/fiz/FizCommon.js 
// Fiz:include static/fiz/canvas.text.js 
// Fiz:include static/fiz/Chart.js 
// Fiz:include static/fiz/ChartFormat.js 
// Fiz:include static/fiz/ChartBorder.js 
// Fiz:include static/fiz/ChartPlot.js 
// Fiz:include static/fiz/ChartTitle.js 
// Fiz:include static/fiz/ChartTicks.js
// Fiz:include static/fiz/ChartLegend.js
// Fiz:include static/fiz/ChartAxis.js
// Fiz:include static/fiz/ChartSeries.js
// Fiz:include static/fiz/ChartBar.js
// Fiz:include static/fiz/ChartScatter.js

/*
 * A Bar object represents a bar plot and is responsible for initializing and
 * drawing it on the chart. A bar chart make contain several series, making it
 * into a stacked bar chart.
 */
Fiz.Chart.Bar = function () {
	this.series = [];
	this.barWidth = 1;
	this.discrete = true;
	
	for (var i = 0; i < arguments.length; i += 1) {
		if (Fiz.isArray(arguments[i])) {
			this.series[i] = new Fiz.Chart.Series(arguments[i]);
		}
	}

	this.config = {
		name: null,
		axis: ["bottom", "left"],
		opacity: 0.5,
		border: [1, "black", "inside", [true, true, false, true]],
		showInLegend: true,
		allowRemove: true,
		nameFormat: ["1em 'helvetiker'", "black"],
		display: true,
	};

	for (var name in this.config) {
		this.set(name, this.config[name]);
	}
};

Fiz.Chart.Bar.prototype = new Fiz.Chart.Plot();

/*
 * Draws the plot on the canvas. This method expects the origin to be at the
 * bottom left of the plot area and the canvas to be rotated so that the 
 * x axis can be assumed to be at the bottom.
 *
 * @param ctx         Canvas context to draw on
 * @param properties  See Bar.drawPlots documentation
 */
Fiz.Chart.Bar.prototype.draw = function (ctx, properties) {
	ctx.save();
	ctx.translate(properties.barSpacer, 0);
	ctx.globalAlpha = this.config.opacity;
	
	// If we are not 0 based (i.e., we have negative values), we need to draw
    // the bottom border of the plots.
	if (this.yAxis.min < 0) {
		this.config.border[3] = [true, true, true, true];
	}
	
	var stacked = this.stack();
	
	for (var i = 0; i < stacked.length; i++) {
		var yVals = stacked[i];
		var totalY = 0;
		for (j = 0; j < yVals.length; j += 1) {
			
			var x = i * properties.allWidth + properties.i * properties.barWidth;
			
			var height = yVals[j] - totalY;
			var y = totalY - this.yAxis.zero();
			
			ctx.save();
			ctx.fillStyle = this.series[j].config.color || this.config.color;
			
			ctx.translate(x, y);
			Fiz.Chart.drawRect(ctx, properties.barWidth, height,
							   this.config.border, ctx.fillStyle, [false, true, true, true]);
			ctx.restore();
			
			totalY += height;
		}
	}
	ctx.restore();
};

Fiz.Chart.Bar.prototype.drawPlots = function (plots, ctx, config) {
	
	var width = plots[0].xAxis.size;
	
	var wEach = (plots[0].xAxis.size - config.barSpacer) / 
			plots[0].xAxis.labels.length - config.barSpacer;
	
	var properties = {};
	properties.barWidth = wEach / plots.length;
	properties.allWidth = wEach + config.barSpacer;
	properties.barSpacer = config.barSpacer;
	
	for (var i = 0; i < plots.length; i++) {
		properties.i = i;
		ctx.save();
		plots[i].draw(ctx, properties);
		ctx.restore();
	}
};
