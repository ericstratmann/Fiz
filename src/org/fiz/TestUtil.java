/**
 * TestUtil provides an assortment of methods to make it easier to write
 * tests for Fiz.
 */

package org.fiz;
import java.io.*;
import java.lang.ProcessBuilder;
import org.junit.Assert;

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
            Assert.assertEquals("error running xmlvalid: " + e.getMessage(),
                    true, false);
        }
        deleteFile("xmlvalid.tmp");
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
     *                                   contain substring
     */
    public static void assertSubstring(String message, String substring, String actual) {
        if ((actual == null) || (actual.indexOf(substring) < 0)) {
            Assert.fail(message + "\nExpected substring :" + substring
                    + "\nTest output :" + actual);
        }
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
     * Deletes a file.  If an error occurs while deleting the file, a test
     * assertion error is generated with error information.
     * @param fileName                   Name of the desired file
     */
    public static void deleteFile(String fileName) {
        Assert.assertEquals("delete test file \"" + fileName + "\"",
                true, new File(fileName).delete());
    }
}
