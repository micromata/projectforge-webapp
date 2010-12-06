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

  $("#normal .main a").click(function(){
    // Ein Klick auf das Hauptmenü öffnet...ein weiterer
    // schließt das Hauptmenü
    $(this).parent(".main").toggleClass("active");
    // wenn nach dem tooglen das hauptmenü offen ist..
    if($(".main").hasClass("active")){
      // sortable wird angeschalten...
      $( "#personal, #nav ul" ).sortable("enable");
      // gleichzeitig wird der blaue rand um das persönliche
      // menü geklebt
      $("ul#personal").addClass("dotted");
      // ansonsten..
    } else {
      // sortable deaktivieren
      $( "#personal, #nav ul" ).sortable("disable");
      // @kai: hier deine magic
      // serialize den shice
      s = $( "#personal, #nav ul" ).sortable("toArray");
      $("#serialitzkowitsch").val(s);
      // blauer rand wech...
      $("ul#personal").removeClass("dotted");
    }
  })
});
