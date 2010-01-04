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

package org.fiz.section;

import org.fiz.Dataset;
import org.fiz.section.FormSection.FormDataException;
import org.fiz.*;
import org.fiz.test.*;

import java.util.*;

/**
 * Junit tests for the DateFormElement class.
 */
public class DateFormElementTest extends junit.framework.TestCase {

    private Date createDate(int month, int day, int year) {
        Calendar date = new GregorianCalendar(year, month - 1, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    public void test_constructor_formatValidator() {
        DateFormElement element = new DateFormElement(
                new Dataset("id", "elem", "dateFormat", "mM:dD-Yy"));
        assertEquals(element.validatorData.validators.get(0).get("pattern"),
                "([1-9]|1[0-2])(0[1-9]|1[0-2])\\:" +
                "([1-2]?[0-9]|3[0-1])([0-2][0-9]|3[0-1])\\-[0-9]{4}[0-9]{2}");

        DateFormElement element2 = new DateFormElement(
                new Dataset("id", "elem", "dateFormat", "Ma1.dD2"));
        assertEquals(element2.validatorData.validators.get(0).get("pattern"),
                "(0[1-9]|1[0-2])a1\\.([1-2]?[0-9]|3[0-1])" +
                "([0-2][0-9]|3[0-1])2");
    }

    public void test_constructor_excludeValidator() {
        DateFormElement element = new DateFormElement(
                new Dataset("id", "elem", "exclude", "9/8/2009"));
        assertEquals(element.validatorData.validators.get(1).toString(),
                "dateFormat: m/d/Y\n" +
                "exclude:\n" +
                "    filter:\n" +
                "        date: \"September 8, 2009\"\n" +
                "        type: date\n" +
                "type:       section.DateFormElement.validateDate\n");
    }

    public void test_render_basics() {
        DateFormElement.today = createDate(1, 14, 2008);
        DateFormElement element = new DateFormElement(
                new Dataset("id", "cal1", "label", "Calendar"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset());
        assertEquals("generated HTML", "\n" +
                "<!-- Start DateFormElement cal1 -->\n" +
                "<div class=\"DateFormElement\" id=\"cal1_container\">\n" +
                "  <input type=\"text\" name=\"cal1\" id=\"cal1\" " +
                "onblur=\"Fiz.ids.cal1.validateAndUpdate()\" " +
                "onkeyup=\"Fiz.ids.cal1.validateAndDisplay()\" " +
                "value=\"January 14, 2008\" />\n" +
                "  <img src=\"/static/fiz/images/dateForm-icon.gif\" " +
                "id=\"cal1_icon\" alt=\"Pick a date\" " +
                "onclick=\"Fiz.ids.cal1.togglePicker()\" />\n" +
                "  <div id=\"cal1_picker\" class=\"picker\">\n" +
                "    <div id=\"cal1_header\" class=\"header\"></div>\n" +
                "    <a onclick=\"Fiz.ids.cal1.closePicker()\" " +
                "class=\"close-button\">[x]</a>\n" +
                "    <table id=\"cal1_table\">\n" +
                "      <col class=\"col-Su\" />\n" +
                "      <col class=\"col-M\" />\n" +
                "      <col class=\"col-Tu\" />\n" +
                "      <col class=\"col-W\" />\n" +
                "      <col class=\"col-Th\" />\n" +
                "      <col class=\"col-F\" />\n" +
                "      <col class=\"col-Sa\" />\n" +
                "      <thead>\n" +
                "        <tr><th>Su</th><th>M</th><th>Tu</th><th>W</th>" +
                "<th>Th</th><th>F</th><th>Sa</th></tr>\n" +
                "      </thead>\n" +
                "      <tbody id=\"cal1_grid\">\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td>" +
                "<td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td>" +
                "<td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td>" +
                "<td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td>" +
                "<td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td>" +
                "<td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td>" +
                "<td></td><td></td><td></td><td></td></tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "    <div id=\"cal1_navigation\" class=\"nav\">\n" +
                "      <a onclick=\"Fiz.ids.cal1.prevYear()\" " +
                "id=\"cal1_prevYear\" class=\"arrow-prev-year\">" +
                "&laquo;</a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.prevMonth()\" " +
                "id=\"cal1_prevMonth\" class=\"arrow-prev-month\">" +
                "&lsaquo;</a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.today()\" " +
                "class=\"arrow-today\">Today</a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.nextMonth()\" " +
                "id=\"cal1_nextMonth\" class=\"arrow-next-month\">" +
                "&rsaquo;</a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.nextYear()\" " +
                "id=\"cal1_nextYear\" class=\"arrow-next-year\">" +
                "&raquo;</a>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<!-- End DateFormElement cal1 -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\">\n");
        out.append("</form>\n");
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement('cal1', " +
                "'m/d/Y', 'bottom');\n",
                 cr.getHtml().getJs());
        TestUtil.assertSubstring("CSS file names", "DateFormElement.css",
                cr.getHtml().getCssFiles());
        assertEquals("Javascript file names",
                "static/fiz/Ajax.js, static/fiz/DateFormElement.js, " +
                "static/fiz/Fiz.js, static/fiz/FormElement.js, " +
                "static/fiz/date.js",
                cr.getHtml().getJsFiles());
        TestUtil.assertXHTML(cr.getHtml().toString());
    }

    public void test_render_attachPosition() {
        DateFormElement element = new DateFormElement(
                new Dataset(
                        "id", "cal1",
                        "label", "Calendar",
                        "attachPosition", "right"));
        ClientRequest cr = new ClientRequestFixture();
        element.render(cr, new Dataset());
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement('cal1', " +
                "'m/d/Y', 'right');\n",
                 cr.getHtml().getJs());
    }

    public void test_render_dateFormat() {
        DateFormElement element = new DateFormElement(
                new Dataset(
                        "id", "cal1",
                        "label", "Calendar",
                        "dateFormat", "m-d-Y"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset());
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement('cal1', " +
                "'m-d-Y', 'bottom');\n",
                 cr.getHtml().getJs());
    }

    public void test_render_calendarIcon() {
        DateFormElement.today = createDate(1, 14, 2008);
        DateFormElement element = new DateFormElement(
                new Dataset(
                        "id", "cal1",
                        "label", "Calendar",
                        "family", "test.jpg"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset());
        assertTrue("generated HTML", out.toString().contains(
                "<img src=\"/static/fiz/images/test-icon.jpg\" " +
                "id=\"cal1_icon\" alt=\"Pick a date\" " +
                "onclick=\"Fiz.ids.cal1.togglePicker()\" />\n"));
        out.insert(0, "<form action=\"a/b/c\">\n");
        out.append("</form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }

    public void test_render_exclude() {
        Dataset.sortOutput = true;
        DateFormElement element = new DateFormElement(
                new Dataset(
                        "id", "cal1",
                        "label", "Calendar",
                        "dateFormat", "m-d-Y",
                        "exclude", "Saturday, 1/12/2005, 7/23," +
                        "8/13/2010:9/11/2010, :6/24/2009, 9/3/2011:"));
        ClientRequest cr = new ClientRequestFixture();
        element.render(cr, new Dataset());
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement(" +
                "'cal1', 'm-d-Y', 'bottom');\n" +
                "Fiz.ids.cal1.setFilters(" +
                "{filter: [{dayOfWeek: \"6\", type: \"dayOfWeek\"}, " +
                "{date: \"January 12, 2005\", type: \"date\"}, " +
                "{day: \"23\", month: \"7\", " +
                "type: \"annualDate\"}, " +
                "{endDate: \"September 11, 2010\", " +
                "startDate: \"August 13, 2010\", type: \"range\"}, " +
                "{endDate: \"June 24, 2009\", startDate: \"null\", " +
                "type: \"range\"}, " +
                "{endDate: \"null\", startDate: \"September 3, 2011\", " +
                "type: \"range\"}]});\n",
                 cr.getHtml().getJs());
    }

    public void test_stringToDate() {
        DateFormElement.today = createDate(1, 14, 2008);
        assertNull("No date", DateFormElement.stringToDate(""));
        assertTrue("Today", createDate(1, 14, 2008).equals(
                DateFormElement.stringToDate("today")));
        assertTrue("6/8/2009", createDate(6, 8, 2009).equals(
                DateFormElement.stringToDate("6/8/2009")));
        assertTrue("8 months", createDate(9, 14, 2008).equals(
                DateFormElement.stringToDate("8 months")));
        assertTrue("3 days", createDate(1, 17, 2008).equals(
                DateFormElement.stringToDate("3 days")));
        assertTrue("2 years", createDate(1, 14, 2010).equals(
                DateFormElement.stringToDate("2 years")));
    }

    public void test_dateToString() {
        assertEquals("January 14, 2008",
                DateFormElement.dateToString(createDate(1, 14, 2008)));
    }

    public void test_getDayOfWeek() {
        assertEquals("Sunday", 0, DateFormElement.getDayOfWeek("Sunday"));
        assertEquals("mon", 1, DateFormElement.getDayOfWeek("mon"));
        assertEquals("Tues", 2, DateFormElement.getDayOfWeek("Tues"));
        assertEquals("wednesday", 3, DateFormElement.getDayOfWeek("wednesday"));
        assertEquals("Thursday", 4, DateFormElement.getDayOfWeek("Thursday"));
        assertEquals("fri", 5, DateFormElement.getDayOfWeek("fri"));
        assertEquals("sat", 6, DateFormElement.getDayOfWeek("sat"));
    }

    public void test_getToday() {
        DateFormElement.today = createDate(1, 14, 2008);
        assertTrue(createDate(1, 14, 2008).equals(DateFormElement.getToday()));
    }

    public void test_validate_validFormat() {
        DateFormElement element = new DateFormElement(new Dataset(
                "id", "elem1",
                "dateFormat", "Y-M-D"));

        boolean exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "2001-01-01"));
            element.validate(new Dataset("elem1", "1999-03-23"));
            element.validate(new Dataset("elem1", "2009-12-31"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Valid inputs", !exceptionOccurred);
    }

    public void test_validate_invalidFormat() {
        DateFormElement element = new DateFormElement(new Dataset(
                "id", "elem1",
                "dateFormat", "Y-M-D"));

        boolean exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "99-01-01"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Invalid input: 99-01-01", exceptionOccurred);

        exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "1999-1-01"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Invalid input: 1999-1-01", exceptionOccurred);

        exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "1999-01-1"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Invalid input: 1999-01-1", exceptionOccurred);

        exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "1999-13-01"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Invalid input: 1999-13-01", exceptionOccurred);

        exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "1999-12-32"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Invalid input: 1999-12-32", exceptionOccurred);
    }

    public void test_validate_validDate() {
        DateFormElement element = new DateFormElement(new Dataset(
                "id", "elem1",
                "exclude", "9/8/2008"));

        boolean exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "9/6/2008"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Invalid input: 9/6/2008", !exceptionOccurred);
    }

    public void test_validate_invalidDate() {
        DateFormElement element = new DateFormElement(new Dataset(
                "id", "elem1",
                "exclude", "9/8/2008"));

        boolean exceptionOccurred = false;
        try {
            element.validate(new Dataset("elem1", "9/8/2008"));
        } catch (FormDataException e) {
            exceptionOccurred = true;
        }
        assertTrue("Invalid input: 9/8/2008", exceptionOccurred);
    }

    public void test_validateDate_dayOfWeek() {
        Dataset properties = new Dataset("errorMessage", "<failed>",
                "dateFormat", "m-d-Y", "exclude",
                DateFormElement.parseFilter("Saturday")
        );

        assertEquals("1-1-2011", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "1-1-2011")));
        assertEquals("1-2-2011", null,
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "1-2-2011")));
    }

    public void test_validateDate_specificDate() {
        Dataset properties = new Dataset("errorMessage", "<failed>",
                "dateFormat", "m-d-Y", "exclude",
                DateFormElement.parseFilter("1/12/2011")
        );

        assertEquals("1-12-2011", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "1-12-2011")));
        assertEquals("1-2-2011", null,
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "1-2-2011")));
    }

    public void test_validateDate_backDateRange() {
        Dataset properties = new Dataset("errorMessage", "<failed>",
                "dateFormat", "m-d-Y", "exclude",
                DateFormElement.parseFilter(":6/24/2009")
        );

        assertEquals("6-23-2009", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "6-23-2009")));
        assertEquals("6-24-2009", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "6-24-2009")));
        assertEquals("6-25-2009", null,
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "6-25-2009")));
    }

    public void test_validateDate_dateRange() {
        Dataset properties = new Dataset("errorMessage", "<failed>",
                "dateFormat", "m-d-Y", "exclude",
                DateFormElement.parseFilter("8/18/2010:9/14/2010")
        );

        assertEquals("8-17-2010", null,
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "8-17-2010")));
        assertEquals("8-18-2010", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "8-18-2010")));
        assertEquals("8-19-2010", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "8-19-2010")));
        assertEquals("9-13-2010", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "9-13-2010")));
        assertEquals("9-14-2010", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "9-14-2010")));
        assertEquals("9-15-2010", null,
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "9-15-2010")));
    }

    public void test_validateDate_forwardDateRange() {
        Dataset properties = new Dataset("errorMessage", "<failed>",
                "dateFormat", "m-d-Y", "exclude",
                DateFormElement.parseFilter("9/5/2011:")
        );

        assertEquals("9-4-201", null,
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "9-4-2011")));
        assertEquals("9-5-2011", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "9-5-2011")));
        assertEquals("9-6-2011", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "9-6-2011")));
    }

    public void test_validateDate_annualDate() {
        Dataset properties = new Dataset("errorMessage", "<failed>",
                "dateFormat", "m-d-Y", "exclude",
                DateFormElement.parseFilter("1/12")
        );

        assertEquals("1-12-2009", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "1-12-2009")));
        assertEquals("1-12-2011", "<failed>",
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "1-12-2011")));
        assertEquals("1-2-2011", null,
                DateFormElement.validateDate("elem1", properties,
                new Dataset("elem1", "1-2-2011")));
    }
}
