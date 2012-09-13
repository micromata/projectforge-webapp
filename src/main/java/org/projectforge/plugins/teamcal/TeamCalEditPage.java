/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.EventSource;
import net.ftlines.wicket.fullcalendar.callback.SelectedRange;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.web.calendar.MyFullCalendar;
import org.projectforge.web.calendar.MyFullCalendarConfig;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@EditPage(defaultReturnPage = TeamCalListPage.class)
public class TeamCalEditPage extends AbstractEditPage<TeamCalDO, TeamCalEditForm, TeamCalDao> implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditPage.class);

  private static final long serialVersionUID = -3352981782657771662L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public TeamCalEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#init()
   */
  @Override
  protected void init()
  {
    super.init();
    final MyFullCalendarConfig config = new MyFullCalendarConfig(this);
    config.setSelectable(true);
    config.setSelectHelper(true);
    config.setLoading("function(bool) { if (bool) $(\"#loading\").show(); else $(\"#loading\").hide(); }");
    config.setAllDaySlot(true);
    config.setDefaultView("agendaWeek");
    @SuppressWarnings("serial")
    final MyFullCalendar myCalendar = new MyFullCalendar("calendar", config){
      @Override
      protected void onDateRangeSelected(final SelectedRange range, final CalendarResponse response)
      {
        if (log.isDebugEnabled() == true) {
          log.debug("Selected region: " + range.getStart() + " - " + range.getEnd() + " / allDay: " + range.isAllDay());
        }
        final PageParameters parameters = new PageParameters();
        parameters.add(TeamEventEditPage.PARAMETER_KEY_START_DATE_IN_MILLIS, DateHelper.getDateTimeAsMillis(range.getStart()));
        parameters.add(TeamEventEditPage.PARAMETER_KEY_END_DATE_IN_MILLIS, DateHelper.getDateTimeAsMillis(range.getEnd()));
        parameters.add(TeamEventEditPage.PARAMETER_KEY_TEAMCALID, getData().getId());
        final TeamEventEditPage teamEventEditPage = new TeamEventEditPage(parameters);
        teamEventEditPage.setReturnToPage((WebPage) getPage());
        setResponsePage(teamEventEditPage);
      }
    };
    getForm().add(myCalendar);
    myCalendar.setMarkupId("calendar");
    final EventSource eventSource = new EventSource();
    final TeamCalEventProvider eventProvider = new TeamCalEventProvider(this, teamCalDao);
    eventSource.setEventsProvider(eventProvider);
    eventSource.setEditable(true);
    config.add(eventSource);
  }

  /**
   * required to find the component which called the request.
   * components are marked with ids.
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("fullAccessGroupId".equals(property) == true) {
      teamCalDao.setFullAccessGroup(getData(), (Integer) selectedValue);
    } else if ("readOnlyAccessGroupId".equals(property)) {
      teamCalDao.setReadOnlyAccessGroup(getData(), (Integer) selectedValue);
    } else if ("minimalAccessGroupId".equals(property)) {
      teamCalDao.setMinimalAccessGroup(getData(), (Integer) selectedValue);
    }
  }

  /**
   * @see #select
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    if ("fullAccessGroupId".equals(property)) {
      getData().setFullAccessGroup(null);
    } else if ("readOnlyAccessGroupId".equals(property)) {
      getData().setReadOnlyAccessGroup(null);
    } else if ("minimalAccessGroupId".equals(property)) {
      getData().setMinimalAccessGroup(null);
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  @Override
  public void cancelSelection(final String property)
  {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected TeamCalDao getBaseDao()
  {
    return teamCalDao;
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
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage,
   *      org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected TeamCalEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final TeamCalDO data)
  {
    return new TeamCalEditForm(this, data);
  }
}
