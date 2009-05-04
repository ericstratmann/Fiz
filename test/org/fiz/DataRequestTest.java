package org.fiz;
import java.util.*;

/**
 * Junit tests for the DataRequest class.
 */

public class DataRequestTest extends junit.framework.TestCase {
    // The following class is used to signal completion of a request
    // after a specifiable delay.
    protected static class DataRequestTestCompleter extends Thread {
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

    public void setUp() {
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

    public void test_RequestError() {
        DataRequest.RequestError e = new DataRequest.RequestError(
                "test message");
        assertEquals("exception message", "test message", e.getMessage());
    }

    public void test_constructor() {
        DataRequest request = new DataRequest("foo.bar");
        assertEquals("request name", "foo.bar", request.name);
    }

    // No tests for "cancel" method: it doesn't do anything.

    public void test_setComplete() {
        DataRequest request = new DataRequest("foo.bar");
        assertEquals("before completion", false, request.completed);
        request.setComplete(new Dataset("name", "Bob", "age", "34"));
        assertEquals("after completion", true, request.completed);
        assertEquals("result dataset", "age:  34\n" +
                "name: Bob\n",
                request.getResponseData().toString());
    }

    public void test_setComplete_noResponse() {
        DataRequest request = new DataRequest("foo.bar");
        request.setComplete();
        assertEquals("after completion", true, request.completed);
        assertEquals("result dataset", "",
                request.getResponseData().toString());
    }

    public void test_isComplete() {
        DataRequest request = new DataRequest("foo.bar");
        assertEquals("request not yet finished", false, request.isComplete());
        request.setComplete(new Dataset("status", "ok"));
        assertEquals("request finished", true, request.isComplete());
    }

    public void test_getResponseData_notYetCompleted() {
        DataRequest request = new DataRequest("foo.bar");
        DataRequestTestCompleter completer = new DataRequestTestCompleter(
                request, 10, new Dataset("result", "success"), false);
        long start = System.nanoTime();
        completer.start();
        Dataset result = request.getResponseData();
        long stop = System.nanoTime();
        assertEquals("request completed", true, request.completed);
        assertEquals("result dataset", "result: success\n",
                result.toString());
        double delay = (stop - start)/1000000.0;
        if (delay < 10.0) {
            fail(String.format("delay too short: expected 10ms, got %.2fms",
                    delay));
        }
    }
    public void test_getResponseData_alreadyCompleted() {
        DataRequest request = new DataRequest("foo.bar");
        request.setComplete(new Dataset("status", "ok"));
        Dataset result = request.getResponseData();
        assertEquals("result dataset", "status: ok\n",
                result.toString());
    }

    public void test_getResponseOrAbort_success() {
        DataRequest request = new DataRequest("foo.bar");
        request.setComplete(new Dataset("status", "ok"));
        Dataset response = request.getResponseOrAbort();
        assertEquals("result dataset", "status: ok\n",
                response.toString());
    }
    public void test_getResponseOrAbort_error() {
        DataRequest request = new DataRequest("foo.bar");
        request.setError(new Dataset("message", "test message"));
        boolean gotException = false;
        try {
            request.getResponseOrAbort();
        }
        catch (DataRequest.RequestError e) {
            assertEquals("exception message",
                    "error in data request foo.bar: test message",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_setError_synchronizeProperly() {
        DataRequest request = new DataRequest("foo.bar");
        DataRequestTestCompleter completer = new DataRequestTestCompleter(
                request, 10, new Dataset("problem", "core dump"), true);
        long start = System.nanoTime();
        completer.start();
        Dataset result = request.getResponseData();
        long stop = System.nanoTime();
        assertEquals("request completed", true, request.completed);
        assertEquals("result dataset", null, result);
        double delay = (stop - start)/1000000.0;
        if (delay < 10.0) {
            fail(String.format("delay too short: expected 10ms, got %.2fms",
                    delay));
        }
        assertEquals("error dataset", "problem: core dump\n",
                request.getErrorData()[0].toString());
    }
    public void test_setError_multipleDatasets() {
        DataRequest request = new DataRequest("foo.bar");
        request.setError(new Dataset("message", "core dump", "code", "ENOENT"),
                new Dataset("message", "error 33"));
        assertEquals("count of error datasets", 2,
                request.getErrorData().length);
        assertEquals("first error dataset", "code:    ENOENT\n" +
                "message: core dump\n",
                request.getErrorData()[0].toString());
        assertEquals("second error dataset", "message: error 33\n",
                request.getErrorData()[1].toString());
    }
    public void test_setError_withArrayList() {
        DataRequest request = new DataRequest("foo.bar");
        ArrayList<Dataset> errors = new ArrayList<Dataset>();
        errors.add(new Dataset("message", "core dump", "code", "ENOENT"));
        errors.add(new Dataset("message", "error 33"));
        errors.add(new Dataset("message", "error 44"));
        request.setError(errors);
        assertEquals("count of error datasets", 3,
                request.getErrorData().length);
        assertEquals("first error dataset", "code:    ENOENT\n" +
                "message: core dump\n",
                request.getErrorData()[0].toString());
        assertEquals("second error dataset", "message: error 33\n",
                request.getErrorData()[1].toString());
        assertEquals("third error dataset", "message: error 44\n",
                request.getErrorData()[2].toString());
    }

    public void test_getErrorData() {
        DataRequest request = new DataRequest("foo.bar");
        request.setError(new Dataset("cause", "core dump"));
        assertEquals("error dataset", "cause: core dump\n",
                request.getErrorData()[0].toString());
    }
    public void test_getErrorData_multipleErrors() {
        DataRequest request = new DataRequest("foo.bar");
        request.setError(new Dataset("message", "first error"),
                new Dataset("message", "second error"),
                new Dataset("message", "third error"));
        Dataset[] errors = request.getErrorData();
        assertEquals("number of errors", 3,
                errors.length);
        assertEquals("first dataset", "message: first error\n",
                errors[0].toString());
        assertEquals("second dataset", "message: second error\n",
                errors[1].toString());
        assertEquals("third dataset", "message: third error\n",
                errors[2].toString());
    }

    public void test_getErrorMessage_noError() {
        DataRequest request = new DataRequest("foo.bar");
        DataRequestTestCompleter completer = new DataRequestTestCompleter(
                request, 1, new Dataset("x", "1"), false);
        completer.start();
        assertEquals("error message", null, request.getErrorMessage());
    }
    public void test_getErrorMessage_oneError() {
        DataRequest request = new DataRequest("foo.bar");
        request.setError(new Dataset("message", "detail 47"));
        assertEquals("error message", "detail 47",
                request.getErrorMessage());
    }

    public void test_getDetailedErrorMessage_noError() {
        DataRequest request = new DataRequest("foo.bar");
        DataRequestTestCompleter completer = new DataRequestTestCompleter(
                request, 1, new Dataset("x", "1"), false);
        completer.start();
        assertEquals("error message", null, request.getDetailedErrorMessage());
    }
    public void test_getDetailedErrorMessage_oneError() {
        DataRequest request = new DataRequest("foo.bar");
        request.setError(new Dataset(
                "message", "access violation",
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

    public void test_throwError() {
        DataRequest request = new DataRequest("foo.bar");
        request.setError(new Dataset("message", "test message",
                "culprit", "field #1"));
        boolean gotException = false;
        try {
            request.throwError();
        }
        catch (DataRequest.RequestError e) {
            assertEquals("exception message",
                    "error in data request foo.bar: test message:\n" +
                            "  culprit:     field #1",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_throwError_noName() {
        DataRequest request = new DataRequest();
        request.setError(new Dataset("message", "test message",
                "culprit", "field #1"));
        boolean gotException = false;
        try {
            request.throwError();
        }
        catch (DataRequest.RequestError e) {
            assertEquals("exception message",
                    "error in data request: test message:\n" +
                            "  culprit:     field #1",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_setAndGetName() {
        DataRequest request = new DataRequest("foo.bar");
        request.setName("xyzzy");
        assertEquals("new name", "xyzzy", request.getName());
    }
}