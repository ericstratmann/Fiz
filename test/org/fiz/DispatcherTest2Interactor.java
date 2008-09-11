package org.fiz;

/**
 * This file defines a class that is used for testing the destroy callbacks
 * made by Dispatcher.java.
 */

public class DispatcherTest2Interactor extends Interactor {
    public static int destroyCount= 0;
    public DispatcherTest2Interactor() {
    }
    public void destroy() {
        destroyCount += 3;
    }
    public void x(ClientRequest cr) {
        cr.getHtml().getBody().append("<p>Sample text.</p>\n");
        throw new Error("error in DispatcherTest2.x");
    }
}