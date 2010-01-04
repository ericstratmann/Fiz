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

import org.fiz.test.*;

/**
 * Junit tests for the Convert class
 */

public class ConvertTest extends junit.framework.TestCase {

    protected Convert.Success success = new Convert.Success();

    public void setUp() {
        success.setSuccess(false);
    }

    public void test_toInt() {
        assertEquals("int", 5, Convert.toInt(5, success));
        assertEquals("success", true, success.succeeded());
        assertEquals("double", 4, Convert.toInt(4.2, success));
        assertEquals("bool, true", 1, Convert.toInt(true, success));
        assertEquals("bool, false", 0, Convert.toInt(false, success));
        assertEquals("string", 44, Convert.toInt("44.3", success));

        Convert.toInt("abc", success);
        assertEquals("bad string", false, success.succeeded());
        Convert.toInt(new Dataset(), success);
        assertEquals("bad type", false, success.succeeded());
    }

    private final double DOUBLE_DELTA = 0.000000000001;
    public void test_toDouble() {
        assertEquals("int", 5, Convert.toDouble(5, success), DOUBLE_DELTA);
        assertEquals("success", true, success.succeeded());
        assertEquals("double", 4.12345678901,
                     Convert.toDouble(4.12345678901, success),
                     DOUBLE_DELTA);
        assertEquals("bool, true", 1, Convert.toDouble(true, success),
                     DOUBLE_DELTA);
        assertEquals("bool, false", 0, Convert.toDouble(false, success),
                     DOUBLE_DELTA);
        assertEquals("string", 44.325, Convert.toDouble("44.325", success),
                     DOUBLE_DELTA);

        Convert.toDouble("abc", success);
        assertEquals("bad string", false, success.succeeded());
        Convert.toDouble(new Dataset(), success);
        assertEquals("bad type", false, success.succeeded());
    }

    public void test_toBool() {
        assertEquals("int non-zero", true, Convert.toBool(5, success));
        assertEquals("success", true, success.succeeded());
        assertEquals("int zero", false, Convert.toBool(0, success));
        assertEquals("double", true, Convert.toBool(4.12, success));
        assertEquals("bool, true", true, Convert.toBool(true, success));
        assertEquals("bool, false", false, Convert.toBool(false, success));
        assertEquals("string value true", true, Convert.toBool("true", success));
        assertEquals("string value 1", true, Convert.toBool("1", success));
        assertEquals("string value other", false, Convert.toBool("cat", success));

        Convert.toBool(new Dataset(), success);
        assertEquals("bad type", false, success.succeeded());
    }

    public void test_String() {
        assertEquals("int", "5", Convert.toString(5, success));
        assertEquals("success", true, success.succeeded());
        assertEquals("double", "6.3999999999999995",
                     Convert.toString(5.1 + 1.3, success));
        assertEquals("bool, true", "true", Convert.toString(true, success));
        assertEquals("bool, false", "false", Convert.toString(false, success));
        assertEquals("string", "cat", Convert.toString("cat", success));
        assertEquals("dataset", "a: x\nb: y\n",
                     Convert.toString(new Dataset("a", "x", "b", "y"), success));
    }

    public void test_Dataset() {
        Dataset ds = new Dataset("hi", "bye");
        assertEquals("dataset", ds, Convert.toDataset(ds, success));
        assertEquals("success", true, success.succeeded());

        Convert.toDataset(new Integer(5), success);
        assertEquals("bad type", false, success.succeeded());
    }
}
