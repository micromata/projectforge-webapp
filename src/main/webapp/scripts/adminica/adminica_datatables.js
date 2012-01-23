$(document).ready(function() {
	// DataTables Config (more info can be found at http://www.datatables.net/)
	var table1 = $('#dt1 .datatable').dataTable( {
				"bJQueryUI": true,
				"sScrollX": "",
				"bSortClasses": false,
				"aaSorting": [[0,'asc']],
				"bAutoWidth": true,
				"bInfo": true,
				"sScrollX": "101%",
				"bScrollCollapse": true,
				"sPaginationType": "full_numbers",
				"bRetrieve": true,
				"fnInitComplete": function () {
				
					$("#dt1 .dataTables_length > label > select").uniform();
					$("#dt1 .dataTables_filter input[type=text]").addClass("text");
					$(".datatable").css("visibility","visible");
				
				}
	});
	
	var table2 = $('#dt2 .datatable').dataTable( {
				"bJQueryUI": true,
				"sScrollX": "",
				"bSortClasses": false,
				"aaSorting": [[0,'asc']],
				"bAutoWidth": true,
				"bInfo": true,
				"sScrollY": "100%",	
				"sScrollX": "100%",
				"bScrollCollapse": true,
				"sPaginationType": "full_numbers",
				"bRetrieve": true,
				"fnInitComplete": function () {
				
					$("#dt2 .dataTables_length > label > select").uniform();
					$("#dt2 .dataTables_filter input[type=text]").addClass("text");
					$(".datatable").css("visibility","visible");
				
				}
	});
	
	var table3 = $('#dt3 .datatable').dataTable( {
				"bJQueryUI": true,
				"bPaginate": false,
				"sScrollX": "",
				"bSortClasses": false,
				"aaSorting": [[0,'asc']],
				"bAutoWidth": true,
				"bInfo": true,
				"sScrollY": "100%",	
				"sScrollX": "100%",
				"bScrollCollapse": true,
				"sPaginationType": "full_numbers",
				"bRetrieve": true,
				"fnInitComplete": function () {
				
					$("#dt3 .dataTables_length > label > select").uniform();
					$("#dt3 .dataTables_filter input[type=text]").addClass("text");
					$(".datatable").css("visibility","visible");
				
				}
	});

	


// DataTables Config - this reloads dataTables when it is placed in a hidden tab			
	
	$(".tabs").tabs( {
        "show": function(event, ui) {
            var oTable = $('div.dataTables_scrollBody > table', ui.panel).dataTable();
            if ( oTable.length > 0 ) {
                oTable.fnAdjustColumnSizing();
                $(".tabs div.dataTables_scroll").css({
                    "display":"none",
                    "visibility":"visible" 
                }).show();
            }
        }
    });

// DataTables Config - this reloads dataTables when it is placed in a closed accordion			
	$( ".content_accordion" ).accordion( {
		"change": function(event, ui) {
			var oTable = $('div.dataTables_scrollBody > table', ui.panel).dataTable();
            if ( oTable.length > 0 ) {
                oTable.fnAdjustColumnSizing();
                $(".content_accordion div.dataTables_scroll").css({
                    "display":"none",
                    "visibility":"visible" 
                }).show();
            }
		}
	});
    
	$(window).resize(function(){
        table1.fnAdjustColumnSizing();
        table2.fnAdjustColumnSizing();
        table3.fnAdjustColumnSizing();
	});
});