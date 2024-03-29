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
 * A Ticks represents the ticks on one axis, including their labels and
 * is used to render them. It also draws the grid lines on the chart.
 */
Fiz.Chart.Ticks = function (ctx, axis) {
    this.ctx = ctx;
    this.axis = axis;
};

/*
 * Draws the tick marks, labels, and grid lines.
 */
Fiz.Chart.Ticks.prototype.render = function (box) {
    if (this.axis.discrete && this.axis.isXAxis()) {
        this.drawDiscreteAxis(box);
    } else {
        this.drawContinuousAxis(box);
    }
}

/*
 * Draws a discrete axis. In a discrete axis, the labels are used to determine
 * where to draw the grid lines. They should be in between two labels. The
 * labels ought to be underneath a bar (or whatever we are drawing).
 *
 * @box       Object describing the containing box, including width and height
 */
Fiz.Chart.Ticks.prototype.drawDiscreteAxis = function (box) {
    this.ctx.save();
    this.moveAndOrient(box);
//    this.ctx.strokeRect(0, 0, box.height, -box.width);
    this.ctx.translate(box.height, 0);
    this.drawDiscreteLabels();
    this.drawDiscreteGridLines();
    this.ctx.restore();
}

/*
 * Draws the labels for a discrete axis.
 */
Fiz.Chart.Ticks.prototype.drawDiscreteLabels = function () {
    for (var i = 0; i < this.axis.labels.length; i++) {
        this.ctx.save();
        // 0.5 is to place in the label in the middle
        var y = (i + 0.5) * (this.axis.size-this.axis.config.barSpacer)/
            this.axis.labels.length +
            this.axis.config.barSpacer/2;

        this.ctx.translate(0, -y);
        this.drawLabel(this.axis.labels[i]);
        this.ctx.restore();

    }
};

/*
 * Draws the grid lines for a discrete axis
 */
Fiz.Chart.Ticks.prototype.drawDiscreteGridLines = function () {
    for (var i = 1; i < this.axis.labels.length; i++) {
        this.ctx.save();
        var wEach = (this.axis.size - this.axis.config.barSpacer) /
            this.axis.labels.length;
        var y = i * wEach + this.axis.config.barSpacer/2;

        this.ctx.translate(0, -y);
        this.drawMajorGridLine();
        this.ctx.restore();
    }
};

/*
 * Draws a continuous axis. A continuous axis uses the tick mark positions to
 * decide where to draw the ticks, labels and grid lines. The label should be
 * right next to the tick mark.
 */
Fiz.Chart.Ticks.prototype.drawContinuousAxis = function (box) {
    this.ctx.save();
    this.moveAndOrient(box);
//    this.ctx.strokeRect(0, 0, box.height, -box.width);
    this.ctx.translate(box.height, 0);

    for (var i = 0; i < this.axis.labels.length; i++) {
        var tick = this.axis.labels[i];
        var pos = this.axis.logicalToChartCoords(tick);
        this.ctx.save();
        this.ctx.translate(0, -pos);


        // Don't draw if it's near the bottom or top, since they're part of
        // the border
        if (i > 0 && Math.abs(this.axis.size - pos) > 1) {
            this.drawMajorGridLine();
        }

        this.drawMinorTicks(pos);
        this.drawTickMark(tick);
        var format = new Fiz.Chart.Format(this.ctx, [this.axis.config.labelFont,
                                                     this.axis.config.labelColor]);
//      format.render("tick");
        this.drawLabel(tick);
        this.ctx.restore();
    }

    this.ctx.restore();
};


/*
 * Returns the width in pixels required to draw the tick marks and labels.
 * If the tick marks are inside the chart, they do not add to the total.
 *
 * @return          Width in pixels required
 */
Fiz.Chart.Ticks.prototype.sizeRequired = function () {
    this.size = 0;

    if (!this.axis.discrete || this.axis.type === "y") {
        if (this.axis.config.tickSide === "outside") {
            this.size += this.axis.config.tickLength * 1;
        } else if (this.axis.config.tickSide === "middle") {
            this.size += this.axis.config.tickLength * 0.5;
        }
    }

    if (this.axis.config.displayLabels === false ||
        this.axis.config.displayLabels === "false") {
        return this.size * 1.5;
    }

    var labels = this.axis.labels;
    var size;
    var format = new Fiz.Chart.Format(this.ctx, this.axis.config.labelFont,
                                                 this.axis.config.labelColor);

    var max = this.maxSizeLabel();
    if (this.axis.side % 2 === 0) {
        this.size += max * 2;
    } else {
        this.size += max + format.height("x") * 2;
    }
    return this.size;
};

/*
 * Returns the largest size in pixels required to draw all of the labels.
 *
 * @return          Largest size in pixels required to draw the labels
 */
Fiz.Chart.Ticks.prototype.maxSizeLabel = function () {
    var max = 0;
    var labels = this.axis.labels;
    var format = new Fiz.Chart.Format(this.ctx, this.axis.config.labelFont,
                                                 this.axis.config.labelColor);

    if (this.axis.side % 2 === 0) {
        return format.height("x");
    }

    for (var i = 0; i < labels.length; i++) {
        size = format.width(labels[i]);

        max = Math.max(max, size);
    }

    return max;
};

/*
 * Draws one tick mark and the associated label. This function expects the
 * origin to be the start of the tick mark.

 * @param axis      Axis object
 * @param tick      Tick label
 */
Fiz.Chart.Ticks.prototype.drawTickMark = function (tick) {
    var pos = this.axis.logicalToChartCoords(tick);

    if (pos !== 0 || this.axis.config.tickSide === "outside") {
        this.ctx.save();
        this.ctx.beginPath();
        this.ctx.moveTo(0, 0); // nop, but doesn't work without this for some reason
        var len = this.axis.config.tickLength;
        if (this.axis.config.tickSide === "outside") {
            len *= -1;
        } else if (this.axis.config.tickSide === "middle") {
            len *= -0.5;
        }
        this.ctx.lineTo(len, 0);
        this.ctx.stroke();
        this.ctx.restore();
    }
};

/*
 * Draws a grid line across the chart. This function expects the origin to be
 * at the start of the grid line on the left side.
 *
 * @param axis      Axis object
 */
Fiz.Chart.Ticks.prototype.drawMajorGridLine = function (axis) {
    if (this.axis.config.majorGridWidth == 0) {
        return;
    }


    this.ctx.save();
    this.ctx.beginPath();
    this.ctx.moveTo(0, 0);
    this.ctx.lineCap = "round";
    this.ctx.translate(2, 0);
    Fiz.Chart.drawLineTo(this.ctx, [this.axis.config.majorGridWidth,
                                    this.axis.config.majorGridColor],
                         this.axis.pSize, 0);
    this.ctx.closePath();
    this.ctx.stroke();
    this.ctx.restore();
};

/*
 * Draws the tick marks between two major tick marks. This function expects
 * the origin to be at the start of major tick mark below the ones we are
 *
 * @param pos       (Number) The number of pixels we are above the bottom of
 *                  the chart. Needed to stop drawing ticks marks at the end.
 */
Fiz.Chart.Ticks.prototype.drawMinorTicks = function (pos) {
    var minorTicks = this.axis.config.minorTicks;
    if (minorTicks) {
        var tickLen = this.axis.size * (this.axis.labels[1] - this.axis.labels[0]) /
            (this.axis.max - this.axis.min);
        var tickSpacing = tickLen/minorTicks;
        for (var j = 1; j < minorTicks; j += 1) {
            if (pos + tickSpacing * j > this.axis.size) {
                break;
            }
            this.ctx.save();
            this.ctx.beginPath();
            this.ctx.moveTo(0, -tickSpacing * j);
            var len = this.axis.config.minorTickLength;
            if (this.axis.config.tickSide === "outside") {
                len *= -1;
            }
            this.ctx.lineTo(len, -tickSpacing * j);
            this.ctx.stroke();
            this.ctx.restore();
        }
    }
};

/*
 * Rotates and translates the chart as necessary so that the rest of the code
 * can assume we are drawing onto the left axis. The one exception to this is
 * drawing the labels, which will need to do further transformation (or else
 * the text will be rotated or flipped).
 *
 */
Fiz.Chart.Ticks.prototype.moveAndOrient = function (box) {
    var side = box.side;
    if (side === "top") {
        this.ctx.rotate(Math.PI/2);
    }

    if (side === "left") {
    }

    if (side === "bottom") {
        this.ctx.rotate(-Math.PI/2);
        this.ctx.transform(1, 0, 0, -1, 0, 0);
    }

    if (side === "right") {
        this.ctx.rotate(Math.PI);
        this.ctx.translate(-box.height, 0);
        this.ctx.transform(1, 0, 0, -1, 0, 0);
    }

    if (this.axis.oSide === 3 || this.axis.oSide === 2) {
        this.ctx.transform(1, 0, 0, -1, 0, -this.axis.size);
    }
};



/*
 * Draws a label on the chart. Since the chart may be currently rotated, we
 * might need to rotate it in order for the text to be display right side up.
 *
 * @param label     (Text or Number) Label to draw
 */
Fiz.Chart.Ticks.prototype.drawLabel = function (label) {
    if (this.axis.config.displayLabels === false || this.axis.config.displayLabels === "false") {
        return;
    }
    var x, y;


    var format = new Fiz.Chart.Format(this.ctx, this.axis.config.labelFont,
                                                 this.axis.config.labelColor);
    this.translateForTicks();
    this.translateForSide(format, label);
    this.ctx.save();

    this.undoRotation(format, label);

//  this.ctx.translate(x, 0);
    format.draw(label);
//  this.ctx.fillText(label + "", x, y);
    this.ctx.restore();
};

/**
 * Depending on the location of major ticks, we'll need to translate our
 * location in order to not overwrite the ticks. For "outside" ticks,
 * we translate by the length of the tick mark. For "middle" ticks, we translate
 * by half of its length. If the ticks are "inside", no action is necessary.
 */
Fiz.Chart.Ticks.prototype.translateForTicks = function () {
    if (this.axis.config.tickSide === "outside") {
        this.ctx.translate(-this.axis.config.tickLength, 0);
    } else if (this.axis.config.tickSide === "middle") {
        this.ctx.translate(-this.axis.config.tickLength/2, 0);
    }
};

/*
 * Depending on what side we are drawing a label, translate our position so that
 * we draw the label in the right position.
 *
 * @param format    (Chart.Format) Format we are using to draw the label
 * @param label     (Chart.Label) Label we are rendering
 */
Fiz.Chart.Ticks.prototype.translateForSide = function (format, label) {
    var mul = 1;
    if (this.axis.oSide === 2 || this.axis.oSide === 3) {
        mul = -1;
    }

    if (this.axis.side === 0) {
        this.ctx.translate(-format.height("x"), mul * format.width(label)/2);
    } else if (this.axis.side === 1) {
        this.ctx.translate(-this.maxSizeLabel() - format.width("x"),
                           mul * format.height("x")/2);
    } else if (this.axis.side === 2) {
        this.ctx.translate(-0.5 * format.height("x"), mul * format.width(label)/2);
    } else if (this.axis.side === 3) {
        this.ctx.translate(-format.width("x"), mul * format.height("x")/2);
    }

    if (this.axis.oSide === 3) {
            this.ctx.transform(1, 0, 0, -1, 0, 0); // mirroring matrix
    }
};

/**
 * Undo any rotations or translations made in the past so that we can draw a
 * label. This is necessary so that labels we draw do not appear mirrored or
 * flipped.
 *
 * @param format    (Chart.Format) Format we are using to draw our label
 */
Fiz.Chart.Ticks.prototype.undoRotation = function (format, label) {
    var side = this.axis.side;

    if (side === 0) {
        this.ctx.transform(1, 0, 0, -1, 0, 0); // mirroring matrix
        this.ctx.rotate(Math.PI/2);
    } else if (side === 1) {
    } else if (side === 2) {
        this.ctx.rotate(-Math.PI/2);

    } else if (side === 3) {
       // this.ctx.transform(1, 0, 0, -1, 0, 0); // mirroring matrix
       this.ctx.transform(-1, 0, 0, 1, 0, 0); // mirroring matrix
    }

    if (this.axis.oSide === 2) {
       this.ctx.transform(1, 0, 0, -1, 0, 0); // mirroring matrix
    }
};
