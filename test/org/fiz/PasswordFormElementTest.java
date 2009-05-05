package org.fiz;

/**
 * Junit tests for the PasswordFormElement class.
 */
public class PasswordFormElementTest extends junit.framework.TestCase {
    public void test_constructor_withIdAndLabel() {
        PasswordFormElement element = new PasswordFormElement(
                "id11", "label22");
        assertEquals("properties dataset", "id:    id11\n" +
                "label: label22\n", element.properties.toString());
    }

    public void test_collect_validateOK()
            throws FormSection.FormDataException {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "secret", "duplicate", "secret2"));
        Dataset out = new Dataset();
        element.collect(cr, new Dataset("secret", "value2",
                "secret2", "value2"), out);
        assertEquals("collected dataset", "", out.toString());
    }
    public void test_collect_validateFailedFirstMissing() {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "secret", "duplicate", "secret2"));
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            element.collect(cr, new Dataset("secret2", "value2"),
                    new Dataset());
        }
        catch (FormSection.FormDataException e) {
            assertEquals("exception message",
                    "the passwords are not the same", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_collect_validateFailedSecondMissing() {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "secret", "duplicate", "secret2"));
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            element.collect(cr, new Dataset("secret", "xyzzy"),
                    new Dataset());
        }
        catch (FormSection.FormDataException e) {
            assertEquals("exception message",
                    "the passwords are not the same", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_collect_validateFailed() {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "secret", "duplicate", "secret2"));
        boolean gotException = false;
        Dataset d = new Dataset("a", "a_value");
        try {
            element.collect(cr, new Dataset("secret", "xyzzy",
                    "secret2", "bogus"),new Dataset());
        }
        catch (FormSection.FormDataException e) {
            assertEquals("exception message",
                    "the passwords are not the same", e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_collect_justCopyValue()
            throws FormSection.FormDataException {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "secret"));
        Dataset out = new Dataset();
        element.collect(cr, new Dataset("secret", "xyzzy",
                "secret2", "value2"), out);
        assertEquals("collected dataset", "secret: xyzzy\n", out.toString());
    }

    public void test_render_defaultClass() {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                "secret", "Secret:");
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("secret", "<confidential>"), out);
        assertEquals("CSS includes", "PasswordFormElement.css",
                cr.getHtml().getCssFiles());
        assertEquals("generated HTML",
                "<input type=\"password\" name=\"secret\" " +
                "class=\"PasswordFormElement\" />",
                out.toString());
    }
    public void test_render_explicitClass() {
        ClientRequest cr = new ClientRequestFixture();
        PasswordFormElement element = new PasswordFormElement(
                new Dataset("id", "secret", "class", "xyzzy"));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset(), out);
        assertEquals("generated HTML",
                "<input type=\"password\" name=\"secret\" " +
                "class=\"xyzzy\" />",
                out.toString());
    }
}
