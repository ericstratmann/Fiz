package org.fiz;
import java.io.*;

/**
 * This class is an application that measures the performance of the
 * various operations on Strings.
 */

public class PerfString {
    public static void main(String[] argv) throws IOException {
        int count = 10000;
        Dataset d = null;

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                d = new Dataset("name", "Alice", "age", "24",
                        "weight", "140", "height", "64", "ssn",
                        "xxx-yy-zzzz", "element6", "9254231",
                        "element7", "9254231", "element8", "9254231",
                        "element9", "9254231", "element10", "9254231");
            }
            long finish = System.nanoTime();
            System.out.printf("%.4f us per iteration%n",
                    (finish - start)/(1000.0*count));
        }
        System.out.printf("Dataset:\n%s\n", d.toString());
    }

    protected static int inc(int i) {
        return i+1;
    }
    protected static boolean stringEquals(String first, String second) {
        int length1 = first.length();
        int length2 = second.length();
        if (length1 != length2) {
            return false;
        }
        for (int i = 0; i <length1; i++) {
            if (first.charAt(i) != second.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
