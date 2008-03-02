/**
 * This class provides facilities for rendering HTML links (<a href=...),
 * combining data from a variety of sources.  Links support the following
 * properties:
 * text -       (optional) Text and/or other HTML to display for the Link;
 *              this is a template that is expanded using the data available
 *              when the Link is rendered.
 * icon -       (optional) URL for an image to display to the left of the text.
 * alt -        (optional) "Alt" string to include in the icon.  Defaults
 *              to an empty string.
 * base -       Template for base URL that will be visited when the text or
 *              icon is clicked (extended with arguments specified by "args").
 *              May contain some initial query values.  Must contain only
 *              proper URL characters: no escaping will be done on this
 *              value.
 * args -       (optional) Describes query information to be added to the
 *              URL; contains one or more specifiers separated by commas.
 *              Specifiers can have a long form "queryName: dataName"
 *              or a short form "name".  For example, "foo: bar"
 *              means find the data value named "bar" and use its value
 *              for the query option "foo".  The short form "foo" is
 *              the same as "foo: foo".
 * confirm -    (optional) If specified, the user will be asked to
 *              confirm before the URL is visited, and this text is a
 *              template for the confirmation message.  The template
 *              is expanded in the context of the data available at
 *              render-time.
 */

package org.fiz;

public class Link {
    /**
     * Instances of this enum are used to indicate how to display a
     * Link:
     * <p>
     * TEXT: render only the text form of the link
     * <p>
     * ICON: render only the iconic form of the link
     * <p>
     * BOTH: render both the text and the icon, with the icon on the left
     * and the text on the right.
     */
    public enum DisplayForm {TEXT, ICON, BOTH}

    // The following variables hold configuration information passed
    // to the constructor.  The names and meanings of these variables
    // are the same as described in the configuration option documentation
    // above.  If an option is omitted, the corresponding variable will
    // have a null value.
    protected String text, icon, alt, base, confirm;
    protected String[] queryNames, queryData;

    // Copy of the constructor argument by the same name.
    DisplayForm displayForm;

    /**
     * Constructor for Link objects.
     * @param properties           A collection of values describing the
     *                             configuration of the Link; see above for
     *                             the supported values
     * @param displayForm          Indicates whether to render the link's text,
     *                             icon, or both
     */
    public Link(Dataset properties, DisplayForm displayForm) {
        text = properties.check("text");
        icon = properties.check("icon");
        alt = properties.check("alt");
        base = properties.get("base");
        String args = properties.check("args");
        if (args != null) {
            queryNames = Util.split(args, ',');

            // QueryNames is currently in an intermediate form (some of the
            // values may contain colons).  Make another pass-through
            // queryNames to split up the values that contain colons and
            // generate the queryData values.

            queryData = new String[queryNames.length];
            for (int i = 0; i < queryNames.length; i++) {
                String spec = queryNames[i];
                int colon = spec.indexOf(':');
                if (colon < 0) {
                    // Only one name, so queryNames[i] is already in the
                    // correct form.
                    queryData[i] = spec;
                } else {
                    // Two names: separate them and ignore spaces after the
                    // colon.
                    queryData[i] = spec.substring(
                            Util.skipSpaces(spec, colon+1));
                    queryNames[i] = spec.substring(0, colon);
                }
            }
        } else {
            queryNames = queryData = new String[0];
        }
        confirm = properties.check("confirm");
        this.displayForm = displayForm;
    }

    /**
     * Constructor for Link objects; in this constructor the display form
     * defaults to BOTH.
     * @param properties           A collection of values describing the
     *                             configuration of the Link; see above for
     *                             the supported values
     */
    public Link(Dataset properties) {
        this(properties, DisplayForm.BOTH);
    }

    /**
     * Generates HTML for a Link, using properties passed to the Link
     * constructor and data passed into this method.
     * @param data                Values in this dataset are used when
     *                            expanding templates from the properties.
     * @param out                 HTML for the Link is appended here.
     */
    public void html(Dataset data, StringBuilder out) {
        boolean displayText = (displayForm != DisplayForm.ICON)
                && (text != null);
        boolean displayIcon = (displayForm != DisplayForm.TEXT)
                && (icon != null);
        if (!displayText && !displayIcon) {
            // Nothing to display, so return immediately.
            return;
        }

        // Generate the URL, which consists of the base plus some number of
        // query arguments.
        StringBuilder url = new StringBuilder(30);
        String separator = "?";
        Template.expand(base, data, url, Template.Encoding.NONE);
        if (url.indexOf("?") >= 0) {
            separator = "&amp;";
        }
        for (int i = 0; i < queryNames.length; i++) {
            url.append(separator);
            Util.escapeUrlChars(queryNames[i], url);
            url.append('=');
            Util.escapeUrlChars(data.get(queryNames[i]), url);
            separator = "&amp;";
        }

        // Generate the <a> element, including an event handler for
        // confirmation, if requested.
        out.append("<a href=\"");
        out.append(url);
        if (confirm != null) {
            out.append("\" ");
            confirmHtml(confirm, data, out);
            out.append(">");
        } else {
            out.append("\">");
        }

        // Generate the icon and/or text.
        if (displayText && displayIcon) {
            // Display both image and text.  Use a table for this, because
            // it allows the text to wrap cleanly (i.e. not under the image).
            out.append("<table class=\"link_table\" cellspacing=\"0\">"
                    + "<tr><td>");
            iconHtml(data, out);
            out.append("</td><td class=\"link_text\">");
            Template.expand(text, data, out);
            out.append("</td></tr></table>");
        } else if (displayText) {
            // Display only text.
            Template.expand(text, data, out);
        } else {
            // Display only an image.
            iconHtml(data, out);
        }

        // Finish off the link.
        out.append("</a>");
    }

    /**
     * Generates an HTML <img> element for the link's icon.
     * @param data                Used for expanding "alt" template.
     * @param out                 HTML gets appended here.
     */
    protected void iconHtml(Dataset data, StringBuilder out) {
        out.append("<img class=\"link_image\" src=\"");
        out.append(icon);
        out.append("\" alt=\"");
        if (alt != null) {
            Template.expand(alt, data, out);
        }
        out.append("\" />");
    }

    /**
     * Generates an HTML attribute of the form onclick="..." for use in
     * the Link's <a> element;  this attribute will cause a confirmation
     * dialogue to be posted when the link is clicked on.
     * @param template            Template for the confirmation message.
     * @param data                Dataset for expanding the template.
     * @param out                 HTML gets appended here.
     */
    protected void confirmHtml(String template, Dataset data,
            StringBuilder out) {
        StringBuilder message = new StringBuilder();

        // The quoting here is tricky.  First expand the confirmation
        // message template with no quoting, then perform string quoting
        // on the entire message.  Then generate the full block of
        // Javascript code, and finally HTML-quote that entire block.
        // Thus the contents of the message get quoted twice.
        Template.expand(template, data, message, Template.Encoding.NONE);
        StringBuilder code = new StringBuilder("if (!confirm(\"");
        Util.escapeStringChars(message.toString(), code);
        code.append("\") {return false;}");
        out.append("onclick=\"");
        Util.escapeHtmlChars(code.toString(), out);
        out.append("\"");
    }
}
