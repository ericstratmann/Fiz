package org.fiz;
import javax.servlet.http.*;

/**
 * This file defines a class that is used for testing Dispatcher.java.
 * See the unit tests in DispatcherTest.java for details.
 */

public class DispatcherTest1 extends Interactor {
    public static int count = 0;
    public static int initCount = 0;
    public static int destroyCount = 0;
    public void DispatcherTest1() {}
    public void init() {
        initCount++;
    }
    public void destroy() {
        destroyCount++;
    }
    public void incCount(ClientRequest request) {
        count++;
    }
    public void resetCount(ClientRequest request) {
        count = 0;
    }
    public void error(ClientRequest request) {
        throw new Error("error in method");
    }
    private void privateMethod(ClientRequest request) {
    }
    public void twoArgs(ClientRequest request, int count) {
    }
    public void wrongArgType(HttpServletRequest request) {
    }
}