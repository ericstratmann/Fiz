package org.fiz;
import java.io.*;
import java.util.*;

/**
 * Html objects are used to generate HTML documents.  Each object encapsulates
 * the state of a document, including things such as the document's body
 * and CSS and Javascript files needed in the document.  It also provides
 * various utility methods that simplify the creation of documents, such as
 * methods for escaping special HTML characters.
 */

public class Html {
    // Contents of the document; see getBody for details.
    protected StringBuilder body = new StringBuilder();

    // Title for the document; see setTitle for details.
    protected String title = null;

    // The initial portion of all URI's referring to this Web application:
    // used to generate HTML for stylesheets and Javascript files.
    protected String contextPath;

    // The following field keeps track of all of the stylesheet files that
    // are to be included in the HTML document.
    protected TreeSet<String> stylesheets = new TreeSet<String>();

    /**
     * Constructor for Html objects.
     * @param contextPath             The "root URI" corresponding to this
     *                                Web application.
     */
    public Html(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Returns the StringBuilder object used to assemble the main body of
     * the HTML document.  Typically, information is appended to this
     * object, but the caller can manipulate it in arbitrary ways.
     * @return                        StringBuilder object; the contents of
     *                                this object will eventually appear
     *                                between the <body> and </body> tags
     *                                in the final HTML document.
     */
    public StringBuilder getBody() {
        return body;
    }

    /**
     * This method generates and returns the information that appears at the
     * beginning of the HTML document; this can include a <?xml> element,
     * a <!DOCTYPE> element, and the <html> element, but nothing after the
     * <html> element.  Subclasses can override this method if they want to
     * supply a custom prologue.
     * @return                        Prologue text for the HTML document.
     */
    public String getPrologue() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
                + "Strict//EN\"\n"
                + "        \"http://www.w3.org/TR/xhtml1/DTD/"
                + "xhtml1-strict.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                + "xml:lang=\"en\" lang=\"en\">";
    }

    /**
     * Returns the current title text for the document (this text will
     * appear between <title> and </title> in the document header).
     * @return                        Current title text for the document;
     *                                null means no title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title text for the document (this text will appear between
     * <title> and </title> in the document header).
     * @param title                   New title text for the document; null
     *                                means document will have no title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * This method is used to request that the HTML document reference
     * a particular stylesheet.
     * @param uri                     Application-relative URI for the
     *                                stylesheet; it typically begins with
     *                                "fiz/css/", in which case the stylesheet
     *                                is a dynamically generated one whose
     *                                template is stored under WEB-INF/css.
     */
    public void includeCss(String uri) {
        stylesheets.add(uri);
    }

    /**
     * Generates a complete HTML document from the information that has been
     * provided so far and writes it on a given Writer.  If no information
     * has been provided for the HTML since the last reset, then no output
     * whatsoever is generated.  Note: this method ignores I/O errors; it
     * assumes that the Writer does not actually generate exceptions even
     * though the interface allows it.
     * @param writer                  Where to write the HTML document.
     *                                Must be a subclass of Writer that does
     *                                not actually generate exceptions.
     */
    public void print(Writer writer) {
        if ((title == null) && (body.length() == 0)) {
            return;
        }
        try {
            writer.write(getPrologue());
            writer.write("<head>\n");
            if (title != null) {
                writer.write("    <title>" + title + "</title>\n");
            }

            // Output stylesheet links.
            stylesheets.add("fiz/css/main.css");
            for (String sheet : stylesheets) {
                writer.write("    <link href=\"" + contextPath + "/" + sheet
                        + "\" rel=\"stylesheet\" type=\"text/css\" />\n");
            }

            // Output body.
            writer.write("</head>\n<body>\n");
            writer.write(body.toString());
            writer.write("</body>\n</html>\n");
        }
        catch (IOException e) {
            // Ignore exceptions here.  Exceptions shouldn't happen in
            // practice anyway, since normal usage is through a PrintWriter
            // or StringWriter and neither of these generates exceptions.
        }
    }

    /**
     * Generates a complete HTML document from the information that has been
     * provided so far, and returns it in a String.
     * @return                        The HTML document.
     */
    public String toString() {
        StringWriter result = new StringWriter();
        print(result);
        return result.toString();
    }

    /**
     * Clears all information that has been specified for the HTML, restoring
     * the object to its initial empty state.
     */
    public void reset() {
        title = null;
        body.setLength(0);
    }

    /**
     * This method is invoked to replace characters that are special in
     * HTML (<, >, &, ") with HTML entity references (&lt;, &gt;, &amp;,
     * and &quot;, respectively); this allows arbitrary data to be
     * included in HTML without accidentally invoking special HTML
     * behavior.
     * @param s                       Input string; may contain arbitrary
     *                                characters
     * @param out                     The contents of <code>s</code> are
     *                                copied here, replacing special characters
     *                                with entity references
     */
    public static void escapeHtmlChars(String s, StringBuilder out) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"') {
                out.append("&quot;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '<') {
                out.append("&lt;");
            } else {
                out.append(c);
            }
        }
    }

    /**
     * This method transforms a string into a form that may be used in
     * URLs (such as for query values).  It does this by replacing
     * unusual characters with %xx-sequences as defined by RFC1738.  It
     * also converts non-ASCII characters to UTF-8 before encoding, as
     * recommended in  http://www.w3.org/International/O-URL-code.html.
     * @param s                       Input string; may contain arbitrary
     *                                characters
     * @param out                     The contents of <code>s</code> are
     *                                copied here after converting to UTF-8
     *                                and converting nonalphanumeric
     *                                characters to %xx sequences.
     */
    public static void escapeUrlChars(String s, StringBuilder out) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'a') && (c <= 'z') || (c >= 'A') && (c <= 'Z')
                    || (c >= '0') && (c <= '9') || (c == '.') || (c == '-')) {
                out.append(c);
            } else if (c <= 0x7f) {
                out.append(urlCodes[c]);
            } else if (c <= 0x7ff) {
                out.append(urlCodes[0xc0 | (c >> 6)]);
                out.append(urlCodes[0x80 | (c & 0x3f)]);
            } else {
                out.append(urlCodes[0xe0 | (c >> 12)]);
                out.append(urlCodes[0x80 | ((c >> 6) & 0x3f)]);
                out.append(urlCodes[0x80 | (c & 0x3f)]);
            }
      }
    }

    // The following array is used by escapeUrlChars to map from character
    // values 0-255 to the corresponding URL-encoded values.
    final static String[] urlCodes = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
        "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
          "+", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
        "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
        "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
        "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
        "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
        "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
        "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
        "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
        "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
        "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
        "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
        "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
        "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
        "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
        "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
        "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
        "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
        "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
        "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
        "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
        "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
        "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
        "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
        "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
        "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
    };

    /**
     * This method transforms a string into a form that may be used safely
     * in a string literal for Javascript and many other languages.  For
     * example, if <code>s</code> is a\x"z then it gets escaped to
     * a\\x\"z so that the original value will be regenerated when
     * Javascript evaluates the string literal.
     * @param s                       Value that is to be encoded in a string
     *                                literal.
     * @param out                     A converted form of <code>s</code>
     *                                is copied here; if this information
     *                                is used in a Javascript string, it
     *                                will evaluate to exactly the characters
     *                                in  <code>s</code>.
     */
    public static void escapeStringChars(String s, StringBuilder out) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c <= '\037') {
                if (c == '\n') {
                    out.append("\\n");
                } else if (c == '\t') {
                    out.append("\\t");
                } else {
                    out.append(String.format("\\x%02x", (int) c));
                }
            } else if (c == '\\') {
                out.append("\\\\");
            } else if (c == '\"') {
                out.append("\\\"");
            } else if (c == '\177') {
                out.append("\\x3f");
            } else {
                out.append(c);
            }
        }
    }
}
