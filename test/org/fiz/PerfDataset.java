/**
 * This class tests the speed of manipulating basic Datasets.
 */

package org.fiz;
import java.io.*;

public class PerfDataset {
    public static void main(String[] argv) throws IOException {
        int count = 10000;
        int i;
        Dataset d = null;
        String value = "xyz";

        // Synthesize strings for the keys to thwart reference matching
        // during hash table lookup.
        String[] keys = new String[10];
        for (i= 0; i < 10; i++) {
            keys[i] = "value" + (i+1);
        }

        for (i= 0; i < 10; i++) {
                d = new Dataset(new String[] {"value1", "100.1",
                        "value2", "200.2", "value3", "300.3",
                        "value4", "400.4", "value5", "500.5",
                        "value6", "600.6", "value7", "700.7",
                        "value8", "800.8", "value9", "900.9",
                        "value10", "1000.10"});
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                for (int k= 0; k < 10; k++) {
                    value = d.get(keys[k]);
                }
            }
            long finish = System.nanoTime();
            System.out.printf("%.2f us per iteration%n", (finish - start)/(1000.0*count));
        }
        System.out.printf("Value extracted: %s\n", value);
    }
}