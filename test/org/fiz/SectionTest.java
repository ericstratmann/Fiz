package org.fiz;

/**
 * Junit tests for the Section class.
 */
public class SectionTest extends junit.framework.TestCase {
    protected static class SectionFixture extends Section {
        public SectionFixture(Dataset properties) {
            this.properties = properties;
        }
        @Override
        public void html(ClientRequest cr){}
    }

    public void test_checkId() {
        Section section = new SectionFixture(null);
        assertEquals("properties null", null, section.checkId());
        section.properties = new Dataset();
        assertEquals("properties exists but no id", null, section.checkId());
        section.properties.set("id", "id44");
        assertEquals("id value exists", "id44", section.checkId());
    }

    public void test_getId_noProperties() {
        Section section = new SectionFixture(null);
        boolean gotException = false;
        try {
            section.getId();
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "SectionFixture object has no id",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getId_noIdProperty() {
        Section section = new SectionFixture(new Dataset());
        boolean gotException = false;
        try {
            section.getId();
        }
        catch (InternalError e) {
            assertEquals("exception message",
                    "SectionFixture object has no id",
                    e.getMessage());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
    public void test_getId_idExists() {
        Section section = new SectionFixture(new Dataset("id", "id44"));
        assertEquals("id value exists", "id44", section.getId());
    }

    public void test_registerRequests() {
        ClientRequest cr = new ClientRequestFixture();
        Section section = new SectionFixture(null);
        section.registerRequests(cr);
        assertEquals("registered request names (no properties)", "",
                cr.getRequestNames());
        assertTrue("dataRequest value null", section.dataRequest == null);

        section.properties = new Dataset("request", "getState");
        section.registerRequests(cr);
        assertEquals("registered request names", "getState",
                cr.getRequestNames());
        assertEquals("dataRequest manager", "file",
                section.dataRequest.getRequestData().get("manager"));
    }
}
