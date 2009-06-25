package org.fiz;
import java.io.*;
import java.util.*;
import org.apache.log4j.Level;

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

    public void test_clearCache() {
        TestUtil.writeFile("_testData_/test.yml", "value: abc\n");
        Dataset d = manager.loadDataset("test");
        assertEquals("# cached files before clearCache", 1,
                manager.datasetCache.size());
        manager.clearCache();
        assertEquals("# cached files after clearCache", 0,
                manager.datasetCache.size());
    }

    public void test_newCreateRequest() throws FileNotFoundException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "third: ghi\n" +
                "level1: 123\n");
        DataRequest request = manager.newCreateRequest("test",
                "level1.level2", new Dataset("first", "123", "new", "456"));
        assertEquals("empty response dataset", "",
                request.getResponseOrAbort().toString());
        assertEquals("dataset file", "first:  abc\n" +
                "level1:\n" +
                "    level2:\n" +
                "        first: 123\n" +
                "        new:   456\n" +
                "second: def\n" +
                "third:  ghi\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_newCreateRequest_clearExisting()
            throws FileNotFoundException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "level1:\n" +
                "  level2:\n" +
                "    name: Alice\n" +
                "    age: 26\n");
        DataRequest request = manager.newCreateRequest("test",
                "level1.level2", new Dataset("first", "123", "new", "456"));
        assertEquals("dataset file", "first:  abc\n" +
                "level1:\n" +
                "    level2:\n" +
                "        first: 123\n" +
                "        new:   456\n" +
                "second: def\n",
                Util.readFile("_testData_/test.yml").toString());
    }

    public void test_newDeleteRequest_deleteRoot() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        DataRequest request = manager.newDeleteRequest("test", null);
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_newDeleteRequest_deleteNested() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n" +
                "country: USA");
        DataRequest request =  manager.newDeleteRequest("test", "child");
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "country: USA\n",
                Util.readFile("_testData_/test.yml").toString());
    }

    public void test_newReadRequest() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "level1:\n" +
                "  x: 47\n" +
                "  y: 58\n" +
                "second: def\n" +
                "third: ghi\n");
        DataRequest request = manager.newReadRequest("test", "level1");
        assertEquals("response", "x: 47\n" +
                "y: 58\n", request.getResponseData().toString());
    }
    public void test_newReadRequest_nonexistentDataset() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "third: ghi\n");
        DataRequest request = manager.newReadRequest("test",
                "level1.level2");
        assertEquals("error information", "culprit: path\n" +
                "message: nested dataset \"level1.level2\" doesn't exist\n",
                request.getErrorData()[0].toString());
    }

    public void test_newUpdateRequest_modifyRoot() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "values:\n" +
                "  first: 123\n" +
                "  new: 456\n");
        DataRequest request = manager.newUpdateRequest("test", null,
                new Dataset("first", "123", "new", "456"));
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "first:  123\n" +
                "new:    456\n" +
                "second: def\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_newUpdateRequest_modifyNested() throws IOException {
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
        DataRequest request = manager.newUpdateRequest("test", "child",
                new Dataset("age", "36", "height", "65"));
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "child:\n" +
                "    age:    36\n" +
                "    height: 65\n" +
                "    name:   Alice\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_newUpdateRequest_nonexistentChild() throws FileNotFoundException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        DataRequest request = manager.newUpdateRequest("test",
                "child.grandchild", new Dataset("age", "36"));
        assertEquals("response", null, request.getResponseData());
        assertEquals("error dataset",
                "culprit: path\n" +
                "message: nested dataset \"child.grandchild\" doesn't exist\n",
                request.getErrorData()[0].toString());
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
                manager.findNestedDataset(root, null, null));
    }
    public void test_findNestedDataset_useDescendent() {
        Dataset root = YamlDataset.newStringInstance(
                "first: abc\n" +
                "level1:\n" +
                "  level2:\n" +
                "     id: 666\n");
        assertEquals("returned dataset", "id: 666\n",
                manager.findNestedDataset(root, "level1.level2",
                null).toString());
    }
    public void test_findNestedDataset_cantFindDescendent() {
        Dataset root = YamlDataset.newStringInstance(
                "first: abc\n" +
                "second: def\n");
        DataRequest request = new DataRequest("test");
        manager.findNestedDataset(root, "level1.level2", request);
        assertEquals("error dataset",
                "culprit: path\n" +
                "message: nested dataset \"level1.level2\" doesn't exist\n",
                request.getErrorData()[0].toString());
    }
}
