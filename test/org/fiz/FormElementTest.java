package org.fiz;

/**
 * Junit tests for the FormElement class.
 */
public class FormElementTest extends junit.framework.TestCase {
    // The following class definition fills in the abstract elements of
    // FormElement to create a class that can be instantiated for testing.
    private static class FormElementFixture extends FormElement {
        public FormElementFixture(Dataset properties) {
            super(properties);
        }
        public void html(ClientRequest cr, Dataset data, StringBuilder out) {
            // Do nothing.
        }
    }

    public void test_constructor() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "4815162342", "name", "Alice"));
        assertEquals("properties dataset", "id:   4815162342\n" +
                "name: Alice\n", element.properties.toString());
        assertEquals("element id", "4815162342", element.id);
    }

    public void test_collect() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "name", "Alice"));
        Dataset out = new Dataset("name", "Bob", "age", "30");
        element.collect(null, new Dataset("name", "Carol", "age", "36"),
                out);
        assertEquals("output dataset", "age:  30\n" +
                "name: Carol\n", out.toString());
    }

    public void test_getId() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "age", "8"));
        assertEquals("return value", "name", element.getId());
    }

    public void test_labelHtml_withTemplate() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "age", "label", "Age of @name:"));
        StringBuilder out = new StringBuilder();
        element.labelHtml(null, new Dataset("name", "<Bob>", "age", "30"),
                out);
        assertEquals("generated HTML", "Age of &lt;Bob&gt;:", out.toString());
    }

    public void test_labelHtml_noTemplate() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "<age>"));
        StringBuilder out = new StringBuilder();
        element.labelHtml(null, new Dataset("name", "<Bob>", "age", "30"),
                out);
        assertEquals("generated HTML", "&lt;age&gt;", out.toString());
    }

    public void test_registerRequests() {
        ClientRequest cr = new ClientRequestFixture();
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "age"));
        element.registerRequests(cr, "xyz");
        assertEquals("registered request names", "",
                cr.getRequestNames());
    }

    public void test_responsibleFor() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "age"));
        assertEquals("responsible for this element", true,
                element.responsibleFor("age"));
        assertEquals("not responsible for this element", false,
                element.responsibleFor("name"));
    }
}
