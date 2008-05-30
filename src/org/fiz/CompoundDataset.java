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
     * Searches the component data sets in order, looking for one or more
     * top-level values matching {@code key} and {@code wanted}.
     * @param key                  Name of the desired value.
     * @param wanted               Indicates what kind of value is desired
     *                             (string, nested dataset, or either).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @return                     The return value is null if no matching
     *                             value is found.  Otherwise, if
     *                             {@code quantity} is {@code FIRST_ONLY}
     *                             then the return value is a String or
     *                             Dataset; otherwise the return value is
     *                             an ArrayList, each of whose members is
     *                             a String or Dataset.
     */
    public Object lookup(String key, DesiredType wanted, Quantity quantity) {
        return lookup(key, wanted, quantity, null);
    }

    /**
     * Searches the component data sets in order, looking for one or more
     * top-level values matching {@code key} and {@code wanted}.
     * @param key                  Name of the desired value.
     * @param wanted               Indicates what kind of value is desired
     *                             (string, nested dataset, or either).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @param out                  If {@code quantity} is {@code ALL} and
     *                             this argument is non-null then the
     *                             matching values are appended to this
     *                             rather than creating a new ArrayList,
     *                             and the return value will be {@code out}.
     * @return                     The return value is null if no matching
     *                             value is found.  Otherwise, if
     *                             {@code quantity} is {@code FIRST_ONLY}
     *                             then the return value is a String or
     *                             Dataset; otherwise the return value is
     *                             an ArrayList, each of whose members is
     *                             a String or Dataset.
     */
    @SuppressWarnings("unchecked")
    public Object lookup(String key, DesiredType wanted, Quantity quantity,
            ArrayList<Object> out) {
        Object result = null;
        for (Dataset component : components) {
            result = component.lookup(key, wanted, quantity, out);
            if (result != null) {
                if (quantity == Quantity.FIRST_ONLY) {
                    return result;
                }

                // The following statement is necessary in the case where
                // {@code out} is initially null: once a list has been
                // created, use it for future nested lookups.
                out = (ArrayList<Object>) result;
            }
        }
        return result;
    }

    /**
     * Searches the component data sets in order, looking for one or more
     * values matching {@code path} and {@code wanted}.
     * @param path                 A sequence of keys separated by dots.
     *                             For example, {@code a.b.c} refers to a value
     *                             {@code c} contained in a nested dataset
     *                             {@code b} contained in a dataset {@code a}
     *                             contained in the current dataset.
     * @param wanted               Indicates what kind of value is desired
     *                             (string, nested dataset, or either).
     * @param quantity             Indicates whether all matching values
     *                             should be returned, or only the first
     *                             one found.
     * @param out                  If {@code quantity} is {@code ALL} and
     *                             this argument is non-null then the
     *                             matching values are appended to this
     *                             rather than creating a new ArrayList,
     *                             and the return value will be {@code out}.
     * @return                     The return value is null if no matching
     *                             value is found.  Otherwise, if
     *                             {@code quantity} is {@code FIRST_ONLY}
     *                             then the return value is a String or
     *                             Dataset; otherwise the return value is
     *                             an ArrayList, each of whose members is
     *                             a String or Dataset.
     */
    @SuppressWarnings("unchecked")
    public Object lookupPath(String path, DesiredType wanted,
            Quantity quantity, ArrayList<Object> out) {
        Object result = null;
        for (Dataset component : components) {
            result = component.lookupPath(path, wanted, quantity, out);
            if (result != null) {
                if (quantity == Quantity.FIRST_ONLY) {
                    return result;
                }

                // The following statement is necessary in the case where
                // {@code out} is initially null: once a list has been
                // created, use it for future nested lookups.
                out = (ArrayList<Object>) result;
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

//    /**
//     * This recursive method does all of the work of the {@code lookupPath}
//     * method.  Having this method, with the same signature as the
//     * method in Dataset, allows CompoundDatasets to contain other
//     * CompoundDatasets.  This method is invoked once for each component of
//     * the CompoundDataset
//     * @param path                 Dot-separated collection of element names,
//     *                             indicating the desired values.
//     * @param start                Index at which to begin processing in
//     *                             {@code path}; always zero.
//     * @param dataset              Nested dataset in which to start searching:
//     *                             always {@code map}, and not used here.
//     * @param wanted               The kind of values that are desired.
//     * @param results              Used to collect results; callers may
//     *                             already have placed some values here.
//     */
//    @Override
//    public void lookupPathHelper(String path, int start, HashMap dataset,
//            DesiredType wanted, ArrayList<Object> results) {
//        for (Dataset component : components) {
//            component.lookupPathHelper(path, 0, component.map, wanted,
//                    results);
//            if ((results.size() > 0) && ((wanted == DesiredType.STRING)
//                    || (wanted == DesiredType.DATASET))) {
//                // We only need to return one value and we have found
//                // it; no need to search the remaining components.
//                break;
//            }
//        }
//    }
}
