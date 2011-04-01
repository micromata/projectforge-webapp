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

function autoIframe(frameId) {
	frame = document.getElementById(frameId);
	innerDoc = (frame.contentDocument) ? frame.contentDocument
			: frame.contentWindow.document;

	objToResize = (frame.style) ? frame.style : frame;
	objToResize.height = getDocHeight(innerDoc);
}