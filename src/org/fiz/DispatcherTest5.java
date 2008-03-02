/**
 * This file defines a class that is used for testing the destroy callbacks
 * made by Dispatcher.java.
 */

package org.fiz;
import javax.servlet.http.*;

public class DispatcherTest5 extends Interactor {
    public static int destroyCount= 0;
    public DispatcherTest5() {
    }
    public void destroy() {
        destroyCount += 3;
    }
    public void x(HttpServletRequest request,
                        HttpServletResponse response) {
        throw new Error("error in DispatcherTest4.x");
    }
}