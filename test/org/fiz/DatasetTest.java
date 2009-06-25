package org.fiz;

import java.io.*;
import java.util.*;

import org.fiz.test.*;

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

    public void test_UnknownFileFormatError() {
        Error e = new Dataset.UnknownFileFormatError("simple message");
        assertEquals("simple message", e.getMessage());
    }

    // Constructor Dataset(String... keysAndValues)

    public void test_constructor_keysAndValues_basics() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("dataset contents", "a: a_value\n" +
                "b: b_value\n", d.toString());
    }
    public void test_constructor_keysAndValues_ignoreExtraKey() {
        Dataset d = new Dataset("a", "a_value", "b");
        assertEquals("dataset contents", "a: a_value\n", d.toString());
    }

    // Constructor Dataset(Object... keysAndValues)

    public void test_constructor_keysAndValuesObjects_basics() {
        Dataset d = new Dataset("a", "123", "b", "abcde",
                "b", new Dataset("name", "Alice", "age", "23"),
                "b", new Dataset("child", new Dataset("name", "Bill")),
                "c", new Dataset("state", "California"));
        assertEquals("dataset contents", "a: 123\n" +
                "b:\n" +
                "  - age:  23\n" +
                "    name: Alice\n" +
                "  - child:\n" +
                "        name: Bill\n" +
                "c:\n" +
                "    state: California\n", d.toString());
    }
    public void test_constructor_keysAndValuesObjects_extraKey() {
        Dataset d = new Dataset("a", new Dataset("x", "1"), "b");
        assertEquals("dataset contents", "a:\n" +
                "    x: 1\n", d.toString());
    }
    public void test_constructor_keysAndValuesObjects_typeError() {
        boolean gotException = false;
        try {
            Dataset d = new Dataset("a", new int[] {1, 2, 3});
        }
        catch (ClassCastException e) {
            assertEquals("exception message",
                    "[I cannot be cast to org.fiz.Dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    // Constructor Dataset(HashMap contents, String fileName)

    @SuppressWarnings("unchecked")
    public void test_constructor_hashMapAndString() {
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
                new String[] {"_test1_"}, Dataset.Quantity.ALL);
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
                Dataset.Quantity.FIRST_ONLY);
        assertEquals("class name for result", "YamlDataset",
                d.getClass().getSimpleName());
        assertEquals("first value should exist", "24", d.get("first"));
        assertEquals("third value shouldn't exist", null, d.check("third"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_newFileInstanceFromPath_compound() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        (new File("_test1_/child2")).mkdir();
        TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        TestUtil.writeFile("_test1_/child2/test.yaml",
                "first: 99\nfourth: 777\n");
        Dataset d = Dataset.newFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child", "_test1_/child2"},
                Dataset.Quantity.ALL);
        assertEquals("class name for result", "CompoundDataset",
                d.getClass().getSimpleName());
        assertEquals("value from first dataset", "24", d.get("first"));
        assertEquals("value from second dataset", "value", d.check("third"));
        assertEquals("value from last dataset", "777", d.check("fourth"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_newFileInstanceFromPath_FileNotFoundError() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        Dataset d = Dataset.newFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child"},
                Dataset.Quantity.ALL);
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
                    Dataset.Quantity.FIRST_ONLY);
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
    public void test_newFileInstanceFromPath_dontReturnCompoundForOneDataset() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
        Dataset d = Dataset.newFileInstanceFromPath("test",
                new String[] {"_test1_", "_test1_/child"},
                Dataset.Quantity.ALL);
        assertEquals("class name for result", "YamlDataset",
                d.getClass().getSimpleName());
        TestUtil.deleteTree("_test1_");
    }

    public void test_newSerializedInstance() {
        Dataset d = new Dataset("name", "Alice", "age", "64",
                "child", new Dataset("name", "Bob"));
        String s = d.serialize();
        Dataset out = Dataset.newSerializedInstance(s);
        assertEquals("dataset contents", "age:  64\n" +
                "child:\n" +
                "    name: Bob\n" +
                "name: Alice\n", out.toString());
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

    public void test_addSerializedData_missingOpenParen() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            out.addSerializedData("xyz", 0);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: serialized dataset didn't " +
                    "start with \"(\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_addSerializedData_pastEndOfString() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            out.addSerializedData("(((", 3);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: serialized dataset didn't " +
                    "start with \"(\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_addSerializedData_emptyInput() {
        Dataset out = new Dataset("weight", "125");
        assertEquals("return value", 2, out.addSerializedData("()", 0));
        assertEquals("contents of dataset", "weight: 125\n", out.toString());
    }
    public void test_addSerializedData_missingValue() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            out.addSerializedData("(4.name", 0);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: no value for element " +
                    "\"name\" in serialized dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_addSerializedData_singleNestedDataset() {
        String source = "(3.age2.24\n" +
                "5.child(4.name5.Alice\n" +
                "3.age3.100)\n" +
                "9.eye color5.brown)";
        Dataset out = new Dataset();
        out.addSerializedData(source, 0);
        assertEquals("contents of dataset",
                "age:       24\n" +
                "child:\n" +
                "    age:  100\n" +
                "    name: Alice\n" +
                "eye color: brown\n", out.toString());
    }
    public void test_addSerializedData_parensInValue() {
        String source = "(3.age2.24\n" +
                "5.child16.(4.name5.Alice\n" +
                "))";
        Dataset out = new Dataset();
        out.addSerializedData(source, 0);
        assertEquals("contents of dataset",
                "age:   24\n" +
                "child: \"(4.name5.Alice\\n)\"\n", out.toString());
    }
    public void test_addSerializedData_dataEndsInChildren() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            out.addSerializedData("(5.child(1.a2.xx)", 0);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: serialized dataset not " +
                    "terminated by \")\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_addSerializedData_missingCloseParen() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            out.addSerializedData("(4.name5.Alice", 0);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: serialized dataset not " +
                    "terminated by \")\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_addSerializedData_noStartArgument() {
        Dataset out = new Dataset("weight", "125");
        out.addSerializedData("()");
        assertEquals("contents of dataset", "weight: 125\n", out.toString());
    }

    public void test_check() {
        Dataset d = YamlDataset.newStringInstance(
                "a: a_value\nb: b_value\nnested:\n  x: x_value\n");
        assertEquals("value from dataset", "a_value", d.check("a"));
        assertEquals("undefined key", null, d.check("undefined"));
        assertEquals("wrong type", null, d.check("nested"));
    }

    public void test_checkChild() {
        Dataset d = YamlDataset.newStringInstance(
                "a: a_value\nb: b_value\nnested:\n  - x: x_value\n" +
                "  - y: y_value\n");
        assertEquals("value is string", null, d.checkChild("a"));
        assertEquals("undefined key", null, d.checkChild("undefined"));
        assertEquals("contents of nested dataset",
                "x: x_value\n", d.checkChild("nested").toString());
    }

    public void test_clear() {
        Dataset d = YamlDataset.newStringInstance(
                "a: a_value\nb: b_value\nnested:\n  x: x_value\n");
        d.clear();
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
        d.getChildren("list").get(2).set("age", "444");
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
                "  - age:  28\n" +
                "    name: Carol\n", clone.toString());
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
        assertEquals("first value exists", true, d.containsKey("first"));
        assertEquals("second value exists", true, d.containsKey("second"));
        assertEquals("third value doesn't exist", false,
                d.containsKey("third"));
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
                "    age:  24\n" +
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
        assertEquals("child contents", "age:  6\n" +
                "id:   9924\n" +
                "name: Carol\n", d2.toString());
        assertEquals("dataset contents", "child:\n" +
                "    age:  6\n" +
                "    id:   9924\n" +
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
        assertEquals("child contents", "age:  6\n" +
                "id:   9924\n" +
                "name: Carol\n", d2.toString());
        assertEquals("dataset contents", "level1:\n" +
                "    level2:\n" +
                "        age:  6\n" +
                "        id:   9924\n" +
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
                "        first:  1\n" +
                "        second: 2\n", d.toString());
    }

    public void test_expand_simpleValues() {
        Dataset input = new Dataset("first", "first",
                "second", "ouster@electric-cloud",
                "third", "third@@");
        Dataset result = input.expand(new Dataset("name", "Alice"));
        assertEquals("result dataset", "first:  first\n" +
                "second: \"ouster@electric-cloud\"\n" +
                "third:  \"third@@\"\n",
                result.toString());
    }
    public void test_expand_doubleAtSign() {
        Dataset input = new Dataset("first", "@@x1",
                "second", "@@first");
        Dataset result = input.expand(new Dataset("name", "Alice"));
        assertEquals("result dataset", "first:  \"@x1\"\n" +
                "second: \"@first\"\n",
                result.toString());
    }
    public void test_expand_atSignSubstitution() {
        Dataset input = new Dataset("first", "@name",
                "second", "@sibling.age");
        Dataset result = input.expand(new Dataset("name", "Alice",
                "sibling", new Dataset("age", "18")));
        assertEquals("result dataset", "first:  Alice\n" +
                "second: 18\n",
                result.toString());
    }
    public void test_expand_nestedDatasets() {
        Dataset input = YamlDataset.newStringInstance(
                "children:\n" +
                "  - name: @name\n" +
                "    age:  22\n" +
                "  - name: Bill\n" +
                "    age:  @age\n");
        Dataset result = input.expand(new Dataset("name", "Alice",
                "age", "18"));
        assertEquals("result dataset", "children:\n" +
                "  - age:  22\n" +
                "    name: Alice\n" +
                "  - age:  18\n" +
                "    name: Bill\n",
                result.toString());
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
        ArrayList<Dataset> children = d.getChildren("children");
        assertEquals("number of children", 2, children.size());
        assertEquals("name of second child", "Bob",
                children.get(1).get("name"));
    }
    public void test_getChildren_noSuchKey() {
        Dataset d = new Dataset("a", "a_value");
        ArrayList<Dataset>children = d.getChildren("nonexistent");
        assertEquals("number of children", 0, children.size());
    }
    public void test_getChildren_valueIsString() {
        Dataset d = new Dataset("a", "a_value");
        ArrayList<Dataset> children = d.getChildren("a");
        assertEquals("number of children", 0, children.size());
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
        ArrayList<Dataset> kids = d.getChildrenPath("level0.level1.level2.level3");
        assertEquals("member of datasets", 3, kids.size());
        assertEquals("value from first descendent", "1",
                kids.get(0).get("value"));
        assertEquals("value from second descendent", "2",
                kids.get(1).get("value"));
    }
    public void test_getChildrenPath_noSuchDescendents() {
        Dataset d = YamlDataset.newStringInstance(
                "level0:\n" +
                "  level1:\n");
        ArrayList<Dataset> kids = d.getChildrenPath("level0.level1.level2.level3");
        assertEquals("number of datasets", 0, kids.size());
    }

    public void test_getFileName() {
        Dataset d = new Dataset("name", "Alice");
        d.fileName = "sample file name";
        assertEquals("getFileName result", "sample file name",
                d.getFileName());
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

    public void test_lookup() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("existing value", "b_value", d.lookup("b",
                Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY));
        assertEquals("nonexistent value", null, d.lookup("bogus",
                Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY));
    }

	public void test_lookup_withOneArgument() {
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("existing value", "b_value", d.lookup("b"));
        assertEquals("nonexistent value", null, d.lookup("bogus"));
	}

    public void test_lookup_withExtraArgument() {
        ArrayList<Object> out = new ArrayList<Object>();
        out.add("First value");
        Dataset d = new Dataset("a", "a_value", "b", "b_value");
        assertEquals("return value", out, d.lookup("b",
                Dataset.DesiredType.STRING, Dataset.Quantity.ALL,
                out));
        assertEquals("contents of ArrayList", "First value, b_value",
                StringUtil.join(out, ", "));
        assertEquals("null return value", null, d.lookup("bogus",
                Dataset.DesiredType.STRING, Dataset.Quantity.ALL));
        assertEquals("contents of ArrayList", "First value, b_value",
                StringUtil.join(out, ", "));
    }

    public void test_lookupPath() {
        Dataset d = YamlDataset.newStringInstance(
                "children:\n" +
                "  - name: Bob\n" +
                "  - name: Alice\n");
        Object result = d.lookupPath("children.name",
                Dataset.DesiredType.ANY, Dataset.Quantity.ALL);
        assertEquals("result class", "ArrayList",
                result.getClass().getSimpleName());
        assertEquals("result value", "Bob, Alice",
                StringUtil.join((ArrayList) result, ", "));
    }

    public void test_lookupPath_withOneArgument() {
        Dataset d = YamlDataset.newStringInstance(
                "children:\n" +
                "  - name: Bob\n" +
                "  - name: Alice\n");
        Object result = d.lookupPath("children.name");
        assertEquals("result class", "String",
                result.getClass().getSimpleName());
        assertEquals("result value", "Bob", result);
    }

    public void test_lookupPath_withExtraArgument() {
        ArrayList<Object> out = new ArrayList<Object>();
        out.add("First value");
        Dataset d = YamlDataset.newStringInstance(
                "children:\n" +
                "  - name: Bob\n" +
                "  - name: Alice\n");
        assertEquals("return value", out, d.lookupPath("children.name",
                Dataset.DesiredType.ANY, Dataset.Quantity.ALL, out));
        assertEquals("result value", "First value, Bob, Alice",
                StringUtil.join(out, ", "));
    }

    public void test_serialize_withOut() {
        StringBuilder out = new StringBuilder("abc");
        Dataset d = new Dataset("first", "12345");
        d.serialize(out);
        assertEquals("result", "abc(5.first5.12345)", out.toString());
    }

    public void test_serialize_noArgs() {
        Dataset d = new Dataset("first", "12345", "second", "x");
        assertEquals("result", "(6.second1.x\n" +
                "5.first5.12345)", d.serialize());
    }

    public void test_set() {
        Dataset d = new Dataset("first", "12345");
        d.set("second", "66");
        assertEquals("dataset contents", "first:  12345\n" +
                "second: 66\n", d.toString());
    }

    public void test_toJavascript() {
        StringBuilder out = new StringBuilder("var obj = ");
        Dataset d = new Dataset("name", "Alice", "age", "32");
        d.toJavascript(out);
        assertEquals("generated Javascript",
                "var obj = {age: \"32\", name: \"Alice\"}",
                out.toString());
    }

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
                "  - age:  28\n" +
                "    name: Carol\n", clone.toString());
    }

    public void test_collectResults_gotStringWantedDataset() {
        Dataset d = new Dataset();
        assertEquals(null, d.collectResults("California",
                Dataset.DesiredType.DATASET, Dataset.Quantity.FIRST_ONLY,
                null));
    }
    public void test_collectResults_gotStringWantedFirstString() {
        Dataset d = new Dataset();
        assertEquals("California", d.collectResults("California",
                Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY,
                null));
    }
    public void test_collectResults_gotStringWantedAllStrings_createList() {
        Dataset d = new Dataset();
        Object out = d.collectResults("California",
                Dataset.DesiredType.STRING, Dataset.Quantity.ALL,
                null);
        assertEquals("type of result", "ArrayList",
                out.getClass().getSimpleName());
        assertEquals("return value", "California", StringUtil.join(
                (ArrayList) out, ", "));
    }
    public void test_collectResults_gotStringWantedAllAny_useExistingList() {
        Dataset d = new Dataset();
        ArrayList<Object> out = new ArrayList<Object>();
        out.add("Connecticut");
        assertEquals("use existing ArrayList", out,
                d.collectResults("California", Dataset.DesiredType.ANY,
                Dataset.Quantity.ALL, out));
        assertEquals("return value", "Connecticut, California",
                StringUtil.join(out, ", "));
    }
    public void test_collectResults_gotHashMapWantedFirstString() {
        Dataset d = new Dataset();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        assertEquals(null, d.collectResults(map,
                Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY,
                null));
    }
    public void test_collectResults_gotArrayListWantedFirstDataset() {
        Dataset d = new Dataset();
        HashMap<String,String> map1 = new HashMap<String,String>();
        map1.put("name", "Alice");
        HashMap<String,String> map2 = new HashMap<String,String>();
        map2.put("name", "Bob");
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(map1);
        list.add(map2);
        Object out = d.collectResults(list, Dataset.DesiredType.DATASET,
                Dataset.Quantity.FIRST_ONLY, null);
        assertEquals("type of result", "Dataset",
                out.getClass().getSimpleName());
        assertEquals("contents of result", "name: Alice\n",
                out.toString());
    }
    public void test_collectResults_gotHashMapWantedFirstDataset() {
        Dataset d = new Dataset();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        Object out = d.collectResults(map, Dataset.DesiredType.DATASET,
                Dataset.Quantity.FIRST_ONLY, null);
        assertEquals("type of result", "Dataset",
                out.getClass().getSimpleName());
        assertEquals("contents of result", "name: Alice\n",
                out.toString());
    }
    public void test_collectResults_gotHashMapWantedAllDatasets_createList() {
        Dataset d = new Dataset();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        Object out = d.collectResults(map, Dataset.DesiredType.DATASET,
                Dataset.Quantity.ALL, null);
        assertEquals("type of result", "ArrayList",
                out.getClass().getSimpleName());
        assertEquals("contents of result", "name: Alice\n",
                StringUtil.join((ArrayList) out, "---\n"));
    }
    public void test_collectResults_gotHashMapWantedAllDatasets_useExistingList() {
        Dataset d = new Dataset();
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("name", "Alice");
        ArrayList<Object> out = new ArrayList<Object>();
        out.add("Connecticut");
        assertEquals("use existing ArrayList", out,
                d.collectResults(map, Dataset.DesiredType.DATASET,
                Dataset.Quantity.ALL, out));
        assertEquals("return value", "Connecticut, name: Alice\n",
                StringUtil.join(out, ", "));
    }
    public void test_collectResults_gotArrayListWantedAllAny() {
        Dataset d = new Dataset();
        HashMap<String,String> map1 = new HashMap<String,String>();
        map1.put("name", "Alice");
        HashMap<String,String> map2 = new HashMap<String,String>();
        map2.put("name", "Bob");
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(map1);
        list.add(map2);
        Object out = d.collectResults(list, Dataset.DesiredType.ANY,
                Dataset.Quantity.ALL, null);
        assertEquals("type of result", "ArrayList",
                out.getClass().getSimpleName());
        assertEquals("contents of result", "name: Alice\n" +
                "---\n" +
                "name: Bob\n",
                StringUtil.join((ArrayList) out, "---\n"));
    }

    public void test_createChildInternal_childExists() {
        Dataset d = YamlDataset.newStringInstance("child:\n  name: Alice\n"
                + "  age: 24\n");
        assertEquals("child contents", "age:  24\n" +
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
        assertEquals("child contents", "age:  24\n" +
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

    public void test_getEncodedString_missingDot() {
        boolean gotException = false;
        try {
            IntBox end = new IntBox();
            Dataset.getEncodedString("x.144", 2, end);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: missing \".\" in " +
                    "serialized dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getEncodedString_computeLength() {
        IntBox end = new IntBox();
        String s = Dataset.getEncodedString("23.aaaaabbbbbcccccdddddeeeeefffff",
                0, end);
        assertEquals("end index", 26, end.value);
        assertEquals("string value", "aaaaabbbbbcccccdddddeee", s);
    }
    public void test_getEncodedString_negativeLength() {
        boolean gotException = false;
        try {
            IntBox result = new IntBox();
            Dataset.getEncodedString("19999999999.", 0, result);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: serialized dataset has " +
                    "improper length \"19999999999\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getEncodedString_lengthTooBig() {
        boolean gotException = false;
        try {
            IntBox end = new IntBox();
            Dataset.getEncodedString("6.12345", 0, end);
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset: unexpected end of " +
                    "serialized dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_javascriptForSubtree_emptyDataset() {
        StringBuilder out = new StringBuilder("var obj = ");
        Dataset d = new Dataset();
        d.toJavascript(out);
        assertEquals("generated Javascript", "var obj = {}", out.toString());
    }
    public void test_javascriptForSubtree_nestedDatasets() {
        StringBuilder out = new StringBuilder();
        Dataset d = YamlDataset.newStringInstance("child:\n" +
                "  name: Alice\n" +
                "  child:\n" +
                "    name: Bill\n" +
                "    age: 16\n");
        d.toJavascript(out);
        assertEquals("generated Javascript",
                "{child: {child: {age: \"16\", name: \"Bill\"}, " +
                "name: \"Alice\"}}",
                out.toString());
    }
    public void test_javascriptForSubtree_listOfChildren() {
        StringBuilder out = new StringBuilder();
        Dataset d = YamlDataset.newStringInstance("child:\n" +
                "  - name: Alice\n" +
                "  - name: Bill\n" +
                "  - name: Carol\n" +
                "    age: 16\n");
        d.toJavascript(out);
        assertEquals("generated Javascript",
                "{child: [{name: \"Alice\"}, {name: \"Bill\"}, " +
                "{age: \"16\", name: \"Carol\"}]}",
                out.toString());
    }
    public void test_javascriptForSubtree_quoteStringCharacters() {
        StringBuilder out = new StringBuilder();
        Dataset d = new Dataset("value", "xyz&<\n\"\'\0");
        d.toJavascript(out);
        assertEquals("generated Javascript",
                "{value: \"xyz&<\\n\\\"'\\x00\"}",
                out.toString());
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
        // appears in the right place in the overall dataset.
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
        // appears in the right place in the overall dataset.
        info.parentMap.put("value", "12345");
        assertEquals("overall dataset contents", "level1:\n" +
                "    level2:\n" +
                "        level3:\n" +
                "            value: 12345\n", d.toString());
    }

    public void test_lookupPathHelper_sanityCheck() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  - level2: Alice\n" +
                "  - level2: Bill\n");
        ArrayList<Object> results = new ArrayList<Object>();
        d.lookupPathHelper("a.level1.level2", 2, d.map,
                Dataset.DesiredType.ANY, Dataset.Quantity.ALL, results);
        assertEquals("string value", "Alice, Bill",
                StringUtil.join(results, ", "));
    }
    public void test_lookupPathHelper_noSuchPath() {
        Dataset d = YamlDataset.newStringInstance(
                "city: San Francisco\n");
        assertEquals("result is null", null, d.lookupPathHelper("a.state", 2, d.map,
                Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY,
                null));
    }
    public void test_lookupPathHelper_lastElementInPath() {
        Dataset d = YamlDataset.newStringInstance(
                "city: San Francisco\n");
        ArrayList<Object> results = new ArrayList<Object>();
        Object out = d.lookupPathHelper("a.city", 2, d.map,
                Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY,
                null);
        assertEquals("type of result", "String",
                out.getClass().getSimpleName());
        assertEquals("result value", "San Francisco", (String) out);
    }
    public void test_lookupPathHelper_nextObjectIsHashMap() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  level2: Alice\n");
        assertEquals("string value", "Alice",
                d.lookupPathHelper("level1.level2", 0, d.map,
                Dataset.DesiredType.ANY, Dataset.Quantity.FIRST_ONLY, null));
    }
    public void test_lookupPathHelper_nextObjectIsArrayList_firstOnly() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  - level2: Alice\n" +
                "  - level2: Bob\n" +
                "  - level2: Carol\n");
        Object out = d.lookupPathHelper("level1.level2", 0, d.map,
                Dataset.DesiredType.ANY, Dataset.Quantity.FIRST_ONLY, null);
        assertEquals("type of result", "String",
                out.getClass().getSimpleName());
        assertEquals("string value", "Alice", (String) out);
    }
    public void test_lookupPathHelper_nextObjectIsArrayList_collectMany() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  - level2: Alice\n" +
                "  - level2: Bob\n" +
                "  - level2: Carol\n" +
                "  - state: California\n" +
                "    level2:\n" +
                "      age: 48\n" +
                "      weight:182\n");
        Object out = d.lookupPathHelper("level1.level2", 0, d.map,
                Dataset.DesiredType.ANY, Dataset.Quantity.ALL, null);
        assertEquals("type of result", "ArrayList",
                out.getClass().getSimpleName());
        assertEquals("string value", "Alice, Bob, Carol, age:    48\n" +
                "weight: 182\n",
                StringUtil.join((ArrayList) out, ", "));
    }
    public void test_lookupPathHelper_nextObjectIsArrayList_useExistingList() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  - level2: Alice\n" +
                "  - level2: Bob\n" +
                "  - level2: Carol\n");
        ArrayList<Object> out = new ArrayList<Object>();
        out.add("Connecticut");
        d.lookupPathHelper("level1.level2", 0, d.map,
                Dataset.DesiredType.ANY, Dataset.Quantity.ALL, out);
        assertEquals("string value", "Connecticut, Alice, Bob, Carol",
                StringUtil.join((ArrayList) out, ", "));
    }
    public void test_lookupPathHelper_nextObjectIsArrayList_foundNothing() {
        Dataset d = YamlDataset.newStringInstance(
                "level1:\n" +
                "  - level2: Alice\n" +
                "  - level2: Bob\n" +
                "  - level2: Carol\n");
        assertEquals("result is null", null,
                d.lookupPathHelper("level1.bogus", 0, d.map,
                Dataset.DesiredType.ANY, Dataset.Quantity.ALL, null));
    }
    public void test_lookupPathHelper_nextObjectIsString() {
        Dataset d = YamlDataset.newStringInstance(
                "level1: value\n");
        assertEquals("result is null", null,
                d.lookupPathHelper("level1.bogus", 0, d.map,
                Dataset.DesiredType.ANY, Dataset.Quantity.ALL, null));
    }

    public void test_serializeSubtree_basics() {
        Dataset d = new Dataset("name", "Alice", "age", "36",
                "child", new Dataset("name", "Bill"));
        assertEquals("serialized result", "(5.child(4.name4.Bill)\n" +
                "3.age2.36\n" +
                "4.name5.Alice)", d.serialize());
    }
    public void test_serializeSubtree_listOfChildren() {
        Dataset d = new Dataset("child", new Dataset("name", "Bill"));
        d.addChild("child", new Dataset("name", "Carol"));
        d.addChild("child", new Dataset("name", "David"));
        assertEquals("serialized result",
                "(5.child(4.name4.Bill)(4.name5.Carol)(4.name5.David))",
                d.serialize());
    }
    public void test_serializeSubtree_emptyDataset() {
        Dataset d = new Dataset();
        assertEquals("serialized result", "()",
                d.serialize());
    }
}
