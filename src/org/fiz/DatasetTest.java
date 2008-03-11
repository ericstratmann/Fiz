/**
 * Junit tests for the Dataset class.
 */

package org.fiz;
import java.io.*;

public class DatasetTest extends junit.framework.TestCase {

    public void test_MissingValueError() {
        Error e = new Dataset.MissingValueError("keyName");
        assertEquals("exception message",
                "couldn't find dataset element \"keyName\"", e.getMessage());
    }

    public void test_SyntaxError() {
        Error e = new Dataset.SyntaxError(null, null);
        assertEquals("exception message",
                "syntax error in dataset input", e.getMessage());
        e = new Dataset.SyntaxError(null, "something bad happened");
        assertEquals("exception message",
                "syntax error in dataset input: something bad happened",
                e.getMessage());
        e = new Dataset.SyntaxError("file_name", "operator error");
        assertEquals("exception message",
                "syntax error in dataset input file \"file_name\": " +
                "operator error", e.getMessage());
    }

    public void test_UnsupportedFormatError() {
        Error e = new Dataset.UnsupportedFormatError("file_name.foo");
        assertEquals("exception message",
                "couldn't recognize format of dataset file \"file_name.foo\"",
                e.getMessage());
    }

    public void test_WrongTypeError() {
        Error e = new  Dataset.WrongTypeError("simple message");
        assertEquals("simple message", e.getMessage());
    }

    // Constructor Dataset(String[] keysAndValues)

    public void test_Dataset_keysAndValues_basics() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("first value", "a_value", d.get("a"));
        assertEquals("second value", "b_value", d.get("b"));
        assertEquals("number of values", 2, d.size());
    }
    public void test_Dataset_keysAndValues_ignoreExtraKey() {
        Dataset d = new Dataset("a", "a_value", "b");
        assertEquals("first value", "a_value", d.get("a"));
        assertEquals("number of values", 1, d.size());
    }

    public void test_getFileInstance_mustFindExtension() {
        TestUtil.writeFile("test.yml", "first: abc\nsecond: def\n");
        Dataset d = Dataset.getFileInstance("test");
        assertEquals("dataset value", "abc", d.get("first"));
        TestUtil.deleteTree("test.yml");
    }
    public void test_getFileInstance_cantFindExtension() {
        boolean gotException = false;
        try {
            Dataset d = Dataset.getFileInstance("a/b/bogus");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't open dataset file \"a/b/bogus\": couldn't "
                    + "find a file with a supported extension",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getFileInstance_ymlExtension() {
        TestUtil.writeFile("test.yml", "first: abc\nsecond: def\n");
        Dataset d = Dataset.getFileInstance("test.yml");
        assertEquals("check value", "abc", d.get("first"));
        TestUtil.deleteTree("test.yml");
    }
    public void test_getFileInstance_yamlExtension() {
        TestUtil.writeFile("test.yaml", "first: 24\nsecond: 35\n");
        Dataset d = Dataset.getFileInstance("test.yaml");
        assertEquals("check value", "24", d.get("first"));
        TestUtil.deleteTree("test.yaml");
    }
    public void test_getFileInstance_unknownExtension() {
        boolean gotException = false;
        try {
            Dataset d = Dataset.getFileInstance("bogus_44.xyz");
        }
        catch (Dataset.UnsupportedFormatError e) {
            assertEquals("exception message",
                    "couldn't recognize format of dataset file " +
                    "\"bogus_44.xyz\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getFileInstanceFromPath_basics() {
        (new File("_test1_")).mkdir();
        TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
        Dataset d = Dataset.getFileInstanceFromPath("test",
                new String[] {"_test1_"}, Dataset.PathHandling.CHAIN);
        assertEquals("check value", "24", d.get("first"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_getFileInstanceFromPath_firstOnly() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        Dataset d = Dataset.getFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child"},
                Dataset.PathHandling.FIRST_ONLY);
        assertEquals("first value should exist", "24", d.get("first"));
        assertEquals("third value shouldn't exist", null, d.check("third"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_getFileInstanceFromPath_chain() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        (new File("_test1_/child2")).mkdir();
        TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        TestUtil.writeFile("_test1_/child2/test.yaml",
                "first: 99\nfourth: 777\n");
        Dataset d = Dataset.getFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child", "_test1_/child2"},
                Dataset.PathHandling.CHAIN);
        assertEquals("value from beginning of chain", "24", d.get("first"));
        assertEquals("value from middle of chain", "value", d.check("third"));
        assertEquals("value from end of chain", "777", d.check("fourth"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_getFileInstanceFromPath_FileNotFoundError() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        Dataset d = Dataset.getFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child"},
                Dataset.PathHandling.CHAIN);
        assertEquals("dataset value", "value", d.check("third"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_getFileInstanceFromPath_datasetNotFound() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        boolean gotException = false;
        try {
            Dataset d = Dataset.getFileInstanceFromPath("sample",
                    new String[] {"_test1_", "_test1_/child"},
                    Dataset.PathHandling.FIRST_ONLY);
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find dataset file \"sample\" in path "
                    + "(\"_test1_\", \"_test1_/child\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteTree("_test1_");
    }

    public void test_containsKey() {
        Dataset d = new Dataset(new String[] {"first", "abc",
                "second", "def"});
        assertEquals("first value exists", true, d.containsKey("first"));
        assertEquals("second value exists", true, d.containsKey("second"));
        assertEquals("third value doesn't exist", false, d.containsKey("third"));
    }

    public void test_check() {
        Dataset d = new Dataset(new String[] {"a", "a_value", "b", "b_value"});
        Dataset d2 = new Dataset();
        Dataset d3 = new Dataset(new String[] {"c", "c_value"});
        d.setChain(d2);
        d2.setChain(d3);
        assertEquals("value from dataset", "a_value", d.check("a"));
        assertEquals("value from chain", "c_value", d.check("c"));
        assertEquals("undefined key", null, d.check("undefined"));
    }

    public void test_get_basics() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("first value", "b_value", d.get("b"));
    }
    public void test_get_searchChain() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        Dataset d2 = new Dataset();
        Dataset d3 = new Dataset("c", "c_value");
        d.setChain(d2);
        d2.setChain(d3);
        assertEquals("value from chained dataset", "c_value", d.get("c"));
    }
    public void test_get_missingValue() {
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            String value = d.get("x");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"x\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getChild_basics() {
        Dataset d = new YamlDataset("child:\n  age: 25\n  weight: 115\n");
        Dataset d4 = d.getChild("child");
        assertEquals("first value", "25", d4.get("age"));
    }
    public void test_getChild_searchChain() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        Dataset d2 = new Dataset();
        Dataset d3 = new YamlDataset("child:\n  age: 25\n  weight: 115\n");
        d.setChain(d2);
        d2.setChain(d3);
        Dataset d4 = d.getChild("child");
        assertEquals("value from childin chained dataset", "115",
                d4.get("weight"));
    }
    public void test_getChild_missingValue() {
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            d.getChild("x");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"x\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getChildren_basics() {
        Dataset d = new YamlDataset("children:\n  - name: Alice\n"
                + "  - name: Bob\n");
        Dataset[] children = d.getChildren("children");
        assertEquals("number of children", 2, children.length);
        assertEquals("name of second child", "Bob", children[1].get("name"));
    }
    public void test_getChildren_searchChain() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        Dataset d2 = new Dataset();
        Dataset d3 = new YamlDataset("children:\n  - name: Alice\n"
                + "  - name: Bob\n");
        d.setChain(d2);
        d2.setChain(d3);
        Dataset[] children = d.getChildren("children");
        assertEquals("number of children", 2, children.length);
        assertEquals("name of first child", "Alice", children[0].get("name"));
    }
    public void test_getChildren_keyExists() {
        boolean gotException = false;
        Dataset d = new Dataset(new String[] {"a", "a_value"});
        try {
            d.getChildren("a");
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset key \"a\": expected nested "
                    + "dataset, found string value", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getChildren_noSuchKey() {
        Dataset d = new Dataset("a", "a_value");
        Dataset[] children = d.getChildren("nonexistent");
        assertEquals("number of children", 0, children.length);
    }

    public void test_getPath_success() {
        Dataset d = new Dataset("first", "12345");
        assertEquals("value", "12345", d.getPath("first"));
    }
    public void test_getPath_searchChain() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        Dataset d2 = new Dataset("first", "12345");
        d.setChain(d2);
        assertEquals("value", "12345", d.getPath("first"));
    }
    public void test_getPath_missingValueError() {
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            d.getPath("non-existent");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"non-existent\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getPath_multiLevelPath() {
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            d.getPath("a.b.c");
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected nested "
                    + "dataset, found string value",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_lookup_nonexistentKey() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("null return value", null,
                d.lookup("c", Dataset.DesiredType.STRING));
    }
    public void test_lookup_foundStringValue() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("string value", "b_value",
                d.lookup("b", Dataset.DesiredType.STRING));
    }
    public void test_lookup_wantNestedDataset() {
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            d.lookup("a", Dataset.DesiredType.DATASET);
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset key \"a\": expected nested "
                    + "dataset, found string value", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_lookupPath_noDotsInPath() {
        Dataset d = new Dataset("first", "12345");
        assertEquals("value", "12345", d.lookupPath("first",
                Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_missingKey() {
        Dataset d = new Dataset("a", "a_value");
        assertEquals("value", null, d.lookupPath("non-existent",
                Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_multiLevelPath() {
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            d.lookupPath("a.b.c", Dataset.DesiredType.STRING);
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected nested "
                    + "dataset, found string value",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getSetChain() {
        Dataset d = new Dataset("first", "12345");
        Dataset d2 = new Dataset("second", "55");
        assertEquals("chain originally null", null, d.getChain());
        d.setChain(d2);
        assertEquals("chain refers to another dataset", d2, d.getChain());
        d.setChain(null);
        assertEquals("chain null again", null, d.getChain());
    }

    // No tests needed for size() method.
}