$(function() {
	// hier verknüpfe ich die listen und konfiguriere die sortable-funktion.
	$("#normal #nav ul li").draggable( {
		connectToSortable : "#personal",
		placeholder : "ui-state-highlight",
		helper : 'clone'
	}).disableSelection();

	$("#personal").sortable( {
		placeholder : "ui-state-highlight"
	}).disableSelection();

	// @kai: Das ist neu, beim Laden muss man natürlich das Sortable disablen...
	$("#personal, #nav ul").sortable("disable");

	// @kai: dies aktiviert die remover
	$('.remover').live('click', function() {
		$(this).parents("li").remove();
		return false;
	});

});
