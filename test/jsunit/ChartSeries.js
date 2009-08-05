// ChartSeriesTest.js --
//
// Jsunit tests for ChartSeriesTest.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/Chart.js");
include("static/fiz/ChartSeries.js");

ChartSeriesTest = {};

ChartSeriesTest.test_set = function () {
	var series = new Fiz.Chart.Series();
	series.set("foo", "bar");
	assertEqual("bar", series.config.foo);
};

SeriesTest.test_get = function () {
	series.config.bar = "foo";
	assertEqual("foo", seriess.get("bar"));
};
