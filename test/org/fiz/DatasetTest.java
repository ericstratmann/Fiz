package org.fiz;
import java.io.*;
import java.util.*;

/**
 * Junit tests for the Dataset class.
 */

public class DatasetTest extends junit.framework.TestCase {

    public void test_MissingValueError() {
        Dataset.MissingValueError e = new Dataset.MissingValueError("keyName");
        assertEquals("exception message",
                "couldn't find dataset element \"keyName\"", e.getMessage());
        assertEquals("name of missing key", "keyName", e.getMissingKey());
    }

    public void test_SyntaxError() {
        Error e = new Dataset.SyntaxError(null, null);
        assertEquals("exception message",
                "syntax error in dataset", e.getMessage());
        e = new Dataset.SyntaxError(null, "something bad happened");
        assertEquals("exception message",
                "syntax error in dataset: something bad happened",
                e.getMessage());
        e = new Dataset.SyntaxError("file_name", "operator error");
        assertEquals("exception message",
                "syntax error in dataset (file \"file_name\"): " +
                "operator error", e.getMessage());
    }

    public void test_UnsupportedFormatError() {
        Error e = new Dataset.UnsupportedFormatError("file_name.foo");
        assertEquals("exception message",
                "couldn't recognize format of dataset file \"file_name.foo\"",
                e.getMessage());
    }

    public void test_WrongTypeError() {
        Error e = new Dataset.WrongTypeError("simple message");
        assertEquals("simple message", e.getMessage());
    }

    public void test_UnknownFileFormatError() {
        Error e = new Dataset.UnknownFileFormatError("simple message");
        assertEquals("simple message", e.getMessage());
    }

    // Constructor Dataset(String... keysAndValues)

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

    // Constructor Dataset(HashMap contents, String fileName)

    @SuppressWarnings("unchecked")
    public void test_Dataset_hashMapAndString() {
        HashMap map = new HashMap();
        map.put("name44", "value66");
        Dataset d = new Dataset(map, "file123");
        assertEquals("value from dataset", "value66", d.get("name44"));
    }

    public void test_newFileInstance_searchForExtension() {
        TestUtil.writeFile("test.yml", "first: abc\nsecond: def\n");
        Dataset d = Dataset.newFileInstance("test");
        assertEquals("dataset value", "abc", d.get("first"));
        TestUtil.deleteTree("test.yml");
    }
    public void test_newFileInstance_cantFindExtension() {
        boolean gotException = false;
        try {
            Dataset d = Dataset.newFileInstance("a/b/bogus");
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
    public void test_newFileInstance_ymlExtension() {
        TestUtil.writeFile("test.yml", "first: abc\nsecond: def\n");
        Dataset d = Dataset.newFileInstance("test");
        assertEquals("check value", "abc", d.get("first"));
        TestUtil.deleteTree("test.yml");
    }
    public void test_newFileInstance_yamlExtension() {
        TestUtil.writeFile("test.yaml", "first: 24\nsecond: 35\n");
        Dataset d = Dataset.newFileInstance("test");
        assertEquals("check value", "24", d.get("first"));
        TestUtil.deleteTree("test.yaml");
    }
    public void test_newFileInstance_xmlExtension() {
        TestUtil.writeFile("test.xml",
                "<message><first>123</first><second>456</second></message>");
        Dataset d = Dataset.newFileInstance("test");
        assertEquals("check value", "123", d.get("first"));
        TestUtil.deleteTree("test.xml");
    }
    public void test_newFileInstance_unknownExtension() {
        boolean gotException = false;
        try {
            Dataset d = Dataset.newFileInstance("bogus_44.xyz");
        }
        catch (Dataset.UnsupportedFormatError e) {
            assertEquals("exception message",
                    "couldn't recognize format of dataset file " +
                    "\"bogus_44.xyz\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_newFileInstanceFromPath_basics() {
        (new File("_test1_")).mkdir();
        TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
        Dataset d = Dataset.newFileInstanceFromPath("test",
                new String[] {"_test1_"}, Dataset.PathHandling.CHAIN);
        assertEquals("check value", "24", d.get("first"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_newFileInstanceFromPath_firstOnly() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        Dataset d = Dataset.newFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child"},
                Dataset.PathHandling.FIRST_ONLY);
        assertEquals("first value should exist", "24", d.get("first"));
        assertEquals("third value shouldn't exist", null, d.check("third"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_newFileInstanceFromPath_chain() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        (new File("_test1_/child2")).mkdir();
        TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        TestUtil.writeFile("_test1_/child2/test.yaml",
                "first: 99\nfourth: 777\n");
        Dataset d = Dataset.newFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child", "_test1_/child2"},
                Dataset.PathHandling.CHAIN);
        assertEquals("value from beginning of chain", "24", d.get("first"));
        assertEquals("value from middle of chain", "value", d.check("third"));
        assertEquals("value from end of chain", "777", d.check("fourth"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_newFileInstanceFromPath_FileNotFoundError() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        Dataset d = Dataset.newFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child"},
                Dataset.PathHandling.CHAIN);
        assertEquals("dataset value", "value", d.check("third"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_newFileInstanceFromPath_datasetNotFound() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        boolean gotException = false;
        try {
            Dataset d = Dataset.newFileInstanceFromPath("sample",
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

    public void test_addChild() {
        Dataset d = new Dataset("first", "12345", "child", "Fred");
        Dataset d2 = new Dataset("name", "Alice");
        Dataset d3 = new Dataset("name", "Bob");
        Dataset d4 = new Dataset("name", "Charlie");
        Dataset d5 = new Dataset("name", "Donna");
        d.addChild("child", d2);
        d.addChild("child", d3);
        d.addChild("child", d4);
        d.addChild("wife", d5);
        assertEquals("dataset contents", "child:\n" +
                "  - name: Alice\n" +
                "  - name: Bob\n" +
                "  - name: Charlie\n" +
                "first: 12345\n" +
                "wife:\n" +
                "    name: Donna\n", d.toString());
    }

    public void test_check() {
        Dataset d = YamlDataset.newStringInstance(
                "a: a_value\nb: b_value\nnested:\n  x: x_value\n");
        Dataset d2 = new Dataset();
        Dataset d3 = new Dataset("c", "c_value");
        d.setChain(d2);
        d2.setChain(d3);
        assertEquals("value from dataset", "a_value", d.check("a"));
        assertEquals("value from chain", "c_value", d.check("c"));
        assertEquals("undefined key", null, d.check("undefined"));
        assertEquals("wrong type", null, d.check("nested"));
    }

    public void test_clear() {
        Dataset d = YamlDataset.newStringInstance(
                "a: a_value\nb: b_value\nnested:\n  x: x_value\n");
        d.clear();
        assertEquals("size after clearing", 0, d.size());
        assertEquals("contents of cleared dataset", "", d.toString());
    }

    public void test_clone_basics() {
        Dataset d = YamlDataset.newStringInstance(
                "a: a_value\n" +
                "b: b_value\n" +
                "level1:\n" +
                "  level2:\n" +
                "    c: c_value\n" +
                "    d: d_value\n" +
                "list:\n" +
                "  - name: Alice\n" +
                "  - name: Bill\n" +
                "  - name: Carol\n" +
                "    age: 28\n");
        Dataset clone = d.clone();

        // Modify the original dataset and make sure that the
        // modifications don't affect the clone.
        d.set("b", "111");
        d.getChildPath("level1.level2").set("d", "222");
        d.getChild("list").set("name", "333");
        d.getChildren("list")[2].set("age", "444");
        assertEquals("cloned dataset",
                "a: a_value\n" +
                "b: b_value\n" +
                "level1:\n" +
                "    level2:\n" +
                "        c: c_value\n" +
                "        d: d_value\n" +
                "list:\n" +
                "  - name: Alice\n" +
                "  - name: Bill\n" +
                "  - age: 28\n" +
                "    name: Carol\n", clone.toString());
    }
    public void test_clone_copyChain() {
        Dataset d = new Dataset("name", "Alice");
        Dataset d2 = new Dataset("name", "Bill");
        d.setChain(d2);
        Dataset clone = d.clone();
        assertEquals("chain in clone", "Bill", clone.getChain().get("name"));
    }
    public void test_clone_copyFileName() {
        Dataset d = new Dataset("name", "Alice");
        d.fileName = "xyzzy";
        Dataset clone = d.clone();
        assertEquals("file name in clone", "xyzzy", clone.fileName);
    }

    public void test_containsKey() {
        Dataset d = new Dataset(new String[] {"first", "abc",
                "second", "def"});
        Dataset d2 = new Dataset(new String[] {"chain", "chain_value"});
        d.setChain(d2);
        assertEquals("first value exists", true, d.containsKey("first"));
        assertEquals("second value exists", true, d.containsKey("second"));
        assertEquals("third value doesn't exist", false,
                d.containsKey("third"));
        assertEquals("value exists in chain", true,
                d.containsKey("chain"));
    }

    public void test_copyFrom() {
        Dataset source = YamlDataset.newStringInstance(
                "a:\n" +
                "  name: Alice\n" +
                "  age: 24\n" +
                "c: xyzzy\n");
        Dataset dest = YamlDataset.newStringInstance(
                "a: a_value\n" +
                "b: b_value\n" +
                "c:\n" +
                "    d: d_value\n");
        dest.copyFrom(source);

        // Modify the source dataset and make sure that the
        // modifications don't affect the destination.
        source.getChild("a").set("name", "333");
        assertEquals("cloned dataset",
                "a:\n" +
                "    age: 24\n" +
                "    name: Alice\n" +
                "b: b_value\n" +
                "c: xyzzy\n", dest.toString());
    }

    public void test_createChild() {
        Dataset d = YamlDataset.newStringInstance("child: value\n");
        d.createChild("child").set("id", "9924");
        assertEquals("dataset contents", "child:\n" +
                "    id: 9924\n", d.toString());
    }

    public void test_createChild_withDataset() {
        Dataset d = YamlDataset.newStringInstance(
                "child:\n" +
                "  age: 4\n ");
        Dataset d2 = d.createChild("child", new Dataset("age", "6",
                "name", "Carol"));
        d2.set("id", "9924");
        assertEquals("child contents", "age: 6\n" +
                "id: 9924\n" +
                "name: Carol\n", d2.toString());
        assertEquals("dataset contents", "child:\n" +
                "    age: 6\n" +
                "    id: 9924\n" +
                "    name: Carol\n", d.toString());
    }

    public void test_createChildPath() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2:\n" +
                "    child: value\n");
        d.createChildPath("level1.level2.child").set("id", "9924");
        assertEquals("dataset contents", "level1:\n" +
                "    level2:\n" +
                "        child:\n" +
                "            id: 9924\n", d.toString());
        assertEquals("result from second call", "child:\n" +
                "    id: 9924\n",
                d.createChildPath("level1.level2").toString());
    }

    public void test_createChildPath_withDataset() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2:\n" +
                "    child: value\n");
        Dataset d2 = d.createChildPath("level1.level2",
                new Dataset("age", "6", "name", "Carol"));
        d2.set("id", "9924");
        assertEquals("child contents", "age: 6\n" +
                "id: 9924\n" +
                "name: Carol\n", d2.toString());
        assertEquals("dataset contents", "level1:\n" +
                "    level2:\n" +
                "        age: 6\n" +
                "        id: 9924\n" +
                "        name: Carol\n", d.toString());
    }

    public void test_delete() {
        Dataset d = new Dataset("first", "12345", "second", "6789");
        d.delete("first");
        assertEquals("dataset contents", "second: 6789\n", d.toString());
    }

    public void test_deletePath() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2:\n" +
                "    first: 1\n" +
                "    second: 2\n");
        d.deletePath("level1.level2.first");
        assertEquals("dataset contents", "level1:\n" +
                "    level2:\n" +
                "        second: 2\n", d.toString());
    }
    public void test_deletePath_nonexistentPath() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2:\n" +
                "    first: 1\n" +
                "    second: 2\n");
        d.deletePath("level3.level4.first");
        d.deletePath("level1.level2.bogus");
        assertEquals("dataset contents", "level1:\n" +
                "    level2:\n" +
                "        first: 1\n" +
                "        second: 2\n", d.toString());
    }

    public void test_get_basics() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("first value", "b_value", d.get("b"));
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
        Dataset d = YamlDataset.newStringInstance("child:\n  age: 25\n  weight: 115\n");
        Dataset d4 = d.getChild("child");
        assertEquals("first value", "25", d4.get("age"));
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

    public void test_getChildPath_basics() {
        Dataset d = YamlDataset.newStringInstance(
                "level0:\n" +
                "  level1:\n" +
                "    level2:\n" +
                "      level3:\n" +
                "        value: 88\n");
        Dataset d2 = d.getChildPath("level0.level1.level2.level3");
        assertEquals("value from descended", "88", d2.get("value"));
    }
    public void test_getChildPath_missingValue() {
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            d.getChildPath("x.y.z");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"x.y.z\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getChildren_basics() {
        Dataset d = YamlDataset.newStringInstance("children:\n  - name: Alice\n"
                + "  - name: Bob\n");
        Dataset[] children = d.getChildren("children");
        assertEquals("number of children", 2, children.length);
        assertEquals("name of second child", "Bob", children[1].get("name"));
    }
    public void test_getChildren_keyExists() {
        boolean gotException = false;
        Dataset d = new Dataset(new String[] {"a", "a_value"});
        try {
            d.getChildren("a");
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected nested "
                    + "dataset but found string value \"a_value\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getChildren_noSuchKey() {
        Dataset d = new Dataset("a", "a_value");
        Dataset[] children = d.getChildren("nonexistent");
        assertEquals("number of children", 0, children.length);
    }

    public void test_getChildrenPath_descendentsExist() {
        Dataset d = YamlDataset.newStringInstance(
                "level0:\n" +
                "  level1:\n" +
                "    level2:\n" +
                "      level3:\n" +
                "        - value: 1\n" +
                "        - value: 2\n" +
                "        - value: 3\n");
        Dataset[] kids = d.getChildrenPath("level0.level1.level2.level3");
        assertEquals("member of datasets", 3, kids.length);
        assertEquals("value from first descendent", "1",
                kids[0].get("value"));
        assertEquals("value from second descendent", "2",
                kids[1].get("value"));
    }
    public void test_getChildrenPath_noSuchDescendents() {
        Dataset d = YamlDataset.newStringInstance(
                "level0:\n" +
                "  level1:\n");
        Dataset[] kids = d.getChildrenPath("level0.level1.level2.level3");
        assertEquals("number of datasets", 0, kids.length);
    }

    public void test_getFileName() {
        Dataset d = new Dataset("name", "Alice");
        d.fileName = "sample file name";
        assertEquals("getFileName result", "sample file name",
                d.getFileName());
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
                    "wrong type for dataset element \"a\": expected nested "
                    + "dataset but found string value \"a_value\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_lookup_searchChain() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        Dataset d2 = new Dataset();
        Dataset d3 = new Dataset("c", "c_value");
        d.setChain(d2);
        d2.setChain(d3);
        assertEquals("string value", "c_value",
                d.lookup("c", Dataset.DesiredType.STRING));
    }
    public void test_lookup_nonexistentKey() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("null return value", null,
                d.lookup("c", Dataset.DesiredType.STRING));
    }

    public void test_lookupPath_noDotsInPath() {
        Dataset d = new Dataset("first", "12345");
        assertEquals("value", "12345", d.lookupPath("first",
                Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_searchChain() {
        Dataset d = YamlDataset.newStringInstance("child1:\n  name: Alice\n"
                + "  age: 24\n");
        Dataset d2 = YamlDataset.newStringInstance("child1:\n  weight: 118\n"
                + "child2:\n  name: Bill\n  weight: 185\n");
        d.setChain(d2);
        assertEquals("value in chain", "Bill", d.lookupPath("child2.name",
                Dataset.DesiredType.STRING));
        assertEquals("chain is hidden", null, d.lookupPath("child1.weight",
                Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_missingKey() {
        Dataset d = new Dataset("a", "a_value");
        assertEquals("value", null, d.lookupPath("non-existent",
                Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_listInPath() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2:\n" +
                "    - name: Alice\n" +
                "    - name: Bill\n");
        assertEquals("value in list", "Alice", d.lookupPath(
                "level1.level2.name", Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_wrongTypeInPath() {
        Dataset d = new Dataset("a", "a_value");
        assertEquals("value", null, d.lookupPath("a.b.c",
                Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_valueHasWrongType() {
        boolean gotException = false;
        Dataset d = YamlDataset.newStringInstance("child1:\n  name: Alice\n"
                + "  age: 24\n");
        try {
            d.lookupPath("child1", Dataset.DesiredType.STRING);
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"child1\": expected "
                    + "string value but found nested dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getPath_success() {
        Dataset d = YamlDataset.newStringInstance("child:\n  name: Alice\n"
                + "  age: 24\n");
        assertEquals("value", "24", d.getPath("child.age"));
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

    public void test_set() {
        Dataset d = new Dataset("first", "12345");
        d.set("second", "66");
        assertEquals("dataset contents", "first: 12345\n" +
                "second: 66\n", d.toString());
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

    // No tests needed for toString() method.

    public void test_writeFile() {
        boolean gotException = false;
        Dataset d = new Dataset();
        try {
            d.writeFile("foo", "simple comment");
        }
        catch (Dataset.UnknownFileFormatError e) {
            assertEquals("exception message",
                    "class Dataset doesn't know how to write datasets " +
                    "to files",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_checkValue_wantedStringGotString() {
        Dataset d = new Dataset();
        assertEquals("value", "abc", d.checkValue("a",
                Dataset.DesiredType.STRING, new String("abc")));
    }
    public void test_checkValue_wantedStringGotChild() {
        boolean gotException = false;
        Dataset d = new Dataset();
        try {
            d.checkValue("a", Dataset.DesiredType.STRING, new HashMap());
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected string "
                    + "value but found nested dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_checkValue_wantedStringGotList() {
        boolean gotException = false;
        Dataset d = new Dataset();
        try {
            d.checkValue("a", Dataset.DesiredType.STRING, new ArrayList());
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected string "
                    + "value but found list",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_checkValue_wantedChildGotString() {
        boolean gotException = false;
        Dataset d = new Dataset();
        try {
            d.checkValue("a", Dataset.DesiredType.DATASET, new String("abc"));
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected nested "
                    + "dataset but found string value \"abc\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_checkValue_wantedChildGotChild() {
        Dataset d = new Dataset();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        Object result = d.checkValue("a", Dataset.DesiredType.DATASET, map);
        assertEquals("class of result", "Dataset",
                result.getClass().getSimpleName());
        assertEquals("contents of returned dataset", "Alice",
                ((Dataset) result).get("name"));
    }
    @SuppressWarnings("unchecked")
    public void test_checkValue_wantedDatasetGotList() {
        ArrayList<HashMap> list = new ArrayList<HashMap>();
        list.add(new HashMap());
        list.get(0).put("name", "Bob");
        list.add(new HashMap());
        list.get(1).put("name", "Alice");
        Dataset d = new Dataset();
        Object result = d.checkValue("a", Dataset.DesiredType.DATASET, list);
        assertEquals("class of result", "Dataset",
                result.getClass().getSimpleName());
        assertEquals("contents of returned dataset", "Bob",
                ((Dataset) result).get("name"));
    }
    public void test_checkValue_wantedListGotString() {
        boolean gotException = false;
        Dataset d = new Dataset();
        try {
            d.checkValue("a", Dataset.DesiredType.DATASETS, new String("abc"));
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected nested "
                    + "dataset but found string value \"abc\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_checkValue_wantedListGotChild() {
        Dataset d = new Dataset();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        Object result = d.checkValue("a", Dataset.DesiredType.DATASETS, map);
        assertEquals("class of result", "Dataset[]",
                result.getClass().getSimpleName());
        Dataset[] array = (Dataset[]) result;
        assertEquals("size of array", 1, array.length);
        assertEquals("contents of returned dataset", "Alice",
                array[0].get("name"));
    }
    @SuppressWarnings("unchecked")
    public void test_checkValue_WantedListGotList() {
        ArrayList<HashMap> list = new ArrayList<HashMap>();
        list.add(new HashMap());
        list.get(0).put("name", "Bob");
        list.add(new HashMap());
        list.get(1).put("name", "Alice");
        Dataset d = new Dataset();
        Object result = d.checkValue("a", Dataset.DesiredType.DATASETS, list);
        assertEquals("class of result", "Dataset[]",
                result.getClass().getSimpleName());
        Dataset[] array = (Dataset[]) result;
        assertEquals("size of array", 2, array.length);
        assertEquals("contents of first dataset", "Bob",
                array[0].get("name"));
        assertEquals("contents of second dataset", "Alice",
                array[1].get("name"));
    }
    public void test_checkValue_wantedAnythingGotString() {
        Dataset d = new Dataset();
        assertEquals("value", "abc", d.checkValue("a",
                Dataset.DesiredType.ANYTHING, new String("abc")));
    }
    public void test_checkValue_wantedAnythingGotChild() {
        Dataset d = new Dataset();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        Object result = d.checkValue("a", Dataset.DesiredType.ANYTHING, map);
        assertEquals("class of result", "Dataset",
                result.getClass().getSimpleName());
        assertEquals("contents of returned dataset", "Alice",
                ((Dataset) result).get("name"));
    }
    @SuppressWarnings("unchecked")
    public void test_checkValue_wantedAnythingGotList() {
        ArrayList<HashMap> list = new ArrayList<HashMap>();
        list.add(new HashMap());
        list.get(0).put("name", "Bob");
        list.add(new HashMap());
        list.get(1).put("name", "Alice");
        Dataset d = new Dataset();
        Object result = d.checkValue("a", Dataset.DesiredType.ANYTHING, list);
        assertEquals("class of result", "Dataset[]",
                result.getClass().getSimpleName());
        Dataset[] array = (Dataset[]) result;
        assertEquals("size of array", 2, array.length);
        assertEquals("contents of first dataset", "Bob",
                array[0].get("name"));
        assertEquals("contents of second dataset", "Alice",
                array[1].get("name"));
    }

    public void test_createChildInternal_childExists() {
        Dataset d = YamlDataset.newStringInstance("child:\n  name: Alice\n"
                + "  age: 24\n");
        assertEquals("child contents", "age: 24\n" +
                "name: Alice\n", d.createChildInternal(
                d.map, "child").toString());
    }
    public void test_createChildInternal_childExistsAsList() {
        Dataset d = YamlDataset.newStringInstance(
                "child:\n" +
                "  - name: Alice\n" +
                "    age:  24\n" +
                "  - name: Bill\n" +
                "  age: 22\n");
        assertEquals("child contents", "age: 24\n" +
                "name: Alice\n", d.createChildInternal(
                d.map, "child").toString());
    }
    public void test_createChildInternal_childExistsAsString() {
        Dataset d = YamlDataset.newStringInstance("child: value\n");
        d.createChildInternal(d.map, "child").set("id", "9924");
        assertEquals("dataset contents", "child:\n" +
                "    id: 9924\n", d.toString());
    }
    public void test_createChildInternal_childDoesntExist() {
        Dataset d = YamlDataset.newStringInstance("wife: Alice\n");
        d.createChildInternal(d.map, "child").set("id", "9924");
        assertEquals("dataset contents", "child:\n" +
                "    id: 9924\n" +
                "wife: Alice\n", d.toString());
    }

    public void test_cloneHelper() {
        Dataset d = YamlDataset.newStringInstance(
                "a: a_value\n" +
                "b: b_value\n" +
                "level1:\n" +
                "  level2:\n" +
                "    c: c_value\n" +
                "    d: d_value\n" +
                "list:\n" +
                "  - name: Alice\n" +
                "  - name: Bill\n" +
                "  - name: Carol\n" +
                "    age: 28\n");
        Dataset clone = d.clone();
        assertEquals("cloned dataset",
                "a: a_value\n" +
                "b: b_value\n" +
                "level1:\n" +
                "    level2:\n" +
                "        c: c_value\n" +
                "        d: d_value\n" +
                "list:\n" +
                "  - name: Alice\n" +
                "  - name: Bill\n" +
                "  - age: 28\n" +
                "    name: Carol\n", clone.toString());
    }

    public void test_lookupParent_noDotsInPath() {
        Dataset d = new Dataset("name", "value1");
        Dataset.ParentInfo info = d.lookupParent("xyz", false);
        assertEquals("last name", "xyz", info.lastName);
        assertEquals("contents of parent", "value1", info.parentMap.get("name"));
    }
    public void test_lookupParent_multipleNamesInPath() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2:\n" +
                "    level3:\n" +
                "      name: Alice\n");
        Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                false);
        assertEquals("last name", "foo", info.lastName);
        assertEquals("contents of parent", "Alice", info.parentMap.get("name"));
    }
    public void test_lookupParent_listInPath() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2:\n" +
                "    - name: Alice\n" +
                "    - name: Bob\n" +
                "    - name: Carol\n");
        Dataset.ParentInfo info = d.lookupParent("level1.level2.foo",
                true);
        assertEquals("last name", "foo", info.lastName);
        assertEquals("contents of parent", "Alice", info.parentMap.get("name"));
    }
    public void test_lookupParent_missingAncestors_noCreate() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  name: Alice\n");
        Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                false);
        assertEquals("return value", null, info);
    }
    public void test_lookupParent_stringInPath_noCreate() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2: string_value\n");
        Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                false);
        assertEquals("return value", null, info);
    }
    @SuppressWarnings("unchecked")
    public void test_lookupParent_missingAncestors_create() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  name: Alice\n");
        Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                true);
        assertEquals("last name", "foo", info.lastName);

        // Add a unique value to the parent map, then check that this value
        // appears in the right place in the overall data set.
        info.parentMap.put("value", "12345");
        assertEquals("overall dataset contents", "level1:\n" +
                "    level2:\n" +
                "        level3:\n" +
                "            value: 12345\n" +
                "    name: Alice\n", d.toString());
    }
    @SuppressWarnings("unchecked")
    public void test_lookupParent_stringInPath_create() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2: Alice\n");
        Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                true);
        assertEquals("last name", "foo", info.lastName);

        // Add a unique value to the parent map, then check that this value
        // appears in the right place in the overall data set.
        info.parentMap.put("value", "12345");
        assertEquals("overall dataset contents", "level1:\n" +
                "    level2:\n" +
                "        level3:\n" +
                "            value: 12345\n", d.toString());
    }

    public void test_wrongTypeMessage_nested() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "string value but found nested dataset",
                Dataset.wrongTypeMessage("key_name",
                Dataset.DesiredType.STRING, new HashMap()));
    }
    public void test_wrongTypeMessage_list() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "nested dataset but found list",
                Dataset.wrongTypeMessage("key_name",
                Dataset.DesiredType.DATASET, new ArrayList()));
    }
    public void test_wrongTypeMessage_string() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "string value but found string value \"472\"",
                Dataset.wrongTypeMessage("key_name",
                Dataset.DesiredType.STRING, new Integer(472)));
    }
    public void test_wrongTypeMessage_stringButLong() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "string value but found string value \"test 01234567890 ...\"",
                Dataset.wrongTypeMessage("key_name",
                Dataset.DesiredType.STRING,
                "test 01234567890 test2 01234567890"));
    }
}