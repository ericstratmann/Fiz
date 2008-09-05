/**
 * TestUtil provides an assortment of methods to make it easier to write
 * tests for Fiz.
 */

package org.fiz;
import java.io.*;
import java.lang.*;
import java.util.regex.*;
import org.junit.*;

public class TestUtil {

    /**
     * Verifies that a given string has proper XHTML syntax, generates
     * proper junit errors if it doesn't.
     * @param html                       String to test.
     */
    public static void assertXHTML(String html) {
        writeFile("xmlvalid.tmp", html);
        String dtd = "C:/cygwin/usr/local/tools/common/share/xhtml"
                + "/xhtml1-strict.dtd";
        StringBuilder output = new StringBuilder();
        Process p = null;
        try {
            ProcessBuilder builder = new ProcessBuilder("xmlvalid", "-q", "-c",
                    "\"--dtd=" + dtd + "\"", "xmlvalid.tmp");
            builder.redirectErrorStream(true);
            p = builder.start();
            InputStreamReader reader = new InputStreamReader(
                    p.getInputStream());
            char buffer[] = new char[4096];
            while (true) {
                int count = reader.read(buffer, 0, buffer.length);
                if (count <= 0) {
                    break;
                }
                output.append(buffer, 0, count);
            }
            p.waitFor();
        }
        catch (Exception e) {
            Assert.assertEquals("error running xmlvalid: " +
                    StringUtil.lcFirst(e.getMessage()),
                    true, false);
        }
        deleteTree("xmlvalid.tmp");
        if (p != null) {
            p.destroy();
        }
        Assert.assertEquals("malformed XHTML", "", output.toString());
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
        Pattern pattern = Pattern.compile(regexp);
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
}
