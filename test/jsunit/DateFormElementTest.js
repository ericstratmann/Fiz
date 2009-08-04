// DateFormElementTest.js --
//
// Jsunit tests for DateFormElement.js, organized in the standard fashion.
//
// Copyright (c) 2009 Stanford University
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
include("static/fiz/date.js");
include("static/fiz/DateFormElement.js");

DateFormElementTest = {};

DateFormElementTest.setUp = function() {
    this.header = document.addElementWithId("cal1_header");
    this.picker = document.addElementWithId("cal1_picker");
    this.input = document.addElementWithId("cal1");
    this.icon = document.addElementWithId("cal1_icon");
}

DateFormElementTest.test_validateAndUpdate_validInput = function() {
    this.input.value = "12/10/1999";
    
	var formElem = new Fiz.DateFormElement("cal1");

	formElem.validateAndUpdate();
	assertEqual("12/10/1999", formElem.input.value, "Value of input: 12/10/1999");

	formElem.input.value = "January 1, 1979";
	formElem.validateAndUpdate();
	assertEqual("1/1/1979", formElem.input.value, "Value of input: January 1, 1979");
}

DateFormElementTest.test_validateAndUpdate_invalidInput = function() {
    this.input.value = "12/099";

	var formElem = new Fiz.DateFormElement("cal1");

	formElem.validateAndUpdate();

	assertEqual("12/099", formElem.input.value, "Value of input: 12/099");

	formElem.input.value = "Octust 35, 1999";
	formElem.validateAndUpdate();

	assertEqual("Octust 35, 1999", formElem.input.value, "Value of input: Octust 35, 1999");
}

DateFormElementTest.test_validateAndDisplay_validInput = function() {
    this.input.value = "3/29/2008";

	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");

	formElem.validateAndDisplay();

	assertEqual(2, formElem.dDate.getMonth(),
			"Displayed Date: Month");
	assertEqual(1, formElem.dDate.getDate(),
			"Displayed Date: Day");
	assertEqual(2008, formElem.dDate.getFullYear(),
			"Displayed Date: Year");

	assertEqual(2, formElem.sDate.getMonth(),
			"Selected Date: Month");
	assertEqual(29, formElem.sDate.getDate(),
			"Selected Date: Day");
	assertEqual(2008, formElem.sDate.getFullYear(),
			"Selected Date: Year");
}

DateFormElementTest.test_validateAndDisplay_invalidInput = function() {
    this.input.value = "12/099";

	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");

	var dDate_oldMonth = formElem.dDate.getMonth();
	var dDate_oldYear = formElem.dDate.getFullYear();

	var sDate_oldMonth = formElem.sDate.getMonth();
	var sDate_oldDay = formElem.sDate.getDate();
	var sDate_oldYear = formElem.sDate.getFullYear();

	formElem.validateAndDisplay();

	assertEqual(dDate_oldMonth, formElem.dDate.getMonth(),
			"Displayed Date: Month");
	assertEqual(1, formElem.dDate.getDate(),
			"Displayed Date: Day");
	assertEqual(dDate_oldYear, formElem.dDate.getFullYear(),
			"Displayed Date: Year");

	assertEqual(sDate_oldMonth, formElem.sDate.getMonth(),
			"Selected Date: Month");
	assertEqual(sDate_oldDay, formElem.sDate.getDate(),
			"Selected Date: Day");
	assertEqual(sDate_oldYear, formElem.sDate.getFullYear(),
			"Selected Date: Year");
}

DateFormElementTest.test_togglePicker = function() {
    var formElem = new Fiz.DateFormElement("cal1");

    formElem.togglePicker();
    assertEqual("block", formElem.picker.style.display,
    "Closed -> Open");

    formElem.togglePicker();
    assertEqual("none", formElem.picker.style.display,
    "Open -> Closed");
}

DateFormElementTest.test_openPicker = function() {
    var parent = document.addElementWithId("parent", {offsetLeft: 0, offsetTop: 0});

    this.picker.style = {display: "none"};
	this.input.setAttributes({offsetLeft: 10,
	        offsetTop: 100,
	        offsetWidth: 8,
	        offsetHeight: 5,
	        offsetParent: parent,
	        className: "undefined"});

	var formElem = new Fiz.DateFormElement("cal1", "", "right");
	formElem.validateAndUpdate = logFunction("validateAndUpdate");
	formElem.openPicker();

	assertEqual("0px", formElem.picker.style.top,
			"attachRight: Top of picker");
	assertEqual("11px", formElem.picker.style.left,
			"attachRight: Left of picker");
	assertEqual("block", formElem.picker.style.display,
			"attachRight: Visibility of picker");

	formElem.attachPosition = "bottom";
	formElem.openPicker();

	assertEqual("8px", formElem.picker.style.top,
			"attachBottom: Top of picker");
	assertEqual("0px", formElem.picker.style.left,
			"attachBottom: Left of picker");
	assertEqual("block", formElem.picker.style.display,
			"attachBottom: Visibility of picker");
}

DateFormElementTest.test_closePicker = function() {
    this.picker.style = {display: "block"};

	var formElem = new Fiz.DateFormElement("cal1");
	formElem.closePicker();

	assertEqual("none", formElem.picker.style.display, "Visibility of picker");
}

DateFormElementTest.test_redraw_basic = function() {
	var header = document.addElementWithId("cal1_header");
	var grid = document.addElementWithId("cal1_grid");
	for(var i = 0; i < 6; i++) {
		var row = document.addElementWithId("row-" + (i + 1), {tagName: "tr"});
		for(var j = 0; j < 7; j++) {
			row.appendChild(document.addElementWithId(
			        "cell-" + (i + 1) + "-" + (j + 1),  {tagName: "td", textContent: ""}));
		}
		grid.appendChild(row);
		row.__defineGetter__("childNodes", function() {
			return this.getChildNodes();
		});
	}
	grid.__defineGetter__("childNodes", function() {
		return this.getChildNodes();
	});

	var formElem = new Fiz.DateFormElement("cal1");
	formElem.sDate = new Date("August 13, 2007");
	formElem.dDate = new Date("August 1, 2007");
	formElem.redrawNav = logFunction("redrawNav");
	formElem.redraw();

	assertEqual("August 2007", header.textContent, "Header");

	// Previous month
	assertEqual(29, document.getElementById("cell-1-1").textContent,
			"Row 1, Column 1");
	assertEqual("excluded", document.getElementById("cell-1-1").className,
			"Row 1, Column 1: className");
	assertEqual(31, document.getElementById("cell-1-3").textContent,
			"Row 1, Column 3");

	// Current month
	assertEqual(1, document.getElementById("cell-1-4").textContent,
			"Row 1, Column 4");
	assertEqual("cur-month", document.getElementById("cell-1-4").className,
			"Row 1, Column 4: className");
	assertEqual(13, document.getElementById("cell-3-2").textContent,
			"Row 3, Column 2");
	assertEqual("cur-month cur-day", document.getElementById("cell-3-2").className,
			"Row 3, Column 2: className");
	assertEqual(31, document.getElementById("cell-5-6").textContent,
			"Row 5, Column 6");

	// Next month
	assertEqual(1, document.getElementById("cell-5-7").textContent,
			"Row 5, Column 7");
	assertEqual("excluded", document.getElementById("cell-5-7").className,
			"Row 5, Column 7: className");
	assertEqual(8, document.getElementById("cell-6-7").textContent,
			"Row 6, Column 7");
}

DateFormElementTest.test_redrawNav_twoMonthRange = function() {
	var prevYearLink = document.addElementWithId("cal1_prevYear");
	var prevMonthLink = document.addElementWithId("cal1_prevMonth");
	var nextMonthLink = document.addElementWithId("cal1_nextMonth");
	var nextYearLink = document.addElementWithId("cal1_nextYear");

	var formElem = new Fiz.DateFormElement("cal1");

	formElem.startLimit = new Date("May 14, 2008");
	formElem.endLimit = new Date("July 14, 2008");

	formElem.redrawNav(new Date("May 23, 2008"),
			new Date("June 15, 2008"), new Date("July 23, 2008"));

	assertEqual("hidden", prevYearLink.style.visibility,
			"Prev Year Link: startLimit = 5/14/2008, curdate = 6/15/2008");
	assertEqual("visible", prevMonthLink.style.visibility,
			"Prev Month Link: startLimit = 5/14/2008, curdate = 6/15/2008");
	assertEqual("visible", prevMonthLink.style.visibility,
			"Next Month Link: endLimit = 7/14/2008, curdate = 6/15/2008");
	assertEqual("hidden", prevYearLink.style.visibility,
			"Next Year Link: endLimit = 7/14/2008, curdate = 6/15/2008");
}

DateFormElementTest.test_redrawNav_twoYearRange = function() {
    var prevYearLink = document.addElementWithId("cal1_prevYear");
    var prevMonthLink = document.addElementWithId("cal1_prevMonth");
    var nextMonthLink = document.addElementWithId("cal1_nextMonth");
    var nextYearLink = document.addElementWithId("cal1_nextYear");
    
    var formElem = new Fiz.DateFormElement("cal1");
    
    formElem.startLimit = new Date("June 14, 2007");
    formElem.endLimit = new Date("June 14, 2009");
    
    formElem.redrawNav(new Date("May 23, 2008"),
            new Date("June 15, 2008"), new Date("July 23, 2008"));
            
    assertEqual("visible", prevYearLink.style.visibility,
            "Prev Year Link: startLimit = 6/14/2007, curdate = 6/15/2008");
    assertEqual("visible", prevMonthLink.style.visibility,
            "Prev Month Link: startLimit = 6/14/2007, curdate = 6/15/2008");
    assertEqual("visible", prevMonthLink.style.visibility,
            "Next Month Link: endLimit = 6/14/2009, curdate = 6/15/2008");
    assertEqual("visible", prevYearLink.style.visibility,
            "Next Year Link: endLimit = 6/14/2009, curdate = 6/15/2008");
}

DateFormElementTest.test_redrawNav_oneYearTenMonthRange = function() {
    var prevYearLink = document.addElementWithId("cal1_prevYear");
    var prevMonthLink = document.addElementWithId("cal1_prevMonth");
    var nextMonthLink = document.addElementWithId("cal1_nextMonth");
    var nextYearLink = document.addElementWithId("cal1_nextYear");
    
    var formElem = new Fiz.DateFormElement("cal1");
    
    formElem.startLimit = new Date("July 14, 2007");
    formElem.endLimit = new Date("May 14, 2009");
    
    formElem.redrawNav(new Date("May 23, 2008"),
            new Date("June 15, 2008"), new Date("July 23, 2008"));
            
    assertEqual("hidden", prevYearLink.style.visibility,
            "Prev Year Link: startLimit = 7/14/2007, curdate = 6/15/2008");
    assertEqual("visible", prevMonthLink.style.visibility,
            "Prev Month Link: startLimit = 7/14/2007, curdate = 6/15/2008");
    assertEqual("visible", prevMonthLink.style.visibility,
            "Next Month Link: endLimit = 5/14/2009, curdate = 6/15/2008");
    assertEqual("hidden", prevYearLink.style.visibility,
            "Next Year Link: endLimit = 5/14/2009, curdate = 6/15/2008");
}

DateFormElementTest.test_dateCell = function() {
	var formElem = new Fiz.DateFormElement("cal1");

	var cell = document.addElementWithId("cell", { onclick: function() {},
												   onmouseover: function() {},
												   onmouseout: function() {},
												   className: "",
												   textContent: "13" });

	var date = new Date(2008, 10, 1);
	var sameCell = formElem.dateCell(cell, date, true);

	assertEqual(true, sameCell.onclick != null, "Selectable: onclick");
	assertEqual(true, sameCell.onmouseover != null, "Selectable: onmouseover");
	assertEqual(true, sameCell.onmouseout != null, "Selectable: onmouseout");
	assertEqual("", sameCell.className, "Selectable: className");
	assertEqual(1, sameCell.textContent, "Selectable: textContent");

	var date = new Date(2009, 5, 4);
	var sameCell = formElem.dateCell(cell, date, false);

	assertEqual(null, sameCell.onclick, "Not Selectable: onclick");
	assertEqual(null, sameCell.onmouseover, "Not Selectable: onmouseover");
	assertEqual(null, sameCell.onmouseout, "Not Selectable: onmouseout");
	assertEqual("excluded", sameCell.className, "Not Selectable: className");
	assertEqual(4, sameCell.textContent, "Not Selectable: textContent");
}

DateFormElementTest.test_setDisplayedDate = function() {
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.setDisplayedDate(new Date("December 3, 2004"));

	assertEqual("12/1/2004",
			(formElem.dDate.getMonth() + 1) + "/" + formElem.dDate.getDate() 
			        + "/" + formElem.dDate.getFullYear(), "Displayed Date");
}

DateFormElementTest.test_setSelectedDate = function() {
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.setSelectedDate(new Date("December 3, 2004"));

	assertEqual("12/3/2004",
			(formElem.sDate.getMonth() + 1) + "/" + formElem.sDate.getDate() 
			        + "/" + formElem.sDate.getFullYear(), "Selected Date");
}

DateFormElementTest.test_prevYear = function() {
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");

	var curYear = formElem.dDate.getFullYear();
	formElem.prevYear();

	assertEqual(curYear - 1, formElem.dDate.getFullYear(), "Previous year");
}

DateFormElementTest.test_prevMonth = function() {
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");

	var curMonth = formElem.dDate.getMonth();
	formElem.prevMonth();

	assertEqual(curMonth - 1, formElem.dDate.getMonth(), "Previous month");
}

DateFormElementTest.test_today = function() {
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");

	var curMonth = formElem.dDate.getMonth();
	formElem.nextMonth();

	assertEqual(curMonth + 1, formElem.dDate.getMonth(), "Next month");
}

DateFormElementTest.test_nextMonth = function() {
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");

	var curMonth = formElem.dDate.getMonth();
	formElem.nextMonth();

	assertEqual(curMonth + 1, formElem.dDate.getMonth(), "Next month");
}

DateFormElementTest.test_nextYear = function() {
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");

	var curYear = formElem.dDate.getFullYear();
	formElem.nextYear();

	assertEqual(curYear + 1, formElem.dDate.getFullYear(), "Next year");
}

DateFormElementTest.test_setFilters = function() {
	var formElem = new Fiz.DateFormElement("cal1");

	formElem.setFilters(
			{filter:
				[
				 {type: "dayOfWeek", dayOfWeek: "6"},
				 {type: "date", date: "January 12, 2005"},
				 {type: "range", startDate: "null", endDate: "June 24, 2009"},
				 {type: "range", startDate: "August 14, 2010", endDate: "September 13, 2010"},
				 {type: "range", startDate: "September 5, 2011", endDate: "null"}
				]
			}
	);

	assertEqual("6", formElem.filters[0].dayOfWeek,
			"Day of week filter: Day of week");
	assertEqual(new Date(2005, 0, 12).toString(), formElem.filters[1].date.toString(),
			"Date filter: Date");
	assertEqual(null, formElem.filters[2].startDate,
			"Back date range filter: Start date");
	assertEqual(new Date(2009, 5, 24).toString(), formElem.filters[2].endDate.toString(),
			"Back date range filter: End date");
	assertEqual(new Date(2010, 7, 14).toString(), formElem.filters[3].startDate.toString(),
			"Date range filter: Start date");
	assertEqual(new Date(2010, 8, 13).toString(), formElem.filters[3].endDate.toString(),
			"Date range filter: End date");
	assertEqual(new Date(2011, 8, 5).toString(), formElem.filters[4].startDate.toString(),
			"Forward date range filter: Start date");
	assertEqual(null, formElem.filters[4].endDate,
			"Forward date range filter: End date");

	assertEqual(new Date(2009, 5, 24).toString(), formElem.startLimit.toString(),
			"Start Limit");
	assertEqual(new Date(2011, 8, 5).toString(), formElem.endLimit.toString(),
			"End Limit");

}

DateFormElementTest.test_isExcluded_dayOfWeek = function() {
	var formElem = new Fiz.DateFormElement("cal1");

	formElem.setFilters({filter:[{type: "dayOfWeek", dayOfWeek: "6"}]});

	assertEqual(true, formElem.isExcluded(new Date("January 1, 2011")),
			"January 1, 2011");
}

DateFormElementTest.test_isExcluded_specificDate = function() {
	var formElem = new Fiz.DateFormElement("cal1");

	formElem.setFilters({filter:[{type: "date", date: "January 12, 2011"}]});

	assertEqual(true, formElem.isExcluded(new Date("January 12, 2011")),
			"January 12, 2011");
}

DateFormElementTest.test_isExcluded_backDateRange = function() {
	var formElem = new Fiz.DateFormElement("cal1");

	formElem.setFilters(
			{filter:
				[{type: "range", startDate: "null", endDate: "June 24, 2009"}]
			}
	);

	assertEqual(true, formElem.isExcluded(new Date("June 23, 2009")),
			"June 23, 2009");
	assertEqual(true, formElem.isExcluded(new Date("June 24, 2009")),
			"June 24, 2009");
	assertEqual(false, formElem.isExcluded(new Date("June 25, 2009")),
			"June 25, 2009");
}

DateFormElementTest.test_isExcluded_dateRange = function() {
	var formElem = new Fiz.DateFormElement("cal1");

	formElem.setFilters(
			{filter:
				[
				 {type: "range", startDate: "August 18, 2010", endDate: "September 14, 2010"},
				]
			}
	);

	assertEqual(false, formElem.isExcluded(new Date("August 17, 2010")),
			"August 17, 2010");
	assertEqual(true, formElem.isExcluded(new Date("August 18, 2010")),
			"August 18, 2010");
	assertEqual(true, formElem.isExcluded(new Date("August 19, 2010")),
			"August 19, 2010");
	assertEqual(true, formElem.isExcluded(new Date("September 13, 2010")),
			"September 13, 2010");
	assertEqual(true, formElem.isExcluded(new Date("September 14, 2010")),
			"September 14, 2010");
	assertEqual(false, formElem.isExcluded(new Date("September 15, 2010")),
			"September 15, 2010");
}

DateFormElementTest.test_isExcluded_forwardDateRange = function() {
	var formElem = new Fiz.DateFormElement("cal1");

	formElem.setFilters(
			{filter:
				[
				 {type: "range", startDate: "September 5, 2011", endDate: "null"}
				]
			}
	);

	assertEqual(false, formElem.isExcluded(new Date("September 4, 2011")),
			"September 4, 2011");
	assertEqual(true, formElem.isExcluded(new Date("September 5, 2011")),
			"September 5, 2011");
	assertEqual(true, formElem.isExcluded(new Date("September 6, 2011")),
			"September 6, 2011");
}

DateFormElementTest.test_formatDate = function() {
	var date1 = new Date("October 13, 2008");
	var date2 = new Date("March 4, 2009");
	assertEqual("10,13,2008", Fiz.DateFormElement.formatDate(date1, "m,d,Y"),
			"Format m,d,Y on date1");
	assertEqual("20081310", Fiz.DateFormElement.formatDate(date1, "YdM"),
			"Format M,D,Y on date1");
	assertEqual("03/04/09", Fiz.DateFormElement.formatDate(date2, "M/D/y"),
			"Format M,D,Y on date2");
	assertEqual("3-09", Fiz.DateFormElement.formatDate(date2, "m-y"),
			"Format m-y on date2");
}

DateFormElementTest.test_highlight_true = function() {
	var elem = document.addElementWithId("elem", { className: "undefined" });
	Fiz.DateFormElement.highlight(elem, true);

	assertEqual("hover", elem.className.match("hover")[0], "Hover on");
}

DateFormElementTest.test_highlight_false = function() {
	var elem = document.addElementWithId("elem", { className: "undefined" });
	Fiz.DateFormElement.highlight(elem, false);

	assertEqual(null, elem.className.match("hover"), "Hover off");
}

DateFormElementTest.test_compareDate = function() {
	var dateA = new Date("October 12, 1999");
	var dateB = new Date("December 13, 2001");
	var dateC = new Date(91, 04, 24); // May 24, 1991
	var dateD = new Date(99, 09, 12); // October 12, 1999

	assertEqual(-1, Fiz.DateFormElement.compareDate(dateA, dateB),
			"11/12/1999 < 12/13/2001");
	assertEqual(0, Fiz.DateFormElement.compareDate(dateA, dateD),
			"11/12/1999 = 11/12/1999");
	assertEqual(1, Fiz.DateFormElement.compareDate(dateB, dateC),
			"12/13/2001 > 5/24/1991");
}

DateFormElementTest.test_getNumberOfDays = function() {
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(0, 1983), "January 1983");
	assertEqual(28, Fiz.DateFormElement.getNumberOfDays(1, 1900), "February 1900");
	assertEqual(29, Fiz.DateFormElement.getNumberOfDays(1, 1920), "February 1920");
	assertEqual(29, Fiz.DateFormElement.getNumberOfDays(1, 2000), "February 2000");
	assertEqual(28, Fiz.DateFormElement.getNumberOfDays(1, 2003), "February 2003");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(2, 1442), "March 1442");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(3, 1103), "April 1103");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(4, 1952), "May 1952");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(5, 1642), "June 1642");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(6, 1999), "July 1999");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(7, 1784), "August 1784");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(8, 2039), "September 2039");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(9, 1542), "October 1542");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(10, 1984), "November 1984");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(11, 1943), "December 1943");
}