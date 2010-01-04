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

package org.fiz.section;

import org.fiz.*;
import org.fiz.test.*;
import java.util.ArrayList;

import org.fiz.test.ClientRequestFixture;

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
        public void render(ClientRequest cr, Dataset data) {
            // Do nothing.
        }
    }

    protected ClientRequest cr;
    protected Dataset main;
    public void setUp() {
        cr = new ClientRequestFixture();
        main = cr.getMainDataset();
        main.clear();
    }

    // This is a sample validation function used to test custom validators
    public static String validateTest(String id, Dataset properties,
            Dataset formData) {
        if (formData.get(id).equals(properties.get("test"))) {
            return null;
        } else {
            return FormValidator.errorMessage("Validation failed",
                    properties, formData);
        }
    }

    // No tests for addDataRequests: it doesn't do anything.

    public void test_constructor_basic() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "4815162342", "name", "Alice"));
        assertEquals("properties dataset", "id:   4815162342\n" +
                "name: Alice\n", element.properties.toString());
        assertEquals("element id", "4815162342", element.id);
    }

    public void test_constructor_required() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "4815162342", "name", "Alice", "required", "true"));
        assertEquals("validator set", "type: required\n",
                element.validatorData.validators.get(0).toString());
    }

    // No tests for addDataRequests: it doesn't do anything.

    public void test_addValidator() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "elem", "label", "elem_label",
                "validator", new Dataset("type", "required")));

        element.addValidator(new Dataset("type", "match"));

        assertEquals("validators", 2, element.validatorData.validators.size());

        assertEquals("validator required",
                "type: required\n",
                element.validatorData.validators.get(0).toString());
        assertEquals("validator match",
                "type: match\n",
                element.validatorData.validators.get(1).toString());
    }

    public void test_ajaxValidate_success() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        ArrayList<Dataset> validators = new ArrayList<Dataset>();
        validators.add(new Dataset("type", "range", "min", "98", "max", "100"));
        FormElement.ValidatorData v = new FormElement.ValidatorData("elem");
        v.parentId = "form";
        v.elementErrorStyle = "FormSection.elementError";
        cr.setPageProperty("elem_validation",  v);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "99"));
        FormElement.ajaxValidate(cr);
        TestUtil.assertSubstring("Ajax javascript",
                "Fiz.ids.form.clearElementError(\"form_elem\");\n",
                cr.getJs());
    }

    public void test_ajaxValidate_failure() {
        cr.setClientRequestType(ClientRequest.Type.AJAX);
        ArrayList<Dataset> validators = new ArrayList<Dataset>();
        validators.add(new Dataset("type", "range", "min", "98", "max", "100",
                "errorMessage", "test: @type @min @max"));
        FormElement.ValidatorData v = new FormElement.ValidatorData("elem");
        v.parentId = "form";
        v.elementErrorStyle = "FormSection.elementError";
        v.validators = validators;
        cr.setPageProperty("elem_validation",  v);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "101"));
        FormElement.ajaxValidate(cr);
        TestUtil.assertSubstring("Ajax javascript",
                "Fiz.ids.form.elementError(\"form_elem\", \"" +
                "<span class=\\\"error\\\">" +
                "test: range 98 100" +
                "</span>\");\n",
                cr.getJs());
    }

    public void test_checkProperty() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "age", "8"));
        assertEquals("existing property", "8", element.checkProperty("age"));
        assertEquals("nonexistent property", null,
                element.checkProperty("bogus"));
    }

    public void test_collect() throws FormSection.FormDataException {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "name", "Alice"));
        Dataset out = new Dataset("name", "Bob", "age", "30");
        element.collect(null, new Dataset("name", "Carol", "age", "36"),
                out);
        assertEquals("output dataset", "age:  30\n" +
                "name: Carol\n", out.toString());
    }
    public void test_collect_valueMissing()
            throws FormSection.FormDataException {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "name", "Alice"));
        Dataset out = new Dataset("name", "Bob", "age", "30");
        element.collect(null, new Dataset(), out);
        assertEquals("output dataset", "age:  30\n" +
                "name: Bob\n", out.toString());
    }

    public void test_getId() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "age", "8"));
        assertEquals("return value", "name", element.getId());
    }

    public void test_renderLabel_withTemplate() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "age", "label", "Age of @name:"));
        ClientRequest cr = new ClientRequestFixture();
        element.renderLabel(cr, new Dataset("name", "<Bob>", "age", "30"));
        assertEquals("generated HTML", "Age of &lt;Bob&gt;:",
                     cr.getHtml().getBody().toString());
    }

    public void test_renderLabel_noTemplate() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "<age>"));
        ClientRequest cr = new ClientRequestFixture();
        element.renderLabel(cr, new Dataset("name", "<Bob>", "age", "30"));
        assertEquals("generated HTML", "", cr.getHtml().getBody().toString());
    }

    public void test_renderValidators_noParent() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "label", "noParent"));
        element.renderValidators(cr);

        assertEquals("Generated javascript", "", cr.getHtml().getJs());
        assertEquals("Javascript file names", "", cr.getHtml().getJsFiles());
    }

    public void test_renderValidators_multipleValidators() {
        ClientRequest cr = new ClientRequestFixture();
        cr.authTokenSet = true;
        cr.pageId = "1234";
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "elem", "label", "elem_label",
                "validator", new Dataset("type", "range", "min", "98", "max", "100"),
                "validator", new Dataset("type", "match", "fields", "test1,test2",
                        "otherFields", "test1,test2")));
        Config.init("test/testData/WEB-INF/app/config", "web/WEB-INF/fiz/config");
        new FormSection(
                new Dataset("id", "form1",
                        "request", "getFormData",
                        "postUrl", "postForm"),
                element);

        element.renderValidators(cr);

        assertEquals("Generated javascript",
                "Fiz.FormElement.attachValidator(" +
                "\"test1\", \"elem\", \"test1,test2\");\n" +
                "Fiz.FormElement.attachValidator(" +
                "\"test2\", \"elem\", \"test1,test2\");\n" +
                "Fiz.FormElement.attachValidator(" +
                "\"elem\", \"elem\", \"elem,test1,test2\");\n",
                cr.getHtml().getJs());
        assertEquals("Page property", element.validatorData,
                cr.getPageProperty("elem_validation"));
        assertEquals("Javascript file names",
                "static/fiz/Ajax.js, static/fiz/Fiz.js, static/fiz/FormElement.js",
                cr.getHtml().getJsFiles());
    }

    public void test_responsibleFor() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "age"));
        assertEquals("responsible for this element", true,
                element.responsibleFor("age"));
        assertEquals("not responsible for this element", false,
                element.responsibleFor("name"));
    }

    public void test_setParentForm() {
        FormElementFixture element = new FormElementFixture(new Dataset(
                "id", "name", "name", "Alice"));
        Config.init("test/testData/WEB-INF/app/config", "web/WEB-INF/fiz/config");
        FormSection section = new FormSection(
                new Dataset("id", "form1",
                        "request", "getFormData",
                        "postUrl", "postForm"),
                element);

        assertEquals("form parent", section, element.parentForm);
    }

    public void test_validate_basic() throws FormSection.FormDataException {
        FormElement element = new FormElementFixture(
                new Dataset("id", "elem",
                        "label", "elem_label",
                        "validator", new Dataset(
                                "type", "range", "min", "98", "max", "100")));
        cr.setPageProperty("elem_validation", element.validatorData);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "98"));

        boolean gotException = false;
        try {
            element.validate(cr.getMainDataset().getDataset("formData"));
        } catch (FormSection.FormDataException e) {

        }
        assertEquals("exception did not happen", false, gotException);
    }

    public void test_validate_success_builtIn() throws FormSection.FormDataException {
        ArrayList<Dataset> validators = new ArrayList<Dataset>();
        validators.add(new Dataset("type", "range", "min", "98", "max", "100"));
        FormElement.ValidatorData v = new FormElement.ValidatorData("elem");
        v.parentId = "form";
        v.elementErrorStyle = "FormSection.elementError";
        v.validators = validators;
        cr.setPageProperty("elem_validation",  v);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "98"));

        boolean gotException = false;
        try {
            FormElement.validate(v, cr.getMainDataset().getDataset("formData"));
        } catch (FormSection.FormDataException e) {

        }
        assertEquals("exception did not happen", false, gotException);
    }

    public void test_validate_success_custom() throws FormSection.FormDataException {
        ArrayList<Dataset> validators = new ArrayList<Dataset>();
        validators.add(new Dataset("type", "section.FormElementTest.validateTest",
                        "test", "98"));
        FormElement.ValidatorData v = new FormElement.ValidatorData("elem");
        v.parentId = "form";
        v.elementErrorStyle = "FormSection.elementError";
        v.validators = validators;
        cr.setPageProperty("elem_validation",  v);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "98"));

        boolean gotException = false;
        try {
            FormElement.validate(v, cr.getMainDataset().getDataset("formData"));
        } catch (FormSection.FormDataException e) {}
        assertEquals("exception did not happen", false, gotException);
    }

    public void test_validate_success_multiple() throws FormSection.FormDataException {
        ArrayList<Dataset> validators = new ArrayList<Dataset>();
        validators.add(new Dataset("type", "range", "min", "98", "max", "100"));
        validators.add(new Dataset("type", "section.FormElementTest.validateTest",
                        "test", "98"));
        FormElement.ValidatorData v = new FormElement.ValidatorData("elem");
        v.parentId = "form";
        v.elementErrorStyle = "FormSection.elementError";
        v.validators = validators;
        cr.setPageProperty("elem_validation",  v);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "98"));

        boolean gotException = false;
        try {
            FormElement.validate(v, cr.getMainDataset().getDataset("formData"));
        } catch (FormSection.FormDataException e) {}
        assertEquals("exception did not happen", false, gotException);
    }

    public void test_validate_failure_builtIn() {
        ArrayList<Dataset> validators = new ArrayList<Dataset>();
        validators.add(new Dataset("type", "range", "min", "98", "max", "100",
                "errorMessage", "test: @type @min @max"));
        validators.add(new Dataset("type", "length", "min", "10", "max", "12",
                        "errorMessage", "test: @type @min @max"));
        FormElement.ValidatorData v = new FormElement.ValidatorData("elem");
        v.parentId = "form";
        v.elementErrorStyle = "FormSection.elementError";
        v.validators = validators;
        cr.setPageProperty("elem_validation",  v);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "97"));

        boolean gotException = false;
        try {
            FormElement.validate(v, cr.getMainDataset()
                    .getDataset("formData"));
        } catch (FormSection.FormDataException e) {
            assertEquals("exception message - range",
                    "culprit: elem\n" +
                    "message: \"test: range 98 100\"\n", e.getMessages()[0].toString());
            assertEquals("exception message - length",
                    "culprit: elem\n" +
                    "message: \"test: length 10 12\"\n", e.getMessages()[1].toString());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }

    public void test_validate_failure_custom() {
        ArrayList<Dataset> validators = new ArrayList<Dataset>();
        validators.add(new Dataset("type", "section.FormElementTest.validateTest",
                "test", "test_value", "errorMessage", "test_error"));
        FormElement.ValidatorData v = new FormElement.ValidatorData("elem");
        v.parentId = "form";
        v.elementErrorStyle = "FormSection.elementError";
        v.validators = validators;
        cr.setPageProperty("elem_validation",  v);
        main.set("elementsToValidate", "elem");
        main.set("formData", new Dataset("elem", "fake_value"));

        boolean gotException = false;
        try {
            FormElement.validate(v, cr.getMainDataset()
                    .getDataset("formData"));
        }
        catch (FormSection.FormDataException e) {
            assertEquals("exception message",
                    "culprit: elem\n" +
                    "message: test_error\n", e.getMessages()[0].toString());
            gotException = true;
        }
        assertEquals("exception happened", true, gotException);
    }
}
