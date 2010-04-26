/* Copyright (c) 2010 Stanford University
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

import org.fiz.*;
import org.fiz.test.*;

/**
 * Junit tests for the MobilePhoneLayout class.
 */

public class MobilePhoneLayoutTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_setup_appendToHead() {
        cr.getMainDataset().set("device", "IPhone");
        MobilePhoneLayout layout = new MobilePhoneLayout();
        layout.setup(cr, "IPhone");
        
        TestUtil.assertSubstring("Information added to head element",
                " <meta name=\"viewport\" " + 
                    "content=\"width=device-width; " + 
                    "initial-scale=1.0; maximum-scale=1.0; minimum-scale=1.0; " +
                    "user-scalable=0;\" />",
                cr.getHtml().getHeadExtraInformation());
    }
    
    public void test_setup_deviceCss() {
        cr.getMainDataset().set("device", "IPhone");
        MobilePhoneLayout layout = new MobilePhoneLayout();
        layout.setup(cr, "IPhone");
        
        TestUtil.assertSubstring("CSS files requested",
                "IPhoneLayout.css",
                cr.getHtml().getCssFiles());
    }
    
    public void test_setup_deviceJs() {
        cr.getMainDataset().set("device", "IPhone");
        MobilePhoneLayout layout = new MobilePhoneLayout();
        layout.setup(cr, "IPhone");
        
        TestUtil.assertSubstring("JS files requested",
                "IPhoneLayout.js",
                cr.getHtml().getJsFiles());
    }
    
    public void test_setup_deviceAccumulatedJs() {
        cr.getMainDataset().set("device", "IPhone");
        MobilePhoneLayout layout = new MobilePhoneLayout();
        layout.setup(cr, "IPhone");
        
        TestUtil.assertSubstring("Accumulated Js",
                "new Fiz.IPhoneLayout();",
                cr.getHtml().getJs());
    }
}