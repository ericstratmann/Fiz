/**
 * This file defines a class that is used for testing Dispatcher.java.
 * See the unit tests in DispatcherTest.java for details.
 */

package org.fiz;
import javax.servlet.http.*;

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
    public void incCount(Request request) {
        count++;
    }
    public void resetCount(Request request) {
        count = 0;
    }
    public void error(Request request) {
        throw new Error("error in method");
    }
    private void privateMethod(Request request) {
    }
    public void twoArgs(Request request, int count) {
    }
    public void wrongArgType(HttpServletRequest request) {
    }
}