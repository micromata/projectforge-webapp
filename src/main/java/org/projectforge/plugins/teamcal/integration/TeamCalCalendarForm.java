/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.projectforge.plugins.teamcal.dialog.TeamCalDialog;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.calendar.CalendarFilter;
import org.projectforge.web.calendar.CalendarForm;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class TeamCalCalendarForm extends CalendarForm
{

  private static final long serialVersionUID = -5838203593605203398L;

  private TeamCalCalendarFilter filter;

  /**
   * @param parentPage
   */
  public TeamCalCalendarForm(final CalendarPage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.calendar.CalendarForm#addControlButtons(org.projectforge.web.wicket.flowlayout.FieldsetPanel)
   */
  @Override
  protected void addControlButtons(final FieldsetPanel fs)
  {
    final TeamCalDialog dialog = new TeamCalDialog(fs.newChildId(), new ResourceModel("plugins.teamcal.title.list"), filter);
    fs.add(dialog);
    final IconButtonPanel calendarButtonPanel = new AjaxIconButtonPanel(fs.newChildId(), IconType.CALENDAR,
        getString("plugins.teamcal.title.list")) {
      private static final long serialVersionUID = -8572571785540159369L;

      /**
       * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        dialog.open(target);
      }
    };
    calendarButtonPanel.setLight();
    fs.add(calendarButtonPanel);
    setDefaultButton(calendarButtonPanel.getButton());
  }

  /**
   * @see org.projectforge.web.calendar.CalendarForm#setIcsImportButtonTooltip(java.lang.String)
   */
  @Override
  protected String setIcsImportButtonTooltip()
  {
    return "plugins.teamcal.subscribe.teamcalendar";
  }

  /**
   * @see org.projectforge.web.calendar.CalendarForm#setICalTarget()
   */
  @Override
  protected String setICalTarget()
  {
    final PFUserDO user = PFUserContext.getUser();
    final String authenticationKey = userDao.getAuthenticationToken(user.getId());
    final String contextPath = WebApplication.get().getServletContext().getContextPath();
    final String iCalTarget = contextPath
        + "/export/ProjectForge.ics?timesheetUser="
        + user.getUsername()
        + "&token="
        + authenticationKey
        + additionalInformation();
    return iCalTarget;
  }

  @Override
  public CalendarFilter getFilter()
  {
    if (this.filter == null && super.getFilter() != null) {
      return super.getFilter();
    }
    return filter;
  }

  @Override
  protected void setFilter(final CalendarFilter filter)
  {
    if (filter instanceof TeamCalCalendarFilter) {
      this.filter = (TeamCalCalendarFilter) filter;
    }
    super.setFilter(filter);
  }

  /**
   * @return the selectedCalendars
   */
  public Set<Integer> getSelectedCalendars()
  {
    return filter.getCalendarPk(filter.getCurrentCollection());
  }

  /**
   * add information to ics export url
   */
  @Override
  protected String additionalInformation()
  {
    String calendarIds = "";
    for (final Integer id : getSelectedCalendars())
      calendarIds = calendarIds + id + ";";

    final String additionals = "&teamCals="
        + calendarIds
        + "&timesheetRequired="
        + true;
    return additionals;
  }
}
