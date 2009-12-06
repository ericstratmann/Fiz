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
 * Junit tests for the FileNotFoundError class.
 */

public class FileNotFoundErrorTest extends junit.framework.TestCase {
    public void test_FileNotFoundError_nameAndMessage() {
        Error e = new FileNotFoundError("foo.txt", null,
                "foo.txt (sample message)");
        assertEquals("exception message without type",
                "couldn't open file \"foo.txt\": sample message",
                e.getMessage());
        e = new FileNotFoundError("foo.txt", "dataset",
                "sample message");
        assertEquals("exception message with type",
                "couldn't open dataset file \"foo.txt\": sample message",
                e.getMessage());
    }
    public void test_FileNotFoundError_messageOnly() {
        Error e = new FileNotFoundError("foo.txt (sample message)");
        assertEquals("exception message", "foo.txt (sample message)",
                e.getMessage());
    }

    public void test_newPathInstance() {
        Error e = FileNotFoundError.newPathInstance("foo.yaml", null,
                new String[] {"/bin", "/usr/local/tools", "C:/code"});
        assertEquals("exception message without type",
                "couldn't find file \"foo.yaml\" in path (\"/bin\", "
                + "\"/usr/local/tools\", \"C:/code\")",
                e.getMessage());
        e = FileNotFoundError.newPathInstance("foo.yaml", "image",
                new String[] {"/bin", "/usr/local/tools", "C:/code"});
        assertEquals("exception message without type",
                "couldn't find image file \"foo.yaml\" in path (\"/bin\", "
                + "\"/usr/local/tools\", \"C:/code\")",
                e.getMessage());
    }
}