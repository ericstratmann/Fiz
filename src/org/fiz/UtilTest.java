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

    public void testEscapeHtmlChars() {
        StringBuilder out = new StringBuilder();
        Util.escapeHtmlChars("abc123", out);
        assertEquals("no special characters", "abc123", out.toString());
        out.setLength(0);
        Util.escapeHtmlChars("!\"#%&';<=>?", out);
        assertEquals("special characters", "!&quot;#%&amp;';&lt;=&gt;?",
                out.toString());
    }

    public void testEscapeUrlChars() {
        StringBuilder out = new StringBuilder();
        Util.escapeUrlChars(" +,-./@ABYZ[`abyz{", out);
        assertEquals("ASCII characters", "+%2b%2c-.%2f%40ABYZ%5b%60abyz%7b",
                out.toString());
        out.setLength(0);
        Util.escapeUrlChars("/0123456789:", out);
        assertEquals("digits","%2f0123456789%3a", out.toString());
        out.setLength(0);
        Util.escapeUrlChars("\u007f--\u0080--\u07ff--\u0800--\u1234", out);
        assertEquals("Unicode characters",
                "%7f--%c2%80--%df%bf--%e0%a0%80--%e1%88%b4",
                out.toString());
    }

    public void testEscapeStringChars() {
        StringBuilder out = new StringBuilder();
        Util.escapeStringChars("abc\037\040 | \n\t | \001 | \\\"", out);
        assertEquals("abc\\x1f  | \\n\\t | \\x01 | \\\\\\\"",
                out.toString());
    }

    public void testGetUriAndQuery() {
        // TODO: need tests for testGetUriAndQuery
    }
}
