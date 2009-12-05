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
 * This class can be extended by other objects which represent a plot, such as
 * a bar or line plot. This class provides a few methods which are useful to
 * many types of plots, though they can be overridden if necessary. Since
 * javascipt doesn't have any real notion of interfaces or inheritance, it
 * is not necessary for plots to use this class.
 */
Fiz.Chart.Plot = function () {};

/*
 * Returns the value of ths object's discrete variable, which is a boolean
 * describing whether the X axis of the plot is discrete or not.
 *
 * @return          (Bool) Whether the plot has a discrete X axis
 */
Fiz.Chart.Plot.prototype.hasDiscreteXAxis = function () {
    return this.discrete;
};

/*
 * Returns the minimum and maximum values of the data. If the plot has a
 * discrete X axis, then minX and maxX are not set since they do not make sense
 * in that context. If the plot is stacked, it will take the sum of all the
 * stacks.
 *
 * @return          An object with minY, maxY, minX, maxX set
 */
Fiz.Chart.Plot.prototype.getBoundingBox = function () {
    var maxX, maxY;
    var minX, minY;
    var boundary = {};

    if (this.series.length === 0) {
        return {};
    }

    // Get min and max x values
    if (this.discrete === false) {
        for (var i = 0; i < this.series[0].data.length; i++) {
            var data = this.series[0].data[i];
            if (maxX === undefined) {
                minX = data[0];
                maxX = data[0];
            } else {
                minX = Math.min(data[0], minX);
                maxX = Math.max(data[0], maxX);
            }
        }
        boundary.minX = minX;
        boundary.maxX = maxX;
    }

    // Get min and max y values
    for (var i = 0; i < this.series[0].data.length; i++) {
        var total = 0; // sum of all series if it's stacked
        for (var j = 0; j < this.series.length; j++) {
            var data = this.series[j].data[i];
            total += data[1];
        }
        if (maxY === undefined) {
            minY = total;
            maxY = total;
        } else {
            minY = Math.min(total, minY);
            maxY = Math.max(total, maxY);
        }
    }

    boundary.minY = minY;
    boundary.maxY = maxY;

    return boundary;
};

/*
 * Looks for the value associated with the given key in the series' data
 * variable
 *
 * @param series    (Chart.Series) Series object to get the data from
 * @param key       (String or Int) Key to search for
 *
 * @return          (Int or undefined) Value associated with the key or
 *                  undefined if the key is not present
 */
Fiz.Chart.Plot.prototype.findVal = function (series, key) {
    var data = series.data;
    for (var i = 0; i < data.length; i += 1) {
        if (data[i][0] === key) {
            return data[i][1];
        }
    }
    return undefined;
};

/**
 * Sets the @code{name} key in the plot's config object, as well as for
 * all of plot's series.
 *
 * @param name      (String) Name of key in config object
 * @param value     Value to set in config
 */
Fiz.Chart.Plot.prototype.set = function(name, value) {
    this.config[name] = value;

    for (var i in this.series) {
        if (this.series.hasOwnProperty(i)) {
            this.series[i].config[name] = value;
        }
    }
};

/**
 * Returns the value of the given key in config object
 *
 * @param name      (String) Name of key
 * @return          Value of key in config object
 */
Fiz.Chart.Plot.prototype.get = function(name) {
    return this.config[name];
};

/**
 * Returns the series with the given index
 *
 * @return          (Chart.Series) Appropriate series
 */
Fiz.Chart.Plot.prototype.getSeries = function(i) {
    return this.series[i];
};

/**
 * Calls the draw method on every plot passed in.
 *
 * @param plots     Array<Chart.Plot> Array of plots to call draw() on
 * @param ctx       Canvas context
 */
Fiz.Chart.Plot.prototype.drawPlots = function (plots, ctx) {
    for (var i = 0; i < plots.length; i++) {
        plots[i].draw(ctx);
    }
};

/*
 * Stacks all the series in the plot. For each data point, creates an array
 * of a running total of
 *
 * @return          An two dimentional array. The first dimension corresponds
 *                  each data point in the x axis, and the second dimension
 *                  is the running total of the series for that data point.
 */
Fiz.Chart.Plot.prototype.stackSeries = function () {
    var allVals = [];
    for (var i = 0; i < this.xAxis.labels.length; i++) {
        var vals = [];
        for (var j = 0; j < this.series.length; j++) {
            var val = this.findVal(this.series[j], this.xAxis.labels[i]);
            var previous = j === 0 ? 0 : vals[j - 1];
            vals.push(val + previous);
        }
        allVals.push(vals);
    }

    return allVals;
};

/**
 * Converts all values from logical coordinates to chart coordinates.
 */
Fiz.Chart.Plot.prototype.convertCoordinates = function (vals, axis) {
    for (var i = 0; i < vals.length; i++) {
        if (typeof vals[i] === "number") {
            vals[i] = axis.logicalToChartCoords(vals[i]);
        } else {
            for (var j = 0; j < vals[i].length; j++) {
                vals[i][j] = axis.logicalToChartCoords(vals[i][j]);
            }
        }
    }
};

/*
 * Adds a new series to the plot. In addition to adding it to its internal list,
 * it also sets all undefined properties that the plot has set.
 *
 * @param series    (Chart.Series) Series to add to the plot
 */
Fiz.Chart.Plot.prototype.addSeries = function (series) {
    this.series.push(series);
    for (var name in this.config) {
        if (series.get(name) === undefined) {
            series.set(name, this.config[name]);
        }
    }
};
