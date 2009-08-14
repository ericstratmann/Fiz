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

/** Chart.js --
 *
 * This file draws a chart with various plots and series. Different
 * types of plots may be used if the appropriate files are included
 * (BarChart.js, etc.) The chart can be configured in numerous ways by setting
 * configuration variables. Please see the java documentation for a full list
 * of variables.
 */


// Public methods

/**
 * Creates a new Chart object. The only argument is the HTML id of the canvas
 * element. The object initializes a few private variables but does not display
 * anything yet.
 *
 * @param id   (String) Id for the {code <canvas>} element that contains
 *             this element
 */
Fiz.Chart = function (id) {
    // A list of colors to use when we need to assign a series a color.
    this.colors = ["blue", "red", "green", "purple", "orange", "skyblue",
                   "darkgray", "deeppink", "gold", "olive", "black", "tan", "palegreen"];


    // List of prefixes for different configuration objects (e.g., xAxisConfig).
    // Each of the these objects contains configuration options for a part of
    // the chart, such as the legend, one of the axes, or the chart itself.
    this.prefixes = ["legend", "xAxis", "yAxis", "bottomAxis", "topAxis",
                    "leftAxis", "rightAxis", ""];

    // List of sides axes can be on
    this.sides = ["bottom","left","top","right"];

    this.canvasId = id; // Id of <canvas> element
    // Reference to the canvas element
    this.canvas = document.getElementById(id);
    if (this.canvas === null) {
        throw "Chart: Cannot find canvas element with ID: " + id;
    }

    this.ctx = this.canvas.getContext('2d'); // Context we draw on

    // Whether we're just using the x and y axes or whether we're using
    // bottom, top, left and right
    this.usingFourSides = false;

    // Default configuration for legend variables
    this.legendConfigClean = {
        display: true,
        location: "right",
        textFormat: ["1em helvetiker", "black"]
    };


    // Default configuration for chart variables
    this.configClean = {
        title: null,
        titleFont: "1em helvetiker",
        titleColor: "black",
        borderWidth: 1,
        borderColor: "black",
        plotBorderWidth: 1,
        plotBorderColor: "black",
        width: null,
        height: null,
        barSpacer: 15,
        background: "white",
    };


    // Default configuration for axis variables. Unique to each axis.
    this.axisConfigClean = {
        majorGrid: null,
        minorGrid: null,
        ticks: true,
        tickLength: 10,
        tickSide: "outside",
        minorTicks: null,
        minorTickLength: 5,
        scale: "linear",
        location: null,
        showLabels: true,
        barSpacer: 15,
        labelFont: "1em helvetiker",
        labelColor: "black",
        titleFont: "1em helvetiker",
        titleColor: "black",
    };

    this.legendConfig = Fiz.deepCopy(this.legendConfigClean);
    this.config = Fiz.deepCopy(this.configClean);
    this.xAxisConfig = Fiz.deepCopy(this.axisConfigClean);
    this.yAxisConfig = Fiz.deepCopy(this.axisConfigClean);

    this.bottomAxisConfig = Fiz.deepCopy(this.axisConfigClean);
    this.leftAxisConfig = Fiz.deepCopy(this.axisConfigClean);
    this.rightAxisConfig = Fiz.deepCopy(this.axisConfigClean);
    this.topAxisConfig = Fiz.deepCopy(this.axisConfigClean);

    this.plots = []; // List of plot objects

    // Object that keeps track of the size (in pixels) of the area where the
    // plots are drawn. Filled in when we're drawing the chart.
    this.plotArea = {
        height: 0,
        width: 0
    };

    // Used for layout of the chart. For each side of the chart, contains an
    // array of objects on that side (title, legend, ticks, etc.)
    this.sections = {
        top: [],
        right: [],
        bottom: [],
        left: []
    };

    // A list of 4 elements used to store a reference to each of the axes.
    // axes[0] is the bottom axis and the rest go clockwise. If an axis does
    // not exist for the current chart (i.e., there is no plot that uses that
    // axis) it is undefined.
    this.axes = [];
};

/**
 * Removes a plot from the chart. The chart is not redrawn until the draw()
 * method is called.
 *
 * @param plot      (Chart.Plot) Plot object to remove
 */
Fiz.Chart.prototype.removePlot = function (plot) {
    var index = Fiz.arrayIndexOf(this.plots, plot);
    if (index === -1) {
        throw "Chart.removePlot: Plot not found"
    }
    this.plots.splice(index, 1);
};

/**
 * Adds the plot object internally, which will be displayed the next time
 * draw() is called.
 *
 * @param plot      (Chart.Plot) Plot object to addd
 */
Fiz.Chart.prototype.addPlot = function (plot) {
    this.plots.push(plot);
};


/**
 * Setter function for the various chart configuration objects. To the user, it
 * looks like a normal setter function, but internally different options are
 * stored in different objects based on the object's prefix.
 *
 * @param name      (String) Name of configuration property
 * @param value     Value to set in configuration object
 */
Fiz.Chart.prototype.set = function (name, value) { // variable length arguments
    var result = this.findConfigObject(name);
    result[0][result[1]] = value;

    if (Fiz.arrayIndexOf(this.sides, result[0]) !== -1)  {
        this.usingFourSides = true;
    }
};

/*
 * Getter function for the various configuration properties. Like the setter,
 * it must first find the right configuration object based on prefixes.
 */
Fiz.Chart.prototype.get = function (name) {
    var result = this.findConfigObject(name);
    return result[0][result[1]];
};

// Static methods

/**
 * Draws a shape at the given coordinates and with the given radius.
 * Supported shapes are:
 * square, diamond, circle, triangle
 *
 * @ctx        (Context) Context for the canvas element
 * @x          (Int) X position for the center of the shape
 * @y          (Int) Y position for the center of the shape
 * @shape      (String) Name of shape to draw
 * @color      (String) Color of the shape
 * @r          (Int) Radius of the shape. Obviously only circles have a radius,
 *                   but approximations that make sense for the others are used
 */

Fiz.Chart.drawShape = function(ctx, x, y, shape, color, r) {
    ctx.save();
    ctx.globalAlpha = 1;
    ctx.translate(x, y);
    ctx.beginPath();
    ctx.fillStyle = color;

    if (shape === "diamond") {
        ctx.rotate(Math.PI/4);
        shape = "square"; // Same thing, just rotated
    }

    if (shape === "square") {
        ctx.moveTo(-r/2, -r/2);
        ctx.lineTo(r/2, -r/2);
        ctx.lineTo(r/2, r/2);
        ctx.lineTo(-r/2, r/2);
    } else if (shape === "circle") {
        ctx.arc(0, 0, r/1.5, 0, 2 * Math.PI, true);
    } else if (shape === "triangle") {
        var h = Math.round(Math.sqrt(3)/4 * r * 10)/10;
        ctx.moveTo(-r/2, h);
        ctx.lineTo(r/2, h);
        ctx.lineTo(0, -h);
        ctx.lineTo(-r/2, h);

    } else {
        throw "Chart.drawShape: Invalid shape: " + shape;
    }
    ctx.fill();
    ctx.restore();
};

/*
 * Draws a line using this object to describe the border.
 *
 * @param x         (Int) X location to draw to
 * @param y         (Int) Y location to draw to
 */
Fiz.Chart.drawLineTo = function(ctx, border, x, y) {
    ctx.lineWidth = border[0] || 1;
    ctx.strokeStyle = border[1] || "black";
    ctx.lineTo(x, y);
};

/**
 * Draws a rectangle with the given parameters and the border around it.
 *
 * @param ctx         Context object to draw on
 * @param width       (Int) Width of the rectangle
 * @param height      (Int) Height of the rectangle
 * @param border      (Border) Array with border parameters (see documentation)
 * @param background  (String) Background color of the rectangle
 * @param which       (Array) Optional. Describes which sides of the border
 *                    to draw, in the order of top, right, bottom, left
 */
Fiz.Chart.drawRect  = function(ctx, width, height, border, background, which) {

    if (border === undefined) {
        border = [0];
    }

    // draw the rectangle
    if (background !== undefined) {
        ctx.save();
        ctx.fillStyle = background;
        ctx.fillRect(0, 0, width, height);
        ctx.restore();
    }

    var x = 0;
    var y = 0;

    if (border[2] == "inside") {
        x += border[0]/2;
        y += border[0]/2;
        width -= border[0];
        height -= border[0];

    } else if (border[2] == "outside") {
        x -= border[0]/2;
        y -=  border[0]/2;
        width += border[0];
        height += border[0];

    }

    ctx.save();
    // We want the inside of the rectangle to be affected by transperancy, but
    // not the border, so we temporarly set opacity to 100%
    ctx.globalAlpha = 1;
    ctx.beginPath();
    ctx.moveTo(x - border[0]/2, y);

    // We might not need to draw all four borders if, e.g., the edge of a plot
    // overlaps with the outside border. We draw the border in the order of
    // top, right, bottom, left.
    which = which || [true, true, true, true];
    if (which[0]) {
        Fiz.Chart.drawLineTo(ctx, border, x + width, y);
    } else {
        ctx.moveTo(x + width, y);
    }
    if (which[1]) {
        Fiz.Chart.drawLineTo(ctx, border, x + width, y + height);
    } else {
        ctx.moveTo(x + width, y + height);
    }
    if (which[2]) {
        Fiz.Chart.drawLineTo(ctx, border, x, y + height);
    } else {
        ctx.moveTo(x, y + height);
    }
    if (which[3]) {
        Fiz.Chart.drawLineTo(ctx, border, x, y);
    } else {
        ctx.moveTo(x, y);
    }
    ctx.stroke();
    ctx.restore();

};



// Private methods (Not actually private, but the user shouldn't ever call them)

/*
 * Finds the configuration object associated with the property based on its
 * prefix (e.g., legendTitle refers to the title property of the legendConfig
 * object. Also returns the name of the property without the prefix.
 *
 * @param name      Name of configuration property
 * @return          (Array) [configuration object, property name without prefix]
 */
Fiz.Chart.prototype.findConfigObject = function (name) {
    for (var i = 0; i < this.prefixes.length; i++) {
        var prefix = this.prefixes[i];
        if (name.substr(0, prefix.length) === prefix) {
            var conf = (prefix === "") ? "config" : "Config";
            var rest = name.substr(prefix.length);
            rest = rest.slice(0, 1).toLowerCase().concat(rest.slice(1));
            return [this[prefix.concat(conf)], rest];
        }
    }
};

/*
 * Assigns colors to series that do not have one. It checks the color of all
 * series to avoid duplicate colors. This function uses the @code{colors}
 * instance variable for its color list.
 */
Fiz.Chart.prototype.setColors = function () {
    var series = this.getVisibleSeries();
    var colors = []; // list of colors already assigned
    var i, j;

    // gets list of colors in use
    for (i = 0; i < series.length; i++) {
        if (series[i].config.color !== undefined) {
            colors.push(series[i].config.color);
        }
    }

    // For each series without a color, we loop through our color list and find
    // the first color not used by another series. After assigning a color to
    // series, we move that color to the end of our color list. The reasoning
    // for this is if a plot is removed and another one added, we want to have
    // a new color for the new plot.
    for (i = 0; i < series.length; i++) {
        if (series[i].config.color === undefined) {
            for (j = 0; j < this.colors.length; j++) {
                if (Fiz.arrayIndexOf(colors, this.colors[j]) === -1) {
                    series[i].config.color = this.colors[j];
                    colors.push(this.colors[j]);
                    // move to end of list
                    this.colors.concat(this.colors.splice(j, 1));
                    break;
                }
            }
        }
    }
};

/*
 * Draws the border that's on the outside of the chart. To simplify the rest of
 * the code, after drawing the border, the code modifies the @{canvasWidth}
 * and @{canvasHeight} variables to pretend the chart is limited to the area
 * between the chart borders.
 */
Fiz.Chart.prototype.drawBorder = function () {
    Fiz.Chart.drawRect(this.ctx, this.canvasWidth, this.canvasHeight,
                       [this.config.borderWidth, this.config.borderColor, "inside"],
                       this.config.background);

    this.config.borderWidth = parseInt(this.config.borderWidth, 10);
    this.config.borderHeight = parseInt(this.config.borderWidth, 10);
    this.canvasWidth -= this.config.borderWidth * 2;
    this.canvasHeight -= this.config.borderWidth * 2;
    this.ctx.translate(this.config.borderWidth, this.config.borderWidth);

};

/*
 * Draws the border outside of the plot area.
 */
Fiz.Chart.prototype.drawPlotBorder = function () {
    this.ctx.save();
    this.ctx.translate(this.sections.left.size, this.sections.top.size);
    Fiz.Chart.drawRect(this.ctx, this.plotArea.width, this.plotArea.height,
                       [parseInt(this.config.plotBorderWidth, 10),
                        this.config.plotBorderColor, "outside"],
                       this.config.plotBackground);
    this.ctx.restore();
};

/**
 * Instanstantiates all sections to be drawn so that they can be rendered later.
 * The chart consists of four sides and the inner plot area. Each side has a
 * few different objects, depending on which axes exist and configuration
 * options set. See the documentation for renderSections() for a drawing of
 * the different parts of the chart. At the end, the function calls
 * calculateChartSize() to check how much space the different sections require.
 */
Fiz.Chart.prototype.registerLayout = function () {
    // Each variable contains a list of sections for that size as well as
    // a @code{size} variable which is the total size required to draw that
    // side.
    var sections = this.sections = {
        top: [],
        right: [],
        bottom: [],
        left: []
    };

    // Top of the chart
    sections.top.push(new Fiz.Chart.Title(
        this.ctx, this.config.title, this.config.titleFont, this.config.titleColor));
    if (this.axes[2]) {
        sections.top.push(new Fiz.Chart.Title(
            this.ctx, this.axes[2].title, this.axes[2].config.titleFont,
                                           this.axes[2].config.titleColor));
        sections.top.push(new Fiz.Chart.Ticks(
            this.ctx, this.axes[2]));
    }

    // Left of chart
    if (this.axes[1]) {
        sections.left.push(new Fiz.Chart.Title(
            this.ctx, this.axes[1].config.title, this.axes[1].config.titleFont,
                                                  this.axes[1].config.titleColor));
        sections.left.push(new Fiz.Chart.Ticks(
            this.ctx, this.axes[1]));
    }

    // Bottom of chart
    if (this.axes[0]) {
        sections.bottom.push(new Fiz.Chart.Title(
            this.ctx, this.axes[0].config.title, this.axes[0].config.titleFont,
                                                 this.axes[0].config.titleColor));
        sections.bottom.push(new Fiz.Chart.Ticks(
            this.ctx, this.axes[0]));
    }

    // Right of chart
    sections.right.push(new Fiz.Chart.Legend(
        this.ctx, this.legendConfig, this.getVisibleSeries()));
    if (this.axes[3]) {
        sections.right.push(new Fiz.Chart.Title(
            this.ctx, this.axes[3].config.title, this.axes[3].config.titleFont,
                                          this.axes[3].config.titleColor));
        sections.right.push(new Fiz.Chart.Ticks(
            this.ctx, this.axes[3]));
    }

    this.calculateChartSize();
};

/**
 * Calls each section's sizeRequired() method to figure out how much space will
 * be left over to draw the plots in the center of the chart. It updates the
 * variables @code{this.plotArea.width} and @code{this.plotArea.height}
 * accordingly.
 */
Fiz.Chart.prototype.calculateChartSize = function () {
    for (var i in this.sections) {
        if (!this.sections.hasOwnProperty(i)) {
            continue;
        }

        var side = this.sections[i];
        var size = 0;
        for (var j = 0; j < side.length; j++) {
            size += side[j].sizeRequired();
        }

        // If the size is 0, it looks ugly, so make it 5% of the chart size
        // instead.
        if (i === "top" || i === "bottom") {
            side.size = size || this.canvasHeight * 0.05;
        } else {
            side.size = size || this.canvasWidth * 0.05;
        }
    }
    this.plotArea.width = this.canvasWidth - this.sections.left.size -
        this.sections.right.size - this.config.plotBorderWidth * 2;
    this.plotArea.height = this.canvasHeight - this.sections.top.size -
        this.sections.bottom.size - this.config.plotBorderWidth * 2;
};

/**
 * Calls each section's draw() method after translating the canvas to the top
 * left corner of the section's bounding box.
 *
 * A chart could have all of these sections, though usually there are less.
 * ------------------------
 * |    |Chart Title|     |
 * |    ------------|     |
 * |    |   Title   |     |
 * |    ------------|     |
 * |    |  Labels   |     |
 * |----------------|-----|
 * |T|L|            |L|T|L|
 * |i|a|            |a|i|e|
 * |t|b|            |b|t|g|
 * |l|e|            |e|l|e|
 * |e|l|            |l|e|n|
 * | |s|            |s| |d|
 * |----------------|-----|
 * |   |   Labels   |     |
 * |   --------------     |
 * |   |   Title    |     |
 * ------------------------
 */
Fiz.Chart.prototype.renderSections = function () {
    for (var i in this.sections) {
        if (!this.sections.hasOwnProperty(i)) {
            continue;
        }

        var side = this.sections[i];
        var sizeUsedSoFar = 0;
        for (var j = 0; j < side.length; j++) {
            var box = {
                side: i,
                height: side[j].sizeRequired(),
                width: 0
            };

            this.ctx.save();
            if (i === "top") {
                this.ctx.translate(this.sections.left.size, sizeUsedSoFar);
                box.width = this.plotArea.width;
            } else if (i === "left") {
                this.ctx.translate(sizeUsedSoFar, this.plotArea.height +
                                   this.sections.top.size);
                box.width = this.plotArea.height;
            } else if (i === "bottom") {
                this.ctx.translate(this.sections.left.size,
                                   this.canvasHeight - sizeUsedSoFar);
                box.width = this.plotArea.width;
            } else if (i === "right") {
                this.ctx.translate(this.canvasWidth - sizeUsedSoFar -
                                   side[j].sizeRequired(),
                                   this.canvasHeight - this.sections.bottom.size);
                box.width = this.plotArea.height;
            }


            side[j].render(box);
            sizeUsedSoFar += side[j].sizeRequired();
            this.ctx.restore();
        }
    }
};


/*
 * Creates the @code{axes} instance variables and populates them based on
 * configuration and plot data. These objects are implemented in Axis.js.
 */
Fiz.Chart.prototype.resolveAxes = function () {
    this.axes = [];
    var visPlots = this.getVisiblePlots();

    for (var i = 0; i < visPlots.length; i++) {
        var plot = visPlots[i];
        if (plot.config.xLocation !== "bottom" ||
           plot.config.yLocation !== "left") {
            this.usingFourSides = true;
            break;
        }
    }

    for (var i = 0; i < visPlots.length; i++) {
        var plot = visPlots[i];
        this.createAxis(plot.config.xLocation, null, "x", plot);
        this.createAxis(plot.config.yLocation, plot.config.xLocation, "y", plot);
    }
};

/**
 * Creates an axis object for each axis used the plot, and sets the approriate
 * variables in the @code{this.axes} variable.
 */
Fiz.Chart.prototype.createAxis = function (sideName, oSideName, type, plot) {
    var side = Fiz.arrayIndexOf(["bottom", "left", "top", "right"], sideName);
    var oSide = Fiz.arrayIndexOf(["bottom", "left", "top", "right"], oSideName);

    if (this.axes[side] === undefined) {
        var config;
        if (this.usingFourSides) {
            config = this[sideName + "AxisConfig"];
        } else {
            config = type == "x" ? this.xAxisConfig : this.yAxisConfig;
        }
        this.axes[side] = new Fiz.Chart.Axis(config, side, oSide, type);
    }

    this.axes[side].addPlot(plot);

    if (type == "x") {
        plot.xAxis = this.axes[side];
    }

    if (type == "y") {
        plot.yAxis = this.axes[side];
    }
};

/*
 * Returns an array of series which are visible from all plots in the chart.
 *
 * @return     (Array<Chart.Series>) An array of series objects
 */
Fiz.Chart.prototype.getVisibleSeries = function () {
    var series = [];
    var visPlots = this.getVisiblePlots();
    for (var i = 0; i < visPlots.length; i++) {
        series = series.concat(visPlots[i].series);
    }

    return series;
};


/*
 * Returns an array of all visibile plots
 *
 * @return     (Array<Chart.Plot>) An array of plot objects
 */
Fiz.Chart.prototype.getVisiblePlots = function () {
    var plots = [];
    for (var i = 0; i < this.plots.length; i++) {
        if (this.plots[i].config.display !== false) {
            plots.push(this.plots[i]);
        }
    }

    return plots;
};

/**
 * Determines the size of the canvas we are drawning on. Normally, the size
 * is determined by the attributes of the canvas element. The user can,
 * however, override the size by setting the @code{width} and @code{height}
 * variables in the configuration object. Sets the variables
 * @code{canvasWidth} and @code{canvasHeight}.
 */
Fiz.Chart.prototype.resolveSizes = function() {

    if (this.config.width === null) {
        this.canvasWidth = parseInt(this.canvas.getAttribute("width"), 10);
    }  else {
        this.canvas.setAttribute("width", this.config.width);
        this.canvasWidth = this.config.width;
    }

    if (this.config.height === null) {
        this.canvasHeight = parseInt(this.canvas.getAttribute("height"), 10);
    } else {
        this.canvas.setAttribute("height", this.config.height);
        this.canvasHeight = this.config.height;
    }
}

/*
 * Top level function to draw a chart
 */
Fiz.Chart.prototype.draw = function () {

    this.resolveSizes();
    this.ctx.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
    this.drawBorder();
    this.ctx.font = "1em 'helvetiker'";
    this.resolveAxes();
    this.registerLayout();
    this.drawPlotBorder();
    this.setColors();
    this.fixAxisSizes();
    this.renderSections();
    this.drawPlots();

};

/*
 * Updates the axes'size and pSize variables. They cannot be set during
 * initalization because they are not known at that time.
 */
Fiz.Chart.prototype.fixAxisSizes = function () {
    for (var i = 0; i < 4; i++) {
        if (this.axes[i]) {
            if (i === 0 || i === 2) {
                this.axes[i].size = this.plotArea.width;
                this.axes[i].pSize = this.plotArea.height;
            } else {
                this.axes[i].pSize = this.plotArea.width;
                this.axes[i].size = this.plotArea.height;
            }
        }
    }
};

/*
 * Draws the visible plots for the chart. This function is responsible for
 * calling the draw() method of each plot.
 */
Fiz.Chart.prototype.drawPlots = function () {

    this.ctx.save();

    this.ctx.translate(this.sections.left.size, this.sections.top.size);


    for (var i = 0; i < this.axes.length; i++) {
        if (!this.axes[i] || this.axes[i].isXAxis() === false) {
            continue;
        }
        this.rotateCanvasForPlots(i);
        var list = this.sortPlotsByType(this.axes[i].plots);
        for (var j = 0; j < list.length; j++)  {
            list[j][0].drawPlots(list[j], this.ctx, this.config);
        }

    }

    this.ctx.restore();
};

/*
 * Given a list of plots, returns a two dimensional array so that plots of
 * different types are grouped together.
 *
 * @param plots     Array<Chart.Plot> List of plots to group by type
 * @return          Array<Array<Chart.Plot>> A two dimensional array of plots
 *                  that are seperated by type
 */
Fiz.Chart.prototype.sortPlotsByType = function (plots) {
    var list = [];
    for (var i = 0; i < plots.length; i++) {
        var type = plots[i].type;
        for (var j = 0; j < list.length; j++) {
            if (list[j][0].type === plots[i].type) {
                list[j].push(plots[i]);
            }
        }
        if (j === list.length) {
            list.push([]);
            list[j][0] = plots[i];
        }
    }
    return list;
}

/*
 * Rotates and scales the canvas based on which side we drawing on. To simplify
 * the work of plot objects, we can pretend the x axis is on the bottom. This
 * method does not save the canvas state.
 *
 * @param side      (Int) Which side we are trying to use as the x axis
 */
Fiz.Chart.prototype.rotateCanvasForPlots = function (side) {
    // Don't need to do anything if side is 0
    this.ctx.transform(1, 0, 0, -1, 0, this.plotArea.height);

    if (side === 1) {
        this.ctx.translate(this.plotArea.width, 0);
        this.ctx.rotate(Math.PI/2);
        this.ctx.transform(1, 0, 0, -1, 0, this.plotArea.width); // mirroring matrix
    } else if (side === 2) {
        this.ctx.rotate(Math.PI);
        this.ctx.transform(-1, 0, 0, 1, 0, -this.plotArea.height); // mirroring matrix
    } else if (side === 3) {
        this.ctx.translate(0, this.plotArea.height);
        this.ctx.rotate(-Math.PI/2);
        this.ctx.transform(1, 0, 0, -1, 0, 0); // mirroring matrix
        this.ctx.transform(-1, 0, 0, 1, this.plotArea.height, -this.plotArea.width); // mirroring matrix
    }
};
