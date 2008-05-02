package org.fiz;
import java.util.*;

/**
 * Junit tests for the DataRequest class.
 */

public class DataRequestTest extends junit.framework.TestCase {
    public void setUp() {
        DataManagerFixture.init();
        Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                "request1:\n" +
                "  manager: testManager\n" +
                "  name: @name\n" +
                "  nickname: @@fluffy\n" +
                "  id: 44\n" +
                "request2:\n" +
                "  manager: testManager\n" +
                "  id: 22\n"));
    }

    public void test_constructor() {
        DataRequest request = new DataRequest(new Dataset(
                "name", "Bob", "manager", "testManager", "age", "18"));
        assertEquals("request dataset", "age:     18\n" +
                "manager: testManager\n" +
                "name:    Bob\n",
                request.request.toString());
    }

    public void test_constructor_withAuxDataset() {
        DataRequest request = new DataRequest("request1",
                new Dataset("name", "Bob"));
        assertEquals("request dataset", "id:       44\n" +
                "manager:  testManager\n" +
                "name:     Bob\n" +
                "nickname: \"@fluffy\"\n",
                request.request.toString());
    }

    public void test_constructor_withAuxDatasetAndManager() {
        DataRequest request = new DataRequest("request1",
                new Dataset("name", "Bob"), "t2");
        assertEquals("request dataset", "id:       44\n" +
                "manager:  t2\n" +
                "name:     Bob\n" +
                "nickname: \"@fluffy\"\n",
                request.request.toString());
    }
    public void test_constructor_withAuxDatasetAndManager_managerNull() {
        DataRequest request = new DataRequest("request1",
                new Dataset("name", "Bob"), null);
        assertEquals("request dataset", "id:       44\n" +
                "manager:  testManager\n" +
                "name:     Bob\n" +
                "nickname: \"@fluffy\"\n",
                request.request.toString());
    }

    public void test_start_basics() {
        DataRequest request = new DataRequest("request2", new Dataset());
        assertEquals("not yet started", false, request.started);
        request.start();
        assertEquals("started", true, request.started);
        assertEquals("DataManagerFixture log",
                "testManager started 22",
                DataManagerFixture. getLogs());
    }
    public void test_start_alreadyStarted() {
        DataRequest request = new DataRequest("request2", new Dataset());
        request.start();
        assertEquals("log after first start",
                "testManager started 22",
                DataManagerFixture.getLogs());
        DataManagerFixture.clearLogs();
        request.start();
        assertEquals("log after second start", "",
                DataManagerFixture.getLogs());
    }

    public void test_start_multipleRequests() {
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(new DataRequest(new Dataset("manager", "testManager",
                "id", "0")));
        requests.add(new DataRequest(new Dataset("manager", "t2",
                "id", "1")));
        requests.add(new DataRequest(new Dataset("manager", "testManager",
                "id", "2")));
        requests.add(new DataRequest(new Dataset("manager", "testManager",
                "id", "3")));
        DataRequest.start(requests);
        assertEquals("DataManagerFixture.log",
                "t2 started 1; testManager started 0, 2, 3",
                DataManagerFixture.getLogs());
        assertEquals("request 0 started", true, requests.get(0).started);
        assertEquals("request 1 started", true, requests.get(1).started);
        assertEquals("request 2 started", true, requests.get(2).started);
        assertEquals("request 3 started", true, requests.get(3).started);
    }
    public void test_start_multipleRequests_alreadyStarted() {
        ArrayList<DataRequest> requests = new ArrayList<DataRequest>();
        requests.add(new DataRequest(new Dataset("manager", "testManager",
                "id", "0")));
        requests.add(new DataRequest(new Dataset("manager", "t2",
                "id", "1")));
        requests.add(new DataRequest(new Dataset("manager", "testManager",
                "id", "2")));
        requests.get(1).start();
        requests.get(2).start();
        DataManagerFixture.clearLogs();
        DataRequest.start(requests);
        assertEquals("DataManagerFixture.log", "testManager started 0",
                DataManagerFixture.getLogs());
    }

    public void test_cancel() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "testManager", "id", "44"));
        request.cancel();
        assertEquals("don't cancel: not started", "",
                DataManagerFixture.getLogs());
        request.start();
        request.cancel();
        assertEquals("should cancel",
                "testManager started 44; testManager canceled 44",
                DataManagerFixture.getLogs());
        DataManagerFixture.clearLogs();
        request.setComplete(new Dataset());
        request.cancel();
        assertEquals("don't cancel: already completed", "",
                DataManagerFixture.getLogs());
    }

    public void test_setComplete() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "707"));
        request.start();
        assertEquals("before completion", false, request.completed);
        request.setComplete(new Dataset("name", "Bob", "age", "34"));
        assertEquals("after completion", true, request.completed);
        assertEquals("result dataset", "age:  34\n" +
                "name: Bob\n",
                request.getResponseData().toString());
    }

    public void test_getResponseData_notYetStarted() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "707"));
        DataRequestTestCompleter completer = new DataRequestTestCompleter(
                request, 10, new Dataset("result", "success"), false);
        long start = System.nanoTime();
        completer.start();
        Dataset result = request.getResponseData();
        long stop = System.nanoTime();
        assertEquals("request started", true, request.started);
        assertEquals("request completed", true, request.completed);
        assertEquals("result dataset", "result: success\n",
                result.toString());
        assertEquals("data manager log", "t2 started 707",
                DataManagerFixture.getLogs());
        double delay = (stop - start)/1000000.0;
        if (delay < 10.0) {
            fail(String.format("delay too short: expected 10ms, got %.2fms",
                    delay));
        }
    }
    public void test_getResponseData_alreadyStartedAndCompleted() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "707"));
        request.start();
        request.setComplete(new Dataset("status", "ok"));
        DataManagerFixture.clearLogs();
        Dataset result = request.getResponseData();
        assertEquals("result dataset", "status: ok\n",
                result.toString());
        assertEquals("data manager log", "",
                DataManagerFixture.getLogs());
    }

    public void test_setError() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.start();
        DataManagerFixture.clearLogs();
        DataRequestTestCompleter completer = new DataRequestTestCompleter(
                request, 10, new Dataset("problem", "core dump"), true);
        long start = System.nanoTime();
        completer.start();
        Dataset result = request.getResponseData();
        long stop = System.nanoTime();
        assertEquals("request completed", true, request.completed);
        assertEquals("result dataset", null, result);
        assertEquals("data manager log", "",
                DataManagerFixture.getLogs());
        double delay = (stop - start)/1000000.0;
        if (delay < 10.0) {
            fail(String.format("delay too short: expected 10ms, got %.2fms",
                    delay));
        }
        assertEquals("error dataset", "problem: core dump\n",
                request.getErrorData().toString());
    }

    public void test_getErrorData() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        assertEquals("error data before start", null,
                request.getErrorData());
        request.start();
        assertEquals("error data before completion", null,
                request.getErrorData());
        request.setError(new Dataset("cause", "core dump"));
        assertEquals("error dataset", "cause: core dump\n",
                request.getErrorData().toString());
    }

    public void test_getErrorMessage_noError() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        assertEquals("error message", null, request.getErrorMessage());
    }
    public void test_getErrorMessage_messagePresent() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.setError(new Dataset("message", "detail 47",
                "code", "ENOENT"));
        assertEquals("error message", "detail 47", request.getErrorMessage());
    }
    public void test_getErrorMessage_useCode() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.setError(new Dataset("code", "ENOENT"));
        assertEquals("error message", "error ENOENT",
                request.getErrorMessage());
    }
    public void test_getErrorMessage_noInfoAvailable() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.setError(new Dataset("reason", "earthquake"));
        assertEquals("error message", "unknown error",
                request.getErrorMessage());
    }

    public void test_getDetailedErrorMessage_basics() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.setError(new Dataset("message", "access violation",
                "details", "firstline\nsecond line\nthird",
                "code", "ENOENT", "time", "Thursday\n3:15 P.M."));
        assertEquals("error message", "access violation:\n" +
                "  code:        ENOENT\n" +
                "  details:     firstline\n" +
                "               second line\n" +
                "               third\n" +
                "  time:        Thursday\n" +
                "               3:15 P.M.",
                request.getDetailedErrorMessage());
    }
    public void test_getDetailedErrorMessage_noMessage() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.setError(new Dataset("code", "ENOENT", "time",
                "Thursday\n3:15 P.M."));
        assertEquals("error message", "error:\n" +
                "  code:        ENOENT\n" +
                "  time:        Thursday\n" +
                "               3:15 P.M.",
                request.getDetailedErrorMessage());
    }
    public void test_getDetailedErrorMessage_messageOnly() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.setError(new Dataset("message", "access violation"));
        assertEquals("error message", "access violation",
                request.getDetailedErrorMessage());
    }
    public void test_getDetailedErrorMessage_ignoreNestedDataset() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        request.setError(YamlDataset.newStringInstance(
                "message: access violation\n" +
                "nested:\n" +
                "  value: 48\n" +
                "  value2: 49\n" +
                "details: xyzzy\n"));
        assertEquals("error message", "access violation:\n" +
                "  details:     xyzzy",
                request.getDetailedErrorMessage());
    }

    public void test_getDataManager() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        assertEquals("data manager initially null", null,
                request.dataManager);
        assertEquals("data manager name", "t2 data manager",
                request.getDataManager().toString());
        request.request.delete("manager");
        assertEquals("reuse value once computed", "t2 data manager",
                request.getDataManager().toString());
    }

    public void test_getRequestData() {
        DataRequest request = new DataRequest(new Dataset(
                "manager", "t2", "id", "40"));
        assertEquals("contents of request dataset", "id:      40\n" +
                "manager: t2\n",
                request.getRequestData().toString());
    }

    public void test_makeRequest_findTemplateUsingPath() {
        Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                "demo:\n" +
                "  read:\n" +
                "    manager: testManager\n" +
                "    id: read\n" +
                "  write:\n" +
                "    manager: testManager\n" +
                "    id: write\n"));
        DataRequest request = new DataRequest("demo.read",
                new Dataset());
        assertEquals("request dataset", "id:      read\n" +
                "manager: testManager\n",
                request.request.toString());
    }
    public void test_makeRequest_simpleValues() {
        Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                "request1:\n" +
                "  manager: testManager\n" +
                "  first: first\n" +
                "  second: ouster@electric-cloud\n" +
                "  third: third@@\n"));
        DataRequest request = new DataRequest("request1",
                new Dataset("name", "Bob"));
        assertEquals("request dataset", "first:   first\n" +
                "manager: testManager\n" +
                "second:  \"ouster@electric-cloud\"\n" +
                "third:   \"third@@\"\n",
                request.request.toString());
    }
    public void test_makeRequest_doubleAtSign() {
        Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                "request1:\n" +
                "  manager: testManager\n" +
                "  first: @@x1\n" +
                "  second: @@first\n"));
        DataRequest request = new DataRequest("request1", new Dataset());
        assertEquals("request dataset", "first:   \"@x1\"\n" +
                "manager: testManager\n" +
                "second:  \"@first\"\n",
                request.request.toString());
    }
    public void test_makeRequest_atSignSubstitution() {
        Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                "request1:\n" +
                "  manager: testManager\n" +
                "  first: @x1\n" +
                "  second: @first\n"));
        DataRequest request = new DataRequest("request1",
                new Dataset("x1", "999", "first", "Alice"));
        assertEquals("request dataset", "first:   999\n" +
                "manager: testManager\n" +
                "second:  Alice\n",
                request.request.toString());
    }
}

// The following class is used to signal completion on a request
// after a specifiable delay.
class DataRequestTestCompleter extends Thread {
    protected DataRequest request;
    protected int delayMs;
    protected Dataset result;
    protected boolean error;
    public DataRequestTestCompleter(DataRequest request,
            int delayMs, Dataset result, boolean error) {
        this.request = request;
        this.delayMs = delayMs;
        this.result = result;
        this.error = error;
    }
    public void run() {
        try {
            Thread.sleep(delayMs);
        }
        catch (InterruptedException e) {}
        if (error) {
            request.setError(result);
        } else {
            request.setComplete(result);
        }
    }
}