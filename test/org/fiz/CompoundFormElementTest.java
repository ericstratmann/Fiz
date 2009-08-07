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

import org.fiz.test.*;

/**
 * Junit tests for the CompoundFormElement class.
 */
public class CompoundFormElementTest extends junit.framework.TestCase {
    protected static class ElementFixture extends FormElement {
        // The following variable is used to log events such as calls to
        // addDataRequests.
        protected static StringBuffer log = new StringBuffer();

        protected String template;
        public ElementFixture(String id, String template) {
            super(new Dataset("id", id));
            this.template = template;
        }
        @Override
        public void addDataRequests(ClientRequest cr, boolean empty) {
            log.append("addDataRequests " + id + " " +
                    ((empty) ? "empty" : "not empty") + "\n");
        }
        @Override
        public void render(ClientRequest cr, Dataset data, StringBuilder out) {
            Template.appendHtml(out, template, data);
        }
    }

    protected Dataset person = new Dataset(
            "record", new Dataset("name", "David", "age", "66",
            "height", "71", "weight", "220"));

    public void test_constructor() {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11", "template", "@1xyz@2"),
                new ElementFixture("id22", "element 22"),
                new ElementFixture("id44", "element 33"));
        assertEquals("properties dataset", "id:       id11\n" +
                "template: \"@1xyz@2\"\n", element.properties.toString());
        assertEquals("number of components", 2, element.components.length);
    }

    public void test_addDataRequests() {
        // First invocation: no request from the CompoundFormElement; only
        // its components.
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11"),
                new ElementFixture("name", "name @name"),
                new ElementFixture("age", "age @age"));
        ClientRequest cr = new ClientRequestFixture();
        ElementFixture.log.setLength (0);
        element.addDataRequests(cr, true);
        assertEquals("request names (components only)", "",
                cr.getRequestNames());
        assertEquals("element log", "addDataRequests name empty\n" +
                "addDataRequests age empty\n",
                ElementFixture.log.toString());
    }

    public void test_collect() throws FormSection.FormDataException {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11"),
                new ElementFixture("name", "name element"),
                new ElementFixture("age", "age element"));
        Dataset out = new Dataset();
        element.collect(null, new Dataset("name", "Alice", "age", "35",
                "height", "65"), out);
        assertEquals("result dataset", "age:  35\n" +
                "name: Alice\n", out.toString());
    }

    public void test_render_basics() {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11", "template", "@1, @2"),
                new ElementFixture("name", "name @name"),
                new ElementFixture("age", "age @age"));
        StringBuilder out = new StringBuilder();
        element.render(null, new Dataset("name", "Alice", "age", "35",
                "height", "65"), out);
        assertEquals("result HTML", "name Alice, age 35", out.toString());
    }
    public void test_render_errorInRequest() {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11", "template", "@1, @2",
                "request", "error"),
                new ElementFixture("name", "name @name"),
                new ElementFixture("age", "age @age"));
        ClientRequest cr = new ClientRequestFixture();
        cr.addDataRequest("error", RawDataManager.newError(new Dataset(
                "message", "sample <error>", "value", "47")));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("name", "Alice", "age", "35",
                "height", "65"), out);
        assertEquals("result HTML", "name Alice, age 35", out.toString());
        assertEquals("Javascript for HTML",
                "Fiz.addBulletinMessage(\"error: sample &lt;error&gt;\");\n",
                cr.getHtml().jsCode.toString());
    }
    public void test_render_useDataFromRequest() {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11", "template",
                "(for @name from @state) @1, @2",
                "request", "getPerson"),
                new ElementFixture("name", "name @name"),
                new ElementFixture("age", "age @age"));
        ClientRequest cr = new ClientRequestFixture();
        cr.addDataRequest("getPerson", RawDataManager.newRequest(person));
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("name", "Alice", "age", "35",
                "state", "California"), out);
        assertEquals("result HTML",
                "(for Alice from California) name Alice, age 35",
                out.toString());
    }
    public void test_render_noTemplate() {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11"),
                new ElementFixture("name", "name @name"),
                new ElementFixture("age", "age @age"),
                new ElementFixture("misc", ",third element output"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("name", "Alice", "age", "35",
                "state", "California"), out);
        assertEquals("result HTML", "name Aliceage 35,third element output",
                out.toString());
    }
    public void test_render_specialCharsWithTemplate() {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11", "template", "(@name) @1 @2)"),
                new ElementFixture("name", "<div class=\"xyzzy\">@name</div>"),
                new ElementFixture("age", "<div>@age</div>"));
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = new StringBuilder();
        element.render(cr, new Dataset("name", "<Alice>", "age", "35",
                "state", "California"), out);
        assertEquals("result HTML", "(&lt;Alice&gt;) <div class=\"xyzzy\">" +
                "&lt;Alice&gt;</div> <div>35</div>)",
                out.toString());
    }

    public void test_render_responsibleFor() {
        CompoundFormElement element = new CompoundFormElement(
                new Dataset("id", "id11"),
                new ElementFixture("name", "name @name"),
                new ElementFixture("age", "age @age"));
        assertEquals("responsible for age?", true,
                element.responsibleFor("age"));
        assertEquals("responsible for height?", false,
                element.responsibleFor("height"));
        assertEquals("responsible for parent id?", false,
                element.responsibleFor("id11"));
    }
}
