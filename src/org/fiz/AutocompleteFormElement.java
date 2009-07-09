package org.fiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import org.fiz.TreeSection.PageProperty;

/**
 * The DateFormElement allows users to input time and date either manually (in a
 * variety of formats) or through a customizable, JS-driven calendar object. It
 * supports the following properties: class: (optional) Class attribute to use
 * for the <div> containing this element; defaults to CheckboxFormElement. id:
 * (required) Name for this FormElement; must be unique among all ids for the
 * page. This is used as the name for the data value in query and update
 * requests and also as the {@code name} attribute for the HTML input element.
 * label: (optional) Template for label to display next to the checkbox to
 * identify the element for the user. attachPosition: (optional) Defaults to
 * BOTTOM. Define whether the calendar pops up to the RIGHT or to the BOTTOM of
 * the input field dateFormat: (optional) Defaults to m/d/Y. Specifies the
 * format of the date in the input field. The following specifiers may be used:
 * d: day without leading 0s (1, 2, ... , 30, 31) D: day with leading 0s (01,
 * 02, ... , 30, 31) m: month without leading 0s (1, 2, ... , 11, 12) M: month
 * with leading 0s (01, 02, ... , 11, 12) y: year in two-digit form (88, 89, ...
 * , 01, 02) Y: year in four-digit form (1988, 1989, ... , 2001, 2002)
 */
public class AutocompleteFormElement extends FormElement implements DirectAjax {

	public static final int BOTTOM = 0;
	public static final int RIGHT = 1;
	public static final String[] WEEK_SHORT = { "S", "M", "Tu", "W", "Th", "F",
	"S" };

	protected static class PageProperty implements Serializable {
		protected String id;
		protected String requestFactory;

		public PageProperty(String id, String requestFactory) {
			this.id = id;
			this.requestFactory = requestFactory;
		}
	}

	private DataRequest dataRequest;
	protected PageProperty pageProperty;

	/**
	 * Construct a DateFormElement from a set of properties that define its
	 * configuration.
	 * 
	 * @param properties
	 *            Dataset whose values are used to configure the element. See
	 *            the class documentation above for a list of supported values.
	 */
	public AutocompleteFormElement(Dataset properties) {
		super(properties);
		pageProperty = new PageProperty(properties.get("id"), properties.get("requestFactory"));
	}

	/**
	 * Construct a DateFormElement from an identifier and label.
	 * 
	 * @param id
	 *            Value for the element's {@code id} property.
	 * @param label
	 *            Value for the element's {@code label} property.
	 */
	public AutocompleteFormElement(String id, String label) {
		this(new Dataset("id", id, "label", label));
	}

	/**
	 * This method is invoked during the first phase of rendering a page;
	 * it is used to create a data request that will provide information about
	 * the top level of the tree.
	 * @param cr                   Overall information about the client
	 *                             request being serviced.
	 */
	@Override
	public void addDataRequests(ClientRequest cr, boolean empty) {
		dataRequest = (DataRequest) Util.invokeStaticMethod(
				pageProperty.requestFactory, "");
		cr.addDataRequest(dataRequest);
	}

	/**
	 * 
	 */
	public static void ajaxQuery(ClientRequest cr) {
		Dataset main = cr.getMainDataset();
		PageProperty pageProperty = (PageProperty)
				cr.getPageProperty(main.get("id"));

		DataRequest request = (DataRequest) Util.invokeStaticMethod(
				pageProperty.requestFactory, main.get("query"));

		Dataset data = request.getResponseData();
		StringBuilder js = new StringBuilder();
		data.toJavascript(js);
		
		StringBuilder javascript = new StringBuilder();
		Template.expand("Fiz.ids.@(1).showChoices();\n"
				+ "Fiz.ids.@(1).setDataset(@(2));\n", javascript,
				Template.SpecialChars.NONE, pageProperty.id, js);
		cr.evalJavascript(javascript);
	}

	/**
	 * This method is invoked to generate HTML for this form element.
	 * 
	 * @param cr
	 *            Overall information about the client request being serviced.
	 * @param data
	 *            Data for the form (a CompoundDataset including both form data,
	 *            if any, and the global dataset).
	 * @param out
	 *            Generated HTML is appended here.
	 */
	@Override
	public void render(ClientRequest cr, Dataset data, StringBuilder out) {
        cr.setPageProperty(pageProperty.id, pageProperty);
        
		Ajax.invoke(cr, "/AutocompleteFormElement/ajaxQuery?"
				+ "id=@1&"
				+ "query=\" + document.getElementById('@(2)_field').value + \"",
				id, properties.check("id"));
		Template.expand(
				"<div class=\"@class?{AutocompleteFormElement}\" id=\"@id\">",
				properties, out);
		Template.expand("<input type=\"hidden\" name=\"@id\" id=\"@(id)_hidden\" />",
				properties, out);
		Template.expand("<input type=\"text\" id=\"@(id)_field\""
				+ "onkeyup=\"Fiz.ids.@id.fetchResult(event)\" "
				+ "onblur=\"Fiz.ids.@id.hideChoices()\" "
				+ "{{value=\"@1\"}} />"
				+ "<ul id=\"@(id)_dropdown\" class=\"dropdown\"></ul>",
				properties, out, data.check(id));
		out.append("</div>");

        // Generate a Javascript object containing information about the form.
        cr.evalJavascript("Fiz.ids.@id = new Fiz.AutocompleteFormElement(\"@id\");\n",
        				  properties);
		cr.getHtml().includeCssFile("AutocompleteFormElement.css");
		cr.getHtml().includeJsFile("static/fiz/AutocompleteFormElement.js");
	}
}
