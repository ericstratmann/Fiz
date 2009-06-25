package org.fiz;

import java.io.*;
import org.xml.sax.*;

import org.fiz.test.*;

/**
 * Junit tests for the XmlDataset class.
 */

public class XmlDatasetTest extends junit.framework.TestCase {
    public void test_newStringInstance() {
        Dataset d = XmlDataset.newStringInstance(
                "<head> <name>Bill</name> <age> 32 </age>\n" +
                "<child><name>Alice</name></child>\n" +
                "<child><name>Bob</name></child>\n" +
                "</head>\n");
        assertEquals("age:  \" 32 \"\n" +
                "child:\n" +
                "  - name: Alice\n" +
                "  - name: Bob\n" +
                "name: Bill\n", YamlDataset.writeString(d));
    }

    public void test_newFileInstance() {
        TestUtil.writeFile("test.xml",
                "<child><name>Alice</name></child>\n");
        Dataset d = XmlDataset.newFileInstance("test.xml");
        assertEquals("name: Alice\n", YamlDataset.writeString(d));
        TestUtil.deleteTree("test.xml");
    }

    public void test_writeString() {
        Dataset d = new Dataset("first", "332", "second", " ab&cd ");
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<dataset>\n" +
                "  <first>332</first>\n" +
                "  <second> ab&amp;cd </second>\n" +
                "</dataset>\n", XmlDataset.writeString(d));
    }

    public void test_toString() {
        Dataset d = XmlDataset.newStringInstance(
                "<head><name>Bill</name><age>32</age>\n" +
                "<child><name>Alice</name></child>\n" +
                "</head>\n");
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<dataset>\n" +
                "  <age>32</age>\n" +
                "  <child>\n" +
                "    <name>Alice</name>\n" +
                "  </child>\n" +
                "  <name>Bill</name>\n" +
                "</dataset>\n", d.toString());
    }

    public void test_staticWriteFile_noComment() throws FileNotFoundException {
        Dataset d = new Dataset("first", "332", "second", "ab&cd");
        XmlDataset.writeFile(d, "test.xml", null);
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<dataset>\n" +
                "  <first>332</first>\n" +
                "  <second>ab&amp;cd</second>\n" +
                "</dataset>\n",
                Util.readFile("test.xml").toString());
        TestUtil.deleteTree("test.xml");
    }
    public void test_staticWriteFile_addException()
            throws FileNotFoundException {
        Dataset d = new Dataset("first", "332");
        XmlDataset.writeFile(d, "test2", null);
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<dataset>\n" +
                "  <first>332</first>\n" +
                "</dataset>\n",
                Util.readFile("test2.xml").toString());
        TestUtil.deleteTree("test2.xml");
    }
    public void test_staticWriteFile_cantOpenFile() {
        boolean gotException = false;
        try {
            Dataset d = new Dataset("first", "332", "second", " ab\tcd ");
            XmlDataset.writeFile(d, "_bogus_/_missing_/foo.xml", null);
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't open XML dataset file " +
                    "\"_bogus_/_missing_/foo.xml\": ...",
                    TestUtil.truncate(e.getMessage(), "xml\": "));
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_staticWriteFile_withComment()
            throws FileNotFoundException {
        Dataset d = new Dataset("first", "332");
        XmlDataset.writeFile(d, "test.xml", "Line #1\nSecond line");
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!-- Line #1\n" +
                "     Second line -->\n" +
                "<dataset>\n" +
                "  <first>332</first>\n" +
                "</dataset>\n",
                Util.readFile("test.xml").toString());
        TestUtil.deleteTree("test.xml");
    }
    public void test_staticWriteFile_ioError() throws FileNotFoundException {
        Dataset d = new Dataset("value", "332");
        d.generateIoException = true;
        boolean gotException = false;
        try {
            XmlDataset.writeFile(d, "test.xml", null);
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "I/O error in file \"test.xml\": error simulated",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteTree("test.xml");
    }

    public void test_writeFile() throws FileNotFoundException {
        XmlDataset d = XmlDataset.newStringInstance(
                "<head><name>Bill</name><age>32</age>\n</head>\n");
        d.writeFile("test.xml", "Sample header2");
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!-- Sample header2 -->\n" +
                "<dataset>\n" +
                "  <age>32</age>\n" +
                "  <name>Bill</name>\n" +
                "</dataset>\n",
                Util.readFile("test.xml").toString());
        TestUtil.deleteTree("test.xml");
    }

    public void test_writeSubtree_basics() {
        Dataset d = YamlDataset.newStringInstance(
                "a: 45\n"
                + "b: Simple  name\n"
                + "child:\n"
                + "  - name: Bill\n"
                + "    age: 18\n"
                + "  - name: Alice\n"
                + "    age: 36\n"
                + "  - name: Susan\n"
                + "    age: 25\n"
                + "c:\n"
                + "  d:\n"
                + "    e: Arizona\n"
                + "  f: California\n");
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<dataset>\n" +
                "  <a>45</a>\n" +
                "  <b>Simple  name</b>\n" +
                "  <c>\n" +
                "    <d>\n" +
                "      <e>Arizona</e>\n" +
                "    </d>\n" +
                "    <f>California</f>\n" +
                "  </c>\n" +
                "  <child>\n" +
                "    <age>18</age>\n" +
                "    <name>Bill</name>\n" +
                "  </child>\n" +
                "  <child>\n" +
                "    <age>36</age>\n" +
                "    <name>Alice</name>\n" +
                "  </child>\n" +
                "  <child>\n" +
                "    <age>25</age>\n" +
                "    <name>Susan</name>\n" +
                "  </child>\n" +
                "</dataset>\n", XmlDataset.writeString(d));
    }
    public void test_writeSubtree_quoting() {
        Dataset d = YamlDataset.newStringInstance(
                "a: 45\n"
                + "b: \"<name>\"\n"
                + "c: this&that\n");
        assertEquals("generated XML",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<dataset>\n" +
                "  <a>45</a>\n" +
                "  <b>&lt;name&gt;</b>\n" +
                "  <c>this&amp;that</c>\n" +
                "</dataset>\n", XmlDataset.writeString(d));
    }
    public void test_writeSubtree_rereadOutput() {
        Dataset d = YamlDataset.newStringInstance(
                "a: \"trailing space \"\n"
                + "b: Simple  name\n"
                + "children:\n"
                + "  - name: Bill\n"
                + "    age: 18\n"
                + "  - name: Alice\n"
                + "    age: 36\n"
                + "c:\n"
                + "  d: California\n"
                + "special : \"&abcd\\n\\n\"\n");

        Dataset d2 = XmlDataset.newStringInstance(XmlDataset.writeString(d));
        assertEquals("element with trailing space", "trailing space ",
                d2.get("a"));
        assertEquals("value from list of children", "Bill",
                d2.getPath("children.name"));
        assertEquals("bested value", "California",
                d2.getPath("c.d"));
        assertEquals("value with special characters", "&abcd\n\n",
                d2.get("special"));
    }

    public void test_parse_string() {
        Dataset d = XmlDataset.parse(
                "<head><name>Bill</name><age>24</age>\n</head>\n", null);
        assertEquals("age:  24\n" +
                "name: Bill\n", YamlDataset.writeString(d));
    }
    public void test_parse_file() {
        TestUtil.writeFile("test.xml",
                "<foo><name>Bill</name><age>46</age>\n</foo>\n");
        Dataset d = XmlDataset.parse(null, "test.xml");
        assertEquals("age:  46\n" +
                "name: Bill\n", YamlDataset.writeString(d));
        TestUtil.deleteTree("test.xml");
    }
    public void test_parse_syntaxError() {
        boolean gotException = false;
        try {
            Dataset d = XmlDataset.parse(
                    "<head> <name>Bill\n</head>\n", null);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: The element type " +
                    "\"name\" must be terminated by the matching " +
                    "end-tag \"</name>\".",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_parse_disallowedStructure() {
        boolean gotException = false;
        try {
            Dataset d = XmlDataset.parse("<head> <name>Bill</name>\n" +
                    "<name><a></a></name>\n</head>\n", null);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: improper use of XML " +
                    "(line 2): conflicting elements named \"name\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_parse_nonexistentFile() {
        boolean gotException = false;
        try {
            Dataset d = XmlDataset.parse(null, "_bogus/a/b/c.xml");
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "I/O error in file \"_bogus/a/b/c.xml\": ...",
                    TestUtil.truncate(e.getMessage(), "xml\": "));
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
