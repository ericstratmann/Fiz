package org.fiz;

/**
 * Junit tests for the Ajax class.
 */

public class AjaxTest extends junit.framework.TestCase {
    public void test_SyntaxError() {
        Ajax.SyntaxError error = new Ajax.SyntaxError("missing comma");
        assertEquals("missing comma", error.getMessage());
    }

    public void test_readInputData() {
        String source = "3.age2.24\n" +
                "8.children(4.name5.Alice\n" +
                ")(4.name3.Bob\n" +
                ")(4.name5.Carol\n" +
                ")\n";
        Dataset out = new Dataset("weight", "125");
        Ajax.readInputData(source, out);
        assertEquals("contents of dataset",
                "age:    24\n" +
                "children:\n" +
                "  - name: Alice\n" +
                "  - name: Bob\n" +
                "  - name: Carol\n" +
                "weight: 125\n", out.toString());
    }

    public void test_getDataset_emptyInput() {
        Dataset out = new Dataset("weight", "125");
        assertEquals("return value", 0, Ajax.getDataset("", 0, '\0', out));
        assertEquals("contents of dataset", "weight: 125\n", out.toString());
    }
    public void test_getDataset_emptyNestedDataset() {
        Dataset out = new Dataset();
        assertEquals("return value", 4,
                Ajax.getDataset("xxx)\n", 3, ')', out));
        assertEquals("contents of dataset", "", out.toString());
    }
    public void test_getDataset_missingValue() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            Ajax.readInputData("4.name", out);
        }
        catch (Ajax.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in Ajax data input: no value for " +
                    "element \"name\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getDataset_singleNestedDataset() {
        String source = "3.age2.24\n" +
                "5.child(4.name5.Alice\n" +
                "3.age3.100\n" +
                ")\n" +
                "9.eye color5.brown\n";
        Dataset out = new Dataset();
        Ajax.readInputData(source, out);
        assertEquals("contents of dataset",
                "age:       24\n" +
                "child:\n" +
                "    age:  100\n" +
                "    name: Alice\n" +
                "eye color: brown\n", out.toString());
    }
    public void test_getDataset_parensInValue() {
        String source = "3.age2.24\n" +
                "5.child16.(4.name5.Alice\n" +
                ")\n";
        Dataset out = new Dataset();
        Ajax.readInputData(source, out);
        assertEquals("contents of dataset",
                "age:   24\n" +
                "child: \"(4.name5.Alice\\n)\"\n", out.toString());
    }
    public void test_getDataset_missingNewline() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            Ajax.readInputData("4.name5.Alice", out);
        }
        catch (Ajax.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in Ajax data input: no newline after " +
                    "element \"name\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getDataset_missingCloseParen() {
        boolean gotException = false;
        try {
            Dataset out = new Dataset();
            Ajax.readInputData("5.child(4.name5.Alice\n", out);
        }
        catch (Ajax.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in Ajax data input: missing \")\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_getString_missingDot() {
        boolean gotException = false;
        try {
            Ajax.GetStringResult result = new Ajax.GetStringResult();
            Ajax.getString("x.144", 2, result);
        }
        catch (Ajax.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in Ajax data input: missing \".\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getString_computeLength() {
        Ajax.GetStringResult result = new Ajax.GetStringResult();
        Ajax.getString("23.aaaaabbbbbcccccdddddeeeeefffff", 0, result);
        assertEquals("end index", 26, result.end);
        assertEquals("string value", "aaaaabbbbbcccccdddddeee", result.value);
    }
    public void test_getString_negativeLength() {
        boolean gotException = false;
        try {
            Ajax.GetStringResult result = new Ajax.GetStringResult();
            Ajax.getString("19999999999.", 0, result);
        }
        catch (Ajax.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in Ajax data input: improper length " +
                    "field \"19999999999\"",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getString_lengthTooBig() {
        boolean gotException = false;
        try {
            Ajax.GetStringResult result = new Ajax.GetStringResult();
            Ajax.getString("6.12345", 0, result);
        }
        catch (Ajax.SyntaxError e) {
            assertEquals("exception message",
                    "syntax error in Ajax data input: unexpected end of input",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
