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

import org.fiz.test.*;

/**
 * Junit tests for the PasswordFormElement class.
 */
public class PasswordFormElementTest extends junit.framework.TestCase {
    public void test_constructor_withIdAndLabel() {
        PasswordFormElement element = new PasswordFormElement(
                "id11", "label22");
        assertEquals("properties dataset", "id:    id11\n" +
                "label: label22\n", element.properties.toString());
    }

    public void test_constructor_withDuplicate() {
        Dataset.sortOutput = true;
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "id11",
                        "label", "label22",
                        "duplicate", "id22"));
        assertEquals("validator", "" +
                "errorMessage: Password does not match\n" +
                "otherFields:  id22\n" +
                "type:         duplicate\n",
                element.validatorData.validators.get(0).toString());
    }

    public void test_render_defaultClass() {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                "secret", "Secret:");
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("secret", "<confidential>"), out);
        assertEquals("CSS includes", "PasswordFormElement.css",
                cr.getHtml().getCssFiles());
        assertEquals("generated HTML",
                "<input type=\"password\" id=\"secret\" name=\"secret\" " +
                "class=\"PasswordFormElement\" />",
                out.toString());
    }
    public void test_render_explicitClass() {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "secret", "class", "xyzzy"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML",
                "<input type=\"password\" id=\"secret\" name=\"secret\" " +
                "class=\"xyzzy\" />",
                out.toString());
    }
}
