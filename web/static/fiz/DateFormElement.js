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
var MONTH_LONG = [ 'January', 'February', 'March', 'April',
				   'May', 'June', 'July', 'August',
				   'September', 'October', 'November', 'December' ];

var MONTH_SHORT = [ 'Jan', 'Feb', 'Mar', 'Apr',
					'May', 'Jun', 'Jul', 'Aug',
					'Sep', 'Oct', 'Nov', 'Dec' ];

var WEEK_LONG = [ 'Sunday', 'Monday', 'Tuesday', 'Wednesday',
				  'Thursday', 'Friday', 'Saturday' ];

var WEEK_MEDIUM = [ 'Sun', 'Mon', 'Tue', 'Wed', 'Thur', 'Fri', 'Sat' ];

var WEEK_SHORT = [ 'S', 'M', 'Tu', 'W', 'Th', 'F', 'S' ];

/**
 * Create a DateFormElement object.
 * @param id                       Id for the {@code <div>} element
 *                                 that represents the calendar.
 */
Fiz.DateFormElement = function(id)
{
	this.id = id;
		
	// Set some default values
	this.sDate = new Date();	// Selected date: Highlighted in picker, displayed in input field
	this.dDate = new Date();	// Displayed date: First day of the month currently displayed in picker
	this.dDate.setDate(1);
	
	this.field = document.getElementById(this.id + '_field');
	this.picker = document.getElementById(this.id + '_picker');
	this.picker.onmousedown = function(e) {
		cancelBubble(e);
	};
}

Fiz.DateFormElement.BOTTOM = 0;
Fiz.DateFormElement.RIGHT = 1;

/**
 * This method is used to check that the date in the input field is in
 * a valid format. It then processes the input and displays it on the calendar.
 * This function can be used to make real time updates to the calendar display.
 */
Fiz.DateFormElement.prototype.validateAndUpdate = function()
{
//	var dateFormat= /^\d{1,2}\/\d{1,2}\/((\d{2})|(\d{4}))$/;
//	if(dateFormat.test(this.field.value)) {
		// Update the state of the picker to reflect its
		// new attachment	
//		var tempDate = new Date(this.field.value);
//		this.sDate.setTime(tempDate.getTime());
//		this.dDate.setFullYear(tempDate.getFullYear());
//		this.dDate.setMonth(tempDate.getMonth());
		this.redraw();
//	}
}

/**
 * This method opens up the calendar picker window and updates
 * its display and position, attaching it to the corresponding
 * input field.
 */
Fiz.DateFormElement.prototype.openPicker = function()
{
	// Used for accessing this object from
	// anonymous functions
	var self = this;
	
	// Find the location of the input field
	var pos = findPos(this.field);
	var x, y;

	if(this.attachPosition == Fiz.DateFormElement.RIGHT) {
		x = pos[0] + 3 + this.field.offsetWidth;
		y = pos[1];
	} else {
		x = pos[0];
		y = pos[1] + 3 + this.field.offsetHeight;
	}
		
	// Attach calendar picker to the input field
	this.picker.style.top = y + 'px';
	this.picker.style.left = x + 'px';
	this.picker.style.display = 'block';

	// Update the state of the picker to reflect
	// the value of the field it is attached to
	this.validateAndUpdate();
	
	// Store and attach our calendar picker mouse handler
	// to the old document mouse handler. We store the old
	// one so it can be restored when the calendar picker
	// is hidden.
	this.docListener = document.body.onmousedown;
	document.body.onmousedown = function() {
		self.closePicker();
	};
}
	
/**
 * This method closes the calendar picker.
 */
Fiz.DateFormElement.prototype.closePicker = function()
{
	this.picker.style.display = 'none';
	document.body.onmousedown = this.docListener;
}

/**
 * This method updates the calendar to reflect any changes made to
 * it such as navigating to a different month and year or making a
 * selection.
 */
Fiz.DateFormElement.prototype.redraw = function()
{
	var self = this;
	var grid = document.getElementById(this.id + '_grid'); // references tbody element
	
	// Get calendar information (number of days in the month and the day of the week
	// on which it begins), for the current month
	var curDay = this.dDate.getDate();
	var curMonth = this.dDate.getMonth();
	var curYear = this.dDate.getFullYear();
	var curNumDays = Fiz.DateFormElement.getNumberOfDays(curMonth + 1, curYear); // Number of days in curMonth
	var curDayOfWeek = this.dDate.getDay();
	
	// Update heading with new month and year displayed
	document.getElementById(this.id + '_header').textContent = MONTH_LONG[curMonth] + ' ' + curYear;
	
	// Get calendar information for the previous
	var prevDate = new Date();
	prevDate.setMonth(curMonth - 1);

	// Number of days in prevMonth
	var prevNumDays = Fiz.DateFormElement.getNumberOfDays(prevDate.getMonth() + 1, prevDate.getFullYear());
	
	var rows = grid.childNodes; // references the tr elements in the tbody element
	var curRow = 0;
	
	// We go through and draw the tail end of last month up to
	// the day of the week the current month starts
	var row = rows[curRow].childNodes; // references the td elements in the tr element
	for(var i = 0; i < curDayOfWeek; i++) {
		Fiz.DateFormElement.dateCell(row[i], prevNumDays - curDayOfWeek + 1 + i);
	}
	
	// We fill in the calendar with the dates for the current month
	for(var i = 0; i < curNumDays; i++) {
		var item = Fiz.DateFormElement.dateCell(row[curDayOfWeek], i + 1);

		// We use JavaScript closure to pass in
		// parameters to the onclick function
		item.onclick = (function(month, day, year) {
			return function() {
				self.selectDate(month, day, year);
				self.closePicker();
			};
		})(curMonth + 1, i + 1, curYear);
		item.onmouseover = function() { Fiz.DateFormElement.highlight(this, true); };
		item.onmouseout = function() { Fiz.DateFormElement.highlight(this, false); };
			
		// Style the item
		addClass(item, 'cur-month');
		
		// If the date of the current cell matches the selected date,
		// we give it a class name so we can style it
		if(Fiz.DateFormElement.compareDate(this.sDate, new Date(curYear, curMonth, i + 1)) == 0) {
			addClass(item, 'cur-day');
		}
		
		curDayOfWeek++;
		curDayOfWeek %= 7;

		// We start a new row
		if(curDayOfWeek == 0) {
			curRow++;
			var row = rows[curRow].childNodes;
		}		
	}
	
	// We fill in the calendar (up to maximum of 6 rows)
	// with the dates for the next month
	for(var i = 0; curRow < 6; i++) {
		var row = rows[curRow].childNodes;
		Fiz.DateFormElement.dateCell(row[curDayOfWeek], i + 1);

		curDayOfWeek++;
		curDayOfWeek %= 7;

		// It's the first day of the week so we start
		// a new row
		if(curDayOfWeek == 0) {
			curRow++;
		}
	}
}
 
/**
 * This function is used to assign a new date to a cell in the calendar and
 * clears up event handlers and styles on the cell. This function returns
 * the cell so the caller can make additional modifications if needed (like
 * class names or DOM properties).
 * @param cell					The DOM element that represents the cell
 * @param date					The day of the month value for the cell
 * @return						The DOM element passed into the function
 */
Fiz.DateFormElement.dateCell = function(cell, date) 
{
	// Clear all event handlers on the cell
	cell.onclick = null;
	cell.onmouseover = null;
	cell.onmouseout = null;
	
	cell.className = '';
	cell.textContent = date;

	return cell;
}

/**
 * This method is invoked by clicking on a date, setting that as the
 * current value of the input field.
 * @param month					newly selected month
 * @param day					newly selected day
 * @param year					newly selected year
 */
Fiz.DateFormElement.prototype.selectDate = function(month, day, year)
{
	this.dDate.setMonth(month - 1);
	this.dDate.setFullYear(year);

	this.sDate.setDate(day);
	this.sDate.setMonth(month - 1);
	this.sDate.setFullYear(year);
		
	this.field.value = Fiz.DateFormElement.formatDate(this.sDate, this.dateFormat);
	this.redraw();
}

 /**
  * This method is used to navigate the calendar to the previous year.
  */
Fiz.DateFormElement.prototype.prevYear = function()
{
	this.dDate.setFullYear(this.dDate.getFullYear() - 1);
	this.redraw();
}

/**
 * This method is used to navigate the calendar to the previous month.
 */
Fiz.DateFormElement.prototype.prevMonth = function()
{
	this.dDate.setMonth(this.dDate.getMonth() - 1);
	this.redraw();
}

/**
 * This method is used to navigate the calendar to today.
 */
Fiz.DateFormElement.prototype.today = function()
{
	this.dDate.setTime(new Date().getTime());
	this.redraw();
}

/**
 * This method is used to navigate the calendar to the next month.
 */
Fiz.DateFormElement.prototype.nextMonth = function()
{
	this.dDate.setMonth(this.dDate.getMonth() + 1);
	this.redraw();
}

/**
 * This method is used to navigate the calendar to the next year.
 */
Fiz.DateFormElement.prototype.nextYear = function()
{
	this.dDate.setFullYear(this.dDate.getFullYear() + 1);
	this.redraw();
}

/**
 * This function is used to construct a date string with a user specified format.
 * @param date					The date to be converted
 * @param format				The format of the outputted date
 * @return						A date string in the user specified format
 */
Fiz.DateFormElement.formatDate = function(date, format)
{
	if(!format) format = "m/d/Y";
	
	var month = (date.getMonth() + 1).toString();
	var year = date.getFullYear().toString();
	var day = date.getDate().toString();
	
	var paddedMonth = month;
	while (paddedMonth.length < 2) {
		paddedMonth = '0' + paddedMonth;
	}

	var paddedDay = day;
	while (paddedDay.length < 2) {
		paddedDay = '0' + paddedDay;
	}

	format = format.replace("m", month);
	format = format.replace("M", paddedMonth);
	format = format.replace("y", year.substring(2, 4));
	format = format.replace("Y", year);
	format = format.replace("d", day);
	format = format.replace("D", paddedDay);
	
	return format;
}
 
/**
 * This function is used to toggle the highlight a date on mouseover.
 * @param elem					The date to highlight
 * @param enable				true to highlight, false to remove highlight
 */
Fiz.DateFormElement.highlight = function(elem, enable)
{
	if(enable)
		addClass(elem, 'hover');
	else
		removeClass(elem, 'hover');
}

/**
 * This function is used to check if two dates share the same calendar day.
 * @param dateA					The first date to compare
 * @param dateB					The second date to compare
 * @return						0 if the two dates have the same month, day, and year,
 * 								-1 if dateA is before dateB, and
 *								1 if dateB is before dateA
 */
Fiz.DateFormElement.compareDate = function(dateA, dateB)
{
	if(dateA.getDate() == dateB.getDate()
			&& dateA.getMonth() == dateB.getMonth()
			&& dateA.getFullYear() == dateB.getFullYear()) {
		return 0;
	} else {
		return (dateA < dateB ? -1 : 1);
	}
}

/**
 * This function returns the number of days in the month and year passed in.
 * Leap years are considered by this function.
 * @param month					Month for which we are getting the
 * 								number of days
 * @param year					Year for which we are getting the 
 * 								number of days
 * @return						The number of days for month and year
 */
Fiz.DateFormElement.getNumberOfDays = function(month, year)
{
	switch(month) {
		case 2:
			if(year % 400 == 0)
				return 29;
			else if(year % 100 == 0)
				return 28;
			else if(year % 4 == 0)
				return 29;
			else
				return 28;
		case 4: return 30;
		case 6: return 30;
		case 9: return 30;
		case 11: return 30;
		default: return 31; // All the other months of 31 days
	}
}

function addClass(elem, className)
{
	if(elem.className.match(className) == null)
		elem.className += ' ' + className;
}

function removeClass(elem, className)
{
	elem.className = elem.className.replace(className, '');
}

function findPos(obj)
{
	var curleft = 0;
	var curtop = 0;
	if(obj.offsetParent) {
		do {
			curleft += obj.offsetLeft;
			curtop += obj.offsetTop;
		} while(obj = obj.offsetParent);
	}
	return [curleft,curtop];
}

function cancelBubble(e)
{
	if (!e) var e = window.event;
	e.cancelBubble = true;
	if (e.stopPropagation) e.stopPropagation();
}