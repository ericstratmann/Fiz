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

import java.util.*;

/**
 * A LazyDataset can contain values that are not computed until needed.
 * Instead of adding a value directly to the dataset, a {@code DataSource}
 * can be added through the {@code addDataSource} and {@code setDataSource}
 * methods. If a value is requested that has been added through one of those
 * methods, instead of returning the DataSource object, its {@code getData}
 * method is invoked, which will return the value to return. DataSources may
 * modify the LazyDataset, such as to replace the DataSource with a cached
 * value, or to add multiple values to the Dataset at once. DataSources can
 * be overwritten or deleted like any other value -- there is no special
 * behavior in these cases.
 *
 * LazyDatasets also support a default DataSource, which is used for any keys
 * that do not exist in the Dataset, through {@code setDataSource}.
 *
 * Otherwise, LazyDatasets act identically to regular Datasets.
 */
public class LazyDataset extends Dataset {
    /**
     * The DataSource interface should be implemented whenever lazy data is
     * needed. When a lookup is performed on a DataSource added through the
     * {@code setDataSource} and {@code addDataSource} maethods, instead of
     * being returned, its {@code getData} method is invoked.
     */
    public static interface DataSource {
        /**
         * Returns the data associated with the given {@code key}. This
         * method may modify the {@code dataset}.
         * @param key         Key requested in the LazyDataset, such as a
         *                    call to dataset.get("foo").
         * @param dataset     The LazyDataset itself. The DataSource may
         *                    modify the LazyDataset, such as to replace itself
         *                    with a cached value, or to set multiple values in
         *                    the LazyDataset.
         * @return            Value associated with {@code key}
         */
        public abstract Object getData(String key, LazyDataset dataset);
    }

    /**
     * Used internally to store DataSources that have been added through
     * methods such as {@code setDataSource}. DataSources which were added
     * through the normal Dataset methods will be stored as plain DataSources.
     * If a key is requested with a DataSource as a value, the DataSource will
     * be returned. If the value is a DataSourceContainer, then the
     * {@code getData} method of the DataSource it contains is called and that
     * data is returned.
     */
    protected static class DataSourceContainer {
        public DataSource dataSource;

        /**
         * Constructs a new DataSourceContainer
         * @param dataSource  DataSource passed to a method such as
         *                    {@code addDataSource}.
         */
        public DataSourceContainer(DataSource dataSource) {
            this.dataSource = dataSource;
        }
    }

    // "Catch-all" data source for missing keys.
    protected DataSourceContainer defaultDataSourceContainer;

    /**
     * Constructs an empty LazyDataset
     */
    public LazyDataset() {}

    /**
     * Constructs a new LazyDataset, populated with {@code keysAndValues}.
     * @param keysAndValues   An even number of argument objects;
     *                        the first argument of each pair must be
     *                        a string key and the second argument of
     *                        each pair must be a value for that key.
     *                        To create multiple nested values with the
     *                        same name, use multiple key/value pairs
     *                        with the same key. {@code DataSources} should not
     *                        be added here or they will simply be returned
     *                        as-is (as opposed to their {@code getData} method
     *                        being invoked).
     */
    public LazyDataset(Object...keysAndValues) {
        super(keysAndValues);
    }

    /**
     * Adds a DataSource with the given {@code key}, which will be called
     * whenever a value is requested for the key. If there already exist one
     * or more values with the same  key, then the new value is added to them
     * to form a list, with the new value at the end of the list. DataSources
     * should be added through this method or {@code addDataSource}, not
     * through the normal {@code set} and {@code add} methods, otherwise they
     * will be treated as any other object.
     * @param key             Name of a value in the top level of the dataset,
     *                        or null to set the default DataSource.
     * @param source          Object which will compute the requested value
     *                        when invoked.
     */
    public void addDataSource(String key, DataSource source) {
        DataSourceContainer container = new DataSourceContainer(source);
        add(key, container);
    }

    /**
     * Associates a DataSource with the given {@code key}, replacing any existing
     * values, which will be called whenever a value is requested for the key.
     * If {@code key} is null, {@code source} is used as the default DataSource
     * for all non-existing keys. DataSources should be added through this
     * method or {@code addDataSource}, not through the normal {@code set} and
     * {@code add} methods, otherwise they will be treated as any other object.
     * @param key             Name of a value in the top level of the dataset.
     * @param source          Object which will compute the requested value
     *                        when invoked.
     */
    public void setDataSource(String key, DataSource source) {
        DataSourceContainer container = new DataSourceContainer(source);
        if (key == null) {
            defaultDataSourceContainer = container;
        } else {
            set(key, container);
        }
    }

    /**
     * Deletes the default DataSource, if it exists. Future requests for keys
     * that do not exist will return null or throw errors unless another default
     * DataSource is added. Regular DataSources can be deleted using the normal
     * Dataset mechanisms (such as the {@code delete} method).
     */
    public void deleteDefaultDataSource() {
        defaultDataSourceContainer = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object lookup(String key, Quantity quantity) {
        Object value = super.lookup(key, quantity);
        if (value instanceof DSArrayList) {
            if (((ArrayList) value).size() == 0) {
                if (defaultDataSourceContainer != null) {
                    ((ArrayList) value).add(defaultDataSourceContainer);
                }
            }
        } else if (value == null) {
            if (defaultDataSourceContainer != null) {
                value = defaultDataSourceContainer;
            } else {
                return null;
            }
        }

        if (quantity == Quantity.ALL) {
            DSArrayList<Object> list = (DSArrayList<Object>)
                ((DSArrayList<Object>) value).clone();
            for (int i = 0; i < list.size(); i++) {
                Object elem = list.get(i);
                if (elem instanceof DataSourceContainer) {
                    DataSourceContainer dsc = (DataSourceContainer) elem;
                    Object data = dsc.dataSource.getData(key, this);
                    list.set(i, data);
                }
            }
            return list;
        } else if (quantity == Quantity.FIRST_ONLY) {
            if (value instanceof DataSourceContainer) {
                DataSourceContainer dsc = (DataSourceContainer) value;
                return dsc.dataSource.getData(key, this);
            } else {
                return value;
            }
        } else {
            throw new InternalError("LazyDataset.lookup: couldn't recognize " +
                                    "quantity: " + quantity);
        }
    }
}
