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

package org.fiz.section;

import org.fiz.*;

/**
 * An ImageSelector outputs HTML for one of several images, chosen based on a
 * data value and a mapping dataset.
 *
 * ImageSelector supports the following properties:
 * map -    (required) A dataset of mappings. See below for format.
 * id -     (required) Name of column in dataset we are mapping to an image.
 *
 * The {@code map} dataset contains key - value pairs, where a key is one of
 * the possible strings we will use to select an image. A value can either be a
 * string in which case it represents the src tag for an image, or it can be a
 * Dataset which contains {@code src} and {@code alt} elements, describing
 * the URL for the image and an alt tag. Both {@code src} and {@code alt}
 * values are expanded as templates.  Map recognizes one special key, which
 * is {@code default}. If that key exists, it is used to find the default
 * image if a particular key is not found.
 */
public class ImageSelector extends Section {

    /* Constructor properties */
    protected String id;
    protected Dataset map;

    protected static String DEFAULT_KEY = "default";

    /**
     * Construct an ImageSelector object with the given set of properties.
     * @param properties        A collection of values describing the
     *                          configuration of the ImageSelector; see above
     *                          for the supported values.
     */
    public ImageSelector(Dataset properties) {
        map = properties.getDataset("map");
        id = properties.getString("id");
    }

    /**
     * Construct an ImageSelector given values for the {@code id} and {@code map}
     * properties.
     * @param id                Value for the {@code id} property.
     * @param map               Value for the {@code map} property.
     */
    public ImageSelector(String id, Dataset map) {
        this.id = id;
        this.map = map;
    }

    /**
     * Generates HTML for the the ImageSelector, using properties passed to the
     * constructor and data passed into this method
     * @param cr             Overall information about the client
     *                       request being serviced.
     * @param data           Values in this dataset are used to expand
     *                       templates. We also use {@code id} passed to the
     *                       constuctor to reference a column in this row which
     *                       is used to select an image to display.
     */
    public void render(ClientRequest cr, Dataset data) {

        StringBuilder out = cr.getHtml().getBody();
        String src, alt = null;
        String key = data.getString(id);
        Object value = map.check(key);

        if (value == null) {
            value = map.check(DEFAULT_KEY);

            if (value == null) {
                throw new Dataset.MissingValueError(key);
            }
        }

        if (value instanceof String) {
            src = (String) value;
        } else if (value instanceof Dataset) {
            Dataset imgData = (Dataset) value;
            src = imgData.getString("src");
            alt = imgData.checkString("alt");
        } else {
            throw new org.fiz.InternalError("ImageSelector does not support class " +
                value.getClass().getSimpleName() + ". It only supports " +
                "string and dataset values. ");
        }

        if (alt == null) {
            alt = key;
        }

        Template.appendHtml(out, "<img src=\"@1\" alt=\"@2\" />",
                Template.expandUrl(src, data),
            Template.expandRaw(alt, data));
    }
}
