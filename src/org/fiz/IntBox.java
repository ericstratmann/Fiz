package org.fiz;

/**
 * IntBox is used to create objects that hold a single integer value
 * that can be read and written.  The primary use for this class is
 * to simplify methods that need to return multiple values: if one of
 * the return values is an integer, the caller can pass in an IntBox
 * object, whose value the method can then modify to hold the integer
 * return value.
 */
public class IntBox {
    public int value;              // Arbitrary integer value.
}
