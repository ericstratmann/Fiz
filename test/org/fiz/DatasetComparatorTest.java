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

/**
 * Junit tests for the DatasetComparator class.
 */

public class DatasetComparatorTest extends junit.framework.TestCase {
    public int normalize(int value) {
        if (value == 0) {
            return 0;
        }
        return (value < 0) ? -1 : 1;
    }
    public void test_compare_basics() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.STRING,
                DatasetComparator.Order.INCREASING);
        assertEquals(-1, normalize(c.compare(new Dataset("x", "abc"),
                new Dataset("x", "abd"))));
    }
    public void test_compare_firstValueMissing() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.STRING,
                DatasetComparator.Order.INCREASING);
        assertEquals(0, normalize(c.compare(new Dataset("y", "abc"),
                new Dataset("x", "abd"))));
    }
    public void test_compare_secondValueMissing() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.STRING,
                DatasetComparator.Order.INCREASING);
        assertEquals(0, normalize(c.compare(new Dataset("x", "abc"),
                new Dataset("y", "abd"))));
    }
    public void test_compare_types() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.STRING,
                DatasetComparator.Order.INCREASING);
        assertEquals(-1, normalize(c.compare(new Dataset("x", "0100"),
                new Dataset("x", "99"))));
        assertEquals(0, normalize(c.compare(new Dataset("x", "xyzzy"),
                new Dataset("x", "xyzzy"))));
        assertEquals(1, normalize(c.compare(new Dataset("x", "buss"),
                new Dataset("x", "bug"))));
        c = new DatasetComparator("x",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        assertEquals(-1, normalize(c.compare(new Dataset("x", "abc-12def"),
                new Dataset("x", "  -10  "))));
        assertEquals(0, normalize(c.compare(new Dataset("x", "0"),
                new Dataset("x", "0"))));
        assertEquals(1, normalize(c.compare(new Dataset("x", "2147"),
                new Dataset("x", "1326"))));
        c = new DatasetComparator("x",
                DatasetComparator.Type.FLOAT,
                DatasetComparator.Order.INCREASING);
        assertEquals(-1, normalize(c.compare(new Dataset("x", "3.156"),
                new Dataset("x", "3.157"))));
        assertEquals(0, normalize(c.compare(new Dataset("x", "6.992"),
                new Dataset("x", "6.992"))));
        assertEquals(1, normalize(c.compare(new Dataset("x", "-2.4e14"),
                new Dataset("x", "-2.4e15"))));
    }
    public void test_compare_badFloatingPointSyntax() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.FLOAT,
                DatasetComparator.Order.INCREASING);
        assertEquals(0, normalize(c.compare(new Dataset("x", "2.4e10"),
                new Dataset("x", "2.4ef"))));
        assertEquals(0, normalize(c.compare(new Dataset("x", "1.8x"),
                new Dataset("x", "2.5"))));
    }
    public void test_compare_order() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.DECREASING);
        assertEquals(1, normalize(c.compare(new Dataset("x", "6"),
                new Dataset("x", "7"))));
        assertEquals(0, normalize(c.compare(new Dataset("x", "9"),
                new Dataset("x", "9"))));
        assertEquals(-1, normalize(c.compare(new Dataset("x", "21"),
                new Dataset("x", "20"))));
    }
    public void test_compare_multipleKeys() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "y",
                DatasetComparator.Type.STRING,
                DatasetComparator.Order.INCREASING);
        assertEquals("first key decides", -1,
                normalize(c.compare(new Dataset("x", "6", "y", "bbb"),
                new Dataset("x", "7", "y", "aaa"))));
        assertEquals("first key decides", 1,
                normalize(c.compare(new Dataset("x", "6", "y", "bbb"),
                new Dataset("x", "6", "y", "aaa"))));
        assertEquals("neither key decides", 0,
                normalize(c.compare(new Dataset("x", "6", "y", "bbb"),
                new Dataset("x", "6", "y", "bbb"))));
    }
    public void test_compare_multilevelPath() {
        DatasetComparator c = new DatasetComparator("x.y",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        assertEquals(1, normalize(c.compare(
                YamlDataset.newStringInstance("x:\n    y: 13\n"),
                YamlDataset.newStringInstance("x:\n    y: 12\n"))));
    }

    public void test_equals_wrongType() {
        DatasetComparator c = new DatasetComparator("x",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        assertEquals(false, c.equals(new Dataset()));
    }
    public void test_equals_differentSizes() {
        DatasetComparator c = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        DatasetComparator c2 = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        assertEquals(false, c.equals(c2));
    }
    public void test_equals_differentPaths() {
        DatasetComparator c = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        DatasetComparator c2 = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo2",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        assertEquals(false, c.equals(c2));
    }
    public void test_equals_differentTypes() {
        DatasetComparator c = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        DatasetComparator c2 = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.STRING,
                DatasetComparator.Order.INCREASING);
        assertEquals(false, c.equals(c2));
    }
    public void test_equals_differentOrders() {
        DatasetComparator c = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        DatasetComparator c2 = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.DECREASING);
        assertEquals(false, c.equals(c2));
    }
    public void test_equals_everythingMatches() {
        DatasetComparator c = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        DatasetComparator c2 = new DatasetComparator("foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING, "foo",
                DatasetComparator.Type.INTEGER,
                DatasetComparator.Order.INCREASING);
        assertEquals(true, c.equals(c2));
    }

    public void test_compareIntegers_basics() {
        assertEquals("234 vs. 1234", -1,
                DatasetComparator.compareIntegers("234", "1234"));
        assertEquals("abc -99 vs. foo88xyz", -1,
                DatasetComparator.compareIntegers("abc -99", "foo88xyz"));
        assertEquals("144444 vs. 53535", 1,
                DatasetComparator.compareIntegers("144444", "53535"));
    }
    public void test_compareIntegers_findLastDigits() {
        assertEquals("4abcd vs. 3-+ $", 1,
                DatasetComparator.compareIntegers("4abcd", "3-+ $"));
    }
    public void test_compareIntegers_noDigitsAtAll() {
        assertEquals("-abcd vs. 2", 0,
                DatasetComparator.compareIntegers("-abcd", "2"));
        assertEquals("4 vs. -abcd", 0,
                DatasetComparator.compareIntegers("4", "-abcd"));
    }
    public void test_compareIntegers_basicComparison() {
        assertEquals("2 vs. 3", -1,
                DatasetComparator.compareIntegers("2", "3"));
        assertEquals("3 vs. 3", 0,
                DatasetComparator.compareIntegers("3", "3"));
        assertEquals("4 vs. 3", 1,
                DatasetComparator.compareIntegers("4", "3"));
    }
    public void test_compareIntegers_detectStartOfNumber() {
        assertEquals("fff3ggg vs. eee4ggg", -1,
                DatasetComparator.compareIntegers("fff3ggg", "eee4ggg"));
        assertEquals("fff5ggg vs. eee4ggg", 1,
                DatasetComparator.compareIntegers("fff5ggg", "eee4ggg"));
        assertEquals("3ggg vs. eee4ggg", -1,
                DatasetComparator.compareIntegers("35ggg", "eee44ggg"));
        assertEquals("fff3ggg vs. 4ggg", -1,
                DatasetComparator.compareIntegers("fff35ggg", "44ggg"));
    }
    public void test_compareIntegers_ignoreEqualDigits() {
        assertEquals("22268 vs. 22259", 1,
                DatasetComparator.compareIntegers("22268", "22259"));
        assertEquals("555 vs. 555", 0,
                DatasetComparator.compareIntegers("555", "555"));
    }
    public void test_compareIntegers_signHandling() {
        assertEquals("24 vs. -30", 1,
                DatasetComparator.compareIntegers("24", "-30"));
        assertEquals("-100 vs. 50", -1,
                DatasetComparator.compareIntegers("-100", "50"));
        assertEquals("-100 vs. -200", 1,
                DatasetComparator.compareIntegers("-100", "-200"));
        assertEquals("-5 vs. -5", 0,
                DatasetComparator.compareIntegers("-5", "-5"));
    }
}
