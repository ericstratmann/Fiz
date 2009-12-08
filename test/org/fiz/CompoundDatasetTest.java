/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz;
import java.util.*;

/**
 * Junit tests for the CompoundDataset class.
 */
public class CompoundDatasetTest extends junit.framework.TestCase {
    protected CompoundDataset compound = null;
    Dataset d1, d2, d3, d4;

    public void setUp() {
        d1 = YamlDataset.newStringInstance(
                "a: 111\n" +
                "nested:\n" +
                "  x: x_value\n" +
                "  y: y_value\n");
        d2 = new Dataset("b", "222", "nested", "nnn");
        d3 = new Dataset("x", "77", "a", "99");
        d4 = YamlDataset.newStringInstance(
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

    public void test_addSerializedData_twoArgs() {
        boolean gotException = false;
        try {
            compound.addSerializedData("x", 0);
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "addSerializedData invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_addSerializedData_oneArg() {
        boolean gotException = false;
        try {
            compound.addSerializedData("x");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "addSerializedData invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_add() {
        boolean gotException = false;
        try {
            compound.add("x", new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "add invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_addPath() {
        boolean gotException = false;
        try {
            compound.addPath("x", new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "addPath invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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

    public void test_clone_with_arg() {
        boolean gotException = false;
        try {
            compound.clone(new Dataset());
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "clone with argument invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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

    public void test_keySet() {
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(compound.keySet());
        Collections.sort(names);
        assertEquals("top-level keys", "a, b, c, child, nested, x",
                StringUtil.join(names, ", "));
    }

    public void test_lookup_3args() {
        Object out = compound.lookup("a",
                Dataset.Quantity.ALL);
        assertEquals("result class", "DSArrayList",
                out.getClass().getSimpleName());
        assertEquals("result value", "111, 99, 333",
                StringUtil.join((ArrayList) out, ", "));
    }

    public void test_lookup_returnFirst() {
        assertEquals("value of a", 111, compound.lookup("a",
                Dataset.Quantity.FIRST_ONLY));
        assertEquals("value of b", "222", compound.lookup("b",
                Dataset.Quantity.FIRST_ONLY));
    }
    public void test_lookup_returnAll() {
        Object out = compound.lookup("b",
                Dataset.Quantity.ALL);
        assertEquals("result class", "DSArrayList",
                out.getClass().getSimpleName());
        assertEquals("result value", "222, value: 88\n",
                StringUtil.join((ArrayList) out, ", "));
    }

    public void test_lookup_cantFindAny() {
        assertEquals("return value null", null, compound.lookup("bogus",
                Dataset.Quantity.FIRST_ONLY));
    }

    public void test_serialize_withStringBuilder() {
        boolean gotException = false;
        try {
            StringBuilder out = new StringBuilder();
            compound.serialize(out);
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "serialize invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_serialize() {
        boolean gotException = false;
        try {
            compound.serialize();
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "serialize invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
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

    public void test_setPath() {
        boolean gotException = false;
        try {
            compound.setPath("a", "34");
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "setPath invoked on a CompoundDataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_size() {
        assertEquals("result of size method", 4, compound.size());
    }

//    public void test_setComponent() {
//        CompoundDataset c = new CompoundDataset(
//                new Dataset("name", "Alice"),
//                new Dataset("name", "Bob"),
//                new Dataset("name", "Carol"));
//        c.setComponent(2, new Dataset("name", "Frank"));
//        assertEquals("dataset values", "Alice, Bob, Frank",
//                StringUtil.join((Object[]) c.lookup("name",
//                Dataset.DesiredType.ALL), ", "));
//    }

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

    public void test_compoundCompound() {
        // This test makes sure that CompoundDatasets can contain
        // other CompoundDatasets.
        Dataset d1 = YamlDataset.newStringInstance(
                "a: 111\n" +
                "child:\n" +
                "  - name: Alice\n" +
                "    age:  40\n" +
                "  - name: Bob\n");
        Dataset d2 = YamlDataset.newStringInstance(
                "b: 222\n" +
                "child:\n" +
                "    name: Carol\n" +
                "    age:  24\n");
        Dataset d3 = YamlDataset.newStringInstance(
                "a: 333\n" +
                "b: 444\n" +
                "c: 555\n");
        Dataset d4 = YamlDataset.newStringInstance(
                "a: 666\n" +
                "child:\n" +
                "  - name: David\n" +
                "    age:  12\n" +
                "  - name: Elise\n" +
                "    age:  18\n" +
                "  - name: Fred\n");
        CompoundDataset c1 = new CompoundDataset(d1, d2);
        CompoundDataset c2 = new CompoundDataset(d3, d4);
        CompoundDataset compound = new CompoundDataset(c1, c2);
        assertEquals("nested strings",
                "Alice, Bob, Carol, David, Elise, Fred",
                StringUtil.join((ArrayList) compound.lookup("child.name",
                Dataset.Quantity.ALL), ", "));
    }

    public void test_methodsThrowError() throws Throwable {
        CompoundDataset data = new CompoundDataset(new Dataset("a", "b"),
                                                   new Dataset("b", "a"));;
        data.setError(new Dataset("message", "oops"));

        String[] whiteList = {};
        DatasetTest.runPublicMethods(data, whiteList, "oops");
    }
}
