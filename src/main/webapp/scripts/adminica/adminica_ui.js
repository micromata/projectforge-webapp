/*
 * Adminica UI
 *
 * Copyright (c) 2010 Tricycle Interactive
 *
 * http://www.tricycle.ie
 *
 * This file configures all the different jQuery scripts used in the Adminica Admin template. Links to the scripts can be found at the beginning of each function
 *
 */
 
 
 $(function() {

//jQuery UI elements (more info can be found at http://jqueryui.com/demos/)
	
	// Navigation accordions
		$("#nav_top > ul > li").each(function(){
			$(this).children("ul").addClass("dropdown").parent().addClass("has_dropdown");
		});
	
		$("ul.drawer").parent("li").addClass("has_drawer");
		
		$(".has_drawer > a").click(function(){
			var menuType = ($(this).parent().parent().hasClass("open_multiple"));
			if (menuType != true){
				$(this).parent().siblings().removeClass("open").children("ul.drawer").slideUp();
			}
			$(this).parent().toggleClass("open").children("ul").slideToggle();
			return false;
		});
		
		$('#nav_top > ul > li.current > a > img').each(function(){
			var imgPath = $(this).attr('src').replace("/grey/", "/white/");
			$(this).attr('src', imgPath);
		});
	
	// Loading Box - tweak the delays to your liking
		function loadingOverlay(){
			$("#loading_overlay .loading_message").delay(200).fadeOut(function(){}); 
			$("#loading_overlay").delay(500).fadeOut();  
		}
		window.onload = function () {
			loadingOverlay();   
	   	};
	   
	// toggle all boxes

	$(".toggle_all a").click(function(){
		if ($(this).hasClass("hide_all")){
			$(".box .toggle").trigger("click");
		}
		if ($(this).hasClass("show_all")){
			$(".box .toggle_closed").trigger("click");
		}
		
		$(this).parent().not(".closed").toggleClass("closed", 600);
		$(this).parent(".closed").toggleClass("closed");
			
	});

 	
	//Slide to top link
		$().UItoTop({ easingType: 'easeOutQuart' });
		
 	// Content Box Toggle Config 
		$("a.toggle").click(function(){
			$(this).toggleClass("toggle_closed").next().slideToggle("slow");
			$(this).siblings(".box_head").removeClass("round_top").toggleClass("round_all");
			return false; //Prevent the browser jump to the link anchor
		});
 	
 	// Content Box Tabs Config
		$( ".tabs" ).tabs({ 
			fx: {opacity: 'toggle', duration: 'slow'} 
		});

		$( ".side_tabs" ).tabs({ 
			fx: {opacity: 'toggle', duration: 'slow', height:'auto'} 
		});
		
	// Content Box Accordion Config		
		$( ".content_accordion" ).accordion({
			collapsible: true,
			active:false,
			header: 'h3.bar', // this is the element that will be clicked to activate the accordion 
			autoHeight:false,
			event: 'mousedown',
			icons:false,
			animated: true
		});
		
	// Sortable Content Boxes Config				
		$( ".main_container" ).sortable({
			handle:'.grabber',  // the element which is used to 'grab' the item
			items:'div.box', // the item to be sorted when grabbed!
			opacity:0.8,
			revert:true,
			tolerance:'pointer',
			helper:'original',
			forceHelperSize:true,
			placeholder: 'dashed_placeholder',		
			forcePlaceholderSize:true,
			cursorAt: { top: 16, right: 16 }
		});

	// Sortable Accordion Items Config			
		$( ".content_accordion" ).not(".no_rearrange").sortable({
			handle:'a.handle',
			axis: 'y', // the items can only be sorted along the y axis
			revert:true,
			tolerance:'pointer',
			forcePlaceholderSize:true,
			cursorAt: { top: 16, right: 16 }
		});
		
	// static tables alternating rows
		$('table.static tr:even').addClass("even");
	// static table input	
		$("table.static input[type=text]").addClass("text");
		
		
	// Content Boxes without a titlebar
		$('.box').each(function(){
			if (! $(this).children().is('.box_head, .tab_header, .tab_sider')){
			
				$(this).addClass('no_titlebar');
			
			}
		});
		
	// Button Classes
	
		$('button').each(function(){
			if (! $(this).children().is('span')){
				$(this).addClass('icon_only');
			}
			if (! $(this).children().is('img, .ui-icon')){
				$(this).addClass('text_only');
			}
			if ($(this).children().is('img')){
				$(this).addClass('img_icon');
			}
			if ($(this).children().is('.ui-icon')){
				$(this).addClass('div_icon');
			}
			if ($(this).children().is('span')){
				$(this).addClass('has_text');
			}
		});	
		
	// Center isolated class
	
	function centerContent(){
		$(".isolate").each(function(){
			var theHeight =	$(window).height()-60; 
			$(this).css("height", theHeight);
		});
	}
	
  	centerContent();
  		
	$(window).resize(function() {
  		centerContent();
	});
	
	// fade in once page is fully loaded

	
		//$(".box, .block, .flat_area, .indent , #nav_top").css("opacity","0");
				
		$(window).load(function(){
		
 			$("#nav_top, .indent, .flat_area").animate({opacity: 1	});
 			
 			$("#login_box").delay(100).slideDown();
 			
 			$(".box").animate({
	 				opacity: 1
		 			}, function(){
		 				$(".block").animate({
		 				opacity: 1
		 			});
			});	
 			

 		});


});