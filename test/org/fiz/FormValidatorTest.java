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
 * Junit tests for the FormValidator class.
 */

public class FormValidatorTest extends junit.framework.TestCase {
    public void test_errorMessage() {
        assertEquals("Default error message",
                "Test error: p_value d_value",
                FormValidator.errorMessage("Test error: @property @data",
                        new Dataset("property", "p_value"),
                        new Dataset("data", "d_value")));
        assertEquals("Custom error message",
                "Custom error: p_value, d_value",
                FormValidator.errorMessage("Test error: @property @data",
                        new Dataset("property", "p_value",
                                "errorMessage", "Custom error: @property, @data"),
                        new Dataset("data", "d_value")));
    }

    public void test_validateDuplicate() {
        assertEquals("value: blue - no match", "blue does not match red",
                FormValidator.validateDuplicate("id", new Dataset("otherFields", "one,two"),
                new Dataset("id", "blue", "one", "blue", "two", "red")));
        assertEquals("value: blue - match", null,
                FormValidator.validateDuplicate("id", new Dataset("otherFields", "one,two"),
                new Dataset("id", "blue", "one", "blue", "two", "blue")));
    }

    public void test_validateIn() {
        assertEquals("value: blue", "Field must match one of the following: one, two, three, four",
                FormValidator.validateIn("id", new Dataset("valid", "one,two,three,four"),
                new Dataset("id", "blue")));
        assertEquals("value: one", null,
                FormValidator.validateIn("id", new Dataset("valid", "one,two,three,four"),
                new Dataset("id", "one")));

        assertEquals("more than 5 values", "Field must match one of the following: a, b, c, d, e, etc.",
                FormValidator.validateIn("id", new Dataset("valid", "a,b,c,d,e,f"),
                new Dataset("id", "blue")));
    }

    public void test_validateInteger() {
        assertEquals("value: 5", null,
                FormValidator.validateInteger("id", new Dataset(),
                new Dataset("id", "5")));
        assertEquals("value: abc", "Must be an integer",
                FormValidator.validateInteger("id", new Dataset(),
                new Dataset("id", "abc")));
        assertEquals("value: 5.5", "Must be an integer",
                FormValidator.validateInteger("id", new Dataset(),
                new Dataset("id", "5.5")));
        assertEquals("value: 12f", "Must be an integer",
                FormValidator.validateInteger("id", new Dataset(),
                new Dataset("id", "12f")));
    }

    public void test_validateLength_min() {
        assertEquals("value: abcd, min: 5", "Must be at least 5 characters long",
                FormValidator.validateLength("id", new Dataset("min", "5"),
                new Dataset("id", "abcd")));
        assertEquals("value: abcde, min: 5", null,
                FormValidator.validateLength("id", new Dataset("min", "5"),
                new Dataset("id", "abcde")));
        assertEquals("value: abcdef, min: 5", null,
                FormValidator.validateLength("id", new Dataset("min", "5"),
                new Dataset("id", "abcdef")));
    }

    public void test_validateLength_max() {
        assertEquals("value: abcd, max: 5", null,
                FormValidator.validateLength("id", new Dataset("max", "5"),
                new Dataset("id", "abcd")));
        assertEquals("value: abcde, max: 5", null,
                FormValidator.validateLength("id", new Dataset("max", "5"),
                new Dataset("id", "abcde")));
        assertEquals("value: abcdef, max: 5", "Must be at most 5 characters long",
                FormValidator.validateLength("id", new Dataset("max", "5"),
                new Dataset("id", "abcdef")));
    }

    public void test_validateLength_minAndMax() {
        assertEquals("value: abcd, min: 5, max: 7", "Must be between 5 and 7 characters long",
                FormValidator.validateLength("id", new Dataset("min", "5", "max", "7"),
                new Dataset("id", "abcd")));
        assertEquals("value: abcde, min: 5, max: 7", null,
                FormValidator.validateLength("id", new Dataset("min", "5", "max", "7"),
                new Dataset("id", "abcde")));
        assertEquals("value: abcdef, min: 5, max: 7", null,
                FormValidator.validateLength("id", new Dataset("min", "5", "max", "7"),
                new Dataset("id", "abcdef")));
        assertEquals("value: abcdefg, min: 5, max: 7", null,
                FormValidator.validateLength("id", new Dataset("min", "5", "max", "7"),
                new Dataset("id", "abcdefg")));
        assertEquals("value: abcdefgh, min: 5, max: 7", "Must be between 5 and 7 characters long",
                FormValidator.validateLength("id", new Dataset("min", "5", "max", "7"),
                new Dataset("id", "abcdefgh")));
        assertEquals("value: abcdefgh, min: 5, max: 5", "Must be exactly 5 characters long",
                FormValidator.validateLength("id", new Dataset("min", "5", "max", "5"),
                new Dataset("id", "abcdefgh")));
    }

    public void test_validateNumeric() {
        assertEquals("value: 5", null,
                FormValidator.validateNumeric("id", new Dataset(),
                new Dataset("id", "5")));
        assertEquals("value: 5.4", null,
                FormValidator.validateNumeric("id", new Dataset(),
                new Dataset("id", "5.4")));
        assertEquals("value: abc", "Must be a number",
                FormValidator.validateNumeric("id", new Dataset(),
                new Dataset("id", "abc")));
        assertEquals("value: 1f", null,
                FormValidator.validateNumeric("id", new Dataset(),
                new Dataset("id", "1f")));
    }

    public void test_validateRange_numeric() {
        assertEquals("value: abc", "Must be a number",
                FormValidator.validateRange("id", new Dataset("min", "10"),
                new Dataset("id", "abc")));
    }

    public void test_validateRange_min() {
        assertEquals("value: 9, min: 10", "Must be >= 10",
                FormValidator.validateRange("id", new Dataset("min", "10"),
                new Dataset("id", "9")));
        assertEquals("value: 10, min: 10", "Must be > 10",
                FormValidator.validateRange("id", new Dataset("min", "10", "includeMin", "false"),
                new Dataset("id", "10")));
        assertEquals("value: 10, min: 10", null,
                FormValidator.validateRange("id", new Dataset("min", "10"),
                new Dataset("id", "10")));
        assertEquals("value: 11, min: 10", null,
                FormValidator.validateRange("id", new Dataset("min", "10"),
                new Dataset("id", "11")));
    }

    public void test_validateRange_max() {
        assertEquals("value: 99, max: 98.5", "Must be <= 98.5",
                FormValidator.validateRange("id", new Dataset("max", "98.5"),
                new Dataset("id", "99")));
        assertEquals("value: 98.5, max: 98.5", null,
                FormValidator.validateRange("id", new Dataset("max", "98.5"),
                new Dataset("id", "98.5")));
        assertEquals("value: 98.5, max: 98.5", "Must be < 98.5",
                FormValidator.validateRange("id", new Dataset("max", "98.5", "includeMax", "false"),
                new Dataset("id", "98.5")));
        assertEquals("value: 97.5, max: 98.5", null,
                FormValidator.validateRange("id", new Dataset("max", "98.5"),
                new Dataset("id", "97.5")));
    }

    public void test_validateRange_minAndMax() {
        assertEquals("value: 97, min: 98, max: 99.2", "Must be >= 98 and <= 99.2",
                FormValidator.validateRange("id", new Dataset("min", "98", "max", "99.2"),
                new Dataset("id", "97")));
        assertEquals("value: 98, min: 98, max: 99.2", null,
                FormValidator.validateRange("id", new Dataset("min", "98", "max", "99.2"),
                new Dataset("id", "98")));
        assertEquals("value: 98.5, min: 98, max: 99.2", null,
                FormValidator.validateRange("id", new Dataset("min", "98", "max", "99.2"),
                new Dataset("id", "98.5")));
        assertEquals("value: 98, min: 98, max: 99.2",
                "Must be > 98 and < 99.2",
                FormValidator.validateRange("id",
                        new Dataset("min", "98", "includeMin", "false",
                                "max", "99.2", "includeMax", "false"),
                new Dataset("id", "98")));
        assertEquals("value: 98, min: 98, max: 99.2",
                "Must be > 98 and < 99.2",
                FormValidator.validateRange("id",
                        new Dataset("min", "98", "includeMin", "false",
                                "max", "99.2", "includeMax", "false"),
                new Dataset("id", "99.2")));
        assertEquals("value: 99.2, min: 98, max: 99.2", null,
                FormValidator.validateRange("id", new Dataset("min", "98", "max", "99.2"),
                new Dataset("id", "99.2")));
        assertEquals("value: 100, min: 98, max: 99.2",
                "Must be >= 98 and <= 99.2",
                FormValidator.validateRange("id", new Dataset("min", "98", "max", "99.2"),
                new Dataset("id", "100")));
    }

    public void test_validateRegex() {
        assertEquals("value: abcd, format: ^[a-z]{4}$", null,
                FormValidator.validateRegex("id", new Dataset("pattern", "^[a-z]{4}$"),
                new Dataset("id", "abcd")));
        assertEquals("value: abcde, format: ^[a-z]{4}$", "Field format incorrect",
                FormValidator.validateRegex("id", new Dataset("pattern", "^[a-z]{4}$"),
                new Dataset("id", "abcde")));
        assertEquals("value: abcd efgh, format: ^[a-z]{4}$", "Field format incorrect",
                FormValidator.validateRegex("id", new Dataset("pattern", "^[a-z]{4}$"),
                new Dataset("id", "abcd efgh")));
    }

    public void test_validateRequired() {
        assertEquals("value: empty", "Required value",
                FormValidator.validateRequired("id", new Dataset(),
                new Dataset("id", "")));
        assertEquals("value: abcde", null,
                FormValidator.validateRequired("id", new Dataset(),
                new Dataset("id", "abcde")));
    }
}
