/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.EventSource;
import net.ftlines.wicket.fullcalendar.callback.View;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.calendar.MyFullCalendar;
import org.projectforge.web.calendar.MyFullCalendarConfig;
import org.projectforge.web.wicket.AbstractSecuredPage;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollEventEditPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = 2988767055605267801L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PollEventEditPage.class);

  private MyFullCalendarConfig config;

  private MyFullCalendar calendar;

  /**
   * @param parameters
   */
  public PollEventEditPage(final PageParameters parameters)
  {
    super(parameters);
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    config = new MyFullCalendarConfig(this);
    config.setSelectable(true);
    config.setSelectHelper(true);
    config.setDefaultView("agendaWeek");
    config.getHeader().setRight("");
    config.setLoading("function(bool) { if (bool) $(\"#loading\").show(); else $(\"#loading\").hide(); }");
    calendar = new MyFullCalendar("cal", config) {
      private static final long serialVersionUID = -6819899072933690316L;

      @Override
      protected void onViewDisplayed(final View view, final CalendarResponse response)
      {
        response.refetchEvents();
      }
    };
    calendar.setMarkupId("calendar");
    final EventSource eventSource = new EventSource();
    final PollEventEventsProvider eventProvider = new PollEventEventsProvider(this);
    eventSource.setEventsProvider(eventProvider);
    config.add(eventSource);
    body.add(calendar);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.poll.title");
  }

}
