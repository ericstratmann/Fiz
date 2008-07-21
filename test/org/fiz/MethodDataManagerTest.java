package org.fiz;

import java.util.*;

/**
 * Junit tests for the MethodDataManager class.
 */

public class MethodDataManagerTest extends junit.framework.TestCase {
    // The following class is used as the invocation target for tests.
    public static class TargetClass {
        public static int invocations = 0;
        public void method1(DataRequest request) {
            invocations++;
            request.setComplete(request.getRequestData());
        }
        public static void method2(DataRequest request) {
            request.setComplete(new Dataset("message", "method2 invoked"));
        }
        public void exception(DataRequest request) throws Exception {
            throw new Exception("test exception");
        }
    }

    // The following class cannot be used because it doesn't have
    // a no-argument constructor.
    public static class TargetClass2 {
        public String id;
        public TargetClass2(String id) {
            this.id = id;
        }
        public void method1(DataRequest request) {
            request.setComplete(new Dataset());
        }
    }

    protected MethodDataManager manager;

    public void setUp() {
        Config.setDataset("main", new Dataset());
        Config.setDataset("dataManagers", YamlDataset.newStringInstance(
                "method:\n" +
                "  class: org.fiz.MethodDataManager\n"));
        manager = new MethodDataManager();
    }

    public void test_startRequests_basics() {
        DataRequest request1 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "method1",
                "id", "first"));
        DataRequest request2 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "method1",
                "id", "second"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request1);
        requests.add(request2);
        DataRequest.start(requests);
        assertEquals("response from first request", "first",
                request1.getResponseData().get("id"));
        assertEquals("response from second request", "second",
                request2.getResponseData().get("id"));
    }
    public void test_startRequests_noClassName() {
        DataRequest request1 = new DataRequest(new Dataset(
                "manager", "method",
                "method", "method1",
                "id", "first"));
        DataRequest request2 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "method1",
                "id", "second"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request1);
        requests.add(request2);
        DataRequest.start(requests);
        assertEquals("error message from first request",
                "no class name supplied",
                request1.getErrorData()[0].get("message"));
        assertEquals("response from second request", "second",
                request2.getResponseData().get("id"));
    }
    public void test_startRequests_noMethodName() {
        DataRequest request1 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "id", "first"));
        DataRequest request2 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "method1",
                "id", "second"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request1);
        requests.add(request2);
        DataRequest.start(requests);
        assertEquals("error message from first request",
                "no method name supplied",
                request1.getErrorData()[0].get("message"));
        assertEquals("response from second request", "second",
                request2.getResponseData().get("id"));
    }
    public void test_startRequests_cantFindMethod() {
        DataRequest request1 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "bogus",
                "id", "first"));
        DataRequest request2 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "method1",
                "id", "second"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request1);
        requests.add(request2);
        DataRequest.start(requests);
        assertEquals("error message from first request",
                "can't find method \"bogus\" in class " +
                "\"org.fiz.MethodDataManagerTest$TargetClass\"",
                request1.getErrorData()[0].get("message"));
        assertEquals("response from second request", "second",
                request2.getResponseData().get("id"));
    }
    public void test_startRequests_exceptionInMethod() {
        DataRequest request1 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "exception",
                "id", "first"));
        DataRequest request2 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "method1",
                "id", "second"));
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(request1);
        requests.add(request2);
        DataRequest.start(requests);
        assertEquals("error message from first request",
                "uncaught exception in method \"exception\" of class " +
                "\"org.fiz.MethodDataManagerTest$TargetClass\": " +
                "test exception",
                request1.getErrorData()[0].get("message"));
        assertEquals("response from second request", "second",
                request2.getResponseData().get("id"));
    }
    public void test_startRequests_staticMethod() {
        DataRequest request1 = new DataRequest(new Dataset(
                "manager", "method",
                "class", "org.fiz.MethodDataManagerTest$TargetClass",
                "method", "method2"));
        assertEquals("response from request", "message: method2 invoked\n",
                request1.getResponseData().toString());
    }

    public void test_findMethod_noSuchClass() {
        boolean gotException = false;
        try {
            manager.findMethod("bogusClass", "bogusMethod");
        }
        catch (DatasetError e) {
            assertEquals("error data set",
                    "culprit: class\n" +
                    "message: can't find class \"bogusClass\"\n",
                    e.getErrorData()[0].toString());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findMethod_noSuchMethod() {
        boolean gotException = false;
        try {
            manager.findMethod("org.fiz.MethodDataManagerTest$TargetClass",
                    "bogusMethod");
        }
        catch (DatasetError e) {
            assertEquals("error data set",
                    "culprit: method\n" +
                    "message: can't find method \"bogusMethod\" in " +
                    "class \"org.fiz.MethodDataManagerTest$TargetClass\"\n",
                    e.getErrorData()[0].toString());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findMethod_cantConstructInstance() {
        boolean gotException = false;
        try {
            manager.findMethod("org.fiz.MethodDataManagerTest$TargetClass2",
                    "method1");
        }
        catch (DatasetError e) {
            assertEquals("error data set",
                    "culprit: class\n" +
                    "message: can't create instance of class " +
                    "\"org.fiz.MethodDataManagerTest$TargetClass2\" (is " +
                    "there a no-argument constructor?)\n",
                    e.getErrorData()[0].toString());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_findMethod_success() {
        MethodDataManager.MethodInfo info = manager.findMethod(
                "org.fiz.MethodDataManagerTest$TargetClass",
                "method1");
        assertEquals("name of method", "method1", info.method.getName());
        assertEquals("name of class", "TargetClass",
                info.instance.getClass().getSimpleName());
    }
    public void test_findMethod_reuseInstanceInLaterCalls() {
        MethodDataManager.MethodInfo info = manager.findMethod(
                "org.fiz.MethodDataManagerTest$TargetClass",
                "method1");
        MethodDataManager.MethodInfo info2 = manager.findMethod(
                "org.fiz.MethodDataManagerTest$TargetClass",
                "method1");
        assertEquals("reuse instance", info.instance, info2.instance);
    }
    public void test_findMethod_staticMethod() {
        MethodDataManager.MethodInfo info = manager.findMethod(
                "org.fiz.MethodDataManagerTest$TargetClass",
                "method2");
        assertEquals("null instance", null, info.instance);
    }
}
