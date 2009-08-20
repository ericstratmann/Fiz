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

package org.fiz;


import java.util.*;

/**
 * A ChartSection generates a chart containing one or more plots, such as bar
 * line, and scatter plots. Each plot can have several series resulting in
 * "stacked" plots, such as a stacked bar plot. Data for the chart can be
 * automatically retrieved from a data request.
 *
 * A ChartSection supports the following properties:
 * plot:        (Optional) One or more child datasets, each describing one plot
 * xAxis:       (Optional) Dataset describing the properties for the X axis
 * yAxis:       (Optional) Dataset describing the properties for the Y axis
 * bottomAxis:  (Optional) Dataset describing the properties for the bottom axis
 * leftAxis:    (Optional) Dataset describing the properties for the left axis
 * topAxis:     (Optional) Dataset describing the properties for the top axis
 * rightAxis:   (Optional) Dataset describing the properties for the right axis
 * legend:      (Optional) Dataset describing the properties for the legend
 * id:          (Optional) Used to name the Javascript object associated with
 *              the chart. If not given, it is automatically generated.
 *
 * If all plots use the bottom axis as the X axis and the left one as the
 * Y axis, the axis-specific properties should only be set on the xAxis and
 * yAxis datasets. Otherwise, the other four axis datasets should be used.
 *
 * The following properties are supported by datasets representing a plot:
 * type:       (Optional) Type of plot to draw. Currently supports {@code bar}
 *             {@code line}, and @code{scatter}. Defaults to {@code bar}
 * series:     (Optional) One oe more child datasets, each describing one series
 * id:         (Optional) Used to name the Javascript object associated with
 *             this plot. If not given, it is automatically generated.
 *
 * The following properties are supported by datasets representing a series:
 * request:    (Required) Name of a DataRequest whose result will
 *             supply data for the series.  The request is created
 *             by the caller and registered in the ClientRequest by
 *             calling ClientRequest.addDataRequest.  The response
 *             to this request must contain one {@code record} child
 *             for each data point.
 * xId:        (Required) Name of the column in the DataRequest to use for the
 *             X values for the plot or series
 * yId:        (Required) Name of the column in the DataRequest to use for the
 *             Y values for the plot or series
 * id:         (Optional) Used to name the Javascript object associated with
 *             this series. If not given, it is automatically generated.
 *
 * In cases where there is only one series for a plot or one plot for the chart,
 * it is possible to "collapse" the datasets. This is done by not including the

 * {@code plot} or {@code series} parameter, and instead placing the parameters
 * that would have been in that dataset in the chart or plot dataset.
 *
 * Before describing the different configuration options, here are a few
 * parameters they have in common.
 *
 * color:    When a color is needed, you can specify any CSS compliant color,
 *           such as red or #fff
 * side:     One of inside, outside, or middle. For example, if you're drawing
 *           tick marks, you have the option of drawing them outside the axis,
 *           inside of it, or half and half.
 * font:     A CSS font including size, such as "1em arial"
 * shape:    One of square, diamond, circle, or triangle
 *
 * What follows is a complete descripiction of every configuration property.
 *
 * Chart wide properties:
 *
 * title:            Title displayed at the top of the chart
 * titleFont:        Font used to render the title
 * titleColor:       Color used to draw the title
 * borderWidth:      Width of the border surrounding the chart in pixels. If 0,
 *                   the border is not drawn.
 * borderColor:      Color of the corder surrounding the chart
 * plotBorderWidth:  Width of the border surrounding the inner plot area in
 *                   pixels. If 0, the border is not drawn.
 * plotBorderColor:  Color of the border surrounding the inner plot area
 * width:            Width of the chart in pixels
 * height:           Height of the chart in pixels
 * background:       Color for the background
 *
 * Axis-specific properties.
 *
 * These properties should be specified in the dataset for the particular axis.
 *
 * title:            Title to display next to the axis
 * titleFont:        Font used to render the axis' title
 * titleColor:       Color used to draw the axis' title
 * majorGridWidth:   Width of the major grid lines, which are drawn at every
 *                   major tick mark. If 0, they are not drawn.
 * majorGridColor:   Color of the major grid lines
 * minorGridWidth:   Width  of the minor grid lines, which are drawn at every
 *                   minor tick mark. If 0, they are not drawn.
 * minorGridColor:   Color of the minor grid lines
 * displayTicks:     "true" or "false". Whether to display tick marks.
 * tickLength:       Length of a major tick in pixels
 * tickSide:         Which side the tick marks should be drawn on. "inside", "outside",
 *                   or "middle"
 * minorTicks:       Number of minor tick marks between major tick marks. Defaults to 5
 * minorTickLength:  Length of a minor tick in pixels
 * minorTickSide:    Which side the tick marks should be drawn on. "inside", "outside",
 *                   or "middle"
 * scale:            What kind of scale to use for the axis, one of "linear" or "log"
 * logBase:          If using a log scale, which base to use
 * showLabels:       "true" or "false". Whether to draw the labels at each major tick mark
 * labelFont:        Font used to render the labels on the axis
 * labelColor:       Color used to draw the labels
 *
 * Legend-specific properties.
 *
 * These properties should be specified in the dataset for the legend.
 *
 * display:     "true" or "false". Whether to display the legend
 * textFont:    Font used to render text in the legend
 * textColor:   Color used to draw the text in the legend
 *
 * The following properties pertain to both plots and series (unless stated
 * otherwise). If a property is set on a plot, it applies to all series it
 * contains (unless overridden). If it is set on a series, it applies to just
 * the one. For example, you might want to set the opacity for all the series,
 * but set the name of each one individually.
 *
 * name:          Name of the plot or series used to label it in the legend
 * nameFont:      Font used to render the name in the legend
 * opacity:       Float between 0 and 1 describing how opaque to draw the plot
 *                or series. 0 is transparent and 1 is opaque.
 * color:         Color of the plot or series
 * showInLegend:  "true" or "false". Whether to display in the legend
 * display:       "true" or "false". Whether to draw the plot or series
 * xLocation:     If not all plots are using the regular bottom X axis and left
 *                Y Axis, can be set to one of "top", "right", "bottom", or
 *                "left" to indicate where this plot's X axis is. This property
 *                does not apply to series
 * yLocation:     If not all plots are using the regular bottom X axis and left
 *                Y Axis, can be set to one of "top", "right", "bottom", or
 *                "left" to indicate where this plot's Y axis is. This property
 *                does not apply to series
 *
 * Some of the types of plots have properties which are unique to that type
 * of plot (e.g., shape used to represent points in a scatter plot). For the
 * plots supported natively in Fiz, here are the properties for specific plots.
 *
 * Bar chart:
 *
 * borderWidth:  Width of each bar's border in pixels
 * borderColor:  Color of the border
 *
 * Line Chart:
 *
 * area:    "true" or "false". Whether to draw the area under a plot or not.
 * shape:   Which type of shape to draw at every point
 * width:   Width of the line in pixels
 *
 * Scatter Chart:
 *
 * shape:   Which type of shape to draw for the points
 */

public class ChartSection extends Section {
    protected Dataset properties;

    // Will contain the string "Fiz.ids.@1", where @1 is the id of the chart.
    // Since it used in every templated expansion we make it global to the object
    protected String chart;

    // Temporary string to hold the javascript we're going to output
    protected StringBuilder js;

    // Reference to the ClientRequest parameter to render()
    protected ClientRequest cr;

    // Reserved keys are keys that are used in Java when creating a chart, but
    // aren't properties that should be set in Javascript.
    protected String[] reserved = {"id", "xId", "yId",
                                   "type", "plot", "series", "request"};

    // Properties can be set on several "objects", such as the chart, legend
    // and various axes. This is the list of all such prefixes
    protected String[] prefixes = {"legend", "xAxis", "yAxis", "bottomAxis",
                                   "leftAxis", "rightAxis", "topAxis"};

    // Properties that can be set on a chart, as opposed to a plot or a
    // series.
    protected String[] chartVars = {"title", "borderWidth", "plotBorderWidth", "width",
                                    "borderColor", "plotBorderColor",
                                    "height", "barSpacer", "background", "plotBackground",
                                    "titleFont", "titleColor", "majorGrid", "display",
                                    "ticks", "minorTicks", "tickLength",
                                    "minorTickLength", "minorGrid", "tickSide",
                                    "minorTickSide", "scale", "location",
                                    "showLabels", "labelFont", "labelColor"};


    /**
     * Constructor for a ChartSection
     *
     * @param properties      See description above
     */
    public ChartSection(Dataset properties) {
        this.properties = properties.clone();
    }

    /**
     * This method is invoked during the final phase of rendering a page;
     * it generates HTML for this section and appends it to the Html
     * object associated with {@code cr}.
     * @param cr                   Overall information about the client
     *                             request being serviced; HTML will be
     *                             appended to {@code cr.getHtml()}.
     */
    public void render(ClientRequest cr) {
        this.cr = cr;
        Html html = cr.getHtml();

        StringBuilder out = html.getBody();
        js = new StringBuilder();

        html.includeJsFile("static/fiz/ChartBar.js");
        html.includeJsFile("static/fiz/ChartScatter.js");
        html.includeJsFile("static/fiz/ChartLine.js");

        String id = getId(properties, "chart");
        out.append("<div>\n");
        // Dimensions can be changed dynamically through properties
        Template.appendRaw(out, "<canvas width=\"500\" height=\"400\" id=\"@1\">" +
                           "Your browser does not support the canvas element</canvas>\n",
                        id);
        out.append("</div>\n");


        chart = Template.expandRaw("Fiz.ids.@1", id);
        Template.appendRaw(js, "try {\n");
        Template.appendRaw(js, "@1 = {};\n", chart);
        Template.appendRaw(js, "@1.chart = new Fiz.Chart(\"@2\");\n",
                        chart, id);


        ArrayList<Dataset> plots = properties.getChildren("plot");
        for (Dataset plot : plots) {
            addPlot(plot);
        }

        if (plots.size() == 0) {
            addPlot(properties);
        }

        setProperties(properties, "chart");

        Template.appendRaw(js, "@1.chart.draw();\n", chart);
        Template.appendJs(js, "} catch (e) {\nFiz.addBulletinMessage(" +
                           "\"<div class=\\\"bulletinError\\\">\" + e + \"</div>\");\n}\n");

        cr.evalJavascript(js);
    }

    /**
     * Adds a plot to chart, including creating the data for it and setting the
     * given properties on it, and finally adding it to the chart. Modifies the
     * {@code js} variable.
     *
     * @param plot       Dataset describing the plot to add.
     */
    protected void addPlot(Dataset plot) {
        String request = plot.check("request");
        String plotId = getId(plot, "plot");

        // If there is a request, then we are not using series. Otherwise, we
        // assume the requests are in the series.
        if (request != null) { // no series
            addData(plot);
            Template.appendRaw(js, "@1.@(2) = " +
                            "new Fiz.Chart.@3(@1.@(2)_data);\n",
                            chart, plotId, getTypeOfPlot(plot));
            setProperties(plot, plotId);
            Template.appendRaw(js, "@1.chart.addPlot(@1.@(2));\n",
                            chart, plotId);
        } else { //using series
            Template.appendRaw(js, "@1.@(2) = new Fiz.Chart.@3();\n",
                            chart, plotId, getTypeOfPlot(plot));
            setProperties(plot, plotId);
            ArrayList<Dataset> series = plot.getChildren("series");
            for (Dataset serie : series) { // add each series in turn
                String seriesRequest = serie.get("request");
                String serieId = getId(serie, "series");
                addData(serie);
                Template.appendRaw(js, "@1.@2 = new Fiz.Chart.Series(@1.@(2)_data);\n",
                                chart, serieId);
                setProperties(serie, serieId);
                Template.appendRaw(js, "@1.@(2).addSeries(@1.@(3));\n",
                                chart, plotId, getId(serie, "series"));
            }
            Template.appendRaw(js, "@1.chart.addPlot(@1.@(2));\n",
                            chart, plotId);
        }

    }

    /**
     * Returns the type of the plot if specified, otherwise chooses a default
     * of "Bar"
     *
     * @param plot  Dataset describing the plot's properties
     * @return      Name of the plot type (representing the js object
     *              Fiz.Chart.ReturnValue)
     */
    protected String getTypeOfPlot(Dataset plot) {
        String type = plot.check("type");
        return type == null ? "Bar" : type;
    };

    /**
     * Adds javascript code to create an array with data for a plot. It takes
     * the request defined in the properties dataset to get the data to add
     * to the plot, using the xId and yId properties to get the proper data
     * from the dataset. Modifies the {@code js} variable.
     *
     * @param properties      Dataset describing the plot or series. Used for
     *                        the id, request, xId, and yId properties.
     */
    protected void addData(Dataset properties) {
        DataRequest req = cr.getDataRequest(properties.get("request"));
        Dataset response = req.getResponseOrAbort();

        Template.appendRaw(js, "@1.@(2)_data = [", chart, properties.get("id"));
        ArrayList<Dataset> rows = response.getChildren("record");

        if (rows.size() != 0) {
            String xIdVal = properties.get("xId");
            String yIdVal = properties.get("yId");
            for (int i = 0; i < rows.size(); i++) {
                Dataset row = rows.get(i);
                Template.appendJs(js, "[\"@1\", @2]", row.get(xIdVal),
                                          row.get(yIdVal));
                if (i != rows.size() - 1) {
                    js.append(", ");
                }
            }
        }

        js.append("];\n");
    }

    /**
     * Sets the properties for a plot, chart, or series. This is complicated due
     * to possible collapse of datasets for brevity, so it is not immediately
     * obvious which object we are setting the property on. We keep a list of
     * all properties which are unique to the chart. If a property is in that
     * list, then we assume we set it on the chart. Otherwise on the plot or
     * series. We also make sure to avoid java reserved variables, such as
     * xId, yId, request, etc. Modifies the {@code js} variable.
     *
     * @param plot       Dataset describing the plot
     * @param name       Name of the object to set the properties on
     */
    protected void setProperties(Dataset plot, String name) {
        Set<String> keySet = plot.keySet();
        for (String key : keySet) {
            if (name == "chart" && hasKey(prefixes, key)) {
                Dataset sub = plot.getChild(key);
                Set<String> subKeys = sub.keySet();
                for (String subKey : subKeys) {
                    String value = sub.get(subKey);
                    Template.appendRaw(js, "@1.@2.set(\"@3\", \"@4\");\n",
                                       chart, name, combineCamel(key, subKey), value);
                }
                continue;
            }
            String value = plot.check(key);
            if (value != null) {
                if (!hasKey(reserved, key)) {
                    if (!hasKey(chartVars, key) ^ (name == "chart")) {
                        Template.appendRaw(js, "@1.@2.set(\"@3\", \"@4\");\n",
                                    chart, name, key, value);
                    }
                }
            }

        }
    }

    /**
     * Combines the two strings in standard camel case notation. For example,
     * foo and bar combined to become fooBar.
     *
     * @param start      First part of the string
     * @param end        Second part of the string
     * @return           The two strings combined in camel case format
     */
    protected String combineCamel(String start, String end) {
        if (end == "") {
            return start;
        }

        return start + Character.toUpperCase(end.charAt(0)) + end.substring(1);
    };

    /**
     * Returns whether the given key is an element of the array.
     *
     * @param arr        Array of elements
     * @param key        Key to lookup in the array.
     * @return           Whether the key (which may have a prefix) was an
     *                   element of the array
     */
    protected boolean hasKey(String[] arr, String key) {
        for (String k : arr) {
            if (key.equals(k))  {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the {@code id} property of a dataset. If it does not have one, it
     * generates a unique one and modifies the dataset to add it.
     *
     * @param properties      Dataset describing the plot's properties
     * @param base            Base name to use for the id if it needs to be
     *                        generated.
     *                        For example, foo might return foo0.
     * @return                The id associated with the dataset
     */
    protected String getId(Dataset properties, String base) {
        String id = properties.check("id");
        if (id == null) {
            id = cr.uniqueId(base);
            properties.set("id", id);
        }

        return id;
    }

}
