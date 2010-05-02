/* IPhonePhoneToolbarSectionTest.js --
 *
 * Jsunit tests for IPhonePhoneToolbarSection.js, organized in the standard
 * fashion.
 *
 * Copyright (c) 2010 Stanford University
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
include("static/fiz/IPhonePhoneToolbarSection.js");

IPhonePhoneToolbarSectionTest = {};

IPhonePhoneToolbarSectionTest.test_constructor_noHandlersCalled = function(){
    var testElement = document.addElementWithId("iSection");
    var childElement = document.addElementWithId("myLabel-toolbarSection-myImage");
    childElement.setAttribute("class", "unparsedClassName");
    childElement.listeners = new Array();
    testElement.appendChild(childElement);
    var iSection = new Fiz.IPhonePhoneToolbarSection("iSection");
    assertEqual(undefined, childElement.listeners["touchstart"],
            "Touch Start Handler");
}

IPhonePhoneToolbarSectionTest.test_addHandlerFirst = function(){
    var testElement = document.addElementWithId("iSection");
    var childElement = document.addElementWithId("myLabel-toolbarButton-myImage");
    childElement.setAttribute("class", "toolbarButton");
    childElement.listeners = new Array();
    testElement.appendChild(childElement);
    var iSection = new Fiz.IPhonePhoneToolbarSection("iSection");
    assertEqual("\nfunction () {\n" +
        "    Fiz.changeImage(image + label, image + \"-active.png\");\n" +
        "    Fiz.addClass(document.getElementById(\"td\" + image + label), \"active\");\n" +
        "}\n", childElement.listeners["touchstart"][0].toString(), "Touch Start Handler");
}

IPhonePhoneToolbarSectionTest.test_addHandlerSecond = function(){
    var testElement = document.addElementWithId("iSection");
    var childElement = document.addElementWithId("myLabel-toolbarButton-myImage");
    childElement.setAttribute("class", "toolbarButton");
    childElement.listeners = new Array();
    testElement.appendChild(childElement);
    var iSection = new Fiz.IPhonePhoneToolbarSection("iSection");
    assertEqual("\nfunction () {\n" +
        "    Fiz.changeImage(image + label, image + \".png\");\n" +
        "    Fiz.removeClass(document.getElementById(\"td\" + image + label), \"active\");\n" +
        "}\n", childElement.listeners["click"][0].toString(), "Click Handler");
}