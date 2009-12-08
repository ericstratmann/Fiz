package org.fiz;

import java.io.File;

public class DirectoryInteractor extends Interactor {
    /**
     * Displays the local directory in a tree section.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void tree(ClientRequest cr) {
        Html html = cr.getHtml();
        html.clear();
        html.setTitle("TreeSection Demo");
        String edgeFamily = "treeSolid";
        cr.showSections(
                new TreeSection(
                        new Dataset("id", "tree1", "dataFactory",
                                    "DirectoryInteractor.dirData", "rootName",
                                    "C:/Documents and Settings/John Ousterhout/My Documents/Fiz",
                                    "edgeFamily", edgeFamily))
        );
    }

    /**
     * Creates a Dataset that returns the contents of a directory
     * @param directory            Name of the directory whose children
     *                             will be displayed.
     * @return                     Dataset that contains information about
     *                             all of the children of directory.
     */
    public static Dataset dirData(String directory) {
        Dataset data = new Dataset();
        File dir = new File(directory);

        if (dir.isDirectory()) {
            for (String child : dir.list()) {
                File file = new File(dir, child);
                Dataset childData = new Dataset("name", dir + "/" + child);
                if (file.isDirectory()) {
                    childData.set("expandable", "1");
                    childData.set("text", child);
                } else {
                    childData.set("text",
                            child + " (" + file.length() + " bytes)");
                }
                data.add("record", childData);
            }
        }

        return data;
    }
}
