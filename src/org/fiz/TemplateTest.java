/**
 * Junit tests for the Template class.
 */

package org.fiz;

public class TemplateTest extends junit.framework.TestCase {
    public void testExpand_basics() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.expand("name: @name, age: @age, {weight: @weight} misc: @{@}",
                data, out);
        assertEquals("output string", "123name: Alice, age: 28, misc: {}",
                out.toString());
    }
    public void testExpand_encoding() {
        Dataset data = new Dataset("name", "<Alice>", "age", "28");
        StringBuilder out = new StringBuilder();
        Template.expand("<name>: @name", data, out);
        assertEquals("output string", "<name>: &lt;Alice&gt;", out.toString());
        out.setLength(0);
        Template.expand("<name>: @name", data, out, Template.Encoding.URL);
        assertEquals("output string", "<name>: %3cAlice%3e", out.toString());
    }

    public void testExpandAtSign_simpleName() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandAtSign("xx@name + foo", data, out, false, 3);
        assertEquals("end of specifier", 7, TestTemplate.end);
        assertEquals("output string", "123Alice", out.toString());
    }
    public void testExpandAtSign_nameInParens() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandAtSign("xx@(name)", data, out, false, 3);
        assertEquals("end of specifier", 9, TestTemplate.end);
        assertEquals("output string", "123Alice", out.toString());
    }
    public void testExpandAtSign_atSign() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandAtSign("xx@@yyy", data, out, false, 3);
        assertEquals("end of specifier", 4, TestTemplate.end);
        assertEquals("output string", "123@", out.toString());
    }
    public void testExpandAtSign_openBrace() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandAtSign("xx@{yyy", data, out, false, 3);
        assertEquals("end of specifier", 4, TestTemplate.end);
        assertEquals("output string", "123{", out.toString());
    }
    public void testExpandAtSign_closeBrace() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandAtSign("xx@}yyy", data, out, false, 3);
        assertEquals("end of specifier", 4, TestTemplate.end);
        assertEquals("output string", "123}", out.toString());
    }
    public void testExpandAtSign_illegalCharacter() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TestTemplate.expandAtSign("xx@+yyy", data, out, false, 3);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "invalid sequence \"@+\" in template \"xx@+yyy\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void testExpandParenName_saveAndRestoreEncoding() {
        Dataset data = new Dataset("<name>", "<West>");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandParenName("@(<name>)", data, out, false, 2,
                Template.Encoding.HTML);
        assertEquals("output string", "123&lt;West&gt;", out.toString());
    }
    public void testExpandParenName_nestedAtSign() {
        Dataset data = new Dataset("last_name", "West", "name", "last_name");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandParenName("@(@name)", data, out, false, 2,
                Template.Encoding.NONE);
        assertEquals("end of specifier", 8, TestTemplate.end);
        assertEquals("missingData", false, TestTemplate.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void testExpandParenName_doublyNestedAtSign() {
        Dataset data = new Dataset("last_name", "West", "name", "name2",
                "name2", "last_name");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandParenName("@(@(@name))", data, out, false, 2,
                Template.Encoding.NONE);
        assertEquals("end of specifier", 11, TestTemplate.end);
        assertEquals("missingData", false, TestTemplate.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void testExpandParenName_concatenation() {
        Dataset data = new Dataset("last_name", "West", "name1", "last",
                "name2", "name");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandParenName("@(@(name1)_@name2)", data, out, false, 2,
                Template.Encoding.NONE);
        assertEquals("end of specifier", 18, TestTemplate.end);
        assertEquals("missingData", false, TestTemplate.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void testExpandParenName_missingCloseParen() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TestTemplate.expandParenName("@(abcdef", data, out, false, 2,
                    Template.Encoding.NONE);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "missing \")\" for \"@(\" in template \"@(abcdef\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void testAppendValue_conditionalSucceeded() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.appendValue("last_name", data, out, true,
                Template.Encoding.HTML);
        assertEquals("missingData", false, TestTemplate.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void testAppendValue_conditionalFailed() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.appendValue("first_name", data, out, true,
                Template.Encoding.HTML);
        assertEquals("missingData", true, TestTemplate.missingData);
        assertEquals("output string", "123", out.toString());
    }
    public void testAppendValue_unconditionalSucceeded() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.appendValue("last_name", data, out, false,
                Template.Encoding.HTML);
        assertEquals("missingData", false, TestTemplate.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void testAppendValue_unconditionalFailed() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TestTemplate.appendValue("last_name", data, out, false,
                    Template.Encoding.HTML);
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"last_name\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void testAppendValue_HtmlEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.appendValue("last_name", data, out, false,
                Template.Encoding.HTML);
        assertEquals("output string", "123&lt;West&gt;", out.toString());
    }
    public void testAppendValue_UrlEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.appendValue("last_name", data, out, false,
                Template.Encoding.URL);
        assertEquals("output string", "123%3cWest%3e", out.toString());
    }
    public void testAppendValue_NoEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.appendValue("last_name", data, out, false,
                Template.Encoding.NONE);
        assertEquals("output string", "123<West>", out.toString());
    }

    public void testExpandBraces_basics() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandBraces("{abc@last_name@}}x", data, out, 1);
        assertEquals("end of specifier", 17, TestTemplate.end);
        assertEquals("conditional", false, TestTemplate.conditional);
        assertEquals("output string", "123abcWest}", out.toString());
    }
    public void testExpandBraces_missingData() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TestTemplate.expandBraces("{abc@data.47@data2}x", data, out, 1);
        assertEquals("end of specifier", 19, TestTemplate.end);
        assertEquals("conditional", false, TestTemplate.conditional);
        assertEquals("output string", "123", out.toString());
    }
    public void testExpandBraces_collapsePrecedingSpace() {
        Dataset data = new Dataset();
        StringBuilder out = new StringBuilder();
        Template.expand("x {@foo}", data, out);
        assertEquals("braces at end of string", "x", out.toString());
        out.setLength(0);
        Template.expand("- {@foo} -", data, out);
        assertEquals("spaces on both sides", "- -", out.toString());
        out.setLength(0);
        Template.expand("x {@x}] x {@x}> x {@x}} x {@x}) x {@x}\" x {@x}\'",
                data, out);
        assertEquals("close-delimiter follows braces", "x] x> x} x) x\" x'",
                out.toString());
        out.setLength(0);
        out.append("x ");
        TestTemplate.expandBraces("x {@x}]", data, out, 3, -1);
        assertEquals("update lastDeletedSpace", 1,
                TestTemplate.lastDeletedSpace);
        out.setLength(0);
        Template.expand("<{@x} {@x}>", data, out);
        assertEquals("don't delete the same space twice", "<>",
                out.toString());
        out.setLength(0);
        Template.expand("{@x}) + b{@x}> + {@x}y", data, out);
        assertEquals("don't remove spaces", ") + b> + y",
                out.toString());
    }
    public void testExpandBraces_collapseTrailingSpace() {
        Dataset data = new Dataset();
        StringBuilder out = new StringBuilder();
        Template.expand("{@x} bcd", data, out);
        assertEquals("braces at start of string", "bcd", out.toString());
        out.setLength(0);
        Template.expand("[{@x} + <{@x} + @{{@x} + ({@x} + \"{@x} + \'{@x} +",
                data, out);
        assertEquals("open-delimiter precedes braces", "[+ <+ {+ (+ \"+ '+",
                out.toString());
        out.setLength(0);
        TestTemplate.expandBraces("<{@x} abc]", data, out, 2, -1);
        assertEquals("update lastDeletedSpace", 5,
                TestTemplate.lastDeletedSpace);
        out.setLength(0);
        Template.expand("<{@x}abc + y{@x}y + <{@x}", data, out);
        assertEquals("don't remove spaces", "<abc + yy + <",
                out.toString());
    }
    public void testExpandEmbraces_missingCloseBrace() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TestTemplate.expandBraces("{abc", data, out, 1);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "missing \"}\" in template \"{abc\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}

// The following class definition provides a mechanism for accessing
// protected/private fields and methods.
class TestTemplate extends Template {
    public static boolean missingData;
    public static boolean conditional;
    public static int end;
    public static int lastDeletedSpace;

    public static void expandAtSign(String template, Dataset data,
            StringBuilder out, boolean conditional, int start) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.data = data;
        info.out = out;
        info.conditional = conditional;
        info.missingData = false;
        info.end = -1;
        Template.expandAtSign(info, start);
        missingData = info.missingData;
        end = info.end;
    }

    public static void expandParenName(String template, Dataset data,
            StringBuilder out, boolean conditional, int start,
            Template.Encoding encoding) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.data = data;
        info.out = out;
        info.encoding = encoding;
        info.conditional = conditional;
        info.missingData = false;
        info.end = -1;
        Template.expandParenName(info, start);
        missingData = info.missingData;
        end = info.end;
    }

    public static void appendValue(String name, Dataset data,
            StringBuilder out, boolean conditional,
            Template.Encoding encoding) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.data = data;
        info.out = out;
        info.encoding = encoding;
        info.conditional = conditional;
        info.missingData = false;
        info.end = -1;
        Template.appendValue(info, name);
        missingData = info.missingData;
        end = info.end;
    }

    public static void expandBraces(String template, Dataset data,
            StringBuilder out, int start, int lastDeletedSpace) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.data = data;
        info.out = out;
        info.conditional = false;
        info.missingData = true;
        info.end = -1;
        info.lastDeletedSpace = lastDeletedSpace;
        Template.expandBraces(info, start);
        missingData = info.missingData;
        conditional = info.conditional;
        end = info.end;
        TestTemplate.lastDeletedSpace = info.lastDeletedSpace;
    }
    public static void expandBraces(String template, Dataset data,
            StringBuilder out, int start) {
        expandBraces(template, data, out, start, start-1);
    }
}
