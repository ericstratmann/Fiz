package org.fiz;
import java.io.*;

/**
 * This class is an application that measures the performance of the
 * various operations on Strings.
 */

public class PerfString {
    public static void main(String[] argv) throws IOException {
        int count = 1;
        Dataset d = new Dataset();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            String name = "item" + i;
            String value = "value " + i;
            s.append(name.length() + "."  + name + value.length() + "." +
                    value + "\n");
        }
        String ajaxData = s.toString();
        Runtime runtime = Runtime.getRuntime();

        for (int i = 0; i < 10; i++) {
            long startingUsage = runtime.totalMemory()
                    - runtime.freeMemory();
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                d = new Dataset("name", "Alice", "age", "24", "weight", "140",
                        "height", "64", "ssn", "xxx-yy-zzzz");
            }
            long finish = System.nanoTime();
            long endingUsage = runtime.totalMemory()
                    - runtime.freeMemory();
            System.out.printf("Initial usage: %d bytes\n", startingUsage);
            System.out.printf("Ending usage: %d bytes\n", endingUsage);
            System.out.printf("%d bytes per dataset\n",
                    (endingUsage - startingUsage)/count);
            System.out.printf("%.4f us per iteration%n", (finish - start)/(1000.0*count));
        }
        System.out.printf("%d bytes Ajax data\n", ajaxData.length());
        // System.out.printf("Dataset: %s\n", d.toString());
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
