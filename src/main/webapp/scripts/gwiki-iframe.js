/*
 * disabled until the problem with dynamic document expansion is solved
 * 
	$(document).ready(function() {
		$("#gwiki-frame").iframeAutoHeight();
	});
 */
function updateScrolling() {
	document.body.style.overflow = "hidden !important";
}

function setCookie(name, value, expiration) {
	var expirationDate = new Date();
	expirationDate.setDate(expirationDate.getDate() + expiration);
	var value=escape(value) + ((expiration == null) ? "" : "; expires=" + expirationDate.toUTCString());
	document.cookie = name + "=" + value;
}

function setBookmark() {
	var bookmarkUrl = $('#bookmark').children().last().text();
	var src = $('#gwiki-frame').contents().get(0).location.href;
	var startIndex = src.indexOf('/gwiki/') + '/gwiki/'.length;
	var endIndex = src.indexOf(";jsessionid") == -1 ? src.length : src.indexOf(";jsessionid");
	var pageId = src.substring(startIndex, endIndex);

	if (bookmarkUrl.indexOf('?pageId=') != -1) {
		bookmarkUrl = bookmarkUrl.substring(0, bookmarkUrl.indexOf('?pageId='));
	}

	bookmarkUrl += '?pageId=' + pageId;

	$('#bookmark').children().last().text(bookmarkUrl);
	setCookie('frameSrc', pageId, 365);
}