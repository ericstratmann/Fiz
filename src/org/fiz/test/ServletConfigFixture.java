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
import javax.servlet.*;
import java.util.*;

/**
 * This class provides a dummy implementation of the ServletConfig
 * interface; it is used for testing.
 */

public class ServletConfigFixture implements ServletConfig {
    // Some methods just set the following variable to indicate that they
    // were called.  This is used in situations where the method can't easily
    // synthesize an appropriate return type.
    public String lastMethod = null;

    public ServletContext servletContext;

    // Returned this as the result of the next call to getInitParameter.
    public String parameterValue = null;

    public ServletConfigFixture(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String getInitParameter(String name) {
        return parameterValue;
    }
    public Enumeration getInitParameterNames() {
        Vector<String> v = new Vector<String>();
        v.addElement("pname1");
        return v.elements();
    }
    public ServletContext getServletContext() {
        lastMethod = "getServletContext";
        return servletContext;
    }
    public String getServletName() {
        return "getServletName";
    }
}
