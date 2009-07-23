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
 * Junit tests for the Template class.
 */

public class TemplateTest extends junit.framework.TestCase {
    // The following class definition provides a mechanism for accessing
    // protected/private fields and methods.
    protected static class TemplateFixture {
        public static int templateEnd;
        public static boolean missingData;
        public static boolean conditional;
        public static boolean skip;
        public static int end;
        public static int lastDeletedSpace;
        public static Template.SpecialChars currentQuoting;

        public static void expandRange(String template, Dataset data,
                StringBuilder out, int start, int end) {
            Template.ParseInfo info = new Template.ParseInfo(template,
                    out, Template.SpecialChars.NONE, data, null);
            info.templateEnd = -15;
            info.skip = false;
            info.end = -1;
            Template.expandRange(info, start, end);
            templateEnd = info.templateEnd;
            TemplateFixture.end = info.end;
        }

        public static void expandAtSign(String template, Dataset data,
                StringBuilder out, boolean conditional, int start) {
            Template.ParseInfo info = new Template.ParseInfo(template,
                    out, Template.SpecialChars.NONE, data, null);
            info.templateEnd = template.length();
            info.ignoreMissing = conditional;
            info.skip = false;
            info.end = -1;
            Template.expandAtSign(info, start);
            missingData = info.missingData;
            end = info.end;
        }

        public static void expandChoice(String template, Dataset data,
                StringBuilder out, String name, int start) {
            Template.ParseInfo info = new Template.ParseInfo(template,
                    out, Template.SpecialChars.HTML, data, null);
            info.templateEnd = template.length();
            info.skip = false;
            info.end = -1;
            Template.expandChoice(info, name, start);
            end = info.end;
        }

        public static void expandParenName(String template, Dataset data,
                StringBuilder out, boolean conditional, int start,
                Template.SpecialChars encoding) {
            Template.ParseInfo info = new Template.ParseInfo(template,
                    out, encoding, data, null);
            info.templateEnd = template.length();
            info.ignoreMissing = conditional;
            info.skip = false;
            info.end = -1;
            Template.expandParenName(info, start);
            missingData = info.missingData;
            end = info.end;
        }

        public static void appendValue(String name, Dataset data,
                StringBuilder out, boolean conditional,
                Template.SpecialChars encoding,
                ArrayList<String> sqlParameters) {
            Template.ParseInfo info = new Template.ParseInfo("dummy template",
                    out, encoding, data, null);
            info.sqlParameters = sqlParameters;
            info.ignoreMissing = conditional;
            info.skip = false;
            info.end = -1;
            Template.appendValue(info, name);
            missingData = info.missingData;
            end = info.end;
        }

        public static void expandBraces(String template, Dataset data,
                StringBuilder out, int start, int lastDeletedSpace) {
            Template.ParseInfo info = new Template.ParseInfo(template,
                    out, Template.SpecialChars.NONE, data, null);
            info.templateEnd = template.length();
            info.missingData = true;
            info.skip = false;
            info.end = -1;
            info.lastDeletedSpace = lastDeletedSpace;
            Template.expandBraces(info, start);
            missingData = info.missingData;
            conditional = info.ignoreMissing;
            end = info.end;
            TemplateFixture.lastDeletedSpace = info.lastDeletedSpace;
        }

        public static void expandBraces(String template, Dataset data,
                StringBuilder out, int start) {
            expandBraces(template, data, out, start, start-1);
        }

        public static int skipTo(String template, StringBuilder out,
                int start, char c1, char c2) {
            Template.ParseInfo info = new Template.ParseInfo(template,
                    out, Template.SpecialChars.NONE, new Dataset(), null);
            info.templateEnd = template.length();
            info.skip = false;
            info.end = -1;
            info.lastDeletedSpace = lastDeletedSpace;
            int result = Template.skipTo(info, start, c1, c2);
            missingData = info.missingData;
            conditional = info.ignoreMissing;
            skip = info.skip;
            end = info.end;
            TemplateFixture.lastDeletedSpace = info.lastDeletedSpace;
            return result;
        }

        public static String findValue(String name, boolean required,
                Dataset data, Template.SpecialChars quoting,
                Object[] indexedData, Template.SpecialChars indexedQuoting) {
            Template.ParseInfo info = new Template.ParseInfo(
                    "dummy template", null, quoting, data, indexedData);
            info.indexedQuoting = indexedQuoting;
            String result = Template.findValue(info, name, required);
            currentQuoting = info.currentQuoting;
            return result;
        }
    }

    public void test_missingValueError() {
        Template.MissingValueError e = new Template.MissingValueError(
                "sample message");
        assertEquals("error message", "sample message", e.getMessage());
    }

    public void test_syntaxError() {
        Template.SyntaxError e = new Template.SyntaxError(
                "sample message");
        assertEquals("error message", "sample message", e.getMessage());
    }

    public void test_expandHtml() {
        Dataset data = new Dataset("name", "<Alice>", "age", "28");
        String result = Template.expandHtml("name: @name, age: @2",
                data, "abc", "25");
        assertEquals("output string", "name: &lt;Alice&gt;, age: 25",
                result);
    }

    public void test_expandHtml_noDataset() {
        String result = Template.expandHtml("name: @1, age: @2",
                "<Alice>", "25");
        assertEquals("output string", "name: &lt;Alice&gt;, age: 25",
                result);
    }

    public void test_appendHtml() {
        Dataset data = new Dataset("name", "<Alice>", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.appendHtml(out, "name: @name, age: @2",
                data, "abc", "25");
        assertEquals("output string", "123name: &lt;Alice&gt;, age: 25",
                out.toString());
    }

    public void test_appendHtml_noDataset() {
        StringBuilder out = new StringBuilder("123");
        Template.appendHtml(out, "name: @1, age: @2", "<Alice>", "25");
        assertEquals("output string", "123name: &lt;Alice&gt;, age: 25",
                out.toString());
    }

    public void test_expandJavascript() {
        Dataset data = new Dataset("name", "\"Alice\n", "age", "28");
        String result = Template.expandJavascript("name: @name, age: @2",
                data, "abc", "a\tb");
        assertEquals("output string", "name: \\\"Alice\\n, age: a\\tb",
                result);
    }

    public void test_expandJavascript_noDataset() {
        String result = Template.expandJavascript("name: @1, age: @2",
                "\"Alice\n", "a\tb");
        assertEquals("output string", "name: \\\"Alice\\n, age: a\\tb",
                result);
    }

    public void test_appendJavascript() {
        Dataset data = new Dataset("name", "\"Alice\n", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.appendJavascript(out, "name: @name, age: @2",
                data, "abc", "a\tb");
        assertEquals("output string", "123name: \\\"Alice\\n, age: a\\tb",
                out.toString());
    }

    public void test_appendJavascript_noDataset() {
        StringBuilder out = new StringBuilder("123");
        Template.appendJavascript(out, "name: @1, age: @2", "\"Alice\n", "25");
        assertEquals("output string", "123name: \\\"Alice\\n, age: 25",
                out.toString());
    }

    public void test_expandUrl() {
        Dataset data = new Dataset("name", "C&H Sugar");
        String result = Template.expandUrl("/a/b?name=@name&count=@2",
                data, "abc", 99);
        assertEquals("output string", "/a/b?name=C%26H+Sugar&count=99",
                result);
    }

    public void test_expandUrl_noDataset() {
        String result = Template.expandUrl("/a/b?name=@1&count=@2",
                "C&H Sugar", 99);
        assertEquals("output string", "/a/b?name=C%26H+Sugar&count=99",
                result);
    }

    public void test_appendUrl() {
        Dataset data = new Dataset("name", "C&H Sugar", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.appendUrl(out, "/a/b?name=@name&count=@2",
                data, "abc", 99);
        assertEquals("output string", "123/a/b?name=C%26H+Sugar&count=99",
                out.toString());
    }

    public void test_appendUrl_noDataset() {
        StringBuilder out = new StringBuilder("123");
        Template.appendUrl(out, "/a/b?name=@1&count=@2", "C&H Sugar", 99);
        assertEquals("output string", "123/a/b?name=C%26H+Sugar&count=99",
                out.toString());
    }

    public void test_expandRaw() {
        Dataset data = new Dataset("x", "<\">");
        String result = Template.expandRaw("first: @x, second: @2",
                data, "abc", "&\n");
        assertEquals("output string", "first: <\">, second: &\n",
                result);
    }

    public void test_expandRaw_noDataset() {
        String result = Template.expandRaw("first: @1, second: @2",
                "<\">", "&\n");
        assertEquals("output string", "first: <\">, second: &\n",
                result);
    }

    public void test_appendRaw() {
        Dataset data = new Dataset("x", "<\">");
        StringBuilder out = new StringBuilder("123");
        Template.appendRaw(out, "first: @x, second: @2",
                data, "abc", "&\n");
        assertEquals("output string", "123first: <\">, second: &\n",
                out.toString());
    }

    public void test_appendRaw_noDataset() {
        StringBuilder out = new StringBuilder("123");
        Template.appendRaw(out, "first: @1, second: @2", "<\">", "&\n");
        assertEquals("output string", "123first: <\">, second: &\n",
                out.toString());
    }

    public void test_expandSql() {
        ArrayList<String> parameters = new ArrayList<String>();
        String result = Template.expandSql("SELECT * FROM people " +
                "WHERE name=@name AND age=@age;",
                new Dataset("name", "\"Alice\"", "age", "28"), parameters);
        assertEquals("output string",
                "SELECT * FROM people WHERE name=? AND age=?;",
                result);
        assertEquals("saved variables", "\"Alice\", 28",
                StringUtil.join(parameters, ", "));
    }
    public void test_expandSql_withConditional() {
        ArrayList<String> parameters = new ArrayList<String>();
        String result = Template.expandSql("xyz{{@a @x @c}}abc{{b:@b}}",
                new Dataset("a", "0", "b", "1", "c", "2"), parameters);
        assertEquals("output string", "xyzabcb:?",
                result);
        assertEquals("saved variables", "1",
                StringUtil.join(parameters, ", "));
    }
    public void test_expandSql_withSkips() {
        ArrayList<String> parameters = new ArrayList<String>();
        String result = Template.expandSql("@a?{b:@b} @x?{a:@a|c:@c}",
                new Dataset("a", "0", "b", "1", "c", "2"), parameters);
        assertEquals("output string", "? c:?",
                result);
        assertEquals("saved variables", "0, 2",
                StringUtil.join(parameters, ", "));
    }

    public void test_expandRange_atSign() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandRange("---name: @name ---", data, out, 2, 16);
        assertEquals("output string", "123-name: Alice -", out.toString());
    }
    public void test_expandRange_openBracesAtEnd() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandRange("abc{{x", data, out, 0, 4);
        assertEquals("output string", "123abc{", out.toString());
        boolean gotException = false;
        try {
            TemplateFixture.expandRange("abc{{", data, out, 1, 5);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "unmatched \"{{\" in template \"abc{{\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expandRange_singleBrace() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandRange("{}}", data, out, 0, 3);
        assertEquals("output string", "123{}}", out.toString());
    }
    public void test_expandRange_restoreTemplateEnd() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandRange("name: @name", data, out, 0, 11);
        assertEquals("output string", "123name: Alice", out.toString());
        assertEquals("info.templateEnd", -15, TemplateFixture.templateEnd);
    }

    public void test_expandAtSign_nothingAfterAtSign() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandAtSign("xx@", data, out, false, 3);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "dangling \"@\" in template \"xx@\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expandAtSign_choice() {
        Dataset data = new Dataset("age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.appendHtml(out, "name: @name?{unknown}, xyz", data, out);
        assertEquals("output string", "123name: unknown, xyz", out.toString());
    }
    public void test_expandAtSign_simpleName() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandAtSign("xx@name + foo", data, out, false, 3);
        assertEquals("end of specifier", 7, TemplateFixture.end);
        assertEquals("output string", "123Alice", out.toString());
    }
    public void test_expandAtSign_simpleNameStartsWithDigit() {
        Dataset data = new Dataset("444", "Alice");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandAtSign("xx@444 + foo", data, out, false, 3);
        assertEquals("end of specifier", 6, TemplateFixture.end);
        assertEquals("output string", "123Alice", out.toString());
    }
    public void test_expandAtSign_nameInParens() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandAtSign("xx@(name)", data, out, false, 3);
        assertEquals("end of specifier", 9, TemplateFixture.end);
        assertEquals("output string", "123Alice", out.toString());
    }
    public void test_expandAtSign_atSign() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandAtSign("xx@@yyy", data, out, false, 3);
        assertEquals("end of specifier", 4, TemplateFixture.end);
        assertEquals("output string", "123@", out.toString());
    }
    public void test_expandAtSign_openBrace() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandAtSign("xx@{yyy", data, out, false, 3);
        assertEquals("end of specifier", 4, TemplateFixture.end);
        assertEquals("output string", "123{", out.toString());
    }
    public void test_expandAtSign_closeBrace() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandAtSign("xx@}yyy", data, out, false, 3);
        assertEquals("end of specifier", 4, TemplateFixture.end);
        assertEquals("output string", "123}", out.toString());
    }
    public void test_expandAtSign_illegalCharacter() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandAtSign("xx@+yyy", data, out, false, 3);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "invalid sequence \"@+\" in template \"xx@+yyy\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_expandChoice_endOfTemplate() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandChoice("@foo?", data, out, "name", 5);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "missing \"{\" after \"?\" in template \"@foo?\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expandChoice_braceMissing() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandChoice("@foo?(", data, out, "name", 5);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "missing \"{\" after \"?\" in template \"@foo?(\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expandChoice_noBarOrCloseBrace() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandChoice("@foo?{aaa", data, out, "name", 5);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "incomplete @...?{...} substitution in template " +
                    "\"@foo?{aaa\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expandChoice_noVerticalBar_variableExists() {
        Dataset data = new Dataset("name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandChoice("@foo?{aaa}zz", data, out, "name", 5);
        assertEquals("output string", "123&lt;West&gt;", out.toString());
        assertEquals("info.end", 10, TemplateFixture.end);
    }
    public void test_expandChoice_noVerticalBar_variableDoesntExist() {
        Dataset data = new Dataset("age", "50");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandChoice("@foo?{@age!}zz", data, out,
                    "name", 5);
        assertEquals("output string", "12350!", out.toString());
        assertEquals("info.end", 12, TemplateFixture.end);
    }
    public void test_expandChoice_barButNoCloseBrace() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandChoice("@foo?{aaa|zz", data, out, "name", 5);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "incomplete @...?{...} substitution in template " +
                    "\"@foo?{aaa|zz\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expandChoice_verticalBar_variableExists() {
        Dataset data = new Dataset("name", "<West>", "age", "24",
                "weight", "125");
        StringBuilder out = new StringBuilder("123");
        String template = "@foo?{(@age)|[@weight]}zz";
        TemplateFixture.expandChoice(template, data, out, "name", 5);
        assertEquals("output string", "123(24)", out.toString());
        assertEquals("remainder of template", "zz",
                template.substring(TemplateFixture.end));
    }
    public void test_expandChoice_verticalBar_variableDoesntExist() {
        Dataset data = new Dataset("age", "24", "weight", "125");
        StringBuilder out = new StringBuilder("123");
        String template = "@foo?{(@age)|[@weight]}zz";
        TemplateFixture.expandChoice(template, data, out, "name", 5);
        assertEquals("output string", "123[125]", out.toString());
        assertEquals("remainder of template", "zz",
                template.substring(TemplateFixture.end));
    }

    public void test_expandParenName_saveAndRestoreEncoding() {
        Dataset data = new Dataset("<name>", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandParenName("@(<name>)", data, out, false, 2,
                Template.SpecialChars.HTML);
        assertEquals("output string", "123&lt;West&gt;", out.toString());
    }
    public void test_expandParenName_nestedAtSign() {
        Dataset data = new Dataset("last_name", "West", "name", "last_name");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandParenName("@(@name)", data, out, false, 2,
                Template.SpecialChars.NONE);
        assertEquals("end of specifier", 8, TemplateFixture.end);
        assertEquals("missingData", false, TemplateFixture.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void test_expandParenName_doublyNestedAtSign() {
        Dataset data = new Dataset("last_name", "West", "name", "name2",
                "name2", "last_name");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandParenName("@(@(@name))", data, out, false, 2,
                Template.SpecialChars.NONE);
        assertEquals("end of specifier", 11, TemplateFixture.end);
        assertEquals("missingData", false, TemplateFixture.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void test_expandParenName_concatenation() {
        Dataset data = new Dataset("last_name", "West", "name1", "last",
                "name2", "name");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandParenName("@(@(name1)_@name2)", data, out, false, 2,
                Template.SpecialChars.NONE);
        assertEquals("end of specifier", 18, TemplateFixture.end);
        assertEquals("missingData", false, TemplateFixture.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void test_expandParenName_missingCloseParen() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandParenName("@(abcdef", data, out, false, 2,
                    Template.SpecialChars.NONE);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "missing \")\" for \"@(\" in template \"@(abcdef\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_appendValue_conditionalSucceeded() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, true,
                Template.SpecialChars.HTML, null);
        assertEquals("missingData", false, TemplateFixture.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void test_appendValue_conditionalFailed() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("first_name", data, out, true,
                Template.SpecialChars.HTML, null);
        assertEquals("missingData", true, TemplateFixture.missingData);
        assertEquals("output string", "123", out.toString());
    }
    public void test_appendValue_emptyConditionalValue() {
        Dataset data = new Dataset("first_name", "");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("first_name", data, out, true,
                Template.SpecialChars.HTML, null);
        assertEquals("missingData", true, TemplateFixture.missingData);
        assertEquals("output string", "123", out.toString());
    }
    public void test_appendValue_unconditionalSucceeded() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.HTML, null);
        assertEquals("missingData", false, TemplateFixture.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void test_appendValue_unconditionalFailed() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.appendValue("last_name", data, out, false,
                    Template.SpecialChars.HTML, null);
        }
        catch (Template.MissingValueError e) {
            assertEquals("exception message",
                    "missing value \"last_name\" in template " +
                    "\"dummy template\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_appendValue_HtmlEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.HTML, null);
        assertEquals("output string", "123&lt;West&gt;", out.toString());
    }
    public void test_appendValue_UrlEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.URL, null);
        assertEquals("output string", "123%3cWest%3e", out.toString());
    }
    public void test_appendValue_JavascriptEncoding() {
        Dataset data = new Dataset("value", "\\\n\"");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("value", data, out, false,
                Template.SpecialChars.JAVASCRIPT, null);
        assertEquals("output string", "123\\\\\\n\\\"", out.toString());
    }
    public void test_appendValue_NoEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.NONE, null);
        assertEquals("output string", "123<West>", out.toString());
    }
    public void test_appendValue_Sql() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        ArrayList<String> parameters = new ArrayList<String>();
        TemplateFixture.appendValue("last_name", data, out, false,
                null, parameters);
        assertEquals("output string", "123?", out.toString());
        assertEquals("saved variables", "<West>",
                StringUtil.join(parameters, ", "));
    }

    public void test_expandBraces_basics() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandBraces("{{abc@last_name@}}x}}y", data, out, 2);
        assertEquals("end of specifier", 21, TemplateFixture.end);
        assertEquals("ignoreMissing", false, TemplateFixture.conditional);
        assertEquals("output string", "123abcWest}}x", out.toString());
    }
    public void test_expandBraces_closeBracesAtEnd() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandBraces("{{abc}}", data, out, 2);
        assertEquals("output string", "123abc", out.toString());
    }
    public void test_expandBraces_singleCloseBraceAtEnd() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        boolean gotException = false;
        try {
            TemplateFixture.expandBraces("{{abc}", data, out, 2);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "unmatched \"{{\" in template \"{{abc}\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expandBraces_missingData() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandBraces("{{abc@data.47@data2}}x", data, out, 2);
        assertEquals("end of specifier", 21, TemplateFixture.end);
        assertEquals("ignoreMissing", false, TemplateFixture.conditional);
        assertEquals("output string", "123", out.toString());
    }
    public void test_expandBraces_collapsePrecedingSpace() {
        Dataset data = new Dataset();
        StringBuilder out = new StringBuilder();
        Template.appendHtml(out, "x {{@foo}}", data);
        assertEquals("braces at end of string", "x", out.toString());
        out.setLength(0);
        Template.appendHtml(out, "- {{@foo}} -", data);
        assertEquals("spaces on both sides", "- -", out.toString());
        out.setLength(0);
        Template.appendHtml(out, "x {{@x}}] x {{@x}}> x {{@x}}} x {{@x}}) x "
                + "{{@x}}\" x {{@x}}\'",
                data);
        assertEquals("close-delimiter follows braces", "x] x> x} x) x\" x'",
                out.toString());
        out.setLength(0);
        out.append("x ");
        TemplateFixture.expandBraces("x {{@x}}]", data, out, 4, -1);
        assertEquals("update lastDeletedSpace", 1,
                TemplateFixture.lastDeletedSpace);
        out.setLength(0);
        Template.appendHtml(out, "<{{@x}} {{@x}}>", data);
        assertEquals("don't delete the same space twice", "<>",
                out.toString());
        out.setLength(0);
        Template.appendHtml(out, "{{@x}}) + b{{@x}}> + {{@x}}y", data);
        assertEquals("don't remove spaces", ") + b> + y",
                out.toString());
    }
    public void test_expandBraces_collapseTrailingSpace() {
        Dataset data = new Dataset();
        StringBuilder out = new StringBuilder();
        Template.appendHtml(out, "{{@x}} bcd", data);
        assertEquals("braces at start of string", "bcd", out.toString());
        out.setLength(0);
        Template.appendHtml(out, "[{{@x}} + <{{@x}} + @{{{@x}} + ({{@x}} + "
                + "\"{{@x}} + \'{{@x}} +",
                data);
        assertEquals("open-delimiter precedes braces", "[+ <+ {+ (+ \"+ '+",
                out.toString());
        out.setLength(0);
        TemplateFixture.expandBraces("<{{@x}} abc]", data, out, 3, -1);
        assertEquals("update lastDeletedSpace", 7,
                TemplateFixture.lastDeletedSpace);
        out.setLength(0);
        Template.appendHtml(out, "<{{@x}}abc + y{{@x}}y + <{{@x}}", data);
        assertEquals("don't remove spaces", "<abc + yy + <",
                out.toString());
    }
    public void test_expandBraces_discardCollectedVariables() {
        Dataset data = new Dataset("a", "0", "x", "1", "y", "2");
        ArrayList<String> parameters = new ArrayList<String>();
        String out = Template.expandSql("a:@a {{x:@x x:@x z:@z}} y:@y", data, parameters);
        assertEquals("expanded template", "a:? y:?", out);
        assertEquals("saved variables", "0, 2",
                StringUtil.join(parameters, ", "));
    }
    public void test_expandEmbraces_missingCloseBrace() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.expandBraces("{{abc", data, out, 2);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "unmatched \"{{\" in template \"{{abc\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_skipTo_basics() {
        StringBuilder out = new StringBuilder();
        int result = TemplateFixture.skipTo("}bc@}}", out, 1, '}', '}');
        assertEquals("result index", 5, result);
    }
    public void test_skipTo_atSign() {
        StringBuilder out = new StringBuilder();
        String template = "@(first})@second@}}123";
        int result = TemplateFixture.skipTo(template, out, 0, '}', '}');
        assertEquals("remainder of template", "}123",
                template.substring(result));
    }
    public void test_skipTo_doubleBraces() {
        StringBuilder out = new StringBuilder();
        String template = "{{xyz@(foo})x}yy@}}}}123";
        int result = TemplateFixture.skipTo(template, out, 0, '}', '}');
        assertEquals("remainder of template", "}123",
                template.substring(result));
    }
    public void test_skipTo_checkTwoCharacters() {
        StringBuilder out = new StringBuilder();
        String template = "abc,xyz|123";
        int result = TemplateFixture.skipTo(template, out, 0, '|', ',');
        assertEquals("remainder of template", ",xyz|123",
                template.substring(result));
        template = "abc|xyz,123";
        result = TemplateFixture.skipTo(template, out, 0, '|', ',');
        assertEquals("remainder of template", "|xyz,123",
                template.substring(result));
    }
    public void test_skipTo_restoreState() {
        StringBuilder out = new StringBuilder("abc");
        int result = TemplateFixture.skipTo("12345", out, 0, '|', ',');
        assertEquals("truncate output", "abc", out.toString());
        assertEquals("truncate output", false, TemplateFixture.skip);
    }

    public void test_findValue_indexedData() {
        Object[] indexedData = new Object[] {"first", null, "third"};
        String result = TemplateFixture.findValue("0", false, null, null,
                indexedData, null);
        assertEquals("index too small", null, result);
        result = TemplateFixture.findValue("4", false, null, null,
                indexedData, null);
        assertEquals("index too large", null, result);
        result = TemplateFixture.findValue("1", false, null, null,
                indexedData, Template.SpecialChars.HTML);
        assertEquals("index valid", "first", result);
        assertEquals("currentQuoting", "HTML",
                TemplateFixture.currentQuoting.toString());
        result = TemplateFixture.findValue("2", false, null, null,
                indexedData, null);
        assertEquals("null object supplied", null, result);
    }
    public void test_findValue_datasetValue() {
        Dataset data = new Dataset("name", "Alice", "age", "21");
        String result = TemplateFixture.findValue("bogus", false, data, null,
                null, null);
        assertEquals("name not in dataset", null, result);
        result = TemplateFixture.findValue("bogus", false, data,
                Template.SpecialChars.NONE, null, null);
        assertEquals("name in dataset", null, result);
        assertEquals("currentQuoting", "NONE",
                TemplateFixture.currentQuoting.toString());
    }
    public void test_findValue_throwError() {
        Dataset data = new Dataset("name", "Alice", "age", "21");
        boolean gotException = false;
        try {
            String result = TemplateFixture.findValue("6", true,
                    new Dataset("name", "Alice"), null,
                    new Object[] {"first"}, null);
        }
        catch (Template.MissingValueError e) {
            assertEquals("exception message",
                    "missing value \"6\" in template \"dummy template\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_variousComplexTemplates() {
        StringBuilder out = new StringBuilder();
        Template.appendHtml(out, "first {{name: @name, age: @age?{unknown}}}",
                new Dataset("name", "Bob"));
        assertEquals("output string", "first name: Bob, age: unknown",
                out.toString());

        out.setLength (0);
        Template.appendHtml(out, "first {{name: @name, age: @age?{unknown}, " +
                "weight: @weight}}", new Dataset("name", "Bob"));
        assertEquals("output string", "first", out.toString());
    }
}
