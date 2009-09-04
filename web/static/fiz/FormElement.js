/* FormElement.js --
 *
 * This file provides Javascript functions needed to implement the
 * FormElement class. Functions are invoked to setup validators on form
 * elements, send AJAX requests to perform validation, and retrieve the actual
 * values from form elements.
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

// The following lines are used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js
// Fiz:include static/fiz/Ajax.js

// Create a container to encapsulate all the various functions and data that
// apply to the form elements in Fiz.
Fiz.FormElement = {};

// Form elements may have validators attached them. We create a container to
// hold information about the validators for each form element, indexed by its
// id.
Fiz.FormElement.validators = {};

/**
 * This function creates and stores an entry containing information that allows
 * the element identified by {@code triggerId} to invoke the {@code validate}
 * call on the elements identified by {@code targetId}.
 * @param triggerId           (String) The element triggering the validation
 * @param targetId            (String) The element to validate
 * @param otherFields         (String) Other elements whose values are needed
 *                            during validation (comma separated)
 */
Fiz.FormElement.attachValidator = function(triggerId, targetId, otherFields)
{
    if (undefined == Fiz.FormElement.validators[triggerId]) {
        Fiz.FormElement.validators[triggerId] = [];
        Fiz.addEvent(document.getElementById(triggerId),
                "blur", Fiz.FormElement.validate);
    }
    
    var binding = {targetId: targetId, otherFields: otherFields.split(',')};
    Fiz.FormElement.validators[triggerId].push(binding);
}

/**
 * Ths function retrieves the value of a form element.
 * @param element             (Element) Form element containing the target
 *                            value
 * @return                    The value of the form element (may be an array
 *                            for form elements that allow selection of multiple
 *                            items)
 */
Fiz.FormElement.getValue = function(element) {
    var type = element.type;
    if (type == "checkbox" || type == "radio") {
        if (element.checked) {
            return element.value;
        }
    } else if (type == "hidden" || type == "password" || type == "select-one"
            || type == "text" || type == "textarea") {
        return element.value;
    } else if (type == "select-multiple") {
        var value = [];
        for (var i in element.options) {
            var option = element.options[i];
            if (option.selected) {
                value.push({value: option.value});
            }
        }
        return value;
    }
    return null;
}

/**
 * This function is invoked when a FormElement loses focus and launches an
 * Ajax request to validate the FormElement. This function can also be manually
 * invoked to validate a form element by calling it with the id of that form
 * element.
 * @param trigger                   (Event or String) Event object from the DOM
 *                                  element that triggered this callback or
 *                                  the id of the element that is triggering
 *                                  a validation request
 */
Fiz.FormElement.validate = function(trigger)
{
    if (typeof trigger != 'string') {
        trigger = trigger.target.id;
    }
    
    // Gather data from each of the form elements that contribute to the
    // validation of the form element that launched this request
    var bindings = Fiz.FormElement.validators[trigger];
    if (undefined == bindings) {
        return;
    }

    // ids of form elements that will be validated during this Ajax call
    var elementsToValidate = [];
    
    // Object containing the value for each form element that is used in the
    // validation
    var formData = {};
    
    for (var i in bindings) {
        var target = document.getElementById(bindings[i].targetId);
        var otherFields = bindings[i].otherFields;

        elementsToValidate.push(bindings[i].targetId);
        
        // Collect values from all form elements that that will be validated
        // and other values needed to perform the validation
        formData[target.name] = Fiz.FormElement.getValue(target);
        for (var j in otherFields) {
            formData[otherFields[j]] = Fiz.FormElement.getValue(
                    document.getElementById(otherFields[j]));
        }
    }

    // Send off the Ajax request to perform the validation
    new Fiz.Ajax({
        url: '/FormElement/ajaxValidate',
        data: {
            elementsToValidate: elementsToValidate.join(","),
            formData: formData
        }
    });
}