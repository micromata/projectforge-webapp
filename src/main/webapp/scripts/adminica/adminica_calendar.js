$(document).ready(function() {
//Fullcalendar		
	
    $('#calendar').fullCalendar({
    		firstDay:'1',
    		weekMode:'liquid',
    		aspectRatio: '1.5',
			theme:true,
			selectable:true,
			editable:true,
			draggable:true,
			droppable:true,
			timeFormat:'H:mm',
	    	axisFormat:'H:mm',
	    	columnFormat:{
			    month: 'ddd',    // Mon
			    week: 'ddd dS', // Mon 9/7
			    day: 'dddd dS MMMM'  // Monday 9/7
			},
			titleFormat:{
			    month: 'MMMM yyyy',                             // September 2009
			    week: "MMM d[ yyyy]{ 'to'[ MMM] d, yyyy}", // Sep 7 - 13 2009
			    day: 'ddd, MMMM d, yyyy'                  // Tuesday, Sep 8, 2009
			},
	    	allDayText:'All Day',
			header:{
			    left:   'prev title next, today',
			    center: '',
			    right:  'agendaWeek,agendaDay,month'
				},
			
			eventSources: [

			        // your event source
			        {
			            events: [ // put the array in the `events` property
			                {
			                    title  : 'Company AGM',
			                    start  : '2011-04-03',
							    className:'calendar_green'
			                },
			                {
			                    title  : 'Business Trip',
			                    start  : '2011-04-15',
			                    end    : '2011-04-20',
							    className:'calendar_blue'
			                },
			                {
			                    title  : 'Day off',
			                    start  : '2011-04-08 12:30:00',
							    className:'calendar_red'
			                }
			            ]
			        },
			        {
					    url: 'https://www.google.com/calendar/feeds/nueoipsjhgm857gpojq5563cfo@group.calendar.google.com/public/basic',
					    className:'calendar_magenta'
					},
					{
						url: 'http://www.google.com/calendar/feeds/usa__en%40holiday.calendar.google.com/public/basic',
						className: 'calendar_navy'
					}
			
			
			    ],
			
			drop: function(date, allDay) { // this function is called when something is dropped
		
			// retrieve the dropped element's stored Event Object
			var originalEventObject = $(this).data('eventObject');
			
			// we need to copy it, so that multiple events don't have a reference to the same object
			var copiedEventObject = $.extend({}, originalEventObject);
			
			// assign it the date that was reported
			copiedEventObject.start = date;
			copiedEventObject.allDay = allDay;
			
			// render the event on the calendar
			// the last `true` argument determines if the event "sticks" (http://arshaw.com/fullcalendar/docs/event_rendering/renderEvent/)
			$('#calendar').fullCalendar('renderEvent', copiedEventObject, true);
			
			// is the "remove after drop" checkbox checked?
			if ($('#drop-remove').is(':checked')) {
				// if so, remove the element from the "Draggable Events" list
				$(this).remove();
			}
			
		}
	        
	    });
	
	$('ul#calendar_drag_list li a').each(function() {
	
		// create an Event Object (http://arshaw.com/fullcalendar/docs/event_data/Event_Object/)
		// it doesn't need to have a start or end
		var eventObject = {
			title: $.trim($(this).text()), // use the element's text as the event title
			className: 'calendar_grad'
		};
		
		// store the Event Object in the DOM element so we can get to it later
		$(this).data('eventObject', eventObject);
		
		// make the event draggable using jQuery UI
		$(this).draggable({
			zIndex: 999,
			revert: true,      // will cause the event to go back to its
			revertDuration: 10,  //  original position after the drag
			cursorAt: { top:15, left: 0 }
		});
		
	});
});