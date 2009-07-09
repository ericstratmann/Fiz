package org.fiz;

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
    
    public void test_render_basics() {
        DateFormElement.today = createDate(1, 14, 2008);
        DateFormElement element = new DateFormElement(
                new Dataset("id", "cal1", "label", "Calendar"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start DateFormElement cal1 -->\n" +
                "<div class=\"DateFormElement\" id=\"cal1\">\n" + 
                "  <input type=\"text\" name=\"cal1\" id=\"cal1_input\" onblur=\"Fiz.ids.cal1.validateAndUpdate()\" value=\"January 14, 2008\" />\n" +
                "  <img src=\"/static/fiz/images/calendar-icon.gif\" id=\"cal1_icon\" alt=\"Pick a date\" />\n" +
                "  <div id=\"cal1_picker\" class=\"picker\">\n" +
                "    <div id=\"cal1_header\" class=\"header\"></div>\n" +
                "    <a onclick=\"Fiz.ids.cal1.closePicker()\" class=\"close-button\">[x]</a>\n" +
                "    <table id=\"cal1_table\">\n" +
                "      <col class=\"col-Su\" />\n" +
                "      <col class=\"col-M\" />\n" +
                "      <col class=\"col-Tu\" />\n" +
                "      <col class=\"col-W\" />\n" +
                "      <col class=\"col-Th\" />\n" +
                "      <col class=\"col-F\" />\n" +
                "      <col class=\"col-Sa\" />\n" +
                "      <thead>\n" +
                "        <tr><th>Su</th><th>M</th><th>Tu</th><th>W</th><th>Th</th><th>F</th><th>Sa</th></tr>\n" +
                "      </thead>\n" +
                "      <tbody id=\"cal1_grid\">\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "    <div id=\"cal1_navigation\" class=\"nav\">\n" +
                "      <a onclick=\"Fiz.ids.cal1.prevYear()\" id=\"cal1_prevYear\" class=\"arrow-prev-year\"><img src=\"/static/fiz/images/arrow-left-double.gif\" alt=\"Previous Year\" /></a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.prevMonth()\" id=\"cal1_prevMonth\" class=\"arrow-prev-month\"><img src=\"/static/fiz/images/arrow-left.gif\" alt=\"Previous Month\" /></a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.today()\" class=\"arrow-today\">Today</a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.nextMonth()\" id=\"cal1_nextMonth\" class=\"arrow-next-month\"><img src=\"/static/fiz/images/arrow-right.gif\" alt=\"Next Month\" /></a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.nextYear()\" id=\"cal1_nextYear\" class=\"arrow-next-year\"><img src=\"/static/fiz/images/arrow-right-double.gif\" alt=\"Next Year\" /></a>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" + 
                "<!-- End DateFormElement cal1 -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\">\n");
        out.append("</form>\n");
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement('cal1', 'm/d/Y', 'bottom');\n",
                 cr.getHtml().jsCode.toString());
        TestUtil.assertSubstring("CSS file names", "DateFormElement.css",
                cr.getHtml().getCssFiles());
        assertEquals("Javascript file names",
                "static/fiz/DateFormElement.js, static/fiz/Fiz.js, static/fiz/date.js",
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
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement('cal1', 'm/d/Y', 'right');\n",
                 cr.getHtml().jsCode.toString());
    }
    
    public void test_render_dateFormat() {
        DateFormElement element = new DateFormElement(
                new Dataset(
                        "id", "cal1",
                        "label", "Calendar",
                        "dateFormat", "m-d-Y"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement('cal1', 'm-d-Y', 'bottom');\n",
                 cr.getHtml().jsCode.toString());
    }
    


    public void test_render_calendarIcon() {
        DateFormElement.today = createDate(1, 14, 2008);
        DateFormElement element = new DateFormElement(
                new Dataset(
                        "id", "cal1",
                        "label", "Calendar",
                        "calendarIcon", "test.jpg"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML", "\n" +
                "<!-- Start DateFormElement cal1 -->\n" +
                "<div class=\"DateFormElement\" id=\"cal1\">\n" + 
                "  <input type=\"text\" name=\"cal1\" id=\"cal1_input\" onblur=\"Fiz.ids.cal1.validateAndUpdate()\" value=\"January 14, 2008\" />\n" +
                "  <img src=\"test.jpg\" id=\"cal1_icon\" alt=\"Pick a date\" />\n" +
                "  <div id=\"cal1_picker\" class=\"picker\">\n" +
                "    <div id=\"cal1_header\" class=\"header\"></div>\n" +
                "    <a onclick=\"Fiz.ids.cal1.closePicker()\" class=\"close-button\">[x]</a>\n" +
                "    <table id=\"cal1_table\">\n" +
                "      <col class=\"col-Su\" />\n" +
                "      <col class=\"col-M\" />\n" +
                "      <col class=\"col-Tu\" />\n" +
                "      <col class=\"col-W\" />\n" +
                "      <col class=\"col-Th\" />\n" +
                "      <col class=\"col-F\" />\n" +
                "      <col class=\"col-Sa\" />\n" +
                "      <thead>\n" +
                "        <tr><th>Su</th><th>M</th><th>Tu</th><th>W</th><th>Th</th><th>F</th><th>Sa</th></tr>\n" +
                "      </thead>\n" +
                "      <tbody id=\"cal1_grid\">\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"even\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "        <tr class=\"odd\"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "    <div id=\"cal1_navigation\" class=\"nav\">\n" +
                "      <a onclick=\"Fiz.ids.cal1.prevYear()\" id=\"cal1_prevYear\" class=\"arrow-prev-year\"><img src=\"/static/fiz/images/arrow-left-double.gif\" alt=\"Previous Year\" /></a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.prevMonth()\" id=\"cal1_prevMonth\" class=\"arrow-prev-month\"><img src=\"/static/fiz/images/arrow-left.gif\" alt=\"Previous Month\" /></a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.today()\" class=\"arrow-today\">Today</a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.nextMonth()\" id=\"cal1_nextMonth\" class=\"arrow-next-month\"><img src=\"/static/fiz/images/arrow-right.gif\" alt=\"Next Month\" /></a>\n" +
                "      <a onclick=\"Fiz.ids.cal1.nextYear()\" id=\"cal1_nextYear\" class=\"arrow-next-year\"><img src=\"/static/fiz/images/arrow-right-double.gif\" alt=\"Next Year\" /></a>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" + 
                "<!-- End DateFormElement cal1 -->\n",
                out.toString());
        out.insert(0, "<form action=\"a/b/c\">\n");
        out.append("</form>\n");
        TestUtil.assertXHTML(cr.getHtml().toString());
    }
    
    public void test_render_exclude() {
        DateFormElement element = new DateFormElement(
                new Dataset(
                        "id", "cal1",
                        "label", "Calendar",
                        "dateFormat", "m-d-Y",
                        "exclude", "Saturday, 1/12/2005, 7/23," +
                        "8/13/2010:9/11/2010, :6/24/2009, 9/3/2011:"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = cr.getHtml().getBody();
        element.render(cr, new Dataset(), out);
        assertEquals("accumulated Javascript",
                "Fiz.ids.cal1 = new Fiz.DateFormElement('cal1', 'm-d-Y', 'bottom');\n" +
                "Fiz.ids.cal1.setFilters({filter: [{type: \"dayOfWeek\", dayOfWeek: \"6\"}, " +
                "{type: \"date\", date: \"January 12, 2005\"}, {month: \"7\", day: \"23\", " +
                "type: \"annualDate\"}, {startDate: \"August 13, 2010\", type: \"range\", " +
                "endDate: \"September 11, 2010\"}, {startDate: \"null\", type: \"range\", " +
                "endDate: \"June 24, 2009\"}, {startDate: \"September 3, 2011\", type: " +
                "\"range\", endDate: \"null\"}]});\n",
                 cr.getHtml().jsCode.toString());
    } 

    public void test_stringToDate() {
        DateFormElement.today = createDate(1, 14, 2008);
        assertNull(DateFormElement.stringToDate(""));
        assertTrue(createDate(1, 14, 2008).equals(
                DateFormElement.stringToDate("today")));
        assertTrue(createDate(6, 8, 2009).equals(
                DateFormElement.stringToDate("6/8/2009")));
        assertTrue(createDate(9, 14, 2009).equals(
                DateFormElement.stringToDate("8 months")));
        assertTrue(createDate(1, 17, 2008).equals(
                DateFormElement.stringToDate("3 days")));
        assertTrue(createDate(1, 14, 2010).equals(
                DateFormElement.stringToDate("2 years")));
    }

    public void test_dateToString() {
        assertEquals("January 14, 2008",
                DateFormElement.dateToString(createDate(1, 14, 2008)));
    }

    public void test_getDayOfWeek() {
        assertEquals(0, DateFormElement.getDayOfWeek("Sunday"));
        assertEquals(1, DateFormElement.getDayOfWeek("mon"));
        assertEquals(2, DateFormElement.getDayOfWeek("Tues"));
        assertEquals(3, DateFormElement.getDayOfWeek("wednesday"));
        assertEquals(4, DateFormElement.getDayOfWeek("Thursday"));
        assertEquals(5, DateFormElement.getDayOfWeek("fri"));
        assertEquals(6, DateFormElement.getDayOfWeek("sat"));
    }

    public void test_getToday() {
        DateFormElement.today = createDate(1, 14, 2008);
        assertTrue(createDate(1, 14, 2008).equals(DateFormElement.getToday()));
    }
}
