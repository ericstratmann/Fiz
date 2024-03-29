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

package org.fiz.test;

import java.io.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

/**
 * This class implements XHTML document validation as a wrapper layer around
 * a SAX-based XML document parser.
 */
public class XhtmlValidator {
    private XhtmlValidator()
    {}

    /**
     * Validates an XHTML document or fragment.
     *
     * @param xhtml                XHTML document to validate.
     * @return                     The return value is empty if the document
     *                             is valid XHTML; otherwise it contains a
     *                             human readable message describing the
     *                             error(s).
     */

    public static String validate(String xhtml) {
        XhtmlErrorHandler handler = new XhtmlErrorHandler(xhtml);
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            if (!xhtml.startsWith("<?xml ")) {
                // This is just a document fragment so we need to prefix a
                // DOCTYPE line for validation and encase the fragment in
                // a <div> element.
                xhtml = "<!DOCTYPE div SYSTEM \"xhtml1-strict.dtd\">\n" +
                        "<div>\n" +
                        xhtml +
                        "</div>";
                handler = new XhtmlErrorHandler(xhtml);
            }

            factory.setValidating(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            parser.setEntityResolver(new XhtmlEntityResolver());
            parser.setErrorHandler(handler);
            InputStream in = new ByteArrayInputStream(xhtml.getBytes());
            parser.parse(in);
            return handler.getErrorMessage();
        }
        catch (ParserConfigurationException e) {
            return "XhtmlValidator couldn't configure parser: " +
                    e.getMessage();
        } catch (SAXException e) {
            // Ignore this exception and just return the accumulated
            // error information.
            return handler.getErrorMessage();
        } catch (IOException e) {
            return "I/O error in XhtmlValidator: " + e.getMessage();
        }
    }

    /**
     * The following class is used by the XML parser to map from external
     * entity names (for DTD's) to files.
     */
    private static class XhtmlEntityResolver implements EntityResolver {
        // File present in Fiz applications, containing a reference to the
        // Fiz tools installation to use:
        protected static final String FIZ_LOCATION_PROPERTY = "fiz.location";

        protected static final String DEFAULT_DTD_ROOT = "test" +
            File.separator + "dtd" + File.separator;
        protected static String dtdRoot = null;

        /**
         * Given the name of an entity, this method extracts the last
         * file name in the entity's path, finds the corresponding
         * file in the appropriate dtd directory, and returns an
         * InputSource that can be used to read the file.
         * @param publicId         Not used.
         * @param systemId         System identifier for the entity, which can
         *                         be mapped to a file name.
         * @return                 InputSource for reading the entity.
         * @throws FileNotFoundException
         *                         Couldn't find a file corresponding to
         *                         the entity.
         */
        public InputSource resolveEntity(String publicId, String systemId)
                throws FileNotFoundException {
            if (dtdRoot == null) {
                updateDtdRoot();
            }
            String fileName = systemId.substring(systemId.lastIndexOf('/')+1);
            return new InputSource(new InputStreamReader(
                    new FileInputStream(dtdRoot + fileName)));
        }

        /**
         * Determines where the dtd XHTML validation files are kept.  If a
         * fiz.location system property is set, use the directory
         * <fiz.location>/test/dtd.  Otherwise assume that the dtd files are
         * kept locally in ./test/dtd/.
         *
         */
        private void updateDtdRoot() {
            String fizLocation = System.getProperty(FIZ_LOCATION_PROPERTY);
            dtdRoot = (fizLocation == null? "" : fizLocation + File.separator) + DEFAULT_DTD_ROOT;
        }
    }

    /**
     * The following class is invoked to report errors discovered during
     * XHTML validation; it generates an error message that includes the
     * portion of the document up through any errors, with annotations
     * describing the error(s).
     */
    private static class XhtmlErrorHandler implements ErrorHandler {
        protected StringBuilder message = new StringBuilder();

        // When an error occurs, we include the entire document in the
        // error message, in order to make it easy to see the problem.
        // The following variables keep track of how many lines we
        // have already output, and the portion of the original document
        // that this represents.
        protected String xhtml;              // Document to validate.
        protected int prevLineNumber = 0;    // Index of last line output.
        protected int offset = 0;            // First byte in xhtml that
                                             // hasn't been copied to message.
        /**
         * Construct an XhtmlErrorHandler for a given document.
         * @param document         The document about to be validated; used
         *                         to generate error messages.
         */
        public XhtmlErrorHandler(String document) {
            xhtml = document;
        }

        /**
         * This method is invoked by the XHTML validator to report an error
         * in the XHTML.
         * @param e                Information about the error.
         */
        public void error(SAXParseException e) {
            addErrorInfo(e);
        }

        /**
         * This method is invoked by the XHTML validator to report a fatal
         * error in the XHTML.
         * @param e                Information about the error.
         */
        public void fatalError(SAXParseException e) {
            addErrorInfo(e);
        }

        /**
         * This method is invoked by the XHTML validator to report an error
         * in the XHTML.
         * @param e                Information about the error.
         */
        public void warning(SAXParseException e) {
            addErrorInfo(e);
        }

        /**
         * This method returns a string describing all of the errors
         * encountered so far.
         * @return                 Empty string means there were no errors.
         *                         Otherwise the result is a human-readable
         *                         string describing all of the validation
         *                         errors encountered.
         */
        public String getErrorMessage() {
            return message.toString();
        }

        /**
         * This method doess all of the work of reporting an error.  It
         * adds to the {@code method} string.
         * @param e                Information about the error.
         */
        protected void addErrorInfo(final SAXParseException e) {
            if (message.length() != 0) {
                message.append('\n');
            }
            copyUpToLine(e.getLineNumber(), "    ");
            int column = e.getColumnNumber();
            message.append("    ");
            for (int i = 1; i < column; i++) {
                message.append('-');
            }
            message.append("^\n    **** ");
            message.append(e.getMessage());
        }

        /**
         * Copy lines from the input document to the error message until
         * the line with a given index has been copied.
         * @param lineNumber       Stop when this line number has been
         *                         copied (1 means first line of the document).
         * @param prefix           String to prefix each line that is copied
         *                         (typically spaces for indentation).
         */
        protected void copyUpToLine(int lineNumber, String prefix) {
            while ((prevLineNumber < lineNumber) && (offset < xhtml.length())) {
                int newOffset = xhtml.indexOf('\n', offset);
                if (newOffset < 0) {
                    newOffset = xhtml.length();
                } else {
                    newOffset += 1;
                }
                message.append(prefix);
                message.append(xhtml.substring(offset, newOffset));
                prevLineNumber++;
                offset = newOffset;
            }
        }
    }
}
