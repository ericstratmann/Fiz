/**
 * Junit tests for the Util class.
 */

package org.fiz;
import java.util.HashMap;
import java.io.*;

public class UtilTest extends junit.framework.TestCase {

    public void test_skipSpaces() {
        assertEquals(6, Util.skipSpaces("abc   def", 3));
        assertEquals(6, Util.skipSpaces("abc   def", 5));
        assertEquals(6, Util.skipSpaces("abc   def", 6));
        assertEquals(9, Util.skipSpaces("abc   def", 9));
        assertEquals("already past end of string", 6,
                Util.skipSpaces("abc   ", 4));
    }

    public void test_joinedLength() {
        assertEquals("basics", 20, Util.joinedLength(new String[]
                {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", 0,
                Util.joinedLength(new String[] {}, ", "));

    }

    public void test_joinStringArray() {
        assertEquals("basics", "first, second, third",
                Util.join(new String[] {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", "",
                Util.join(new String[] {}, ", "));

    }

    public void test_joinIterable() {
        HashMap<Integer,String> hash = new HashMap<Integer, String>();
        hash.put(123, "string_123");
        hash.put(99, "string_99");
        hash.put(-48, "string_-48");
        assertEquals("basics", "-48, 99, 123", Util.join(hash.keySet(), ", "));
        assertEquals("no values to join", "",
                Util.join(new HashMap<String,String>().keySet(), ", "));

    }

    public void test_split() {
        String[] result = Util.split("first, second,,third ", ',');
        String[] result2 = Util.split("  no separators", ',');
        String[] result3 = Util.split("  ", ',');
        String[] result4 = Util.split(":  ", ':');
        assertEquals("count of substrings", 4, result.length);
        assertEquals("substring count, no separators", 1, result2.length);
        assertEquals("substring count for empty string", 0, result3.length);
        assertEquals("ignore empty element after last separator", 1,
                result4.length);
        assertEquals("simple substring", "first", result[0]);
        assertEquals("empty substring", "", result[2]);
        assertEquals("empty substring, again", "", result4[0]);
        assertEquals("trim spaces", "no separators", result2[0]);
        assertEquals("trim spaces ", "second", result[1]);
    }

    public void test_identifierEnd() {
        assertEquals("stop on dot", 6,
                Util.identifierEnd("a+cdef.ghi", 2));
        assertEquals("stop at end of string", 7,
                Util.identifierEnd("abc123_", 0));
        assertEquals("already past end of string", 10,
                Util.identifierEnd("abc", 10));
    }

    public void test_fileExtension() {
        assertEquals("extension exists", ".java",
                Util.fileExtension("C:/a/b/c.java"));
        assertEquals("nothing after the dot", null,
                Util.fileExtension("C:/a/b/c."));
        assertEquals("slash after last dot", null,
                Util.fileExtension("C:/a/b.test/c"));
        assertEquals("backslash after last dot", null,
                Util.fileExtension("C:\\a\\b.test\\c"));
        assertEquals("no slash, no dot", null,
                Util.fileExtension("name"));
    }

     public void test_findFileWithExtension() {
        TestUtil.writeFile("_test_.abc", "abc");
        TestUtil.writeFile("_test_.x", "abc");
        assertEquals("found file", "_test_.x",
                Util.findFileWithExtension("_test_", ".def", ".x", ".abc"));
        assertEquals("couldn't find file", null,
                Util.findFileWithExtension("_test_", ".q", ".y"));
        TestUtil.deleteFile("_test_.abc");
        TestUtil.deleteFile("_test_.x");
    }

    public void test_getUriAndQuery() {
        assertEquals("no query data", "/a/b",
                Util.getUriAndQuery(new UtilRequestFixture("/a/b", null)));
        assertEquals("query data", "/a/b?x=24&y=13",
                Util.getUriAndQuery(new UtilRequestFixture("/a/b", "x=24&y=13")));
    }

    public void test_copyStream() throws IOException {
        StringReader in = new StringReader("01234567890abcdefg");
        StringWriter out = new StringWriter();
        in.read(); in.read();
        out.write('x');
        Util.copyStream (in, out);
        assertEquals("destination stream", "x234567890abcdefg",
                out.toString());
        out.getBuffer().setLength(0);
        out.write('q');
        Util.copyStream (in, out);
        assertEquals("input stream already at end-of-file", "q",
                out.toString());
    }

    public void test_respondWithFile() throws IOException {
        TestUtil.writeFile("test.html", "0123456789abcde");
        UtilResponseFixture response = new UtilResponseFixture();
        Util.respondWithFile(new File("test.html"), response);
        assertEquals("content length", 15, response.contentLength);
        assertEquals("returned data", "0123456789abcde",
                response.writer.toString());
        TestUtil.deleteFile("test.html");
    }
}

// The following class implements just enough of the HttpServletRequest
// interface to test the testGetUriAndQuery method.
class UtilRequestFixture extends ServletRequestFixture {
    public String uri;
    public String queryString;

    public UtilRequestFixture(String uri, String query) {
        this.uri = uri;
        this.queryString = query;
    }

    public String getQueryString() {return queryString;}
    public String getRequestURI() {return uri;}
}

class UtilResponseFixture extends ServletResponseFixture {
    public int contentLength;
    public void setContentLength(int length) {
        contentLength = length;
    }
    public StringWriter writer = new StringWriter();
    public PrintWriter getWriter() {
        return new PrintWriter(writer);
    }
}
