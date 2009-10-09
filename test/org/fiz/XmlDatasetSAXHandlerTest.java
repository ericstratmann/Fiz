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
import org.xml.sax.*;

import java.util.*;

/**
 * Junit tests for the XmlDatasetSAXHandler class.
 */

public class XmlDatasetSAXHandlerTest extends junit.framework.TestCase {
    // The following class provides a dummy invitation of Locator to allow
    // more precise control during testing.
    protected static class LocatorFixture implements Locator {
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

    Dataset top;
    XmlDatasetSAXHandler handler;

    public void setUp() {
        top = new Dataset();
        handler = new XmlDatasetSAXHandler(top);
        handler.locator = new LocatorFixture(3);
    }

    public void test_constructor() {
        handler = new XmlDatasetSAXHandler(top);
        assertEquals("top dataset", top, handler.top);
        assertEquals("notStarted", true, handler.notStarted);
    }

    public void test_startElement_notStarted() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        assertEquals("notStarted", false, handler.notStarted);
        assertEquals("ancestors length", top, handler.elementChildren);
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
    public void test_endElement_childIsString() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementText.append("xyz");
        handler.endElement("url", "localName", "child");
        assertEquals("value of new element", "xyz",
                  top.getString("child"));
    }
    public void test_endElement_childIsDataset() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementChildren = new Dataset();
        handler.elementChildren.set("key 46", "value 18");
        handler.endElement("url", "localName", "child");
        assertEquals("class of new value", "Dataset",
                top.get("child").getClass().getSimpleName());
        assertEquals("value in new dataset", "value 18",
                     top.getDataset("child").getString("key 46"));
    }
    @SuppressWarnings("unchecked")
    public void test_endElement_newDatasetWithExistingDataset()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        Dataset oldChild = new Dataset();
        top.add("child", oldChild);
        Dataset newChild = new Dataset();
        handler.elementChildren = newChild;
        newChild.set("key 46", "value 18");
        handler.endElement("url", "localName", "child");
        assertEquals("class of new value", "ArrayList",
                top.getDatasetList("child").getClass().getSimpleName());
        ArrayList<Dataset> list = top.getDatasetList("child");
        assertEquals("size of list", 2, list.size());
        assertEquals("first dataset in list", oldChild, list.get(0));
        assertEquals("second dataset in list", newChild, list.get(1));
    }
    @SuppressWarnings("unchecked")
    public void test_endElement_newDatasetWithExistingList()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        top.add("child", new Dataset());
        top.add("child", new Dataset());
        top.add("child", new Dataset());
        Dataset newChild = new Dataset();
        handler.elementChildren = newChild;
        newChild.set("key 46", "value 18");
        handler.endElement("url", "localName", "child");
        ArrayList<Dataset> list = top.getDatasetList("child");
        assertEquals("size of list", 4, list.size());
        assertEquals("second dataset in list", newChild, list.get(3));
    }
    public void test_endElement_newDatasetWithExistingText()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child2", null);
        top.set("child2", "text");
        Dataset newChild = new Dataset();
        handler.elementChildren = newChild;
        newChild.set("key 46", "value 18");
        handler.endElement("url", "localName", "child2");
        assertEquals("text", top.getStringList("child2").get(0));
        assertEquals(newChild, top.getList("child2").get(1));
    }
    public void test_endElement_popStateBackToParent() throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementText.append("xyz");
        handler.endElement("url", "localName", "child");
        assertEquals("elementChildren", top, handler.elementChildren);
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
        handler.elementChildren = new Dataset();
        handler.characters(StringUtil.newCharArray("  \n\t"), 0, 4);
        assertEquals("elementText", "", handler.elementText.toString());
    }
    public void test_characters_conflict()
            throws SAXException {
        handler.startElement("url", "localName", "qName", null);
        handler.startElement("url", "localName", "child", null);
        handler.elementChildren = new Dataset();
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
