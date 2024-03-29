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
import java.io.*;

import org.fiz.test.*;

/**
 * Junit tests for the PathCache class.
 */

public class PathCacheTest extends junit.framework.TestCase {
    public void test_constructor() {
        PathCache p = new PathCache("a/b", "c/d", "e/f");
        assertEquals("saved path", "a/b; c/d; e/f",
                StringUtil.join(p.path, "; "));
    }

    public void test_clearCache() {
        (new File("_test1_/child")).mkdirs();
        TestUtil.writeFile("_test1_/child/sample", "test data");
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");
        p.find("child/sample");
        TestUtil.deleteTree("_test1_");
        p.clearCache();
        boolean gotException = false;
        try {
            // This should fail, since the file has been deleted and
            // the cache has been flushed.
            p.find("child/sample");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find file \"child/sample\" in path " +
                    "(\"_bogus_\", \"_test1_\", \"test1_/child\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_find_searchThroughPath() {
        (new File("_test1_/child")).mkdirs();
        TestUtil.writeFile("_test1_/child/sample", "test data");
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");
        assertEquals("result", "_test1_/child/sample",
                p.find("child/sample"));
        assertEquals("result", "_test1_/child", p.find("child"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_find_useCachedInfo() {
        (new File("_test1_/child")).mkdirs();
        TestUtil.writeFile("_test1_/child/sample", "test data");
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");

        // The next will fail if it tries to actually find the file, since
        // the file has now been deleted.
        assertEquals("result", "_test1_/child/sample",
                p.find("child/sample"));
        TestUtil.deleteTree("_test1_");
    }
    public void test_find_fileNotFound() {
        PathCache p = new PathCache("_bogus_", "_test1_", "test1_/child");
        boolean gotException = false;
        try {
            p.find("xyz");
        }
        catch (FileNotFoundError e) {
            assertEquals("exception message",
                    "couldn't find file \"xyz\" in path (\"_bogus_\", " +
                    "\"_test1_\", \"test1_/child\")",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
