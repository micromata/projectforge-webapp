/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.calendar.MonthHolder;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.TooltipImage;

public class CalendarForm extends AbstractForm<CalendarFilter, CalendarPage>
{
  private static final long serialVersionUID = -145923669780937370L;

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private CalendarFilter filter;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    @SuppressWarnings("unchecked")
    final Link< ? > previousMonthButton = new Link("previousMonth") {
      @Override
      public void onClick()
      {
        parentPage.goToPreviousMonth();
      }
    };
    WicketUtils.addTooltip(previousMonthButton, getString("calendar.tooltip.selectPrevious"), true);
    add(previousMonthButton);
    // previousMonthButton.add(new TooltipImage("previousMonthImage", getResponse(), WebConstants.IMAGE_CALENDAR_PREVIOUS_MONTH,
    // getString("calendar.tooltip.selectPrevious")));
    @SuppressWarnings("unchecked")
    final Link< ? > nextMonthButton = new Link("nextMonth") {
      @Override
      public void onClick()
      {
        parentPage.goToNextMonth();
      }
    };
    WicketUtils.addTooltip(nextMonthButton, getString("calendar.tooltip.selectNext"), true);
    add(nextMonthButton);
    // nextMonthButton.add(new TooltipImage("nextMonthImage", getResponse(), WebConstants.IMAGE_CALENDAR_NEXT_MONTH,
    // getString("calendar.tooltip.selectNext")));
    @SuppressWarnings("unchecked")
    final Model monthLabelModel = new Model<String>() {
      @Override
      public String getObject()
      {
        return getString("calendar.month." + getMonthHolder().getMonthKey()) + "&nbsp;" + getMonthHolder().getYear();
      }
    };
    final Label monthLabel = new Label("monthLabel", monthLabelModel);
    monthLabel.setEscapeModelStrings(false);
    add(monthLabel);
    @SuppressWarnings("unchecked")
    final Link< ? > selectMonthButton = new Link("selectMonth") {
      @Override
      public void onClick()
      {
        parentPage.onSelectPeriod(getMonthHolder().getBegin(), getMonthHolder().getEnd());
      }
    };
    WicketUtils.addTooltip(selectMonthButton, getString("calendar.tooltip.selectMonth"), true);
    add(selectMonthButton);
    final Label monthSelectLabel = new Label("monthSelectLabel", monthLabelModel);
    monthSelectLabel.setEscapeModelStrings(false);
    selectMonthButton.add(monthSelectLabel);
    if (isSelectMode() == true && parentPage.isSelectPeriodMode() == true) {
      monthLabel.setVisible(false);
    } else {
      selectMonthButton.setVisible(false);
    }

    @SuppressWarnings("unchecked")
    final Link showTodayButton = new Link("showToday") {
      @Override
      public void onClick()
      {
        parentPage.goToToday();
      }
    };
    WicketUtils.addTooltip(showTodayButton, getString("calendar.today"), true);
    add(showTodayButton);
    @SuppressWarnings("unchecked")
    final Link< ? > cancelButton = new Link("cancel") {
      @Override
      public void onClick()
      {
        getParentPage().onCancel();
      }
    };
    WicketUtils.addTooltip(cancelButton, getString("cancel"), true);
    add(cancelButton);
    // cancelButton.add(new PresizedImage("cancelImage", getResponse(), WebConstants.IMAGE_BUTTON_CANCEL));
    if (isSelectMode() == false) {
      cancelButton.setVisible(false);
    }
    add(new Label("monthDuration", new Model<String>() {
      @Override
      public String getObject()
      {
        if (StringUtils.isEmpty(parentPage.getFormattedMonthDuration()) == true) {
          return "";
        }
        return parentPage.getFormattedMonthDuration()
            + " ("
            + DateTimeFormatter.instance().getFormattedDate(getMonthHolder().getBegin(), DateTimeFormatter.I18N_KEY_DATE_NONE_YEAR_FORMAT)
            + "-"
            + DateTimeFormatter.instance().getFormattedDate(getMonthHolder().getEnd(), DateTimeFormatter.I18N_KEY_DATE_NONE_YEAR_FORMAT)
            + ")";
      }
    }));

    @SuppressWarnings("unchecked")
    final Link< ? > showBirthdaysButton = new Link("showBirthdays") {
      @Override
      public void onClick()
      {
        getFilter().setShowBirthdays(true);
      }

      @Override
      public boolean isVisible()
      {
        return !getFilter().isShowBirthdays();
      }
    };
    add(showBirthdaysButton);
    showBirthdaysButton.add(new TooltipImage("showBirthdaysImage", getResponse(), WebConstants.IMAGE_BIRTHDAY,
        getString("tooltip.showBirthdays")));
    @SuppressWarnings("unchecked")
    final Link< ? > hideBirthdaysButton = new Link("hideBirthdays") {
      @Override
      public void onClick()
      {
        getFilter().setShowBirthdays(false);
      }

      @Override
      public boolean isVisible()
      {
        return getFilter().isShowBirthdays();
      }
    };
    add(hideBirthdaysButton);
    hideBirthdaysButton.add(new TooltipImage("hideBirthdaysImage", getResponse(), WebConstants.IMAGE_BIRTHDAY_DELETE,
        getString("tooltip.hideBirthdays")));
    showTimesheetFilterElements();
  }

  private boolean isOtherUsersAllowed()
  {
    return accessChecker.isUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
        ProjectForgeGroup.PROJECT_MANAGER);
  }

  @SuppressWarnings("serial")
  private void showTimesheetFilterElements()
  {
    @SuppressWarnings("unchecked")
    final Link< ? > showTimesheetsButton = new Link("showTimesheets") {
      @Override
      public void onClick()
      {
        getFilter().setUserId(getUser().getId());
      }

      @Override
      public boolean isVisible()
      {
        return isOtherUsersAllowed() == false && getFilter().getUserId() == null;
      }
    };
    WicketUtils.addTooltip(showTimesheetsButton, getString("calendar.tooltip.showTimesheeets"), true);
    add(showTimesheetsButton);
    // showTimesheetsButton.add(new PresizedImage("showTimesheetsImage", getResponse(), WebConstants.IMAGE_CLOCK));
    @SuppressWarnings("unchecked")
    final Link< ? > hideTimesheetsButton = new Link("hideTimesheets") {
      @Override
      public void onClick()
      {
        getFilter().setUserId(null);
      }

      @Override
      public boolean isVisible()
      {
        return isOtherUsersAllowed() == false && getFilter().getUserId() != null;
      }
    };
    WicketUtils.addTooltip(hideTimesheetsButton,getString( "calendar.tooltip.hideTimesheeets"), true);
    add(hideTimesheetsButton);
    // hideTimesheetsButton.add(new PresizedImage("hideTimesheetsImage", getResponse(), WebConstants.IMAGE_CLOCK_DELETE));

    UserSelectPanel userSelectPanel = new UserSelectPanel("timesheetsUser", new PropertyModel<PFUserDO>(this, "timesheetsUser"),
        parentPage, "userId") {
      @Override
      public boolean isVisible()
      {
        return isOtherUsersAllowed();
      }
    };
    add(userSelectPanel);
    userSelectPanel.init().withAutoSubmit(true);
  }

  public CalendarForm(CalendarPage parentPage)
  {
    super(parentPage);
  }

  public CalendarFilter getFilter()
  {
    return filter;
  }

  void setFilter(CalendarFilter filter)
  {
    this.filter = filter;
  }

  private MonthHolder getMonthHolder()
  {
    return parentPage.getMonthHolder();
  }

  private boolean isSelectMode()
  {
    return parentPage.isSelectMode();
  }

  public void refresh()
  {
  }

  public PFUserDO getTimesheetsUser()
  {
    final Integer userId = getFilter().getUserId();
    return userId != null ? userGroupCache.getUser(userId) : null;
  }

  public void setTimesheetsUser(PFUserDO user)
  {
    if (user == null) {
      getFilter().setUserId(null);
    } else {
      getFilter().setUserId(user.getId());
    }
  }
}
