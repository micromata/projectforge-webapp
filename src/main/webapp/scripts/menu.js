$(function() {
  // hier verknüpfe ich die listen und konfiguriere die sortable-funktion.
  $("#normal #nav ul li").draggable( {
    connectToSortable : "#personal",
    placeholder : "ui-state-highlight",
    helper : 'clone'
  }).disableSelection();

  $("#personal").sortable( {
    placeholder : "ui-state-highlight",
    change : function(event, ui) {
      $(".remover").remove();
      $("ul#personal li a").prepend('<span class="remover"> 2 </span>');
      serialize(getMenuEntries());
  }
  }).disableSelection();

  $("#personal").bind("update", function(event, ui) {
    console.log("--");
    $("ul#personal li").each(function(index) {
      console.log($(this).attr("id"));
      if (ui.item.attr("id") == $(this).attr("id")) {
        console.log(ui.item.attr("id") + "is a duplicate");
        //ui.item.remove();
        
      }
      
      window.setTimeout( function(){
    	  serialize(getMenuEntries());
      }, 1000);
    });
  });

  // Disable sortable on load...
  $("#personal, #nav ul").sortable("disable");

  // Activating remover
  $('.remover').live('click', function() {
    $(this).parents("li").remove();
    return false;
  });
  
  function getMenuEntries() {
    return $("#personal, #nav ul").sortable("toArray");
  }

  $("#normal .main a").click(function() {
      // One click opens the main menu and and the next click closes the
      // main menu
        $('#normal .main').toggleClass('active');
        // If the main menu is open after toggling...
        if ($('.main').hasClass('active')) {
          // enable sortable...
          $('#personal, #nav ul').sortable('enable');
          // and add blue border around the personal menu...
          $('ul#personal').addClass('dotted');
          // Add the crosses for removing menu entries:
          $("ul#personal li a").prepend(
              '<span class="remover"> 2 </span>');
          $('#remover')
          // main menu is now closed...
        } else {
          // disable sortable....
          $('#personal, #nav ul').sortable('disable');
          // Remove the removers (crosses for removing menu entries):
          $('.remover').remove();
          // remove the blue border around the personal menu
          $('ul#personal').removeClass('dotted');
          serialize(getMenuEntries());
        }
      })
});
