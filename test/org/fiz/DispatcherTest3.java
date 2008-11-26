package org.fiz;

/**
 * This file defines a class that is used for testing Dispatcher.java.
 * See the unit tests in DispatcherTest.java for details.
 */
public class DispatcherTest3 implements DirectAjax {
    public static String log = "";

    public static void ajaxRequest(ClientRequest cr) {
        log = "Invoked ajaxRequest";
    }

    public static void ajaxExtraArgument(ClientRequest cr, String s) {
        log = "invoked ajaxExtraArgument";
    }

    public static void ajaxBadSignature(String s) {
        log = "invoked ajaxBadSignature";
    }

    public  void ajaxNotStatic(ClientRequest cr) {
        log = "Invoked ajaxNotStatic";
    }
}
