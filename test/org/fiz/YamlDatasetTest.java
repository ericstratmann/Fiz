package org.fiz;
import java.io.*;

/**
 * Junit tests for the YamlDataset class.
 */

public class YamlDatasetTest extends junit.framework.TestCase {

    public void test_UnnamedValueError() {
        Error e = new YamlDataset.UnnamedValueError("my_file");
        assertEquals("exception message",
                "YAML dataset contains sequence(s) with unnamed values "
                + "(file \"my_file\")", e.getMessage());
    }

    public void test_newStringInstance() {
        Dataset d = YamlDataset.newStringInstance("first: 123\n"
                + "second: Simple test\n"
                + "third:\n  name: Bill\n  age: 27\n");
        assertEquals("dataset contents", "first:  123\n" +
                "second: Simple test\n" +
                "third:\n" +
                "    age:  27\n" +
                "    name: Bill\n", d.toString());
    }
    public void test_newStringInstance_unnamedValueError() {
        boolean gotException = false;
        try {
            Dataset d = YamlDataset.newStringInstance("- a\n- b\n");
        }
        catch (YamlDataset.UnnamedValueError e) {
            assertEquals("exception message",
                    "YAML dataset contains sequence(s) with unnamed values",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_newStringInstance_syntaxError() {
        boolean gotException = false;
        try {
            Dataset d = YamlDataset.newStringInstance("- a\n - b\n");
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: Error near line 2: "
                    + "End of document expected.",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_newFileinstance() {
        TestUtil.writeFile("test.yml", "first: abc\nsecond: 111\n");
        Dataset d = YamlDataset.newFileInstance("test.yml");
        assertEquals("check value", "abc", d.get("first"));
        assertEquals("check second value", "111", d.get("second"));
        TestUtil.deleteTree("test.yml");
    }
    public void test_newFileinstance_bogusFileName() {
        boolean gotException = false;
        try {
            Dataset d = YamlDataset.newFileInstance("bogus_44.yml");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't open dataset file \"bogus_44.yml\": The system "
                    + "cannot find the file specified",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_newFileinstance_syntaxError() {
        boolean gotException = false;
        TestUtil.writeFile("test.yml", "- a\n - b\n");
        try {
            Dataset d = YamlDataset.newFileInstance("test.yml");
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset (file \"test.yml\"): "
                    + "Error near line 2: End of document expected.",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteTree("test.yml");
    }
    public void test_newFileinstance_unnamedValue() {
        boolean gotException = false;
        TestUtil.writeFile("test.yml", "- a\n- b\n");
        try {
            Dataset d = YamlDataset.newFileInstance("test.yml");
        }
        catch (YamlDataset.UnnamedValueError e) {
            assertEquals("exception message",
                    "YAML dataset contains sequence(s) with unnamed values "
                    + "(file \"test.yml\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteTree("test.yml");
    }

    public void test_writeString() {
        Dataset d = new Dataset("first", "332", "second", " ab\tcd ");
        assertEquals("generated YAML",
                "first:  332\n" +
                "second: \" ab\\tcd \"\n", YamlDataset.writeString(d));
    }
    public void test_writeString_ioError() {
        Dataset d = new Dataset("value", "332");
        d.generateIoException = true;
        boolean gotException = false;
        try {
            YamlDataset.writeString(d);
        }
        catch (IOError e) {
            assertEquals("exception message", "error simulated",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_toString() {
        Dataset d = YamlDataset.newStringInstance(
                "a: 45\n"
                + "b:\n"
                + "  c: 78\n");
        assertEquals("generated YAML",
                "a: 45\n" +
                "b:\n" +
                "    c: 78\n", d.toString());
    }

    public void test_staticWriteFile_noComment() throws FileNotFoundException {
        Dataset d = new Dataset("first", "332", "second", " ab\tcd ");
        YamlDataset.writeFile(d, "test.yml", null);
        assertEquals("generated YAML",
                "first:  332\n" +
                "second: \" ab\\tcd \"\n",
                Util.readFile("test.yml").toString());
        TestUtil.deleteTree("test.yml");
    }
    public void test_staticWriteFile_addExtension()
            throws FileNotFoundException {
        Dataset d = new Dataset("first", "332");
        YamlDataset.writeFile(d, "test2", null);
        assertEquals("generated YAML",
                "first: 332\n",
                Util.readFile("test2.yml").toString());
        TestUtil.deleteTree("test2.yml");
    }
    public void test_staticWriteFile_cantOpenFile() {
        boolean gotException = false;
        try {
            Dataset d = new Dataset("first", "332", "second", " ab\tcd ");
            YamlDataset.writeFile(d, "_bogus_/_missing_/foo.yml", null);
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't open YAML dataset file " +
                    "\"_bogus_/_missing_/foo.yml\": The system cannot " +
                    "find the path specified",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_staticWriteFile_withComment()
            throws FileNotFoundException {
        Dataset d = new Dataset("value", "332");
        YamlDataset.writeFile(d, "test.yml", "Line #1\nSecond line");
        assertEquals("generated YAML",
                "# Line #1\n" +
                "# Second line\n" +
                "value: 332\n",
                Util.readFile("test.yml").toString());
        TestUtil.deleteTree("test.yml");
    }
    public void test_staticWriteFile_ioError() throws FileNotFoundException {
        Dataset d = new Dataset("value", "332");
        d.generateIoException = true;
        boolean gotException = false;
        try {
            YamlDataset.writeFile(d, "test.yml", null);
        }
        catch (IOError e) {
            assertEquals("exception message",
                    "I/O error in file \"test.yml\": error simulated",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteTree("test.yml");
    }

    public void test_writeFile() throws FileNotFoundException {
        YamlDataset d = YamlDataset.newStringInstance(
                "a: 45\n"
                + "b: \" abc \"\n");
        d.writeFile("test.yml", "Sample header2");
        assertEquals("generated YAML",
                "# Sample header2\n" +
                "a: 45\n" +
                "b: \" abc \"\n",
                Util.readFile("test.yml").toString());
        TestUtil.deleteTree("test.yml");
    }

    public void test_writeSubtree_basics() {
        Dataset d = YamlDataset.newStringInstance(
                "a: 45\n"
                + "b: Simple  name\n"
                + "children:\n"
                + "  - name: Bill\n"
                + "    age: 18\n"
                + "  - name: Alice\n"
                + "    age: 36\n"
                + "  - name: Susan\n"
                + "    age: 25\n"
                + "c:\n"
                + "  d:\n"
                + "    e: Arizona\n"
                + "  f: California\n"
                + "funny : \" abcd\\n\\n\"\n");
        assertEquals("generated YAML",
                "a:     45\n" +
                        "b:     Simple  name\n" +
                        "c:\n" +
                        "    d:\n" +
                        "        e: Arizona\n" +
                        "    f: California\n" +
                        "children:\n" +
                        "  - age:  18\n" +
                        "    name: Bill\n" +
                        "  - age:  36\n" +
                        "    name: Alice\n" +
                        "  - age:  25\n" +
                        "    name: Susan\n" +
                        "funny: \" abcd\\n\\n\"\n", d.toString());
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
                + "  d: \"Vallejo, California\"\n"
                + "special : \" abcd\\n\\n\"\n");

        Dataset d2 = YamlDataset.newStringInstance(d.toString());
        assertEquals("element with trailing space", "trailing space ",
                d2.get("a"));
        assertEquals("value from list of children", "Bill",
                d2.getPath("children.name"));
        assertEquals("nested value", "Vallejo, California",
                d2.getPath("c.d"));
        assertEquals("value with special characters", " abcd\n\n",
                d2.get("special"));
    }

    public void test_escapeYamlChars_emptyString() {
        assertEquals("\"\"", YamlDataset.escapeYamlChars(""));
    }
    public void test_escapeYamlChars_checkFirstChar() {
        assertEquals("first char requires quoting",
                "\"[a b]\"", YamlDataset.escapeYamlChars("[a b]"));
        assertEquals("first char requires quoting",
                "\"{xxx}\"", YamlDataset.escapeYamlChars("{xxx}"));
        assertEquals("first char requires quoting",
                "\"*xyz\"", YamlDataset.escapeYamlChars("*xyz"));
        assertEquals("first char requires quoting",
                "\"|xyz\"", YamlDataset.escapeYamlChars("|xyz"));
        assertEquals("first char requires quoting",
                "\">xyz\"", YamlDataset.escapeYamlChars(">xyz"));
        assertEquals("first char requires quoting",
                "\"\\\"xyz\"", YamlDataset.escapeYamlChars("\"xyz"));
        assertEquals("first char requires quoting",
                "\"'xyz\"", YamlDataset.escapeYamlChars("\'xyz"));
        assertEquals("first char requires quoting",
                "\"%xyz\"", YamlDataset.escapeYamlChars("%xyz"));
        assertEquals("first char requires quoting",
                "\" xyz\"", YamlDataset.escapeYamlChars(" xyz"));
    }
    public void test_escapeYamlChars_checkAllChars() {
        assertEquals("char requires quoting",
                "\"abc&def\"", YamlDataset.escapeYamlChars("abc&def"));
        assertEquals("char requires quoting",
                "\"abc!\"", YamlDataset.escapeYamlChars("abc!"));
        assertEquals("char requires quoting",
                "\"@abc\"", YamlDataset.escapeYamlChars("@abc"));
        assertEquals("char requires quoting",
                "\"abc, def\"", YamlDataset.escapeYamlChars("abc, def"));
        assertEquals("char requires quoting",
                "\"abc`def\"", YamlDataset.escapeYamlChars("abc`def"));
        assertEquals("char requires quoting",
                "\"abc #\"", YamlDataset.escapeYamlChars("abc #"));
        assertEquals("char requires quoting",
                "\"ab\\n\\x1f\\x00\"",
                YamlDataset.escapeYamlChars("ab\n\u001f\000"));
    }
    public void test_escapeYamlChars_trailingSpace() {
        assertEquals("\"12345 \"", YamlDataset.escapeYamlChars("12345 "));
    }
    public void test_escapeYamlChars_noQuotingNeeded() {
        assertEquals("12345", YamlDataset.escapeYamlChars("12345"));
        assertEquals("word two", YamlDataset.escapeYamlChars("word two"));
    }

    public void test_checkAndConvert_unnamedValue() {
        boolean gotException = false;
        TestUtil.writeFile("test.yml", "child:\n  - a\n  - b\n");
        try {
            Dataset d = YamlDataset.newFileInstance("test.yml");
        }
        catch (YamlDataset.UnnamedValueError e) {
            assertEquals("exception message",
                    "YAML dataset contains sequence(s) with unnamed values "
                    + "(file \"test.yml\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteTree("test.yml");
    }
    public void test_checkAndConvert_nestedDatasets() {
        Dataset y = YamlDataset.newStringInstance("a:\n  value: 41\n"
                + "  b:\n    value: 123.456\n    c:\n      value: 99\n");
        assertEquals("first-level value", "41", y.getPath("a.value"));
        assertEquals("second-level value", "123.456", y.getPath("a.b.value"));
        assertEquals("third-level value", "99", y.getPath("a.b.c.value"));
    }
    public void test_checkAndConvert_nestedLists() {
        Dataset y = YamlDataset.newStringInstance("a:\n  people:\n"
                + "    - name: Alice\n      age: 32\n"
                + "    - name: Bill\n      age: 44\n"
                + "    - name: Carol\n      age: 50\n"
                + "b: 999\nc: 1000\n");
        assertEquals("a.people[0].age", "32", y.getPath("a.people.age"));
        assertEquals("a.people[1].age", "44",
                y.getChild("a").getChildren("people")[1].get("age"));
        assertEquals("a.people[2].age", "50",
                y.getChild("a").getChildren("people")[2].get("age"));
    }
    public void test_checkAndConvert_nullValue() {
        Dataset y = YamlDataset.newStringInstance("first: abc\nsecond: def\n"
                + "child:\nthird: xyz\n");
        assertEquals("size of top-level dataset", 4, y.keySet().size());
        assertEquals("size of child dataset", 0,
                y.getChild("child").keySet().size());
    }
    public void test_checkAndConvert_convertValues() {
        Dataset y = YamlDataset.newStringInstance("first: 123\nsecond: 17.3\n"
                + "third: 18.1234567890123\nfourth: true\n");
        assertEquals("first value", "123", y.get("first"));
        assertEquals("read first value again", "123", y.get("first"));
        assertEquals("second value", "17.3", y.get("second"));
        assertEquals("third value", "18.1234567890123", y.get("third"));
        assertEquals("fourth value", "true", y.get("fourth"));
    }
}