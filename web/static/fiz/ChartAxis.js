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
 *                  and it goes clockwise)
 * @param type      (String) "x" or "y" depending on what type of axis this is
 */
Fiz.Chart.Axis = function (config, side, oSide, type) {
    this.config = config;

    if (this.config.scale === "log") {
        this.config.logBase = parseInt(this.config.logBase, 10);
    }
    this.side = side;
    this.oSide = oSide;
    this.type = type;

    this.plots = []; // List of plots on this axis

    // Whether this axis is discrete or not (continuous). We find this out by
    // checking the plots on the axis.
    this.discrete = undefined; // Whether this axis is discrete or not
                               // (continuous)

    //
    this.min = undefined; // Minimum value on this axis.
    this.max = undefined // Maximum value on this axis

    this.rmin = undefined;
    this.rmax = undefined;
};

/*
 * Returns whether this axis represents an X axis or not. An axis is either
 * an X axis or a Y axis.
 *
 * @return          (Bool) Whether this axis is an X axis
 */
Fiz.Chart.Axis.prototype.isXAxis = function () {
    return this.type === "x";
};

/**
 * Adds a plot to our list of plots.
 *
 * @param plot      (Chart.Plot) Plot to add to this axis
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
 * Sets the @code{labels} instance variable for the axis.
 *
 * @param plot      Plot to get the labels from
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
 * @return          An array of string labels
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
 * @return          An array of string labels
 */
Fiz.Chart.Axis.prototype.getContinuousAxisValues = function () {
    if (this.config.ticks === true || this.config.ticks === "true") {
        if (this.config.scale === "log") {
            return this.generateLogTickMarks();
        } else if (this.config.scale === "linear") {
            return this.generateTickMarks();
        } else {
            throw "Axis.getContinuousAxisValues: bad config.scale value";
        }
    } else {
        return [];
    }
};


/**
 * Returns the @code{n}'th most significant figures of @code{number}.
 *
 * @param number    (Number) Number to get the signicant figures of
 * @param n         (Integer) Number of significant figures
 * @return          The @code{n}'th significant figures of @code{number}
 */
Fiz.Chart.Axis.prototype.getNSigFigs = function (number, n) {
    var tmp = Math.log(number) * Math.LOG10E;
    if (tmp < 0) {
        tmp -= 1; // necessary because rounding works differently below 0
    }
    return parseInt(number / Math.pow(10, parseInt(tmp, 10) - (n - 1)), 10);
};

/*
 * Returns tick mark locations for an axis using a logarithmic scale. Values
 * are placed at every log power.
 *
 * @return          Array<Number> Array of locations to place tick marks
 */
Fiz.Chart.Axis.prototype.generateLogTickMarks = function () {
    var ticks = [];
    for (i = this.min; i <= this.max; i *= this.config.logBase) {
        ticks.push(i);
    }
    return ticks;
};

/**
 * This function attempts to smartly guess what would be some nice places to
 * put tick marks. For example [0,1,2,3], [5000,10000,15000], etc.
 *
 * @return          (Array<Int>) An array of tick mark locations
 */
Fiz.Chart.Axis.prototype.generateTickMarks = function () {
    var min = this.min;
    var max = this.max;
    var good = [10, 20, 50];
    var minTicks = 3;

    var range = this.getNSigFigs(max - min, 2);

    for (var numTicks = minTicks; numTicks < 100; numTicks++) {
        var num = (max - min) / numTicks;
        var minTick = this.getNSigFigs(range / (numTicks + 1), 2);
        var excTick = this.getNSigFigs(range / (numTicks), 2);

        for (var i = 0; i < good.length; i++) {
            if ((minTick < good[i] && good[i] <= excTick) ||
                (excTick <= good[i] && good[i] < minTick)) {
                // we found a good number

                // Now we need to undo what we did before in converting to
                // two sig figs
                var log = Math.log((max - min) / numTicks)/Math.log(10);
                if (log < 0) {
                    log -= 0;
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

    throw "Axis.generateTickMarks: unknown error";
}

/*
 * If @code{plot} has any values outside of the current min and max values,
 * update them to the new min or max. If the axis is linear and does not have
 * negative values, the minimum is also zero. If the axis is logarithmic, the
 * maximum value is the largest multiple of the base smaller or equal the
 * minimum of all plot values.
 *
 * @param plot      Plot to get min and max values from
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
}

/**
 * Scales the number depending on what scale the axis is using. This is
 * necessary when converting from logical to chart coordinates. For example,
 * a value of 10 on an axis with a log scale using base 10 from 1-100 is in
 * the middle, not near the bottom.
 *
 * @param number    (Number) Number to scale
 * @return          @code{number} after being scaled
 */
Fiz.Chart.Axis.prototype.getScaledNumber = function (number) {
    if (this.config.scale === "linear") {
        return number;
    } else if (this.config.scale === "log") {
        return Math.log(number)/Math.log(this.config.logBase);
    } else {
        throw "Axis.getScaledNumber: bad config.scale value: " +
            this.config.scale;
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
    var min = this.getScaledNumber(this.min);
    var max = this.getScaledNumber(this.max);
    var val = this.getScaledNumber(val);

    return  this.size * ((val - min) / (max - min));
}

/*
 * Returns the chart coordinates for the 0 of the chart. This may not be at the
 * bottom of the chart if there are negative values. In the axis is a log scale,
 * then it always refers to the bottom of the chart.
 *
 * @return          The chart coordinates for the "zero" of the axis.
 */
Fiz.Chart.Axis.prototype.zero = function () {
    if (this.config.scale == "linear") {
        return this.logicalToChartCoords(0);
    } else if (this.config.scale == "log") {
        return 0;
    }
}
