package org.fiz;
import java.io.*;
import java.util.*;

/**
 * Junit tests for the Util class.
 */

public class UtilTest extends junit.framework.TestCase {

    public void test_skipSpaces() {
        assertEquals(6, Util.skipSpaces("abc   def", 3));
        assertEquals(6, Util.skipSpaces("abc   def", 5));
        assertEquals(6, Util.skipSpaces("abc   def", 6));
        assertEquals(9, Util.skipSpaces("abc   def", 9));
        assertEquals("already past end of string", 6,
                Util.skipSpaces("abc   ", 4));
    }

    public void test_skipSpaces_withEnd() {
        assertEquals(5, Util.skipSpaces("abc   def", 3, 5));
        assertEquals(6, Util.skipSpaces("abc   def", 5, 8));
        assertEquals(5, Util.skipSpaces("abc   def", 5, 5));
    }

    public void test_joinedLength() {
        assertEquals("basics", 20, Util.joinedLength(new String[]
                {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", 0,
                Util.joinedLength(new String[] {}, ", "));

    }

    public void test_join_stringArray() {
        assertEquals("basics", "first, second, third",
                Util.join(new String[] {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", "",
                Util.join(new String[] {}, ", "));

    }

    public void test_join_iterable() {
        HashMap<Integer,String> hash = new HashMap<Integer, String>();
        hash.put(123, "string_123");
        hash.put(99, "string_99");
        hash.put(-48, "string_-48");
        assertEquals("basics", "-48, 99, 123", Util.join(hash.keySet(), ", "));
        assertEquals("no values to join", "",
                Util.join(new HashMap<String,String>().keySet(), ", "));

    }

    public void test_join_enumeration() {
        Vector<String> list = new Vector<String>();
        list.add("123");
        list.add("99");
        list.add("-48");
        assertEquals("basics", "123, 99, -48", Util.join(list.elements(),
                ", "));
        list.clear();
        assertEquals("no values to join", "", Util.join(list.elements(),
                ", "));

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

    public void test_isWhitespace() {
        assertEquals("all whitespace", true, Util.isWhitespace(" \n\r\t "));
        assertEquals("not all whitespace", false, Util.isWhitespace("  abc "));
    }

    public void test_isWhitespace_characterArray() {
        char[] data = new char[] {'a', ' ', '\n', '\r', '\t', ' ', ' ', 'b'};
        assertEquals("all whitespace", true, Util.isWhitespace(data, 1, 6));
        assertEquals("leading non-whitespace", false,
                Util.isWhitespace(data, 0, 3));
        assertEquals("trailing non-whitespace", false,
                Util.isWhitespace(data, 4, 4));
    }

    public void test_excerpt_String() {
        assertEquals("short value", "abcdef", Util.excerpt("abcdef", 6));
        assertEquals("long value", "abc...", Util.excerpt("abcdefg", 6));
    }

    public void test_excerpt_StringBuilder() {
        assertEquals("short value", "abcdef", Util.excerpt(
                new StringBuilder("abcdef"), 6));
        assertEquals("long value", "abc...", Util.excerpt(
                new StringBuilder("abcdefg"), 6));
    }

    public void test_newCharArray() {
        char[] ch = Util.newCharArray("abc");
        assertEquals("array length", 3, ch.length);
        assertEquals("array contents", "abc", new String(ch));
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
        TestUtil.deleteTree("_test_.abc");
        TestUtil.deleteTree("_test_.x");
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

    public void test_deleteTree_success() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/first", "data for first file");
        TestUtil.writeFile("_test1_/child/second", "data for second file");
        assertEquals("successful return value", true,
                Util.deleteTree("_test1_"));
        assertEquals("directory is gone", false,
                (new File("_test1_")).exists());
    }

    public void test_deleteTree_failure() throws IOException {
        // This test only works on Windows, where a file cannot be
        // deleted if it is open.
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return;
        }
        TestUtil.writeFile("_test", "data");
        FileReader reader = new FileReader("_test");
        assertEquals("can't delete open file", false,
                Util.deleteTree("_test"));
        reader.close();
        assertEquals("can delete file once it's closed", true,
                Util.deleteTree("_test"));
    }

    public void test_respondWithFile() throws IOException {
        TestUtil.writeFile("test.html", "0123456789abcde");
        UtilResponseFixture response = new UtilResponseFixture();
        Util.respondWithFile(new File("test.html"), response);
        assertEquals("content length", 15, response.contentLength);
        assertEquals("returned data", "0123456789abcde",
                response.writer.toString());
        TestUtil.deleteTree("test.html");
    }

    public void test_extractInnerMessage() {
        assertEquals("close paren missing", "file (xyzzy",
                Util.extractInnerMessage("file (xyzzy"));
        assertEquals("open paren missing", "file xyzzy)",
                Util.extractInnerMessage("file xyzzy)"));
        assertEquals("open paren too soon", " (file xyzzy)",
                Util.extractInnerMessage(" (file xyzzy)"));
        assertEquals("no space before paren", "a/b/c/test(message2)",
                Util.extractInnerMessage("a/b/c/test(message2)"));
        assertEquals("everything matches", "message",
                Util.extractInnerMessage("abc (message)"));
    }

    public void test_readFile_shortFile() throws FileNotFoundException {
        String contents = "Line 1\nLine 2\n";
        TestUtil.writeFile("test.foo", contents);
        StringBuilder s = Util.readFile("test.foo");
        assertEquals("file length", 14, s.length());
        assertEquals("file contents", contents, s.toString());
        TestUtil.deleteTree("test.foo");
    }
    public void test_readFile_longFile() throws FileNotFoundException {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i <1000; i++) {
            contents.append(String.format("This is line #%04d\n", i));
        }
        TestUtil.writeFile("test.foo", contents.toString());
        StringBuilder s = Util.readFile("test.foo");
        assertEquals("file length", 19000, s.length());
        assertEquals("check one line", "This is line #0010",
                s.substring(190, 208));
        assertEquals("full file contents", contents.toString(), s.toString());
        TestUtil.deleteTree("test.foo");
    }
    public void test_readFile_FileNotFoundException() {
        boolean gotException = false;
        try {
            Util.readFile("bogus/nonexistent");
        }
        catch (FileNotFoundException e) {
            assertEquals("exception message",
                    "bogus\\nonexistent (The system cannot find the "
                    + "path specified)", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_readFileFromPath() throws FileNotFoundError {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/foo.txt", "_test1_/foo.txt");
        TestUtil.writeFile("_test1_/child/foo.txt", "_test1_/child/foo.txt");
        StringBuilder contents = Util.readFileFromPath("foo.txt", "test",
                ".", "bogus/xyz", "_test1_", "_test1_/child");
        assertEquals("full file contents", "_test1_/foo.txt",
                contents.toString());
        TestUtil.deleteTree("_test1_");
    }
    public void test_readFileFromPath_notFound() {
        boolean gotException = false;
        try {
            Util.readFileFromPath("src", "test", ".", "bogus1/a",
                    "bogus2/x/y");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find test file \"src\" in path "
                    + "(\".\", \"bogus1/a\", \"bogus2/x/y\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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
