package org.fiz;

import java.util.*;
import javax.servlet.http.*;

import org.fiz.test.*;

/**
 * Junit tests for the PageState class.
 */

public class PageStateTest extends junit.framework.TestCase {
    protected ClientRequestFixture cr;

    public void setUp() {
        cr = new ClientRequestFixture();
        ServletRequestFixture.session = null;
    }

    public String getPageIds(ClientRequest cr) {
        HttpSession session = cr.getServletRequest().getSession();
        PageState.AllPageInfo info = (PageState.AllPageInfo)
                session.getAttribute("fiz.PageState");
        if (info == null) {
            return "";
        }
        Object[] keys = info.keySet().toArray();
        Arrays.sort(keys);
        return StringUtil.join(keys, ", ");
    }

    public String getPropertyNames(PageState state) {
        Object[] keys = state.properties.keySet().toArray();
        Arrays.sort(keys);
        return StringUtil.join(keys, ", ");
    }

    public void test_removeEldestEntry_setMaxPageStates() {
        PageState.maxPageStates = -1;
        Config.setDataset("main", new Dataset("maxPageStates", "7"));
        PageState state1 = PageState.getPageState(cr, "id123", true);
        assertEquals("maxPageStates", 7, PageState.maxPageStates);
    }
    public void test_removeEldestEntry_maxPageStatesNotDefined() {
        PageState.maxPageStates = -1;
        Config.setDataset("main", new Dataset());
        boolean gotException = false;
        try {
            PageState.getPageState(cr, "id123", true);
        }
        catch (Dataset.MissingValueError e) {
            assertEquals("exception message",
                    "couldn't find dataset element \"maxPageStates\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_removeEldestEntry_maxPageStatesValueBogus() {
        PageState.maxPageStates = -1;
        Config.setDataset("main", new Dataset("maxPageStates", "7bogus8"));

        boolean gotException = false;
        try {
            PageState.getPageState(cr, "id123", true);
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "bad value \"7bogus8\" for maxPageStates configuration " +
                    "option: must be an integer",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_removeEldestEntry_lruReplacement() {
        PageState.maxPageStates = 3;
        Config.setDataset("main", new Dataset("maxPageStates", "4"));
        PageState.getPageState(cr, "id1", true);
        PageState.getPageState(cr, "id2", true);
        PageState.getPageState(cr, "id3", true);
        assertEquals("page ids before replacement",
                "id1, id2, id3", getPageIds(cr));
        PageState.getPageState(cr, "id4", true);
        assertEquals("page ids after replacement",
                "id2, id3, id4", getPageIds(cr));
    }

    public void test_getPageState_createFalse() {
        PageState state = PageState.getPageState(cr, "id123", false);
        assertEquals("no page state", null, state);
        assertEquals("defined page ids", "", getPageIds(cr));
    }
    public void test_getPageState_createNewPageState() {
        PageState state = PageState.getPageState(cr, "id123", true);
        assertNotNull("page state not null", state.properties.size());
        assertEquals("defined page ids", "id123", getPageIds(cr));
        PageState state2 = PageState.getPageState(cr, "abc", true);
        assertEquals("defined page ids", "abc, id123", getPageIds(cr));
    }

    public void test_getPageProperty() {
        PageState state1 = PageState.getPageState(cr, "id123", true);
        PageState state2 = PageState.getPageState(cr, "abc", true);
        state1.setPageProperty("first", "123");
        state1.setPageProperty("second", "456");
        state2.setPageProperty("third", "789");
        assertEquals("value of first property", "123",
                state1.getPageProperty("first"));
        assertEquals("value of third property", "789",
                state2.getPageProperty("third"));
        assertEquals("missing property", null,
                state2.getPageProperty("first"));
    }

    public void test_setPageProperty() {
        PageState state1 = PageState.getPageState(cr, "id123", true);
        PageState state2 = PageState.getPageState(cr, "abc", true);
        state1.setPageProperty("first", "123");
        state1.setPageProperty("second", "456");
        state2.setPageProperty("third", "789");
        assertEquals("properties in first PageState", "first, second",
                getPropertyNames(state1));
        assertEquals("properties in second PageState", "third",
                getPropertyNames(state2));
    }
}
