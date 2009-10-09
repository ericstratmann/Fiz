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
 * supports the following properties (additional properties applicable to all
 * form elements may be found in {@link FormElement}):
 *   attachPosition:    (optional) Defaults to {@code bottom}. Define whether
 *                      the calendar pops up to the {@code right} or to the
 *                      {@code bottom} of the input field.
 *   dateFormat:        (optional) Defaults to {@code m/d/Y}. Specifies the
 *                      format of the date in the input field. This will also be
 *                      the format of the date returned by the form when it is
 *                      submitted. The format syntax is described below.
 *   errorMessage:      (optional) Overrides the default error message returned
 *                      when validating the DateFormElement. Refer to the
 *                      {@code errorMessage} property in the
 *                      {@link FormValidator} class.
 *   exclude:           (optional) Specifies the days to exclude from selection
 *                      on the calendar. The syntax for the exclude property is
 *                      described below.
 *   family:            (optional) Family name for images to display. If the
 *                      family name is {@code x.gif}, then image
 *                      {@code x-icon.gif} must exist (this is the only image
 *                      in the family at this time).  Default value is
 *                      {@code dateForm.gif}
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
 *
 * DateFormElement automatically sets the following {@code class} attributes
 * for use in CSS:
 *   picker:           The {@code <div>} containing the calendar dropdown.
 *   header:           The {@code <div>} containing the currently displayed
 *                     calendar month / year.
 *   nav:              The {@code <div>} containing the calendar navigation.
 *   close-button:     The {@code <a>} for the close button in the top-right
 *                     of the calendar.
 *   col-{week_short}: The {@code <col>} corresponding to the day of the week.
 *                     {week_short} may take on the values "Su", "M", "Tu", "W",
 *                     "Th", "F", "Sa".
 *   cur-month:        The {@code <td>} elements in the calendar representing a
 *                     day in the currently displayed month.
 *   cur-day:          The {@code <td>} in the calendar representing the
 *                     currently selected date.
 *   excluded:         The {@code <td>} elements in the calendar representing
 *                     days that have been omitted from selection.
 */
public class DateFormElement extends FormElement {

    // Used to manually set the current date for debugging purposes
    protected static Date today = null;

    public static final String[] WEEK_SHORT = { "Su", "M", "Tu", "W", "Th",
        "F", "Sa" };

    // The format of the date that will be displayed to the user in the input
    // field and also returned by the form on submission
    String dateFormat = null;

    // Parsed set of filters used to check if a date can be selected or not
    Dataset filters = null;

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
        dateFormat = properties.checkString("dateFormat");

        if (dateFormat == null) {
            dateFormat = "m/d/Y";
        }

        // Create a validator to check that the date is in the right format
        String regex = dateFormat.replaceAll("[^A-Za-z0-9]", "\\\\$0")
                                 .replace("m", "([1-9]|1[0-2])")
                                 .replace("M", "(0[1-9]|1[0-2])")
                                 .replace("d", "([1-2]?[0-9]|3[0-1])")
                                 .replace("D", "([0-2][0-9]|3[0-1])")
                                 .replace("y", "[0-9]{2}")
                                 .replace("Y", "[0-9]{4}");
        addValidator(new Dataset("type", "regex", "pattern", regex));

        // Create a validator to check that the date is selectable
        String exclude = properties.checkString("exclude");
        if (exclude != null) {
            filters = parseFilter(exclude.toLowerCase());
            Dataset validator = new Dataset(
                    "type", "DateFormElement.validateDate",
                    "exclude", filters,
                    "dateFormat", dateFormat);
            String errorMessage = properties.checkString("errorMessage");
            if (errorMessage != null) {
                validator.set("errorMessage", errorMessage);
            }
            addValidator(validator);
        }
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
     */
    @Override
    public void render(ClientRequest cr, Dataset data) {
        StringBuilder out = cr.getHtml().getBody();
        // Create the form fields in HTML
        Template.appendHtml(out, "\n<!-- Start DateFormElement @id -->\n" +
                "<div class=\"@class?{DateFormElement}\" " +
                "id=\"@(id)_container\">\n" +
                "  <input type=\"text\" name=\"@id\" id=\"@id\" " +
                "onblur=\"Fiz.ids.@id.validateAndUpdate()\" " +
                "onkeyup=\"Fiz.ids.@id.validateAndDisplay()\" " +
                "{{value=\"@1?{" + dateToString(getToday()) + "}\"}} />\n",
                properties, data.checkString(id));

        String family = properties.checkString("family");
        if (family == null) {
            family = "dateForm.gif";
        }
        Template.appendHtml(out, "  <img src=\"" +
                "/static/fiz/images/@1\" " +
                "id=\"@(id)_icon\" alt=\"Pick a date\" " +
                "onclick=\"Fiz.ids.@id.togglePicker()\" />\n",
                properties, StringUtil.addSuffix(family, "-icon"));

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
                "&laquo;</a>\n" +
                "      <a onclick=\"Fiz.ids.@id.prevMonth()\" " +
                "id=\"@(id)_prevMonth\" class=\"arrow-prev-month\">" +
                "&lsaquo;</a>\n" +
                "      <a onclick=\"Fiz.ids.@id.today()\" " +
                "class=\"arrow-today\">Today</a>\n" +
                "      <a onclick=\"Fiz.ids.@id.nextMonth()\" " +
                "id=\"@(id)_nextMonth\" class=\"arrow-next-month\">" +
                "&rsaquo;</a>\n" +
                "      <a onclick=\"Fiz.ids.@id.nextYear()\" " +
                "id=\"@(id)_nextYear\" class=\"arrow-next-year\">" +
                "&raquo;</a>\n    </div>\n", properties);
        out.append("  </div>\n</div>\n");
        Template.appendHtml(out, "<!-- End DateFormElement @id -->\n",
                properties);

        // Generate a Javascript object containing information about the form.
        cr.evalJavascript("Fiz.ids.@id = new Fiz.DateFormElement('@id', "
                + "'@dateFormat?{m/d/Y}', '@attachPosition?{bottom}'" + ");\n",
                properties);
        // Process and pass the filters to our JavaScript
        if (filters != null) {
            StringBuilder js = new StringBuilder("Fiz.ids.@id.setFilters(");
            filters.toJavascript(js);
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
    protected static Dataset parseFilter(String filterString) {
        Dataset filters = new Dataset();
        String[] filterList = filterString.split("\\s*,\\s*");

        int dayOfWeek;
        for (String filter : filterList) {
            if (filter.contains(":")) {
                // filters out a range of dates
                String[] range = filter.split(":", -1);
                filters.add("filter", new Dataset("type", "range",
                        "startDate", formatForJs(range[0]), "endDate",
                        formatForJs(range[1])));
            } else if ((dayOfWeek = getDayOfWeek(filter)) >= 0) {
                // filters out a day of the week
                filters.add("filter", new Dataset("type", "dayOfWeek",
                        "dayOfWeek", Integer.toString(dayOfWeek)));
            } else {
                String[] dateValues = filter.split("/");
                if (dateValues.length == 3) {
                    // filters out a specific date
                    filters.add("filter", new Dataset("type", "date",
                            "date", formatForJs(filter)));
                } else {
                    // filters the same day/month on an annual basis
                    filters.add("filter", new Dataset("type",
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

    /**
     * Validates that the value of {@code id} is a selectable date according
     * to the filter rules passed in as the {@code exclude} property of the
     * DateFormElement.
     *
     *   exclude:                Nested dataset containing one nested dataset
     *                           per filter. The filter dataset will include a
     *                           {@code type} property describing the type of
     *                           filter along with a set of parameters for each
     *                           type of filter. The nested dataset for this
     *                           property should not be constructed manually
     *                           and should instead utilize the
     *                           {@code parseFilter} method paired with an
     *                           {@code exclude} string described in the header
     *                           for the DateFormElement.
     *   dateFormat:             The format of the date string being validated.
     *                           The date string is the one that appears in the
     *                           input element.
     *
     * @param id                        id of the input element to validate
     * @param properties                Configuration properties for the
     *                                  validator: see above for supported
     *                                  values
     * @param formData                  Data from all relevant form elements
     *                                  needed to perform this validation
     * @return                          Error message if validation fails,
     *                                  null otherwise
     */
    public static String validateDate(String id, Dataset properties,
            Dataset formData) {
        Dataset filters = properties.getDataset("exclude");

        // Parses the value in the input field into a Date object
        Calendar date = Calendar.getInstance();
        String dateFormat = properties.getString("dateFormat");
        dateFormat = dateFormat.replace('m', 'M');
        dateFormat = dateFormat.replace('D', 'd');
        dateFormat = dateFormat.replace("Y", "yy");
        DateFormat parser = new SimpleDateFormat(dateFormat);
        try {
            date.setTime(parser.parse(formData.getString(id)));
        } catch (Exception e) {
            return FormValidator.errorMessage("Invalid date format",
                    properties, formData);
        }

        // Used to parse the Javascript formatted dates stored in the
        // exclude dataset
        DateFormat dateParser = new SimpleDateFormat("MMMM d, yyyy");

        for (Dataset filter : filters.getDatasetList("filter")) {
            if (filter.getString("type").equals("dayOfWeek")) {
                // Excludes the day of the week
                int dayOfWeek = Integer.parseInt(filter.getString("dayOfWeek"));
                int curDayOfWeek = date.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek + 1 == curDayOfWeek) {
                    return FormValidator.errorMessage("Invalid day of the week",
                            properties, formData);
                }
            } else if (filter.getString("type").equals("date")) {
                // Excludes the specified date
                Date filterDate = null;
                try {
                    filterDate = dateParser.parse(filter.getString("date"));
                } catch (Exception e) {
                }
                if (filterDate.compareTo(date.getTime()) == 0) {
                    return FormValidator.errorMessage("Invalid date",
                            properties, formData);
                }
            } else if (filter.getString("type").equals("annualDate")) {
                // Excludes the specified date for all years
                int month = Integer.parseInt(filter.getString("month"));
                int day = Integer.parseInt(filter.getString("day"));
                if (date.get(Calendar.MONTH) == month - 1
                        && date.get(Calendar.DATE) == day) {
                    return FormValidator.errorMessage("Invalid day of the year",
                            properties, formData);
                }
            } else if (filter.getString("type").equals("range")) {
                // Excludes a range of dates
                // If an endpoint on a range is null, then we only look at the
                // other endpoint to determine if a date is within range. We
                // use the value 0 to ignore an endpoint.
                String startDate = filter.getString("startDate");
                String endDate = filter.getString("endDate");
                int startCompare = 0, endCompare = 0;
                try {
                    if (!startDate.equals("null")) {
                        startCompare = date.getTime().compareTo(
                                dateParser.parse(startDate));
                    }

                    if (!endDate.equals("null")) {
                        endCompare = date.getTime().compareTo(
                                dateParser.parse(endDate));
                    }
                } catch (Exception e) {
                }

                if ((startCompare == 1 || startCompare == 0)
                    && (endCompare == -1 || endCompare == 0)) {
                    return FormValidator.errorMessage("Date not in range",
                            properties, formData);
                }
            }
        }

        return null;
    }
}
