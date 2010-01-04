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

package org.fiz.datamanager;

import org.fiz.*;
import java.util.*;

/**
 * FileDataManager is a Fiz data manager that provides access to
 * files whose contents are datasets.  It is intended primarily for
 * testing, but may have other uses also.
 */
public class FileDataManager {
    // The following object contains a collection of directory names
    // in which we will search for files.
    protected String[] path;

    // The following hash table maps from a file name to the
    // main-memory-cached copy of the corresponding dataset.
    protected HashMap<String,Dataset> datasetCache
            = new HashMap<String,Dataset>();

    /**
     * Construct a FileDataManager that will look for files using a
     * given path.
     * @param path                 Each argument is the name of a
     *                             directory.  When looking up files,
     *                             the file is looked up in each of these
     *                             directories in order (first existing
     *                             file wins).  If no directories are
     *                             provided the path defaults to the
     *                             WEB-INF directory in the application's
     *                             deployment.
     */
    public FileDataManager(String ... path) {
        this.path = path;
        if (this.path.length == 0) {
            this.path = new String[1];
            this.path[0] = Config.get("main", "home") + "/WEB-INF";
        }
    }

    /**
     * Flushes all datasets that have been cached in memory, so that they
     * will be reread from disk the next time they are needed.
     * TODO: add mechanism for clearing caches in data managers.
     */
    public void clearCache() {
        datasetCache.clear();
    }

    /**
     * Create a new dataset in a file, or overwrite an existing dataset
     * if it already exists.
     * @param file                 Name of the file containing the dataset.
     * @param path                 Path of the desired dataset within the file.
     * @param values               New contents for the dataset given by
     *                             {@code file} and {@code path}.
     * @return                     Null if the create was successful and a
     *                             dataset with errors otherwise.
     */
    public Dataset create(String file, String path, Dataset values) {
        Dataset root = loadDataset(file);
        root.setPath(path, values.clone());
        root.writeFile(root.getFileName(), null);
        return null;
    }

    /**
     * Delete a dataset in a file.
     * @param file                 Name of the file containing the dataset.
     * @param path                 Path of the desired dataset within the
     *                             file; null means use the top-level
     *                             dataset in the file.
     * @return                     Null if the delete was successful and a
     *                             dataset with errors otherwise.
     */
    public Dataset delete(String file, String path) {
        Dataset d = loadDataset(file);
        if (path != null) {
            d.delete(path);
        } else {
            d.clear();
        }
        d.writeFile(d.getFileName(), null);

        return null;
    }

    /**
     * Read a nested dataset from a file.
     * @param file                 Name of the file containing the dataset.
     * @param path                 Path of the desired dataset within the
     *                             file; null means use the top-level
     *                             dataset in the file.
     * @return                     The dataset associated with {@code path},
     *                             or null if there is no such dataset.
     */
    public Dataset read(String file, String path) {
        Dataset d = loadDataset(file);
        return findNestedDataset(d, path);
    }

    /**
     * Replace one or more values within an existing dataset in a file.
     * This method differs from {@code create} in that existing values
     * in the target dataset are retained unless overwritten by entries
     * in the {@code values} argument.
     * @param file                 Name of the file containing the dataset.
     * @param path                 Path of the desired dataset within the file.
     *                             Null refers to the top-level dataset.
     * @param values               Each of the values in this dataset will be
     *                             copied to the dataset given by {@code file}
     *                             and {@code path}, replacing any existing
     *                             values by the same names.
     * @return                     Null if the update was successful and a
     *                             dataset with errors otherwise.
     */
    public Dataset update(String file, String path, Dataset values) {
        Dataset root = loadDataset(file);
        Dataset target = findNestedDataset(root, path);
        if (target.getErrorData() == null) {
            target.copyFrom(values);
            root.writeFile(root.getFileName(), null);
            return null;
        } else {
            return target;
        }
    }

    /**
     * Given the name of a file containing a dataset, load the dataset
     * into the main-memory cache (if it isn't there already) and return it.
     * @param fileName             Name of a file containing a dataset
     *                             (if it has no extension, the Dataset
     *                             code will search for a supported extension).
     * @return                     Dataset corresponding to {@code fileName}.
     */
    protected Dataset loadDataset(String fileName) {
        Dataset result = datasetCache.get(fileName);
        if (result == null) {
            result = Dataset.newFileInstanceFromPath(fileName, path,
                    Dataset.Quantity.FIRST_ONLY);
            datasetCache.put(fileName, result);
        }
        return result;
    }

    /**
     * Locates a nested dataset within a larger dataset, and generates
     * an appropriate error if there is no such nested dataset.
     * @param root                 Top-level dataset.
     * @param path                 Path to a nested dataset within
     *                             {@code root}; null or empty means use
     *                             the top-level dataset.
     * @return                     Dataset named by {@code path}, or
     *                             an emptry dataset if no such dataset could be
     *                             found (in which case an error has been
     *                             set on the dataset)
     */
    protected Dataset findNestedDataset(Dataset root, String path) {
        if (path == null) {
            return root;
        }
        try {
            return root.getDataset(path);
        }
        catch (Dataset.MissingValueError e) {
            Dataset data = new Dataset();
            data.setError(new Dataset("message", "nested dataset \"" +
                    path + "\" doesn't exist", "culprit", "path"));
            return data;
        }
    }
}
