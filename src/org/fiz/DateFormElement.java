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

import java.text.*;
import java.util.*;
import java.util.regex.MatchResult;

/**
 * The DateFormElement allows users to input time and date either manually (in a
 * variety of formats) or through a customizable, JS-driven calendar object. It
 * supports the following properties:
 *   class:             (optional) Class attribute to use for the {@code <div>}
 *                      containing this element; defaults to DateFormElement.
 *   id:                (required) Name for this FormElement; must be unique
 *                      among all ids for the page. This is used as the name for
 *                      the data value in query and update requests and also as
 *                      the {@code name} attribute for the HTML input element.
 *   label:             (optional) Template for label to display next to the
 *                      input field to identify the element for the user.
 *   attachPosition:    (optional) Defaults to {@code bottom}. Define whether
 *                      the calendar pops up to the {@code right} or to the
 *                      {@code bottom} of the input field.
 *   dateFormat:        (optional) Defaults to {@code m/d/Y}. Specifies the
 *                      format of the date in the input field. The format syntax
 *                      is described below.
 *   calendarIcon:      (optional) Defaults to /static/fiz/calendar-icon.gif.
 *                      Decides which icon to use on the right of the form field
 *                      for opening the calendar.
 *   exclude:           (optional) Specifies the days to exclude from selection
 *                      on the calendar. The syntax and specifies for the
 *                      exclude property are described below.
 *                                         
 * The {@code dateFormat} property allows a developer to specify the format
 * of the date displayed to the user in the {@code <input>} field. The following
 * specifiers may be used:
 *   d:                 day without leading 0s (1, 2, ... , 30, 31)
 *   D:                 day with leading 0s (01, 02, ... , 30, 31)
 *   m:                 month without leading 0s (1, 2, ... , 11, 12)
 *   M:                 month with leading 0s (01, 02, ... , 11, 12)
 *   y:                 year in two-digit form (88, 89, ... , 01, 02)
 *   Y:                 year in four-digit form (1988, ... , 2002)
 *                          
 * The {@code exclude} property is a comma-separated list of filter rules used
 * to determine whether a date is selectable or not. The following specifiers
 * may be used:
 *   {day_of_week}:             Monday, Tuesday, ... , Saturday, Sunday
 *   today:                     replaced with the current date
 *   {m}/{d}{/{Y}}?:            Specifies specific dates to omit. If the year is
 *                              left out, then the date is omitted on an annual
 *                              basis.
 *   {number} {unit} {ago?}:    Specifies a relative date. The optional
 *                              {@code ago} flag specifies a relative date in
 *                              the past e.g. 12 months ago, 3 days.
 * 
 * The following syntax may be used:
 *   {start_date?}:{end_date?}:     {@code start_date} and {@code end_date} can
 *                                  be replaced with any of the specifiers
 *                                  above. If either the {@code start_date} or
 *                                  the {@code end_date} are omitted, then the
 *                                  range goes infinitely into the past or
 *                                  infinitely into the future respectively.
 */
public class DateFormElement extends FormElement {

    // Used to manually set the current date for debugging purposes
    protected static Date today = null;

    public static final String[] WEEK_SHORT = { "Su", "M", "Tu", "W", "Th",
        "F", "Sa" };

    /**
     * Construct a DateFormElement from a set of properties that define its
     * configuration.
     * 
     * @param properties        Dataset whose values are used to configure the
     *                          element. See the class documentation above for
     *                          a list of supported values.
     */
    public DateFormElement(Dataset properties) {
        super(properties);
    }

    /**
     * Construct a DateFormElement from an identifier and label.
     * 
     * @param id                Value for the element's {@code id} property.
     * @param label             Value for the element's {@code label} property.
     */
    public DateFormElement(String id, String label) {
        this(new Dataset("id", id, "label", label));
    }

    /**
     * This method is invoked to generate HTML for this form element.
     * 
     * @param cr                Overall information about the client
     *                          request being serviced.
     * @param data              Data for the form (a CompoundDataset including
     *                          both form data, if any, and the global dataset).
     * @param out               Generated HTML is appended here.
     */
    @Override
    public void render(ClientRequest cr, Dataset data, StringBuilder out) {

        // Create the form fields in HTML
        Template.appendHtml(out, "\n<!-- Start DateFormElement @id -->\n" +
                "<div class=\"@class?{DateFormElement}\" " +
                "id=\"@(id)_container\">\n" +
                "  <input type=\"text\" name=\"@id\" id=\"@id\" " +
                "onblur=\"Fiz.ids.@id.validateAndUpdate()\" " +
                "onkeyup=\"Fiz.ids.@id.validateAndDisplay()\" " +
                "{{value=\"@1?{" + dateToString(getToday()) + "}\"}} />\n",
                properties, data.check(id));

        Template.appendHtml(out, "  <img src=\"" +
                "@calendarIcon?{/static/fiz/images/calendar-icon.gif}\" " +
                "id=\"@(id)_icon\" alt=\"Pick a date\" " +
                "onclick=\"Fiz.ids.@id.togglePicker()\" />\n",
                properties, data.check(id));

        // Create the calendar picker in HTML
        Template.appendHtml(out, "  <div id=\"@(id)_picker\" class=\"picker\">\n" +
                "    <div id=\"@(id)_header\" class=\"header\"></div>\n" +
                "    <a onclick=\"Fiz.ids.@id.closePicker()\" " +
                "class=\"close-button\">[x]</a>\n" +
                "    <table id=\"@(id)_table\">\n", properties);

        for (String week : WEEK_SHORT) {
            out.append("      <col class=\"col-" + week + "\" />\n");
        }

        out.append("      <thead>\n" + "        <tr>");

        for (String week : WEEK_SHORT) {
            out.append("<th>" + week + "</th>");
        }

        Template.appendHtml(out, "</tr>\n" + "      </thead>\n" +
                "      <tbody id=\"@(id)_grid\">\n", properties);
        for (int i = 0; i < 6; i++) {
            if (i % 2 == 0) {
                out.append("        <tr class=\"even\">");
            } else {
                out.append("        <tr class=\"odd\">");
            }
            for (int j = 0; j < 7; j++) {
                out.append("<td></td>");
            }
            out.append("</tr>\n");
        }
        out.append("      </tbody>\n" + "    </table>\n");

        // Add navigation to our calendar
        Template.appendHtml(out, 
                "    <div id=\"@(id)_navigation\" class=\"nav\">\n" +
                "      <a onclick=\"Fiz.ids.@id.prevYear()\" " +
                "id=\"@(id)_prevYear\" class=\"arrow-prev-year\">" +
                "<img src=\"/static/fiz/images/arrow-left-double.gif\" " +
                "alt=\"Previous Year\" /></a>\n      " +
                "<a onclick=\"Fiz.ids.@id.prevMonth()\" " +
                "id=\"@(id)_prevMonth\" class=\"arrow-prev-month\">" +
                "<img src=\"/static/fiz/images/arrow-left.gif\" " +
                "alt=\"Previous Month\" /></a>\n      " +
                "<a onclick=\"Fiz.ids.@id.today()\" " +
                "class=\"arrow-today\">Today</a>\n      " +
                "<a onclick=\"Fiz.ids.@id.nextMonth()\" " +
                "id=\"@(id)_nextMonth\" class=\"arrow-next-month\">" +
                "<img src=\"/static/fiz/images/arrow-right.gif\" " +
                "alt=\"Next Month\" /></a>\n      " +
                "<a onclick=\"Fiz.ids.@id.nextYear()\" " +
                "id=\"@(id)_nextYear\" class=\"arrow-next-year\">" +
                "<img src=\"/static/fiz/images/arrow-right-double.gif\" " +
                "alt=\"Next Year\" /></a>\n    " + "</div>\n",
                properties);
        out.append("  </div>\n</div>\n");
        Template.appendHtml(out, "<!-- End DateFormElement @id -->\n",
                properties);

        // Generate a Javascript object containing information about the form.
        cr.evalJavascript("Fiz.ids.@id = new Fiz.DateFormElement('@id', "
                + "'@dateFormat?{m/d/Y}', '@attachPosition?{bottom}'" + ");\n",
                properties);

        // Process and pass the filters to our JavaScript
        String filters = properties.check("exclude");
        if (filters != null && !filters.equals("")) {
            StringBuilder js = new StringBuilder("Fiz.ids.@id.setFilters(");
            parseFilter(filters.toLowerCase()).toJavascript(js);
            js.append(");\n");
            cr.evalJavascript(js.toString(), properties);
        }

        cr.getHtml().includeCssFile("DateFormElement.css");
        cr.getHtml().includeJsFile("static/fiz/DateFormElement.js");
    }

    /**
     * Parses the filter string to create a Dataset containing a list of rules
     * for determining whether a date is selectable or not.
     * 
     * @param filterString         String consisting of a comma-separated list
     *                             of filters
     * @return Dataset containing a list of the filters in a processed format
     */
    protected Dataset parseFilter(String filterString) {
        Dataset filters = new Dataset();
        String[] filterList = filterString.split("\\s*,\\s*");

        int dayOfWeek;
        for (String filter : filterList) {
            if (filter.contains(":")) {
                // date range
                String[] range = filter.split(":", -1);
                filters.addChild("filter", new Dataset("type", "range",
                        "startDate", formatForJs(range[0]), "endDate",
                        formatForJs(range[1])));
            } else if ((dayOfWeek = getDayOfWeek(filter)) >= 0) { 
                // day of the week filter
                filters.addChild("filter", new Dataset("type", "dayOfWeek",
                        "dayOfWeek", Integer.toString(dayOfWeek)));
            } else {
                String[] dateValues = filter.split("/");
                if (dateValues.length == 3) {
                    filters.addChild("filter", new Dataset("type", "date",
                            "date", formatForJs(filter)));
                } else {
                    filters.addChild("filter", new Dataset("type",
                            "annualDate", "month", dateValues[0], "day",
                            dateValues[1]));
                }
            }
        }

        return filters;
    }

    /**
     * Formats a user-defined date string into a Javascript parseable string.
     * 
     * @param dateString        A string that fits one of the specified forms
     *                          allowed for defining a date
     * @return A Javascript parsable date string
     */
    protected static String formatForJs(String dateString) {
        return dateToString(stringToDate(dateString));
    }

    /**
     * Converts various strings into Java Date objects
     * 
     * @param dateString        A string that fits one of the specified forms
     *                          allowed for defining a date
     * @return A date object representing the input string
     */
    protected static Date stringToDate(String dateString) {
        dateString = dateString.toLowerCase();
        if (dateString.equals("")) {
            return null;
        }
        if (dateString.equals("today")) {
            return getToday();
        } else {
            DateFormat f = new SimpleDateFormat("M/d/yyyy");
            Date dateObject = null;
            try {
                dateObject = f.parse(dateString);
            } catch (ParseException e) {
                dateObject = null;
            }
            if (dateObject != null) {
                return dateObject;
            } else {
                Calendar cal = new GregorianCalendar();
                dateObject = getToday();
                cal.setTime(dateObject);

                Scanner s = new Scanner(dateString);
                if (s.findInLine("^(\\d+) (month|day|year)s?\\s?(ago)?$")
                        != null) {
                    MatchResult result = s.match();

                    int amount = Integer.parseInt(result.group(1));
                    if (result.group(3) != null) {
                        amount *= result.group(3).equals("ago") ? -1 : 1;
                    }
                    String unit = result.group(2);

                    if (unit.equals("month")) {
                        cal.add(Calendar.MONTH, amount);
                    } else if (unit.equals("day")) {
                        cal.add(Calendar.DAY_OF_MONTH, amount);
                    } else if (unit.equals("year")) {
                        cal.add(Calendar.YEAR, amount);
                    }
                    return cal.getTime();
                }
            }
        }
        return null;
    }

    /**
     * Converts a Java Date object into a Javascript parsable date string
     * 
     * @param dateObject        The date object to be converted into a string
     * @return                  A Javascript parsable date string
     */
    protected static String dateToString(Date dateObject) {
        if (dateObject == null) {
            return "null";
        }
        DateFormat f = new SimpleDateFormat("MMMM d, yyyy");
        return f.format(dateObject);
    }

    /**
     * Gets the corresponding index for the day of the week
     * 
     * @param dayOfWeek         String representation of the day of the week
     *                          (Sunday, sun, Monday, tues, ...)
     * @return                  Index for the day of the week (0 = Sunday, 
     *                          1 = Monday, ... , 6 = Saturday)
     */
    protected static int getDayOfWeek(String dayOfWeek) {
        for (int i = 0; i < WEEK_SHORT.length; i++) {
            if (dayOfWeek.toLowerCase().indexOf(
                    WEEK_SHORT[i].toLowerCase()) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets a date object representing the current date (or a debug date if one
     * is specified by setting the static {@code today} variable).
     * 
     * @return                  Date object for the current date (or debug date)
     */
    protected static Date getToday() {
        return (today == null ? new Date() : today);
    }
}
