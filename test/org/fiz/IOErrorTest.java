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
 * Junit tests for the IOError class.
 */

public class IOErrorTest extends junit.framework.TestCase {
    public void test_IOError() {
        Error e = new IOError("sample message");
        assertEquals("exception message", "sample message",
                e.getMessage());
    }

    public void test_newFileInstance() {
        Error e = IOError.newFileInstance("foo.yaml",
                "foo.yaml (disk exploded)");
        assertEquals("exception message",
                "I/O error in file \"foo.yaml\": disk exploded",
                e.getMessage());
    }
}
