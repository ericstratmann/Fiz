package org.fiz;
import org.xml.sax.*;

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
        assertEquals("age:  32 \n" +
                "child[0]:\n" +
                "  name: Alice\n" +
                "child[1]:\n" +
                "  name: Bob\n" +
                "name: Bill\n", d.toString());
    }
    public void test_newFileInstance() {
        TestUtil.writeFile("test.xml",
                "<child><name>Alice</name></child>\n");
        Dataset d = XmlDataset.newFileInstance("test.xml");
        assertEquals("name: Alice\n", d.toString());
        TestUtil.deleteTree("test.xml");
    }

    public void test_parse_string() {
        Dataset d = XmlDataset.parse(
                "<head><name>Bill</name><age>24</age>\n</head>\n", null);
        assertEquals("age: 24\n" +
                "name: Bill\n", d.toString());
    }
    public void test_parse_file() {
        TestUtil.writeFile("test.xml",
                "<foo><name>Bill</name><age>46</age>\n</foo>\n");
        Dataset d = XmlDataset.parse(null, "test.xml");
        assertEquals("age: 46\n" +
                "name: Bill\n", d.toString());
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
                    "I/O error in file \"_bogus/a/b/c.xml\": The system " +
                    "cannot find the path specified",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
