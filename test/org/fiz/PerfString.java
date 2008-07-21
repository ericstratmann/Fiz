package org.fiz;
import java.io.*;
import java.util.*;

/**
 * This class is an application that measures the performance of the
 * various operations on Strings.
 */

public class PerfString {
    public static void main(String[] argv) throws IOException {
        int count = 10000;
        Dataset d = null;
        int value = 0;
        ArrayList<String> list = new ArrayList<String>();
        long sum = 0;

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                // value = Integer.parseInt("144444") - Integer.parseInt("53535");
                sum += System.nanoTime();
                
            }
            long finish = System.nanoTime();
            System.out.printf("%.4f us per iteration%n",
                    (finish - start)/(1000.0*count));
        }
        System.out.printf("list contents: %s\n", StringUtil.join(list, ", "));
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
