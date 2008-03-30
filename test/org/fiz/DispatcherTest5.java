package org.fiz;

/**
 * This file defines a class that is used for testing the destroy callbacks
 * made by Dispatcher.java.
 */

public class DispatcherTest5 extends Interactor {
    public static int destroyCount= 0;
    public DispatcherTest5() {
    }
    public void destroy() {
        destroyCount += 3;
    }
    public void x(ClientRequest request) {
        throw new Error("error in DispatcherTest4.x");
    }
}