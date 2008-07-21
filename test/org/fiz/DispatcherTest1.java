package org.fiz;
import javax.servlet.http.*;

/**
 * This file defines a class that is used for testing Dispatcher.java.
 * See the unit tests in DispatcherTest.java for details.
 */

public class DispatcherTest1 extends Interactor {
    public static class TestError extends DatasetError
            implements HandledError {
        public TestError(Dataset... errorDatasets) {
            super(errorDatasets);
        }
    }
    public static int count = 0;
    public static int initCount = 0;
    public static int destroyCount = 0;
    public static boolean isAjax = false;
    public void DispatcherTest1() {}
    public void init() {
        initCount++;
    }
    public void destroy() {
        destroyCount++;
    }
    public void ajaxIncCount(ClientRequest cr) {
        count++;
        isAjax = cr.isAjax();
    }
    public void incCount(ClientRequest cr) {
        count++;
        isAjax = cr.isAjax();
    }
    public void resetCount(ClientRequest cr) {
        count = 0;
    }
    public void error(ClientRequest cr) {
        throw new Error("error in method");
    }
    public void handledError(ClientRequest cr) {
        throw new TestError(new Dataset("message", "error handled OK",
                "name", "Alice"));
    }
    private void privateMethod(ClientRequest cr) {
    }
    public void twoArgs(ClientRequest cr, int count) {
    }
    public void wrongArgType(HttpServletRequest request) {
    }
}