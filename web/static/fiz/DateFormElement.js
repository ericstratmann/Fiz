/* DateFormElement.js --
 *
 * This file provides Javascript functions needed to implement the
 * DateFormElement class.  One Fiz.DateFormElement is created
 * for each DateFormElement Java object.  Methods on the Javascript
 * are invoked for functions such as redrawing and navigating the calendar,
 * showing and hiding the calendar, and updating the form element.
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
// Fiz:include static/fiz/date.js

/**
 * Create a DateFormElement object.
 * @param id                    (String) Id for the {@code <div>} element
 *                              that represents the calendar.
 * @param dateFormat            (String) Specifies the format that the date
 *                              will be displayed in the input field
 * @param attachPosition        (String) Specifies where the calendar will
 *                              open up in relation to input field (either
 *                              bottom or right)
 */
Fiz.DateFormElement = function(id, dateFormat, attachPosition)
{
    // Set default values for this form element
    this.id = id;
    this.dateFormat = (undefined == dateFormat) ? 'm/d/Y' : dateFormat;
    this.attachPosition =
            (undefined == attachPosition) ? 'bottom' : attachPosition;

    // The date filter rules for determining whether a date can be selected
    // or not
    this.filters = [];
    
    // The calendar starts displaying from this month / year
    this.startLimit = null;
    
    // The calendar stops displaying on this month / year
    this.endLimit = null;
    
    // Determines if the picker is open or not
    this.isOpen = false;
        
    // Selected date: Highlighted in picker, displayed in input field
    this.sDate = new Date();

    // Displayed date: First day of the month currently displayed in picker
    this.dDate = new Date();
    this.dDate.setDate(1);
    
    // Retrieve and the various DOM elements used for this DateFormElement
    this.input = document.getElementById(this.id);
    this.picker = document.getElementById(this.id + '_picker');
    this.icon = document.getElementById(this.id + '_icon');

    // Format the default date into the format specified by dateFormat
    this.validateAndUpdate();
}

/**
 * This method is used to check that the date in the input field is in
 * a valid format and updates the field with the new date.
 */
Fiz.DateFormElement.prototype.validateAndUpdate = function()
{
    var tempDate = Date.parse(this.input.value);
    if (tempDate != null) {
        this.input.value =
                Fiz.DateFormElement.formatDate(tempDate, this.dateFormat);
    }
}

/**
 * This method is used to check that the date in the input field is in
 * a valid format and updates the calendar to show the new date.
 */
Fiz.DateFormElement.prototype.validateAndDisplay = function()
{
    var tempDate = Date.parse(this.input.value);
    if (tempDate != null) {
        this.setDisplayedDate(tempDate);
        this.setSelectedDate(tempDate);
        this.redraw();
    }
}

/**
 * This method opens the picker if it is closed and closes the picker if it
 * open.
 */
Fiz.DateFormElement.prototype.togglePicker = function()
{
    if (this.isOpen) {
        this.closePicker();
    } else {
        this.openPicker();
    }
}

/**
 * This method opens up the calendar picker window and updates
 * its display and position, attaching it to the corresponding
 * input field.
 */
Fiz.DateFormElement.prototype.openPicker = function()
{
    // Find the location of the input field
    var x, y;

	if (this.attachPosition == 'right') {
		x = 3 + this.input.offsetWidth;
		y = 0;
	} else {
		x = 0;
		y = 3 + this.input.offsetHeight;
	}

	// Attach calendar picker to the input field
	this.picker.style.top = y + 'px';
	this.picker.style.left = x + 'px';
	this.picker.style.display = 'block';

	// Update the state of the picker to reflect
    // the value of the field it is attached to
    this.validateAndDisplay();

    this.isOpen = true;
}

/**
 * This method hides the calendar and updates the field to reflect
 * the selection made.
 */
Fiz.DateFormElement.prototype.closePicker = function()
{
    this.picker.style.display = 'none';
    this.isOpen = false;
    
    this.input.value =
            Fiz.DateFormElement.formatDate(this.sDate, this.dateFormat);
}

/**
 * This method updates the calendar to reflect any changes made to
 * it such as navigating to a different month and year or making a
 * selection.
 */
Fiz.DateFormElement.prototype.redraw = function()
{
    // Get calendar information (number of days in the month and the day of the
    // week on which it begins), for the current month
    var curDay = this.dDate.getDate();
    var curMonth = this.dDate.getMonth();
    var curYear = this.dDate.getFullYear();

    // Number of days in curMonth
    var curNumDays = Fiz.DateFormElement.getNumberOfDays(curMonth, curYear);

    // Day of the week on whic the month starts
    var curDayOfWeek = this.dDate.getDay();
    
    // Update heading with new month and year displayed
    Fiz.setText(document.getElementById(this.id + '_header'),
            Fiz.DateFormElement.i18n.monthNamesLong[curMonth] + ' ' + curYear);

    // Get calendar information for the previous and next months
    var prevDate = new Date();
    prevDate.setFullYear(curYear);
    prevDate.setMonth(curMonth - 1);

    var nextDate = new Date();
    nextDate.setFullYear(curYear);
    nextDate.setMonth(curMonth + 1);

    // Change the navigation depending on the limits set
    this.redrawNav(prevDate, this.dDate, nextDate);
    
    // Number of days in prevMonth
    var prevNumDays = Fiz.DateFormElement.getNumberOfDays(
            prevDate.getMonth(),
            prevDate.getFullYear()
    );

    var curWeek = 0;
    
    // <tbody> element
    var month = document.getElementById(this.id + '_grid');
    
    // <tr> elements in the <tbody>
    var weeks = month.getElementsByTagName('tr');

    // <td> elements in the <tr>
    var week = weeks[curWeek].getElementsByTagName('td');

    // We go through and draw the tail end of last month up to
    // the day of the week the current month starts
    for (var i = 0; i < curDayOfWeek; i++) {
        var date = new Date(prevDate.getFullYear(), prevDate.getMonth(),
                prevNumDays - curDayOfWeek + 1 + i);
        this.dateCell(week[i], date, false);
    }
    
    // We fill in the calendar with the dates for the current month
    for (var i = 1; i <= curNumDays; i++) {
        var date = new Date(curYear, curMonth, i);
        var item = this.dateCell(week[curDayOfWeek], date, !this.isExcluded(date));
            
        // Style the item
        Fiz.addClass(item, 'cur-month');
        
        // If the date of the current cell matches the selected date,
        // we give it a class name so we can style it
        if (Fiz.DateFormElement.compareDate(this.sDate, date) == 0) {
            Fiz.addClass(item, 'cur-day');
        }
        
        curDayOfWeek++;
        curDayOfWeek %= 7;

        // We start a new week
        if (curDayOfWeek == 0) {
            curWeek++;
            week = weeks[curWeek].getElementsByTagName('td');
        }
    }
    
    // We fill in the calendar (up to maximum of 6 weeks)
    // with the dates for the next month
    for (var i = 0; curWeek < 6; i++) {
        var date = new Date(nextDate.getFullYear(), nextDate.getMonth(), i + 1);
        var week = weeks[curWeek].getElementsByTagName('td');
        this.dateCell(week[curDayOfWeek], date, false);

        curDayOfWeek++;
        curDayOfWeek %= 7;

        // It's the first day of the week so we start
        // a new week
        if (curDayOfWeek == 0) {
            curWeek++;
        }
    }
}

/**
 * This method updates the navigation bar to restrict navigating to only
 * dates that are in the user specified range.
 * @param prevDate                (Date) Some random day in the previous month
 * @param curDate                 (Date) The first day of the current month
 * @param nextDate                (Date) Some random day in the next month
 */
Fiz.DateFormElement.prototype.redrawNav = function(prevDate, curDate, nextDate)
{
    var prevYearLink = document.getElementById(this.id + "_prevYear");
    var prevMonthLink = document.getElementById(this.id + "_prevMonth");
    var nextMonthLink = document.getElementById(this.id + "_nextMonth");
    var nextYearLink = document.getElementById(this.id + "_nextYear");

    prevYearLink.style.visibility = 'visible';
    prevMonthLink.style.visibility = 'visible';
    nextMonthLink.style.visibility = 'visible';
    nextYearLink.style.visibility = 'visible';

    if (this.startLimit != null) {
        // We hide the previous month link if the previous month is earlier
        // than the starting month of the calendar
        if (prevDate.getMonth() < this.startLimit.getMonth()
                && prevDate.getFullYear() <= this.startLimit.getFullYear()) {
            prevMonthLink.style.visibility = 'hidden';
        }
        
        // We hide the previous year link in two cases:
        // 1) We are already displaying the earliest year for which
        //        we have months to display
        // 2) We are displaying the year after the earliest year 
        //        but an earlier month
        if ((curDate.getFullYear() <= this.startLimit.getFullYear())
                || (curDate.getFullYear() == this.startLimit.getFullYear() + 1
                && curDate.getMonth() < this.startLimit.getMonth())) {
            prevYearLink.style.visibility = 'hidden';
        }
    }

    if (this.endLimit != null) {
        // We hide the next month link if the next month is later
        // than the starting month of the calendar
        if (nextDate.getMonth() > this.endLimit.getMonth()
                && nextDate.getFullYear() >= this.endLimit.getFullYear()) {
            nextMonthLink.style.visibility = 'hidden';
        }

        // We hide the next year link in two cases:
        // 1) We are already displaying the latest year for which
        //    we have months to display
        // 2) We are displaying the year before the latest year 
        //    but a later month
        if ((curDate.getFullYear() >= this.endLimit.getFullYear())
                || (curDate.getFullYear() == this.endLimit.getFullYear() - 1
                && curDate.getMonth() > this.endLimit.getMonth())) {
            nextYearLink.style.visibility = 'hidden';
        }
    }
}

/**
 * This function is used to assign a new date to a cell in the calendar and
 * clears up event handlers and styles on the cell. This function returns
 * the cell so the caller can make additional modifications if needed (like
 * class names or DOM properties).
 * @param cell                (Element) The DOM element that represents the cell
 * @param date                (Date) The date represented by the calendar cell
 * @return                    (Element) The DOM element passed into the function
 */
Fiz.DateFormElement.prototype.dateCell = function(cell, date, selectable)
{
    var self = this;
    cell.className = '';

    if (selectable) {
        // We use JavaScript closure to pass in
        // parameters to the onclick function
        cell.onclick = (function(date) {
            return function() {
                self.setSelectedDate(date);
                self.closePicker();
            };
        })(date);
        
        // Provides crossbweekser compatible highlighting of table cells
        cell.onmouseover = function() { 
            Fiz.DateFormElement.highlight(this, true);
        };
        cell.onmouseout = function() {
            Fiz.DateFormElement.highlight(this, false);
        };
    } else {
        // Clear all event handlers on the cell
        cell.onclick = null;
        cell.onmouseover = null;
        cell.onmouseout = null;
        Fiz.addClass(cell, 'excluded');
    }
    
    Fiz.setText(cell, date.getDate());

    return cell;
}

/**
 * This method is invoked to set the calendar display to date
 * @param date                    (Date) The date to display on the calendar
 */
Fiz.DateFormElement.prototype.setDisplayedDate = function(date)
{
    this.dDate.setMonth(date.getMonth());
    this.dDate.setFullYear(date.getFullYear());
}

/**
 * This method is invoked to set the calendar selection to date
 * @param date                    (Date) The date to select for the input field
 */
Fiz.DateFormElement.prototype.setSelectedDate = function(date)
{
    this.sDate = date;
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
    var today = new Date();
    this.dDate.setMonth(today.getMonth());
    this.dDate.setFullYear(today.getFullYear());
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
 * This method is used to setup the filters on the calendar used
 * to determine which dates should be selectable. It takes in a
 * list of filters and processes them to make them easier to work
 * with in JavaScript (such as converting date strings to objects).
 * @param filters                (Object) A JavaScript object containing a list
 *                               of filters, each specified by a filter type and
 *                               various parameters used by the filter
 */
Fiz.DateFormElement.prototype.setFilters = function(filters)
{
    this.filters = [];
    for (var filterIndex in filters.filter) {
        var filter = filters.filter[filterIndex];

        if (filter.type == 'date') {
            filter.date = Date.parse(filter.date);
        } else if (filter.type == 'annualDate') {
            filter.day = parseInt(filter.day);
            filter.month = parseInt(filter.month) - 1;
        } else if (filter.type == 'range') {

            var startDate = Date.parse(filter.startDate);
            var endDate = Date.parse(filter.endDate);

            if (startDate == null) {
                // We set the calendar to only display those months that
                // are not excluded. If we have overlapping ranges, we will
                // need to take the most exclusive range (the one with the
                // latest end date)
                if (this.startLimit == null
                        || Fiz.DateFormElement.compareDate(
                                endDate, this.startLimit) == 1) {
                    this.startLimit = endDate;
                }
            }

            if (endDate == null) {
                // We set the calendar to only display those months that
                // are not excluded. If we have overlapping ranges, we will
                // need to take the most exclusive range (the one with the
                // earliest start date)
                if (this.endLimit == null
                        || Fiz.DateFormElement.compareDate(
                                startDate, this.endLimit) == -1) {
                    this.endLimit = startDate;
                }
            }
            
            filter.startDate = startDate;
            filter.endDate = endDate;
        }
        
        this.filters.push(filter);
    }
}

/**
 * This method is used to determine whether a date has been excluded
 * from the calendar selection or not.
 * @param date                      (Date) The date to be tested for exclusion
 * @return                          (Boolean) true if date is not selectable,
 *                                  false otherwise
 */
Fiz.DateFormElement.prototype.isExcluded = function(date)
{
    var excluded = false;
    for (var filterIndex in this.filters) {
        var filter = this.filters[filterIndex];
        if ((filter.type == 'dayOfWeek' && date.getDay() == filter.dayOfWeek)
                || (filter.type == 'date' && Fiz.DateFormElement.compareDate(
                        date, filter.date) == 0)
                || (filter.type == 'annualDate'
                        && date.getMonth() == filter.month
                        && date.getDate() == filter.day)) {
            excluded = true
        } else if (filter.type == 'range') {
            // If an endpoint on a range is null, then we only look at the
            // other endpoint to determine if a date is within range. We
            // use the value 0 to ignore an endpoint.
            var startCompare = (filter.startDate == null ? 0 :
                    Fiz.DateFormElement.compareDate(date, filter.startDate));
            var endCompare = (filter.endDate == null ? 0 :
                    Fiz.DateFormElement.compareDate(date, filter.endDate));
            if ((startCompare == 1 || startCompare == 0)
                && (endCompare == -1 || endCompare == 0)) {
                excluded = true;
            }
        }
    }
    return excluded;
}

/**
 * This function is used to construct a date string with a user specified format.
 * @param date               (Date) The date to be converted
 * @param format             (String) The format of the outputted date
 * @return                   (String) A date string in the user specified format
 */
Fiz.DateFormElement.formatDate = function(date, format)
{
    if (!format) format = "m/d/Y";
    
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
 * @param elem                  (Element) The date cell to highlight
 * @param enable                (Boolean) true to highlight,
 *                              false to remove highlight
 */
Fiz.DateFormElement.highlight = function(elem, enable)
{
    if (enable) {
        Fiz.addClass(elem, 'hover');
    } else {
        Fiz.removeClass(elem, 'hover');
    }
}

/**
 * This function is used to check if two dates share the same calendar day.
 * @param dateA                    (Date) The first date to compare
 * @param dateB                    (Date) The second date to compare
 * @return                         (Integer) 0 if the two dates have the
 *                                 same month, day, and year,
 *                                 -1 if dateA is before dateB, and
 *                                 1 if dateB is before dateA
 */
Fiz.DateFormElement.compareDate = function(dateA, dateB)
{
    if (dateA.getDate() == dateB.getDate()
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
 * @param month                 (Integer) Month for which we are getting the
 *                              number of days (0 = January ... 11 = December)
 * @param year                  (Integer) Year for which we are getting the
 *                              number of days (4 digit format)
 * @return                      (Integer) The number of days for month and year
 */
Fiz.DateFormElement.getNumberOfDays = function(month, year)
{
    switch(month) {
        case 1:
            if (year % 400 == 0) {
                return 29;
            } else if (year % 100 == 0) {
                return 28;
            } else if (year % 4 == 0) {
                return 29;
            } else {
                return 28;
            }
        case 3: case 5: case 8: case 10: return 30;
        default: return 31; // All the other months of 31 days
    }
}

/**
 * Provides internationalization strings for changing the text values in
 * the calendar.
 */
Fiz.DateFormElement.i18n = {
    monthNamesLong: [ 'January', 'February', 'March', 'April',
    'May', 'June', 'July', 'August',
    'September', 'October', 'November', 'December' ],

    monthNamesShort: [ 'Jan', 'Feb', 'Mar', 'Apr',
    'May', 'Jun', 'Jul', 'Aug',
    'Sep', 'Oct', 'Nov', 'Dec' ],

    dayNamesLong: [ 'Sunday', 'Monday', 'Tuesday', 'Wednesday',
    'Thursday', 'Friday', 'Saturday' ],

    dayNamesMedium: [ 'Sun', 'Mon', 'Tue', 'Wed', 'Thur', 'Fri', 'Sat' ],

    dayNamesShort: [ 'S', 'M', 'Tu', 'W', 'Th', 'F', 'S' ]
};