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
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * This class is used internally by XmlDataset: it provides handlers
 * that are invoked by SAX while parsing an XML document.  The handlers
 * fill in the contents of a dataset been generated, and also generate
 * errors for XML constructs that aren't allowed in datasets.
 */

class XmlDatasetSAXHandler extends DefaultHandler {
    // Top-level Dataset
    protected Dataset top;

    // True means that we have not yet seen the outermost element
    // for the XML data; once this becomes false there's always
    // a "current element", which is the most deeply nested element
    // for which startElement has been called but not endElement.
    protected boolean notStarted = true;

    // The ancestors of the current element, with the current element's
    // parent in the highest index position.  Empty means the current
    // element is the outermost XML element.
    protected ArrayList<Dataset> ancestors = new ArrayList<Dataset>();

    // Textual data that has accumulated for the current element.
    protected StringBuilder elementText = new StringBuilder();

    // If the following field is non-null, it means the current element
    // is a dataset, so it cannot have a string value; it can only contain
    // nested datasets.  The nested datasets are the values in the
    // HashMap.
    protected Dataset elementChildren;

    // This object is provided by SAX; we can use it to get line number
    // information for error messages.
    protected Locator locator;

    /**
     * Constructor for XmlDatasetSAXHandlers.
     * @param top                  Top-level Dataset for the new dataset;
     *                             should initially be empty.
     */
    public XmlDatasetSAXHandler(Dataset top) {
        this.top = top;
    }

    /**
     * The SAX parser invokes this method to provide an object that can be
     * used to retrieve location information such as the current line number,
     * for use in error messages.
     * @param locator              Use this later to retrieve location info.
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * The SAX parser invokes this method each time it encounters the
     * beginning of a new XML element (e.g., "<foo>").
     * @param uri                  Not used here.
     * @param localName            Not used here.
     * @param qName                The name of the element (the text
     *                             inside <> before attributes).
     * @param atts                 Attributes specified in the <>; ignored.
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (notStarted) {
            // We are just entering the outermost XML element, which
            // corresponds to the overall dataset.
            notStarted = false;
            elementChildren = top;
            return;
        }

        // We're starting a new child of the current element.  This means
        // that the current element is a nested dataset, which has 2
        // implications:
        // 1. The current element better not not have any nonblank text
        //   (we do ignore whitespace as a convenience in writing
        //   XML files).
        // 2. If we haven't already created a Dataset to hold the
        //    children of the current element, create it now.

        if (!StringUtil.isWhitespace(elementText)) {
            throw new SAXException("improper use of XML"
                    + locationMessage() + ": element contains both text (\""
                    + StringUtil.excerpt(elementText, 20) + "\") and child ("
                    + qName + ")");
        }
        if (elementChildren == null) {
            elementChildren = new Dataset();
        }

        // Adjust our state to reflect the fact that we now have a new
        // current element.  Note that we don't know what kind of element
        // this will be yet (string, dataset, list).  Thus,
        // we don't make an official entry for the new element in its parent
        // yet; that will happen when endElement is invoked.
        ancestors.add(elementChildren);
        elementText.setLength(0);
        elementChildren = null;
    }

    /**
     * The SAX parser invokes this method each time it encounters the
     * end of an XML element (e.g., "</foo>").  The method takes the data
     * that has accumulated for the current element and adds it into the
     * parent element as an official child.
     * @param uri                  Not used here.
     * @param localName            Not used here.
     * @param qName                The name of the element (the text
     *                             inside <> before attributes).
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // Pop the parent container from the ancestor list.
        int last = ancestors.size()-1;
        if (last < 0) {
            // We are ending the outermost XML element; nothing to do here.
            return;
        }

        Dataset parent = ancestors.get(last);
        ancestors.remove(last);

        if (elementChildren == null) {
            // The current element is a simple string value (no children).
            parent.add(qName, elementText.toString());
        } else {
            // Element is a dataset
            parent.add(qName, elementChildren);
        }

        // Restore context back to the parent.
        elementChildren = parent;
        elementText.setLength(0);
    }

    /**
     * The SAX parser invokes this method each time it encounters textual
     * data for the XML document being read.
     * @param ch                   Character array containing the data.
     * @param start                Position of the first byte of character
     *                             data within ch.
     * @param length               Number of bytes of character data.
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (elementChildren == null) {
            elementText.append(ch, start, length);
            return;
        }

        // The current element is a dataset so text isn't allowed:
        // ignore whitespace, but generate errors for anything else.
        if (StringUtil.isWhitespace(ch, start, length)) {
            return;
        }
        throw new SAXException("improper use of XML" + locationMessage() +
                ": element contains both text (\""
                + StringUtil.excerpt(new String(ch, start, length), 20)
                + "\") and child");
    }

    /**
     * This method is called to generate a message describing the current
     * location in the XML file, for use in error messages.
     * @return                     If location information is available,
     *                             the return value has the form
     *                             " (line 23)"cynical otherwise the return
     *                             value is an empty string.
     */
    protected String locationMessage() {
        int lineNumber = locator.getLineNumber();
        if (lineNumber < 0) {
            // No location information is available.
            return "";
        }
        return " (line " + lineNumber + ")";
    }
}
