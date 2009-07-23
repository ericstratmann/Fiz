/* Copyright (c) 2009 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fiz;
import javax.servlet.http.*;

/**
 * This file defines a class that is used for testing Dispatcher.java.
 * See the unit tests in DispatcherTest.java for details.
 */

public class DispatcherTest1Interactor extends Interactor {
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
    public static ClientRequest.Type requestType =
            ClientRequest.Type.NORMAL;
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
    public void ajaxUserError(ClientRequest cr) {
        throw new UserError("ajax user error");
    }
    public void incCount(ClientRequest cr) {
        count++;
        isAjax = cr.isAjax();
    }
    public void postTest(ClientRequest cr) {
        requestType = cr.getClientRequestType();
    }
    public void postUserError(ClientRequest cr) {
        throw new UserError("post user error");
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
    public void userError(ClientRequest cr) {
        throw new UserError("test user error");
    }
    private void privateMethod(ClientRequest cr) {
    }
    public void twoArgs(ClientRequest cr, int count) {
    }
    public void wrongArgType(HttpServletRequest request) {
    }
}