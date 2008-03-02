package org.fiz;
import java.io.*;
import java.util.ArrayList;

/**
 * This class is an application that measures the performance of the
 * various operations on Templates.
 */
public class PerfTemplate {
    public static void main(String[] argv) throws IOException {
        int count = 100000;
        int code = 0;
        StringBuilder result = new StringBuilder();
        Dataset data = new Dataset("project", "Commander",
                "procedure", "Master");

        for (int i= 0; i < 10; i++) {
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                result.setLength(0);
                Template.expand("a{@bogus} {@bogus} {@bogus}b",
                        data, result);
            }
            long finish = System.nanoTime();
            System.out.printf("%.4f us per iteration%n", (finish - start)/(1000.0*count));
        }
        System.out.printf("Result value: %s\n", result);
    }
}
