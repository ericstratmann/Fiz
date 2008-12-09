package org.fiz;

import java.io.*;
import java.util.*;

/**
 * Junit tests for the TreeSection class.
 */
public class TreeSectionTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
        Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                "treeInfo:\n" +
                "  manager: raw\n" +
                "  result:\n" +
                "     record:\n" +
                "       - name: child1\n" +
                "         text: Child #1\n"));
        Config.setDataset("styles", YamlDataset.newStringInstance(
                "TreeSection:\n" +
                "  leaf: \"leaf: @name\"\n" +
                "  node: \"node: @name\"\n" +
                "  node-expanded: \"node-expanded: @name\"\n" +
                "  leaf2: \"leaf2: @name\"\n" +
                "  node2: \"node2: @name\"\n" +
                "  node2-expanded: \"node-expanded: @name\"\n"));
        Reminder.testMode = true;
    }

    public void tearDown() {
        Reminder.testMode = false;
    }

    public void test_constructor_noRequestProperty() {
        TreeSection tree = new TreeSection(new Dataset("id", "1234",
                "request", "getInfo"));
        assertEquals("properties dataset", "id:      1234\n" +
                "request: getInfo\n",
                tree.properties.toString());
    }

    public void test_ajaxExpand() {
        cr.setReminder("TreeSection.row", new Dataset(
                "name", "node16", "id", "tree1_2"));
        cr.setReminder("TreeSection", new Dataset("id", "tree1",
                "request", "treeInfo"));
        TreeSection.ajaxExpand(cr);
        StringWriter out = ((ServletResponseFixture)
                cr.getServletResponse()).out;
        assertEquals("Ajax javascript action",
                "var actions = [{type: \"eval\", " +
                "javascript: \"Fiz.ids[\\\"tree1_2\\\"].expand(\\\"<table " +
                "cellspacing=\\\\\\\"0\\\\\\\" class=\\\\\\\"TreeSection" +
                "\\\\\\\" id=\\\\\\\"tree1_2\\\\\\\">\\\\n  <tr " +
                "id=\\\\\\\"tree1_2_0\\\\\\\">\\\\n    <td class=\\\\\\\"" +
                "left\\\\\\\" style=\\\\\\\"background-image: " +
                "url(/fizlib/images/treeSolid-line.gif); background-repeat: " +
                "no-repeat;\\\\\\\"><img src=\\\\\\\"/fizlib/images/" +
                "treeSolid-leaf.gif\\\\\\\"></td>\\\\n    <td " +
                "class=\\\\\\\"right\\\\\\\">leaf: child1" +
                "</td>\\\\n  </tr>\\\\n" +
                "</table>\\\\n\\\");\"}",
                out.toString());
    }

    public void test_html_basics() {
        TreeSection tree = new TreeSection(new Dataset("id", "tree1",
                "request", "treeInfo"));
        tree.registerRequests(cr);
        tree.html(cr);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TreeSection tree1 -->\n" +
                "<table cellspacing=\"0\" class=\"TreeSection\" " +
                "id=\"tree1\">\n" +
                "  <tr id=\"tree1_0\">\n" +
                "    <td class=\"left\" style=\"background-image: " +
                "url(/fizlib/images/treeSolid-line.gif); background-repeat: " +
                "no-repeat;\"><img src=\"/fizlib/images/treeSolid-leaf." +
                "gif\"></td>\n" +
                "    <td class=\"right\">leaf: child1</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TreeSection @id -->\n",
                cr.getHtml().getBody().toString());
        assertEquals("accumulated Javascript",
                "Fiz.Reminder.reminders[\"tree1\"] = " +
                "\"24.JHB9AM69@,7:GY68T5G<EIB*47.11.TreeSection(2.id5." +
                "tree1\\n7.request8.treeInfo)\";\n",
                 cr.getHtml().jsCode.toString());
        TestUtil.assertSubstring("CSS file names", "TreeSection.css",
                cr.getHtml().getCssFiles());
        assertEquals("Javascript file names",
                "fizlib/Fiz.js, fizlib/Reminder.js, fizlib/TreeRow.js",
                cr.getHtml().getJsFiles());
    }
    public void test_html_propertiesForReminder() {
        TreeSection tree = new TreeSection(new Dataset("class", "foo",
                "edgeFamily", "edge16", "id", "tree1",
                "leafStyle", "TreeSection.node",
                "nodeStyle", "TreeSection.leaf", "request", "treeInfo"));
        tree.registerRequests(cr);
        tree.html(cr);
        assertEquals("accumulated Javascript",
                "Fiz.Reminder.reminders[\"tree1\"] = \"24.JHB9AM69@," +
                "7:GY68T5G<EIB*144.11.TreeSection(5.class3.foo\\n" +
                "10.edgeFamily6.edge16\\n2.id5.tree1\\n" +
                "9.leafStyle16.TreeSection.node\\n" +
                "9.nodeStyle16.TreeSection.leaf\\n7.request8.treeInfo)\";\n",
                 cr.getHtml().jsCode.toString());
    }
    public void test_html_explicitClass() {
        TreeSection tree = new TreeSection(new Dataset("id", "tree1",
                "request", "treeInfo", "class", "xyzzy"));
        tree.registerRequests(cr);
        tree.html(cr);
        assertEquals("generated HTML", "\n" +
                "<!-- Start TreeSection tree1 -->\n" +
                "<table cellspacing=\"0\" class=\"xyzzy\" " +
                "id=\"tree1\">\n" +
                "  <tr id=\"tree1_0\">\n" +
                "    <td class=\"left\" style=\"background-image: " +
                "url(/fizlib/images/treeSolid-line.gif); background-repeat: " +
                "no-repeat;\"><img src=\"/fizlib/images/treeSolid-leaf." +
                "gif\"></td>\n" +
                "    <td class=\"right\">leaf: child1</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "<!-- End TreeSection @id -->\n",
                cr.getHtml().getBody().toString());
    }

    public void test_registerRequests() {
        TreeSection tree = new TreeSection(new Dataset("id", "tree1",
                "request", "treeInfo"));
        tree.registerRequests(cr);
        tree.html(cr);
        assertEquals("registered requests", "treeInfo",
                cr.getRequestNames());
    }

    public void test_childrenHtml_propertyDefaults() {
        StringBuilder out = new StringBuilder();
        ArrayList<Dataset> children = new ArrayList<Dataset>();
        children.add(new Dataset("name", "Alice", "id", "111",
                "expandable", "1"));
        children.add(new Dataset("name", "Bob"));
        TreeSection.childrenHtml(cr, new Dataset("id", "tree1"), children,
                "tree1_3", out);
        assertEquals("generated HTML", "  <tr id=\"tree1_3_0\">\n" +
                "    <td class=\"left\" style=\"background-image: url(" +
                "/fizlib/images/treeSolid-line.gif); background-repeat: " +
                "repeat-y;\" onclick=\"void new Fiz.Ajax({url: &quot;" +
                "/fiz/TreeSection/ajaxExpand&quot;, reminders: [Fiz.Reminder." +
                "reminders[&quot;tree1&quot;], Fiz.Reminder.reminders[&quot;" +
                "tree1_3_0&quot;]]});\"><img src=\"/fizlib/images/treeSolid-" +
                "plus.gif\"></td>\n" +
                "    <td class=\"right\">node: Alice</td>\n" +
                "  </tr>\n" +
                "  <tr id=\"tree1_3_0_childRow\" style=\"display:none\">\n" +
                "    <td style=\"background-image: url(/fizlib/images/" +
                "treeSolid-line.gif); background-repeat: repeat-y;\"></td>\n" +
                "    <td><div class=\"nested\" id=\"tree1_3_0_childDiv\">" +
                "</div></td>\n" +
                "  </tr>\n" +
                "  <tr id=\"tree1_3_1\">\n" +
                "    <td class=\"left\" style=\"background-image: url(" +
                "/fizlib/images/treeSolid-line.gif); background-repeat: " +
                "no-repeat;\"><img src=\"/fizlib/images/treeSolid-leaf.gif\">" +
                "</td>\n" +
                "    <td class=\"right\">leaf: Bob</td>\n" +
                "  </tr>\n",
                out.toString());
    }
    public void test_childrenHtml_explicitProperties() {
        StringBuilder out = new StringBuilder();
        ArrayList<Dataset> children = new ArrayList<Dataset>();
        children.add(new Dataset("name", "Alice", "id", "111",
                "expandable", "1"));
        children.add(new Dataset("name", "Bob"));
        TreeSection.childrenHtml(cr, new Dataset("id", "tree1",
                "edgeFamily", "edge16", "leafStyle", "TreeSection.leaf2",
                "nodeStyle", "TreeSection.node2"), children,
                "tree1_3", out);
        assertEquals("generated HTML", "  <tr id=\"tree1_3_0\">\n" +
                "    <td class=\"left\" style=\"background-image: url(" +
                "/fizlib/images/edge16-line.gif); background-repeat: " +
                "repeat-y;\" onclick=\"void new Fiz.Ajax({url: &quot;" +
                "/fiz/TreeSection/ajaxExpand&quot;, reminders: [Fiz.Reminder." +
                "reminders[&quot;tree1&quot;], Fiz.Reminder.reminders[&quot;" +
                "tree1_3_0&quot;]]});\"><img src=\"/fizlib/images/edge16-" +
                "plus.gif\"></td>\n" +
                "    <td class=\"right\">node2: Alice</td>\n" +
                "  </tr>\n" +
                "  <tr id=\"tree1_3_0_childRow\" style=\"display:none\">\n" +
                "    <td style=\"background-image: url(/fizlib/images/" +
                "edge16-line.gif); background-repeat: repeat-y;\"></td>\n" +
                "    <td><div class=\"nested\" id=\"tree1_3_0_childDiv\">" +
                "</div></td>\n" +
                "  </tr>\n" +
                "  <tr id=\"tree1_3_1\">\n" +
                "    <td class=\"left\" style=\"background-image: url(" +
                "/fizlib/images/edge16-line.gif); background-repeat: " +
                "no-repeat;\"><img src=\"/fizlib/images/edge16-leaf.gif\">" +
                "</td>\n" +
                "    <td class=\"right\">leaf2: Bob</td>\n" +
                "  </tr>\n",
                out.toString());
    }
    public void test_childrenHtml_javascriptForExpandableElement() {
        StringBuilder out = new StringBuilder();
        ArrayList<Dataset> children = new ArrayList<Dataset>();
        children.add(new Dataset("name", "Alice", "id", "111",
                "expandable", "1"));
        children.add(new Dataset("name", "Bob"));
        TreeSection.childrenHtml(cr, new Dataset("id", "tree1"), children,
                "tree1_3", out);
        assertEquals("accumulated Javascript",
                "Fiz.Reminder.reminders[\"tree1_3_0\"] = \"24.JHB9AM69@," +
                "7:GY68T5G<EIB*49.15.TreeSection.row(2.id9.tree1_3_0\\n" +
                "4.name5.Alice)\";\n" +
                "Fiz.ids[\"tree1_3_0\"] = new Fiz.TreeRow(\"tree1_3_0\", " +
                "\"  <tr id=\\\"tree1_3_0\\\">\\n    <td class=\\\"left\\\" " +
                "style=\\\"background-image: url(/fizlib/images/treeSolid-" +
                "line.gif); background-repeat: repeat-y;\\\" onclick=\\\"void " +
                "new Fiz.Ajax({url: &quot;/fiz/TreeSection/ajaxExpand&quot;, " +
                "reminders: [Fiz.Reminder.reminders[&quot;tree1&quot;], " +
                "Fiz.Reminder.reminders[&quot;tree1_3_0&quot;]]});\\\">" +
                "<img src=\\\"/fizlib/images/treeSolid-plus.gif\\\"></td>\\n" +
                "    <td class=\\\"right\\\">node: Alice</td>\\n  </tr>\\n\"," +
                " \"  <tr id=\\\"tree1_3_0\\\">\\n    <td class=\\\"left\\\" " +
                "style=\\\"background-image: url(/fizlib/images/treeSolid-" +
                "line.gif); background-repeat: repeat-y;\\\" onclick=\\\"" +
                "Fiz.ids['tree1_3_0'].unexpand();\\\"><img src=\\\"/fizlib/" +
                "images/treeSolid-minus.gif\\\"></td>\\n    <td class=\\\"" +
                "right\\\">node-expanded: Alice</td>\\n  </tr>\\n\");\n",
                 cr.getHtml().jsCode.toString());
    }
    public void test_childrenHtml_lastRowExpandable() {
        StringBuilder out = new StringBuilder();
        ArrayList<Dataset> children = new ArrayList<Dataset>();
        children.add(new Dataset("name", "Alice", "id", "111",
                "expandable", "1"));
        TreeSection.childrenHtml(cr, new Dataset("id", "tree1"), children,
                "tree1_3", out);
        TestUtil.assertSubstring("generated HTML",
                "<tr id=\"tree1_3_0_childRow\" style=\"display:none\">\n" +
                "    <td></td>\n" +
                "    <td><div class=\"nested\" id=\"tree1_3_0_childDiv\">" +
                "</div></td>\n" +
                "  </tr>\n",
                out.toString());
    }

}
