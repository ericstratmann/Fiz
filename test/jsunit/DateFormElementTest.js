// DateFormElementTest.js --
//
// Jsunit tests for DateFormElement.js, organized in the standard fashion.

include("static/fiz/Fiz.js");
include("static/fiz/DateFormElement.js");

DateFormElementTest = {};

/**
 * Tests the behavior of valid input in the text field
 */
DateFormElementTest.test_validateAndUpdate_validInput = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field", {value: "12/10/1999"});
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	formElem.validateAndUpdate();
	
//	assertEqual(11, formElem.sDate.getMonth(), "Selected Month");
//	assertEqual(10, formElem.sDate.getDate(), "Selected Day");
//	assertEqual(1999, formElem.sDate.getFullYear(), "Selected Year");
//
//	assertEqual(11, formElem.dDate.getMonth(), "Displayed Month");
//	assertEqual(1, formElem.dDate.getDate(), "Displayed Day");
//	assertEqual(1999, formElem.dDate.getFullYear(), "Displayed Year");
}

/**
 * Tests the behavior of invalid input in the text field.
 */
DateFormElementTest.test_validateAndUpdate_invalidInput = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field", {value: "12/099"});
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	
	var sDate_oldMonth = formElem.sDate.getMonth();
	var sDate_oldDay = formElem.sDate.getDate();
	var sDate_oldYear = formElem.sDate.getFullYear();
	
	var dDate_oldMonth = formElem.dDate.getMonth();
	var dDate_oldDay = formElem.dDate.getDate();
	var dDate_oldYear = formElem.dDate.getFullYear();

	formElem.validateAndUpdate();
	
//	assertEqual((sDate_oldMonth + 1) + "/" + sDate_oldDay + "/" + sDate_oldYear,
//			(formElem.sDate.getMonth() + 1) + "/" + formElem.sDate.getDate() + "/" + formElem.sDate.getFullYear(),
//			"Selected Date after input 12/099");
//
//	assertEqual((dDate_oldMonth + 1) + "/" + dDate_oldDay + "/" + dDate_oldYear,
//			(formElem.dDate.getMonth() + 1) + "/" + formElem.dDate.getDate() + "/" + formElem.dDate.getFullYear(),
//			"Displayed Date after input 12/099");
//
//	field.setAttribute("value", "4/29/399")
//	formElem.validateAndUpdate();
//	
//	assertEqual((sDate_oldMonth + 1) + "/" + sDate_oldDay + "/" + sDate_oldYear,
//			(formElem.sDate.getMonth() + 1) + "/" + formElem.sDate.getDate() + "/" + formElem.sDate.getFullYear(),
//			"Selected Date after input 4/29/399");
//
//	assertEqual((dDate_oldMonth + 1) + "/" + dDate_oldDay + "/" + dDate_oldYear,
//			(formElem.dDate.getMonth() + 1) + "/" + formElem.dDate.getDate() + "/" + formElem.dDate.getFullYear(),
//			"Displayed Date after input 4/29/399");
//
//	field.setAttribute("value", "433/29/20")
//	formElem.validateAndUpdate();
//	
//	assertEqual((sDate_oldMonth + 1) + "/" + sDate_oldDay + "/" + sDate_oldYear,
//			(formElem.sDate.getMonth() + 1) + "/" + formElem.sDate.getDate() + "/" + formElem.sDate.getFullYear(),
//			"Selected Date after input 433/29/20");
//
//	assertEqual((dDate_oldMonth + 1) + "/" + dDate_oldDay + "/" + dDate_oldYear,
//			(formElem.dDate.getMonth() + 1) + "/" + formElem.dDate.getDate() + "/" + formElem.dDate.getFullYear(),
//			"Displayed Date after input 433/29/20");
}

DateFormElementTest.test_openPicker = function() {
	var picker = document.addElementWithId("cal1_picker", {style: {display: "none"}});
	var parent = document.addElementWithId("parent", {offsetLeft: 0, offsetTop: 0});
	var field = document.addElementWithId("cal1_field", {offsetLeft: 10,
														 offsetTop: 100,
														 offsetHeight: 5,
														 offsetParent: parent,
														 className: "undefined"});
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.validateAndUpdate = logFunction("validateAndUpdate");
	formElem.openPicker();
	
	assertEqual("108px", picker.style.top, "Top of picker");
	assertEqual("10px", picker.style.left, "Left of picker");
	assertEqual("block", picker.style.display, "Visibility of picker");
}

DateFormElementTest.test_closePicker = function() {
	var picker = document.addElementWithId("cal1_picker", {style: {display: "block"}});
	var field = document.addElementWithId("cal1_field");
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.closePicker();

	assertEqual("none", picker.style.display, "Visibility of picker");
}

DateFormElementTest.test_redraw = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field");
	var header = document.addElementWithId("cal1_header");
	var grid = document.addElementWithId("cal1_grid");
	for(var i = 0; i < 6; i++) {
		var row = document.addElementWithId("row-" + (i + 1));
		for(var j = 0; j < 7; j++) {
			row.appendChild(document.addElementWithId("cell-" + (i + 1) + "-" + (j + 1)));
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
	formElem.sDate = new Date("August 11, 2008");
	formElem.dDate = new Date("August 1, 2008");
	formElem.redraw();

	assertEqual("August 2008", header.textContent, "Header");
	
	// Previous month
	assertEqual(27, document.getElementById("cell-1-1").textContent, "Row 1, Column 1");
	assertEqual("", document.getElementById("cell-1-1").className, "Row 1, Column 1: className");
	assertEqual(31, document.getElementById("cell-1-5").textContent, "Row 1, Column 5");

	// Current month
	assertEqual(1, document.getElementById("cell-1-6").textContent, "Row 1, Column 6");
	assertEqual(" cur-month", document.getElementById("cell-1-6").className, "Row 1, Column 6: className");
	assertEqual(11, document.getElementById("cell-3-2").textContent, "Row 3, Column 2");
	assertEqual(" cur-month cur-day", document.getElementById("cell-3-2").className, "Row 3, Column 2: className");
	assertEqual(31, document.getElementById("cell-6-1").textContent, "Row 6, Column 1");

	// Next month
	assertEqual(1, document.getElementById("cell-6-2").textContent, "Row 6, Column 2");
	assertEqual("", document.getElementById("cell-6-2").className, "Row 6, Column 2: className");
	assertEqual(6, document.getElementById("cell-6-7").textContent, "Row 6, Column 7");
}

DateFormElementTest.test_dateCell = function() {
	var cell = document.addElementWithId("cell", { onclick: function() {},
												   onmouseover: function() {},
												   onmouseout: function() {},
												   className: "",
												   textContent: "13" });
	var sameCell = Fiz.DateFormElement.dateCell(cell, 27);

	assertEqual(null, sameCell.onclick, "sameCell.onclick");
	assertEqual(null, sameCell.onmouseover, "sameCell.onmouseover");
	assertEqual(null, sameCell.onmouseout, "sameCell.onmouseout");
	assertEqual("", sameCell.className, "sameCell.className");
	assertEqual(27, sameCell.textContent, "sameCell.textContent");
}

DateFormElementTest.test_selectDate = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field");
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	formElem.selectDate(12, 3, 2004);
	
	assertEqual("12/3/2004",
			(formElem.sDate.getMonth() + 1) + "/" + formElem.sDate.getDate() + "/" + formElem.sDate.getFullYear(),
			"Selected Date");

	assertEqual("12/1/2004",
			(formElem.dDate.getMonth() + 1) + "/" + formElem.dDate.getDate() + "/" + formElem.dDate.getFullYear(),
			"Displayed Date");

	assertEqual("12/3/2004", field.value, "Value of input field");
}

DateFormElementTest.test_prevYear = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field");
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	
	var curYear = formElem.dDate.getFullYear();
	formElem.prevYear();

	assertEqual(curYear - 1, formElem.dDate.getFullYear(), "Previous year");
}

DateFormElementTest.test_prevMonth = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field");
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	
	var curMonth = formElem.dDate.getMonth();
	formElem.prevMonth();

	assertEqual(curMonth - 1, formElem.dDate.getMonth(), "Previous month");
}

DateFormElementTest.test_today = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field");
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	
	var curMonth = formElem.dDate.getMonth();
	formElem.nextMonth();

	assertEqual(curMonth + 1, formElem.dDate.getMonth(), "Next month");
}

DateFormElementTest.test_nextMonth = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field");
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	
	var curMonth = formElem.dDate.getMonth();
	formElem.nextMonth();

	assertEqual(curMonth + 1, formElem.dDate.getMonth(), "Next month");
}

DateFormElementTest.test_nextYear = function() {
	var picker = document.addElementWithId("cal1_picker");
	var field = document.addElementWithId("cal1_field");
	
	var formElem = new Fiz.DateFormElement("cal1");
	formElem.redraw = logFunction("redraw");
	
	var curYear = formElem.dDate.getFullYear();
	formElem.nextYear();

	assertEqual(curYear + 1, formElem.dDate.getFullYear(), "Next year");
}

DateFormElementTest.test_formatDate = function() {
	var date1 = new Date("October 13, 2008");
	var date2 = new Date("March 4, 2009");
	assertEqual("10,13,2008", Fiz.DateFormElement.formatDate(date1, "m,d,Y"), "Format m,d,Y on date1");
	assertEqual("20081310", Fiz.DateFormElement.formatDate(date1, "YdM"), "Format M,D,Y on date1");
	assertEqual("03/04/09", Fiz.DateFormElement.formatDate(date2, "M/D/y"), "Format M,D,Y on date2");
	assertEqual("3-09", Fiz.DateFormElement.formatDate(date2, "m-y"), "Format m-y on date2");
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
	
	assertEqual(-1, Fiz.DateFormElement.compareDate(dateA, dateB), "11/12/1999 < 12/13/2001");
	assertEqual(0, Fiz.DateFormElement.compareDate(dateA, dateD), "11/12/1999 = 11/12/1999");
	assertEqual(1, Fiz.DateFormElement.compareDate(dateB, dateC), "12/13/2001 > 5/24/1991");
}

DateFormElementTest.test_getNumberOfDays = function() {
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(1, 1983), "January 1983");
	assertEqual(28, Fiz.DateFormElement.getNumberOfDays(2, 1900), "February 1900");
	assertEqual(29, Fiz.DateFormElement.getNumberOfDays(2, 1920), "February 1920");
	assertEqual(29, Fiz.DateFormElement.getNumberOfDays(2, 2000), "February 2000");
	assertEqual(28, Fiz.DateFormElement.getNumberOfDays(2, 2003), "February 2003");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(3, 1442), "March 1442");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(4, 1103), "April 1103");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(5, 1952), "May 1952");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(6, 1642), "June 1642");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(7, 1999), "July 1999");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(8, 1784), "August 1784");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(9, 2039), "September 2039");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(10, 1542), "October 1542");
	assertEqual(30, Fiz.DateFormElement.getNumberOfDays(11, 1984), "November 1984");
	assertEqual(31, Fiz.DateFormElement.getNumberOfDays(12, 1943), "December 1943");
}