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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
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
import org.projectforge.plugins.teamcal.TeamCalChoiceProvider;
import org.projectforge.plugins.teamcal.TeamCalDO;
import org.projectforge.plugins.teamcal.TeamCalDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.common.MultiChoiceListHelper;
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

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * 
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class CalendarForm extends AbstractForm<CalendarFilter, CalendarPage>
{
  private static final long serialVersionUID = -145923669780937370L;

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  private CalendarFilter filter;

  private GridBuilder gridBuilder;

  @SuppressWarnings("unused")
  private boolean showTimesheets;

  protected JodaDatePanel currentDatePanel;

  protected Label durationLabel;

  MultiChoiceListHelper<TeamCalDO> multipleTeamCalList;

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
    if (accessChecker.isRestrictedUser(getUser()) == false) {
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
    }
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
    if (accessChecker.isRestrictedUser() == false && WebConfiguration.isDevelopmentMode() == true) {
      final PFUserDO user = PFUserContext.getUser();
      final String authenticationKey = userDao.getAuthenticationToken(user.getId());
      final String contextPath = WebApplication.get().getServletContext().getContextPath();
      final String iCalTarget = contextPath
          + "/export/ProjectForge.ics?timesheetUser="
          + user.getUsername()
          + "&token="
          + authenticationKey;
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

    if (showTeamCalAddons()) {
      final List<TeamCalDO> list = teamCalDao.getTeamCalsByAccess(getUser(), TeamCalDao.FULL_ACCESS_GROUP,
          TeamCalDao.READONLY_ACCESS_GROUP, TeamCalDao.MINIMAL_ACCESS_GROUP);
      gridBuilder.newColumnPanel(DivType.COL_75);
      final FieldsetPanel listFieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.teamCal"), true);
      multipleTeamCalList = new MultiChoiceListHelper<TeamCalDO>().setComparator(new IdComparator()).setFullList(list);
      // schon ausgewählte teamcals -> aus teamcaldao z.b. full_access_groups
      // if (assignedTeamCals != null) {
      // for (final TeamCalDO cals : assignedTeamCals) {
      // multipleTeamCalList.addOriginalAssignedItem(cals).assignItem(cals);
      // }
      // }
      final TeamCalChoiceProvider teamProvider = new TeamCalChoiceProvider();
      final Select2MultiChoice<TeamCalDO> teamCalChoice = new Select2MultiChoice<TeamCalDO>(fs.getSelect2MultiChoiceId(),
          new PropertyModel<Collection<TeamCalDO>>(this.multipleTeamCalList, "assignedItems"), teamProvider);
      teamCalChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          if (multipleTeamCalList.getAssignedItems().isEmpty() == false) {
            filter.setAssignedtItems(multipleTeamCalList.getAssignedItems());
            setResponsePage(CalendarForm.this.getParentPage());
          }
        }
      });
      // listFieldSet.add(teamCalChoice);
      listFieldSet.setVisible(false);
    }

  }

  /**
   * TODO currently hide teamcal features
   * @return
   */
  private boolean showTeamCalAddons() {
    return false;
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

  /**
   * compare ids
   * 
   * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
   * 
   */
  private class IdComparator implements Comparator<TeamCalDO>, Serializable
  {

    private static final long serialVersionUID = 5501418454944208820L;

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final TeamCalDO arg0, final TeamCalDO arg1)
    {
      final Integer n1 = arg0.getId() != null ? arg0.getId() : 0;

      final Integer n2 = arg1.getId() != null ? arg1.getId() : 0;

      return n1.compareTo(n2);
    }
  }

  /**
   * @return the multipleTeamCalList
   */
  public MultiChoiceListHelper<TeamCalDO> getMultipleTeamCalList()
  {
    return multipleTeamCalList;
  }

  /**
   * @param multipleTeamCalList the multipleTeamCalList to set
   * @return this for chaining.
   */
  public void setMultipleTeamCalList(final MultiChoiceListHelper<TeamCalDO> multipleTeamCalList)
  {
    this.multipleTeamCalList = multipleTeamCalList;
  }
}
