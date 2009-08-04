/* AutocompleteFormElements.js --
 *
 * This file provides Javascript functions needed to implement the
 * AutocompleteFormElement class.  One Fiz.AutocompleteFormElement
 * is created for each AutocompleteFormElement Java object. 
 * Methods on the object are invoked for functions such as
 * populating the autcomplete list, fetching data from the server,
 * and navigating the list of results.
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
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

// The following lines are used by Fiz to manage Javascript dependencies.
// Fiz:include static/fiz/Fiz.js
// Fiz:include static/fiz/Ajax.js

// Define symbolic names for keycodes used by the AutocompleteFormElement
Fiz.Key = {
    UP: 38, DOWN: 40, ENTER: 13, ESC: 27, TAB: 9
};

/**
 * Create a AutocompleteFormElement object.
 * @param id                       Id for the {@code <div>} element
 *                                 that represents the autcomplete form.
 */
Fiz.AutocompleteFormElement = function(id)
{
    // Set the id identifying the <input> element for this form
    // element and from which all related ids are derived
    this.id = id;

    // Indicates whether the dropdown is visible or not
    this.isOpen = false;
    
    // Used to disable to onblur event handler. We want to keep the dropdown 
    // open if the user is clicking a choice in the dropdown. By default,
    // the input field will lose focus, thus closing the dropdown before the
    // user's click is registered.
    this.keepOpen = false;
        
    // The currently selected choice in the dropdown menu (this value will be
    // shown in the input field)
    this.selected = null;
    
    // The currently highlighted choice in the dropdown menu (this value may not
    // neccessarily be displayed in the input field if the choice is highlighted
    // when the mouse hovers over it)
    this.highlighted = null;
    
    // <input> element for which this autocomplete object is registered
    this.input = document.getElementById(id);
    
    // Stores the last value the user entered into the <input> field so that
    // it can be restored if needed
    this.lastUserInput = this.input.value;
    
    // Stores the last value displayed in the <input> field to prevent
    // refreshMenu from being called if the value has not changed
    this.lastInputValue = this.input.value;
    
    // Disables browser built-in autocomplete, not supported in all browsers and
    // not part of the web standard
    this.input.setAttribute('autocomplete', 'off');
    
    // Dropdown menu of autocomplete choices
    this.dropdown = document.getElementById(id + '_dropdown');
}

/**
 * This method is invoked when a keydown event occurs while the <input> element
 * is in focus. Overrides the default key behavior of the tab, enter, esc,
 * up arrow, down arrow keys in order to provide mechanisms such as keyboard
 * navigation and selection.
 * @param e                    (Event) The event object containing information
 *                             about the keystroke and event type.
 */
Fiz.AutocompleteFormElement.prototype.captureKeydown = function(e)
{
    if (this.isOpen) {
        switch(Fiz.getKeyCode(e)) {
            // Move the user selection up the list of choices
            case Fiz.Key.UP:
                this.prevResult();
                // Prevent the cursor from moving to the beginning of the input
                // field when the user is navigating
                e.preventDefault();
                return false;
            // Move the user selection down the list of choices
            case Fiz.Key.DOWN:
                this.nextResult();
                // Prevent the cursor from moving to the end of the input field
                // when the user is navigating
                e.preventDefault();
                return false;
            // Finalize the user selection and put the value of the selection in
            // the input field
            case Fiz.Key.ENTER:
            // Manually hide the dropdown
            case Fiz.Key.ESC:
                this.hideDropdown(true);
                e.preventDefault();
                return false;
            // Hide the dropdown when the user navigats to another field
            case Fiz.Key.TAB:
                this.hideDropdown(true);
                return true;
        }
    } else {
        // Open the dropdown again if the user wants to pick another choice from
        // the autocomplete list
        if (Fiz.getKeyCode(e) == Fiz.Key.UP
                || Fiz.getKeyCode(e) == Fiz.Key.DOWN) {
            this.showDropdown();
            e.preventDefault();
            return false;
        }
    }    
}

/**
 * This method opens up the autocomplete dropdown. If there is
 * currently no query, this method does nothing.
 */
Fiz.AutocompleteFormElement.prototype.showDropdown = function()
{
    if (this.input.value == '') {
        this.hideDropdown();
        return;
    }
    
    // Reset the state of the dropdown menu
    this.isOpen = true;
    
    // Find the location of the input field
    var x = 0;
    var y = this.input.offsetHeight - 1;
        
    // Attach dropdown to the input field
    this.dropdown.style.top = y + 'px';
    this.dropdown.style.left = x + 'px';
    this.dropdown.style.display = 'block';
}

/**
 * This method hides the autocomplete dropdown if hiding is enabled (hiding
 * will be disabled when the user mouses over dropdown to select something).
 * @param forceHide            (Boolean) Forces the dropdown to hide even if
 *                             the user has their mouse over the dropdown
 */
Fiz.AutocompleteFormElement.prototype.hideDropdown = function(forceHide)
{
    if (forceHide) {
        this.keepOpen = false;
    }
    if (!this.keepOpen) {
        this.isOpen = false;
        this.dropdown.style.display = 'none';
        this.input.focus();
    }
}

/**
 * This method selects the autocomplete entry, highlighting it and filling
 * in the input field.
 * @param choice              (Element) An <li> element in the dropdown menu
 * @param done                (Boolean) Hides the dropdown after selection
 *                            if true
 */
Fiz.AutocompleteFormElement.prototype.selectChoice = function(choice, done)
{    
    // Select a new option
    if (null == choice) {
        this.input.value = this.lastUserInput;
    } else {
        this.input.value = Fiz.getText(choice);        
    }
    
    this.lastInputValue = this.input.value;
    this.selected = choice;

    // Even if done is set to true (the dropdown closes after the selection),
    // we highlight the option in case the dropdown is opened again.
    this.highlightChoice(choice);
    
    if (done) {
        this.hideDropdown(true);
    }
}

/**
 * This method highlights the user's current choice in the dropdown.
 * @param choice                    (Element) The element to highlight
 */
Fiz.AutocompleteFormElement.prototype.highlightChoice = function(choice)
{
    // Clear the old highlighted element
    if (null != this.highlighted) {
        Fiz.removeClass(this.highlighted, 'highlight');
    }
    
    // Highlight a new option
    if (null != choice) {
        Fiz.addClass(choice, 'highlight');
    }
    this.highlighted = choice;
}

/**
 * This method moves the selection up on the dropdown list.
 */
Fiz.AutocompleteFormElement.prototype.prevResult = function()
{
    if (null == this.highlighted) {
        // The user is currently "focused" on the input field so
        // pressing up starts from the bottom of the autocomplete list
        this.selectChoice(document
        		.getElementById(this.id + "_choices").lastChild);
    } else {
        this.selectChoice(this.highlighted.previousSibling);
    }
}

/**
 * This method moves the selection down on the dropdown list.
 */
Fiz.AutocompleteFormElement.prototype.nextResult = function()
{
    if (null == this.highlighted) {
        // The user is currently "focused" on the input field so
        // pressing up starts from the bottom of the autocomplete list
        this.selectChoice(document
        		.getElementById(this.id + "_choices").firstChild);
    } else {
        this.selectChoice(this.highlighted.nextSibling);
    }
}

/**
 * This method generates an Ajax call that retrieves the autocomplete
 * results from the server.
 * @return                    (Fiz.Ajax) Returns the Ajax object for testing
 *                            purposes
 */
Fiz.AutocompleteFormElement.prototype.refreshMenu = function()
{
    if (this.input.value == '') {
        this.hideDropdown(true);
        return null;
    } else {
        // We launch a new Ajax request if the form value has changed.
        if (this.input.value != this.lastInputValue) {
            // Reset the state of the dropdown since we are clearing all its
            // contents
            this.selected = null;
            this.highlighted = null;
            this.lastInputValue = this.input.value;
            this.lastUserInput = this.input.value;
            return new Fiz.Ajax({
                    url: '/AutocompleteFormElement/ajaxQuery',
                    data: { id: this.id, userInput: this.input.value }
            });
        }
        return null;
    }
}