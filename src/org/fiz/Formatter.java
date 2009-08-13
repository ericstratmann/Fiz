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
 * The Formatter interface defines a basic mechanism for generating
 * HTML based on information in a dataset.  A Formatter is a Section that
 * does not make data requests, but instead has its data passed to it. Objects
 * that implement this interface (such as Links) can be used in TableSections
 * and other situations in Fiz.
 */
public abstract class Formatter extends Section {
    /**
     * Generate HTML based on information using the client request's
     * main dataset for our data.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     */
    public void render(ClientRequest cr) {
        render(cr, cr.getMainDataset());
    }
    
    /**
     * Generate HTML based on information provided in a dataset.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param data                 Data available for use in generating
     *                             HTML.
     */
    public abstract void render(ClientRequest cr, Dataset data);

    /**
     * Formatters do not have any requests, therefore subclasses cannot
     * override this method. This method does not do anything.
     */
    public final void addDataRequests() {}
}
