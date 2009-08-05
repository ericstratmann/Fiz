include("static/fiz/Fiz.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartFormat.js");
include("CanvasFixture.js");

FormatTest = {};

FormatTest.setUp = function () {
	this.canvas = new Canvas();
	this.ctx = this.canvas.getContext("2d");
	this.format = new Fiz.Chart.Format(this.ctx, ["1em arial", "green"]);
};

FormatTest.test_width = function () {
	this.format.width("foo");
	assertEqual("measureText(foo)\n", jsunit.log);
};

FormatTest.test_height = function () {
	this.format.height("foo");
	assertEqual("measureText(M\n", jsunit.log);
};

FormatTest.test_draw = function() {
	this.format.draw("foo");
	assertEqual("font = 1em arial\nfillStyle = green\nfillText(foo, 0, 0)\nfont = undefined\n", jsunit.log);
};
j
