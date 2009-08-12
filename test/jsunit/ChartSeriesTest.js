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
    assertEqual("bar", series.config.foo, "foo-bar");
    
    series.set("baz", "foo");
    assertEqual("foo", series.config.baz, "baz-foo");
};

ChartSeriesTest.test_get = function () {
    var series = new Fiz.Chart.Series();
    series.config.bar = "foo";
    assertEqual("foo", series.get("bar"), "bar-fop");
    
    series.config.foo = "baz";
    assertEqual("baz", series.get("foo"), "foo-baz");
};
