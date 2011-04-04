function getDocHeight(doc) {
	var docHt = 0, sh, oh;
	if (doc.height) {
		docHt = doc.height;
	} else if (doc.body) {
		if (doc.body.scrollHeight)
			docHt = sh = doc.body.scrollHeight;
		if (doc.body.offsetHeight)
			docHt = oh = doc.body.offsetHeight;
		if (sh && oh)
			docHt = Math.max(sh, oh);
	}
	return docHt;
}

function autoIframe() {
	frame = document.getElementById('gwiki-frame');
	innerDoc = (frame.contentDocument) ? frame.contentDocument
			: frame.contentWindow.document;

	objToResize = (frame.style) ? frame.style : frame;
	objToResize.height = getDocHeight(innerDoc) + 30 + "px";
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
}