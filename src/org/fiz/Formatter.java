package org.fiz;

/**
 * The Formatter interface defines a basic mechanism for generating
 * HTML based on information in a dataset.  Objects that implement this
 * interface (such as Links) can be used in TableSections and other
 * situations in Fiz.
 */
public interface Formatter {
    /**
     * Generate HTML based on information provided in a dataset.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data available for use in generating
     *                             HTML.
     * @param out                  Generated HTML is appended here.
     */
    public void render(ClientRequest cr, Dataset data, StringBuilder out);
}
