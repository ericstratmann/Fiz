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
 * Junit tests for the Config class.
 */

public class ConfigTest extends junit.framework.TestCase {
    public void test_init() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        TestUtil.writeFile("_test1_/main.yaml", "directory: _test1_\n");
        TestUtil.writeFile("_test1_/child/main.yaml",
                "directory: _test1_/child\n");
        Config.init("_test1_");
        Dataset d = Config.getDataset("main");
        assertEquals("dataset in _test1_", "_test1_", d.checkString("directory"));
        Config.init("_test1_/child");
        d = Config.getDataset("main");
        assertEquals("dataset in _test1_/child", "_test1_/child",
                d.checkString("directory"));
        TestUtil.deleteTree("_test1_");
    }

    public void test_clearCache() {
        Config.init(".");
        TestUtil.writeFile("main.yaml",
                "name1: value1\nname2: value2\n");
        Config.get("main", "name1");
        assertEquals("size of cache after reading a value",
                1, Config.cache.size());
        Config.clearCache();
        assertEquals("size of cache after clearing",
                0, Config.cache.size());
    }

    public void test_get() {
        Config.init(".");
        TestUtil.writeFile("main.yaml",
                "name1: value1\nname2: value2\n");
        assertEquals("value1", Config.get("main", "name1"));
        TestUtil.deleteTree("main.yaml");
    }

    public void test_getPath() {
        Config.init(".");
        TestUtil.writeFile("main.yaml",
                "child:\n  name: Alice\n  age: 24\n");
        assertEquals("Alice", Config.get("main", "child.name"));
        TestUtil.deleteTree("main.yaml");
    }

    public void test_getDataset() {
        (new File("_test1_")).mkdir();
        (new File("_test1_/child")).mkdir();
        Config.init("_test1_", "_test1_/child");
        TestUtil.writeFile("_test1_/child/main.yaml",
                "name1: value1\nname2: value2\n");
        Dataset d = Config.getDataset("main");
        assertEquals("value from first call (uncached)", "value2",
                d.checkString("name2"));
        TestUtil.deleteTree("_test1_");
        d = Config.getDataset("main");
        assertEquals("value from second call (cached)", "value1",
                d.checkString("name1"));
    }

    public void test_setDataset() {
        (new File("_test1_")).mkdir();
        Config.init("_test1_");
        TestUtil.writeFile("_test1_/main.yaml",
                "name1: value1\nname2: value2\n");
        Dataset d = Config.getDataset("main");
        assertEquals("value from first call (file from disk)", "value1",
                d.checkString("name1"));
        Config.setDataset("main", new Dataset("name1", "xyzzy"));
        d = Config.getDataset("main");
        assertEquals("value from second call (overridden with setDataset)",
                "xyzzy", d.checkString("name1"));
        TestUtil.deleteTree("_test1_");

    }
    public void test_getSearchPath() {
        Config.init("a/b/c", "x/y");
        String[] path = Config.getSearchPath();
        assertEquals("length of path", 2, path.length);
        assertEquals("first directory in path", "a/b/c", path[0]);
        assertEquals("second directory in path", "x/y", path[1]);
        path[0] = "modified";
        assertEquals("return value is cloned", "a/b/c", Config.getSearchPath()[0]);
    }
}
