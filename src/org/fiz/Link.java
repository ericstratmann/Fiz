package org.fiz;

/**
 * The Link class displays text or an image in a "hot" form where clicking
 * on the text/image causes one of the following things to occur:
 *   * Display the page at a given URL.  This is the most common form;
 *     it produces a standard HTML link ({@code <a href=...}).
 *   * Invoke an Ajax request.
 *   * Invoke arbitrary Javascript code.
 * Links support the following properties; most of them are templates, which
 * are expanded in the context of the dataset passed to {@code html}.
 * text -       (optional) Template for text and/or other HTML to display
 *              for the Link.
 * url -        (optional) Template for URL that will be visited when the
 *              text or icon is clicked.  May include query values.  Note:
 *              either this property or {@code ajaxUrl} or {@code javascript}
 *              should be specified.
 * ajaxUrl -    (optional) If this property is specified, clicking on the
 *              text or image will invoke an Ajax request; the value of this
 *              property is a template for the URL of the Ajax request,
 *              expanded in the same way as {@code url}.  This property is
 *              ignored if {@code url} is specified.
 * javascript - (optional) If this property specified, clicking on the text
 *              or image will cause the value of this property to be invoked
 *              as Javascript code.  The property value is a template,
 *              expanded using Javascript string quoting, and must end with
 *              ";".  This property is ignored if {@code url} or
 *              {@code ajaxUrl} is specified.
 * iconUrl -    (optional) URL for an image to display to the left of the
 *              text.  This is also a template.
 * alt -        (optional) Template for the {@code alt} attribute for the
 *              icon.  Defaults to an empty string.
 * confirm -    (optional) If specified, the user will be asked to
 *              confirm before the URL is visited, and this text is a
 *              template for the confirmation message (passed to the
 *              Javascript{@code confirm} function).
 */

public class Link implements Formatter {
    /**
     * Instances of this enum are used to indicate how to display a
     * Link.
     */
    public enum DisplayForm {
        /** Render only the text form of the link. */
        TEXT,

        /** Render only the iconic form of the link. */
        ICON,

        /** Render both the text and the icon, with the icon on the left. */
        BOTH}

    // The following variables hold configuration information passed
    // to the constructor.  The names and meanings of these variables
    // are the same as described in the configuration option documentation
    // above.  If an option is omitted, the corresponding variable will
    // have a null value.
    protected String text, url, ajaxUrl, javascript, iconUrl, alt, confirm;

    // Copy of the constructor argument by the same name.
    DisplayForm displayForm;

    /**
     * Construct a Link object with a given set of properties and a
     * particular display form.
     * @param properties           A collection of values describing the
     *                             configuration of the Link; see above for
     *                             the supported values.
     * @param displayForm          Indicates whether to render the link's text,
     *                             icon, or both.
     */
    public Link(Dataset properties, DisplayForm displayForm) {
        text = properties.check("text");
        url = properties.check("url");
        ajaxUrl = properties.check("ajaxUrl");
        javascript = properties.check("javascript");
        iconUrl = properties.check("iconUrl");
        alt = properties.check("alt");
        confirm = properties.check("confirm");
        this.displayForm = displayForm;
    }

    /**
     * Construct a Link object with a given set of properties (the
     * display form defaults to {@code BOTH}).
     * @param properties           A collection of values describing the
     *                             configuration of the Link; see above for
     *                             the supported values
     */
    public Link(Dataset properties) {
        this(properties, DisplayForm.BOTH);
    }

    /**
     * Construct a Link using a named dataset within the {@code links}
     * configuration dataset.
     * @param name                 Name of a nested dataset within the
     *                             "links" configuration dataset.
     * @param displayForm          Indicates whether to render the link's text,
     *                             icon, or both.
     */
    public Link(String name, DisplayForm displayForm) {
        this(Config.getDataset("links").getChild(name), displayForm);
    }

    /**
     * Construct a Link using a named dataset within the {@code links}
     * configuration dataset.  The display form will be {@code BOTH}.
     * @param name                 Name of a nested dataset within the
     *                             "links" configuration dataset.
     */
    public Link(String name) {
        this(Config.getDataset("links").getChild(name), DisplayForm.BOTH);
    }

    /**
     * Generates HTML for a Link, using properties passed to the Link
     * constructor and data passed into this method.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Values in this dataset are used to
     *                             expand templates from the properties.
     * @param out                  HTML for the Link is appended here.
     */
    public void html(ClientRequest cr, Dataset data, StringBuilder out) {
        boolean displayText = (displayForm != DisplayForm.ICON)
                && (text != null);
        boolean displayIcon = (displayForm != DisplayForm.TEXT)
                && (iconUrl != null);
        if (!displayText && !displayIcon) {
            // Nothing to display, so return immediately.
            return;
        }

        // Generate the <a> element, including an event handler for
        // confirmation, if requested.
        if (url != null) {
            // Normal href.
            Template.expand("<a href=\"@1\"", out, Template.expand(url,
                    data, Template.SpecialChars.URL));
        } else {
            StringBuilder expandedJs;
            if (ajaxUrl != null) {
                // AJAX invocation.
                expandedJs = Ajax.invoke(cr, ajaxUrl, data);
            } else {
                // Javascript code.
                expandedJs = new StringBuilder(javascript.length());
                Template.expand(javascript, data, expandedJs,
                        Template.SpecialChars.JAVASCRIPT);
            }
            Template.expand("<a href=\"#\" onclick=\"@1 return false;\"",
                    out, expandedJs);
        }
        if (confirm != null) {
            confirmHtml(confirm, data, out);
        }
        out.append(">");

        // Generate the icon and/or text.
        if (displayText && displayIcon) {
            // Display both image and text.  Use a table for this, because
            // it allows the text to wrap cleanly (i.e. not under the image).
            out.append("<table class=\"Link\" cellspacing=\"0\">"
                    + "<tr><td>");
            iconHtml(cr, data, out);
            out.append("</td><td class=\"text\">");
            Template.expand(text, data, out);
            out.append("</td></tr></table>");
        } else if (displayText) {
            // Display only text.
            Template.expand(text, data, out);
        } else {
            // Display only an image.
            iconHtml(cr, data, out);
        }

        // Finish off the link.
        out.append("</a>");
    }

    /**
     * Generate an HTML <img> element for the link's icon.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Used for expanding "alt" template.
     * @param out                  HTML gets appended here.
     */
    protected void iconHtml(ClientRequest cr, Dataset data,
            StringBuilder out) {
        StringBuilder expandedUrl = new StringBuilder(iconUrl.length());
        Template.expand(iconUrl, data, expandedUrl, Template.SpecialChars.URL);
        out.append("<img class=\"Link\" src=\"");
        Html.escapeHtmlChars(expandedUrl, out);
        out.append("\" alt=\"");
        if (alt != null) {
            Template.expand(alt, data, out);
        }
        out.append("\" />");
    }

    /**
     * Generate an HTML attribute of the form onclick="..." for use in
     * the Link's <a> element;  this attribute will cause a confirmation
     * dialog to be posted when the link is clicked.
     * @param template             Template for the confirmation message.
     * @param data                 Dataset for expanding the template.
     * @param out                  HTML gets appended here.
     */
    protected void confirmHtml(String template, Dataset data,
            StringBuilder out) {
        StringBuilder message = new StringBuilder();

        // The quoting here is tricky.  First expand the confirmation
        // message template with no quoting, then perform string quoting
        // on the entire message.  Then generate the full block of
        // Javascript code, and finally HTML-quote that entire block.
        // Thus the contents of the message get quoted twice.
        Template.expand(template, data, message, Template.SpecialChars.NONE);
        StringBuilder code = new StringBuilder("if (!confirm(\"");
        Html.escapeStringChars(message.toString(), code);
        code.append("\") {return false;}");
        out.append(" onclick=\"");
        Html.escapeHtmlChars(code.toString(), out);
        out.append("\"");
    }
}
