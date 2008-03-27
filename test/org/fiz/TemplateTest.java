package org.fiz;

/**
 * Junit tests for the Template class.
 */

public class TemplateTest extends junit.framework.TestCase {
    public void test_expand_basics() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.expand("name: @name, age: @age, "
                + "{{weight: @weight}} misc: @{@}",
                data, out);
        assertEquals("output string", "123name: Alice, age: 28, misc: {}",
                out.toString());
    }
    public void test_expand_openBracesAtEnd() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.expand("abc{", data, out);
        assertEquals("output string", "123abc{", out.toString());
        boolean gotException = false;
        try {
            Template.expand("abc{{", data, out);
        }
        catch (Template.SyntaxError e) {
            assertEquals("exception message",
                    "unmatched \"{{\" in template \"abc{{\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_expand_singleBrace() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.expand("{}}", data, out);
        assertEquals("output string", "123{}}", out.toString());
    }
    public void test_expand_specialChars() {
        Dataset data = new Dataset("name", "<Alice>", "age", "28");
        StringBuilder out = new StringBuilder();
        Template.expand("<name>: @name", data, out);
        assertEquals("output string", "<name>: &lt;Alice&gt;", out.toString());
        out.setLength(0);
        Template.expand("<name>: @name", data, out, Template.SpecialChars.URL);
        assertEquals("output string", "<name>: %3cAlice%3e", out.toString());
    }

    public void test_expandAtSign_simpleName() {
        Dataset data = new Dataset("name", "Alice");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandAtSign("xx@name + foo", data, out, false, 3);
        assertEquals("end of specifier", 7, TemplateFixture.end);
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
                Template.SpecialChars.HTML);
        assertEquals("missingData", false, TemplateFixture.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void test_appendValue_conditionalFailed() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("first_name", data, out, true,
                Template.SpecialChars.HTML);
        assertEquals("missingData", true, TemplateFixture.missingData);
        assertEquals("output string", "123", out.toString());
    }
    public void test_appendValue_unconditionalSucceeded() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.HTML);
        assertEquals("missingData", false, TemplateFixture.missingData);
        assertEquals("output string", "123West", out.toString());
    }
    public void test_appendValue_unconditionalFailed() {
        boolean gotException = false;
        try {
            Dataset data = new Dataset();
            StringBuilder out = new StringBuilder("123");
            TemplateFixture.appendValue("last_name", data, out, false,
                    Template.SpecialChars.HTML);
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"last_name\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_appendValue_HtmlEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.HTML);
        assertEquals("output string", "123&lt;West&gt;", out.toString());
    }
    public void test_appendValue_UrlEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.URL);
        assertEquals("output string", "123%3cWest%3e", out.toString());
    }
    public void test_appendValue_NoEncoding() {
        Dataset data = new Dataset("last_name", "<West>");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.appendValue("last_name", data, out, false,
                Template.SpecialChars.NONE);
        assertEquals("output string", "123<West>", out.toString());
    }

    public void test_expandBraces_basics() {
        Dataset data = new Dataset("last_name", "West");
        StringBuilder out = new StringBuilder("123");
        TemplateFixture.expandBraces("{{abc@last_name@}}x}}y", data, out, 2);
        assertEquals("end of specifier", 21, TemplateFixture.end);
        assertEquals("conditional", false, TemplateFixture.conditional);
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
        assertEquals("conditional", false, TemplateFixture.conditional);
        assertEquals("output string", "123", out.toString());
    }
    public void test_expandBraces_collapsePrecedingSpace() {
        Dataset data = new Dataset();
        StringBuilder out = new StringBuilder();
        Template.expand("x {{@foo}}", data, out);
        assertEquals("braces at end of string", "x", out.toString());
        out.setLength(0);
        Template.expand("- {{@foo}} -", data, out);
        assertEquals("spaces on both sides", "- -", out.toString());
        out.setLength(0);
        Template.expand("x {{@x}}] x {{@x}}> x {{@x}}} x {{@x}}) x "
                + "{{@x}}\" x {{@x}}\'",
                data, out);
        assertEquals("close-delimiter follows braces", "x] x> x} x) x\" x'",
                out.toString());
        out.setLength(0);
        out.append("x ");
        TemplateFixture.expandBraces("x {{@x}}]", data, out, 4, -1);
        assertEquals("update lastDeletedSpace", 1,
                TemplateFixture.lastDeletedSpace);
        out.setLength(0);
        Template.expand("<{{@x}} {{@x}}>", data, out);
        assertEquals("don't delete the same space twice", "<>",
                out.toString());
        out.setLength(0);
        Template.expand("{{@x}}) + b{{@x}}> + {{@x}}y", data, out);
        assertEquals("don't remove spaces", ") + b> + y",
                out.toString());
    }
    public void test_expandBraces_collapseTrailingSpace() {
        Dataset data = new Dataset();
        StringBuilder out = new StringBuilder();
        Template.expand("{{@x}} bcd", data, out);
        assertEquals("braces at start of string", "bcd", out.toString());
        out.setLength(0);
        Template.expand("[{{@x}} + <{{@x}} + @{{{@x}} + ({{@x}} + "
                + "\"{{@x}} + \'{{@x}} +",
                data, out);
        assertEquals("open-delimiter precedes braces", "[+ <+ {+ (+ \"+ '+",
                out.toString());
        out.setLength(0);
        TemplateFixture.expandBraces("<{{@x}} abc]", data, out, 3, -1);
        assertEquals("update lastDeletedSpace", 7,
                TemplateFixture.lastDeletedSpace);
        out.setLength(0);
        Template.expand("<{{@x}}abc + y{{@x}}y + <{{@x}}", data, out);
        assertEquals("don't remove spaces", "<abc + yy + <",
                out.toString());
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
}

// The following class definition provides a mechanism for accessing
// protected/private fields and methods.
class TemplateFixture {
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
            Template.SpecialChars encoding) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.data = data;
        info.out = out;
        info.quoting = encoding;
        info.conditional = conditional;
        info.missingData = false;
        info.end = -1;
        Template.expandParenName(info, start);
        missingData = info.missingData;
        end = info.end;
    }

    public static void appendValue(String name, Dataset data,
            StringBuilder out, boolean conditional,
            Template.SpecialChars encoding) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.data = data;
        info.out = out;
        info.quoting = encoding;
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
        TemplateFixture.lastDeletedSpace = info.lastDeletedSpace;
    }
    public static void expandBraces(String template, Dataset data,
            StringBuilder out, int start) {
        expandBraces(template, data, out, start, start-1);
    }
}
