package org.fiz;

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
    HashMap<String,Object> attributes = new HashMap<String,Object>();

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
