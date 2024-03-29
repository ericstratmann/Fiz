/* Copyright (c) 2008-2010 Stanford University
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

package org.fiz.section;

import org.fiz.*;
import org.fiz.test.*;

/**
 * Junit tests for the CompoundSection class.
 */

public class CompoundSectionTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
        TabSection.setTemplate(null);
    }

    public void test_constructor() {
        CompoundSection section = new CompoundSection(new Dataset(
                "a", "1", "b", "2"), new TemplateSection("abc"),
                new TemplateSection("xyz"));
        assertEquals("configuration properties", "a: 1\n" +
                "b: 2\n",
                section.properties.toString());
        assertEquals("count of child sections", 2, section.children.length);
    }

    public void test_add() {
        Section s1 = new TemplateSection("section 1");
        Section s2 = new TemplateSection("section 2");
        Section s3 = new TemplateSection("section 3");
        CompoundSection section = new CompoundSection(
                new Dataset(), s1);
        section.add(s2, s3);
        assertEquals("size of extraChildren", 2, section.extraChildren.size());
        assertEquals("first extra child", s2,
                section.extraChildren.get(0));
        assertEquals("second extra child", s3,
                section.extraChildren.get(1));
    }

    public void test_render_basics() {
        CompoundSection section = new CompoundSection(
                new Dataset("borderFamily", "a/b/c.gif"),
                new TemplateSection("<h1>First section</h1>\n"),
                new TemplateSection("<h1>Second section</h1>\n"));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection -->\n" +
                "<table cellspacing=\"0\">\n" +
                "  <tr style=\"line-height: 0px;\">\n" +
                "    <td><img src=\"a/b/c-nw.gif\" alt=\"\" /></td>\n" +
                "    <td style=\"background-image: url(a/b/c-n.gif); " +
                "background-repeat: repeat-x;\"></td>\n" +
                "    <td><img src=\"a/b/c-ne.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td style=\"background-image: url(a/b/c-w.gif); " +
                "background-repeat: repeat-y;\"></td>\n" +
                "    <td class=\"compoundBody\">\n" +
                "<h1>First section</h1>\n" +
                "<h1>Second section</h1>\n" +
                "    </td>\n" +
                "    <td style=\"background-image: url(a/b/c-e.gif); " +
                "background-repeat: repeat-y;\"></td>\n" +
                "  </tr>\n" +
                "  <tr style=\"line-height: 0px;\">\n" +
                "    <td><img src=\"a/b/c-sw.gif\" alt=\"\" /></td>\n" +
                "    <td style=\"background-image: url(a/b/c-s.gif); " +
                "background-repeat: repeat-x;\"></td>\n" +
                "    <td><img src=\"a/b/c-se.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End CompoundSection -->\n",
                html);
    }
    public void test_render_idProperty() {
        CompoundSection section = new CompoundSection(
                new Dataset("id", "test44", "borderFamily", "a/b/c.gif"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        TestUtil.assertSubstring("display id in start comment",
                "<!-- Start CompoundSection test44 -->", html);
        TestUtil.assertSubstring("generate id attribute for table",
                "<table id=\"test44\" cellspacing=\"0\">", html);
        TestUtil.assertSubstring("display id in end comment",
                "<!-- End CompoundSection test44 -->", html);
    }
    public void test_render_classProperty() {
        CompoundSection section = new CompoundSection(
                new Dataset("class", "class22", "borderFamily", "a/b/c.gif"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        TestUtil.assertSubstring("generate class attribute for table",
                "<table class=\"class22\" cellspacing=\"0\">", html);
    }
    public void test_render_backgroundProperty() {
        CompoundSection section = new CompoundSection(
                new Dataset("background", "#ff0000", "borderFamily", "a/b/c.gif"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        TestUtil.assertSubstring("use background property",
                "<td class=\"compoundBody\" style=\"background: #ff0000;\">",
                html);
    }
    public void test_render_layout() {
        Layout layout = new TableLayout(new Dataset("format",
                        "+-------+\n" +
                        "| first |\n" +
                        "+-------+"));
        CompoundSection section = new CompoundSection(
                  new Dataset("id", "test44", "layout", layout));
        layout.addData(new Dataset("first", "<h1>First section</h1>\n"));

        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        // todo should id be in here???
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection test44 -->\n" +
                "<div id=\"test44\">\n" +
                "<table cellspacing=\"0\" >\n" +
                "  <tr>\n" +
                "    <td colspan=\"1\" rowspan=\"1\">\n" +
                "<h1>First section</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "</div>\n" +
                "<!-- End CompoundSection test44 -->\n", html);
    }
    public void test_render_borderFamilyAndLayout() {
        Layout layout = new TableLayout(new Dataset("format",
                        "+-------+\n" +
                        "| first |\n" +
                        "+-------+"));
        layout.addData(new Dataset("first", "<h1>First section</h1>\n"));
        CompoundSection section = new CompoundSection(
                new Dataset("id", "test44",
                        "borderFamily", "a/b/c.gif",
                        "layout", layout));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection test44 -->\n" +
                "<table id=\"test44\" cellspacing=\"0\">\n" +
                "  <tr style=\"line-height: 0px;\">\n" +
                "    <td><img src=\"a/b/c-nw.gif\" alt=\"\" /></td>\n" +
                "    <td style=\"background-image: url(a/b/c-n.gif); " +
                "background-repeat: repeat-x;\"></td>\n" +
                "    <td><img src=\"a/b/c-ne.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td style=\"background-image: url(a/b/c-w.gif); " +
                "background-repeat: repeat-y;\"></td>\n" +
                "    <td class=\"compoundBody\">\n" +
                "<table cellspacing=\"0\" >\n" +
                "  <tr>\n" +
                "    <td colspan=\"1\" rowspan=\"1\">\n" +
                "<h1>First section</h1>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "    </td>\n" +
                "    <td style=\"background-image: url(a/b/c-e.gif); " +
                "background-repeat: repeat-y;\"></td>\n" +
                "  </tr>\n" +
                "  <tr style=\"line-height: 0px;\">\n" +
                "    <td><img src=\"a/b/c-sw.gif\" alt=\"\" /></td>\n" +
                "    <td style=\"background-image: url(a/b/c-s.gif); " +
                "background-repeat: repeat-x;\"></td>\n" +
                "    <td><img src=\"a/b/c-se.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End CompoundSection test44 -->\n", html);
    }
    public void test_render_divSimple() {
        CompoundSection section = new CompoundSection(
                new Dataset(),
                new TemplateSection("<h1>First section</h1>\n"));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection -->\n" +
                "<div>\n" +
                "<h1>First section</h1>\n" +
                "</div>\n" +
                "<!-- End CompoundSection -->\n",
                html);
    }
    public void test_render_divWithIdAndClassAndBackground() {
        CompoundSection section = new CompoundSection(
                new Dataset("id", "test44", "class", "class22",
                "background", "#ff0000"),
                new TemplateSection("<h1>First section</h1>\n"));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection test44 -->\n" +
                "<div id=\"test44\" class=\"class22\" " +
                "style=\"background: #ff0000;\">\n" +
                "<h1>First section</h1>\n" +
                "</div>\n" +
                "<!-- End CompoundSection test44 -->\n",
                html);
    }
    public void test_render_extraChildren() {
        CompoundSection section = new CompoundSection(
                new Dataset(),
                new TemplateSection("first\n"));
        section.add(new TemplateSection("second\n"),
                new TemplateSection("third\n"));
        section.render(cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "\n" +
                "<!-- Start CompoundSection -->\n" +
                "<div>\n" +
                "first\n" +
                "second\n" +
                "third\n" +
                "</div>\n" +
                "<!-- End CompoundSection -->\n",
                html);
    }

    public void test_renderChild() {
        CompoundSection section = new CompoundSection(
                new Dataset("id", "test44", "class", "class22",
                        "background", "#ff0000"),
                new TemplateSection(new Dataset("id", "first",
                        "template", "<h1>First section</h1>\n")));
        section.renderChild("first", cr);
        String html = cr.getHtml().getBody().toString();
        TestUtil.assertXHTML(html);
        assertEquals("generated HTML", "<h1>First section</h1>\n", html);
    }
}
