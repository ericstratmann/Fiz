/* Copyright (c) 2008-2010 Stanford University
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

public class LazyDatasetTest extends junit.framework.TestCase {
    protected static class DataSourceImp implements LazyDataset.DataSource {
        public Object getData(String key, LazyDataset data) {
            data.set("abc", "xyz");
            return key + "!!";
        }
    }

    protected ClientRequest cr;
    protected LazyDataset lds;
    protected DataSourceImp source;
    public void setUp() {
        cr = new ClientRequestFixture();
        lds = new LazyDataset();
        source = new DataSourceImp();
    }

    public void test_addDataSource() {
        lds.addDataSource("foo", source);
        LazyDataset.DataSource stored = ((LazyDataset.DataSourceContainer)
                   lds.map.get("foo")).dataSource;
        assertEquals("normal source", source, stored);
    }

    public void test_setDataSource() {
        lds.setDataSource("foo", source);
        LazyDataset.DataSource stored = ((LazyDataset.DataSourceContainer)
                   lds.map.get("foo")).dataSource;
        assertEquals("normal source", source, stored);

        lds.setDataSource(null, source);
        stored = lds.defaultDataSourceContainer.dataSource;
        assertEquals("global source", source, stored);
    }

    public void test_deleteDefaultDataSource() {
        lds.addDataSource(null, source);
        lds.deleteDefaultDataSource();
        assertEquals(null, lds.defaultDataSourceContainer);
    }

    public void test_lookup_returnNull() {
        assertEquals(null, lds.lookup("bogus", Dataset.Quantity.FIRST_ONLY));
    }

    public void test_lookup_defaultDataSource_first() {
        lds.setDataSource(null, source);
        assertEquals("bogus!!", lds.getString("bogus"));
    }

    public void test_lookup_defaultDataSource_all() {
        lds.setDataSource(null, source);
        assertEquals("[bogus!!]", lds.getStringList("bogus").toString());
    }

    public void test_lookup_getAll() {
        lds.add("a", "b");
        lds.addDataSource("a", source);
        assertEquals("[b, a!!]", lds.getStringList("a").toString());
    }

    public void test_lookup_getFirst() {
        lds.add("f", "g");
        lds.addDataSource("f", source);
        assertEquals("regular value", "g", lds.getString("f"));

        lds.addDataSource("h", source);
        lds.add("h", "i");
        assertEquals("lazy value", "h!!", lds.getString("h").toString());
    }
}
