package org.fiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import org.fiz.TreeSection.PageProperty;

/**
 * The DateFormElement allows users to input time and date either manually (in a
 * variety of formats) or through a customizable, JS-driven calendar object. It
 * supports the following properties:
 *   class: 			(optional) Class attribute to use for the <div>
 *   					containing this element; defaults to AutocompleteFormElement.
 *   id:				(required) Name for this FormElement; must be unique
 *   					among all ids for the page. This is used as the name
 *   					for the data value in query and update requests and also
 *   					as the {@code name} attribute for the HTML input element.
 *   label: (optional)	Template for label to display next to the input field to
 * 						identify the element for the user.
 *   attachPosition:	(optional) Defaults to {@code bottom}. Define whether
 *   					the calendar pops up to the {@code right} or to the
 *   					{@code bottom} of the input field.
 */
public class AutocompleteFormElement extends FormElement implements DirectAjax {

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
	 * Construct a AutocompleteFormElement from a set of properties that define its
	 * configuration.
	 * 
	 * @param properties
	 *            Dataset whose values are used to configure the element. See
	 *            the class documentation above for a list of supported values.
	 */
	public AutocompleteFormElement(Dataset properties) {
		super(properties);
		pageProperty = new PageProperty(properties.get("id"),
				properties.get("requestFactory"));
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
	 * This method is an Ajax entry point, invoked to fetch the autocomplete
	 * results for a query in an AutocompleteFormElement.
	 * @param cr					Overall information about the client
	 * 								request being serviced; there must
	 * 								be a {@code query} value in the main
	 * 								dataset, which is the value for which
	 * 								we are trying to autocomplete. 
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
	 * @param cr				Overall information about the client
	 * 							request being serviced.
	 * @param data				Data for the form (a CompoundDataset
	 * 							including both form data, if any, and
	 * 							the global dataset).
	 * @param out				Generated HTML is appended here.
	 */
	@Override
	public void render(ClientRequest cr, Dataset data, StringBuilder out) {
        cr.setPageProperty(pageProperty.id, pageProperty);
        cr.setAuthToken();
        
		Template.expand("\n<!-- Start AutocompleteFormElement @id -->\n" +
				"<div class=\"@class?{AutocompleteFormElement}\" id=\"@id\">\n",
				properties, out);
		Template.expand("  <input type=\"hidden\" name=\"@id\" id=\"@(id)_hidden\" />\n",
				properties, out);
		Template.expand("  <input type=\"text\" id=\"@(id)_input\""
				+ "onkeyup=\"Fiz.ids.@id.fetchResult(event)\" "
				+ "onblur=\"Fiz.ids.@id.hideChoices()\" "
				+ "{{value=\"@1\"}} />\n"
				+ "  <ul id=\"@(id)_dropdown\" class=\"dropdown\"></ul>\n",
				properties, out, data.check(id));
		out.append("</div>\n");
		Template.expand("<!-- End AutocompleteFormElement @id -->\n", properties, out);

        // Generate a Javascript object containing information about the form.
        cr.evalJavascript("Fiz.ids.@id = new Fiz.AutocompleteFormElement(\"@id\", @numResults?{5});\n",
        				  properties);
		cr.getHtml().includeCssFile("AutocompleteFormElement.css");
		cr.getHtml().includeJsFile("static/fiz/AutocompleteFormElement.js");
	}
}
