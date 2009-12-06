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

/**
 * Junit tests for the DocTranslator class.
 */

public class DocTranslatorTest extends junit.framework.TestCase {
    // The following method collects all of the information about the
    // current line into a single string for easy testing.
    protected String lineInfo(DocTranslator translator) {
        return String.format("lineStart: %d, type: %s, term: \"%s\", " +
                "bulletIndent: %d, text: \"%s\", endLength: %d",
                translator.lineStart, translator.type, translator.term,
                translator.bulletIndent,
                ((translator.lineStart < translator.input.length())
                        ? translator.input.substring(translator.lineStart +
                        translator.textIndent, translator.lineEnd)
                        : ""),
                translator.nextLine - translator.lineEnd);
    }

    public void test_constructor() {
        DocTranslator translator = new DocTranslator("foo");
        assertEquals("input field", "foo", translator.input);
        assertEquals("nextLine", 0, translator.nextLine);
        assertEquals("standardTags", "@author ...@version ",
                translator.standardTags.get(0) + "..." +
                translator.standardTags.get(translator.standardTags.size()-1));
    }

    public void test_translate() {
        DocTranslator translator = new DocTranslator(
                " Some text\n" +
                "  * Level 1.1\n" +
                "  * Level 1.2\n" +
                " More text\n");
        assertEquals("output buffer", " Some text\n" +
                "  <ul><li>Level 1.1</li>\n" +
                "  <li>Level 1.2</li></ul>\n" +
                " More text\n",
                translator.translate());
    }

    public void test_translateLines_checkIndent() {
        DocTranslator translator = new DocTranslator(
                "    Line 1\n" +
                "   Line 2\n" +
                "  Line 3\n" +
                " Line 4\n");
        translator.translateLines(2);
        assertEquals("info about current line",
                "lineStart: 30, type: NORMAL, term: \"\", " +
                "bulletIndent: 1, text: \"Line 4\", endLength: 1",
                lineInfo(translator));
        assertEquals("output buffer", "    Line 1\n" +
                "   Line 2\n" +
                "  Line 3\n",
                translator.output.toString());
    }
    public void test_translateLines_pre() {
        DocTranslator translator = new DocTranslator(
                "<pre>\n" +
                "* not a bullet\n" +
                "term:    not really a definition\n" +
                "</pre>\n");
        translator.translateLines(0);
        assertEquals("info about current line",
                "lineStart: 61, type: EOF, term: \"\", " +
                "bulletIndent: -1, text: \"\", endLength: 0",
                lineInfo(translator));
        assertEquals("output buffer", "<pre>\n" +
                "* not a bullet\n" +
                "term:    not really a definition\n" +
                "</pre>\n",
                translator.output.toString());
    }
    public void test_translateLines_bullets() {
        DocTranslator translator = new DocTranslator(
                "  * Level 1.1\n" +
                "  * Level 1.2\n" +
                "    * Level 2.1\n" +
                "     * Level 3.1\n" +
                "  * Level 1.3\n");
        translator.translateLines(0);
        assertEquals("info about current line",
                "lineStart: 75, type: EOF, term: \"\", " +
                "bulletIndent: -1, text: \"\", endLength: 0",
                lineInfo(translator));
        assertEquals("output buffer", "  <ul><li>Level 1.1</li>\n" +
                "  <li>Level 1.2\n" +
                "    <ul><li>Level 2.1\n" +
                "     <ul><li>Level 3.1</li></ul></li></ul></li>\n" +
                "  <li>Level 1.3</li></ul>\n",
                translator.output.toString());
    }
    public void test_translateLines_definitions() {
        DocTranslator translator = new DocTranslator(
                "first:   a line\n" +
                "         another line\n" +
                "second:  another definition\n" +
                "blank:   \n");
        translator.translateLines(0);
        assertEquals("info about current line",
                "lineStart: 76, type: EOF, term: \"\", " +
                "bulletIndent: -1, text: \"\", endLength: 0",
                lineInfo(translator));
        assertEquals("output buffer",
                "<table cellspacing=\"0\"><tr><td class=\"fizterm\">first" +
                "</td><td class=\"fizdef\">a line\n" +
                "         another line</td></tr>\n" +
                "<tr><td class=\"fizterm\">second</td><td class=\"fizdef\">" +
                "another definition</td></tr>\n" +
                "<tr><td class=\"fizterm\">blank</td><td class=\"fizdef\">" +
                "</td></tr></table>\n",
                translator.output.toString());
    }
    public void test_translateLines_blankLine() {
        DocTranslator translator = new DocTranslator(
                "first line\n" +
                "   \n" +
                "second line\n");
        translator.translateLines(0);
        assertEquals("info about current line",
                "lineStart: 27, type: EOF, term: \"\", " +
                "bulletIndent: -1, text: \"\", endLength: 0",
                lineInfo(translator));
        assertEquals("output buffer",
                "first line\n" +
                "<p>\n" +
                "second line\n",
                translator.output.toString());
    }

    public void test_classify_sanityCheck() {
        DocTranslator translator = new DocTranslator(
                "xxx  first:   defined  here.\nnew line");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: DEFINITION, term: \"first\", " +
                "bulletIndent: 2, text: \"defined  here.\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_endOfString() {
        DocTranslator translator = new DocTranslator(
                "xxx");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: EOF, term: \"\", bulletIndent: -1, " +
                "text: \"\", endLength: 0",
                lineInfo(translator));
    }
    public void test_classify_lineEndsWithCr() {
        DocTranslator translator = new DocTranslator(
                "xxx  text\rnext line");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: NORMAL, term: \"\", bulletIndent: 1, " +
                "text: \"text\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_lineEndsWithLf() {
        DocTranslator translator = new DocTranslator(
                "xxx  text\nnext line");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: NORMAL, term: \"\", bulletIndent: 1, " +
                "text: \"text\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_lineEndsWithCrLf() {
        DocTranslator translator = new DocTranslator(
                "xxx  text\r\nnext line");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: NORMAL, term: \"\", bulletIndent: 1, " +
                "text: \"text\", endLength: 2",
                lineInfo(translator));
    }
    public void test_classify_lineEndsAtStringEnd() {
        DocTranslator translator = new DocTranslator(
                "xxx  text");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: NORMAL, term: \"\", bulletIndent: 1, " +
                "text: \"text\", endLength: 0",
                lineInfo(translator));
    }
    public void test_classify_crAtEndOfString() {
        DocTranslator translator = new DocTranslator(
                "xxx  text\r");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: NORMAL, term: \"\", bulletIndent: 1, " +
                "text: \"text\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_onlySpace() {
        DocTranslator translator = new DocTranslator(
                "xxx    \n");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: BLANK, term: \"\", bulletIndent: 0, " +
                "text: \"\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_pre() {
        DocTranslator translator = new DocTranslator(
                "xxx    <pre> \n");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: PRE, term: \"\", bulletIndent: 3, " +
                "text: \"<pre> \", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_endPre() {
        DocTranslator translator = new DocTranslator(
                "xxx    </pre> \n");
        translator.classify(4);
        assertEquals("info about current line",
                "lineStart: 4, type: END_PRE, term: \"\", bulletIndent: 3, " +
                "text: \"</pre> \", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_hyphenBullet() {
        DocTranslator translator = new DocTranslator(
                "xxx   - first point\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: BULLET, term: \"\", bulletIndent: 3, " +
                "text: \"first point\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_starBullet() {
        DocTranslator translator = new DocTranslator(
                "xxx *   first point\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: BULLET, term: \"\", bulletIndent: 1, " +
                "text: \"first point\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_noSpaceAfterBullet() {
        DocTranslator translator = new DocTranslator(
                "xxx   *first point\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: NORMAL, term: \"\", bulletIndent: 3, " +
                "text: \"*first point\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_bulletAtEndOfLine() {
        DocTranslator translator = new DocTranslator(
                "xxx   *");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: NORMAL, term: \"\", bulletIndent: 3, " +
                "text: \"*\", endLength: 0",
                lineInfo(translator));
    }
    public void test_classify_nonDefinitionBecauseOfPeriod() {
        DocTranslator translator = new DocTranslator(
                "xxx  foo.  who knows?\n");
        translator.classify(3);
        assertEquals("line type", "NORMAL", translator.type.toString());
    }
    public void test_classify_nonDefinitionBecauseOfSemicolon() {
        DocTranslator translator = new DocTranslator(
                "xxx  foo;  who knows?\n");
        translator.classify(3);
        assertEquals("line type", "NORMAL", translator.type.toString());
    }
    public void test_classify_checkJavadocTags() {
        DocTranslator translator = new DocTranslator(
                "xxx  @tag   who knows?\n");
        translator.classify(3);
        assertEquals("line type", "DEFINITION", translator.type.toString());
        translator = new DocTranslator(
                "xxx  @param foo   Sample argument\n");
        translator.classify(3);
        assertEquals("line type", "NORMAL", translator.type.toString());
        translator = new DocTranslator(
                "xxx  @return    Always returns null.\n");
        translator.classify(3);
        assertEquals("line type", "NORMAL", translator.type.toString());
    }
    public void test_classify_definitionWithColon() {
        DocTranslator translator = new DocTranslator(
                "xxx  foo:  who knows?\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: DEFINITION, term: \"foo\", " +
                "bulletIndent: 2, text: \"who knows?\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_definitionWithHyphen() {
        DocTranslator translator = new DocTranslator(
                "xxx  foo -  who knows?\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: DEFINITION, term: \"foo\", " +
                "bulletIndent: 2, text: \"who knows?\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_definitionWithHyphenButNoSpace() {
        DocTranslator translator = new DocTranslator(
                "xxx  foo-  who knows?\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: DEFINITION, term: \"foo-\", " +
                "bulletIndent: 2, text: \"who knows?\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_definitionWithNoHyphenOrColon() {
        DocTranslator translator = new DocTranslator(
                "xxx  foo.  more    who knows?\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: DEFINITION, term: \"foo.  more\", " +
                "bulletIndent: 2, text: \"who knows?\", endLength: 1",
                lineInfo(translator));
    }
    public void test_classify_normalLine() {
        DocTranslator translator = new DocTranslator(
                "xxx  sample: line.  Next\n");
        translator.classify(3);
        assertEquals("info about current line",
                "lineStart: 3, type: NORMAL, term: \"\", bulletIndent: 2, " +
                "text: \"sample: line.  Next\", endLength: 1",
                lineInfo(translator));
    }

    public void test_translateBullets_basics() {
        DocTranslator translator = new DocTranslator(
                "  * First point\n" +
                "    continuation\n" +
                "  * Second point\n" +
                "  * Third point");
        translator.classify(0);
        translator.translateBullets();
        assertEquals("output buffer", "  <ul><li>First point\n" +
                "    continuation</li>\n" +
                "  <li>Second point</li>\n" +
                "  <li>Third point</li></ul>",
                translator.output.toString());
    }
    public void test_translateBullets_stopOnNonBullet() {
        DocTranslator translator = new DocTranslator(
                "  * First point\n" +
                "  stop here\n");
        translator.classify(0);
        translator.translateBullets();
        assertEquals("output buffer", "  <ul><li>First point</li></ul>\n",
                translator.output.toString());
    }
    public void test_translateBullets_stopBecauseOfIndentation() {
        DocTranslator translator = new DocTranslator(
                "  * First point\n" +
                " * Not a point\n");
        translator.classify(0);
        translator.translateBullets();
        assertEquals("output buffer", "  <ul><li>First point</li></ul>\n",
                translator.output.toString());
    }

    public void test_translateDefinitions_basic() {
        DocTranslator translator = new DocTranslator(
                " foo   first term\n" +
                "  continuation\n" +
                " foo2  second term\n" +
                " foo3  third term\n" +
                "not in list\n");
        translator.classify(0);
        translator.translateDefinitions();
        assertEquals("output buffer",
                " <table cellspacing=\"0\"><tr><td class=\"fizterm\">foo" +
                "</td><td class=\"fizdef\">first term\n" +
                "  continuation</td></tr>\n" +
                " <tr><td class=\"fizterm\">foo2</td><td class=\"fizdef\">" +
                "second term</td></tr>\n" +
                " <tr><td class=\"fizterm\">foo3</td><td class=\"fizdef\">" +
                "third term</td></tr></table>\n",
                translator.output.toString());
    }
    public void test_translateDefinitions_stopOnNonDefinition() {
        DocTranslator translator = new DocTranslator(
                " foo   first term\n" +
                " stop here\n");
        translator.classify(0);
        translator.translateDefinitions();
        assertEquals("output buffer", " <table cellspacing=\"0\"><tr>" +
                "<td class=\"fizterm\">foo</td><td class=\"fizdef\">" +
                "first term</td></tr></table>\n",
                translator.output.toString());
    }
    public void test_translateDefinitions_stopBecauseOfIndentation() {
        DocTranslator translator = new DocTranslator(
                " foo   first term\n" +
                "foo2   not at same level\n");
        translator.classify(0);
        translator.translateDefinitions();
        assertEquals("output buffer", " <table cellspacing=\"0\"><tr>" +
                "<td class=\"fizterm\">foo</td><td class=\"fizdef\">" +
                "first term</td></tr></table>\n",
                translator.output.toString());
    }

    public void test_handledPre_basics() {
        DocTranslator translator = new DocTranslator(
                "  <pre> \n" +
                "    line 1<b>&amp;</b>\n" +
                " line 2\n" +
                "    </pre> \n" +
                "    stop here\n");
        translator.classify(0);
        translator.handlePre();
        assertEquals("output buffer", "<pre> \n" +
                "  line 1&lt;b&gt;&amp;amp;&lt;/b&gt;\n" +
                "line 2\n" +
                "</pre> \n",
                translator.output.toString());
        assertEquals("info about current line",
                "lineStart: 52, type: NORMAL, term: \"\", " +
                "bulletIndent: 4, text: \"stop here\", endLength: 1",
                lineInfo(translator));
    }
    public void test_handledPre_noEndPre() {
        DocTranslator translator = new DocTranslator(
                "  <pre> \n" +
                "line\n");
        translator.classify(0);
        translator.handlePre();
        assertEquals("output buffer", "<pre> \n" +
                "line\n",
                translator.output.toString());
        assertEquals("info about current line",
                "lineStart: 14, type: EOF, term: \"\", " +
                "bulletIndent: -1, text: \"\", endLength: 0",
                lineInfo(translator));
    }

    public void test_appendToLastLine_emptyString() {
        DocTranslator translator = new DocTranslator("xyz");
        translator.appendToLastLine("|");
        assertEquals("output buffer", "|",
                translator.output.toString());
    }
    public void test_appendToLastLine_noNewline() {
        DocTranslator translator = new DocTranslator("xyz");
        translator.output.append("a\nb\nc");
        translator.appendToLastLine("|");
        assertEquals("output buffer", "a\nb\nc|",
                translator.output.toString());
    }
    public void test_appendToLastLine_newlineOnly() {
        DocTranslator translator = new DocTranslator("xyz");
        translator.output.append("a\nb\n");
        translator.appendToLastLine("|");
        assertEquals("output buffer", "a\nb|\n",
                translator.output.toString());
    }
    public void test_appendToLastLine_returnOnly() {
        DocTranslator translator = new DocTranslator("xyz");
        translator.output.append("a\nb\r");
        translator.appendToLastLine("|");
        assertEquals("output buffer", "a\nb|\r",
                translator.output.toString());
    }
    public void test_appendToLastLine_crlf() {
        DocTranslator translator = new DocTranslator("xyz");
        translator.output.append("a\nb\r\n");
        translator.appendToLastLine("|");
        assertEquals("output buffer", "a\nb|\r\n",
                translator.output.toString());
    }
    public void test_appendToLastLine_newlineAtStartOfBuffer() {
        DocTranslator translator = new DocTranslator("xyz");
        translator.output.append("\n");
        translator.appendToLastLine("|");
        assertEquals("output buffer", "|\n",
                translator.output.toString());
    }
}
