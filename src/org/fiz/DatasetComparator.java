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
 * DatasetComparator is used to sort collections of datasets based on the
 * values within the datasets.
 */
public class DatasetComparator implements Comparator<Dataset> {
    /**
     * This class is used to specify how values should be compared.
     */
    public enum Type {
        /**
         * Compare values as strings, using {@code String.compareTo}.
         */
        STRING,

        /**
         * Compare values as arbitrary length decimal integers.
         */
        INTEGER,

        /**
         * Convert values to {@code double} and then compare them as
         * floating-point numbers.
         */
        FLOAT
    }

    /**
     * This class specifies the direction of the sort.
     */
    public enum Order {
        /**
         * Smaller values (or those earlier in collation order) should
         * appear first.
         */
        INCREASING,

        /**
         * Larger values (or those appearing later in collation order)
         * should appear first.
         */
        DECREASING
    }

    /**
     * An object of the following class describes one criterion for
     * deciding the relative order of two datasets.
     */
    protected static class SortKey {
        public String path;        // Location of the dataset values to
                                   // compare, specified as a path.
        public Type type;          // How to perform the comparison.
        public Order order;        // How to order the results after
                                   // comparison.
        public SortKey(String path, Type type, Order order) {
            this.path = path;
            this.type = type;
            this.order = order;
        }
    }

    // The following variable holds one or more SortKeys, which are used
    // to perform comparisons.  The element at index 0 has highest priority;
    // a given SortKey is tested only if all the preceding SortKeys found
    // the two Datasets equal.
    protected ArrayList<SortKey> keys = new ArrayList<SortKey>();

    /**
     * Construct a DatasetComparator that will compare one element from
     * each dataset.
     * @param path                 Name of the element to compare
     *                             in each dataset (a path).
     * @param type                 Type of comparison for the elements named
     *                             by {@code path}.
     * @param order                Order of sorting.
     */
    public DatasetComparator(String path, Type type, Order order) {
        keys.add(new SortKey(path, type, order));
    }

    /**
     * Construct a DatasetComparator that will perform a two-step comparison,
     * first comparing a given element from each dataset and then, if those
     * elements are considered equal, performing a second comparison with
     * different elements.
     * @param path1                Name of the first element to compare
     *                             in each dataset (a path).
     * @param type1                Type of comparison for the elements named
     *                             by {@code path1}.
     * @param order1               Order of sorting for the first comparison.
     * @param path2                Name of the second element to compare;
     *                             used only if the first comparison finds
     *                             the dataset equal.
     * @param type2                Type of comparison for the second
     *                             comparison.
     * @param order2               Order of sorting for the second comparison.
     */
    public DatasetComparator(String path1, Type type1, Order order1,
            String path2, Type type2, Order order2) {
        keys.add(new SortKey(path1, type1, order1));
        keys.add(new SortKey(path2, type2, order2));
    }

    /**
     * Compared to datasets using the criteria that were specified
     * when this object was constructed.
     * @param d1                   First dataset for comparison.
     * @param d2                   Second dataset for comparison.
     * @return                     The return value is negative if the
     *                             {@code d1} should sort before {@code d2};
     *                             it is positive if {@code d1} should sort
     *                             after {@code d2}; and it is 0 if the 2
     *                             datasets are equivalent or their order
     *                             could not be determined (this can happen,
     *                             for example, if one of the datasets is
     *                             missing the element to be compared).
     */
    public int compare(Dataset d1, Dataset d2) {
        int result;
        for (SortKey key : keys) {
            // Fetch the elements.
            String value1 = (String) d1.lookupPath(key.path,
                    Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY);
            String value2 = (String) d2.lookupPath(key.path,
                    Dataset.DesiredType.STRING, Dataset.Quantity.FIRST_ONLY);

            // If either value is nonexistent then their ordering is unknown.
            if ((value1 == null) || (value2 == null)) {
                continue;
            }

            // Compare the elements.
            if (key.type == Type.STRING) {
                result = value1.compareTo(value2);
            } else if (key.type == Type.INTEGER) {
                result = compareIntegers(value1, value2);
            } else {
                try {
                    result = Double.compare(Double.parseDouble(value1),
                            Double.parseDouble(value2));
                }
                catch (NumberFormatException e) {
                    // If either of the numbers doesn't have proper
                    // floating-point syntax than their ordering is unknown.
                    continue;
                }
            }
            if (result != 0) {
                if (key.order == Order.INCREASING) {
                    return result;
                }
                return -result;
            }
        }
        return 0;
    }

    /**
     * Determine whether to DatasetComparators are identical (i.e., will
     * always produce the same results).
     * @param obj                  Another Comparator to compare
     *                             with this one.
     * @return                     True is returned if {@code other} is a
     *                             DatasetComparator with the same number
     *                             of sort keys as this objects and the
     *                             sort keys are identical; otherwise false
     *                             is returned.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DatasetComparator)) {
            return false;
        }
        DatasetComparator other = (DatasetComparator) obj;
        int size = keys.size();
        if (other.keys.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            SortKey key1 = keys.get(i);
            SortKey key2 = other.keys.get(i);
            if (!(key1.path.equals(key2.path)) || (key1.type != key2.type) ||
                    (key1.order != key2.order)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare two strings containing signed integer values.  This procedure
     * is used instead of converting the strings to integers and using standard
     * Java arithmetic because (a) it's about 3x faster, (b) it handles
     * arbitrary length integers, and (c) it handles integers embedded in
     * other text.
     * @param s1                   First integer value.
     * @param s2                   Second integer value.
     * @return                     -1 is returned if {@code s1} is less than
     *                             {@code s2}, +1 if {@code s1} is greater
     *                             than {@code s2}, and 0 if the values
     *                             are the same or if either of the values
     *                             doesn't contain a proper integer.
     */
    protected static int compareIntegers(String s1, String s2) {
        int i1, i2;
        char c1 = '0', c2 = '0';
        int result = 0;

        // Find the last digit in each number.
        for (i1 = s1.length()-1; i1 >= 0; i1--) {
            c1 = s1.charAt(i1);
            if ((c1 >= '0') && (c1 <= '9')) {
                break;
            }
        }
        for (i2 = s2.length()-1; i2 >= 0; i2--) {
            c2 = s2.charAt(i2);
            if ((c2 >= '0') && (c2 <= '9')) {
                break;
            }
        }
        if ((i1 < 0) || (i2 < 0)) {
            // At least one of the strings doesn't contain a valid integer,
            // so the comparison order is unknown.
            return 0;
        }

        // Work backwards from the ends of the numbers to the beginnings;
        // in each iteration we are comparing digits from the same decimal
        // position, and the comparison in each iteration has higher
        // precedence than any comparison from previous iterations.
        while (true) {
            // Get the next digit from each string, and see if we have reached
            // the end of each string (indicated by a negative current index)
            int thisComparison = 0;
            if (i1 >= 0) {
                c1 = s1.charAt(i1);
                if ((c1 < '0') || (c1 > '9')) {
                    i1 = -1;
                } else {
                    thisComparison += c1;
                    i1--;
                }
            }
            if (i2 >= 0) {
                c2 = s2.charAt(i2);
                if ((c2 < '0') || (c2 > '9')) {
                    i2 = -1;
                } else {
                    thisComparison -= c2;
                    i2--;
                }
            }
            if (thisComparison != 0) {
                result = (thisComparison < 0) ? -1 : 1;
            }
            if ((i1 < 0) && (i2 < 0)) {
                // We have reached the end of both numbers.  All that's left
                // is to incorporates signs, if any, in the comparison.
                if (c1 == '-') {
                    if (c2 == '-') {
                        // Both numbers have signs; this reverses the
                        // comparison.
                        return -result;
                    }
                    // Only c1 has a sign, so it must be less.
                    return -1;
                }
                if (c2 == '-') {
                    // Only c2 has a sign, so it must be less.
                    return 1;
                }
                return result;
            }
        }
    }
}