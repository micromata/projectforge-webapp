function showConfirmDialog(text) {
	return window.confirm(text);
}

function rowClick(row) {
	if (suppressRowClick != 'true') {
		window.location.href = $(row).find("a:first").attr("href");
	}
	suppressRowClick = 'false';
}
function rowCheckboxClick(row) {
	cb = $(row).find("input[type='checkbox']");
	cb.attr('checked', !cb.is(':checked'));
}
function suppressNextRowClick() {
	suppressRowClick = 'true';
}
function preventBubble(e) {
	if (!e)
		var e = window.event;
	if (e.stopPropagation) { // if stopPropagation method supported
		e.stopPropagation();
		e.preventDefault();
	} else {
		e.cancelBubble = true;
		e.returnValue = false;
	}
	return false;
}
function initTooltips() {
	// Enable all title attributes as tooltip:
//	$(':*[title]').tooltip( {
//		track : true,
//		delay : 1000,
//		showURL : false,
//		opacity : 1,
//		showBody : " - ",
//		top : 5,
//		left : 5
//	});
}
function showBookmark() {
	$("#bookmark").toggle("normal");
}




var suppressRowClick = 'false';

