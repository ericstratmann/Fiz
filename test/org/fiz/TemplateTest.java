package org.fiz;

/**
 * Junit tests for the Template class.
 */

public class TemplateTest extends junit.framework.TestCase {
    public void test_expand() {
        Dataset data = new Dataset("name", "Alice", "age", "28");
        StringBuilder out = new StringBuilder("123");
        Template.expand("name: @name, age: @age, "
                + "{{weight: @weight}} misc: @{@}",
                data, out);
        assertEquals("output string", "123name: Alice, age: 28, misc: {}",
                out.toString());
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
        Template.expand("name: @name?{unknown}, xyz", data, out);
        assertEquals("output string", "123name: unknown, xyz", out.toString());
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
        assertEquals("ignoreErrors", false, TemplateFixture.conditional);
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
        assertEquals("ignoreErrors", false, TemplateFixture.conditional);
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

    public void test_variousComplexTemplates() {
        StringBuilder out = new StringBuilder();
        Template.expand("first {{name: @name, age: @age?{unknown}}}",
                new Dataset("name", "Bob"), out);
        assertEquals("output string", "first name: Bob, age: unknown",
                out.toString());

        out.setLength (0);
        Template.expand("first {{name: @name, age: @age?{unknown}, " +
                "weight: @weight}}", new Dataset("name", "Bob"), out);
        assertEquals("output string", "first", out.toString());
    }
}

// The following class definition provides a mechanism for accessing
// protected/private fields and methods.
class TemplateFixture {
    public static int templateEnd;
    public static boolean missingData;
    public static boolean conditional;
    public static boolean skip;
    public static int end;
    public static int lastDeletedSpace;

    public static void expandRange(String template, Dataset data,
            StringBuilder out, int start, int end) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.templateEnd = -15;
        info.data = data;
        info.out = out;
        info.ignoreErrors = false;
        info.missingData = false;
        info.skip = false;
        info.end = -1;
        Template.expandRange(info, start, end);
        templateEnd = info.templateEnd;
        TemplateFixture.end = info.end;
    }

    public static void expandAtSign(String template, Dataset data,
            StringBuilder out, boolean conditional, int start) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.templateEnd = template.length();
        info.data = data;
        info.out = out;
        info.ignoreErrors = conditional;
        info.missingData = false;
        info.skip = false;
        info.end = -1;
        Template.expandAtSign(info, start);
        missingData = info.missingData;
        end = info.end;
    }

    public static void expandChoice(String template, Dataset data,
            StringBuilder out, String name, int start) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.templateEnd = template.length();
        info.data = data;
        info.out = out;
        info.quoting = Template.SpecialChars.HTML;
        info.ignoreErrors = false;
        info.missingData = false;
        info.skip = false;
        info.end = -1;
        Template.expandChoice(info, name, start);
        end = info.end;
    }

    public static void expandParenName(String template, Dataset data,
            StringBuilder out, boolean conditional, int start,
            Template.SpecialChars encoding) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.templateEnd = template.length();
        info.data = data;
        info.out = out;
        info.quoting = encoding;
        info.ignoreErrors = conditional;
        info.missingData = false;
        info.skip = false;
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
        info.ignoreErrors = conditional;
        info.missingData = false;
        info.skip = false;
        info.end = -1;
        Template.appendValue(info, name);
        missingData = info.missingData;
        end = info.end;
    }

    public static void expandBraces(String template, Dataset data,
            StringBuilder out, int start, int lastDeletedSpace) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.templateEnd = template.length();
        info.data = data;
        info.out = out;
        info.ignoreErrors = false;
        info.missingData = true;
        info.skip = false;
        info.end = -1;
        info.lastDeletedSpace = lastDeletedSpace;
        Template.expandBraces(info, start);
        missingData = info.missingData;
        conditional = info.ignoreErrors;
        end = info.end;
        TemplateFixture.lastDeletedSpace = info.lastDeletedSpace;
    }

    public static int skipTo(String template, StringBuilder out,
            int start, char c1, char c2) {
        Template.ParseInfo info = new Template.ParseInfo();
        info.template = template;
        info.templateEnd = template.length();
        info.data = new Dataset();
        info.out = out;
        info.ignoreErrors = false;
        info.missingData = false;
        info.skip = false;
        info.end = -1;
        info.lastDeletedSpace = lastDeletedSpace;
        int result = Template.skipTo(info, start, c1, c2);
        missingData = info.missingData;
        conditional = info.ignoreErrors;
        skip = info.skip;
        end = info.end;
        TemplateFixture.lastDeletedSpace = info.lastDeletedSpace;
        return result;
    }
    public static void expandBraces(String template, Dataset data,
            StringBuilder out, int start) {
        expandBraces(template, data, out, start, start-1);
    }
}
