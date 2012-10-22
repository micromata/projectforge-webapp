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
import org.projectforge.plugins.teamcal.dialog.TeamCalDialog;
import org.projectforge.web.calendar.CalendarFilter;
import org.projectforge.web.calendar.CalendarForm;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

import de.micromata.wicket.ajax.AjaxCallback;

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
    dialog.setOnCloseCallback(new AjaxCallback() {
      private static final long serialVersionUID = 1L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        // TODO do not use response page, just update the calendar
        setResponsePage(getPage().getClass(), getPage().getPageParameters());
      }
    });
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
    return filter.getCalendarPk();
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
