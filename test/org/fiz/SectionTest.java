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

/**
 * Junit tests for the Section class.
 */
public class SectionTest extends junit.framework.TestCase {
    protected static class SectionFixture extends Section {
        public SectionFixture(Dataset properties) {
            this.properties = properties;
        }
        @Override
        public void render(ClientRequest cr){}
    }

    // No unit tests for addDataRequests, because it doesn't do anything.

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
}
