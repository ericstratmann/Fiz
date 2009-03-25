package org.fiz;
import java.io.*;
import java.lang.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * TestUtil provides an assortment of methods for use in Fiz unit tests.
 */
public class TestUtil {

    /**
     * Verifies that a given string has proper XHTML syntax, generates
     * proper junit errors if it doesn't.
     * @param html                       String to test.
     */
    public static void assertXHTML(String html) {
        String errors = XhtmlValidator.validate(html);
        if (errors.length() != 0) {
            Assert.fail("malformed XHTML:\n" + errors);
        }
    }

    /**
     * Verifies that test output contains a particular substring, generates
     * a junit error message if it doesn't.
     * @param message                    Message to incorporate in any
     *                                   error message that is generated.
     * @param substring                  Desired substring.
     * @param actual                     Output from test, which should
     *                                   contain substring.
     */
    public static void assertSubstring(String message, String substring,
                                       String actual) {
        if ((actual == null) || (actual.indexOf(substring) < 0)) {
            Assert.fail(message + "\nExpected substring :" + substring
                    + "\nTest output :" + actual);
        }
    }

    /**
     * Verifies that the test output matches a given regular expression;
     * generates a junit error message if it doesn't.
     * @param message                    Message to incorporate in any
     *                                   error message that is generated.
     * @param pattern                    Regular expression to match against
     *                                   the test output.
     * @param actual                     Output from test, which should match
     *                                   pattern.
     */
    public static void assertMatch(String message, String pattern, String actual) {
        if ((actual == null) || !actual.matches(pattern)) {
            Assert.fail(message + "\nPattern :" + pattern
                    + "\nDidn't match test output :" + actual);
        }
    }

    /**
     * Extract a substring from the test output using a regular expression
     * pattern, then verify that the substring matches an expected value;
     * generate a junit error message if it doesn't, or if the pattern
     * doesn't match.
     * @param message                    Message to incorporate in any
     *                                   error message that is generated.
     * @param expected                   Expected value for the substring of
     *                                   {@code actual} that matches
     *                                   {@code regexp}.
     * @param actual                     Test output.
     * @param regexp                     Regular expression pattern.
     */
    public static void assertMatchingSubstring(String message,
            String expected, String actual, String regexp) {
        Pattern pattern = Pattern.compile(regexp, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(actual);
        if (!matcher.find()) {
            Assert.fail(message + "\nPattern :" + regexp
                    + "\nDidn't match test output :" + actual);
        }
        Assert.assertEquals(message, expected, matcher.group());
    }

    /**
     * Creates a file containing a given string as contents.  If an error
     * occurs while creating the file, a test assertion error is generated
     * with error information.
     * @param fileName                   Name of the desired file
     * @param contents                   Information to write to the file
     * @return                           True means the file was successfully
     *                                   written; false means there was an
     *                                   error.
     */
    public static boolean writeFile(String fileName, String contents) {
        try {
            File f = new File(fileName);
            PrintWriter p = new PrintWriter(f);
            p.print(contents);
            p.close();
        }
        catch (Exception e) {
            Assert.assertEquals("couldn't write test file \"" + fileName + "\": "
                    + e.getMessage(), true, false);
            return false;
        }
        return true;
    }

    /**
     * Deletes a file or directory subtree.  If an error occurs, a test
     * assertion error is generated with error information.
     * @param fileName                   Name of the file or directory to
     *                                   delete
     */
    public static void deleteTree(String fileName) {
        Assert.assertEquals("delete test file/directory \"" + fileName + "\"",
                true, Util.deleteTree(fileName));
    }

    /**
     * The following class is used by the XML parser to map from external
     * entity names (for DTD's) to files.
     */
    private static class XhtmlEntityResolver implements EntityResolver {
        // Directory containing all of the DTD files:
        protected static final String DTD_ROOT = "test/dtd";

        /**
         * Given the name of an entity, this method extracts the last
         * file name in the entity's path, finds the corresponding
         * file in DTD_ROOT, and returns an InputSource that can be used
         * to read the file.
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
            String fileName = systemId.substring(systemId.lastIndexOf('/')+1);
            return new InputSource(new InputStreamReader(
                    new FileInputStream(DTD_ROOT + fileName)));
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
