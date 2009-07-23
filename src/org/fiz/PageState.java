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
import java.util.*;
import javax.servlet.http.*;

/**
 * PageState is used internally by Fiz to maintain state information
 * that is created when a page is initially rendered, but must be
 * available to Ajax and form handlers that are invoked later.  This
 * class implements the notion of a "page", which ties together the initial
 * rendering of a page with all subsequent Ajax requests and form posts
 * emanating from that page.  It also implements "page properties",
 * which are named entities accessible across all of the requests for
 * a given page.
 *
 * Page state is stored as part of the session. Unfortunately there is no
 * foolproof way to tell when the state for a page is no longer needed
 * (the user can use the "Back" button to return to pages
 * from the distant past). In order to keep page state to a manageable
 * size, this class automatically deletes old page state (only the most
 * recently accessed pages are retained).
 *
 * Each PageState object holds the properties for a particular page.
 */
class PageState implements Serializable {
    // Maximum number of pages for which we will retain state; this is a
    // copy of the maxPageStates property in the main configuration dataset.
    // -1 means we haven't yet read the value from the main configuration
    // dataset.
    protected static int maxPageStates = -1;

    // An object of the following class is stored in the session under
    // the key {@code fiz.PageState}; it holds all of the properties for
    // all of the known pages for this session.  This class is a subclass
    // of LinkedHashMap (it implements LRU replacement). Keys are page
    // identifiers and values are PageState objects for the pages known
    // in that session.
    protected static class AllPageInfo
            extends LinkedHashMap<String, PageState> implements Serializable {
        public AllPageInfo() {
            // Track LRU for objects in the map.
            super(10, (float) 0.75, true);
        }

        // The method below is invoked when a new entry is added to the
        // object.  If the total number of entries now exceeds the limit,
        // discard the state for the least-recently-accessed page.
        protected boolean removeEldestEntry(Map.Entry<String,PageState>
                eldest) {
            if (maxPageStates < 0) {
                // Refresh the limit value from the configuration dataset.
                String option = Config.get("main", "maxPageStates");
                try {
                    maxPageStates = Integer.parseInt(option);
                }
                catch (NumberFormatException e) {
                    throw new InternalError("bad value \"" + option +
                            "\" for maxPageStates configuration option: " +
                            "must be an integer");
                }
            }
            return (maxPageStates < size());
        }
    }

    // The following object holds all of the properties for this page.
    // keys are string identifiers (by convention, these always start with
    // the name of the class on whose behalf the information is being stored),
    // values are arbitrary objects.
    protected HashMap<String,Object> properties =
            new HashMap<String,Object>();

    /**
     * Find and return the state object for the current page.
     * @param cr                   Overall information about the client
     *                             request being serviced.
     * @param id                   Unique identifier for the current page.
     * @param create               True means create a new state object
     *                             if one doesn't already exist.
     * @return                     Returns the page state object for the
     *                             current page, or null if there is no
     *                             state for the current page and
     *                             {@code create} is false.
     */
    public synchronized static PageState getPageState(ClientRequest cr,
            String id, boolean create) {
        HttpSession session = cr.getServletRequest().getSession();
        AllPageInfo info = (AllPageInfo) session.getAttribute("fiz.PageState");
        if (info == null) {
            if (create) {
                info = new AllPageInfo();
                session.setAttribute("fiz.PageState", info);
            } else {
                return null;
            }
        }
        PageState state = info.get(id);
        if (state != null) {
            return state;
        }
        if (create) {
            state = new PageState();
            info.put(id, state);
        }
        return state;
    }

    /**
     * Returns a page property associated with this page.
     * @param name                 Name of the desired property.
     * @return                     If a property named {@code name} has
     *                             been defined for the current page (by
     *                             calling {@code setPageProperty}, return
     *                             it; otherwise return null.
     */
    public Object getPageProperty(String name) {
        return properties.get(name);
    }

    /**
     * Set a page property on the current page.  It can be retrieved later
     * by calling getPageProperty.
     * @param name                 Name of the desired property.
     * @param value                Value to remember for this property.
     */
    public void setPageProperty(String name, Object value) {
        properties.put(name, value);
    }
}
