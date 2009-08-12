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
 * An axis represents one axis of the chart, and has some information about it as
 * well as some helper functions. Plots may use this to figure out where to draw
 * on the chart.
 *
 * @param config    An object with the different configuration options for this
 *                  axis. See the java documentation for a list of supported
 *                  options.
 * @param side      (Integer) Which side of the chart we are on (0 is bottom
 *                  it goes clockwise
 * @param type      (String) "x" or "y" depending on what type of axis this is
 */
Fiz.Chart.Axis = function (config, side, type) {
    this.config = config;
    if (this.config.scale === "log") {
        this.config.logBase = parseInt(this.config.logBase, 10);
    }
    this.side = side;
    this.type = type;
    
    this.plots = []; // List of plots on this axis
    
    // Whether this axis is discrete or not (continuous). We find this out by
    // checking the plots on the axis.
    this.discrete = undefined; // Whether this axis is discrete or not
                               // (continuous)
    this.min = undefined; // Minimum value on this axis.
    this.max = undefined // Maximum value on this axis
};

/*
 * Returns whether this axis represents an X axis or not. An axis is either
 * an X axis or a Y axis.
 */
Fiz.Chart.Axis.prototype.isXAxis = function () {
    return this.type === "x";
};

/**
 * Adds a plot to out list of plots.
 */
Fiz.Chart.Axis.prototype.addPlot = function (plot) {
    this.plots.push(plot);

    this.setDiscreteness(plot);
    this.setMinAndMax(plot);
    this.setLabels(plot);
    
};

/**
 * Sets whether the axis is discrete or not based on the discreteness of the
 * plot. If this is a Y axis, then it is automatically not discrete.
 *
 * @param plot      Plot object to check discreteness of
 */
Fiz.Chart.Axis.prototype.setDiscreteness = function (plot) {
    if (this.discrete === undefined)  {
        this.discrete = plot.hasDiscreteXAxis() && this.isXAxis();
    } else {
        if (this.discrete !== plot.hasDiscreteXAxis() && this.isXAxis()) {
            throw "Axis.addPlot: Plots disagree on discreteness";
        }
    }
};

/*
 * Sets the @code{labels} isntance variable for the axis.
 * 
 * @param plot      Plot to get the labels from
 *
 */
Fiz.Chart.Axis.prototype.setLabels = function (plot) {
    if (this.discrete && this.isXAxis()) {
        this.labels = this.getDiscreteAxisValues(plot);
    } else {
        this.labels = this.getContinuousAxisValues(plot);
    }
};

/*
 * Find and return the labels we will use for @code{plot}. Currently only 
 * X axes can be discrete, but this could potentially also support Y axes
 * in the future. The labels are based on the X part of the (X, Y) pairs in
 * the plot's data.
 *
 * @param plot      The plot object to get the labels from
 * @param return    An array of string labels
 */
Fiz.Chart.Axis.prototype.getDiscreteAxisValues = function (plot) {
    var labels = [];
    
    var data = plot.series[0].data;
    for (var i = 0; i < data.length; i++) {
        labels.push(data[i][0].toString());
    }
    
    return labels;
};

/*
 * Finds and returns the tick marks we will use to label the plot. This only
 * makes sense if the axis is continuous. There are several ways to get these
 * tick marks. If the user provides a number @code{X} in the appropriate .ticks
 * config variable, put a tick mark every @code{X} numbers. If the user provides
 * an array, just use that. Otherwise, calculate them in a way that makes sense
 * (see generateTickMarks()).
 * 
 * @param axis      Axis object
 * @param plot      A plot object. Needed to find the min and max values
 * @return          An array of string labels
 */
Fiz.Chart.Axis.prototype.getContinuousAxisValues = function (plot) {
    var tickConfig = this.config.ticks;
    if (typeof tickConfig === "number") {
        var ticks = [];
        for (var i = this.min; i <= this.max; i += tickConfig) {
            ticks.push(i);
        }
        return ticks;
    } else if (Fiz.isArray(tickConfig)) {
        return tickConfig;
    } else {
        if (this.config.scale == "log") {
            var ticks = [];
            for (i = this.rmin; i <= this.rmax; i *= this.config.logBase) {
                ticks.push(i);
            }
            return ticks;
        } else {
            return this.generateTickMarks(this.min, this.max); 4
        }
    }
};


Fiz.Chart.Axis.prototype.getNSigFigs = function (number, n) {
    var tmp = Math.log(number) * Math.LOG10E;
    if (tmp < 0) {
        tmp -= 1; // necessary because rounding works differently below 0
    }
    return parseInt(number / Math.pow(10, parseInt(tmp, 10) - (n - 1)), 10);
};

/**
 * This function attempts to smartly guess what would be some nice places to
 * put tick marks. For example [0,1,2,3], [5000,10000,15000], etc.
 *
 * @param min       (Int) Minimum value on the axis

 * @param max       (Int) Maximum value on the axis
 * @return          (Array<Int>) An array of tick mark locations
 */
Fiz.Chart.Axis.prototype.generateTickMarks = function (min, max) {
    var good = [10, 20, 50];
    var minTicks = 3;

    var range = this.getNSigFigs(max - min, 2);

    for (var numTicks = minTicks; numTicks < 100; numTicks++) {
        var num = (max - min) / numTicks;
        var minTick = range / (numTicks + 1);
        var excTick = range / (numTicks);

        for (var i = 0; i < good.length; i++) {
            if ((minTick < good[i] && good[i] <= excTick) || 
                (excTick <= good[i] && good[i] < minTick)) {
                // we found a good number
                
                // Now we need to undo what we did before in converting to
                // two sig figs
                var log = Math.log((max - min) / numTicks)/Math.log(10);
                if (log < 0) {
                    log -= 1;
                }
                
                var val = good[i] * Math.pow(10, parseInt(log)-1);
                var ticks = [];
                for (var j = min; j <= max + .00000001; j += val) { // float roundoff
                    ticks.push(j);
                }
                return ticks;
            }
        }
    }
}
/*
 * Sets the minimum and maximum values for the axis. This is done by looping
 * through all plots on the axis and find their min and max values. The axis is
 * 0-based if linear and has no negative values. If logarithmic, it is a
 * fraction of the min value.
 *
 * @param axis      Axis object
 */
Fiz.Chart.Axis.prototype.setMinAndMax = function (plot) {
    if (this.discrete) {
        return;
    }

    var min, max;
    var boundary = plot.getBoundingBox();
    if (this.isXAxis()) {
        if (this.min === undefined) {
            min = boundary.minX;
            max = boundary.maxX;
        } else {
            min = Math.min(this.min, boundary.minX);
            max = Math.max(this.max, boundary.maxX);
        }
    } else {
        if (this.min === undefined) {
            min = boundary.minY;
            max = boundary.maxY;
        } else {
            min = Math.min(min, boundary.minY) || boundary.minY;
            max = Math.max(max, boundary.maxY) || boundary.maxY;
        }
    }

    if (this.config.scale == "linear" && min > 0) {
        min = 0;
    } else if (this.config.scale == "log") {
        min = Math.pow(this.config.logBase, parseInt(
            Math.log(min)/Math.log(this.config.logBase), 10));
    }
    if (this.config.scale == "linear" && max < 0) {
        max = 0;
    }

    if (this.min !== undefined) {
        this.max = Math.max(this.max, max);
        this.min = Math.min(this.min, min);
    } else {
        this.max = max;
        this.min = min;
    }

    this.fixValues(); // needed for log
}


/*
 * Scales min and max values for log axes to make them look linear. Should
 * be called after setting the min and max values elsewhere.
 */
Fiz.Chart.Axis.prototype.fixValues = function () {
    if (this.config.scale == "log") {
        this.rmin = this.min;
        this.rmax = this.max;
        this.min = Math.log(this.min)/Math.log(this.config.logBase);
        this.max = Math.log(this.max)/Math.log(this.config.logBase);
    }
}

/*
 * Given a y value (or array of them), returns the position of this value in
 * terms of chart coordinates. This involves two steps. In the first, we must
 * modify the value depending on the type of scale, so that the plot does not
 * need to do this itself. Second, we convert from logical values to coordinates
 * of the actual chart.
 *
 * @param keys      A y value or an array of them
 * @return          A converted y value or an array of them
 */
Fiz.Chart.Axis.prototype.positionOf = function (vals) {

    var isNum = false;
    if (typeof vals == "number") {
        isNum = true;
        vals = [vals];
    }


    var val;
    for (var j = 0; j < vals.length; j++) {
        if (this.config.scale === "log") {
            val = Math.log(vals[j])/Math.log(this.config.logBase);
        } else {
            val = vals[j];
        }
        vals[j] = this.size * ((val - this.min) / (this.max - this.min));
    }
    
    if (isNum) {
        return vals[0];
    } else {
        return vals;
    }
};

/*
 * Converts from logical coordinates to chart coordinates. For instance, we
 * might be trying to plot the point 5 on an axis that goes from 0 to 10. If
 * the size of the axis is 100, then the chart coordinate would be 50.
 *
 * @params val      (Number) Logical coordinate
 * @return          (Number) Chart coordinate
 */
Fiz.Chart.Axis.prototype.logicalToChartCoords = function (val) {
    return  this.size * ((val - this.min) / (this.max - this.min));
}

/*
 * Returns the chart coordinates for the 0 of the chart. This may not be at the
 * bottom of the chart if there are negative values. In the axis is a log scale,
 * then it always refers to the bottom of the chart.
 */
Fiz.Chart.Axis.prototype.zero = function () {
    if (this.config.scale == "linear") {
        return this.logicalToChartCoords(0);
    } else if (this.config.scale == "log") {
        return 0;
    }
}
