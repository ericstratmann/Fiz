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
import java.util.*;

/**
 * A CompoundDataset takes a collection of Datasets (possibly including
 * other CompoundDatasets) and makes them behave as if they were merged
 * into a single dataset.
 * * The components of a CompoundDataset are ordered, with values in earlier
 *   components considered "earlier" and values in later components.  Methods
 *   that return a single result (such as {@code get} will search the
 *   component datasets in order, returning the first value found.
 *   Methods that return multiple results (such as {@code getList}
 *   will return all of the values found in any component; the order of
 *   the results will reflect the order of the components.
 * * A CompoundDataset refers to its component datasets by reference,
 *   which makes it cheap to create a CompoundDataset even if the
 *   components are large, and also means that changes to the components
 *   will appear in the CompoundDataset.
 * * CompoundDatasets are read-only.  Attempts to modify them generate
 *   InternalError exceptions.
 */
public class CompoundDataset extends Dataset {
    // Of the component datasets that make up this compound dataset.
    protected Dataset[] components;

    // The following variables are used by accumulateChildren to keep track
    // of all the child datasets found in all of the components.  The idea
    // here is to avoid allocation in the common case where children
    // exist in only one of the components.
    ArrayList<Dataset> union = null;
    Dataset[] firstBatch = null;
    int numBatches = 0;

    /**
     * Construct a CompoundDataset out of a collection of existing
     * datasets.
     * @param components           Each argument is a Dataset that will become
     *                             part of the CompoundDataset.   Data in
     *                             earlier components takes precedence over
     *                             data in later components.
     */
    public CompoundDataset(Dataset... components) {
        this.components = components;
    }

    /**
     * Construct a CompoundDataset from an ArrayList containing the
     * component datasets.
     * @param components           Eeach element of this ArrayList is a
     *                             Dataset that will become a component of
     *                             the CompoundDataset.  The order of
     *                             elements in {@code components} determines
     *                             the precedence for the CompoundDataset.
     */
    public CompoundDataset(ArrayList<Dataset> components) {
        this.components = new Dataset[components.size()];
        for (int i = 0, length = components.size(); i < length; i++) {
            this.components[i] = components.get(i);
        }
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param source               Ignored.
     * @param start                Ignored.
     */
    @Override
    public int addSerializedData(CharSequence source, int start) {
        throwIfError();
        throw new InternalError("addSerializedData invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param source               Ignored.
     */
    @Override
    public void addSerializedData(CharSequence source) {
        throwIfError();
        throw new InternalError("addSerializedData invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param key                  Ignored.
     * @param child                Ignored.
     */
    @Override
    public void add(String key, Object child) {
        throwIfError();
        throw new InternalError("add invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param path                 Ignored.
     * @param child                Ignored.
     */
    @Override
    public void addPath(String path, Object child) {
        throwIfError();
        throw new InternalError("addPath invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     */
    @Override
    public void clear() {
        throwIfError();
        throw new InternalError("clear invoked on a CompoundDataset");
    }

    /**
     * Generates and returns a "deep copy" of the dataset, such that
     * no modification to either dataset will be visible in the other.
     * This is done by cloning each of the component data sets and
     * creating a new CompoundDataset consisting of the clones.
     * @return                     A copy of this dataset.
     */
    @Override
    public CompoundDataset clone() {
        throwIfError();
        Dataset[] clones = new Dataset[components.length];
        for (int i = 0; i < components.length; i++) {
            clones[i] = components[i].clone();
        }
        return new CompoundDataset(clones);
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param dest                      Ignored
     */
    @Override
    public CompoundDataset clone(Dataset dest) {
        throwIfError();
        throw new InternalError("clone with argument invoked on a CompoundDataset");
    }


    /**
     * Indicates whether a key exists in any of the component datasets.
     * @param key                  Name of the desired value.
     * @return                     True if the key exists in the top
     *                             level of any of the component datasets,
     *                             false otherwise.
     */
    @Override
    public boolean containsKey(String key) {
        throwIfError();
        for (Dataset component : components) {
            if (component.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param source              Ignored.
     */
    @Override
    public void copyFrom(Dataset source) {
        throwIfError();
        throw new InternalError("copyFrom invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param key                  Ignored.
     */
    @Override
    public void delete(String key) {
        throwIfError();
        throw new InternalError("delete invoked on a CompoundDataset");
    }

    /**
     * Given an index, returns the component dataset corresponding to that
     * index.
     * @param index                Selects the desired component dataset,
     *                             where 0 corresponds to the first component.
     *                             Must be >= zero and less than the total
     *                             number of components.
     * @return                     The component dataset corresponding to
     *                             {@code index}.
     */
    public Dataset getComponent(int index) {
        throwIfError();
        return components[index];
    }

    /**
     * Returns the array containing all of the component datasets.
     * @return                     See above.
     */
    public Dataset[] getComponents() {
        throwIfError();
        return components;
    }

    /**
     * If this dataset was originally read from a file, this method
     * will provide the name of that file.
     * @return                     Always returns null, since this
     *                             CompoundDataset was not read from a file.
     */
    @Override
    public String getFileName() {
        throwIfError();
        return null;
    }

    /**
     * Returns a Set containing all of the top-level keys in all of the
     * component datasets.
     * @return                     All of the keys at the top level of
     *                             the component datasets.
     */
    @Override
    public Set<String> keySet() {
        throwIfError();
        HashSet<String> result = new HashSet<String>();
        for (Dataset component : components) {
            result.addAll(component.keySet());
        }
        return result;
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param out                  Ignored.
     */
    @Override
    public void serialize(StringBuilder out) {
        throwIfError();
        throw new InternalError("serialize invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     */
    @Override
    public String serialize() {
        throwIfError();
        throw new InternalError("serialize invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param key                  Ignored.
     * @param value                Ignored.
     */
    @Override
    public void set(String key, Object value) {
        throwIfError();
        throw new InternalError("set invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param path                 Ignored.
     * @param value                Ignored.
     */
    @Override
    public void setPath(String path, Object value) {
        throwIfError();
        throw new InternalError("setPath invoked on a CompoundDataset");
    }

    /**
     * Changes one of the component datasets in the CompoundDataset.
     * @param index                Index of the component to replace; 0
     *                             corresponds to the first component.
     * @param dataset              Dataset to use for the component
     *                             specified by {@code index}.
     */
    public void setComponent(int index, Dataset dataset) {
        throwIfError();
        components[index] = dataset;
    }

    /**
     * Returns a count of the number of component data sets in this
     * CompoundDataset.
     * @return                     Number of component datasets in this
     *                             dataset.
     */
    public int size() {
        throwIfError();
        return components.length;
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param out                  Ignored.
     */
    @Override
    public void toJavascript(StringBuilder out) {
        throwIfError();
        throw new InternalError("toJavascript invoked on a CompoundDataset");
    }

    /**
     * Generates a nicely formatted string displaying the contents
     * of the dataset.
     * @return                     Pretty-printed string.
     */
    @Override
    public String toString() {
        throwIfError();
        StringBuilder result = new StringBuilder();
        String separator = "";
        for (int i = 0; i < components.length; i++) {
            result.append(separator);
            result.append("Component #");
            result.append(i);
            result.append(":\n  ");
            result.append(components[i].toString().trim().replace(
                    "\n", "\n  "));
            separator = "\n";
        }
        result.append("\n");
        return result.toString();
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param name                 ignored.
     * @param comment              Ignored.
     */
    @Override
    public void writeFile(String name, String comment) {
        throwIfError();
        throw new InternalError("writeFile invoked on a CompoundDataset");
    }

    /**
     * Searches the component data sets in order, looking for one or more
     * values (either top level or a path) matching {@code key}.
     * @param keyOrPath            Name of the desired element (key in
     *                             top-level dataset or multi-level path).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @return                     The return value is null if no matching
     *                             values are found.  If {@code quantity} is
     *                             {@code FIRST_ONLY} then the return
     *                             value is the first match found; otherwise
     *                             the return value is a DSArrayList, containing
     *                             all matches.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object lookup(String keyOrPath, Quantity quantity) {
        throwIfError();
        Object result;
        DSArrayList<Object> out = null;
        if (quantity == Quantity.ALL) {
            out = new DSArrayList<Object>();
        }

        boolean foundAny = false;
        for (Dataset component : components) {
            result = component.lookup(keyOrPath, quantity);
            if (result != null) {
                if (quantity == Quantity.FIRST_ONLY) {
                    return result;
                }
                foundAny = true;
                if (result instanceof DSArrayList) {
                    out.addAll((DSArrayList<Object>) result);
                } else {
                    out.add(result);
                }
            }
        }
        return (foundAny) ? out : null;
    }

}
