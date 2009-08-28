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
import javax.servlet.http.*;
import java.util.*;

/**
 * This class provides a dummy implementation of HttpSession for
 * use in tests.
 */
@SuppressWarnings("deprecation")
public class SessionFixture implements HttpSession {
    // The following variable is used to hold attribute values.
    public HashMap<String,Object> attributes = new HashMap<String,Object>();

    // Methods from HttpSession.  Many of these methods do nothing
    // right now; they can be filled in with something more reasonable
    // if/when the functionality is needed.
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    public Enumeration getAttributeNames() {
        return null;
    }
    public long getCreationTime() {
        return 0;
    }
    public String getId() {
        return null;
    }
    public long getLastAccessedTime() {
        return 0;
    }
    public int getMaxInactiveInterval() {
        return 0;
    }
    public ServletContext getServletContext() {
        return null;
    }
    public HttpSessionContext getSessionContext() {
        return null;
    }
    public Object getValue(String name) {
        return null;
    }
    public String[] getValueNames() {
        return null;
    }
    public void invalidate() {
    }
    public boolean isNew() {
        return false;
    }
    public void putValue(String name, Object value) {
    }
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    public void removeValue(String name) {
    }
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    public void setMaxInactiveInterval(int interval) {
    }
}
