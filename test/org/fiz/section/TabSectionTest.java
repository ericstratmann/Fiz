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

package org.fiz.section;

import org.fiz.*;
import org.fiz.test.*;

/**
 * Junit tests for the TabSection class.
 */

public class TabSectionTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
        TabSection.setTemplate(null);
    }

    public void tearDown() {
        Util.deleteTree("_tabtest_");
    }

    public void test_constructor() {
        TabSection section = new TabSection(new Dataset("id", "xyzzy",
                "a", "1", "b", "2"),
                new Dataset("id", "1"), new Dataset("id", "2"));
        assertEquals("configuration properties", "a:  1\n" +
                "b:  2\n" +
                "id: xyzzy\n",
                section.properties.toString());
        assertEquals("tab descriptions", "id: 1\n" +
                "--------\n" +
                "id: 2\n",
                StringUtil.join(section.tabs, "--------\n"));
    }
    public void test_constructor_defaultId() {
        TabSection section = new TabSection(new Dataset("a", "1"));
        assertEquals("configuration properties", "a:  1\n" +
                "id: tabs\n",
                section.properties.toString());
    }

    public void test_clearCache() {
        TabSection.setTemplate("xyzzy");
        assertEquals("template before clearCache", "xyzzy",
                TabSection.cssTemplate);
        TabSection.clearCache();
        assertEquals("template after clearCache", null,
                TabSection.cssTemplate);
    }

    public void test_render_basics() {
        TabSection section = new TabSection(new Dataset("id", "section12",
                "selector", "selectedTab"),
                new Dataset("id", "first", "text", "First", "url", "/a/b"),
                new Dataset("id", "second", "text", "Second", "url", "/a/c"),
                new Dataset("id", "third", "text", "Third", "url", "/xyz"));
        cr.getMainDataset().set("selectedTab", "second");
        section.render(cr);
        TestUtil.assertXHTML(cr.getHtml().getBody().toString());
        assertEquals("generated HTML", "\n" +
                "<!-- Start TabSection section12 -->\n" +
                "<table id=\"section12\" class=\"TabSection\" cellspacing=\"0\">\n" +
                "  <tr>\n" +
                "    <td class=\"spacer\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"left\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"mid\" id=\"section12_first\">" +
                "<a href=\"/a/b\"><div>First</div></a></td>\n" +
                "    <td class=\"right\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"spacer\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"leftSelected\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"midSelected\" id=\"section12_second\">" +
                "<a href=\"/a/c\"><div>Second</div></a></td>\n" +
                "    <td class=\"rightSelected\"><img src=\"/static/fiz" +
                "/images/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"spacer\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"left\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"mid\" id=\"section12_third\">" +
                "<a href=\"/xyz\"><div>Third</div></a></td>\n" +
                "    <td class=\"right\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "    <td class=\"rightSpacer\"><img src=\"/static/fiz/images" +
                "/blank.gif\" alt=\"\" /></td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TabSection section12 -->\n",
                cr.getHtml().getBody().toString());
    }
    public void test_render_defaultSelector() {
        TabSection section = new TabSection(new Dataset("id", "section12"),
                new Dataset("id", "first", "text", "label1", "url", "/a/b"),
                new Dataset("id", "second", "text", "Second", "url", "/a/c"));
        cr.getMainDataset().set("currentTabId", "second");
        cr.showSections(section);
        TestUtil.assertXHTML(cr.getHtml().getBody().toString());
        TestUtil.assertSubstring("second tab selected",
                "<td class=\"midSelected\" id=\"section12_second\">" +
                "<a href=\"/a/c\"><div>Second",
                cr.getHtml().getBody().toString());
    }
    public void test_render_noData() {
        TabSection section = new TabSection(new Dataset("id", "section12"),
                new Dataset("id", "first", "text", "Name: @name, age: @age",
                "url", "/a/b"));
        cr.showSections(section);
        TestUtil.assertXHTML(cr.getHtml().getBody().toString());
        TestUtil.assertSubstring("tab text", "<div>Name: Alice, age: 36</div>",
                cr.getHtml().getBody().toString());
    }
    public void test_render_useData() {
        TabSection section = new TabSection(new Dataset("id", "section12",
                "data", new Dataset("name", "Alice", "age", "44")),
                new Dataset("id", "first", "text", "Name: @name, age: @age",
                "url", "/a/b"));
        cr.showSections(section);
        TestUtil.assertXHTML(cr.getHtml().getBody().toString());
        TestUtil.assertSubstring("tab text", "<div>Name: Alice, age: 44</div>",
                cr.getHtml().getBody().toString());
    }
    public void test_render_defaultStyle() {
        Config.setDataset("tabSections", YamlDataset.newStringInstance(
                "testStyle:\n" +
                "  name: testStyle\n" +
                "tabGray:\n" +
                "  name: tabGray\n"));
        Config.setDataset("css", new Dataset("name", "css config",
                "border", "1px solid red"));
        TabSection.setTemplate("name: <@name>, border: @border");
        TabSection section = new TabSection(new Dataset("id", "section12"),
                new Dataset("id", "first", "text", "Name: @name, age: @age",
                "url", "/a/b"));
        section.render(cr);
        assertEquals("generated CSS", "name: <tabGray>, border: 1px solid red",
                cr.getHtml().getCss());
    }
    public void test_render_explicitStyle() {
        Config.setDataset("tabSections", YamlDataset.newStringInstance(
                "testStyle:\n" +
                "  sidePadding: 2px\n" +
                "  border: 1px solid red\n"));
        TabSection.setTemplate("sidePadding: @sidePadding, border: @border");
        TabSection section = new TabSection(new Dataset("id", "section12",
                "style", "testStyle"),
                new Dataset("id", "first", "text", "Name: @name, age: @age",
                "url", "/a/b"));
        section.render(cr);
        assertEquals("generated CSS",
                "sidePadding: 2px, border: 1px solid red",
                cr.getHtml().getCss());
    }
    public void test_render_explicitClass() {
        TabSection section = new TabSection(new Dataset("id", "section12",
                "class", "testClass"),
                new Dataset("id", "first", "text", "Name: @name, age: @age",
                "url", "/a/b"));
        section.render(cr);
        TestUtil.assertSubstring("class attribute",
                "<table id=\"section12\" class=\"testClass\"",
                cr.getHtml().getBody().toString());
    }
    public void test_render_noSelectedTab() {
        TabSection section = new TabSection(new Dataset("id", "section12"),
                new Dataset("id", "first", "text", "label1",
                "url", "/a/b"));
        cr.showSections(section);
        TestUtil.assertXHTML(cr.getHtml().getBody().toString());
        TestUtil.assertSubstring("first tab not selected",
                "<td class=\"mid\" id=\"section12_first\">" +
                "<a href=\"/a/b\"><div>label1",
                cr.getHtml().getBody().toString());
    }
    public void test_render_templateExpansionForUrl() {
        TabSection section = new TabSection(new Dataset("id", "section12"),
                new Dataset("id", "first", "text", "First",
                "url", "/a/@name"));
        section.render(cr);
        TestUtil.assertSubstring("generated URL", "<a href=\"/a/Alice\">",
                cr.getHtml().getBody().toString());
    }
    public void test_render_javascriptAction() {
        TabSection section = new TabSection(new Dataset("id", "section12"),
                new Dataset("id", "first", "text", "First",
                "javascript", "window.xyz=\"@text\""));
        cr.getMainDataset().set("text", "<\">");
        section.render(cr);
        assertEquals("included Javascript files",
                "static/fiz/Fiz.js, static/fiz/TabSection.js",
                cr.getHtml().getJsFiles());
        TestUtil.assertSubstring("onclick handler",
                "<a href=\"#\" onclick=\"Fiz.TabSection.selectTab" +
                "(&quot;section12_first&quot;); window.xyz=&quot;&lt;" +
                "\\&quot;&gt;&quot;; return false;\">",
                cr.getHtml().getBody().toString());
    }
    public void test_render_ajaxAction() {
        TabSection section = new TabSection(new Dataset("id", "section12"),
                new Dataset("id", "first", "text", "First",
                "ajaxUrl", "a/b/@name"));
        section.render(cr);
        TestUtil.assertSubstring("onclick handler",
                "<a href=\"#\" onclick=\"Fiz.TabSection.selectTab" +
                "(&quot;section12_first&quot;); " +
                "void new Fiz.Ajax({url: &quot;a/b/Alice&quot;});;",
                cr.getHtml().getBody().toString());
    }

    public void test_getTemplate_usedCachedCopy() {
        TabSection.setTemplate("cached TabSection template");
        assertEquals("getTemplate result", "cached TabSection template",
                TabSection.getTemplate());
    }

    // No tests for setTemplate: it is already fully exercised by
    // other tests.
}
