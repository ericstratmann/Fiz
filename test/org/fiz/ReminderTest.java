package org.fiz;

/**
 * Junit tests for the Reminder class.
 */

public class ReminderTest extends junit.framework.TestCase {
    protected static ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor() {
        Reminder reminder = new Reminder("id11", "name44");
        assertEquals("reminder name", "name44", reminder.name);
    }

    public void test_constructor_withValues() {
        Reminder reminder = new Reminder("id11", "xyzzy", "name", "Alice",
                "age", "18");
        assertEquals("reminder contents", "5.xyzzy(4.name5.Alice\n" +
                "3.age2.18", reminder.out.toString());
    }

    public void test_add_stringValues() {
        Reminder reminder = new Reminder("id11", "xyz");
        reminder.add("first", "12345", "second", "999", "noValue");
        assertEquals("after first call",
                "3.xyz(5.first5.12345\n" +
                "6.second3.999",
                reminder.out.toString());
        reminder.add("capital city", "Sacramento");
        assertEquals("after second call",
                "3.xyz(5.first5.12345\n" +
                "6.second3.999\n" +
                "12.capital city10.Sacramento",
                reminder.out.toString());
    }
    public void test_add_stringsAndDatasets() {
        Reminder reminder = new Reminder("id11", "xyz");
        reminder.add("first", "12345", "child", new Dataset("name", "Alice"),
                "second", "xxx", "child", new Dataset("age", "36"));
        assertEquals("Reminder.out",
                "3.xyz(5.first5.12345\n" +
                        "5.child(4.name5.Alice)\n" +
                        "6.second3.xxx\n" +
                        "5.child(3.age2.36)",
                reminder.out.toString());
        // See if the reminder decodes properly.
        Dataset d = new Dataset();
        String name = Reminder.decode(cr, reminder.get(cr), 0, d,
                new IntBox());
        assertEquals("Decoded reminder",
                "child:\n" +
                        "  - name: Alice\n" +
                        "  - age: 36\n" +
                        "first:  12345\n" +
                        "second: xxx\n",
                d.toString());
    }
    public void test_add_bogusValueType() {
        Reminder reminder = new Reminder("id11", "xyz");
        reminder.add("first", "12345");
        boolean gotException = false;
        try {
            reminder.add("value", 14);
        }
        catch (ClassCastException e) {
            assertEquals("error message",
                    "java.lang.Integer cannot be cast to org.fiz.Dataset",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_addFromDataset() {
        Dataset d = new Dataset("name", "Alice", "age", "34",
                "child", new Dataset("name", "Bob", "age", "6"),
                "child", new Dataset("name", "Carol", "age", "3"));
        Reminder reminder = new Reminder("id11", "xyz");
        reminder.addFromDataset(d, "child", "name");
        assertEquals("reminder contents",
                "3.xyz(5.child(3.age1.6\n" +
                "4.name3.Bob)\n" +
                "5.child(3.age1.3\n" +
                "4.name5.Carol)\n" +
                "4.name5.Alice",
                reminder.out.toString());
    }

    public void test_decode_basics() {
        Reminder reminder = new Reminder("id11", "name1");
        reminder.add("name", "value\000\"", "state", "California");
        reminder.out.append(")");
        StringBuilder out = new StringBuilder("xyz");
        reminder.appendHeader(cr, out);
        out.append(reminder.out);
        Dataset d = new Dataset("extra", "123");
        IntBox end = new IntBox();
        String name = Reminder.decode(cr, out, 3, d, end);
        assertEquals("reminder name", "name1", name);
        assertEquals("reminder dataset", "extra: 123\n" +
                "name:  \"value\\x00\\\"\"\n" +
                "state: California\n", d.toString());
    }
    public void test_decode_macLengthWrong() {
        Dataset d = new Dataset();
        IntBox end = new IntBox();
        boolean gotException = false;
        try {
            Reminder.decode(cr, "4.xxxx8.01234567", 0, d, end);
        }
        catch (Reminder.SignatureError e) {
            assertEquals("error message",
                    "reminder appears to have been tampered with: " +
                    "doesn't match signature", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_decode_macCorrupted() {
        // Create a reminder, then corrupt it.
        Reminder reminder = new Reminder("id11", "name1");
        reminder.add("name", "value\000\"", "state", "California");
        reminder.out.append(")");
        StringBuilder out = new StringBuilder();
        reminder.appendHeader(cr, out);
        out.append(reminder.out);
        out.setCharAt(10, (char) (out.charAt(10)+1));

        Dataset d = new Dataset();
        IntBox end = new IntBox();
        boolean gotException = false;
        try {
            Reminder.decode(cr, out, 0, d, end);
        }
        catch (Reminder.SignatureError e) {
            assertEquals("error message",
                    "reminder appears to have been tampered with: " +
                    "doesn't match signature", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_decode_reminderCorrupted() {
        // Create a reminder, then corrupt it.
        Reminder reminder = new Reminder("id11", "name1");
        reminder.add("name", "value\000\"", "state", "California");
        reminder.out.append(")");
        StringBuilder out = new StringBuilder();
        reminder.appendHeader(cr, out);
        out.append(reminder.out);
        int i = out.length() -1;
        out.setCharAt(i, (char) (out.charAt(i)+1));

        Dataset d = new Dataset();
        IntBox end = new IntBox();
        boolean gotException = false;
        try {
            Reminder.decode(cr, out, 0, d, end);
        }
        catch (Reminder.SignatureError e) {
            assertEquals("error message",
                    "reminder appears to have been tampered with: " +
                    "doesn't match signature", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_flush() {
        Reminder reminder = new Reminder("id\00211", "xyz\"abc");
        reminder.add("name", "value\000\"");
        reminder.flush(cr);

        // Replace the signature with a fixed value, so we don't need to
        // worry about the particular MAC algorithm.
        String javascript = cr.getHtml().jsCode.toString().replaceFirst(
                "43\\..*26\\.7\\.xyz", "43.--MAC skipped--26.7.xyz");
        assertEquals("generated Javascript",
                "Fiz.Reminder.reminders[\"id\\x0211\"] = \"43." +
                "--MAC skipped--26.7.xyz\\\"abc(4.name7.value\\x00\\\")\";\n",
                javascript);

        // Make sure the method erased the closing parenthesis.
        assertEquals("last character of Reminder.out", "\"",
                reminder.out.substring(reminder.out.length() - 1));
    }

    public void test_get() {
        Reminder reminder = new Reminder("id11", "xyz\"abc");
        reminder.add("name", "value\000\"");
        String value = reminder.get(cr);

        // Replace the signature with a fixed value, so we don't need to
        // worry about the particular MAC algorithm.
        String s = value.substring(0,3) + "--MAC skipped--" +
                value.substring(46);
        assertEquals("reminder contents",
                "43.--MAC skipped--26.7.xyz\"abc(4.name7.value\u0000\")",
                s);

        // Make sure the method erased the closing parenthesis.
        assertEquals("last character of Reminder.out", "\"",
                reminder.out.substring(reminder.out.length() - 1));
    }

    public void test_getJsReference() {
        assertEquals("Javascript expression",
                "Fiz.Reminder.reminders[\"xyzzy\"]",
                Reminder.getJsReference("xyzzy"));
    }

    public void test_appendHeader() {
        Reminder reminder = new Reminder("id11", "xyz\"abc");
        reminder.add("name", "value\000\"");
        reminder.out.append(")");
        StringBuilder out = new StringBuilder("xyz");
        reminder.appendHeader(cr, out);
        assertEquals("first part of header", "xyz43.",
                out.substring(0, 6));
        assertEquals("last part of header", "26.",
                out.substring(out.length() -3));
    }

    public void test_parseLength_basics() {
        IntBox end = new IntBox();
        int value = Reminder.parseLength("123.12.aaabbbcccddd", 4, end);
        assertEquals("integer value", 12, value);
        assertEquals("end of value", 7, end.value);
    }
    public void test_parseLength_noPeriod() {
        IntBox end = new IntBox();
        boolean gotException = false;
        try {
            Reminder.parseLength("1234", 1, end);
        }
        catch (Reminder.SyntaxError e) {
            assertEquals("error message",
                    "syntax error in reminder: length field \"234\" " +
                    "not terminated by \".\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_parseLength_lengthNegative() {
        IntBox end = new IntBox();
        boolean gotException = false;
        try {
            Reminder.parseLength("19999999999.2", 0, end);
        }
        catch (Reminder.SyntaxError e) {
            assertEquals("error message",
                    "syntax error in reminder: illegal length " +
                    "field \"19999999999\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_parseLength_lengthTooLarge() {
        IntBox end = new IntBox();
        boolean gotException = false;
        try {
            Reminder.parseLength("3.xy", 0, end);
        }
        catch (Reminder.SyntaxError e) {
            assertEquals("error message",
                    "syntax error in reminder: illegal length " +
                    "field \"3\"", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
