package org.fiz;

import java.io.*;
import java.util.*;
import javax.crypto.*;

/**
 * A Reminder is a dataset whose contents are transmitted to the
 * browser, stored there as part of a page, and returned to the
 * server later (typically in an Ajax request).  Reminders are
 * used for data that will be needed later, but for which there is
 * not a great place to store the data on the server.  For example,
 * a section might use a reminder to store configuration information
 * that will be needed to handle Ajax requests.  This class provides
 * facilities for serializing the data so that it can be transmitted
 * to the browser, deserializing data that is returned by the browser,
 * and preventing fabrication or tampering with reminders by using
 * cryptographic checksums.
 */
public class Reminder {
    /**
     * Construct a SignatureError.
     */
    public static class SignatureError extends Error {
        public SignatureError() {
            super("reminder appears to have been tampered with: " +
                    "doesn't match signature");
        }
    }

    /**
     * SyntaxError is thrown when the {@code keycode} method discovers
     * that an incoming reminder doesn't have the expected structure.
     */
    public static class SyntaxError extends Error {
        /**
         * Construct a SyntaxError with a message.
         * @param message          Detailed information about the problem.
         */
        public SyntaxError(String message) {
            super("syntax error in reminder: " + message);
        }
    }

    // Arguments passed to the constructor:
    protected String id;
    protected String name;

    // Used to accumulate the serialized representation for the Reminder,
    // which has the following form:
    // <nameLength>.<name><dataset>
    // where <name> is the name of this reminder, <nameLength> is the
    // number of characters in <name>, and <dataset> is the reminder's
    // data, in the same form used for serialized datasets (see
    // Dataset.serialize for details).

    protected StringBuilder out = new StringBuilder();

    // If the following variable is true it means that no data has
    // been added to this reminder yet.
    protected boolean outEmpty = true;

    // The following variable is set to true during some tests; this
    // causes the normal signature generation and checking algorithms to
    // be bypassed, using instead a single fixed signature (so that
    // tests don't have to worry about the signature being different
    // every round).
    protected static boolean testMode = false;

    /**
     * Construct a Reminder with a given name.
     * @param id                   Identifier for this Reminder, used as a
     *                             name for the reminder in the browser and
     *                             also used to identify the reminder in the
     *                             getJsReference method.  Must be unique
     *                             among all Reminders for the Web page;
     *                             typically this is achieved by using the
     *                             id of a related HTML element as the id of
     *                             the Reminder.
     * @param name                 When the Reminder is returned to the
     *                             server, it will be available under this
     *                             name, which typically reflects how the
     *                             Reminder will be used.
     */
    public Reminder(String id, String name) {
        this.id = id;
        this.name = name;
        this.out.append(name.length());
        this.out.append('.');
        this.out.append(name);
        this.out.append('(');
    }

    /**
     * Construct a Reminder with a given name and an initial set of values.
     * @param id                   Identifier for this Reminder, used as a
     *                             name for the reminder in the browser.
     *                             Must be unique among all Reminders for
     *                             the Web page; typically this is achieved by
     *                             using the id of a related HTML element as
     *                             the id of the Reminder.
     * @param name                 When the Reminder is returned to the
     *                             server, it will be available under this
     *                             name, which typically reflects how the
     *                             Reminder will be used.
     * @param namesAndValues       An array of strings, where the first
     *                             element is the name for a Reminder entry,
     *                             the second element is the value of that
     *                             entry (either a String or a Dataset), the
     *                             third element is the name for another
     *                             Reminder entry, and so on.
     */
    public Reminder(String id, String name, Object ... namesAndValues) {
        this(id, name);
        add(namesAndValues);
    }

    /**
     * Add any number of name-value pairs to this Reminder.
     * @param namesAndValues       An array of strings, where the first
     *                             element is the name for a Reminder entry,
     *                             the second element is the value of that
     *                             entry (either a String or a Dataset), the
     *                             third element is the name for another
     *                             Reminder entry, and so on.
     */
    public void add(Object ... namesAndValues) {
        int last = namesAndValues.length - 2;
        for (int i = 0; i <= last; i += 2) {
            if (outEmpty) {
                outEmpty = false;
            } else {
                out.append('\n');
            }
            String name = (String) namesAndValues[i];
            out.append(name.length());
            out.append('.');
            out.append(name);
            Object value = namesAndValues[i+1];
            if (value instanceof String) {
                String s = (String) value;
                out.append(s.length());
                out.append('.');
                out.append(s);
            } else {
                ((Dataset) value).serialize(out);
            }
        }
    }

    /**
     * Add one or more top-level values from an existing dataset to
     * this reminder.
     * @param d                    Dataset containing values to be added
     *                             to the reminder.
     * @param names                Any number of names of top-level values
     *                             in the dataset (not paths).  Each name
     *                             and its corresponding value is added to the
     *                             reminder.  Each value can be either a
     *                             string or nested dataset(s).
     */
    @SuppressWarnings("unchecked")
    public void addFromDataset(Dataset d, String ... names) {
        for (String name: names) {
            ArrayList<Object> values = (ArrayList<Object>) d.lookup(name,
                    Dataset.DesiredType.ANY, Dataset.Quantity.ALL);
            if (values == null) {
                continue;
            }
            for (Object value: values) {
                add(name, value);
            }
        }
    }

    /**
     * Parse a signed reminder from an input source, making sure that the
     * reminder has been signed by the current session.
     * @param cr                   Overall information about the client
     *                             request being serviced.  Used to locate
     *                             the Mac for the current session.
     * @param input                Holds the reminder.
     * @param start                Index within {@code input} of the first
     *                             character of the reminder.
     * @param d                    Information from the reminder will be
     *                             added to this dataset.
     * @param end                  The value will be set to the index in
     *                             {@code input} of the character just after
     *                             the last one in the reminder.
     * @return                     The name of this reminder.
     * @throws SignatureError      The Mac signature for the incoming reminder
     *                             does not make sense for this session.
     */
    public static String decode(ClientRequest cr, CharSequence input,
            int start, Dataset d, IntBox end) throws SignatureError {
        // Break the incoming data into the Mac signature and the main
        // body of the reminder.
        int i = start;
        int macLength = parseLength(input, i, end);
        byte[] incomingMac = StringUtil.decode4to3(input, end.value,
                macLength);
        i = end.value + macLength;
        int reminderLength = parseLength(input, i, end);
        int reminderStart = end.value;

        // Validate the signature.
        Mac mac = cr.getMac();
        byte[] macBytes;
        try {
            if (testMode) {
                // Special mode for some tests: generate a fixed signature.
                macBytes = "**fake signature**".getBytes("UTF-8");
            } else {
                macBytes = mac.doFinal(input.subSequence(reminderStart,
                        reminderStart + reminderLength).toString().getBytes(
                        "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(
                    "Reminder.decode couldn't convert to UTF-8");
        }
        if (macBytes.length != incomingMac.length) {
            throw new SignatureError();
        }
        for (int j = 0; j < macBytes.length; j++) {
            if (macBytes[j] != incomingMac[j]) {
                throw new SignatureError();
            }
        }
        i = reminderStart;

        // The signature is OK; now decode the name of the reminder and its
        // contents.

        int nameLength = parseLength(input, i, end);
        i = end.value;
        String name = input.subSequence(i, i+nameLength).toString();
        i += nameLength;
        end.value = d.addSerializedData(input, i);
        return name;
    }

    /**
     * Once all of the data for a reminder has been specified with calls to
     * methods such as {@code add}, this method is invoked to output the
     * reminder as part of the current Web page (or Ajax response).
     * @param cr                   Overall information about the client
     *                             request being serviced.  Javascript
     *                             will be added to this request in order
     *                             to record the Reminder in the browser.
     */
    public void flush(ClientRequest cr) {
        cr.getHtml().includeJsFile("fizlib/Reminder.js");
        out.append(')');
        StringBuilder header = new StringBuilder(50);
        appendHeader(cr, header);
        StringBuilder javascript = new StringBuilder(out.length() + 60);
        Template.expand("Fiz.Reminder.reminders[\"@1\"] = \"@2@3\";\n",
                javascript, Template.SpecialChars.JAVASCRIPT, id,
                header, out);
        cr.evalJavascript(javascript);

        // Remove the closing parenthesis, in case the caller decides to
        // add more information to the reminder.
        out.setLength(out.length()-1);
    }

    /**
     * Returns a string containing the contents of the reminder along with
     * a MAC signature; this value can be passed to {@code decode} to extract
     * the data from the reminder.
     * @param cr                   Overall information about the client
     *                             request being serviced.  Used to generate
     *                             the MAC signature.
     * @return                     The complete reminder, including both
     *                             encoded data and signature.
     */
    public String get(ClientRequest cr) {
        out.append(')');
        StringBuilder reminder = new StringBuilder(50);
        appendHeader(cr, reminder);
        reminder.append(out);

        // Remove the closing parenthesis, in case the caller decides to
        // add more information to the reminder.
        out.setLength(out.length()-1);

        return reminder.toString();
    }

    /**
     * Given the id for a reminder, returns a Javascript expression that,
     * when executed in the browser, will evaluate to the contents of
     * this reminder.
     * @param id                   Identifier that was used to create the
     *                             reminder.
     * @return                     See above.
     */
    public static String getJsReference(String id) {
        return Template.expand("Fiz.Reminder.reminders[\"@1\"]", id);
    }

    /**
     * This method generates all of the header information for this
     * reminder (everything except the serialized dataset containing
     * the reminder data) and appends it to a StringBuilder.  Note:
     * {@code out} must be complete before this method is invoked.
     * The main reason for separating this method from {@code code}
     * is to simplify testing.
     * @param cr                   Overall information about the client
     *                             request being serviced.  Used to locate
     *                             the Mac for the current session.
     * @param header               Mac header info is appended here.
     */
    protected void appendHeader(ClientRequest cr,
            StringBuilder header) {
        // Create a cryptographic checksum for the reminder.  This prevents
        // the browser from tampering with the contents of the reminder.
        Mac mac = cr.getMac();
        byte[] macBytes;
        try {
            if (testMode) {
                // Special mode for some tests: generate a fixed signature.
                macBytes = "**fake signature**".getBytes("UTF-8");
            } else {
                macBytes = mac.doFinal(out.toString().getBytes("UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(
                    "Reminder.flush couldn't convert to UTF-8");
        }

        // The reminder has the form
        //     <macLength>.<encodedMac><reminderLength>.<reminder>
        // where <macLength> is a decimal integer giving the number of
        // encoded characters in the MAC and <reminderLength> is a decimal
        // integer giving the number of characters in the reminder itself.
        // The MAC is encoded in an ASCII format to allow it to pass safely
        // into and out of the browser.
        header.append(StringUtil.lengthEncoded3to4(macBytes.length));
        header.append('.');
        StringUtil.encode3to4(macBytes, header);
        header.append(out.length());
        header.append('.');
    }

    /**
     * Parse a substring of the form "ddd." from a string, where the
     * {@code d} characters form a decimal integer indicating the length in
     * characters of a substring following the "."  This method does not
     * validate that the characters are actually decimal digits; it assumes
     * that all characters up until the next "." are decimal digits.
     * @param s                    Input containing a decimal integer.
     * @param start                Index within {@code s} of the first
     *                             character of the integer.
     * @param end                  Used to return the index in {@code s}
     *                             of the character just after the "." that
     *                             terminated the integer.
     * @return                     The return value is the value of the
     *                             integer parsed from {@code s}.  In
     *                             addition, {@code end.value} is set
     *                             as described above.
     * @throws SyntaxError         There was no "." or the integer was
     *                             out of range.
     */
    protected static int parseLength(CharSequence s, int start, IntBox end)
            throws SyntaxError {
        int length = s.length();
        int i = start;
        int value = 0;
        while (true) {
            if (i >= length) {
                throw new SyntaxError("length field \"" +
                        s.subSequence(start, length) +
                        "\" not terminated by \".\"");
            }
            char c = s.charAt(i);
            if (c == '.') {
                break;
            }
            value = value*10 + (c - '0');
            i++;
        }
        end.value = i+1;
        if ((value < 0) || ((end.value + value) > length)) {
                throw new SyntaxError("illegal length field \"" +
                        s.subSequence(start, i) + "\"");
        }
        return value;
    }
}
