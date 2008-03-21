/**
 * This class tests the speed of manipulating YamlDatasets.
 */

package org.fiz;
import java.io.*;

public class PerfYamlDataset {
    static final String yaml =
            "container:\n" +
            "    project:\n" +
            "        projectName: ElectricCommander build\n" +
            "        description: Prototype build system for new build management" +
                    " product\n" +
            "        modifyTime: 2005-09-22 09:55:00.0\n" +
            "        jobNotesAclTemplateId: 23\n";
    public static void main(String[] argv) throws IOException {
        String value = null;
        int count = 10000;

        System.out.printf("9-line document:%n");
        Dataset top = YamlDataset.newStringInstance(yaml);
        Dataset container = top.getChild("container");
        for (int i= 0; i < 10; i++) {
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                // Dataset child = container.getChild("project");
                value = container.getPath("project.projectName");
            }
            long finish = System.nanoTime();
            System.out.printf("%.2f us per iteration%n", (finish - start)/(1000.0*count));
        }
        System.out.printf("Value extracted: %s%n", value);
    }
}