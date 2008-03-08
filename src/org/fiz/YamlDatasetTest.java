/**
 * Junit tests for the YamlDataset class.
 */

package org.fiz;
import java.util.HashMap;
import java.util.ArrayList;

public class YamlDatasetTest extends junit.framework.TestCase {

    public void test_UnnamedValueError() {
        Error e = new YamlDataset.UnnamedValueError("my_file");
        assertEquals("exception message",
                "YAML dataset contains sequence(s) with unnamed values "
                + "(file \"my_file\")", e.getMessage());
    }

    // Constructor YamlDataset(String s)

    public void test_YamlDataset_String() {
        Dataset d = new YamlDataset("first: 123\nsecond: Simple test\n"
                + "third:\n  name: Bill\n  age: 27\n");
        assertEquals("check value", "Simple test", d.get("second"));
        assertEquals("check value", "123", d.getPath("first"));
        assertEquals("number of values", 3, d.size());
    }
    public void test_YamlDataset_String_unnamedValueError() {
        boolean gotException = false;
        try {
            Dataset d = new YamlDataset("- a\n- b\n");
        }
        catch (YamlDataset.UnnamedValueError e) {
            assertEquals("exception message",
                    "YAML dataset contains sequence(s) with unnamed values",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_YamlDataset_String_syntaxError() {
        boolean gotException = false;
        try {
            Dataset d = new YamlDataset("- a\n - b\n");
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset input: Error near line 2: "
                    + "End of document expected.",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_YamlDataset_Object_unnamedValueError() {
        boolean gotException = false;
        TestUtil.writeFile("test.yml", "- a\n- b\n");
        try {
            Dataset d = YamlDataset.getFileInstance("test.yml");
        }
        catch (YamlDataset.UnnamedValueError e) {
            assertEquals("exception message",
                    "YAML dataset contains sequence(s) with unnamed values "
                    + "(file \"test.yml\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteFile("test.yml");
    }

    public void test_getFileInstance() {
        TestUtil.writeFile("test.yml", "first: abc\nsecond: 111\n");
        Dataset d = YamlDataset.getFileInstance("test.yml");
        assertEquals("check value", "abc", d.get("first"));
        assertEquals("check second value", "111", d.get("second"));
        TestUtil.deleteFile("test.yml");
    }
    public void test_getFileInstance_bogusFileName() {
        boolean gotException = false;
        try {
            Dataset d = YamlDataset.getFileInstance("bogus_44.yml");
        }
        catch (Dataset.FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't read dataset file bogus_44.yml " +
                    "(The system cannot find the file specified)",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getFileInstance_syntaxError() {
        boolean gotException = false;
        TestUtil.writeFile("test.yml", "- a\n - b\n");
        try {
            Dataset d = YamlDataset.getFileInstance("test.yml");
        }
        catch (Dataset.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in dataset input file \"test.yml\": "
                    + "Error near line 2: End of document expected.",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        TestUtil.deleteFile("test.yml");
    }

    public void test_containsKey() {
        Dataset d = new YamlDataset("first: 123\nsecond: Simple test\n");
        assertEquals("first value exists", true, d.containsKey("first"));
        assertEquals("second value exists", true, d.containsKey("second"));
        assertEquals("third value doesn't exist", false, d.containsKey("third"));
    }

    public void test_getPath_noDotsInPath() {
        YamlDataset d = new YamlDataset("first: 12345\n");
        assertEquals("value", "12345", d.getPath("first"));
    }
    public void test_getPath_missingKey() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("first: 12345\n");
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
    public void test_getPath_useFirstChildInArrayList() {
        YamlDataset d = new YamlDataset("people:\n"
                + "  - name: Alice\n    age: 19\n"
                + "  - name: Bob\n    age: 46\n"
                + "  - name: Carol\n    age: 32\n");
        assertEquals("value", "Alice", d.getPath("people.name"));
    }
    public void test_getPath_arrayListValuesNotHashMaps() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("people:\n"
                + "  - first\n  - second\n  - third\n");
        try {
            d.getPath("people.first");
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"people\": expected "
                    + "nested dataset but found list",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getPath_nestedDatasets() {
        YamlDataset d = new YamlDataset("a:\n  b:\n    c:\n"
                + "      name: Alice\n      age: 19\n"
                + "    valueInB: 99\n  valueInA: 101\n");
        assertEquals("value", "19", d.getPath("a.b.c.age"));
        assertEquals("value", "99", d.getPath("a.b.valueInB"));
        assertEquals("value", "101", d.getPath("a.valueInA"));
    }
    public void test_getPath_wrongTypeForNode() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("a: 44\n");
        try {
            d.getPath("a.b");
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a\": expected nested "
                    + "dataset but found string value \"44\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getPath_wrongTypeForValue() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("a:\n  b:\n    c:\n      value: 88\n");
        try {
            d.getPath("a.b");
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a.b\": expected string "
                    + "value but found nested dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_lookup_missingValue() {
        YamlDataset d = new YamlDataset("a: 44\n");
        assertEquals("value", null, d.lookup("b",
                Dataset.DesiredType.STRING));
    }
    public void test_lookup_callCheckValue() {
        YamlDataset d = new YamlDataset("a:\n  child1: 44\n  child2: 55\n");
        Object result = d.lookup("a", Dataset.DesiredType.DATASET);
        assertEquals("class of result", "YamlDataset",
                result.getClass().getSimpleName());
        assertEquals("contents of returned dataset", "55",
                ((YamlDataset) result).get("child2"));
    }

    public void test_lookupPath_noDotsInPath() {
        YamlDataset d = new YamlDataset("first: 12345\n");
        Object result = d.lookupPath("first", Dataset.DesiredType.STRING);
        assertEquals("class of result", "String",
                result.getClass().getSimpleName());
        assertEquals("value of result", "12345", (String) result);
    }
    public void test_lookupPath_missingKey() {
        YamlDataset d = new YamlDataset("first: 12345\n");
        Object result = d.lookupPath("second", Dataset.DesiredType.STRING);
        assertEquals("null result", null, result);
    }
    public void test_lookupPath_useFirstChildInArrayList() {
        YamlDataset d = new YamlDataset("people:\n"
                + "  - name: Alice\n    age: 19\n"
                + "  - name: Bob\n    age: 46\n"
                + "  - name: Carol\n    age: 32\n");
        Object result = d.lookupPath("people.name",
                Dataset.DesiredType.STRING);
        assertEquals("class of result", "java.lang.String",
                result.getClass().getName());
        assertEquals("value of result", "Alice", (String) result);
    }
    public void test_lookupPath_arrayListValuesNotHashMaps() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("people:\n"
                + "  - first\n  - second\n  - third\n");
        try {
            d.lookupPath("people.first", Dataset.DesiredType.STRING);
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"people\": expected "
                    + "nested dataset but found list",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_lookupPath_nestedDatasets() {
        YamlDataset d = new YamlDataset("a:\n  b:\n    c:\n"
                + "      name: Alice\n      age: 19\n"
                + "    valueInB: 99\n  valueInA: 101\n");
        assertEquals("value", "19", d.lookupPath("a.b.c.age",
                Dataset.DesiredType.STRING));
        assertEquals("value", "99", d.lookupPath("a.b.valueInB",
                Dataset.DesiredType.STRING));
        assertEquals("value", "101", d.lookupPath("a.valueInA",
                Dataset.DesiredType.STRING));
    }
    public void test_lookupPath_wrongTypeForNode() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("a:\n  b: 35\n");
        try {
            d.lookupPath("a.b.c", Dataset.DesiredType.STRING);
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a.b\": expected nested "
                    + "dataset but found string value \"35\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_lookupPath_callCheckValue() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("a:\n  b:\n    c:\n      value: 88\n");
        try {
            d.lookupPath("a.b", Dataset.DesiredType.STRING);
        }
        catch (Dataset.WrongTypeError e) {
            assertEquals("exception message",
                    "wrong type for dataset element \"a.b\": expected string "
                    + "value but found nested dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_lookupPath_callViaGetPath() {
        YamlDataset d = new YamlDataset("a:\n  b:\n    c:\n"
                + "      name: Alice\n      age: 19\n"
                + "    valueInB: 99\n  valueInA: 101\n");
        assertEquals("value", "Alice", d.getPath("a.b.c.name"));
    }

    public void test_size() {
        YamlDataset d = new YamlDataset("first: 123\nsecond: Test string\n"
                + "person:\n  name: Alice\n  age: 27\n"
                + "person2:\n  name: Bob\n  age: 12\n");
        assertEquals("data set size", 4, d.size());
    }

    public void test_checkAndConvert_nestedDatasets() {
        YamlDataset y = new YamlDataset("a:\n  value: 41\n"
                + "  b:\n    value: 123.456\n    c:\n      value: 99\n");
        assertEquals("first-level value", "41", y.getPath("a.value"));
        assertEquals("second-level value", "123.456", y.getPath("a.b.value"));
        assertEquals("third-level value", "99", y.getPath("a.b.c.value"));
    }
    public void test_checkAndConvert_nestedLists() {
        YamlDataset y = new YamlDataset("a:\n  people:\n"
                + "    - name: Alice\n      age: 32\n"
                + "    - name: Bill\n      age: 44\n"
                + "    - name: Carol\n      age: 50\n"
                + "data:\n - first\n - second\n - third\n");
        assertEquals("a.people[0].age", "32", y.getPath("a.people.age"));
        assertEquals("a.people[1].age", "44",
                y.getChild("a").getChildren("people")[1].get("age"));
        assertEquals("a.people[2].age", "50",
                y.getChild("a").getChildren("people")[2].get("age"));
    }
    public void test_checkAndConvert_convertValues() {
        YamlDataset y = new YamlDataset("first: 123\nsecond: 17.3\n"
                + "third: 18.1234567890123\nfourth: true\n");
        assertEquals("first value", "123", y.get("first"));
        assertEquals("read first value again", "123", y.get("first"));
        assertEquals("second value", "17.3", y.get("second"));
        assertEquals("third value", "18.1234567890123", y.get("third"));
        assertEquals("fourth value", "true", y.get("fourth"));
    }

    public void test_checkValue_foundStringValue() {
        YamlDataset d = new YamlDataset("a: 44\n");
        assertEquals("value", "44", d.lookup("a",
                Dataset.DesiredType.STRING));
    }
    public void test_checkValue_wantedStringValue() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("a:\n  child1: 44\n  child2: 55\n");
        try {
            d.lookup("a", Dataset.DesiredType.STRING);
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
    public void test_checkValue_foundDataset() {
        YamlDataset d = new YamlDataset("a:\n  child1: 44\n  child2: 55\n");
        Object result = d.lookup("a", Dataset.DesiredType.DATASET);
        assertEquals("class of result", "YamlDataset",
                result.getClass().getSimpleName());
        assertEquals("contents of returned dataset", "55",
                ((YamlDataset) result).get("child2"));
    }
    public void test_checkValue_wantedListFoundDataset() {
        YamlDataset d = new YamlDataset("a:\n  child1: 44\n  child2: 55\n");
        Object result = d.lookup("a", Dataset.DesiredType.DATASETS);
        assertEquals("class of result", "YamlDataset[]",
                result.getClass().getSimpleName());
        YamlDataset[] array = (YamlDataset[]) result;
        assertEquals("size of array", 1, array.length);
        assertEquals("contents of returned dataset", "44",
                array[0].get("child1"));
    }
    public void test_checkValue_wantedDatasetFoundList() {
        YamlDataset d = new YamlDataset("a:\n  - child1: 4\n    child2: 5\n"
                + "  - child1: 10\n    child2:20\n");
        Object result = d.lookup("a", Dataset.DesiredType.DATASET);
        assertEquals("class of result", "YamlDataset",
                result.getClass().getSimpleName());
        assertEquals("contents of returned dataset", "5",
                ((YamlDataset) result).get("child2"));
    }
    public void test_checkValue_FoundList() {
        YamlDataset d = new YamlDataset("a:\n  - child1: 4\n    child2: 5\n"
                + "  - child1: 10\n    child2:20\n");
        Object result = d.lookup("a", Dataset.DesiredType.DATASETS);
        assertEquals("class of result", "YamlDataset[]",
                result.getClass().getSimpleName());
        YamlDataset[] array = (YamlDataset[]) result;
        assertEquals("size of array", 2, array.length);
        assertEquals("contents of first dataset", "4",
                array[0].get("child1"));
        assertEquals("contents of second dataset", "20",
                array[1].get("child2"));
    }
    public void test_checkValue_wantedDatasetFoundListOfStrings() {
        boolean gotException = false;
        YamlDataset d = new YamlDataset("a:\n  - child1\n  - child2\n");
        try {
            d.lookup("a", Dataset.DesiredType.STRING);
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

    public void test_wrongTypeMessage_nested() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "string value but found nested dataset",
                YamlDatasetFixture.wrongTypeMessage("key_name", true,
                new HashMap()));
    }
    public void test_wrongTypeMessage_list() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "string value but found list",
                YamlDatasetFixture.wrongTypeMessage("key_name", true,
                new ArrayList()));
    }
    public void test_wrongTypeMessage_string() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "string value but found string value \"472\"",
                YamlDatasetFixture.wrongTypeMessage("key_name", true,
                new Integer(472)));
    }
    public void test_wrongTypeMessage_stringButLong() {
        assertEquals("wrong type for dataset element \"key_name\": expected "
                + "string value but found string value \"test 0123456789 ...\"",
                YamlDatasetFixture.wrongTypeMessage("key_name", true,
                "test 01234567890 test2 01234567890"));
    }
}

// The following class definition provides a mechanism for accessing
// protected/private fields and methods.
class YamlDatasetFixture extends YamlDataset {
    public YamlDatasetFixture(String s) {
        super(s);
    }
    public static String wrongTypeMessage(String key,
            Boolean wantSimple, Object got) {
        return YamlDataset.wrongTypeMessage(key,
                (wantSimple ? DesiredType.STRING: DesiredType.DATASET), got);
    }
}