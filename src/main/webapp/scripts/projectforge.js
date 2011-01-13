function showConfirmDialog(text) {
	return window.confirm(text);
}

function toggle(component) {
	$(component).toggle('slow');
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
	// $(':*[title]').tooltip( {
	// track : true,
	// delay : 1000,
	// showURL : false,
	// opacity : 1,
	// showBody : " - ",
	// top : 5,
	// left : 5
	// });
}
function showBookmark() {
	$("#bookmark").toggle("normal");
}

var suppressRowClick = 'false';

// Begin: Functionality for DropDownMenu
var timeout = 500;
var closetimer = 0;
var ddmenuitem = 0;

// open hidden layer
function mopen(id) {
	// cancel close timer
	mcancelclosetime();

	// close old layer
	if (ddmenuitem)
		ddmenuitem.style.visibility = 'hidden';

	// get new layer and show it
	ddmenuitem = document.getElementById(id);
	ddmenuitem.style.visibility = 'visible';

}
// close showed layer
function mclose() {
	if (ddmenuitem)
		ddmenuitem.style.visibility = 'hidden';
}

// go close timer
function mclosetime() {
	closetimer = window.setTimeout(mclose, timeout);
}

// cancel close timer
function mcancelclosetime() {
	if (closetimer) {
		window.clearTimeout(closetimer);
		closetimer = null;
	}
}

// close layer when click-out
document.onclick = mclose;
// End: Functionality for DropDownMenu
