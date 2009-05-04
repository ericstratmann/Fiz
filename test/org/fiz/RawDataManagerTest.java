package org.fiz;

/**
 * Junit tests for the RawDataManager class.
 */

public class RawDataManagerTest extends junit.framework.TestCase {
    public void test_newRequest() {
        DataRequest request = RawDataManager.newRequest(
                new Dataset("first", "123", "second", "456"));
        assertEquals("response dataset", "first:  123\n" +
                "second: 456\n",
                request.getResponseOrAbort().toString());
    }

    public void test_newError() {
        DataRequest request = RawDataManager.newError(
                new Dataset("message", "first error"),
                new Dataset("message", "second error"));
        assertEquals("error information", "first error\n" +
                "second error",
                request.getErrorMessage());
    }
}
