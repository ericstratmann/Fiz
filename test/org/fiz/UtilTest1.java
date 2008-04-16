package org.fiz;

/**
 * This file defines a class that is used for testing Util.newInstance.
 * See the unit tests in DispatcherTest.java for details.
 */

public class UtilTest1 {
    public Dataset dataset;
    public String string;
    public UtilTest1(Dataset dataset, String string)
            throws java.lang.Exception {
        this.dataset = dataset;
        this.string = string;
        if (string.equals("error")) {
            throw new Exception("test exception message");
        }
    }
}
