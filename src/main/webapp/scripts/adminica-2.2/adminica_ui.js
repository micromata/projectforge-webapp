$(document).ready(function() {
	adminicaUi();
});

function adminicaUi(){

	//jQuery UI elements (more info can be found at http://jqueryui.com/demos/)

	// Navigation accordions
		$(".dropdown_menu > ul > li").each(function(){
			$(this).children("ul").addClass("dropdown").parent().addClass("has_dropdown");
		});

		$("ul.drawer").parent("li").addClass("has_drawer");

		$(".has_drawer > a").bind('click',function(){
			var menuType = ($(this).parent().parent().hasClass("open_multiple"));
			if (menuType != true){
				$(this).parent().siblings().removeClass("open").children("ul.drawer").slideUp();
			}
			$(this).parent().toggleClass("open").children("ul").slideToggle();
			return false;
		});


		// Set on state icon colour

		$('#nav_top > ul > li.current > a > img').each(function(){
			var imgPath = $(this).attr('src').replace("/grey/", "/white/");
			$(this).attr('src', imgPath);
		});

	navCurrent();



	// Side Nav

	$('#sidebar').mouseenter(function(){
		if ($(this).stop(true,true).css('z-index') == '999'){
		$(this).animate({
			left: '-10px'
		}, 200);
		}
	});

	$('#sidebar').mouseleave(function(){
		if ($(this).stop(true,true).css('z-index') == '999'){
		$(this).animate({
			left: '-200px'
		}, 300);
		}
	});

	sideNavCurrent();

	// Stack Nav

	$(".stackbar > ul > li > a").on("click", function(){

		if ($(this).attr("href") == "#"){
			$(".stackbar > ul li").removeClass("current");
			$(this).parent().addClass("current");
		}

		$(".stackbar > ul li").removeClass("current");
		$(this).parent().addClass("current");

		if ($(this).parent().find("ul").length>0){
			$(this).parents(".stackbar").removeClass("list_view").addClass("stack_view");
		}
		else{
			$(this).parents(".stackbar").addClass("list_view").removeClass("stack_view");
		}

	});

	stackNavCurrent();


 	// Content Box Toggle Config

	$("a.toggle").on('click', function(){
		$(this).toggleClass("toggle_closed").parent().next().slideToggle("slow");
		$(this).parent().siblings(".box_head, .tab_header").removeClass("round_top").toggleClass("round_all");
		$(this).parent().parent().toggleClass("closed");
		return false; //Prevent the browser jump to the link anchor
	});


	// toggle all boxes

	$(".toggle_all a").on('click', function(){
		if ($(this).hasClass("close_all")){
			$(".box .toggle").trigger("click");
		}
		if ($(this).hasClass("show_all")){
			$(".box .toggle_closed").trigger("click");
		}

		$(this).parent().not(".closed").toggleClass("closed", 600);
		$(this).parent(".closed").toggleClass("closed");
	});

	$("[data-toggle-class]").on('click', function(){
		x = $(this).attr('data-toggle-class');
		$(".box."+x+" .toggle").trigger("click");
	});


 	// Hide a Content Box

	$(".dismiss_button").on("click",function(){
		var theTarget = $(this).attr("data-dismiss-target");
		console.log(theTarget);
		$(theTarget).animate({opacity:0},'slow',function(){
			$(this).slideUp();
		});
	});


	// Content Boxes without a titlebar

		$('.box').each(function(){
			if (! $(this).children().is('.box_head, .tab_header, .tab_sider')){

				$(this).addClass('no_titlebar');

			}
		});



	// Button Classes

		//$("input[type=button]").notClass("btn").addClass("button");
		$("input[type=button]").addClass("button");
		
		$('.button').each(function(){
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

		$('.indented_button_bar > .columns').each(function(){
			$(this).parent().addClass('has_columns');
		});

		$(".button").on('mousedown',function(){
			$(this).addClass("button_down");
		}).on('mouseup',function(){
			$(this).removeClass("button_down");
		}).on('mouseleave',function(){
			$(this).removeClass("button_down");
		});




	columnHeight();
  centerContent();

	$(window).resize(function() {
		columnHeight();
		centerContent();
	});
}

function columnHeight(){
	$(".even fieldset.label_side, .even > div, .even fieldset").css('height','auto');

	$(".label_side > div, .columns").addClass("clearfix");

	$(".columns.even").each(function() {
		x = 0
		$(this).find("fieldset").children().each(function(){
			y = $(this).outerHeight();
			if(y > x){
				x = y
			}
		});
		$(this).find("fieldset.label_side").children().css('height',x-30);

		$(this).children(".col_50,.col_33,.col_66,.col_25,.col_75,.col_60,.col_40,.col_20").each(function(){
			y = $(this).outerHeight();
			if(y > x){
				x = y
			}
		});
		$(this).children().css('height',x);


		$(this).find("fieldset").not(".label_side").each(function(){
			y = $(this).outerHeight();
			if(y > x){
				x = y
			}
		});
		$(this).find("fieldset").css('height',x-1);

	});

	z = 0
	$(this).find("fieldset").children().each(function(){
		y = $(this).outerHeight();
		if(y > z){
			z = y
		}
	});
	$(this).find("fieldset.label_side").children().css('height',z-31);
}

function centerContent(){
	$(".isolate").each(function(){
		var theHeight =	$(window).height()-60;
		$(this).css("height", theHeight);
	});
}

function navCurrent(){

	var nav1 = $("#wrapper").data("adminica-nav-top");
	var nav2 = $("#wrapper").data("adminica-nav-inner");
	$('#nav_top > ul > li').eq(nav1 - 1).addClass("current").find("li").eq(nav2 - 1).addClass("current");

	$('#nav_top > ul > li.current > a > img').each(function(){
		var imgPath = $(this).attr('src').replace("/grey/", "/white/");
		$(this).attr('src', imgPath);
	});
}

function sideNavCurrent(){

	var snav1 = $("#wrapper").data("adminica-side-top");
	var snav2 = $("#wrapper").data("adminica-side-inner");

	$('ul#nav_side > li').eq(snav1 - 1).addClass("current").find("li").eq(snav2 - 1).addClass("current");
	$('ul#nav_side > li').addClass("icon_only").children("a").children("span:visible").parent().parent().removeClass("icon_only");
}

function stackNavCurrent(stnav1, stnav2){

	var stnav1 = $("#wrapper").data("adminica-stack-top");
	var stnav2 = $("#wrapper").data("adminica-stack-inner");

	if(stnav2 == null){
		$('#stackbar').addClass("list_view");
	}
	else{
		$('#stackbar').addClass("stack_view");
	}

	$('#stackbar > ul > li').eq(stnav1 - 1).addClass("current").find("li").eq(stnav2 - 1).addClass("current");
}