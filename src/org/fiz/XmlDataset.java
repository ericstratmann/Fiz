/**
 * XmlDataset allows XML documents to be accessed using the standard
 * Dataset mechanisms.
 */

package org.fiz;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class XmlDataset {
    // It is *much* faster to parse an XML document with a reused parser
    // than to create a fresh parser for each document (more than 10x
    // faster for simple documents).  The following variable points to
    // a reusable parser (or null if there hasn't yet been a need for a
    // parser.  The down-side of this is that the "parse" method needs to
    // be synchronized so that only one XML dataset parse is going
    // on at a time, but the efficiency savings are so great that this
    // seems worthwhile.
    static SAXParser parser = null;

    /**
     * Creates a dataset from an XML input string.
     * @param s                    String in XML format
     * @return                     Dataset containing information from the XML.
     * @throws Dataset.SyntaxError There was a problem with the XML document:
     *                             either it wasn't valid XML or it used valid
     *                             XML constructs that aren't supported for
     *                             XMLDatasets.
     */
    public static synchronized Dataset newStringInstance(String s)
            throws Dataset.SyntaxError {
        return parse(s, null);
    }

    /**
     * Creates a dataset from the contents of an XML file.
     * @param fileName             Name of a file describing a dataset in
     *                             XML format.
     * @return                     Dataset containing information from the XML.
     * @throws Dataset.SyntaxError There was a problem with the XML document:
     *                             either it wasn't valid XML or it used valid
     *                             XML constructs that aren't supported for
     *                             XMLDatasets.
     */
    public static synchronized Dataset newFileInstance(String fileName)
            throws Dataset.SyntaxError {
        return parse(null, fileName);
    }

    /**
     * This shared method does all of the work of both newStringInstance
     * and newFileInstance.
     * @param s                    String containing XML, or null if the
     *                             XML is in a file.  Ignored if fileName
     *                             isn't null.
     * @param fileName             Name of a file containing XML, or null
     *                             if the XML is in <code>s</code>.
     * @return                     Dataset containing information from the XML.
     * @throws Dataset.SyntaxError There was a problem with the XML document:
     *                             either it wasn't valid XML or it used valid
     *                             XML constructs that aren't supported for
     *                             XMLDatasets.
     * @throws IOError             There was an I/O error reading a file.
     */
    protected static synchronized Dataset parse(String s, String fileName)
            throws Dataset.SyntaxError, IOError {
        try {
            if (parser == null) {
                parser = SAXParserFactory.newInstance().newSAXParser();
            } else {
                parser.reset();
            }
            HashMap<String,Object> map = new HashMap<String,Object>();
            XmlDatasetSAXHandler handler = new XmlDatasetSAXHandler(map);
            if (fileName != null) {
                parser.parse(new File(fileName), handler);
            } else {
                parser.parse(new InputSource(new StringReader(s)), handler);
            }
            return new Dataset(map, null);
        }
        catch (SAXException e) {
            throw new Dataset.SyntaxError(null, e.getMessage());
        }
        catch (ParserConfigurationException e) {
            throw new Error("SAX parser configuration error: " +
                    e.getMessage());
        }
        catch (IOException e) {
            if (fileName != null) {
                throw IOError.newFileInstance(fileName, e.getMessage());
            } else {
                throw new IOError("I/O error reading XML dataset: " +
                        e.getMessage());
            }
        }
    }
}
