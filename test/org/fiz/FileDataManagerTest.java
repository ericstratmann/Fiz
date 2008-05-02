package org.fiz;
import java.io.*;
import java.util.*;
import org.apache.log4j.Level;

/**
 * Junit tests for the FileDataManager class.
 */

public class FileDataManagerTest extends junit.framework.TestCase {
    protected FileDataManager manager = null;
    public void setUp() {
        Config.setDataset("main", new Dataset());
        DataManager.logger.setLevel(Level.ERROR);
        (new File("_testData_")).mkdir();
        manager = new FileDataManager(new Dataset("pathTemplate",
                "bogus,_testData_"));
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "test:\n" +
                "  class: FileDataManager\n" +
                "  pathTemplate: \"bogus,_testData_\"\n"));
        Config.setDataset("main", YamlDataset.newStringInstance(
                "searchPackages: org.fiz\n"));
        DataManager.destroyAll();
    }

    public void tearDown() {
        TestUtil.deleteTree("_testData_");
    }

    public void test_constructor() {
        Config.setDataset("main", new Dataset("home", "/a/b/c",
                "test", "<test>, xyz"));
        manager = new FileDataManager(new Dataset("pathTemplate",
                "@home/myDir, /demo/@test"));
        assertEquals("path size", 3, manager.path.length);
        assertEquals("expanded path", "/a/b/c/myDir, /demo/<test>, xyz",
                Util.join(manager.path, ", "));
    }

    public void test_startRequests_create() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager: test\n" +
                "request: create\n" +
                "file: test\n" +
                "dataset: child2\n" +
                "values:\n" +
                "  age: 36\n" +
                "  height: 65\n"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request);
        manager.startRequests(requests);
        assertEquals("error dataset", null, request.getErrorData());
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file",
                "child:\n" +
                "    age:  21\n" +
                "    name: Alice\n" +
                "child2:\n" +
                "    age:    36\n" +
                "    height: 65\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_startRequests_read() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "level1:\n" +
                "  x: 47\n" +
                "  y: 58\n" +
                "second: def\n" +
                "third: ghi\n");
        DataRequest request1 = new DataRequest(new Dataset("manager", "test",
                "request", "read", "file", "test"));
        DataRequest request2 = new DataRequest(new Dataset("manager", "test",
                "request", "read", "file", "test", "dataset", "level1"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request1);
        requests.add(request2);
        manager.startRequests(requests);
        assertEquals("response for first request",
                "first:  abc\n" +
                "level1:\n" +
                "    x: 47\n" +
                "    y: 58\n" +
                "second: def\n" +
                "third:  ghi\n", request1.getResponseData().toString());
        assertEquals("response for first request",
                "x: 47\n" +
                "y: 58\n", request2.getResponseData().toString());
    }
    public void test_startRequests_update() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager: test\n" +
                "request: update\n" +
                "file: test\n" +
                "values:\n" +
                "  age: 36\n"));
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("error dataset", null, request.getErrorData());
        assertEquals("dataset file",
                "age: 36\n" +
                "child:\n" +
                "    age:  21\n" +
                "    name: Alice\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_startRequests_delete() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  grandchild:\n" +
                "    name: Bill\n" +
                "  age: 21\n");
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager: test\n" +
                "request: delete\n" +
                "file: test\n" +
                "dataset: child.grandchild\n"));
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("error dataset", null, request.getErrorData());
        assertEquals("dataset file",
                "child:\n" +
                "    age:  21\n" +
                "    name: Alice\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_startRequests_unknownOperation() {
        DataRequest request = new DataRequest(new Dataset("manager", "test",
                "request", "bogus"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request);
        manager.startRequests(requests);
        assertEquals("response", null, request.getResponseData());
        assertEquals("error information",
                "message: \"unknown request \\\"bogus\\\" for " +
                "FileDataManager; must be create, read, update, or " +
                "delete\"\n",
                request.getErrorData().toString());
    }
    public void test_startRequests_missingParameter_operationNonNull() {
        DataRequest request = new DataRequest(new Dataset("manager", "test",
                "request", "read"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request);
        manager.startRequests(requests);
        assertEquals("response", null, request.getResponseData());
        assertEquals("error information",
                "message: FileDataManager \"read\" request didn't " +
                "contain required parameter \"file\"\n",
                request.getErrorData().toString());
    }
    public void test_startRequests_missingParameter_operationNull() {
        DataRequest request = new DataRequest(new Dataset("manager", "test"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request);
        manager.startRequests(requests);
        assertEquals("response", null, request.getResponseData());
        assertEquals("error information",
                "message: FileDataManager request didn't contain required " +
                "parameter \"request\"\n", request.getErrorData().toString());
    }
    public void test_startRequests_unknownError() {
        DataRequest request = new DataRequest(new Dataset("manager", "test",
                "request", "read", "file", "test", "dataset", "a/b/c"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request);
        manager.startRequests(requests);
        assertEquals("response", null, request.getResponseData());
        assertEquals("error information",
                "internal error in FileDataManager \"read\" request: " +
                "couldn't find dataset file \"test\" in path (\"bogus\", " +
                "\"_testData_\")",
                request.getErrorData().get("message"));
    }

    public void test_createOperation_modifyRoot() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "third: ghi\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "values:\n" +
                "  first: 123\n" +
                "  new: 456\n");
        DataRequest request = new DataRequest(new Dataset());
        manager.createOperation(request, parameters);
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "first: 123\n" +
                "new:   456\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_createOperation_modifyNested() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n" +
                "third: ghi\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "dataset: level1.level2\n" +
                "values:\n" +
                "  first: 123\n" +
                "  new: 456\n");
        DataRequest request = new DataRequest(new Dataset());
        manager.createOperation(request, parameters);
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "first:  abc\n" +
                "level1:\n" +
                "    level2:\n" +
                "        first: 123\n" +
                "        new:   456\n" +
                "second: def\n" +
                "third:  ghi\n",
                Util.readFile("_testData_/test.yml").toString());
    }

    public void test_readOperation() {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "level1:\n" +
                "  x: 47\n" +
                "  y: 58\n" +
                "second: def\n" +
                "third: ghi\n");
        DataRequest request = new DataRequest(new Dataset("manager", "test"));
        manager.readOperation(request, new Dataset("file", "test"));
        assertEquals("response", "first:  abc\n" +
                "level1:\n" +
                "    x: 47\n" +
                "    y: 58\n" +
                "second: def\n" +
                "third:  ghi\n", request.getResponseData().toString());
    }

    public void test_updateOperation_modifyRoot() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "first: abc\n" +
                "second: def\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "values:\n" +
                "  first: 123\n" +
                "  new: 456\n");
        DataRequest request = new DataRequest(new Dataset());
        manager.updateOperation(request, parameters);
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "first:  123\n" +
                "new:    456\n" +
                "second: def\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_updateOperation_modifyNested() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "dataset: child\n" +
                "values:\n" +
                "  age: 36\n" +
                "  height: 65\n");
        DataRequest request = new DataRequest(new Dataset());
        manager.updateOperation(request, parameters);
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "child:\n" +
                "    age:    36\n" +
                "    height: 65\n" +
                "    name:   Alice\n",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_updateOperation_nonexistentChild() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        DataRequest request = new DataRequest(YamlDataset.newStringInstance(
                "manager: test\n" +
                "request: update\n" +
                "file: test\n" +
                "dataset: child.grandchild\n" +
                "values:\n" +
                "  age: 36\n" +
                "  height: 65\n"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request);
        manager.startRequests(requests);
        assertEquals("response", null, request.getResponseData());
        assertEquals("error dataset",
                "culprit: dataset\n" +
                "message: nested dataset \"child.grandchild\" doesn't exist\n",
                request.getErrorData().toString());
        assertEquals("dataset file not modified",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n",
                Util.readFile("_testData_/test.yml").toString());
    }

    public void test_deleteOperation_deleteRoot() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n");
        DataRequest request = new DataRequest(new Dataset());
        manager.deleteOperation(request, parameters);
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "",
                Util.readFile("_testData_/test.yml").toString());
    }
    public void test_deleteOperation_deleteNested() throws IOException {
        TestUtil.writeFile("_testData_/test.yml",
                "child:\n" +
                "  name: Alice\n" +
                "  age: 21\n" +
                "country: USA");
        Dataset parameters = YamlDataset.newStringInstance(
                "file: test\n" +
                "dataset: child\n");
        DataRequest request = new DataRequest(new Dataset());
        manager.deleteOperation(request, parameters);
        assertEquals("response", "", request.getResponseData().toString());
        assertEquals("dataset file", "country: USA\n",
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
        assertEquals("returned dataset", root,
                manager.findNestedDataset(root, "", null));
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
        DataRequest request = new DataRequest(new Dataset());
        boolean gotException = false;
        try {
            manager.findNestedDataset(root, "level1.level2", request);
        }
        catch (FileDataManager.RequestAbortedError e) {
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
        assertEquals("error dataset",
                "culprit: dataset\n" +
                "message: nested dataset \"level1.level2\" doesn't exist\n",
                request.getErrorData().toString());
    }

    public void test_flush() {
        TestUtil.writeFile("_testData_/test.yml", "value: abc\n");
        Dataset d = manager.loadDataset("test");
        assertEquals("value before flushing", "abc", d.get("value"));
        TestUtil.writeFile("_testData_/test.yml", "value: def\n");
        manager.flush();
        d = manager.loadDataset( "test");
        assertEquals("value after flushing", "def", d.get("value"));
    }
}
