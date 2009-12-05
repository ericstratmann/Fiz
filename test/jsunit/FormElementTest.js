/* FormElementTest.js --
 *
 * Jsunit tests for FormElement.js, organized in the standard fashion.
 *
 * Copyright (c) 2009 Stanford University
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
 * ACTION OF CONTRACT, NEGLIGENCE OR otherFields TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

include("static/fiz/Fiz.js");
include("static/fiz/FormElement.js");

// Following is a replacement for the Ajax constructor; it just logs
// information about some of its arguments.

Fiz.Ajax = function(properties) {
    jsunit.log += "Fiz.Ajax url: " + properties.url + ", data:\n" +
            printDataset(properties.data, "    ");
};

FormElementTest = {};

FormElementTest.test_attachValidator = function() {
    var trigger = document.addElementWithId("trigger");
    Fiz.FormElement.attachValidator("trigger", "target1", 
            "include1,include2");
    
    assertEqual("target1", 
            Fiz.FormElement.validators["trigger"][0].targetId,
            "1: target element");
    assertEqual("include1,include2",
            Fiz.FormElement.validators["trigger"][0].otherFields.join(","),
            "1: include elements");
    
    Fiz.FormElement.attachValidator("trigger", "target2", 
    "include3,include4");

    assertEqual("target2", 
            Fiz.FormElement.validators["trigger"][1].targetId,
            "2: target element");
    assertEqual("include3,include4",
            Fiz.FormElement.validators["trigger"][1].otherFields.join(","),
            "2: include elements");
}

FormElementTest.test_getValue_checkboxRadio = function() {
    var checkbox = document.addElementWithId("checkbox",
            { type: "checkbox", checked: true, value: "cb_value" });
    var radio = document.addElementWithId("radio",
            { type: "radio", value: "radio_value" });

    assertEqual("cb_value", Fiz.FormElement.getValue(checkbox), "checkbox");
    assertEqual(null, Fiz.FormElement.getValue(radio), "radio");
}

FormElementTest.test_getValue_openValueFormElements = function() {
    var hidden = document.addElementWithId("hidden",
            { type: "hidden", value: "hidden_value" });
    var textarea = document.addElementWithId("textarea",
            { type: "textarea", value: "ta_value" });

    assertEqual("hidden_value", Fiz.FormElement.getValue(hidden), "hidden");
    assertEqual("ta_value", Fiz.FormElement.getValue(textarea), "textarea");
}

FormElementTest.test_getValue_selectMultiple = function() {
    var selectMultiple = document.addElementWithId("select-multiple", {
                type: "select-multiple", options: [
                    { selected: true, value: "one" },
                    { value: "two" },
                    { selected: true, value: "three" }
                ]
             });

    assertEqual("one", Fiz.FormElement.getValue(selectMultiple)[0].value,
            "first selected");
    assertEqual("three", Fiz.FormElement.getValue(selectMultiple)[1].value,
            "second selected");
}

FormElementTest.test_validate = function() {    
    Fiz.FormElement.validators = [];
    
    var trigger = document.addElementWithId("trigger",
            { type: "text", name: "trigger",  value: "trigger_val" });
    var target1 = document.addElementWithId("target1",
            { type: "password", name: "target1", value: "target1_val" });
    var target2 = document.addElementWithId("target2",
            { type: "select-one", name: "target2", value: "target2_val" });

    Fiz.FormElement.attachValidator("trigger", "target1", 
            "trigger,target1");

    jsunit.log = "";
    Fiz.FormElement.validate({target: {id: "trigger"}});
    assertEqual("Fiz.Ajax url: /FormElement/ajaxValidate, data:\n" +
            "    elementsToValidate: target1\n" +
            "    formData:\n" +
            "        target1: target1_val\n" +
            "        trigger: trigger_val\n",
            jsunit.log, "one binding");

    Fiz.FormElement.attachValidator("trigger", "target2", 
            "trigger,target1,target2");

    jsunit.log = "";
    Fiz.FormElement.validate({target: {id: "trigger"}});
    assertEqual("Fiz.Ajax url: /FormElement/ajaxValidate, data:\n" +
            "    elementsToValidate: target1,target2\n" +
            "    formData:\n" +
            "        target1: target1_val\n" +
            "        target2: target2_val\n" +
            "        trigger: trigger_val\n",
            jsunit.log, "two bindings");
}