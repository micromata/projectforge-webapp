/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;
import net.ftlines.wicket.fullcalendar.callback.DroppedEvent;
import net.ftlines.wicket.fullcalendar.callback.ResizedEvent;
import net.ftlines.wicket.fullcalendar.callback.SelectedRange;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.joda.time.DateTime;
import org.projectforge.plugins.poll.PollDO;
import org.projectforge.web.calendar.MyFullCalendarEventsProvider;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollEventEventsProvider extends MyFullCalendarEventsProvider
{
  private static final long serialVersionUID = -1869612916168574011L;

  private final IModel<PollDO> pollModel;

  private final Map<PollEventDO, Event> pollEventCache;

  /**
   * @param parent
   */
  public PollEventEventsProvider(final Component parent, final IModel<PollDO> model)
  {
    super(parent);
    this.pollModel = model;
    pollEventCache = new HashMap<PollEventDO, Event>();
  }

  /**
   * @see org.projectforge.web.calendar.MyFullCalendarEventsProvider#getEvents(org.joda.time.DateTime, org.joda.time.DateTime)
   */
  @Override
  public Collection<Event> getEvents(final DateTime start, final DateTime end)
  {
    events.clear();
    for (final PollEventDO iterationEvent : pollEventCache.keySet()) {
      Event event = pollEventCache.get(iterationEvent);
      if (event == null) {
        event = new Event();
        event.setId("" + System.currentTimeMillis());
        event.setStart(iterationEvent.getStartDate());
        event.setEnd(iterationEvent.getEndDate());
        event.setTitle("");
        pollEventCache.put(iterationEvent, event);
      }
      events.put("" + event.getId(), event);
    }
    return events.values();
  }

  /**
   * Just use getEvents, no caching enabled at this page!
   * 
   * @see org.projectforge.web.calendar.MyFullCalendarEventsProvider#buildEvents(org.joda.time.DateTime, org.joda.time.DateTime)
   */
  @Override
  protected void buildEvents(final DateTime start, final DateTime end)
  {
    getEvents(start, end);
  }

  /**
   * @param range
   * @param response
   */
  public void addEvent(final SelectedRange range, final CalendarResponse response)
  {
    final PollEventDO newEvent = new PollEventDO();
    newEvent.setPoll(pollModel.getObject());
    newEvent.setStartDate(range.getStart());
    newEvent.setEndDate(range.getEnd());
    pollEventCache.put(newEvent, null);
    clearSelection(response);
  }

  /**
   * Clears the FullCalendar JS Selection and udpates the events
   * @param response
   */
  private void clearSelection(final CalendarResponse response)
  {
    response.clearSelection().refetchEvents();
  }

  /**
   * @param event
   * @param response
   * @return
   */
  public boolean resizeEvent(final ResizedEvent event, final CalendarResponse response)
  {
    return false;
  }

  /**
   * @param event
   * @param response
   * @return
   */
  public boolean dropEvent(final DroppedEvent event, final CalendarResponse response)
  {
    return false;
  }

  /**
   * @param event
   * @param response
   */
  public void eventClicked(final ClickedEvent event, final CalendarResponse response)
  {
    final PollEventDO clickEvent = searchById(event.getEvent().getId());
    if (clickEvent != null) {
      pollEventCache.remove(clickEvent);
    }
    clearSelection(response);
  }

  private PollEventDO searchById(final String id)
  {
    PollEventDO result = null;
    Event temp = null;
    for (final PollEventDO key : pollEventCache.keySet()) {
      temp = pollEventCache.get(key);
      if (temp != null && StringUtils.equals(temp.getId(), id)) {
        result = key;
        break;
      }
    }
    return result;
  }
}
