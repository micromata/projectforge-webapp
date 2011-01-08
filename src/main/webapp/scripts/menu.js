$(function() {
	// hier verknüpfe ich die listen und konfiguriere die sortable-funktion.
	$("#normal #nav ul li").draggable( {
		connectToSortable : "#personal",
		placeholder : "ui-state-highlight",
		helper : 'clone'
	}).disableSelection();

	$("#personal").sortable( {
		placeholder : "ui-state-highlight",
		change: function(event, ui) {
			$(".remover").remove();
			$("ul#personal li a").prepend( '<span class="remover"> 2 </span>');
			// @kai: call a function here to serialize and savePersonal();
		}
	}).disableSelection();
	
	

	// @kai: Das ist neu, beim Laden muss man natürlich das Sortable disablen...
	$("#personal, #nav ul").sortable("disable");

	// @kai: dies aktiviert die remover
	$('.remover').live('click', function() {
		$(this).parents("li").remove();
		return false;
	});

});
