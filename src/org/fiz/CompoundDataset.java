package org.fiz;
import java.util.*;

/**
 * A CompoundDataset takes a collection of Datasets (possibly including
 * other CompoundDatasets) and makes them behave as if they were merged
 * into a single dataset.
 * * The components of a CompoundDataset are ordered, with values in earlier
 *   components considered "earlier" and values in later components.  Methods
 *   that return a single result (such as {@code getPath} will search the
 *   component datasets in order, returning the first value found.
 *   Methods that return multiple results (such as {@code getChildren}
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
                    DesiredType.STRING);
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
            value = component.lookup(key, wanted);
            if (value == null) {
                continue;
            }
            if ((wanted == DesiredType.STRING)
                    || (wanted == DesiredType.DATASET)) {
                // We only need to return one value and we have found
                // it; no need to search the remaining nested datasets.
                return value;
            }

            // We may potentially return multiple values; save everything
            // and we will sort it out later (below).
            values.add(value);
        }
        int length = values.size();
        if (length == 0) {
            // Nothing matching was found.
            return null;
        }
        if (length == 1) {
            // Exactly one nested call returned something; just pass that on
            // to our caller.
            return values.get(0);
        }

        // Must combine multiple results from nested lookup calls; do this
        // differently depending on whether {@code wanted} is DATASETS or
        // ALL (the only possibilities at this point).
        int totalLength = 0;
        int current = 0;
        if (wanted == DesiredType.DATASETS) {
            // Each value is a Dataset[]; combine them into one big
            // Dataset[].
            for (int i = 0; i < length; i++) {
                totalLength += ((Dataset[]) values.get(i)).length;
            }
            Dataset[] result = new Dataset[totalLength];
            for (int i = 0; i < length; i++) {
                Dataset[] d = (Dataset[]) values.get(i);
                for (int j = 0, length2 = d.length; j < length2; j++) {
                    result[current] = d[j];
                    current++;
                }
            }
            return result;
        }
        // Each value is an Object[]; combine them into one big Object[].
        for (int i = 0; i < length; i++) {
            totalLength += ((Object[]) values.get(i)).length;
        }
        Object[] result = new Object[totalLength];
        for (int i = 0; i < length; i++) {
            Object[] objects = (Object[]) values.get(i);
            for (int j = 0, length2 = objects.length; j < length2; j++) {
                result[current] = objects[j];
                current++;
            }
        }
        return result;
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
     * Generates a nicely formatted string displaying the contents
     * of the dataset.
     * @return                     Pretty-printed string.
     */
    @Override
    public String toString() {
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
        throw new InternalError("writeFile invoked on a CompoundDataset");
    }

    /**
     * This recursive method does all of the work of the {@code lookupPath}
     * method.  Having this method, with the same signature as the
     * method in Dataset, allows CompoundDatasets to contain other
     * CompoundDatasets.  This method is invoked once for each component of
     * the CompoundDataset
     * @param path                 Dot-separated collection of element names,
     *                             indicating the desired values.
     * @param start                Index at which to begin processing in
     *                             {@code path}; always zero.
     * @param dataset              Nested dataset in which to start searching:
     *                             always {@code map}, and not used here.
     * @param wanted               The kind of values that are desired.
     * @param results              Used to collect results; callers may
     *                             already have placed some values here.
     */
    @Override
    public void lookupPathHelper(String path, int start, HashMap dataset,
            DesiredType wanted, ArrayList<Object> results) {
        for (Dataset component : components) {
            component.lookupPathHelper(path, 0, component.map, wanted,
                    results);
            if ((results.size() > 0) && ((wanted == DesiredType.STRING)
                    || (wanted == DesiredType.DATASET))) {
                // We only need to return one value and we have found
                // it; no need to search the remaining components.
                break;
            }
        }
    }
}
