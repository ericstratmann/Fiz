package org.fiz;
import java.io.*;

/**
 * Junit tests for the Html class.
 */

public class HtmlTest extends junit.framework.TestCase {
    protected HtmlFixture html = new HtmlFixture();

    public void setUp() {
        html = new HtmlFixture();
        Css.init("test/testData");
        Config.init("test/testData");
    }
    // No tests for getBody; too trivial.

    public void test_getPrologue() {
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
                + "        \"http://www.w3.org/TR/xhtml1/DTD/"
                + "xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                + "xml:lang=\"en\" lang=\"en\">\n",
                (new Html("/fiz")).getPrologue());
    }

    public void test_getAndSetTitle() {
        assertEquals("initial value", null, html.getTitle());
        html.setTitle("new title");
        assertEquals("modified value", "new title", html.getTitle());
        html.setTitle(null);
        assertEquals("new null value", null, html.getTitle());
    }

    public void test_includeCssFile() {
        (new File("_test_")).mkdir();
        TestUtil.writeFile("_test_/first.css", "Sample css with newline\n");
        TestUtil.writeFile("_test_/second.css", "Css with no newline");
        TestUtil.writeFile("_test_/third.css", "Line #1\nLine #2\n");
        Css.init("_test_");
        html.includeCssFile("first.css");
        html.includeCssFile("first.css");
        html.includeCssFile("first.css");
        assertEquals("simple CSS", "Sample css with newline\n",
                html.css.toString());
        html.includeCssFile("second.css");
        html.includeCssFile("third.css");
        assertEquals("check blank lines", "Sample css with newline\n" +
                "\n" +
                "Css with no newline\n" +
                "\n" +
                "Line #1\n" +
                "Line #2\n",
                html.css.toString());
        Util.deleteTree("_test_");
    }

    public void test_includeCss() {
        html.includeCss("/* Sample comment */");
        html.includeCss("h1 {color: red}\n");
        html.includeCss("/* Another comment */");
        assertEquals("simple CSS", "/* Sample comment */\n" +
                "\n" +
                "h1 {color: red}\n" +
                "\n" +
                "/* Another comment */",
                html.css.toString());
    }

    public void test_print_emptyHtml() {
        assertEquals("nothing to print", "", html.toString());
        html.setTitle("sample");
        TestUtil.assertSubstring("title only", "<title>sample</title>",
                html.toString());
        html.reset();
        html.getBody().append("body info\n");
        TestUtil.assertSubstring("body only", "body info",
                html.toString());
    }
    public void test_print_noTitle() {
        html.getBody().append("first line\nsecond line\n");
        StringWriter out = new StringWriter();
        html.print(out);
        assertEquals("**Prologue**\n" +
                "<head>\n" +
                "<style type=\"text/css\">\n" +
                "/* Dummy version of main.css for tests */\n" +
                "body {color: #000000}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "first line\n" +
                "second line\n" +
                "</body>\n" +
                "</html>\n", out.toString());
    }
    public void test_print_withTitle() {
        html.getBody().append("first line\n");
        html.setTitle("sample title");
        StringWriter out = new StringWriter();
        html.print(out);
        assertEquals("**Prologue**\n" +
                "<head>\n" +
                "<title>sample title</title>\n" +
                "<style type=\"text/css\">\n" +
                "/* Dummy version of main.css for tests */\n" +
                "body {color: #000000}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "first line\n" +
                "</body>\n" +
                "</html>\n",
                out.toString());
    }
    public void test_print_css() {
        (new File("_test_")).mkdir();
        TestUtil.writeFile("_test_/first.css", "first.css\n");
        TestUtil.writeFile("_test_/main.css", "main.css\n");
        Css.init("_test_");
        html.setTitle("sample");
        html.includeCssFile("first.css");
        TestUtil.assertSubstring("header section",
                "<head>\n" +
                "<title>sample</title>\n" +
                "<style type=\"text/css\">\n" +
                "main.css\n" +
                "\n" +
                "first.css\n" +
                "</style>\n" +
                "</head>\n",
                html.toString());
        Util.deleteTree("_test_");
    }
    public void test_print_css_newlinesMissing() {
        (new File("_test_")).mkdir();
        TestUtil.writeFile("_test_/first.css", "first.css");
        TestUtil.writeFile("_test_/main.css", "main.css");
        Css.init("_test_");
        html.setTitle("sample");
        html.includeCssFile("first.css");
        TestUtil.assertSubstring("header section",
                "<head>\n" +
                "<title>sample</title>\n" +
                "<style type=\"text/css\">\n" +
                "main.css\n" +
                "\n" +
                "first.css\n" +
                "</style>\n" +
                "</head>\n",
                html.toString());
        Util.deleteTree("_test_");
    }
    public void test_print_css_noCssButMain() {
        (new File("_test_")).mkdir();
        TestUtil.writeFile("_test_/main.css", "main.css");
        Css.init("_test_");
        html.setTitle("sample");
        TestUtil.assertSubstring("header section",
                "<head>\n" +
                "<title>sample</title>\n" +
                "<style type=\"text/css\">\n" +
                "main.css\n" +
                "</style>\n" +
                "</head>\n",
                html.toString());
        Util.deleteTree("_test_");
    }

    public void test_toString() {
        html.getBody().append("first line\n");
        html.setTitle("sample title");
        assertEquals("**Prologue**\n" +
                "<head>\n" +
                "<title>sample title</title>\n" +
                "<style type=\"text/css\">\n" +
                "/* Dummy version of main.css for tests */\n" +
                "body {color: #000000}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "first line\n" +
                "</body>\n" +
                "</html>\n",
                html.toString());
    }

    public void test_reset() {
        html.setTitle("sample");
        html.getBody().append("body info\n");
        html.reset();
        assertEquals("", html.toString());
    }

    public void test_escapeHtmlChars() {
        StringBuilder out = new StringBuilder();
        Html.escapeHtmlChars("abc123", out);
        assertEquals("no special characters", "abc123", out.toString());
        out.setLength(0);
        Html.escapeHtmlChars("!\"#%&';<=>?", out);
        assertEquals("special characters", "!&quot;#%&amp;';&lt;=&gt;?",
                out.toString());
    }

    public void test_escapeUrlChars() {
        StringBuilder out = new StringBuilder();
        Html.escapeUrlChars(" +,-./@ABYZ[`abyz{", out);
        assertEquals("ASCII characters", "+%2b%2c-.%2f%40ABYZ%5b%60abyz%7b",
                out.toString());
        out.setLength(0);
        Html.escapeUrlChars("/0123456789:", out);
        assertEquals("digits","%2f0123456789%3a", out.toString());
        out.setLength(0);
        Html.escapeUrlChars("\u007f--\u0080--\u07ff--\u0800--\u1234", out);
        assertEquals("Unicode characters",
                "%7f--%c2%80--%df%bf--%e0%a0%80--%e1%88%b4",
                out.toString());
    }

    public void test_escapeStringChars() {
        StringBuilder out = new StringBuilder();
        Html.escapeStringChars("abc\037\040 | \n\t | \001 | \\\"", out);
        assertEquals("abc\\x1f  | \\n\\t | \\x01 | \\\\\\\"",
                out.toString());
    }
}

// The following class definition provides additional/modified features
// for testing.
class HtmlFixture extends Html {
    public HtmlFixture() {
        super("/fiz");
    }
    public String getPrologue() {
        // This method overrides getPrologue: a simpler prologue generates
        // less clutter in tests.
        return "**Prologue**\n";
    }
}
