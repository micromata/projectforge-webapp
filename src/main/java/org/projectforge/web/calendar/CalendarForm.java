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

package org.projectforge.web.calendar;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateMidnight;
import org.projectforge.access.AccessChecker;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridBuilder;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.flowlayout.ButtonGroupPanel;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.ComponentSize;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
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
  protected UserDao userDao;

  private CalendarFilter filter;

  private GridBuilder gridBuilder;

  @SuppressWarnings("unused")
  private boolean showTimesheets;

  private JodaDatePanel currentDatePanel;

  private Label durationLabel;

  protected ButtonGroupPanel buttonGroupPanel;

  protected FieldsetPanel fieldset;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    addFeedbackPanel();
    gridBuilder = new GridBuilder(this, "grid", getMySession());
    gridBuilder.newSplitPanel(GridSize.SPAN8);
    fieldset = gridBuilder.newFieldset(getString("label.options"), true);
    if (isOtherUsersAllowed() == true) {
      final UserSelectPanel userSelectPanel = new UserSelectPanel(fieldset.newChildId(),
          new PropertyModel<PFUserDO>(this, "timesheetsUser"), parentPage, "userId");
      fieldset.add(userSelectPanel);
      userSelectPanel.init().withAutoSubmit(true).setLabel(new Model<String>(getString("user")));
    }
    currentDatePanel = new JodaDatePanel(fieldset.newChildId(), new PropertyModel<DateMidnight>(filter, "startDate")).setAutosubmit(true);
    currentDatePanel.getDateField().setOutputMarkupId(true);
    fieldset.add(currentDatePanel);

    final DivPanel checkBoxPanel = fieldset.addNewCheckBoxDiv();
    if (accessChecker.isRestrictedUser(getUser()) == false) {
      if (isOtherUsersAllowed() == false) {
        showTimesheets = getFilter().getUserId() != null;
        checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(this, "showTimesheets"),
            getString("calendar.option.timesheeets"), true) {
          /**
           * @see org.projectforge.web.wicket.flowlayout.CheckBoxPanel#onSelectionChanged()
           */
          @Override
          protected void onSelectionChanged(final Boolean newSelection)
          {
            if (Boolean.TRUE.equals(newSelection) == true) {
              getFilter().setUserId(getUserId());
            } else {
              getFilter().setUserId(null);
            }
          }
        });
      }
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showBreaks"),
          getString("calendar.option.showBreaks"), true).setTooltip(getString("calendar.option.showBreaks.tooltip")));
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showPlanning"),
          getString("calendar.option.planning"), true).setTooltip(getString("calendar.option.planning.tooltip")));
      if (Configuration.getInstance().isAddressManagementConfigured() == true) {
        checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showBirthdays"),
            getString("calendar.option.birthdays"), true));
      }
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showStatistics"),
          getString("calendar.option.statistics"), true).setTooltip(getString("calendar.option.statistics.tooltip")));
    }
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "slot30"),
        getString("calendar.option.slot30"), true).setTooltip(getString("calendar.option.slot30.tooltip")));

    final DropDownChoice<Integer> firstHourDropDownChoice = new DropDownChoice<Integer>(fieldset.getDropDownChoiceId(),
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
    WicketUtils.setSize(firstHourDropDownChoice, ComponentSize.MINI);
    WicketUtils.addTooltip(firstHourDropDownChoice, getString("calendar.option.firstHour.tooltip"));
    fieldset.add(firstHourDropDownChoice);
    buttonGroupPanel = new ButtonGroupPanel(fieldset.newChildId());
    fieldset.add(buttonGroupPanel);
    {
      final IconButtonPanel refreshButtonPanel = new IconButtonPanel(buttonGroupPanel.newChildId(), IconType.REFRESH, getString("refresh")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.IconButtonPanel#onSubmit()
         */
        @Override
        protected void onSubmit()
        {
          setResponsePage(getPage().getClass(), getPage().getPageParameters());
        }
      };
      buttonGroupPanel.addButton(refreshButtonPanel);
      setDefaultButton(refreshButtonPanel.getButton());
    }
    if (accessChecker.isRestrictedUser() == false && WebConfiguration.isDevelopmentMode() == true) {
      final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, new Model<String>() {
        @Override
        public String getObject()
        {
          final PFUserDO timesheetUser = getTimesheetsUser();
          final String iCalTarget = CalendarFeed.getUrl4Timesheets(timesheetUser != null ? timesheetUser.getId() : null);
          return iCalTarget;
        };
      }) {
        @Override
        public boolean isVisible()
        {
          return getTimesheetsUser() != null;
        };
      };
      final IconLinkPanel exportICalButtonPanel = new IconLinkPanel(buttonGroupPanel.newChildId(), IconType.DOWNLOAD,
          getString(setIcsImportButtonTooltip()), iCalExportLink);

      buttonGroupPanel.addButton(exportICalButtonPanel);
    }
    gridBuilder.newSplitPanel(GridSize.SPAN4);
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("timesheet.duration"), true);
    final DivTextPanel durationPanel = new DivTextPanel(fs.newChildId(), new Label(DivTextPanel.WICKET_ID, new Model<String>() {
      @Override
      public String getObject()
      {
        return parentPage.calendarPanel.getTotalTimesheetDuration();
      }
    }));
    durationLabel = durationPanel.getLabel4Ajax();
    fs.add(durationPanel);
    onAfterInit(gridBuilder);
  }

  /**
   * If it is necessary to change the tool tip.
   * 
   * @param i18nKey
   * @return
   */
  protected String setIcsImportButtonTooltip()
  {
    return "timesheet.iCalExport";
  }

  /**
   * Hook method where child implementations could add buttons
   * 
   * @param fs
   */
  protected void addControlButtons(final FieldsetPanel fs)
  {
    // by default nothing happens here
  }

  /**
   * Hook method where child implementations could place their logic
   * 
   * @param gridBuilder
   */
  protected void onAfterInit(final GridBuilder gridBuilder)
  {
    // by default nothing happens here
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

  protected void setFilter(final CalendarFilter filter)
  {
    this.filter = filter;
  }

  public PFUserDO getTimesheetsUser()
  {
    final Integer userId = getFilter().getUserId();
    return userId != null ? userGroupCache.getUser(userId) : null;
  }

  public void setTimesheetsUser(final PFUserDO user)
  {
    if (user == null) {
      getFilter().setUserId(null);
    } else {
      getFilter().setUserId(user.getId());
    }
  }

  /**
   * @return the currentDatePanel
   */
  public JodaDatePanel getCurrentDatePanel()
  {
    return currentDatePanel;
  }

  /**
   * @return the durationLabel
   */
  public Label getDurationLabel()
  {
    return durationLabel;
  }

}
