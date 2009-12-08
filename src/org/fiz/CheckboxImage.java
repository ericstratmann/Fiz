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

import java.util.*;

/**
 * A CheckboxImage outputs HTML for a checkbox image, either empty or checked
 * based on a data value. If the value is a "false" value it displays one image
 * and if it is "true" another.
 *
 * CheckboxImage supports the following properties:
 * id -             (required) Name of column in dataset we are using to
 *                  select an image
 * family -         (optional) Family name for images to display. If the
 *                  family name is {@code x.png}, then images {@code x-true.png}
 *                  and {@code x-false.png} must exist.  Default value is
 *                  {@code checkbox.png}.
 *
 * Additionally, if an array is passed in, it is used to define which values
 * count as false. All other values will be assumed to be true. If no array is
 * passed in, default false values will be used, which include "0", "false",
 * "null", and "" (the empty string).
 */

public class CheckboxImage extends Section {

    /* Constructor properties */
    protected String id;
    protected String family;
    protected ArrayList<String> falseValues;

    public static String[] DEFAULT_FALSE_VALS = {"0", "false", "null", ""};
    public static String DEFAULT_FAMILY = "checkbox.png";

    /**
     * Construct a CheckboxImage object with the given set of properties and
     * and ArryayList of false values
     * @param falseValues       An ArrayList of values considered false
     * @param properties        A collection of values describing the
     *                          configuration of the CheckboxImage; see
     *                          above for the supported values.
     */
    public CheckboxImage(Dataset properties, ArrayList<String> falseValues) {
        this.falseValues = falseValues;
        id = properties.getString("id");
        family = properties.checkString("family");

        if (family == null) {
            family = DEFAULT_FAMILY;
        }

    }

    /**
     * Construct a CheckboxImage object with the given set of properties and
     * and array of false values
     * @param falseValues       An array of values considered false
     * @param properties        A collection of values describing the
     *                          configuration of the CheckboxImage; see
     *                          above for the supported values.
     */

    public CheckboxImage(Dataset properties, String ... falseValues) {
        this(properties, new ArrayList<String>(Arrays.asList(falseValues)));
    }

    /**
     * Construct a CheckboxImage object with the given set of properties
     * using the default false values.
     * @param properties        A collection of values describing the
     *                          configuration of the CheckboxImage; see
     *                          above for the supported values.
     */
    public CheckboxImage(Dataset properties) {
        this(properties, DEFAULT_FALSE_VALS);
    }


    /**
     * Generates HTML for the the CheckboxImage, using properties passed to
     * the constructor and data passed into this method.
     * @param cr             Overall information about the client
     *                       request being serviced.
     * @param data           Values in this dataset are used to expand
     *                       templates. We also use {@code id} passed to the
     *                       constuctor to reference a column in this row which
     *                       is used to select an image to display.
     */
    public void render(ClientRequest cr, Dataset data) {
        StringBuilder out = cr.getHtml().getBody();
        String key = data.getString(id);
        String src, alt;
        String checked;

        if (falseValues.contains(key)) {
            checked = "-false";
            alt = "unchecked";
        } else {
            checked = "-true";
            alt = "checked";
        }

        Template.appendHtml(out, "<img src=\"@1\" alt=\"@2\" />",
                            StringUtil.addSuffix(family, checked), alt);
    }
}
