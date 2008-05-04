package org.fiz;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/**
 * XmlDataset allows XML documents to be accessed using the standard
 * Dataset mechanisms.  XML documents must use a restricted subset of XML
 * that corresponds to the facilities provided by datasets:
 *  - Elements with children become nested datasets; elements without
 *    children become string values.
 *  - If multiple elements in the same parent have the same name, then they
 *    must all have children: they become a list of nested datasets.
 *  - If an element has children than it must not contain any text except
 *    whitespace (which is ignored).
 *  - Whitespace is not ignored in elements without children.
 *  - All attributes are ignored.
 *  - The name of the top-level element of the XML document is irrelevant
 *    and ignored.
 */

public class XmlDataset extends Dataset {

    /**
     * This method is only for the use of the static constructors
     * below.
     * @param contents             Main HashMap for the dataset.
     * @param fileName             Name of the file from which this dataset
     *                             was read, or null if none.
     */
    private XmlDataset(HashMap contents, String fileName) {
        super(contents, fileName);
    }

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
     * Create a dataset from an XML input string.
     * @param s                    String in XML format
     * @return                     Dataset containing information from the XML.
     * @throws Dataset.SyntaxError There was a problem with the XML document:
     *                             either it wasn't valid XML or it used valid
     *                             XML constructs that aren't supported for
     *                             XMLDatasets.
     */
    public static synchronized XmlDataset newStringInstance(String s)
            throws Dataset.SyntaxError {
        return parse(s, null);
    }

    /**
     * Create a dataset from the contents of an XML file.
     * @param fileName             Name of a file describing a dataset in
     *                             XML format.
     * @return                     Dataset containing information from the XML.
     * @throws Dataset.SyntaxError There was a problem with the XML document:
     *                             either it wasn't valid XML or it used valid
     *                             XML constructs that aren't supported for
     *                             XMLDatasets.
     */
    public static synchronized XmlDataset newFileInstance(String fileName)
            throws Dataset.SyntaxError {
        return parse(null, fileName);
    }

    /**
     * Convert a dataset back to XML format.  This method can be used
     * on any dataset, even those that didn't originally come from XML.
     * @param d                    Dataset to convert.
     * @return                     A string describing the contents of the
     *                             dataset using XML syntax.
     */
    public static String writeString(Dataset d) {
        try {
            StringWriter out = new StringWriter();
            out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            out.append("<dataset>\n");
            writeSubtree(d.map, out, "  ");
            out.append("</dataset>\n");
            return out.toString();
        }
        catch (IOException e) {
            throw new IOError(e.getMessage());
        }
    }

    /**
     * Convert the dataset back to XML format.
     * @return                     A string describing the contents of the
     *                             dataset using XML syntax.
     */
    public String toString() {
        return XmlDataset.writeString(this);
    }

    /**
     * Create a file in XML format describing the contents of a dataset.
     * This method can be used on any dataset, even one that didn't
     * originally come from XML.
     * @param d                    Dataset to write to the file.
     * @param name                 Name of file; this file will be overwritten
     *                             if it already exists.  If the name doesn't
     *                             end with an extension, a ".xml" extension
     *                             is added.
     * @param comment              Optional text describing the meaning of
     *                             the file for humans who might stumble
     *                             across it.  If non-null, this is turned
     *                             into an XML comment by adding appropriate
     *                             comment characters and then output at the
     *                             beginning of the file, before the generated
     *                             XML.  May contain embedded newlines,
     *                             which will result in a multi-line comment.
     * @throws FileNotFoundError   The file couldn't be opened.
     * @throws IOError             An error occurred while writing or
     *                             closing the file.
     */
    public static void writeFile(Dataset d, String name, String comment) {
        FileWriter out;
        try {
            name = StringUtil.addExtension(name, ".xml");
            out = new FileWriter(name);
        }
        catch  (IOException e) {
            throw new FileNotFoundError(name, "XML dataset", e.getMessage());
        }
        try {
            out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            if (comment != null) {
                out.append("<!-- ");
                out.append(comment.replace("\n", "\n     "));
                out.append (" -->\n");
            }
            out.append("<dataset>\n");
            writeSubtree(d.map, out, "  ");
            out.append("</dataset>\n");
            out.close();
            if (d.generateIoException) {
                // We get here only during testing.
                throw new IOException("error simulated");
            }
        }
        catch (IOException e) {
            throw IOError.newFileInstance(name, e.getMessage());
        }
    }

    /**
     * Create a file in XML format describing the contents of the dataset.
     * @param name                 Name of file; this file will be overwritten
     *                             if it already exists.  If the name doesn't
     *                             end with an extension, a ".xml" extension
     *                             is added.
     * @param comment              Optional text describing the meaning of
     *                             the file for humans who might stumble
     *                             across it.  If non-null, this is turned
     *                             into a XML comment by adding appropriate
     *                             comment characters and then output at the
     *                             beginning of the file, before the generated
     *                             XML.  May contain embedded newlines,
     *                             which will result in a multi-line comment.
     * @throws FileNotFoundError   The file couldn't be opened.
     * @throws IOError             An error occurred while writing or
     *                             closing the file.
     */
    public void writeFile(String name, String comment) {
        XmlDataset.writeFile(this, name, comment);
    }

    /**
     * This recursive method provides most of the functionality for writing
     * datasets in XML format.  Each invocation is responsible for writing
     * a subtree of the full dataset.
     * @param dataset              HashMap that holds the dataset subtree
     *                             to be written.
     * @param writer               Append XML information here.
     * @param indent               Output this string at the beginning of
     *                             each line of output; contains spaces
     *                             for indentation.
     * @throws IOException         Thrown by writer if it encounters a
     *                             problem.
     */
    @SuppressWarnings("unchecked")
    protected static void writeSubtree(HashMap dataset, Writer writer,
            String indent) throws IOException {
        StringBuilder quotedValue = new StringBuilder();
        ArrayList names = new ArrayList();
        names.addAll(dataset.keySet());
        Collections.sort(names);
        for (Object nameObject: names) {
            String name = (String) nameObject;
            Object value = dataset.get(name);
            if (value instanceof HashMap) {
                writer.append(String.format("%s<%s>\n", indent, name));
                writeSubtree((HashMap) value, writer, indent + "  ");
                writer.append(String.format("%s</%s>\n", indent, name));
            } else if (value instanceof ArrayList) {
                ArrayList<HashMap> list = (ArrayList <HashMap>) value;
                for (int i = 0; i < list.size(); i++) {
                    writer.append(String.format("%s<%s>\n", indent, name));
                    writeSubtree(list.get(i), writer, indent + "  ");
                    writer.append(String.format("%s</%s>\n", indent, name));
                }
            } else {
                quotedValue.setLength(0);
                Html.escapeHtmlChars(value.toString(), quotedValue);
                writer.append(String.format("%s<%s>%s</%s>\n", indent, name,
                        quotedValue.toString(), name));
            }
        }
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
    protected static synchronized XmlDataset parse(String s, String fileName)
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
            return new XmlDataset(map, null);
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
