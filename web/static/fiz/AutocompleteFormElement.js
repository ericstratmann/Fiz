/** DateFormElement.js --
 *
 * This file provides Javascript functions needed to implement the
 * DateFormElement class.  One Fiz.DateFormElement is created
 * for each DateFormElement Java object.  Methods on the Javascript
 * are invoked for functions such as redrawing and navigating the calendar,
 * showing and hiding the calendar, and updating the form element.
 */

// The following lines are used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js

// Constants

/**
 * Create a DateFormElement object.
 * @param id                       Id for the {@code <div>} element
 *                                 that represents the calendar.
 */
Fiz.AutocompleteFormElement = function(id)
{
	var self = this;
	
	this.id = id;
	this.numResults = 5;

	this.keepOpen = false;

	this.field = document.getElementById(id + '_field');
	this.field.setAttribute('autocomplete', 'off');
	this.field.onkeydown = function(e) {
		console.log(getKeyCode(e));
		switch(getKeyCode(e)) {
			case 38: self.prevResult(); break; // Up arrow
			case 40: self.nextResult(); break; // Down arrow
			case 13: self.curSelection.click(); e.preventDefault(); break; // Enter key
		}
	};

	this.dropdown = document.getElementById(id + '_dropdown');
	this.dropdown.onmouseover = function() { self.keepOpen = true; };
	this.dropdown.onmouseout = function() { self.keepOpen = false; };

	this.hidden = document.getElementById(id + '_hidden');
	
	this.previousQuery = '';
	this.previousResult;
	
	this.dataset = [];
}

Fiz.AutocompleteFormElement.prototype.showChoices = function()
{
	if(this.field.value == '') {
		this.hideChoices();
		return;
	}
		
	// Find the location of the input field
	var x, y;

	x = 0;
	y = 3 + this.field.offsetHeight;
		
	// Attach calendar picker to the input field
	this.dropdown.style.top = y + 'px';
	this.dropdown.style.left = x + 'px';
	this.dropdown.style.display = 'block';
}

Fiz.AutocompleteFormElement.prototype.hideChoices = function()
{
	if(!this.keepOpen)
		this.dropdown.style.display = 'none';
}

Fiz.AutocompleteFormElement.prototype.redraw = function()
{
	var self = this;
	
	this.curQuery = this.field.value;
	this.curSelection = null;
	
	// We clear the autocomplete list
	while(this.dropdown.hasChildNodes()) {
		this.dropdown.removeChild(this.dropdown.firstChild);
	}

	if(undefined == this.dataset) {
		var item = document.createElement('li');
		item.textContent = 'No results found';
		this.dropdown.appendChild(item);
		this.highlightSelection(null);
	} else {
		for(var matchIndex in this.dataset) {
			var item = document.createElement('li');
			var match = this.dataset[matchIndex];
			this.resultItem(item, match);
			this.dropdown.appendChild(item);
		}
	}
}

Fiz.AutocompleteFormElement.prototype.resultItem = function(item, match)
{
	var self = this;
	item.match = match;
	item.textContent = match.name;
	item.onclick = (function(match) {
		return function() {
			selectElement(match);
		};
	})(match);
	item.onmouseover = function() {
		self.highlightSelection(this);
	}
}

Fiz.AutocompleteFormElement.prototype.selectElement = function(match)
{
	this.keepOpen = false;
	this.field.value = match.name;
	this.hidden.value = match.value;
	this.hideChoices();
}

Fiz.AutocompleteFormElement.prototype.setDataset = function(dataset)
{
	if(undefined != dataset.data
		&& undefined == dataset.data.length)
		this.dataset = [dataset.data];
	else
		this.dataset = dataset.data;
	
	this.redraw();
}

Fiz.AutocompleteFormElement.prototype.highlightSelection = function(elem)
{
	if(null != this.curSelection)
		Fiz.removeClass(this.curSelection, 'hover');
	
	if(null != elem) {
		this.field.value = elem.match.name;
		this.hidden.value = elem.match.value;
		Fiz.addClass(elem, 'hover');
	}
	this.curSelection = elem;
}

Fiz.AutocompleteFormElement.prototype.prevResult = function()
{
	if(null != this.curSelection.previousSibling) {
		this.highlightSelection(this.curSelection.previousSibling);
	} else {
		this.field.value = this.curQuery;
		this.highlightSelection(null);
	} 
}

Fiz.AutocompleteFormElement.prototype.nextResult = function()
{
	if(null == this.curSelection) {
		this.highlightSelection(this.dropdown.firstChild);
 	} else if(null != this.curSelection.nextSibling) {
		this.highlightSelection(this.curSelection.nextSibling);
	}
}

function getKeyCode(e) {
	if(window.event)
		return e.keyCode;
	else
		return e.which;
}

Fiz.AutocompleteFormElement.prototype.fetchResult = function(e)
{
	if((getKeyCode(e) >= 48 && getKeyCode(e) <= 90)
			|| getKeyCode(e) == 8 || getKeyCode(e) == 32) {
		var ajax = new Fiz.Ajax({url: '/AutocompleteFormElement/ajaxQuery?id=name&query=' + document.getElementById('name_field').value});
	}
}