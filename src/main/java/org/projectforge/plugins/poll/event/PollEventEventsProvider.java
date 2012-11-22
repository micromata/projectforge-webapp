/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import net.ftlines.wicket.fullcalendar.Event;

import org.apache.wicket.Component;
import org.joda.time.DateTime;
import org.projectforge.web.calendar.MyFullCalendarEventsProvider;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollEventEventsProvider extends MyFullCalendarEventsProvider
{
  private static final long serialVersionUID = -1869612916168574011L;

  /**
   * @param parent
   */
  public PollEventEventsProvider(final Component parent)
  {
    super(parent);
  }

  /**
   * @see org.projectforge.web.calendar.MyFullCalendarEventsProvider#buildEvents(org.joda.time.DateTime, org.joda.time.DateTime)
   */
  @Override
  protected void buildEvents(final DateTime start, final DateTime end)
  {
    final Event testEvent = new Event();
    testEvent.setStart(start);
    testEvent.setEnd(start.plusDays(1));
    testEvent.setTitle("test");
    testEvent.setId("id1");
    events.put("id1", testEvent);
  }

}
