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
 * The Canvas object is meant to emulate the canvas DOM element for unit
 * testing. Instead of actually drawning to a canvas, it logs the method called
 * and the arguments to it. In the few cases that return value is expected, a
 * bogus value is returned.
 */

function Canvas() {};

Canvas.prototype.getContext = function () {
    return new Context();
}

Canvas.prototype.getAttribute = function () {
    return 500;
}

function Context() {this.constructor = "context"};

function chartLogFunction(name) {
    return function() {
        jsunit.log += name + "(";
        for (var i = 0; i < arguments.length; i++) {
            if (arguments[i] && arguments[i].constructor === "context") {
                jsunit.log += "ctx";
            } else {
                if (arguments[i] && typeof arguments[i] === "object") {
                    if (arguments[i].constructor === Array) {
                        jsunit.log += arguments[i];
                    } else {
                        jsunit.log += printObject(arguments[i]);
                    }
                } else {
                    jsunit.log += arguments[i];
                }
            }
            if (i != arguments.length - 1) {
                jsunit.log += ", ";
            }
        }
        jsunit.log += ")\n";
    }
}

Context.prototype.arc = chartLogFunction("arc");
Context.prototype.beginPath = chartLogFunction("beginPath");
Context.prototype.clearRect = chartLogFunction("clearRect");
Context.prototype.closePath = chartLogFunction("closePath");
Context.prototype.createLinearGradient = chartLogFunction("createLinearGradient");
Context.prototype.fill = chartLogFunction("fill");
Context.prototype.fillRect = chartLogFunction("fillRect");
Context.prototype.lineTo = chartLogFunction("lineTo");
Context.prototype.moveTo = chartLogFunction("moveTo");
Context.prototype.restore = chartLogFunction("restore");
Context.prototype.rotate = chartLogFunction("rotate");
Context.prototype.save = chartLogFunction("save");
Context.prototype.stroke = chartLogFunction("stroke");
Context.prototype.strokeRect = chartLogFunction("strokeRect");
Context.prototype.translate = chartLogFunction("translate");
Context.prototype.transform = chartLogFunction("transform");
Context.prototype.fillText = chartLogFunction("fillText");
Context.prototype.measureText = function (string) {
    chartLogFunction("measureText")(string);
    return {width: string.length * 2};
};
/*

function logSetter(name) {
    return function (val) {
        this[name] = val;
        jsunit.log += name + " = " + val + "\n";
    };
}
*/

function addSetter(name) {
    Context.prototype.__defineSetter__(name, function(val) {
        //this[name] = val;
        jsunit.log += name + " = " + val + "\n";
    });
};

addSetter("fillStyle");
addSetter("globalAlpha");
addSetter("strokeStyle");
addSetter("lineWidth");
addSetter("font");
addSetter("lineCap");
