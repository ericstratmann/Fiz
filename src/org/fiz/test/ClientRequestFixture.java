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

package org.fiz.test;

import org.fiz.*;

/**
 * This class provides a dummy implementation of the ClientRequest class
 * for use in tests.
 */
public class ClientRequestFixture extends ClientRequest {
    public ClientRequestFixture() {
        super(new ServletFixture(new ServletConfigFixture(
                new ServletContextFixture())),
                new ServletRequestFixture(),
                new ServletResponseFixture());
        Config.init("test/testData/WEB-INF/app/config", 
                "web/WEB-INF/fiz/config");
        Css.init("web/WEB-INF/fiz/css");

        // Create the Html object specially so it will find Javascript files.
        html = new HtmlFixture(null);

        // Provide some initial query values in the main dataset.
        Dataset main = getMainDataset();
        main.set("name", "Alice");
        main.set("age", "36");
        main.set("height", "66");
        main.set("state", "California");

        // Configure various testing values.
        testSkipTokenCheck = true;
        testMode = true;
    }

    public void clearData() {
        mainDataset = null;
        requestDataProcessed = false;
    }
    
    public boolean isAuthTokenSet() {
        return authTokenSet;
    }
    
    public String getJsCode(boolean fromHtml) {
        if (fromHtml) {
            return ((HtmlFixture)html).getJsCode();
        }
        return (jsCode == null) ? "" : jsCode.toString();
    }
}
