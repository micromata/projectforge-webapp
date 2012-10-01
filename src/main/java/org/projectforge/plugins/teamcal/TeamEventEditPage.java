/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamEventEditPage extends AbstractEditPage<TeamEventDO, TeamEventEditForm, TeamEventDao>
{
  private static final long serialVersionUID = 1221484611148024273L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventEditPage.class);

  /**
   * Key for preset the start date.
   */
  public static final String PARAMETER_KEY_START_DATE_IN_MILLIS = "startMillis";

  /**
   * Key for preset the stop date.
   */
  public static final String PARAMETER_KEY_END_DATE_IN_MILLIS = "endMillis";

  /**
   * Key for moving start date.
   */
  public static final String PARAMETER_KEY_NEW_START_DATE = "newStartDate";

  /**
   * Key for moving start date.
   */
  public static final String PARAMETER_KEY_NEW_END_DATE = "newEndDate";

  /**
   * Key for calendar.
   */
  public static final String PARAMETER_KEY_TEAMCALID = "teamCalId";

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public TeamEventEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamevent");
    super.init();
  }

  /**
   * pre-init calendar with page parameters.
   * Sets start and end date and teamCal id, if necessary.
   */
  @SuppressWarnings("null")
  public void preInit()
  {
    if (isNew() == true) {
      final PageParameters parameters = getPageParameters();
      final Long startDateInMillis = WicketUtils.getAsLong(parameters, PARAMETER_KEY_START_DATE_IN_MILLIS);
      final Long stopTimeInMillis = WicketUtils.getAsLong(parameters, PARAMETER_KEY_END_DATE_IN_MILLIS);
      final String teamCalId = WicketUtils.getAsString(parameters, PARAMETER_KEY_TEAMCALID);
      if (startDateInMillis != null) {
        getData().setStartDate(new Timestamp(startDateInMillis));
        if (stopTimeInMillis == null) {
          getData().setEndDate(new Timestamp(stopTimeInMillis));
        }
      }
      if (stopTimeInMillis != null) {
        getData().setEndDate(new Timestamp(stopTimeInMillis));
        if (startDateInMillis == null) {
          getData().setStartDate(new Timestamp(startDateInMillis));
        }
      }
      if (teamCalId != null) {
        getData().setCalendar(teamCalDao.getById(Integer.valueOf(teamCalId)));
      }
    } else {
      final Long newStartTimeInMillis = WicketUtils.getAsLong(getPageParameters(), PARAMETER_KEY_START_DATE_IN_MILLIS);
      final Long newStopTimeInMillis = WicketUtils.getAsLong(getPageParameters(), PARAMETER_KEY_END_DATE_IN_MILLIS);
      if (newStartTimeInMillis != null) {
        getData().setStartDate(new Timestamp(newStartTimeInMillis));
      }
      if (newStopTimeInMillis != null) {
        getData().setEndDate(new Timestamp(newStopTimeInMillis));
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#update()
   */
  @Override
  protected void update()
  {
    if (getData().getStartDate().after(getData().getEndDate())) {
      error(getString("plugins.teamevent.duration.error"));
    } else {
      super.update();
      final PageParameters params = new PageParameters();
      params.add("id", getData().getCalendar().getId());
      final CalendarPage page = new CalendarPage(params);
      setResponsePage(page);
    }
  }

  @Override
  public void setResponsePage()
  {
    super.setResponsePage();
    if (returnToPage instanceof CalendarPage) {
      // Display the date of this time sheet in the CalendarPage (usefull if the time sheet was moved).
      ((CalendarPage) returnToPage).setStartDate(getData().getStartDate());
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#create()
   */
  @Override
  protected void create()
  {
    if (getData().getStartDate().after(getData().getEndDate())) {
      error(getString("plugins.teamevent.duration.error"));
    } else {
      super.create();
      final PageParameters params = new PageParameters();
      params.add("id", getData().getCalendar().getId());
      final CalendarPage page = new CalendarPage(params);
      setResponsePage(page);
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#cancel()
   */
  @Override
  protected void cancel()
  {
    final PageParameters params = new PageParameters();
    params.add("id", getData().getCalendar().getId());
    final CalendarPage page = new CalendarPage(params);
    setResponsePage(page);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected TeamEventDao getBaseDao()
  {
    return teamEventDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage, org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected TeamEventEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final TeamEventDO data)
  {
    return new TeamEventEditForm(this, data);
  }

}
