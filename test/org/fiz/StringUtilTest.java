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

package org.fiz;

import java.util.*;

/**
 * Junit tests for the StringUtil class.
 */
public class StringUtilTest extends junit.framework.TestCase {

    public void test_addBlankLine() {
        StringBuilder s = new StringBuilder();
        StringUtil.addBlankLine(s);
        assertEquals("string initially empty", "", s.toString());
        s = new StringBuilder("abc");
        StringUtil.addBlankLine(s);
        assertEquals("last line not terminated", "abc\n\n", s.toString());
        s = new StringBuilder("abc\n");
        StringUtil.addBlankLine(s);
        assertEquals("last line terminated", "abc\n\n", s.toString());
    }

    public void test_addExtension() {
        assertEquals("path already has extension", "foo.java",
                StringUtil.addExtension("foo.java", ".yml"));
        assertEquals("path doesn't already have an extension",
                "C:/code/foo.xx/bar.yml",
                StringUtil.addExtension("C:/code/foo.xx/bar", ".yml"));
    }

    public void test_decode4to3_basics() {
        byte[] bytes = StringUtil.decode4to3("abc!$0 ____", 3, 8);
        StringBuilder out = new StringBuilder();
        String separator = "";
        for (byte b: bytes) {
            out.append(separator);
            out.append(String.format("0x%02x", ((int) b) & 0xff));
            separator = ", ";
        }
        assertEquals("output bytes", "0x01, 0x01, 0x01, 0xff, 0xff, 0xff",
                out.toString());
    }
    public void test_decode4to3_trailingGroupOf3() {
        byte[] bytes = StringUtil.decode4to3("____!&(", 0, 7);
        StringBuilder out = new StringBuilder();
        String separator = "";
        for (byte b: bytes) {
            out.append(separator);
            out.append(String.format("0x%02x", ((int) b) & 0xff));
            separator = ", ";
        }
        assertEquals("output bytes", "0xff, 0xff, 0xff, 0x81, 0x81",
                out.toString());
    }
    public void test_decode4to3_trailingGroupOf2() {
        byte[] bytes = StringUtil.decode4to3("____!\"", 0, 6);
        StringBuilder out = new StringBuilder();
        String separator = "";
        for (byte b: bytes) {
            out.append(separator);
            out.append(String.format("0x%02x", ((int) b) & 0xff));
            separator = ", ";
        }
        assertEquals("output bytes", "0xff, 0xff, 0xff, 0x81",
                out.toString());
    }

    public void test_detailedErrorMessage_basics() {
        String message = StringUtil.detailedErrorMessage(
                new Dataset("message", "access violation",
                "details", "firstline\nsecond line\nthird",
                "code", "ENOENT", "time", "Thursday\n3:15 P.M."));
        assertEquals("error message", "access violation:\n" +
                "  code:        ENOENT\n" +
                "  details:     firstline\n" +
                "               second line\n" +
                "               third\n" +
                "  time:        Thursday\n" +
                "               3:15 P.M.",
                message);
    }
    public void test_detailedErrorMessage_multipleErrors() {
        String message = StringUtil.detailedErrorMessage(
                new Dataset("message", "access violation"),
                new Dataset("details", "don't ask",
                "time", "Thursday\n3:15 P.M."),
                new Dataset("message", "disk full"));
        assertEquals("error message", "access violation\n" +
                "error:\n" +
                "  details:     don't ask\n" +
                "  time:        Thursday\n" +
                "               3:15 P.M.\n" +
                "disk full",
                message);
    }
    public void test_detailedErrorMessage_noMessage() {
        String message = StringUtil.detailedErrorMessage(
                new Dataset("code", "ENOENT", "time",
                "Thursday\n3:15 P.M."));
        assertEquals("error message", "error:\n" +
                "  code:        ENOENT\n" +
                "  time:        Thursday\n" +
                "               3:15 P.M.",
                message);
    }
    public void test_detailedErrorMessage_messageOnly() {
        String message = StringUtil.detailedErrorMessage(
                new Dataset("message", "access violation"));
        assertEquals("error message", "access violation",
                message);
    }
    public void test_detailedErrorMessage_ignoreNestedDataset() {
        String message = StringUtil.detailedErrorMessage(
                YamlDataset.newStringInstance(
                "message: access violation\n" +
                "nested:\n" +
                "  value: 48\n" +
                "  value2: 49\n" +
                "details: xyzzy\n"));
        assertEquals("error message", "access violation:\n" +
                "  details:     xyzzy",
                message);
    }

    public void test_encode3to4_basics() {
        byte[] bytes = {1, 1, 1, -1, -1, -1};
        StringBuilder out = new StringBuilder("abc");
        StringUtil.encode3to4(bytes, out);
        assertEquals("output StringBuilder", "abc!$0 ____",
                out.toString());
    }
    public void test_encode3to4_trailingGroupOf2() {
        byte[] bytes = {(byte) 0x81, (byte) 0x81};
        StringBuilder out = new StringBuilder();
        StringUtil.encode3to4(bytes, out);
        assertEquals("output StringBuilder", "!&(",
                out.toString());
    }
    public void test_encode3to4_trailingGroupOf1() {
        byte[] bytes = {1, 1, 1, (byte) 0x81};
        StringBuilder out = new StringBuilder();
        StringUtil.encode3to4(bytes, out);
        assertEquals("output StringBuilder", "!$0 !\"",
                out.toString());
    }

    public void test_errorMessage_oneErrorWithMessage() {
        String message = StringUtil.errorMessage(
                new Dataset("message", "detail 47", "code", "ENOENT"));
        assertEquals("error message", "detail 47", message);
    }
    public void test_errorMessage_oneErrorNoMessage() {
        String message = StringUtil.errorMessage(
                new Dataset("code", "earthquake"));
        assertEquals("error message", "error earthquake",
                message);
    }
    public void test_errorMessage_multipleErrors() {
        String message = StringUtil.errorMessage(
                new Dataset("message", "detail 47",
                "code", "ENOENT"), new Dataset("code", "ENOENT"),
                new Dataset("reason", "earthquake"));
        assertEquals("error message", "detail 47\n" +
                "error ENOENT\n" +
                "unknown error", message);
    }

    public void test_excerpt_String() {
        assertEquals("short value", "abcdef", StringUtil.excerpt("abcdef", 6));
        assertEquals("long value", "abc...", StringUtil.excerpt("abcdefg", 6));
    }

    public void test_excerpt_StringBuilder() {
        assertEquals("short value", "abcdef", StringUtil.excerpt(
                new StringBuilder("abcdef"), 6));
        assertEquals("long value", "abc...", StringUtil.excerpt(
                new StringBuilder("abcdefg"), 6));
    }

    public void test_extractInnerMessage() {
        assertEquals("close paren missing", "file (xyzzy",
                StringUtil.extractInnerMessage("file (xyzzy"));
        assertEquals("open paren missing", "file xyzzy)",
                StringUtil.extractInnerMessage("file xyzzy)"));
        assertEquals("open paren too soon", " (file xyzzy)",
                StringUtil.extractInnerMessage(" (file xyzzy)"));
        assertEquals("no space before paren", "a/b/c/test(message2)",
                StringUtil.extractInnerMessage("a/b/c/test(message2)"));
        assertEquals("everything matches", "message",
                StringUtil.extractInnerMessage("abc (message)"));
    }

    public void test_fileExtension() {
        assertEquals("extension exists", ".java",
                StringUtil.fileExtension("C:/a/b/c.java"));
        assertEquals("no extension", null,
                StringUtil.fileExtension("name"));
    }

    public void test_findExtension() {
        assertEquals("extension exists", 8,
                StringUtil.findExtension("C:/a/b/c.java"));
        assertEquals("nothing after the dot", -1,
                StringUtil.findExtension("C:/a/b/c."));
        assertEquals("slash after last dot", -1,
                StringUtil.findExtension("C:/a/b.test/c"));
        assertEquals("backslash after last dot", -1,
                StringUtil.findExtension("C:\\a\\b.test\\c"));
        assertEquals("no slash, no dot", -1,
                StringUtil.findExtension("name"));
    }

    public void test_identifierEnd() {
        assertEquals("stop on dot", 6,
                StringUtil.identifierEnd("a+cdef.ghi", 2));
        assertEquals("stop at end of string", 7,
                StringUtil.identifierEnd("abc123_", 0));
        assertEquals("already past end of string", 10,
                StringUtil.identifierEnd("abc", 10));
    }

    public void test_isWhitespace() {
        assertEquals("all whitespace", true, StringUtil.isWhitespace(" \n\r\t "));
        assertEquals("not all whitespace", false, StringUtil.isWhitespace("  abc "));
    }

    public void test_isWhitespace_characterArray() {
        char[] data = new char[] {'a', ' ', '\n', '\r', '\t', ' ', ' ', 'b'};
        assertEquals("all whitespace", true, StringUtil.isWhitespace(data, 1, 6));
        assertEquals("leading non-whitespace", false,
                StringUtil.isWhitespace(data, 0, 3));
        assertEquals("trailing non-whitespace", false,
                StringUtil.isWhitespace(data, 4, 4));
    }

    public void test_join_stringArray() {
        assertEquals("basics", "first, second, third",
                StringUtil.join(new String[] {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", "",
                StringUtil.join(new String[] {}, ", "));

    }

    public void test_join_objectArray() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        assertEquals("basics", "first, 27, {name=Alice}, 44, last",
                StringUtil.join(new Object[] {"first", 27, map, 44, "last"},
                ", "));
        assertEquals("no values to join", "",
                StringUtil.join(new Object[] {}, ", "));
    }

    public void test_join_iterable() {
        HashMap<Integer,String> hash = new HashMap<Integer, String>();
        hash.put(123, "string_123");
        hash.put(99, "string_99");
        hash.put(-48, "string_-48");
        assertEquals("basics", "-48, 99, 123", StringUtil.join(hash.keySet(), ", "));
        assertEquals("no values to join", "",
                StringUtil.join(new HashMap<String,String>().keySet(), ", "));

    }

    public void test_join_enumeration() {
        Vector<String> list = new Vector<String>();
        list.add("123");
        list.add("99");
        list.add("-48");
        assertEquals("basics", "123, 99, -48", StringUtil.join(list.elements(),
                ", "));
        list.clear();
        assertEquals("no values to join", "", StringUtil.join(list.elements(),
                ", "));

    }

    public void test_join_set() {
        Set<String> set = new HashSet<String>();
        set.add("abcdef");
        set.add("xyz");
        set.add("hijk");
        assertEquals("basics", "abcdef, hijk, xyz", StringUtil.join(set, ", "));
        set.clear();
        assertEquals("no values to join", "", StringUtil.join(set, ", "));
    }

    public void test_joinedLength() {
        assertEquals("basics", 20, StringUtil.joinedLength(new String[]
                {"first", "second", "third"}, ", "));
        assertEquals("no strings to join", 0,
                StringUtil.joinedLength(new String[] {}, ", "));

    }

    public void test_lcFirst() {
        assertEquals("empty string", "", StringUtil.lcFirst(""));
        assertEquals("not uppercase", "12345", StringUtil.lcFirst("12345"));
        assertEquals("already lowercase", "aBCD", StringUtil.lcFirst("aBCD"));
        assertEquals("must convert", "john", StringUtil.lcFirst("John"));
        assertEquals("single character", "x", StringUtil.lcFirst("X"));
    }

    public void test_lengthDecoded4to3() {
        assertEquals("input length 0", 0, StringUtil.lengthDecoded4to3(0));
        assertEquals("input length 8", 6, StringUtil.lengthDecoded4to3(8));
        assertEquals("input length 10", 7, StringUtil.lengthDecoded4to3(10));
        assertEquals("input length 11", 8, StringUtil.lengthDecoded4to3(11));
        assertEquals("input length 12", 9, StringUtil.lengthDecoded4to3(12));
    }

    public void test_lengthEncoded3to4() {
        assertEquals("input length 0", 0, StringUtil.lengthEncoded3to4(0));
        assertEquals("input length 6", 8, StringUtil.lengthEncoded3to4(6));
        assertEquals("input length 7", 10, StringUtil.lengthEncoded3to4(7));
        assertEquals("input length 8", 11, StringUtil.lengthEncoded3to4(8));
        assertEquals("input length 9", 12, StringUtil.lengthEncoded3to4(9));
    }

    public void test_newCharArray() {
        char[] ch = StringUtil.newCharArray("abc");
        assertEquals("array length", 3, ch.length);
        assertEquals("array contents", "abc", new String(ch));
    }

    public void test_numberEnd() {
        assertEquals("stop on alpha", 3,
                StringUtil.numberEnd("123a", 2));
        assertEquals("stop at end of string", 4,
                StringUtil.numberEnd("1234", 0));
        assertEquals("already past end of string", 10,
                StringUtil.numberEnd("123", 10));
    }

    public void test_skipSpaces() {
        assertEquals(6, StringUtil.skipSpaces("abc   def", 3));
        assertEquals(6, StringUtil.skipSpaces("abc   def", 5));
        assertEquals(6, StringUtil.skipSpaces("abc   def", 6));
        assertEquals(9, StringUtil.skipSpaces("abc   def", 9));
        assertEquals("already past end of string", 6,
                StringUtil.skipSpaces("abc   ", 4));
    }

    public void test_skipSpaces_withEnd() {
        assertEquals(5, StringUtil.skipSpaces("abc   def", 3, 5));
        assertEquals(6, StringUtil.skipSpaces("abc   def", 5, 8));
        assertEquals(5, StringUtil.skipSpaces("abc   def", 5, 5));
    }

    public void test_split() {
        String[] result = StringUtil.split("first, second,,third ", ',');
        String[] result2 = StringUtil.split("  no separators", ',');
        String[] result3 = StringUtil.split("  ", ',');
        String[] result4 = StringUtil.split(":  ", ':');
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

    public void test_ucFirst() {
        assertEquals("empty string", "", StringUtil.ucFirst(""));
        assertEquals("not lowercase", "12345", StringUtil.ucFirst("12345"));
        assertEquals("already uppercase", "John", StringUtil.ucFirst("John"));
        assertEquals("must convert", "Bermuda", StringUtil.ucFirst("bermuda"));
        assertEquals("single character", "I", StringUtil.ucFirst("i"));
    }

    public void test_addSuffix() {
        assertEquals("no dot", "firstsecond", StringUtil.addSuffix("first", "second"));
        assertEquals("regular", "test-foo.png",
                     StringUtil.addSuffix("test.png", "-foo"));
    }
}
