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

    // The initial portion of all URL's referring to this Web application:
    // used, among other things to generate URL's for Javascript files.
    protected String contextPath;

    // The following field keeps track of all of the stylesheet files that
    // have already been included in the HTML document.
    protected HashSet<String> cssFiles = new HashSet<String>();

    // The following field accumulates CSS information as the document
    // is being generated (includes everything requested via
    // includeCss and includeCssFile).
    protected StringBuilder css = new StringBuilder(1000);

    // Directory containing Javascript files, ending in "/".
    // TODO: eventually this must be a path with both Fiz and app directories.
    protected String jsDirectory;

    // The following field keeps track of all the Javascript files that
    // will be included in the HTML document.  The keys in this set are
    // the names passed to the includeJsFile method.
    protected HashSet<String> jsFiles = new HashSet<String>();

    // The following field accumulates HTML that will actually read all of
    // the files in javascriptFiles.
    protected StringBuilder jsHtml = new StringBuilder();

    /**
     * Constructs an empty Html document.
     * @param cr                      The ClientRequest for which HTML
     *                                is being generated; used to extract
     *                                various configuration information.
     *                                Null means we are running unit tests,
     *                                which will set configuration information
     *                                manually.
     */
    public Html(ClientRequest cr) {
        if (cr != null) {
            // Production mode.
            contextPath = cr.getServletContext().getContextPath();
            jsDirectory = cr.getServletContext().getRealPath("") + "/";
        } else {
            // We are running unit tests; set default values, which
            // tests may override.
            contextPath = "/servlet";
            jsDirectory = "javascript/";
        }
    }

    /**
     * Returns the StringBuilder object used to assemble the main body of
     * the HTML document.  Typically the caller will append HTML to this
     * object.
     * @return                        StringBuilder object; the contents of
     *                                this object will eventually appear
     *                                between the {@code <body>} and
     *                                {@code </body>} tags in the final HTML
     *                                document.
     */
    public StringBuilder getBody() {
        return body;
    }

    /**
     * Generate a string containing the names of all of the CSS files
     * that have been requested using includeCssFile.  This method is used
     * primarily for testing.
     * @return                     A comma-separated list of filenames, sorted
     *                             alphabetically.
     */
    public String getCssFiles() {
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(cssFiles);
        Collections.sort(names);
        return StringUtil.join(names, ", ");
    }

    /**
     * Returns text that should appear at the beginning of each HTML
     * document; this can include a {@code <?xml>} element and a
     * {@code <!DOCTYPE>} element, but no {@code <head>} element or
     * anything that comes after that.  Subclasses can override
     * this method if they want to supply a custom prologue.
     * @return                        Prologue text for the HTML document.
     */
    public String getPrologue() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
                + "Strict//EN\"\n"
                + "        \"http://www.w3.org/TR/xhtml1/DTD/"
                + "xhtml1-strict.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                + "xml:lang=\"en\" lang=\"en\">\n";
    }

    /**
     * Returns the current title text for the document (this text will
     * appear between {@code <title>} and {@code </title>} in the document
     * header).
     * @return                        Current title text for the document;
     *                                null means no title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title text for the document (this text will appear between
     * {@code <title>} and {@code </title>} in the document header).
     * @param title                   New title text for the document; null
     *                                means document will have no title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Include a particular stylesheet file in the document, if it hasn't
     * been included already.  The accumulated CSS will eventually be
     * output as part of the document header.  If {@code fileName} has
     * already been included via an earlier call this method, then the
     * current invocation has no effect.
     * @param fileName                Name of a CSS file.  The file must be
     *                                in one of the directories in the path
     *                                managed by the Css class.  The file's
     *                                contents are actually the template, which
     *                                will be expanded in the context of
     *                                the css configuration dataset.
     */
    public void includeCssFile(String fileName) {
        if (cssFiles.contains(fileName)) {
            return;
        }
        cssFiles.add(fileName);
        StringUtil.addBlankLine(css);
        css.append(Css.getStylesheet(fileName));
    }

    /**
     * Add a chunk of style information to the CSS that will eventually
     * be output as part of the document header.
     * @param styleInfo            CSS information.  To help debug HTML
     *                             documents, this should start with a short
     *                             comment indicating where this information
     *                             came from.
     */
    public void includeCss(CharSequence styleInfo) {
        StringUtil.addBlankLine(css);
        css.append(styleInfo);
    }

    /**
     * Arrange for a given Javascript file to be included in the
     * document.  This method also checks for Fiz:include comment lines
     * in the file and recursively includes all of the Javascript files
     * so mentioned.
     * @param fileName             Name of the Javascript file.
     *                             TODO: info about paths needed here.
     */
    public void includeJsFile(String fileName) {
        if (jsFiles.contains(fileName)) {
            return;
        }
        jsFiles.add(fileName);

        // Check for other files that this file depends on, and include
        // them (and any files they depend on, and so on).
        includeJsDependencies(fileName);

        // Generate an HTML <script> statement to include the current file.
        jsHtml.append("<script type=\"text/javascript\" src=\"");
        jsHtml.append(contextPath);
        jsHtml.append("/");
        jsHtml.append(fileName);
        jsHtml.append("\" />\n");
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
            writer.write("<title>" + ((title != null) ? title : "") +
                    "</title>\n");

            // Output CSS info.
            writer.write("<style type=\"text/css\">\n");
            String mainCss = Css.getStylesheet("main.css");
            writer.write(mainCss);
            if (mainCss.charAt(mainCss.length()-1) != '\n') {
                writer.write('\n');
            }
            if (css.length() > 0) {
                writer.write('\n');
                writer.write(css.toString());
            }
            if ((css.length() > 0) && (css.charAt(css.length()-1) != '\n')) {
                writer.write('\n');
            }
            writer.write("</style>\n");

            // Output body.
            writer.write("</head>\n<body>\n");
            writer.write(jsHtml.toString());
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
     * the HTML document to its initial empty state.
     */
    public void reset() {
        title = null;
        body.setLength(0);
    }

    /**
     * This method is invoked to replace characters that are special in
     * HTML ({@code <>&"}) with HTML entity references ({@code &lt;},
     * {@code &gt;}, {@code &amp;}, and {@code &quot;}, respectively); this
     * allows arbitrary data to be included in HTML without accidentally
     * invoking special HTML behavior.
     * @param s                       Input string; may contain arbitrary
     *                                characters
     * @param out                     The contents of <code>s</code> are
     *                                copied here, replacing special characters
     *                                with entity references
     */
    public static void escapeHtmlChars(CharSequence s, StringBuilder out) {
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
     * unusual characters with %xx sequences as defined by RFC1738.  It
     * also converts non-ASCII characters to UTF-8 before encoding, as
     * recommended in http://www.w3.org/International/O-URL-code.html.
     * @param s                       Input string; may contain arbitrary
     *                                characters
     * @param out                     The contents of{@code s} are
     *                                copied here after converting to UTF-8
     *                                and converting nonalphanumeric
     *                                characters to %xx sequences.
     */
    public static void escapeUrlChars(CharSequence s, StringBuilder out) {
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
     * example, if {@code s} is {@code a\x"z} then it gets escaped to
     * {@code a\\x\"z} so that the original value will be regenerated when
     * Javascript evaluates the string literal.
     * @param s                       Value that is to be encoded in a string
     *                                literal.
     * @param out                     A converted form of {@code s}
     *                                is copied here; if this information
     *                                is used in a Javascript string, it
     *                                will evaluate to exactly the characters
     *                                in {@code s}.
     */
    public static void escapeStringChars(CharSequence s, Appendable out) {
        try {
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
        catch (IOException e) {
            throw new IOError(e.getMessage());
        }
    }

    /**
     * Given the name of a Javascript file, read in the first few lines
     * of the file to see if it contains special "Fiz:include" lines
     * indicating that this file depends on other Javascript files.  If so,
     * arrange for all of those Javascript files to be included in this
     * HTML document also.
     * @param fileName             Name of a Javascript file that has been
     *                             requested for this HTML document.
     */
    protected void includeJsDependencies(String fileName) {
        String fullName = jsDirectory + fileName;
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(fullName));
            String line;
            // Search the first few lines of the file for comment lines
            // containing "Fiz:include";  stop as soon as we see a line
            // that isn't blank and isn't a comment.
            while ((line = reader.readLine()) != null) {
                int i = StringUtil.skipSpaces(line, 0);
                if (i == line.length()) {
                    continue;
                }
                if (line.startsWith("/*", i)  || line.startsWith("//", i)
                        || line.startsWith("*/", i) ) {
                    i += 2;
                } else if (line.charAt(i) == '*') {
                    i += 1;
                } else {
                    // Not a comment or a blank line; we're done with this
                    // file.
                    break;
                }
                i = StringUtil.skipSpaces(line, i);
                if (!line.startsWith("Fiz:include ", i)) {
                    continue;
                }

                // Extract the substring between "Fiz:include" and "#" or
                // end of line, and break this up into Javascript file names
                // separated by commas.
                int end = line.indexOf('#');
                if (end < 0) {
                    end = line.length();
                }
                String[] files = StringUtil.split(
                        line.substring(i+12, end).trim(), ',');
                for (String file: files) {
                    includeJsFile(file);
                }
            }
            reader.close();
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundError(fullName, "javascript",
                    e.getMessage());
        }
        catch (IOException e) {
            throw IOError.newFileInstance(fullName, e.getMessage());
        }

    }
}
