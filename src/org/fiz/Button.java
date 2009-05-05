package org.fiz;

/**
 * The Button class displays an HTML {@code <button>} element that invokes an
 * AJAX request or Javascript code when clicked.  Buttons support the
 * following properties; most of them are templates, which are expanded
 * in the context of the dataset passed to {@code html}.
 *   class:          (optional) Used as the {@code class} attribute for
 *                   the HTML table that displays the FormSection.
 *                   Defaults to {@code FormSection}.
 *   text -          (required) Template for text and/or other HTML to
 *                   display for the Button.
 *   ajaxUrl -       (optional) If this property is specified, clicking on
 *                   the button will invoke an Ajax request; the value of
 *                   this property is a template for the URL of the Ajax
 *                   request.
 *   javascript -    (optional) If this property specified, clicking on the
 *                   button will cause the value of this property to be invoked
 *                   as Javascript code.  The property value is a template,
 *                   expanded using Javascript string quoting, and must end
 *                   with ";".  This property is ignored if {@code ajaxUrl}
 *                   is specified.
 */

public class Button implements Formatter {
    // The following variable is a copy of the constructor argument
    // by the same name.
    protected Dataset properties;

    /**
     * Construct a Button.
     * @param properties           Contains configuration information
     *                             for the Button; see description above.
     */
    public Button(Dataset properties) {
        this.properties = properties;
    }

    /**
     * Generates HTML for a Button.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Values in this dataset are used to
     *                             expand templates from the properties.
     * @param out                  HTML for the Button is appended here.
     */
    @Override
    public void render(ClientRequest cr, Dataset data, StringBuilder out) {
        Button.render(cr, properties, data, out);
    }

    /**
     * Generates HTML for a Button.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param properties           Configuration information for the Button;
     *                             see description above.
     * @param data                 Values in this dataset are used to
     *                             expand templates from the properties.
     * @param out                  HTML for the Button is appended here.
     */
    public static void render(ClientRequest cr,  Dataset properties,
            Dataset data, StringBuilder out) {
        StringBuilder javascript;
        String ajaxUrl = properties.check("ajaxUrl");
        if (ajaxUrl != null) {
            javascript = Ajax.invoke(cr, ajaxUrl, data);
        } else {
            String jsTemplate = properties.get("javascript");
            javascript = new StringBuilder(jsTemplate.length());
            Template.expand(jsTemplate, data, javascript,
                    Template.SpecialChars.JAVASCRIPT);
        }
        Template.expand("<button {{class=\"@class\"}} " +
                "onclick=\"@1 return false;\">",
                properties, out, javascript);
        Template.expand(properties.get("text"), data, out);
        out.append("</button>");
    }
}
