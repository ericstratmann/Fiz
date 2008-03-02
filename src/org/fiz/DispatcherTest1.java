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
    public void incCount(HttpServletRequest request,
                        HttpServletResponse response) {
        count++;
    }
    public void resetCount(HttpServletRequest request,
                        HttpServletResponse response) {
        count = 0;
    }
    public void error(HttpServletRequest request,
                        HttpServletResponse response) {
        throw new Error("error in method");
    }
    private void privateMethod(HttpServletRequest request,
                        HttpServletResponse response) {
    }
    public void oneArg(HttpServletRequest request) {
    }
    public void firstArgWrongType(int a, HttpServletResponse response) {
    }
    public void secondArgWrongType(HttpServletRequest request, int b) {
    }
}