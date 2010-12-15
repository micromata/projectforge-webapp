$(function() {
  // hier verknüpfe ich die listen und konfiguriere die
  // sortable-funktion.
  $( "#normal #nav ul li" ).draggable({
    connectToSortable: "#personal",
    placeholder: "ui-state-highlight",
    helper: 'clone'
  }).disableSelection();
  
  $("#trash").droppable({
		accept: "#personal > li",
		activeClass: "ui-state-highlight",
		drop: function( event, ui ) {
			ui.item.remove;
		}
  });

  $( "#personal, #trash").sortable({
    placeholder: "ui-state-highlight",
    remove: function(event, ui) {
    	ui.item.remove();
    }
  }).disableSelection();
});
