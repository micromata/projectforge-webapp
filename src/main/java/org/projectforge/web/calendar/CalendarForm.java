/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.calendar;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateMidnight;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

public class CalendarForm extends AbstractForm<CalendarFilter, CalendarPage>
{
  private static final long serialVersionUID = -145923669780937370L;

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private CalendarFilter filter;

  private GridBuilder gridBuilder;

  @SuppressWarnings("unused")
  private boolean showTimesheets;

  JodaDatePanel currentDatePanel;

  Label durationLabel;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    addFeedbackPanel();
    final RepeatingView repeater = new RepeatingView("flowform");
    add(repeater);
    gridBuilder = newGridBuilder(repeater);
    gridBuilder.newGrid16();
    gridBuilder.getPanel().addCssClasses(DivType.MARGIN_TOP_10); // Add additional margin at the top.
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_75);
    FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true).setNoLabelFor();
    if (isOtherUsersAllowed() == true) {
      final UserSelectPanel userSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(this, "timesheetsUser"),
          parentPage, "userId");
      fs.add(userSelectPanel);
      userSelectPanel.init().withAutoSubmit(true).setLabel(new Model<String>(getString("user")));
    }
    currentDatePanel = new JodaDatePanel(fs.newChildId(), new PropertyModel<DateMidnight>(filter, "startDate")).setAutosubmit(true);
    currentDatePanel.getDateField().setOutputMarkupId(true);
    fs.add(currentDatePanel);
    final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
    if (isOtherUsersAllowed() == false) {
      showTimesheets = filter.getUserId() != null;
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(this, "showTimesheets"),
          getString("calendar.option.timesheeets"), true) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.CheckBoxPanel#onSelectionChanged()
         */
        @Override
        protected void onSelectionChanged(final Boolean newSelection)
        {
          if (Boolean.TRUE.equals(newSelection) == true) {
            filter.setUserId(getUserId());
          } else {
            filter.setUserId(null);
          }
        }
      });
    }
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showPlanning"),
        getString("calendar.option.planning"), true).setTooltip(getString("calendar.option.planning.tooltip")));
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showBirthdays"),
        getString("calendar.option.birthdays"), true));
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showStatistics"),
        getString("calendar.option.statistics"), true).setTooltip(getString("calendar.option.statistics.tooltip")));
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "slot30"),
        getString("calendar.option.slot30"), true).setTooltip(getString("calendar.option.slot30.tooltip")));
    final DropDownChoice<Integer> firstHourDropDownChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(),
        new PropertyModel<Integer>(filter, "firstHour"), DateTimePanel.getHourOfDayRenderer().getValues(),
        DateTimePanel.getHourOfDayRenderer()) {
      /**
       * @see org.apache.wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
       */
      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return true;
      }
    };
    firstHourDropDownChoice.setNullValid(false);
    firstHourDropDownChoice.setRequired(true);
    WicketUtils.addTooltip(firstHourDropDownChoice, getString("calendar.option.firstHour.tooltip"));
    fs.add(firstHourDropDownChoice);
    {
      final IconButtonPanel refreshButtonPanel = new IconButtonPanel(fs.newChildId(), IconType.ARROW_REFRESH, getString("refresh"))
      .setLight();
      fs.add(refreshButtonPanel);
      setDefaultButton(refreshButtonPanel.getButton());
    }
    if (WebConfiguration.isDevelopmentMode() == true) {
      final PFUserDO user = PFUserContext.getUser();
      final String authenticationKey = userDao.getAuthenticationToken(user.getId());
      final String contextPath = WebApplication.get().getServletContext().getContextPath();
      final String iCalTarget = contextPath + "/DEVELOPMENT_MODE/NOT_YET_IMPLEMENTED/export/ical?timesheetUser=" + user.getUsername() + "&token=" + authenticationKey;
      final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, iCalTarget);
      final IconLinkPanel exportICalButtonPanel = new IconLinkPanel(fs.newChildId(), IconType.SUBSCRIPTION,
          getString("timesheet.iCalExport"), iCalExportLink).setLight();
      fs.add(exportICalButtonPanel);
    }
    gridBuilder.newColumnPanel(DivType.COL_25);
    fs = gridBuilder.newFieldset(getString("timesheet.duration")).setNoLabelFor();
    final DivTextPanel durationPanel = new DivTextPanel(fs.newChildId(), new Label(DivTextPanel.WICKET_ID, new Model<String>() {
      @Override
      public String getObject()
      {
        return parentPage.calendarPanel.getTotalTimesheetDuration();
      }
    }));
    durationLabel = durationPanel.getLabel4Ajax();
    fs.add(durationPanel);
  }

  private boolean isOtherUsersAllowed()
  {
    return accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
        ProjectForgeGroup.PROJECT_MANAGER);
  }

  public CalendarForm(final CalendarPage parentPage)
  {
    super(parentPage);
  }

  public CalendarFilter getFilter()
  {
    return filter;
  }

  void setFilter(final CalendarFilter filter)
  {
    this.filter = filter;
  }

  public PFUserDO getTimesheetsUser()
  {
    final Integer userId = filter.getUserId();
    return userId != null ? userGroupCache.getUser(userId) : null;
  }

  public void setTimesheetsUser(final PFUserDO user)
  {
    if (user == null) {
      filter.setUserId(null);
    } else {
      filter.setUserId(user.getId());
    }
  }
}
