package org.fiz;

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
        assertEquals("check value", "Simple test", d.get("second"));
        assertEquals("check value", "123", d.getPath("first"));
        assertEquals("number of values", 3, d.size());
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