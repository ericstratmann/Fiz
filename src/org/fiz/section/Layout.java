/* Copyright (c) 2008-2010 Stanford University
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

package org.fiz.section;

import org.fiz.*;

/**
 * Layouts are similiar to Views in other frameworks and are used to give a
 * description of a page (or part of the page) independent of its content.
 * Rendering a layout consists of two phases. In the first, the layout is
 * transformed into HTML (for some layouts, this is a no-op). In the second,
 * values, including other sections, are substituted into the layout, similar
 * to templates, though perhaps using a different syntax. Different layouts
 * will perform the two phases in their own way, but can be used
 * interchanginbly. For example, a layout may be described as wiki markup
 * and another with an ascii image of a table.
 *
 * The base Layout class does not transform its content other than to substitute
 * values in. The layout format should be written in HTML. Substitution of
 * values is done with the Template mechanism; see {@link Template} for more
 * information.
 *
 * By default, layouts support the following properties, though subclasses may
 * add more or ignore these.
 * format                     (optional) String describing the layout. The
 *                            structure of this format will vary from layout to
 *                            layout, but for the Layout class, the format is
 *                            HTML with @ variables.
 * file                       (optional) Location of a file containing the format.
 *                            This property is ignored if {@code format} is
 *                            specified, but is required if it is not.
 * data                       (optional) Values in this dataset are used for
 *                            substitution into the layout when it is rendered.
 *                            Values may be regular variables or sections.
 * ignoreMainDataset          (optional) By default, if a layout can not find a
 *                            variable, it will check the main dataset as a last
 *                            resort. To suppress this behavior, add this
 *                            property with any value, such as the string "true".
 */
public class Layout extends Section {
    private String format; // Describes layout
    private Dataset data; // Data and sections to substitute into the layout
    // Whether we have added the main dataset to data yet
    protected boolean haveAddedMainDataset = false;

    /**
     * Constructs a new Layout object with the given set of properties.
     * @param properties      A collection of values describing the
     *                        configuration of the layout.
     */
    public Layout(Dataset properties) {
        this.properties = properties;

        Dataset data = properties.checkDataset("data");
        if (data != null) {
            addData(data);
        }
    }

    /**
     * Construct a Layout with the given {@code format} string.
     * @param format          Describes the layout. See above for a description.
     */
    public Layout(String format) {
        this(new Dataset("format", format));
    }

    /**
     * Values in the dataset passed to this method will be used to substitute
     * into the layout. For example, if a layout contains "@foo", it may check
     * the data passed in to look for a value with key "foo". Any values in
     * {@code data} supersede values passed in earlier (either through another
     * call to this method or in the constructor).
     * @param data            Values in this dataset are used for substitution
     *                        into the layout when it is rendered
     */
    public void addData(Dataset data) {
        if (this.data != null) {
            this.data = new CompoundDataset(data, this.data);
        } else {
            this.data = data;
        }
    }

    /**
     * Generates HTML for the the Layout, using properties passed to the
     * constructor.
     * @param cr             Overall information about the client
     *                       request being serviced.
     */
    public void render(ClientRequest cr) {
        String format = findFormat(cr);
        Template.appendToClientRequest(cr, format, getData(cr));
    }

    /**
     * Generates HTML for the the Layout, using properties passed to the
     * constructor. Values in {@code data} (and any previous values
     * passed in through addData or the constructor) are used to substitute
     * into the layout.
     * @param cr              Overall information about the client
     *                        request being serviced.
     * @param data            Values in this dataset are used for substitution
     *                        into the layout when it is rendered.
     */
    public void render(ClientRequest cr, Dataset data) {
        addData(data);
        render(cr);
    }

    /**
     * Based on properties in the configuration dataset, returns a string
     * describing the layout to use. If the {@code format} property is specified
     * it is returned, otherwise it checks for {@code file} which refers to a
     * file in the WEB-INF directiory.  If neither property is given, a
     * FormatNotFoundError is thrown.
     * @param cr              Overall information about the client
     *                        request being serviced.
     * @return                Layout specified by the configuration property,
     *                        either read from a file or a template. If neither
     *                        is specified, throws an InternalError
     */
    protected String findFormat(ClientRequest cr) {
        if (format != null) {
            return format;
        }

        format = properties.checkString("format");

        if (format == null) {
            String fileName = properties.checkString("file");
            if (fileName != null) {
                format = Util.readFileFromPath(fileName, "layout format",
                          cr.getServletContext().getRealPath("WEB-INF")).toString();
            }
        }

        if (format == null) {
            throw new org.fiz.InternalError("Format string not found in Layout");
        }

        return format;
    }

    /**
     * Returns the dataset used for substituting data into the layout.
     * @param cr              Overall information about the client
     *                        request being serviced.
     * @return                Values in this dataset are used for substitution
     *                        into the layout when it is rendered.
     */
    protected Dataset getData(ClientRequest cr) {
        if (!haveAddedMainDataset && properties.check("ignoreMainDataset") == null) {
            haveAddedMainDataset = true;
            data = new CompoundDataset(data, cr.getMainDataset());
        }

        return data;
    }

}
