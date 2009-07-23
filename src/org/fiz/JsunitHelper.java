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
import java.io.*;
import org.mozilla.javascript.*;

/**
 * This class provides miscellaneous facilities needed by the {@code jsunit}
 * Javascript program in order to run Javascript unit tests.  The Rhino
 * interpreter under which {@code jsunit} executes is a fairly bare-bones
 * environment; this class extends that environment with some useful
 * utilities.
 */
public class JsunitHelper extends ScriptableObject {
    /**
     * Constructor for JsunitHelper objects.
     */
    public JsunitHelper() {}

    /**
     * This method is invoked when Javascript code creates a new
     * JsunitHelper object.
     */
    public void jsConstructor() {
        // Nothing to do right now; just return.
    }

    /**
     * Return the name of this javascript class.
     * @return                     Class name.
     */
    public String getClassName() {
        return "JsunitHelper";
    }

    /**
     * Jsunit invokes this method as {@code helper.getTestFiles} to help
     * figure out the names of test files to execute, which could be either
     * a single file or all of the test files in a directory.
     * @param fileName             Name of a file or directory.
     * @return                     If {@code fileName} refers to a directory,
     *                             the return value is a comma-separated list
     *                             of the names of all files in the directory
     *                             that end in "Test.js".  Otherwise the
     *                             return value is fileName.
     */
    public String jsFunction_getTestFiles(String fileName) {
        File file = new File(fileName);
        if (!file.isDirectory()) {
            return fileName;
        }
        StringBuilder result = new StringBuilder();
        String prefix = "";
        for (String child : file.list()) {
            if (child.endsWith("Test.js")) {
                result.append(prefix);
                result.append(fileName);
                result.append('/');
                result.append(child);
                prefix = ",";
            }
        }
        return result.toString();
    }

    /**
     * Jsunit invokes this method as {@code helper.fileExists} to
     * determine whether a given file exists before loading it.
     * @param fileName             Name of the desired file.
     * @return                     True if the file exists, false if it
     *                             doesn't.
     */
    public boolean jsFunction_fileExists(String fileName) {
        return (new File(fileName)).exists();
    }
}
