$(function() {
  // hier verknüpfe ich die listen und konfiguriere die
  // sortable-funktion.
  $( "#normal #nav ul li" ).draggable({
    connectToSortable: "#personal",
    placeholder: "ui-state-highlight",
    helper: 'clone'
  }).disableSelection();

  $( "#personal" ).sortable({
    placeholder: "ui-state-highlight",
  }).disableSelection();
});
