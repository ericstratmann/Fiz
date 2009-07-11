/** AutocompleteFormElements.js --
 *
 * This file provides Javascript functions needed to implement the
 * AutocompleteFormElement class.  One Fiz.AutocompleteFormElement is
 * created for each AutocompleteFormElement Java object.  Methods on the
 * Javascript are invoked for functions such as populating the autcomplete
 * list, fetching data from the server, and navigating the list of results.
 */

// The following lines are used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js
// Fiz:include static/fiz/Ajax.js

/**
 * Create a AutocompleteFormElement object.
 * @param id                       Id for the {@code <div>} element
 *                                 that represents the autcomplete form.
 */
Fiz.AutocompleteFormElement = function(id, numResults)
{
	var self = this;
	
	// Set default values for this form element
	this.id = id;
	this.numResults = (undefined == numResults ? 5 : numResults);

	this.isOpen = false; // Tells us if the autocomplete dropdown is visible
	this.keepOpen = false; // Tells us if we should keep the autocomplete
												 // dropdown visible for the time being, prevents
												 // the dropdown from hiding because of the onblur
												 // event

	this.input = document.getElementById(id + '_input');

	// Turn off browser autocomplete, not supported in all browsers and
	// not part of the web standard
	this.input.setAttribute('autocomplete', 'off');
	
	// Provides keyboard navigation of the list
	this.input.onkeydown = function(e) {
		if (self.isOpen) {
			switch(Fiz.getKeyCode(e)) {
				// Up arrow
				case 38: self.prevResult(); e.preventDefault(); break;
				// Down arrow
				case 40: self.nextResult(); break;
				// Enter key
				case 13:
					self.hideChoices();
					if (null != self.curSelection) {
						self.selectElement(self.curSelection.result);
					}
					break; 
			}
		} else {
			// Allows us to open the dropdown again with our keyboard
			// if it has been hidden
			if (Fiz.getKeyCode(e) == 40 || Fiz.getKeyCode(e) == 38) {
				self.showChoices();
			}
		}
	};
	
	// Used to prevent the default behavior of the enter key (form submission)
	// and up arrow (move cursor to beginning of form)
	this.input.onkeypress = function(e) {
		if (Fiz.getKeyCode(e) == 38 || Fiz.getKeyCode(e) == 13) {
			return false;
		} else {
			return true;
		}
	};

	this.dropdown = document.getElementById(id + '_dropdown');
	
	// Tracks when the mouse is over the dropdown to disable the
	// onblur event
	this.dropdown.onmouseover = function() { self.keepOpen = true; };
	this.dropdown.onmouseout = function() { self.keepOpen = false; };

	// Hidden field used to store actual value passed by the form
	this.hidden = document.getElementById(id + '_hidden');
		
	this.dataset = [];
}

/**
 * This method opens up the autocomplete dropdown. If there is
 * currently no query, this method does nothing.
 */
Fiz.AutocompleteFormElement.prototype.showChoices = function()
{
	// Do not show the autocomplete if there is no input
	if (this.input.value == '') {
		this.keepOpen = false;
		this.hideChoices();
		return;
	}
		
	this.isOpen = true;
	
	// Find the location of the input field
	var x = 0;
	var y = 3 + this.input.offsetHeight;
		
	// Attach calendar picker to the input field
	this.dropdown.style.top = y + 'px';
	this.dropdown.style.left = x + 'px';
	this.dropdown.style.display = 'block';
}

/**
 * This method hides the autocomplete dropdown IF hiding
 * is enabled (hiding will be disabled when the user mouses
 * over dropdown)
 */
Fiz.AutocompleteFormElement.prototype.hideChoices = function()
{
	if (!this.keepOpen) {
		this.isOpen = false;
		this.dropdown.style.display = 'none';
	}
}

/** 
 * This method updates the dropdown to show the autocomplete
 * results for the current input.
 */
Fiz.AutocompleteFormElement.prototype.redraw = function()
{
	var self = this;
	
	this.curSelection = null;
	
	// We clear the autocomplete list
	while (this.dropdown.hasChildNodes()) {
		this.dropdown.removeChild(this.dropdown.firstChild);
	}

	if (1 == this.dataset.length) {
		// A dataset of length 1 means we only have the query
		// (which is included with every dataset)
		var item = document.createElement('li');
		item.textContent = 'No results found';
		this.dropdown.appendChild(item);
		this.highlightSelection(null);
	} else {
		for(var resultIndex in this.dataset) {
			var item = this.resultItem(this.dataset[resultIndex]);
			this.dropdown.appendChild(item);
		}
	}
}

/**
 * This method constructs a DOM element for an autocomplete value
 * and sets up mouse listeners and values.
 * @param result						(Object) An autocomplete query result
 * @return								(Element) DOM element representing the query result
 */
Fiz.AutocompleteFormElement.prototype.resultItem = function(result)
{
	var item = document.createElement('li');
	var self = this;

	// We store the autocomplete result object in our element
	// for easy of access and retrieval
	item.result = result;
	
	if(result.isQuery) {
		Fiz.addClass(item, 'query');
		this.highlightSelection(item);
	}
	
	// If the developer does not provide a value for an item,
	// the value defaults to the name of the selection
	if(undefined == result.value) {
		result.value = result.name;
	}
	
	item.textContent = result.name;
	item.onclick = (function(result) {
		return function() {
			self.selectElement(result);
		};
	})(result);
	item.onmouseover = function() {
		self.highlightSelection(this);
	}
	
	return item;
}

/**
 * This method is invoked when the user clicks or selects one of the
 * autocomplete results, populating the input field with the user
 * selection and hiding the dropdown.
 * @param result					(Object) An autocomplete query result
 */
Fiz.AutocompleteFormElement.prototype.selectElement = function(result)
{
	this.keepOpen = false;
	this.input.value = result.name;
	this.hidden.value = result.value;
	this.hideChoices();
}

/**
 * This method sets the dataset that contains an array of the autocomplete
 * query results.
 * @param dataset				(Object) A list of names (along with possibly
 *											values) used to populate the autocomplete dropdown
 */
Fiz.AutocompleteFormElement.prototype.setDataset = function(dataset)
{
	if (undefined == dataset.data) {
		// We got no result back
		this.dataset = [];
	} else {
		if(undefined == dataset.data.length) {
			// There is only one result
			this.dataset = [dataset.data];
		} else {
			// We got a list of results
			this.dataset = dataset.data;
		}	
	}	
	
	// Append the query string to the front of our dataset
	this.dataset.unshift({
			name: this.input.value,
			value: this.input.value,
			isQuery: true
	});
	
	this.redraw();
}

/**
 * This method highlights the user's current selection in the dropdown.
 * The input field is also updated to correspond with the highlighted entry.
 * @param elem					(Element) The element to highlight
 */
Fiz.AutocompleteFormElement.prototype.highlightSelection = function(elem)
{
	// Remove the highlight off the current selection
	if (null != this.curSelection) {
		Fiz.removeClass(this.curSelection, 'hover');
	}
	
	// And highlight our new element, updating the input field in the
	// process
	if (null != elem) {
		this.input.value = elem.result.name;
		this.hidden.value = elem.result.value;
		Fiz.addClass(elem, 'hover');
	}
	this.curSelection = elem;
}

/**
 * This method moves the selection up on the dropdown list.
 */
Fiz.AutocompleteFormElement.prototype.prevResult = function()
{
	if (null != this.curSelection
			&& null != this.curSelection.previousSibling) {
		this.highlightSelection(this.curSelection.previousSibling);
	} 
}

/**
 * This method moves the selection down on the dropdown list.
 */
Fiz.AutocompleteFormElement.prototype.nextResult = function()
{
	if (null == this.curSelection) {
		// Just highlight the query if nothing is selected
		this.highlightSelection(this.dropdown.firstChild);
 	} else if (null != this.curSelection
 			&& null != this.curSelection.nextSibling) {
		this.highlightSelection(this.curSelection.nextSibling);
	}
}

/**
 * This method generates an Ajax call that retrieves the autocomplete
 * results from the server.
 * @param e					(Event) Key listener event that activated this
 *									function.
 * @return					(Fiz.Ajax) Returns the Ajax object for testing
 *									purposes
 */
Fiz.AutocompleteFormElement.prototype.fetchResult = function(e)
{
	// We only fetch results if a character key, backspace, del are pressed
	if ((Fiz.getKeyCode(e) >= 48 && Fiz.getKeyCode(e) <= 90)
			|| (Fiz.getKeyCode(e) >= 186 && Fiz.getKeyCode(e) <= 222)
			|| (Fiz.getKeyCode(e) >= 96 && Fiz.getKeyCode(e) <= 111)
			|| Fiz.getKeyCode(e) == 8 || Fiz.getKeyCode(e) == 32
			|| Fiz.getKeyCode(e) == 46) {
		return new Fiz.Ajax({
				url: '/AutocompleteFormElement/ajaxQuery',
				data: {
						id: this.id,
						query: this.input.value
				}
		});
	}
	return null;
}