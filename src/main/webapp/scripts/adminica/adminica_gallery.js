$(document).ready(function() {	

	
	$(".gallery ul").isotope({
		sortBy:"name",
		filter: "*",
		getSortData : {
		    name : function ( $elem ) {
		      return $elem.find('.name').text();
		    },
		    size : function ( $elem ) {
		      return $elem.find('.size').text();
		    }
		}
	});
	
	
	
	
	$(".isotope_filter").live('click',function(){
		var x = $(this).attr("id").replace("filter_", ".");
		
		if (x === ".all"){
			$(".gallery ul").isotope({filter: "*"});
		}
		else{
			$(".gallery ul").isotope({filter: x});
		}
		
		return false;
	});
	
	$(".isotope_sort").live('click',function(){
		var y = $(this).attr("id").replace("sort_", "");
		
		$(".gallery ul").isotope({sortBy: y});
		
		return false;
	});
	
	
	if($('.fancybox, .fancy'))
	{
		$(".gallery.fancybox ul li a").fancybox({
        	'overlayColor':'#000' 		
		});
	
		$("a img.fancy").fancybox();
	}

});