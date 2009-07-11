// AutocompleteFormElementTest.js --
//
// Jsunit tests for AutocompleteFormElement.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/AutocompleteFormElement.js");

AutocompleteFormElementTest = {};

Fiz.Ajax = function(properties) {
	this.url = properties.url;
	this.data = properties.data;
}

AutocompleteFormElementTest.test_showChoices = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var parent = document.addElementWithId("parent", {offsetLeft: 0, offsetTop: 0});
	var input = document.addElementWithId("auto_input", {offsetLeft: 10,
														 offsetTop: 100,
														 offsetWidth: 8,
														 offsetHeight: 5,
														 offsetParent: parent,
														 value: ""});
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");

	formElem.showChoices();
	
	assertEqual(false, formElem.isOpen, "Open?");

	input.setAttribute('value','Test');
	
	formElem.showChoices();

	assertEqual(true, formElem.isOpen, "Open?");
		
	assertEqual("8px", dropdown.style.top,
			"Top of dropdown");
	assertEqual("0px", dropdown.style.left,
			"Bottom of dropdown");
	assertEqual("block", dropdown.style.display,
			"Visibility of dropdown");
}

AutocompleteFormElementTest.test_hideChoices = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input");
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");

	formElem.hideChoices();
	
	assertEqual(false, formElem.isOpen, "Open?");
	assertEqual("none", dropdown.style.display,
			"Visibility of dropdown");
}

AutocompleteFormElementTest.test_redraw = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input");
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.dataset = [{name: "tes", value: "tes_val"}];
	formElem.redraw();

	assertEqual(null, formElem.curSelection, "No result: Current Selection");
	assertEqual(1, dropdown.getChildNodes().length, "No results: Children after add");
	assertEqual("No results found", dropdown.firstChild.textContent, 'No results');

	formElem.dataset.push({name: "Test1", value: "Test1_val"});
	formElem.redraw();

	assertEqual(null, formElem.curSelection, "One result: Current Selection");
	assertEqual(2, dropdown.getChildNodes().length, "One result: Children after add");
	assertEqual("tes", dropdown.firstChild.textContent, "Query string");
	assertEqual("Test1", dropdown.lastChild.textContent, "First result");	
}

AutocompleteFormElementTest.test_resultItem = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input");
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");

	var item = formElem.resultItem({name: "test1", value: "test1", isQuery: true});
	
	assertEqual("test1", item.result.name, "Query value: Name");
	assertEqual("test1", item.result.value, "Query value: Value");
	assertEqual("query hover", item.className, "Query value: Class");
	assertEqual("test1", item.textContent, "Query value: Text");

	item = formElem.resultItem({name: "test2"});
	
	assertEqual("test2", item.result.name, "No value: Name");
	assertEqual("test2", item.result.value, "No value: Value");
	assertEqual(undefined, item.className, "No value: Class");
	assertEqual("test2", item.textContent, "No value: Text");

	item = formElem.resultItem({name: "test3", value: "test3_val"});
	
	assertEqual("test3", item.result.name, "Has value: Name");
	assertEqual("test3_val", item.result.value, "Has value: Value");
	assertEqual(undefined, item.className, "Has value: Class");
	assertEqual("test3", item.textContent, "Has value: Text");
}

AutocompleteFormElementTest.test_selectElement = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input");
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.selectElement({name: "test", value: "test_val"});
	
	assertEqual(false, formElem.keepOpen, "Keep Open");
	assertEqual("test", input.value, "Input");
	assertEqual("test_val", hidden.value, "Hidden");
}

AutocompleteFormElementTest.test_setDataset = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input", {value: "test1"});
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.setDataset({});
	
	assertEqual(1, formElem.dataset.length, "Empty dataset: Length");
	assertEqual("test1", formElem.dataset[0].name, "Empty dataset: Element 1 Name");
	assertEqual("test1", formElem.dataset[0].value, "Empty dataset: Element 1 Value");

	formElem.setDataset({data: {name: "test2", value: "test2_val"},});
	
	assertEqual(2, formElem.dataset.length, "One dataset: Length");
	assertEqual("test1", formElem.dataset[0].name, "One dataset: Element 1 Name");
	assertEqual("test1", formElem.dataset[0].value, "One dataset: Element 1 Value");
	assertEqual("test2", formElem.dataset[1].name, "One dataset: Element 2 Name");
	assertEqual("test2_val", formElem.dataset[1].value, "One dataset: Element 2 Value");

	formElem.setDataset({data: [{name: "test3", value: "test3_val"}, {name: "test4", value: "test4_val"}]});
	
	assertEqual(3, formElem.dataset.length, "Two dataset: Length");
	assertEqual("test1", formElem.dataset[0].name, "Two dataset: Element 1 Name");
	assertEqual("test1", formElem.dataset[0].value, "Two dataset: Element 1 Value");
	assertEqual("test3", formElem.dataset[1].name, "Two dataset: Element 2 Name");
	assertEqual("test3_val", formElem.dataset[1].value, "Two dataset: Element 2 Value");
	assertEqual("test4", formElem.dataset[2].name, "Two dataset: Element 3 Name");
	assertEqual("test4_val", formElem.dataset[2].value, "Two dataset: Element 3 Value");
}

AutocompleteFormElementTest.test_highlightSelection = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input", {value: "test1"});
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");
	formElem.curSelection = null;
	
	var elem1 = document.addElementWithId("elem1", {
			className: "",
			result: {name: "test1", value: "test1_val"}
	});
	formElem.highlightSelection(elem1);

	assertEqual("test1", input.value, "Input value: elem1");
	assertEqual("test1_val", hidden.value, "Hidden value: elem1");
	assertEqual("hover", elem1.className, "Add hovering: elem1");
	assertEqual(elem1, formElem.curSelection, "Current Selection: elem1");	

	var elem2 = document.addElementWithId("elem2", {
			className: "",
			result: {name: "test2", value: "test2_val"}
	});
	
	formElem.highlightSelection(elem2);

	assertEqual("", elem1.className, "Clear hovering: elem1");
	assertEqual("test2", input.value, "Input value: elem2");
	assertEqual("test2_val", hidden.value, "Hidden value: elem2");
	assertEqual("hover", elem2.className, "Add hovering: elem2");
	assertEqual(elem2, formElem.curSelection, "Current Selection: elem2");
}

AutocompleteFormElementTest.test_prevResult = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input", {value: "test1"});
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");
	var previous = document.addElementWithId("cur_prev",
			{result: {name: "test1", value: "test1_val"},
			className: ''});
	var curSelection = document.addElementWithId("cur", {
			result: {name: "test2", value: "test2_val"},
			previousSibling: previous,
			className: ''});
			
	formElem.curSelection = curSelection;
	formElem.prevResult();
	
	assertEqual("", curSelection.className, "Clear hovering");
	assertEqual("test1", input.value, "Input value");
	assertEqual("test1_val", hidden.value, "Hidden value");
	assertEqual("hover", previous.className, "Add hovering");
	assertEqual(previous, formElem.curSelection, "Current Selection");
}

AutocompleteFormElementTest.test_nextResult = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input", {value: "test1"});
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");
	var next = document.addElementWithId("cur_next",
			{result: {name: "test1", value: "test1_val"},
			className: ''});
	var curSelection = document.addElementWithId("cur", {
			result: {name: "test2", value: "test2_val"},
			nextSibling: next,
			className: ''});

	formElem.curSelection = curSelection;
	formElem.nextResult();	

	assertEqual("", curSelection.className, "Clear hovering");
	assertEqual("test1", input.value, "Input value");
	assertEqual("test1_val", hidden.value, "Hidden value");
	assertEqual("hover", next.className, "Add hovering");
	assertEqual(next, formElem.curSelection, "Current Selection");
}

AutocompleteFormElementTest.test_fetchResult = function() {
	var dropdown = document.addElementWithId("auto_dropdown");
	var input = document.addElementWithId("auto_input", {value: "test1"});
	var hidden = document.addElementWithId("auto_hidden");
	
	var formElem = new Fiz.AutocompleteFormElement("auto");

	assertEqual(null, formElem.fetchResult({which: 9}), "Keycode: 9");
	assertEqual(null, formElem.fetchResult({which: 38}), "Keycode: 38");
	assertEqual(null, formElem.fetchResult({which: 91}), "Keycode: 91");
	assertEqual(null, formElem.fetchResult({which: 112}), "Keycode: 112");
	assertEqual(null, formElem.fetchResult({which: 145}), "Keycode: 145");

	var ajax = formElem.fetchResult({which: 48});
	assertEqual("/AutocompleteFormElement/ajaxQuery", ajax.url, "(48) Ajax URL");
	assertEqual("auto", ajax.data.id, "(48) Ajax ID");
	assertEqual("test1", ajax.data.query, "(48) Ajax Query");

	ajax = formElem.fetchResult({which: 76});
	assertEqual("test1", ajax.data.query, "(76) Ajax Query");

	input.setAttribute("value", "test2");
	ajax = formElem.fetchResult({which: 90});
	assertEqual("/AutocompleteFormElement/ajaxQuery", ajax.url, "(90) Ajax URL");
	assertEqual("auto", ajax.data.id, "(90) Ajax ID");
	assertEqual("test2", ajax.data.query, "(90) Ajax Query");

	ajax = formElem.fetchResult({which: 188});
	assertEqual("test2", ajax.data.query, "(188) Ajax Query");

	ajax = formElem.fetchResult({which: 197});
	assertEqual("test2", ajax.data.query, "(197) Ajax Query");

	ajax = formElem.fetchResult({which: 222});
	assertEqual("test2", ajax.data.query, "(222) Ajax Query");

	ajax = formElem.fetchResult({which: 96});
	assertEqual("test2", ajax.data.query, "(96) Ajax Query");

	ajax = formElem.fetchResult({which: 105});
	assertEqual("test2", ajax.data.query, "(105) Ajax Query");

	ajax = formElem.fetchResult({which: 111});
	assertEqual("test2", ajax.data.query, "(111) Ajax Query");

	ajax = formElem.fetchResult({which: 8});
	assertEqual("test2", ajax.data.query, "(8) Ajax Query");

	ajax = formElem.fetchResult({which: 32});
	assertEqual("test2", ajax.data.query, "(32) Ajax Query");

	ajax = formElem.fetchResult({which: 46});
	assertEqual("test2", ajax.data.query, "(46) Ajax Query");
}