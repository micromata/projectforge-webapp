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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.calendar.MonthHolder;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.TooltipImage;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

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
    final Link<Void> showBirthdaysButton = new Link<Void>("showBirthdays") {
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
    //add(showBirthdaysButton);
    showBirthdaysButton.add(new TooltipImage("showBirthdaysImage", getResponse(), WebConstants.IMAGE_BIRTHDAY,
        getString("tooltip.showBirthdays")));
    final Link<Void> hideBirthdaysButton = new Link<Void>("hideBirthdays") {
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
    //add(hideBirthdaysButton);
    hideBirthdaysButton.add(new TooltipImage("hideBirthdaysImage", getResponse(), WebConstants.IMAGE_BIRTHDAY_DELETE,
        getString("tooltip.hideBirthdays")));

    final PFUserDO user = PFUserContext.getUser();

    if (WebConfiguration.isDevelopmentMode() == true && StringUtils.isNotBlank(user.getStayLoggedInKey())) {
      final String contextPath = WebApplication.get().getServletContext().getContextPath();
      final String iCalTarget = contextPath + "/export/ical?user=" + user.getUsername() + "&key=" + user.getStayLoggedInKey();
      final ExternalLink exportCalendar = new ExternalLink("exportCalendar", iCalTarget);
      exportCalendar.add(new TooltipImage("exportCalendarImage", getResponse(), WebConstants.IMAGE_CALENDAR,
          getString("tooltip.exportCalendar")));
      //add(exportCalendar);
    } else {
      //add(new ExternalLink("exportCalendar", "invisible").setVisible(false));
    }

    showTimesheetFilterElements();
  }

  private boolean isOtherUsersAllowed()
  {
    return accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
        ProjectForgeGroup.PROJECT_MANAGER);
  }

  @SuppressWarnings("serial")
  private void showTimesheetFilterElements()
  {
    final UserSelectPanel userSelectPanel = new UserSelectPanel("timesheetsUser", new PropertyModel<PFUserDO>(this, "timesheetsUser"),
        parentPage, "userId") {
      @Override
      public boolean isVisible()
      {
        return isOtherUsersAllowed();
      }
    };
    add(userSelectPanel);
    userSelectPanel.init().withAutoSubmit(true).setLabel(new Model<String>(getString("user")));
    final IconButtonPanel toggleTimesheetsButton = new IconButtonPanel("toggleTimesheets", IconType.CLIPBOARD, new Model<String>() {
      @Override
      public String getObject()
      {
        if (getFilter().getUserId() == null) {
          return getString("calendar.tooltip.showTimesheeets");
        } else {
          return getString("calendar.tooltip.hideTimesheeets");
        }
      }
    }) {
      @Override
      public void onSubmit()
      {
        if (getFilter().getUserId() == null) {
          getFilter().setUserId(getUser().getId());
        } else {
          getFilter().setUserId(null);
        }
      }

      @Override
      public boolean isVisible()
      {
        return isOtherUsersAllowed() == false;
      }
    };
    // toggleTimesheetsButton.appendCssClass("shaded");
    add(toggleTimesheetsButton.setLight());
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

  private MonthHolder getMonthHolder()
  {
    return null;// parentPage.getMonthHolder();
  }

  public void refresh()
  {
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
