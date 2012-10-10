/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.calendar.CalendarFilter;
import org.projectforge.web.calendar.CalendarForm;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.calendar.CalendarPanel;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class TeamCalCalendarPage extends CalendarPage
{
  private static final long serialVersionUID = -6413028759027309796L;

  private TeamCalCalendarForm form;

  static final String USERPREF_KEY = "TeamCalendarPage.userPrefs";

  /**
   * @param parameters
   */
  public TeamCalCalendarPage(final PageParameters parameters)
  {
    super(parameters);
  }

  @Override
  protected CalendarForm initCalendarForm(final CalendarPage calendarPage)
  {
    this.form = new TeamCalCalendarForm(calendarPage);
    return this.form;
  }

  @Override
  protected CalendarPanel initCalenderPanel()
  {
    final TeamCalCalendarPanel result = new TeamCalCalendarPanel("cal", form.getCurrentDatePanel());
    result.setOutputMarkupId(true);
    form.add(result);
    result.init(getFilter());
    return result;
  }

  /**
   * @see org.projectforge.web.calendar.CalendarPage#initCalendarFilter()
   */
  @Override
  protected CalendarFilter initCalendarFilter()
  {
    TeamCalCalendarFilter filter = (TeamCalCalendarFilter) getUserPrefEntry(USERPREF_KEY);
    if (filter == null) {
      filter = new TeamCalCalendarFilter();
      putUserPrefEntry(USERPREF_KEY, filter, true);
    }
    return filter;

  }

  @Override
  protected CalendarFilter getFilter()
  {
    return form.getFilter();
  }
}
