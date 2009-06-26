package org.fiz;

/**
 * The DateFormElement allows users to input time and date either
 * manually (in a variety of formats) or through a customizable, JS-driven
 * calendar object. It supports the following properties:
 *   class:          (optional) Class attribute to use for the <div>
 *                   containing this element; defaults to CheckboxFormElement.
 *   id:             (required) Name for this FormElement; must be unique
 *                   among all ids for the page.  This is used as the name
 *                   for the data value in query and update requests and
 *                   also as the {@code name} attribute for the HTML input
 *                   element.
 *   label:          (optional) Template for label to display next to the
 *                   checkbox to identify the element for the user.
 *   attachPosition: (optional) Defaults to BOTTOM. Define whether the calendar
 *   				 pops up to the RIGHT or to the BOTTOM of the input field
 *   dateFormat:	 (optional) Defaults to m/d/Y. Specifies the format of
 *   				 the date in the input field. The following specifiers
 *   				 may be used:
 *					 d: day without leading 0s (1, 2, ... , 30, 31)
 *					 D: day with leading 0s (01, 02, ... , 30, 31)
 *					 m: month without leading 0s (1, 2, ... , 11, 12)
 *					 M: month with leading 0s (01, 02, ... , 11, 12)
 *					 y: year in two-digit form (88, 89, ... , 01, 02)
 *					 Y: year in four-digit form (1988, 1989, ... , 2001, 2002)
 */
public class DateFormElement extends FormElement {

	public static final int BOTTOM = 0;
	public static final int RIGHT = 1;
	public static final String[] WEEK_SHORT = { "S", "M", "Tu", "W", "Th", "F", "S" };
	
	/**
     * Construct a DateFormElement from a set of properties that define
     * its configuration.
     * @param properties           Dataset whose values are used to configure
     *                             the element.  See the class documentation
     *                             above for a list of supported values.
     */
    public DateFormElement(Dataset properties) {
        super(properties);
    }

    /**
     * Construct a DateFormElement from an identifier and label.
     * @param id                   Value for the element's {@code id}
     *                             property.
     * @param label                Value for the element's {@code label}
     *                             property.
     */
    public DateFormElement(String id, String label) {
        this(new Dataset ("id", id, "label", label));
    }

    /**
     * This method is invoked to generate HTML for this form element.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data for the form (a CompoundDataset
     *                             including both form data, if any, and the
     *                             global dataset).
     * @param out                  Generated HTML is appended here.
     */
    @Override
    public void render(ClientRequest cr, Dataset data,
            StringBuilder out) {
        Template.expand("<div class=\"@class?{DateFormElement}\" id=\"@id\">", properties, out);
        Template.expand("<input type=\"text\" name=\"@id\" id=\"@(id)_field\"" +
                			"onselect=\"Fiz.ids.@id.openPicker()\" " +
                			"onfocus=\"Fiz.ids.@id.openPicker()\" " +
                			"onkeyup=\"Fiz.ids.@id.validateAndUpdate()\" " +
                			"{{value=\"@1\"}} />" +
                			"<img src=\"/static/fiz/images/calendar-icon.gif\" " +
                			"onclick=\"Fiz.ids.@id.openPicker()\" />",
                			properties, out, data.check(id));

        Template.expand("<div id=\"@(id)_picker\" class=\"picker\">" +
					    	"<div id=\"@(id)_header\" class=\"header\"></div>" +
					    	"<table id=\"@(id)_table\">", properties, out);

        for(String week : WEEK_SHORT) {
        	out.append("<col class=\"col-" + week + "\" />");
        }
        out.append("<thead><tr>");
        for(String week : WEEK_SHORT) {
        	out.append("<th>" + week + "</th>");
        }
        Template.expand("</tr></thead><tbody id=\"@(id)_grid\">", properties, out);
        for(int i = 0; i < 6; i++) {
        	if(i % 2 == 0) {
        		out.append("<tr class=\"even\">");
        	} else {
        		out.append("<tr class=\"odd\">");
        	}
        	for(int j = 0; j < 7; j++) {
        		out.append("<td></td>");
        	}
        	out.append("</tr>");
        }
        out.append("</tbody></table>");
        Template.expand("<div id=\"@(id)_navigation\" class=\"nav\">" +
	    		"<a onclick=\"Fiz.ids.@id.prevYear()\" class=\"arrow-prev-year\">" + 
	    		"<img src=\"/static/fiz/images/arrow-left-double.gif\" ></a>" +
	    		"<a onclick=\"Fiz.ids.@id.prevMonth()\" class=\"arrow-prev-month\">" +
	    		"<img src=\"/static/fiz/images/arrow-left.gif\" ></a>" +
	    		"<a onclick=\"Fiz.ids.@id.today()\" class=\"arrow-today\">Today</a>" +
	    		"<a onclick=\"Fiz.ids.@id.nextMonth()\" class=\"arrow-next-month\">" +
	    		"<img src=\"/static/fiz/images/arrow-right.gif\" ></a>" +
	    		"<a onclick=\"Fiz.ids.@id.nextYear()\" class=\"arrow-next-year\">" +
	    		"<img src=\"/static/fiz/images/arrow-right-double.gif\" ></a>" +
    		"</div>", properties, out);
        out.append("</div></div>");

        // Generate a Javascript object containing information about the form.
        cr.evalJavascript("Fiz.ids.@id = new Fiz.DateFormElement(\"@id\");\n" +
        				  "{{Fiz.ids.@id.attachPosition = @attachPosition;\n}}" +
        				  "{{Fiz.ids.@id.dateFormat = '@dateFormat';\n}}" +
        				  "Fiz.ids.@id.field.value = Fiz.DateFormElement.formatDate(" +
        				  "new Date(Fiz.ids.@id.field.value), '@dateFormat?{'m/d/Y'}');\n",
        				  properties);
        cr.getHtml().includeCssFile("DateFormElement.css");
        cr.getHtml().includeJsFile("static/fiz/DateFormElement.js");
    }
}
