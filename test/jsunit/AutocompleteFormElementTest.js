// AutocompleteFormElementTest.js --
//
// Jsunit tests for AutocompleteFormElement.js, organized in the standard fashion.
//
// Copyright (c) 2009 Stanford University
//
// Permission to use, copy, modify, and distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

include("static/fiz/Fiz.js");
include("static/fiz/AutocompleteFormElement.js");

AutocompleteFormElementTest = {};

Fiz.Ajax = function(properties) {
    this.url = properties.url;
    this.data = properties.data;
}

AutocompleteFormElementTest.setUp = function() {
	this.dropdown = document.addElementWithId("auto_dropdown");
    this.input = document.addElementWithId("auto");
}

AutocompleteFormElementTest.test_constructor = function() {
	this.input.value = "test";
	var formElem = new Fiz.AutocompleteFormElement("auto");
	
	assertEqual("auto", formElem.id, "id");
	assertEqual(false, formElem.isOpen, "isOpen");
	assertEqual(false, formElem.keepOpen, "keepOpen");
	assertEqual(null, formElem.selected, "selected");
	assertEqual(null, formElem.highlighted, "highlighted");
	assertEqual("auto", formElem.input.id, "input");
	assertEqual("test", formElem.lastUserInput, "lastUserInput");
	assertEqual("test", formElem.lastInputValue, "lastInputValue");
	assertEqual("off", formElem.input.autocomplete, "autocomplete");
	assertEqual("auto_dropdown", formElem.dropdown.id, "dropdown");
}

AutocompleteFormElementTest.test_captureKeydown_openUp = function() {
	var formElem = new Fiz.AutocompleteFormElement("auto");
	var event = {which: 38, preventDefault: logFunction("preventDefault")};

    formElem.prevResult = logFunction("prevResult");
    formElem.isOpen = true;
    var result = formElem.captureKeydown(event);

    assertEqual("prevResult()\npreventDefault()\n", jsunit.log,
    		"Function calls (UP)");
    assertEqual(false, result, "Return Value (UP)");
}

AutocompleteFormElementTest.test_captureKeydown_openDown = function() {
	var formElem = new Fiz.AutocompleteFormElement("auto");
    var event = {which: 40, preventDefault: logFunction("preventDefault")};

    formElem.nextResult = logFunction("nextResult");
    formElem.isOpen = true;
    var result = formElem.captureKeydown(event);

    assertEqual("nextResult()\npreventDefault()\n", jsunit.log,
    	"Function calls (DOWN)");
    assertEqual(false, result, "Return Value (DOWN)");
}

AutocompleteFormElementTest.test_captureKeydown_openEnter = function() {
	var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.input.focus = logFunction("focus");

    var event = {which: 13, preventDefault: logFunction("preventDefault")};

    formElem.isOpen = true;
    var result = formElem.captureKeydown(event);

    assertEqual(false, formElem.isOpen, "Open? (ENTER)");
    assertEqual("focus()\npreventDefault()\n", jsunit.log,
    		"Prevent Default (ENTER)");
    assertEqual(false, result, "Return Value (ENTER)");
}

AutocompleteFormElementTest.test_captureKeydown_openEsc = function() {
	var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.input.focus = logFunction("focus");

    var event = {which: 27, preventDefault: logFunction("preventDefault")};

    formElem.isOpen = true;
    var result = formElem.captureKeydown(event);

    assertEqual(false, formElem.isOpen, "Open? (ESC)");
    assertEqual("focus()\npreventDefault()\n", jsunit.log,
    		"Prevent Default (ESC)");
    assertEqual(false, result, "Return Value (ESC)");
}

AutocompleteFormElementTest.test_captureKeydown_closedUp = function() {
	var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.input.focus = logFunction("focus");

    var event = {which: 38, preventDefault: logFunction("preventDefault")}

    formElem.isOpen = false;
    var result = formElem.captureKeydown(event);

    assertEqual(true, formElem.isOpen, "Open? (UP)");
    assertEqual(false, result, "Return Value (UP)");
}

AutocompleteFormElementTest.test_captureKeydown_closedDown = function() {
	var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.input.focus = logFunction("focus");

    var event = {which: 40, preventDefault: logFunction("preventDefault")};

    formElem.isOpen = false;
    var result = formElem.captureKeydown(event);

    assertEqual(true, formElem.isOpen, "Open? (DOWN)");
    assertEqual(false, result, "Return Value (DOWN)");
}

AutocompleteFormElementTest.test_showDropdown_emptyInput = function() {
    this.input.value = "";
    
    var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.input.focus = logFunction("focus");
    formElem.showDropdown();
    
    assertEqual(false, formElem.isOpen, "Open?");
}

AutocompleteFormElementTest.test_showDropdown_validInput = function() {
    var parent = document.addElementWithId("parent",
    		{offsetLeft: 0, offsetTop: 0});
  
    this.input.setAttributes({offsetLeft: 10,
		    offsetTop: 100,
		    offsetWidth: 8,
		    offsetHeight: 5,
		    offsetParent: parent,
		    value: "test"}
    );
    
    var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.showDropdown();
    
    assertEqual(true, formElem.isOpen, "Open?");
    assertEqual("4px", formElem.dropdown.style.top,
    		"Top of dropdown");
    assertEqual("0px", formElem.dropdown.style.left,
    		"Left of dropdown");
    assertEqual("block", formElem.dropdown.style.display,
    		"Visibility of dropdown");
}

AutocompleteFormElementTest.test_showDropdown_emptyInput = function() {
    var parent = document.addElementWithId("parent",
    		{offsetLeft: 0, offsetTop: 0});
  
    this.input.setAttributes({offsetLeft: 10,
		    offsetTop: 100,
		    offsetWidth: 8,
		    offsetHeight: 5,
		    offsetParent: parent,
		    value: ""}
    );
    
    var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.input.focus = logFunction("focus");

    formElem.showDropdown();
    
    assertEqual(false, formElem.isOpen, "Open?");
}

AutocompleteFormElementTest.test_hideDropdown_force = function() {
    this.input.focus = logFunction("focus");

    var formElem = new Fiz.AutocompleteFormElement("auto");

    formElem.keepOpen = true;
    formElem.isOpen = true;
    formElem.hideDropdown(true);
    
    assertEqual(false, formElem.keepOpen, "Keep Open?");
    assertEqual(false, formElem.isOpen, "Open?");
    assertEqual("none", formElem.dropdown.style.display,
            "Visibility of dropdown");
    assertEqual("focus()\n", jsunit.log, "Focus Call");
}

AutocompleteFormElementTest.test_hideDropdown_noForce = function() {
    this.input.focus = logFunction("focus");
 
    var formElem = new Fiz.AutocompleteFormElement("auto");

    formElem.keepOpen = true;
    formElem.isOpen = true;
    formElem.dropdown.style.display = "block";
    formElem.hideDropdown();
    
    assertEqual(true, formElem.keepOpen, "Keep Open?");
    assertEqual(true, formElem.isOpen, "Open?");
    assertEqual("block", formElem.dropdown.style.display,
            "Visibility of dropdown");
}

AutocompleteFormElementTest.test_selectChoice_notDone = function() {
	this.input.value = "test";
    this.input.focus = logFunction("focus");

	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.isOpen = true;
	
	formElem.lastUserInput = "test1";
	formElem.highlightChoice = logFunction("highlightChoice");
	formElem.selectChoice(null);
    
    assertEqual("test1", formElem.input.value, "Input value");
    assertEqual("test1", formElem.lastInputValue, "Last input value");
    assertEqual(null, formElem.selected, "Selected");
    assertEqual("highlightChoice(null)\n", jsunit.log, "Highlight Call");
    assertEqual(true, formElem.isOpen, "Open?");
}

AutocompleteFormElementTest.test_selectChoice_done = function() {
	this.input.value = "test";
    this.input.focus = logFunction("focus");

	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.isOpen = true;
	
    var choice = document.addElementWithId("choice1", {textContent: "test1"});
	formElem.lastUserInput = "test1";
	formElem.highlightChoice = logFunction("highlightChoice");
	formElem.selectChoice(choice, true);
    
    assertEqual("test1", formElem.input.value, "Input value");
    assertEqual("test1", formElem.lastInputValue, "Last input value");
    assertEqual(choice, formElem.selected, "Selected");
    assertEqual(false, formElem.isOpen, "Open?");
}

AutocompleteFormElementTest.test_highlightChoice = function() {
	this.input.value = "test1";
	
    var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.highlighted = null;
    
    var elem1 = document.addElementWithId("elem1", {
            className: "",
            textContent: "test1"
    });
    formElem.highlightChoice(elem1);

    assertEqual("highlight", elem1.className, "Add highlighting: elem1");
    assertEqual(elem1, formElem.highlighted, "Current Highlighted: elem1");

    var elem2 = document.addElementWithId("elem2", {
            className: "",
            textContent: "test2"
    });
    
    formElem.highlightChoice(elem2);

    assertEqual("", elem1.className, "Clear highlighting: elem1");
    assertEqual("highlight", elem2.className, "Add highlighting: elem2");
    assertEqual(elem2, formElem.highlighted, "Current Highlighted: elem2");
}

AutocompleteFormElementTest.test_prevResult = function() {
    this.input.value= "test_default";
    
    var choices = document.addElementWithId("auto_choices");
    choices.appendChild(document.addElementWithId("choice1",
            {textContent: "test1"}));
    choices.appendChild(document.addElementWithId("choice2",
            {textContent: "test2"}));
    
    var formElem = new Fiz.AutocompleteFormElement("auto");
            
    formElem.prevResult();
    assertEqual("test2", formElem.input.value, "1: Input value");
    
    formElem.prevResult();
    assertEqual("test1", formElem.input.value, "2: Input value");

    formElem.prevResult();
    assertEqual("test_default", formElem.input.value, "3: Input value");
}

AutocompleteFormElementTest.test_nextResult = function() {
    this.input.value= "test_default";

    var choices = document.addElementWithId("auto_choices");
    choices.appendChild(document.addElementWithId("choice1",
            {textContent: "test1"}));
    choices.appendChild(document.addElementWithId("choice2",
            {textContent: "test2"}));
    
    var formElem = new Fiz.AutocompleteFormElement("auto");
            
    formElem.nextResult();
    assertEqual("test1", formElem.input.value, "1: Input value");
    
    formElem.nextResult();
    assertEqual("test2", formElem.input.value, "2: Input value");

    formElem.nextResult();
    assertEqual("test_default", formElem.input.value, "3: Input value");
}

AutocompleteFormElementTest.test_refreshMenu_emptyInput = function() {
	this.input.value = "";
	this.input.focus = logFunction("focus");

	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.isOpen = true;
	
	var result = formElem.refreshMenu();
	
    assertEqual(false, formElem.isOpen, "Open?");
    assertEqual(null, result, "Return value");
}

AutocompleteFormElementTest.test_refreshMenu_validNewInput = function() {
	this.input.value = "test";
    
    var formElem = new Fiz.AutocompleteFormElement("auto");
    formElem.lastInputValue = "test1";

	var result = formElem.refreshMenu();

    assertEqual(null, formElem.selected, "Selected");
    assertEqual(null, formElem.highlighted, "Highlighted");
    assertEqual("test", formElem.lastInputValue, "Last input value");
    assertEqual("test", formElem.lastUserInput, "Last user input");
    assertEqual("/AutocompleteFormElement/ajaxQuery", result.url, "Ajax URL");
    assertEqual("auto", result.data.id, "Ajax data ID");
    assertEqual("test", result.data.userInput, "Ajax data user input");
}

AutocompleteFormElementTest.test_refreshMenu_oldInput = function() {
	this.input.value = "test";

	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.lastInputValue = "test";
	
	var result = formElem.refreshMenu();
	
    assertEqual(null, result, "Return value");
}
