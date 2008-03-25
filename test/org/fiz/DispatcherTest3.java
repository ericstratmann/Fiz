/**
 * This file defines a class that is used for testing Dispatcher.java.
 * This particular class is used for testing the error case where the
 * constructor fails with an exception.
 */

package org.fiz;
import javax.servlet.http.*;

public class DispatcherTest3 extends Interactor {
    public DispatcherTest3() {
        throw new Error("sample error");
    }
}