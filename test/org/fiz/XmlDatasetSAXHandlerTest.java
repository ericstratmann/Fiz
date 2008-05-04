package org.fiz;
import org.xml.sax.*;

import java.util.*;

/**
 * Junit tests for the XmlDatasetSAXHandler class.
 */

public class XmlDatasetSAXHandlerTest extends junit.framework.TestCase {
    HashMap<String,Object> map;
    XmlDatasetSAXHandler handler;

    public void setUp() {
        map = new HashMap<String,Object>();
        handler = new XmlDatasetSAXHandler(map);
        handler.locator = new LocatorFixture(3);
    }

    public void test_constructor() {
        handler = new XmlDatasetSAXHandler(map);
        assertEquals("map", map, handler.map);
        assertEquals("notStarted", true, handler.notStarted);
    }

    public void test_startElement_notStarted() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        assertEquals("notStarted", false, handler.notStarted);
        assertEquals("ancestors length", map, handler.elementChildren);
        assertEquals("elementChildren", 0, handler.ancestors.size());
    }
    public void test_startElement_started() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.elementText.append("   ");
        handler.startElement("url", "localName", "child", null);
        assertEquals("elementChildren", null, handler.elementChildren);
        assertEquals("ancestors length", 1, handler.ancestors.size());
        assertEquals("elementText", "", handler.elementText.toString());
        handler.startElement("url", "localName", "child", null);
        assertEquals("ancestors length", 2, handler.ancestors.size());
    }
    public void test_startElement_nonblankText() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.elementText.append("abc");
        boolean gotException = false;
        try {
            handler.startElement("url", "localName", "child", null);
        }
        catch (SAXException e) {
            assertEquals("exception message",
                    "improper use of XML (line 3): element contains both " +
                    "text (\"abc\") and child (child)",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_endElement_outermost() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.elementText.append("xyz");
        handler.endElement("url", "localName", "qName");
        assertEquals("elementText", "xyz", handler.elementText.toString());
    }
    public void test_endElement_illegalText() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementText.append("abc");
        map.put("child", "text");
        boolean gotException = false;
        try {
            handler.endElement("url", "localName", "child");
        }
        catch (SAXException e) {
            assertEquals("exception message",
                    "improper use of XML (line 3): conflicting elements " +
                    "named \"child\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_endElement_childIsString() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementText.append("xyz");
        handler.endElement("url", "localName", "child");
        assertEquals("value of new element", "xyz",
                map.get("child").toString());
    }
    public void test_endElement_childIsDataset() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementChildren = new HashMap<String,Object>();
        handler.elementChildren.put("key 46", "value 18");
        handler.endElement("url", "localName", "child");
        assertEquals("class of new value", "HashMap",
                map.get("child").getClass().getSimpleName());
        assertEquals("value in new dataset", "value 18",
                ((HashMap) map.get("child")).get("key 46").toString());
    }
    @SuppressWarnings("unchecked")
    public void test_endElement_newDatasetWithExistingDataset()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        HashMap<String,Object> oldChild = new HashMap<String,Object>();
        map.put("child", oldChild);
        HashMap<String,Object> newChild = new HashMap<String,Object>();
        handler.elementChildren = newChild;
        newChild.put("key 46", "value 18");
        handler.endElement("url", "localName", "child");
        assertEquals("class of new value", "ArrayList",
                map.get("child").getClass().getSimpleName());
        ArrayList<HashMap<String,Object>> list =
                (ArrayList<HashMap<String,Object>>) map.get("child");
        assertEquals("size of list", 2, list.size());
        assertEquals("first dataset in list", oldChild, list.get(0));
        assertEquals("second dataset in list", newChild, list.get(1));
    }
    @SuppressWarnings("unchecked")
    public void test_endElement_newDatasetWithExistingList()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        ArrayList<HashMap<String,Object>> list
                = new ArrayList<HashMap<String,Object>>();
        list.add(new HashMap<String,Object>());
        list.add(new HashMap<String,Object>());
        list.add(new HashMap<String,Object>());
        map.put("child", list);
        HashMap<String,Object> newChild = new HashMap<String,Object>();
        handler.elementChildren = newChild;
        newChild.put("key 46", "value 18");
        handler.endElement("url", "localName", "child");
        assertEquals("child object", list, map.get("child"));
        assertEquals("size of list", 4, list.size());
        assertEquals("second dataset in list", newChild, list.get(3));
    }
    public void test_endElement_newDatasetWithExistingText()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child2", null);
        map.put("child2", "text");
        HashMap<String,Object> newChild = new HashMap<String,Object>();
        handler.elementChildren = newChild;
        newChild.put("key 46", "value 18");
        boolean gotException = false;
        try {
            handler.endElement("url", "localName", "child2");
        }
        catch (SAXException e) {
            assertEquals("exception message",
                    "improper use of XML (line 3): conflicting elements " +
                    "named \"child2\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_endElement_popStateBackToParent() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementText.append("xyz");
        handler.endElement("url", "localName", "child");
        assertEquals("elementChildren", map, handler.elementChildren);
        assertEquals("ancestors length", 0, handler.ancestors.size());
        assertEquals("elementText", "", handler.elementText.toString());
    }

    public void test_characters() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.characters(StringUtil.newCharArray("abcprefixdef"), 3, 6);
        assertEquals("elementText", "prefix", handler.elementText.toString());
        handler.characters(StringUtil.newCharArray("suffix"), 0, 6);
        assertEquals("elementText", "prefixsuffix",
                handler.elementText.toString());
    }
    public void test_characters_ignoreWhitespaceIfConflict()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementChildren = new HashMap<String,Object>();
        handler.characters(StringUtil.newCharArray("  \n\t"), 0, 4);
        assertEquals("elementText", "", handler.elementText.toString());
    }
    public void test_characters_conflict()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementChildren = new HashMap<String,Object>();
        boolean gotException = false;
        try {
            handler.characters(StringUtil.newCharArray("abcd"), 0, 4);
        }
        catch (SAXException e) {
            assertEquals("exception message",
                    "improper use of XML (line 3): element contains both " +
                    "text (\"abcd\") and child",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_locationMessage_noInfoAvailable() {
        handler.locator = new LocatorFixture(-1);
        assertEquals("no line number available", "",
                handler.locationMessage());
    }
    public void test_locationMessage_infoAvailable() {
        handler.locator = new LocatorFixture(47);
        assertEquals("line number available", " (line 47)",
                handler.locationMessage());
    }
}

// The following class provides a dummy invitation of Locator to allow
// more precise control during testing.
class LocatorFixture implements Locator {
    int lineNumber;

    public LocatorFixture(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    public int getColumnNumber() {
        return 16;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public String getPublicId() {
        return "public id";
    }
    public String getSystemId() {
        return "system id";
    }
}
