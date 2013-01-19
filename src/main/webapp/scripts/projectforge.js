var mouseX, mouseY;

function showConfirmDialog(text) {
	return window.confirm(text);
}

function toggle(component) {
	$(component).toggle('fast');
}

function rowClick(row) {
	if (suppressRowClick != 'true') {
		link = $(row).find("a:first");
		if ($(link).attr('onclick')) {
			suppressNextRowClick();
			$(link).click();
		} else {
			window.location.href = $(link).attr("href");
		}
	}
	suppressRowClick = 'false';
}

function rowCheckboxClick(row, event) {
	if (!event)
		event = window.event; // For ie.
	var t = event.target || event.srcElement;
	if (t.type != "checkbox") { /*
								 * disables tableRowClickFunction if you are
								 * over the checkbox
								 */
		cb = $(row).find("input[type='checkbox']");
		cb.attr('checked', !cb.is(':checked'));
	}
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
function initializeComponents() {
	// Enable tool-tips, button sets etc.
	$('[title]').tooltip({
		track : true
	}).on("focusin", function() {
		$(this).tooltip("close");
	});
	$("div.radio-jquery-ui").buttonset();
	if ($("textarea.autogrow").length) {
		$("textarea.autogrow").autoGrow();
	}
	$('[rel=\'popup-tooltip\']').hover(function(event) {
		mouseX = event.pageX;
		mouseY = event.pageY;
		$(this).mypopover('myshow');
	}, function() {
		$(this).mypopover('hide');
	});

}

// ///////////////////////////////////
//
// BOOTSTRAP based popover
//
// ///////////////////////////////////
var MyPopover = function(element, options) {
	this.init('mypopover', element, options)
}

MyPopover.prototype = $
		.extend(
				$.fn.popover.Constructor.prototype,
				{

					constructor : MyPopover

					,
					myshow : function() {
						var $tip, inside, pos, tipWidth, tipHeight, posX, posY, boundTop, boundBottom, boundLeft, boundRight, tp
						if (this.hasContent() && this.enabled) {
							$tip = this.tip();
							this.setContent();

							if (this.options.animation) {
								$tip.addClass('fade');
							}
							$tip.detach().css({
								top : 0,
								left : 0,
								display : 'block'
							}).insertAfter(this.$element);
							if (this.getContent().length > 100) {
								$tip.addClass('big');
							}
							pos = this.getPosition(inside);
							boundTop = $(document).scrollTop();
							boundLeft = $(document).scrollLeft();
							boundRight = boundLeft + $(window).width();
							boundBottom = boundTop + $(window).height();
							tipWidth = $tip[0].offsetWidth
							tipHeight = $tip[0].offsetHeight
							if (mouseX + tipWidth + 5 < boundRight
									|| mouseX - tipWidth - 5 < boundLeft) {
								// Bottom, if enough space at bottom of mouse
								// position or not enough space above mouse
								// position:
								posX = mouseX + 5;
							} else {
								// Position is in top of mouse position:
								posX = mouseX - tipWidth - 5;
							}
							if (mouseY + tipHeight + 5 < boundBottom
									|| mouseY - tipHeight - 5 < boundTop) {
								// Right if enough space right of mouse position
								// or not enough space left of mouse position:
								posY = mouseY + 5;
							} else {
								// Position is left of mouse position:
								posY = mouseY - tipHeight - 5;
							}
							tp = {
								top : posY,
								left : posX
							}
							$tip.offset(tp).addClass('in')
						}
					}
				})

$.fn.mypopover = function(option) {
	return this
			.each(function() {
				var $this = $(this), data = $this.data('mypopover'), options = typeof option == 'object'
						&& option
				if (!data)
					$this.data('mypopover',
							(data = new MyPopover(this, options)))
				if (typeof option == 'string')
					data[option]()
			})
}

$.fn.mypopover.Constructor = MyPopover

$.fn.mypopover.defaults = $
		.extend(
				{},
				$.fn.tooltip.defaults,
				{
					width : 'normal',
					trigger : 'manual',
					template : '<div class="popover"><div class="arrow"></div><div class="popover-inner"><h3 class="popover-title"></h3><div class="popover-content"></div></div></div>'
				})

// ///////////////////////////////////
//
// END BOOTSTRAP based popover
//
// ///////////////////////////////////

/*
 * Only used if the ToggleContainer works without Ajax (wantOnToggleNotification =
 * false)
 */
function toggleCollapseIcon(icon, iconStatusOpened, iconOpened, iconClosed) {
	if ($(icon).hasClass(iconStatusOpened)) {
		$(icon).removeClass().addClass(iconClosed);
	} else {
		$(icon).removeClass().addClass(iconOpened);
	}
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

function openDialog(element, closeScript) {
	$('#' + element).dialog({
		'resizable' : false,
		'draggable' : false,
		'width' : 'auto',
		'height' : 'auto',
		'position' : 'center',
		'modal' : true,
		close : closeScript
	}).dialog('open');
}

$(function() {
	$(".dialog_content").live("dialogopen", function(event, ui) {
		disableScroll();
	});
	$(".dialog_content").live("dialogclose", function(event, ui) {
		enableScroll();
	});

	initColorPicker();
	doAfterAjaxHandling();

	if (typeof (Wicket) != "undefined" && typeof (Wicket.Event) != "undefined") {
		Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent,
				attributes, jqXHR, errorThrown, textStatus) {
			// handle after AJAX actions
			doAfterAjaxHandling();
		});
	}
	$('.pf_preventClickBubble').on("contextmenu", function(e) {
		e.stopImmediatePropagation();
	});
	$('.pf_preventClickBubble').click(function(e) {
		e.stopImmediatePropagation();
	});
});

function doAfterAjaxHandling() {
	var $uploadProxy = $('.pf_uploadField button[name="fileUploadProxy"], .pf_uploadField .label');
	$uploadProxy.unbind('click').click(function(e) {
		$(this).siblings('input[type="file"]').click();
		e.preventDefault();
	}).siblings('input[type="file"]').change(function(e) {
		$(this).siblings('.label').val(/([^\\\/]+)$/.exec(this.value)[1]); // Extract
		// the
		// filename
		$(this).siblings('.label').change();
	});
	$("fieldset > div > input[type=checkbox]").addClass("checkbox");
	$(".jqui_checkbox").buttonset();
	initializeComponents();
}

function initColorPicker() {
	$('.pf_colorPreview').live('click', function() {
		$(this).siblings('.pf_colorForm').find('.pf_colorPickerField').click();
	});
}

function disableScroll() {
	var before = $(document).width();
	$("html").css("overflow", "hidden");
	var after = $(document).width();
	$("body").css("padding-right", after - before);
}

function enableScroll() {
	$("html").css("overflow", "auto");
	$("body").css("padding-right", 0);
}

function pf_deleteClick(element, content, liElement) {
	var callback = $(element).data("callback");
	callback = callback + "&delete=" + content;
	var wcal = $.get(callback);
	if (wcal != null) {
		var li = $(liElement).parents('li');
		$(li).data("me").flushCache();
		$(li).data("me").clearHideTimeout();
		$(li).remove();
	}
}

/**
 * Drag and drop inspired by http://www.sitepoint.com/html5-file-drag-and-drop,
 * <br/> but was mixed and enhanced with jQuery and the HTML5 file API by
 * Johannes.
 */
(function() {

	$(function() {
		// call initialization file only if API is available
		if (window.File && window.FileList && window.FileReader) {
			initDragAndDrop();
		}
	});

	// initialize drag and drop
	function initDragAndDrop() {
		var fileselect = $(".pf_dnd .pf_fileselect");
		var filedrag = $(".pf_dnd .pf_filedrag");
		// file select
		$(fileselect).on("change", fileSelectHandler);
		try {
			// is XHR2 available?
			var xhr = new XMLHttpRequest();
			if (xhr.upload) {
				// file drop
				$(filedrag).on("dragover", fileDragHover);
				$(filedrag).on("dragleave", fileDragHover);
				$(filedrag).on("drop", fileSelectHandler);
				$(filedrag).show();
				$(fileselect).hide();
			}
		} catch (e) { /* just do nothing, no XHR2 available */
		}
		;
	}

	// file drag hover
	function fileDragHover(e) {
		e.stopPropagation();
		e.preventDefault();
		if (e.type == "dragover") {
			$(e.target).addClass("hover");
		} else {
			$(e.target).removeClass("hover");
		}
	}

	// file selection
	function fileSelectHandler(e) {
		// cancel event and hover styling
		fileDragHover(e);
		// fetch file object
		var files = e.originalEvent.target.files
				|| e.originalEvent.dataTransfer.files;
		if (files == null || files.length != 1) {
			// TODO ju: error handling
			return;
		}
		var file = files[0];
		if (file == null || file.size > 204800 || file.type != "text/calendar") {
			/* 200 kbyte max */
			// TODO ju: error handling
			return;
		}
		try {
			var reader = new FileReader();
			reader.onload = function(event) {
				var result = event.target.result;
				var hiddenForm = $(e.originalEvent.target).closest(".pf_dnd")
						.children(".pf_hiddenForm");
				hiddenForm.children(".pf_text").val(result);
				hiddenForm.children(".pf_submit").click();
			}
			reader.readAsText(file);
		} catch (e) {
			// TODO ju: error handling
		}
	}
})();
