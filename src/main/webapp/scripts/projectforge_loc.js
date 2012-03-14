// Localization of java scripts

$(function() {
	$.datepicker.regional['de'] = { // Default regional settings
		closeText : 'Fertig', // Display text for close link
		prevText : 'Zurück', // Display text for previous month link
		nextText : 'Weiter', // Display text for next month link
		currentText : 'Heute', // Display text for current month link
		monthNames : [ 'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
				'Juli', 'August', 'September', 'Oktober', 'November',
				'Dezember' ], // Names of months for drop-down and formatting
		monthNamesShort : [ 'Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul',
				'Aug', 'Sep', 'Okt', 'Nov', 'Dez' ], // For formatting
		dayNames : [ 'Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag',
				'Freitag', 'Samstag' ], // For formatting
		dayNamesShort : [ 'Son', 'Mon', 'Die', 'Mit', 'Don', 'Fre', 'Sam' ], // For
		// formatting
		dayNamesMin : [ 'So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa' ], // Column
		// headings
		// for days
		// starting
		// at Sunday
		weekHeader : 'Wo', // Column header for week of the year
		dateFormat : 'dd.mm.yy', // See format options on parseDate
		firstDay : 1, // The first day of the week, Sun = 0, Mon = 1, ...
		isRTL : false, // True if right-to-left language, false if
		// left-to-right
		showMonthAfterYear : false, // True if the year select precedes month,
		// false for month then year
		yearSuffix : '' // Additional text to append to the year in the month
	// headers
	};
})
