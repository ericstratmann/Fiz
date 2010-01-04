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

package org.fiz.datamanager;

import java.io.*;
import java.util.*;
import org.apache.log4j.Level;

import org.fiz.*;
import org.fiz.test.*;

/**
 * Junit tests for the FileDataManager class.
 */

public class FileDataManagerTest extends junit.framework.TestCase {
    protected FileDataManager manager = null;
    public void setUp() {
        (new File("_testData_")).mkdir();
        manager = new FileDataManager("bogus", "_testData_");
    }

    public void tearDown() {
        TestUtil.deleteTree("_testData_");
    }

    public void test_constructor() {
        manager = new FileDataManager("abc", "xyz", "first/second");
        assertEquals("path size", 3, manager.path.length);
        assertEquals("path", "abc, xyz, first/second",
                StringUtil.join(manager.path, ", "));
    }
    public void test_constructor_defaultPath() {
        Config.setDataset("main", new Dataset("home", "/a/b/c"));
        manager = new FileDataManager();
        assertEquals("path", "/a/b/c/WEB-INF",
                StringUtil.join(manager.path, ", "));
    }

    public void test_clearCache() {
        TestUtil.writeFile("_testData_/test.yml", "value: abc\n");
        Dataset d = manager.loadDataset("test");
        assertEquals("# cached files before clearCache", 1,
                manager.datasetCache.size());
        manager.clearCache();
        assertEquals("# cached files after clearCache", 0,
                manager.datasetCache.size());
    }

    public void test_create() throws FileNotFoundException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "third: ghi\n" +
                "level1: 123\n");
        Dataset ret = manager.create("test",
                "level1.level2", new Dataset("first", "123", "new", "456"));
        assertEquals("dataset file", "first:  abc\n" +
                "level1:\n" +
                "    level2:\n" +
                "        first: 123\n" +
                "        new:   456\n" +
                "second: def\n" +
                "third:  ghi\n",
                Util.readFile("_testData_/test.yml").toString());
        assertEquals("return value", null, ret);
    }
    public void test_create_clearExisting()
            throws FileNotFoundException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "level1:\n" +
                "  level2:\n" +
                "    name: Alice\n" +
                "    age: 26\n");
        manager.create("test",
                "level1.level2", new Dataset("first", "123", "new", "456"));
        assertEquals("dataset file", "first:  abc\n" +
                "level1:\n" +
                "    level2:\n" +
                "        first: 123\n" +
                "        new:   456\n" +
                "second: def\n",
                Util.readFile("_testData_/test.yml").toString());
    }

    public void test_delete_deleteRoot() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        Dataset ret = manager.delete("test", null);
        assertEquals("dataset file", "",
                Util.readFile("_testData_/test.yml").toString());
        assertEquals("return value", null, ret);
    }
    public void test_delete_deleteNested() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n" +
                "country: USA");
        manager.delete("test", "child");
        assertEquals("dataset file", "country: USA\n",
                Util.readFile("_testData_/test.yml").toString());
    }

    public void test_read() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "level1:\n" +
                "  x: 47\n" +
                "  y: 58\n" +
                "second: def\n" +
                "third: ghi\n");
        Dataset data = manager.read("test", "level1");
        assertEquals("response", "x: 47\n" +
                     "y: 58\n", data.toString());
    }

    public void test_read_nonexistentDataset() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "third: ghi\n");
        Dataset data = manager.read("test",
                "level1.level2");
        assertEquals("error information", "culprit: path\n" +
                "message: nested dataset \"level1.level2\" doesn't exist\n",
                data.getErrorData()[0].toString());
    }

    public void test_update_modifyRoot() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "values:\n" +
                "  first: 123\n" +
                "  new: 456\n");
        manager.update("test", null,
                new Dataset("first", "123", "new", "456"));
        assertEquals("dataset file", "first:  123\n" +
                "new:    456\n" +
                "second: def\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_update_modifyNested() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "name: child\n" +
                "values:\n" +
                "  age: 36\n" +
                "  height: 65\n");
        manager.update("test", "child",
                new Dataset("age", "36", "height", "65"));
        assertEquals("dataset file", "child:\n" +
                "    age:    36\n" +
                "    height: 65\n" +
                "    name:   Alice\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_update_nonexistentChild() throws FileNotFoundException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        Dataset data = manager.update("test",
                "child.grandchild", new Dataset("age", "36"));
        assertEquals("error dataset",
                "culprit: path\n" +
                "message: nested dataset \"child.grandchild\" doesn't exist\n",
                data.getErrorData()[0].toString());
        assertEquals("dataset file not modified",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n",
                Util.readFile("_testData_/test.yml").toString());
    }

    public void test_loadDataset_notCached() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n");
        Dataset d = manager.loadDataset("test");
        assertEquals("returned dataset", "first:  abc\n" +
                "second: def\n", d.toString());
    }
    public void test_loadDataset_cached() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n");
        manager.loadDataset("test");
        TestUtil.deleteTree("_testData_/test.yml");
        Dataset d = manager.loadDataset("test");
        assertEquals("returned dataset", "first:  abc\n" +
                "second: def\n", d.toString());
    }

    public void test_findNestedDataset_useRoot() {
        Dataset root = YamlDataset.newStringInstance(
                "first: abc\n" +
                "second: def\n");
        assertEquals("returned dataset", root,
                manager.findNestedDataset(root, null));
    }
    public void test_findNestedDataset_useDescendent() {
        Dataset root = YamlDataset.newStringInstance(
                "first: abc\n" +
                "level1:\n" +
                "  level2:\n" +
                "     id: 666\n");
        assertEquals("returned dataset", "id: 666\n",
                manager.findNestedDataset(root, "level1.level2").toString());
    }
    public void test_findNestedDataset_cantFindDescendent() {
        Dataset root = YamlDataset.newStringInstance(
                "first: abc\n" +
                "second: def\n");
        Dataset data = manager.findNestedDataset(root, "level1.level2");
        assertEquals("error dataset",
                "culprit: path\n" +
                "message: nested dataset \"level1.level2\" doesn't exist\n",
                 data.getErrorData()[0].toString());
    }
}
