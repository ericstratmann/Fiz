package org.fiz;
import java.util.*;

/**
 * A CompoundDataset takes a collection of Datasets (possibly including
 * other CompoundDatasets) and makes them behave as if they were merged
 * into a single dataset.
 * * The components of a CompoundDataset are ordered, with data from earlier
 *   components taking precedence over data from later components.
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
     * @param key                  Ignored.
     * @param child                Ignored.
     */
    @Override
    public void addChild(String key, Dataset child) {
        throw new InternalError("addChild invoked on a CompoundDataset");
    }


    /**
     * Searches all of the component datasets to see if any of them has
     * a top-level value named {@code key}.  If so, the first such
     * value is returned.
     * @param key                  Name of the desired value
     * @return                     Value associated with {@code key}, or
     *                             null if {@code key} doesn't exist in any
     *                             of the component datasets.
     */
    @Override
    public String check(String key) {
        for (Dataset component : components) {
            Object result = component.lookup(key,
                    DesiredType.ALL);
            if (result instanceof String) {
                return (String) result;
            }
        }
        return null;
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     */
    @Override
    public void clear() {
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
        Dataset[] clones = new Dataset[components.length];
        for (int i = 0; i < components.length; i++) {
            clones[i] = components[i].clone();
        }
        return new CompoundDataset(clones);
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
        throw new InternalError("copyFrom invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param key                  Ignored.
     * @return                     Doesn't return.
     */
    @Override
    public Dataset createChild(String key) {
        throw new InternalError("createChild invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param key                  Ignored.
     * @param dataset              Ignored.
     * @return                     Doesn't return.
     */
    @Override
    public Dataset createChild(String key, Dataset dataset) {
        throw new InternalError("createChild invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param path                 Ignored.
     * @return                     Doesn't return.
     */
    @Override
    public Dataset createChildPath(String path) {
        throw new InternalError("createChildPath invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param path                 Ignored.
     * @param dataset              Ignored.
     * @return                     Doesn't return.
     */
    @SuppressWarnings("unchecked")
    public Dataset createChildPath(String path, Dataset dataset) {
        throw new InternalError("createChildPath invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param key                  Ignored.
     */
    @Override
    public void delete(String key) {
        throw new InternalError("delete invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param path                 Ignored.
     */
    @Override
    public void deletePath(String path) {
        throw new InternalError("deletePath invoked on a CompoundDataset");
    }

    /**
     * Searches all of the component data sets for a key, returning the
     * first value found.  This is a single-level lookup: the key must be
     * defined in the top level of a dataset.
     * @param key                  Name of the desired value.
     * @return                     Value associated with {@code key}.
     * @throws MissingValueError   Thrown if {@code key} can't be found.
     */
    @Override
    public String get(String key) throws MissingValueError {
        String result = (String) lookup(key, DesiredType.STRING);
        if (result != null) {
            return result;
        }
        throw new MissingValueError(key);
    }

    /**
     * Create a new Dataset corresponding to a nested dataset within one of
     * the component datasets.
     * @param key                  Name of the desired child; must be a
     *                             top-level child within one of the component
     *                             datasets.
     * @return                     Dataset providing access to the first
     *                             child found by the name of {@code key}.
     * @throws MissingValueError   Thrown if {@code key} is not defined
     *                             at the top level of any component dataset.
     * @throws WrongTypeError      Thrown if the first definition of
     *                             {@code key} corresponds to a string value
     *                             rather than a nested dataset.
     */
    @Override
    public Dataset getChild(String key) throws MissingValueError {
        Dataset result = (Dataset) lookup(key, DesiredType.DATASET);
        if (result != null) {
            return result;
        }
        throw new MissingValueError(key);
    }

    /**
     * Returns a nested dataset corresponding to a path; searches all of
     * the component datasets in order, returning the first matching child
     * found.
     * @param path                 A sequence of keys separated by dots.
     * @return                     A Dataset providing access to the child.
     * @throws MissingValueError   Thrown if {@code path} is not defined
     *                             at the top level of the current dataset.
     * @throws WrongTypeError      Thrown if the first definition of
     *                             {@code path} corresponds to a string value
     *                             rather than a nested dataset.
     */
    @Override
    public Dataset getChildPath(String path) throws MissingValueError {
        Dataset result = (Dataset) lookupPath(path, DesiredType.DATASET);
        if (result != null) {
            return result;
        }
        throw new MissingValueError(path);
    }

    /**
     * Returns an array of Datasets corresponding to all of the children
     * in all the component datasets with a given name.  If multiple
     * component datasets have children by that name, they are all returned,
     * in the same order as their components.
     * @param key                  Name of the desired child(ren); must be a
     *                             top-level child within one or more of
     *                             the component datasets.
     * @return                     An array of Datasets, one for each child
     *                             dataset corresponding to {@code key};
     *                             the array will be empty if there are no
     *                             children corresponding to {@code key}.
     */
    @Override
    public Dataset[] getChildren(String key) {
        Object result = lookup(key, DesiredType.DATASETS);
        if (result != null) {
            return (Dataset[]) result;
        }
        return new Dataset[0];
    }

    /**
     * Search the component datasets to find all of the nested datasets
     * that correspond to a hierarchical path.
     * @param path                 Path to the desired descendent(s); must
     *                             be a sequence of keys separated by dots.
     * @return                     An array of Datasets, one for each
     *                             descendant dataset corresponding to
     *                             {@code path}; the array will be empty
     *                             if there are no nested datasets
     *                             corresponding to {@code path}.
     */
    @Override
    public Dataset[] getChildrenPath(String path) {
        Object result = lookupPath(path, DesiredType.DATASETS);
        if (result != null) {
            return (Dataset[]) result;
        }
        return new Dataset[0];
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
        return components[index];
    }

    /**
     * Returns the array containing all of the component datasets.
     * @return                     See above.
     */
    public Dataset[] getComponents() {
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
        return null;
    }

    /**
     * Searches the component datasets for a string value located at a
     * given path.  Returns the first value found.
     * @param path                 A sequence of keys separated by dots.
     * @return                     If the desired value exists in one of
     *                             the component datasets then the first
     *                             one is returned.
     * @throws MissingValueError   Thrown if none of the component
     *                             datasets has a value at {@code path}.
     */
    @Override
    public String getPath(String path) throws MissingValueError {
        String result = (String) lookupPath(path, DesiredType.STRING);
        if (result != null) {
            return result;
        }
        throw new MissingValueError(path);
    }

    /**
     * Returns a Set containing all of the top-level keys in all of the
     * component datasets.
     * @return                     All of the keys at the top level of
     *                             the component datasets.
     */
    @Override
    public Set<String> keySet() {
        HashSet<String> result = new HashSet<String>();
        for (Dataset component : components) {
            result.addAll(component.keySet());
        }
        return result;
    }

    /**
     * Searches the component datasets in order, looking for top-level
     * values whose name is {@code key}.  If {@code wanted} is STRING
     * or DATASET, the first value of the appropriate type is returned.
     * If {@code wanted} is DATASETS, all of the child datasets named
     * {@code key} are returned.  If {@code wanted} is ALL, then
     * all of the values are returned, including both string values and
     * child datasets.
     * @param key                  Name of the desired value.
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     The matching value(s), returned as a
     *                             String, Dataset, Dataset[], or Object[] for
     *                             {@code wanted} values of STRING, DATASET,
     *                             DATASETS, and ALL, respectively.  If
     *                             there are no values named {@code key}
     *                             in any of the datasets then null is
     *                             returned.
     */
    @Override
    public Object lookup(String key, DesiredType wanted) {
        Object value;
        ArrayList<Object> values = new ArrayList<Object>(5);
        for (Dataset component : components) {
            value = component.map.get(key);
            if (value == null) {
                continue;
            }
            addToLookupResults(value, wanted, values);
            if ((values.size() > 0) && ((wanted == DesiredType.STRING)
                    || (wanted == DesiredType.DATASET))) {
                // We only need to return one value and we have found
                // it; no need to search the remaining nested datasets.
                break;
            }
        }
        return lookupResult(wanted, values);
    }

    /**
     * Searches the component datasets in order, looking for values at the
     * location given by {@code path}.  The behavior of this method depends
     * on the {@code wanted}:
     *   STRING:     The first string value for {@code path} is returned.
     *   DATASET:    The first nested dataset matching {@code path} is
     *               returned.
     *   DATASETS:   All nested datasets matching {@code path} are returned
     *               in an array.  The order of the datasets in the result
     *               corresponds to their order of discovery.
     *   ALL:        All values for {@code path} are returned in an array
     *               containing one String for each string value found and
     *               one Dataset for each nested dataset found.  The
     *               order of the values in the result corresponds to their
     *               order of discovery in a depth-first search.
     * @param path                 A sequence of keys separated by dots.
     * @param wanted               Indicates what kind of value is expected
     *                             (string value, nested dataset, etc.)
     * @return                     The matching value(s), returned as a
     *                             String, Dataset, Dataset[], or Object[] for
     *                             {@code wanted} values of STRING, DATASET,
     *                             DATASETS, and ALL, respectively.  If
     *                             there are no values corresponding to
     *                             {@code path} in any of the datasets then
     *                             null is returned.
     */
    @Override
    public Object lookupPath(String path, DesiredType wanted) {
        ArrayList<Object> results = new ArrayList<Object>();
        for (Dataset component : components) {
            lookupPathHelper(path, 0, component.map, wanted, results);
            if ((results.size() > 0) && ((wanted == DesiredType.STRING)
                    || (wanted == DesiredType.DATASET))) {
                // We only need to return one value and we have found
                // it; no need to search the remaining components.
                break;
            }
        }
        return lookupResult(wanted, results);
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param key                  Ignored.
     * @param value                Ignored.
     */
    @Override
    public void set(String key, String value) {
        throw new InternalError("set invoked on a CompoundDataset");
    }

    /**
     * Changes one of the component datasets in the CompoundDataset.
     * @param index                Index of the component to replace; 0
     *                             corresponds to the first component.
     * @param dataset              Dataset to use for the component
     *                             specified by {@code index}.
     */
    public void setComponent(int index, Dataset dataset) {
        components[index] = dataset;
    }

    /**
     * Returns a count of the number of component data sets in this
     * CompoundedDataset.
     * @return                     Number of component datasets in this
     *                             dataset.
     */
    public int size() {
        return components.length;
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param out                  Ignored.
     */
    @Override
    public void toJavascript(Appendable out) {
        throw new InternalError("toJavascript invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @return                     Doesn't return.
     */
    @Override
    public String toString() {
        throw new InternalError("toString invoked on a CompoundDataset");
    }

    /**
     * Not implemented for CompoundDatasets; always throws InternalError.
     * @param name                 ignored.
     * @param comment              Ignored.
     */
    @Override
    public void writeFile(String name, String comment) {
        throw new InternalError("writeFile invoked on a CompoundDataset");
    }
}
