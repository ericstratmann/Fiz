/**
 * Junit tests for the Util class.
 */

package org.fiz;
import java.util.HashMap;

public class UtilTest extends junit.framework.TestCase {

    public void testSkipSpaces() {
        assertEquals(6, Util.skipSpaces("abc   def", 3));
        assertEquals(6, Util.skipSpaces("abc   def", 5));
        assertEquals(6, Util.skipSpaces("abc   def", 6));
        assertEquals(9, Util.skipSpaces("abc   def", 9));
        assertEquals("already past end of string", 6,
                Util.skipSpaces("abc   ", 4));
    }

    public void testJoinedLength() {
        assertEquals("basics", 20, Util.joinedLength(new String[]
                {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", 0,
                Util.joinedLength(new String[] {}, ", "));

    }

    public void testJoinStringArray() {
        assertEquals("basics", "first, second, third",
                Util.join(new String[] {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", "",
                Util.join(new String[] {}, ", "));

    }

    public void testJoinIterable() {
        HashMap<Integer,String> hash = new HashMap<Integer, String>();
        hash.put(123, "string_123");
        hash.put(99, "string_99");
        hash.put(-48, "string_-48");
        assertEquals("basics", "-48, 99, 123", Util.join(hash.keySet(), ", "));
        assertEquals("no values to join", "",
                Util.join(new HashMap<String,String>().keySet(), ", "));

    }

    public void testSplit() {
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

    public void testIdentifierEnd() {
        assertEquals("stop on dot", 6,
                Util.identifierEnd("a+cdef.ghi", 2));
        assertEquals("stop at end of string", 7,
                Util.identifierEnd("abc123_", 0));
        assertEquals("already past end of string", 10,
                Util.identifierEnd("abc", 10));
    }

    public void testGetUriAndQuery() {
        assertEquals("no query data", "/a/b",
                Util.getUriAndQuery(new UtilTestRequest("/a/b", null)));
        assertEquals("query data", "/a/b?x=24&y=13",
                Util.getUriAndQuery(new UtilTestRequest("/a/b", "x=24&y=13")));
    }
}

// The following class implements just enough of the HttpServletRequest
// interface to test the testGetUriAndQuery method.
class UtilTestRequest extends TestServletRequest {
    public String uri;
    public String queryString;

    public UtilTestRequest(String uri, String query) {
        this.uri = uri;
        this.queryString = query;
    }

    public String getQueryString() {return queryString;}
    public String getRequestURI() {return uri;}
}
