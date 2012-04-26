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

import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;

public class CalendarForm extends AbstractForm<CalendarFilter, CalendarPage>
{
  private static final long serialVersionUID = -145923669780937370L;

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private CalendarFilter filter;

  private GridBuilder gridBuilder;

  @SuppressWarnings("unused")
  private boolean showTimesheets;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    addFeedbackPanel();
    final RepeatingView repeater = new RepeatingView("flowform");
    add(repeater);
    gridBuilder = newGridBuilder(repeater);
    gridBuilder.newGrid16().newColumnsPanel().newColumnPanel(DivType.COL_75);
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true).setNoLabelFor();
    if (isOtherUsersAllowed() == true) {
      final UserSelectPanel userSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(this, "timesheetsUser"),
          parentPage, "userId");
      fs.add(userSelectPanel);
      userSelectPanel.init().withAutoSubmit(true).setLabel(new Model<String>(getString("user")));
    } else {
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.add(new CheckBoxPanel(DivPanel.CHILD_ID, new PropertyModel<Boolean>(this, "showTimesheets"),
          getString("calendar.timesheeets")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.CheckBoxPanel#wantOnSelectionChangedNotifications()
         */
        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
          return true;
        }

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
    //    gridBuilder.newColumnPanel(DivType.COL_25);
    //    final DivTextPanel durationPanel = new DivTextPanel(gridBuilder.getPanel().CHILD_ID, new Label(DivTextPanel.WICKET_ID, new Model<String>() {
    //      @Override
    //      public String getObject()
    //      {
    //        if (StringUtils.isEmpty(parentPage.getFormattedMonthDuration()) == true) {
    //          return "";
    //        }
    //        return parentPage.getFormattedMonthDuration()
    //            + " ("
    //            + DateTimeFormatter.instance().getFormattedDate(getMonthHolder().getBegin(),
    //                DateFormats.getFormatString(DateFormatType.DATE_WITHOUT_YEAR))
    //                + "-"
    //                + DateTimeFormatter.instance().getFormattedDate(getMonthHolder().getEnd(),
    //                    DateFormats.getFormatString(DateFormatType.DATE_WITHOUT_YEAR))
    //                    + ")";
    //      }
    //    }));
    // final Link<Void> showBirthdaysButton = new Link<Void>("showBirthdays") {
    // @Override
    // public void onClick()
    // {
    // getFilter().setShowBirthdays(true);
    // }
    //
    // @Override
    // public boolean isVisible()
    // {
    // return !getFilter().isShowBirthdays();
    // }
    // };
    // // add(showBirthdaysButton);
    // showBirthdaysButton.add(new TooltipImage("showBirthdaysImage", getResponse(), WebConstants.IMAGE_BIRTHDAY,
    // getString("tooltip.showBirthdays")));
    // final Link<Void> hideBirthdaysButton = new Link<Void>("hideBirthdays") {
    // @Override
    // public void onClick()
    // {
    // getFilter().setShowBirthdays(false);
    // }
    //
    // @Override
    // public boolean isVisible()
    // {
    // return getFilter().isShowBirthdays();
    // }
    // };
    // // add(hideBirthdaysButton);
    // hideBirthdaysButton.add(new TooltipImage("hideBirthdaysImage", getResponse(), WebConstants.IMAGE_BIRTHDAY_DELETE,
    // getString("tooltip.hideBirthdays")));
    //
    // final PFUserDO user = PFUserContext.getUser();
    //
    // if (WebConfiguration.isDevelopmentMode() == true && StringUtils.isNotBlank(user.getStayLoggedInKey())) {
    // final String contextPath = WebApplication.get().getServletContext().getContextPath();
    // final String iCalTarget = contextPath + "/export/ical?user=" + user.getUsername() + "&key=" + user.getStayLoggedInKey();
    // final ExternalLink exportCalendar = new ExternalLink("exportCalendar", iCalTarget);
    // exportCalendar.add(new TooltipImage("exportCalendarImage", getResponse(), WebConstants.IMAGE_CALENDAR,
    // getString("tooltip.exportCalendar")));
    // // add(exportCalendar);
    // } else {
    // // add(new ExternalLink("exportCalendar", "invisible").setVisible(false));
    // }
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
}
