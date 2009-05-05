package org.fiz;

/**
 * Junit tests for the Link class.
 */

public class ButtonTest extends junit.framework.TestCase {
    protected ClientRequest cr;

    public void setUp() {
        cr = new ClientRequestFixture();
    }

    public void test_constructor_and_render () {
        Button button = new Button(new Dataset ("text", "Clear @name",
                "ajaxUrl", "/fiz/form/clear"));
        StringBuilder out = new StringBuilder();
        button.render(cr, new Dataset("name", "Alice"), out);
        assertEquals("HTML for Button",
                "<button onclick=\"void new Fiz.Ajax({url: " +
                "&quot;/fiz/form/clear&quot;}); return false;\">" +
                "Clear Alice</button>",
                out.toString());
    }

    public void test_render_static_ajaxWithClass() {
        Dataset properties = new Dataset("text", "special: @special",
                "ajaxUrl", "/fiz/a/b?user=@name", "class", "class123");
        Dataset data = new Dataset("name", "\"Alice\"", "special", "<>");
        StringBuilder out = new StringBuilder();
        Button.render(cr, properties, data, out);
        assertEquals("HTML for Button",
                "<button class=\"class123\" onclick=\"void new Fiz.Ajax(" +
                "{url: &quot;/fiz/a/b?user=%22Alice%22&quot;}); " +
                "return false;\">" +
                "special: &lt;&gt;</button>",
                out.toString());
    }
    public void test_render_static_javascriptNoClass() {
        Dataset properties = new Dataset("text", "special: @special",
                "javascript", "alert(\"@name\");");
        Dataset data = new Dataset("name", "\"Alice\"", "special", "<>");
        StringBuilder out = new StringBuilder();
        Button.render(cr, properties, data, out);
        assertEquals("HTML for Button",
                "<button onclick=\"alert(&quot;\\&quot;Alice\\&quot;" +
                "&quot;); return false;\">special: &lt;&gt;</button>",
                out.toString());
    }
}
