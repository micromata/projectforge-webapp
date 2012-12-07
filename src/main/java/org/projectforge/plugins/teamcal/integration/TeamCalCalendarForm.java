/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.projectforge.plugins.teamcal.dialog.TeamCalDialog;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.calendar.CalendarFilter;
import org.projectforge.web.calendar.CalendarForm;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
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
        getString("plugins.teamcal.calendar.edit")) {
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

    if (filter.getCurrentCollection() != null) {
      final IChoiceRenderer<TeamCalCalendarCollection> teamCalCollectionRenderer = new IChoiceRenderer<TeamCalCalendarCollection>() {
        private static final long serialVersionUID = 4804134958242438331L;

        @Override
        public String getIdValue(final TeamCalCalendarCollection object, final int index)
        {
          return object.getTeamCalCalendarColletionName();
        }

        @Override
        public Object getDisplayValue(final TeamCalCalendarCollection object)
        {
          return object.getTeamCalCalendarColletionName();
        }
      };

      final IModel<List<TeamCalCalendarCollection>> choicesModel = new PropertyModel<List<TeamCalCalendarCollection>>(filter,
          "teamCalCalendarCollection");
      final IModel<TeamCalCalendarCollection> currentModel = new PropertyModel<TeamCalCalendarCollection>(filter, "currentCollection");
      final DropDownChoicePanel<TeamCalCalendarCollection> collectionChoice = new DropDownChoicePanel<TeamCalCalendarCollection>(
          fs.newChildId(), currentModel, choicesModel, teamCalCollectionRenderer, false);
      fs.add(collectionChoice);
      collectionChoice.getDropDownChoice().setOutputMarkupId(true);

      collectionChoice.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 8999698636114154230L;

        /**
         * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          setResponsePage(getParentPage().getClass());
        }
      });
    }
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

    final String additionals = "&teamCals=" + calendarIds + "&timesheetRequired=" + true;
    return additionals;
  }
}
