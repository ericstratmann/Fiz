/**
 * This file defines a class that is used for testing Dispatcher.java.
 * This particular class is used for testing the case where the
 * class doesn't implement the Interactor interface.
 */

package org.fiz;
import javax.servlet.http.*;

public class DispatcherTest4 {
    public DispatcherTest4() {
    }
    public void x(HttpServletRequest request,
                        HttpServletResponse response) {
        throw new Error("error in DispatcherTest4.x");
    }
}