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

/**
 * The Convert class is used to convert objects from one class to another,
 * if such a conversion is appropriate. For instance, converting the string
 * "55" to an integer would return the integer 55, but converting from a dataset
 * to a boolean is not allowed.
 */
public final class Convert {

    /**
     * A Success represents the success or failure of a conversion.
     * This can be used by the caller to determine whether the conversion was
     * successful. This should be passed in to every method in this class, and
     * the method will modify it appropriately.
     */
    public static class Success {
        protected boolean success;

        public void Success() {}

        /**
         * Sets the success status of the conversion
         *
         * @param success  Should be true if the conversion succeeded,
         *                 false otherwise.
         */
        public void setSuccess(boolean success) {
            this.success = success;
        }

        /**
         * Returns when the conversion was successful or not
         *
         * @return       True if the conversion was successful, false otherwise.
         */
        public boolean succeeded() {
            return success;
        }
    }

    /**
     * Returns an integer representation of the argument. If the
     * argument is a float, then it is cast into an int. If the argument
     * is a String, then it is parsed. If the argument is a boolean, then
     * {@code true} is converted to 1 and {@code false} to 0. Otherwise,
     * Integer.MIN_VALUE is returned, and {@code success} is set to false.
     *
     * @param o          An object which can be represented in int form
     * @param success    Set to true if the conversion succeeded, false
     *                   otherwise
     * @return           The integer representation of the argument
     */
    public static int toInt(Object o, Success success) {
        success.setSuccess(true);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof String) {
            try {
                return Double.valueOf((String) o).intValue();
            } catch (NumberFormatException e) {}
        } else if (o instanceof Boolean) {
            return (Boolean) o ? 1 : 0;
        }

        success.setSuccess(false);
        return Integer.MIN_VALUE;
    }

    /**
     * Returns a double representation of the argument. If the argument
     * is a number, then it is cast into an double. If the argument is a String,
     * then it is parsed. If the argument is a boolean, then {@code true} is
     * converted to 1 and {@code false} to 0. Otherwise, Double.MIN_VALUE is
     * returned, and {@code success} is set to false.
     *
     * @param o          An object which can be represented as a double
     * @param success    Set to true if the conversion succeeded, false
     *                   otherwise
     * @return           The double representation of the argument
     */
    public static double toDouble(Object o, Success success) {
        success.setSuccess(true);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (o instanceof String) {
            try {
                return Double.valueOf((String) o);
            } catch (NumberFormatException e) {}
        } else if (o instanceof Boolean) {
            return (Boolean) o ? 1 : 0;
        }

        success.setSuccess(false);
        return Double.MIN_VALUE;
    }

    /**
     * Returns a boolean representation of the argument. If the
     * argument is a number, then {@code false} is returned if the number is
     * 0, otherwise {@code true} is returned. If the argument is a String, then
     * {@code true} is returned if the String is equal to "true" or "1", and
     * {@code false} otherwise. If the argument is a boolean, it is returned.
     * Otherwise, false is returned, and {@code success} is set to false.
     *
     * @param o          An object which can be represented as a boolean
     * @param success    Set to true if the conversion succeeded, false
     *                   otherwise
     * @return           The double representation of the argument
     */
    public static boolean toBool(Object o, Success success) {
        success.setSuccess(true);
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue() == 0 ? false : true;
        } else if (o instanceof String) {
            if (o.equals("true") || o.equals("1")) {
                return true;
            }
            return false;
        }
        success.setSuccess(false);
        return false;
    }

    /**
     * Returns a String representation of the argument. This method
     * simply calls and returns the object's {@code toString} method.
     *
     * @param o          An object which can be represented as a String
     * @param success    Set to true if the conversion succeeded, false
     *                   otherwise
     * @return           The String representation of the argument
     */
    public static String toString(Object o, Success success) {
        success.setSuccess(true);
        return o.toString();
    }

    /**
     * Returns a Dataset representation of the argument. If the
     * argument is a dataset, it is returned. Otherwise, null is returned and
     * {@code success} is set to false.
     *
     * @param o          An object which can be represented as a Dataset
     * @param success    Set to true if the conversion succeeded, false
     *                   otherwise
     * @return           The Dataset representation of the argument
     */
    public static Dataset toDataset(Object o, Success success) {
        if (o instanceof Dataset) {
            success.setSuccess(true);
            return (Dataset) o;
        }

        success.setSuccess(false);
        return null;
    }
}
