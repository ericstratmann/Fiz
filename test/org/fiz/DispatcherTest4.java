package org.fiz;
import javax.servlet.http.*;

/**
 * This file defines a class that is used for testing Dispatcher.java.
 * This particular class is used for testing the error case where the
 * class doesn't implement the Interactor interface.
 */

public class DispatcherTest4 {
    public DispatcherTest4() {
    }
    public void x(Request request) {
        throw new Error("error in DispatcherTest4.x");
    }
}