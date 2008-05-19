package org.fiz;
import java.util.*;

/**
 * Junit tests for the CompoundDataset class.
 */
public class CompoundDatasetTest extends junit.framework.TestCase {
    protected CompoundDataset compound = null;

    public void setUp() {
        Dataset d1 = YamlDataset.newStringInstance(
                "a: 111\n" +
                "nested:\n" +
                "  x: x_value\n" +
                "  y: y_value\n");
        Dataset d2 = new Dataset("b", "222", "nested", "nnn");
        Dataset d3 = new Dataset("x", "77", "a", "99");
        Dataset d4 = YamlDataset.newStringInstance(
                "a: 333\n" +
                "c: 444\n" +
                "nested:\n" +
                "  - x: xxxx\n" +
                "    z: yyyy\n" +
                "  - x: zzzz\n" +
                "child:\n" +
                "  name: Alice\n" +
                "b:\n" +
                "  value: 88\n");
        compound = new CompoundDataset(d1, d2, d3, d4);
    }

    // No tests for array constructor: nothing interesting to test.

    public void test_constructor_arrayList() {
        ArrayList<Dataset> components = new ArrayList<Dataset>();
        components.add(new Dataset("name", "Alice"));
        components.add(new Dataset("name", "Bob"));
        components.add(new Dataset("name", "Carol"));
        CompoundDataset c = new CompoundDataset(components);
        assertEquals("component datasets", "name: Alice\n" +
                ", name: Bob\n" +
                ", name: Carol\n",
                StringUtil.join(c.components, ", "));
    }

    public void test_addChild() {
        boolean gotException = false;
        try {
            compound.addChild("x", new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "addChild invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_check() {
        assertEquals("duplicate value: choose first", "111",
                compound.check("a"));
        assertEquals("value in last dataset", "444",
                compound.check("c"));
        assertEquals("value doesn't exist", null,
                compound.check("xyz"));
        assertEquals("first value found is a nested dataset", "nnn",
                compound.check("nested"));
    }

    public void test_clear() {
        boolean gotException = false;
        try {
            compound.clear();
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "clear invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_clone() {
        Dataset d1 = new Dataset("a", "111");
        Dataset d2 = new Dataset("b", "222");
        Dataset compound2 = new CompoundDataset(d1, d2);
        Dataset compound3 = compound2.clone();
        d1.set("a", "333");
        d2.set("b", "444");
        assertEquals("a from original", "333", compound2.get("a"));
        assertEquals("b from original", "444", compound2.get("b"));
        assertEquals("a from clone", "111", compound3.get("a"));
        assertEquals("b from clone", "222", compound3.get("b"));
    }

    public void test_containsKey() {
        assertEquals("value in last dataset", true,
                compound.containsKey("c"));
        assertEquals("value doesn't exist", false,
                compound.containsKey("xyz"));
    }

    public void test_copyFrom() {
        boolean gotException = false;
        try {
            compound.copyFrom(new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "copyFrom invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_createChild() {
        boolean gotException = false;
        try {
            compound.createChild("a");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "createChild invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_createChild_keyAndDataset() {
        boolean gotException = false;
        try {
            compound.createChild("a", new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "createChild invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_createChildPath() {
        boolean gotException = false;
        try {
            compound.createChildPath("a");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "createChildPath invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_createChildPath_keyAndDataset() {
        boolean gotException = false;
        try {
            compound.createChildPath("a", new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "createChildPath invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_delete() {
        boolean gotException = false;
        try {
            compound.delete("a");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "delete invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_deletePath() {
        boolean gotException = false;
        try {
            compound.deletePath("a.b");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "deletePath invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_get() {
        assertEquals("value in first dataset", "111", compound.get("a"));
        assertEquals("value in last dataset", "444", compound.get("c"));
        assertEquals("first value found is nested dataset", "nnn",
                compound.get("nested"));
    }
    public void test_get_noSuchValue() {
        boolean gotException = false;
        try {
            compound.get("abcde");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"abcde\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getChild() {
        assertEquals("child in first dataset",
                "x: x_value\n" +
                "y: y_value\n",
                compound.getChild("nested").toString());
        assertEquals("child in last dataset", "name: Alice\n",
                compound.getChild("child").toString());
        assertEquals("string value with same name as child", "value: 88\n",
                compound.getChild("b").toString());
    }
    public void test_getChild_noSuchValue() {
        boolean gotException = false;
        try {
            compound.getChild("abcde");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"abcde\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getChildPath() {
        Dataset d1 = YamlDataset.newStringInstance(
                "x: 99\n");
        Dataset d2 = YamlDataset.newStringInstance(
                "a:\n" +
                "  b:\n" +
                "    name: Alice\n" +
                "    age: 28\n");
        Dataset compound2 = new CompoundDataset(d1, d2);
        assertEquals("child in last dataset", "age:  28\n" +
                "name: Alice\n",
                compound2.getChildPath("a.b").toString());
    }
    public void test_getChildPath_noSuchValue() {
        boolean gotException = false;
        try {
            compound.getChildPath("nested.x.y.bogus");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"nested.x.y.bogus\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getChildren() {
        Dataset[] children = compound.getChildren("nested");
        assertEquals("number of children", 3, children.length);
        assertEquals("first child",
                "x: x_value\n" +
                "y: y_value\n",
                children[0].toString());
        assertEquals("second child",
                "x: xxxx\n" +
                "z: yyyy\n",
                children[1].toString());
        assertEquals("third child",
                "x: zzzz\n",
                children[2].toString());
    }
    public void test_getChildren_noMatches() {
        Dataset[] children = compound.getChildren("bogus");
        assertEquals("number of children", 0, children.length);
    }

    public void test_getChildrenPath() {
        Dataset d1 = YamlDataset.newStringInstance(
                "a:\n" +
                "  b:\n" +
                "    name: Alice\n" +
                "    age: 28\n");
        Dataset d2 = YamlDataset.newStringInstance(
                "a:\n" +
                "  b: 45\n");
        Dataset d3 = YamlDataset.newStringInstance(
                "x: 99\n");
        Dataset d4 = YamlDataset.newStringInstance(
                "a:\n" +
                "  b:\n" +
                "    - name: Bill\n" +
                "      age: 92\n" +
                "    - name: Carol\n" +
                "      age: 6\n");
        Dataset compound2 = new CompoundDataset(d1, d2, d3, d4);
        Dataset[] children = compound2.getChildrenPath("a.b");
        assertEquals("number of children", 3, children.length);
        assertEquals("first child",
                "age:  28\n" +
                "name: Alice\n",
                children[0].toString());
        assertEquals("second child",
                "age:  92\n" +
                "name: Bill\n",
                children[1].toString());
        assertEquals("third child",
                "age:  6\n" +
                "name: Carol\n",
                children[2].toString());
    }
    public void test_getChildrenPath_noMatches() {
        Dataset[] children = compound.getChildren("bogus.bogus.bogus");
        assertEquals("number of children", 0, children.length);
    }

    public void test_getComponent() {
        assertEquals("component index 2", compound.components[2],
                compound.getComponent(2));
    }

    public void test_getComponents() {
        assertEquals("component array", compound.components,
                compound.getComponents());
    }

    public void test_getFileName() {
        assertEquals("null file name", null, compound.getFileName());
    }

    public void test_getPath_valueExists() {
        assertEquals("value exists", "88", compound.getPath("b.value"));
    }
    public void test_getPath_valueDoesntExist() {
        boolean gotException = false;
        try {
            compound.getPath("b.bogus.xyz");
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"b.bogus.xyz\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_keySet() {
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(compound.keySet());
        Collections.sort(names);
        assertEquals("top-level keys", "a, b, c, child, nested, x",
                StringUtil.join(names, ", "));
    }

    public void test_lookup() {
        Object[] values = (Object[]) compound.lookup("a",
                Dataset.DesiredType.ALL);
        assertEquals("wanted == ALL", "111, 99, 333",
                StringUtil.join(values, ", "));
        String s = (String) compound.lookup("a",
                Dataset.DesiredType.STRING);
        assertEquals("wanted == STRING", "111", s);
    }

    public void test_lookupPath() {
        Object values[] = (Object[]) compound.lookupPath("nested.x",
                Dataset.DesiredType.ALL);
        assertEquals("wanted == ALL", "x_value, xxxx, zzzz",
                StringUtil.join(values, ", "));
        String s = (String) compound.lookupPath("nested.x",
                Dataset.DesiredType.STRING);
        assertEquals("wanted == STRING", "x_value", s);
    }

    public void test_set() {
        boolean gotException = false;
        try {
            compound.set("a", "34");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "set invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_size() {
        assertEquals("result of size method", 4, compound.size());
    }

    public void test_setComponent() {
        CompoundDataset c = new CompoundDataset(
                new Dataset("name", "Alice"),
                new Dataset("name", "Bob"),
                new Dataset("name", "Carol"));
        c.setComponent(2, new Dataset("name", "Frank"));
        assertEquals("dataset values", "Alice, Bob, Frank",
                StringUtil.join((Object[]) c.lookup("name",
                Dataset.DesiredType.ALL), ", "));
    }

    public void test_toJavascript() {
        boolean gotException = false;
        try {
            compound.toJavascript(new StringBuilder());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "toJavascript invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_toString() {
        assertEquals("Component #0:\n" +
                "  a: 111\n" +
                "  nested:\n" +
                "      x: x_value\n" +
                "      y: y_value\n" +
                "Component #1:\n" +
                "  b:      222\n" +
                "  nested: nnn\n" +
                "Component #2:\n" +
                "  a: 99\n" +
                "  x: 77\n" +
                "Component #3:\n" +
                "  a: 333\n" +
                "  b:\n" +
                "      value: 88\n" +
                "  c: 444\n" +
                "  child:\n" +
                "      name: Alice\n" +
                "  nested:\n" +
                "    - x: xxxx\n" +
                "      z: yyyy\n" +
                "    - x: zzzz\n", compound.toString());
    }

    public void test_writeFile() {
        boolean gotException = false;
        try {
            compound.writeFile("foo.bar", "Dummy comment");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "writeFile invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
