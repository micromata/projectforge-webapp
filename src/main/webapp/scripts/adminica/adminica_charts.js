$(document).ready(function() {

if ($(".flot")){
	// Graphs and Charts - Flot jQuery
	
	var line_data = [];
    for (var i = 0; i < 14; i += 0.2)
        line_data.push([i, Math.sin(i)+8]);

    var bar_data = [[0, 5], [2, 7], [4, 11], [6, 1], [8, 8], [10, 7], [12, 9], [14, 3]];
	var bar_data_2 = [[1, 3], [3, 8], [5, 5], [7, 13], [9, 8], [11, 5], [13, 8], [15, 5]];

    // a null signifies separate line segments
    var point_data = [[0, 12], [7, 12], [8, 2.5], [12, 2.5], [15, 7]];

	var extra_data_1 = [];
    for (var i = -20; i < 20; i += 0.4)
        extra_data_1.push([i, Math.tan(i)+(i*5)]);

	var extra_data_2 = [[1988, 483994], [1989, 479060], [1991, 401949], [1993, 402375], [1994, 377867], [1996, 337946], [1997, 336185], [1998, 328611], [2000, 342172], [2001, 344932], [2003, 440813], [2004, 480451], [2006, 528692]];

	var pie_data = [];
	var series = Math.floor(Math.random()*5)+1;
	for( var i = 0; i<series; i++)
	{
		pie_data[i] = { label: "Series"+(i+1), data: Math.floor(Math.random()*100)+1 }
	}

	var pie_data_1 = [
			{ label: "Slice 1",  data: [[1,117]], color: '#122b45'},
			{ label: "Slice 2",  data: [[1,30]], color: '#064792'},
			{ label: "Slice 3",  data: [[1,44]], color: '#4C5766'},
			{ label: "Slice 4",  data: [[1,90]], color: '#9e253b'},
			{ label: "Slice 5",  data: [[1,70]], color: '#8d579a'},
			{ label: "Slice 6",  data: [[1,80]], color: '#2b4356'}
		];

	var pie_data_2 = [
			{ label: "Slice 1",  data: [[1,117]], color: '#122b45'},
			{ label: "Slice 2",  data: [[1,30]], color: '#064792'},
			{ label: "Slice 3",  data: [[1,44]], color: '#4C5766'},
			{ label: "Slice 4",  data: [[1,90]], color: '#9e253b'},
			{ label: "Slice 5",  data: [[1,70]], color: '#8d579a'},
			{ label: "Slice 6",  data: [[1,80]], color: '#2b4356'}
		];

    var adminica_grad_black = { colors: ["#4C5766 ", "#313841 "] };
    var adminica_grad_blue = { colors: ["#1C5EA0 ", "#064792 "] };
    var adminica_grad_navy = { colors: ["#2b4356 ", "#122b45 "] };
    var adminica_grad_red = { colors: ["#9e253b ", "#7C1F30 "] };
    var adminica_grad_green = { colors: ["#3d8336 ", "#277423 "] };
    var adminica_grad_magenta = { colors: ["#9b6ca6 ", "#8d579a "] };
    var adminica_grad_brown = { colors: ["#53453e ", "#3b2e28 "] };
    var adminica_grad_grey = { colors: ["#D0D6DA", "#B4BBC1"] };

    var adminica_black = "#4C5766 ";
    var adminica_blue = "#1C5EA0 ";
    var adminica_navy = "#2b4356 ";
    var adminica_red = "#9e253b ";
    var adminica_green = "#3d8336";
    var adminica_magenta = "#9b6ca6";
    var adminica_brown = "#53453e";

	$.plot($("#flot_pie_1"), pie_data_1,
	{
	        series: {
	            pie: {
                	innerRadius: 0,
	                show: true
	            },
		        grid: {
		            hoverable: true,
		            clickable: true
		        }
	        }
	});

    $.plot($("#flot_bar"), 
    [
        {	

			shadowSize: 25,
        	label:'Bar Chart 1',
			color:adminica_magenta,
            data: bar_data,
            bars: { 
            	show: true,
	            fill: true,
  				fillColor: adminica_grad_magenta,
				lineWidth: 0,
				border:false
            	}
        },		
        {	

			shadowSize: 25,
        	label:'Bar Chart 2',
			color:'#4C5766',
            data: bar_data_2,
            bars: { 
            	show: true,
	            fill: true,
  				fillColor: adminica_grad_black,
				lineWidth: 0,
				border:false
            	}
        }
    ],  
	    {
	        grid:{
			    show: true,
				aboveData: false,
				backgroundColor: { colors: ["#fff", "#eee"] },
			    labelMargin:15,
				//axisMargin: number
			    //markings: array of markings or (fn: axes -> array of markings)
			    borderWidth: 1,
				borderColor: '#cccccc',
			    //minBorderMargin: number or null
			    clickable: true,
			    hoverable: true,
			    autoHighlight: true,
			    mouseActiveRadius: 10
	        	},
	        legend: {
			    show: true,
			    //labelFormatter: null or (fn: string, series object -> string)
			    labelBoxBorderColor: '#fff',
			    noColumns: 5,
			    //position: "ne" or "nw" or "se" or "sw"
				margin: 10,
				backgroundColor: '#fff'
			    //backgroundOpacity: number between 0 and 1
			    //container: "#legend_holder"
			  }
	    }
    );

	$.plot($("#flot_line"), 
    [
        {	
			shadowSize: 5,
        	label:'Line Chart 1',
			color:adminica_blue,
            data: line_data,
            lines: { 
            	show: true,
	            fill: true,
  				fillColor: adminica_grad_grey,
				lineWidth: 4
            	}
        },
        {	
			shadowSize: 5,
        	label:'Line Chart 2',
			color:adminica_red,
            data: bar_data,
            lines: { 
            	show: true,
	            fill: false,
				lineWidth: 4
            	},
	        points: { 
            	show: true,
	            fill: false,
				lineWidth: 2
            	}
        }
    ],  
	    {
	        grid:{
			    show: true,
				aboveData: false,
				backgroundColor: { colors: ["#fff", "#eee"] },
			    labelMargin:15,
				//axisMargin: number
			    //markings: array of markings or (fn: axes -> array of markings)
			    borderWidth: 1,
				borderColor: '#cccccc',
			    //minBorderMargin: number or null
			    clickable: true,
			    hoverable: true,
			    autoHighlight: true,
			    mouseActiveRadius: 10
	        	},
	        legend: {
			    show: true,
			    //labelFormatter: null or (fn: string, series object -> string)
			    labelBoxBorderColor: '#fff',
			    noColumns: 5,
			    //position: "ne" or "nw" or "se" or "sw"
				margin: 10,
				backgroundColor: '#fff'
			    //backgroundOpacity: number between 0 and 1
			    //container: "#legend_holder"
			  }
	    }
    );

	$.plot($("#flot_points"), 
    [
        {	
			shadowSize: 10,
        	label:'Points Chart',
			color:adminica_blue,
            data: extra_data_2,
            points: { 
            	show: true,
	            fill: true,
				fillColor: '#ffffff',
				lineWidth: 3
            	},
            lines: { 
            	show: true,
	            fill: true,
  				fillColor: adminica_grad_black,
				lineWidth: 5
            	}
        }
    ],  
	    {
	        grid:{
			    show: true,
				aboveData: false,
				backgroundColor: { colors: ["#fff", "#eee"] },
			    labelMargin:15,
				//axisMargin: number
			    //markings: array of markings or (fn: axes -> array of markings)
			    borderWidth: 1,
				borderColor: '#cccccc',
			    //minBorderMargin: number or null
			    clickable: true,
			    hoverable: true,
			    autoHighlight: true,
			    mouseActiveRadius: 10
	        	},
	        legend: {
			    show: true,
			    //labelFormatter: null or (fn: string, series object -> string)
			    labelBoxBorderColor: '#fff',
			    noColumns: 5,
			    //position: "ne" or "nw" or "se" or "sw"
				margin: 10,
				backgroundColor: '#fff'
			    //backgroundOpacity: number between 0 and 1
			    //container: "#legend_holder"
			  }
	    }
    );
    
    //window.onresize = redrawFunc; 

}   
});