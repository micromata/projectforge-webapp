/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRights;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class CalendarPageSupport implements Serializable
{
  private static final long serialVersionUID = 8223888624685052575L;

  private final PFUserDO user;

  private final ISelectCallerPage parentPage;

  private boolean showTimsheetsSelectors = true, showOptions = true;

  private final CalendarFilter filter;

  private UserSelectPanel userSelectPanel;

  public CalendarPageSupport(final ISelectCallerPage parentPage, final CalendarFilter filter)
  {
    this.parentPage = parentPage;
    this.filter = filter;
    this.user = PFUserContext.getUser();
  }

  public UserSelectPanel addUserSelectPanel(final FieldsetPanel fieldset, final IModel<PFUserDO> model, final boolean autosubmit)
  {
    if (showTimsheetsSelectors == false || isOtherUsersAllowed() == false) {
      return null;
    }
    userSelectPanel = new UserSelectPanel(fieldset.newChildId(), model, parentPage, "userId");
    fieldset.add(userSelectPanel);
    userSelectPanel.init().withAutoSubmit(autosubmit).setLabel(new Model<String>(fieldset.getString("user")));
    return userSelectPanel;
  }

  @SuppressWarnings("serial")
  public void addOptions(final DivPanel checkBoxPanel, final IModel<Boolean> showTimesheetsModel)
  {
    if (UserRights.getAccessChecker().isRestrictedUser(user) == true || showOptions == false) {
      return;
    }
    if (isOtherUsersAllowed() == false) {
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), showTimesheetsModel, checkBoxPanel
          .getString("calendar.option.timesheeets"), true) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.CheckBoxPanel#onSelectionChanged()
         */
        @Override
        protected void onSelectionChanged(final Boolean newSelection)
        {
          if (Boolean.TRUE.equals(newSelection) == true) {
            filter.setUserId(user.getId());
          } else {
            filter.setUserId(null);
          }
        }
      });
    }
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showBreaks"), checkBoxPanel
        .getString("calendar.option.showBreaks"), true).setTooltip(checkBoxPanel.getString("calendar.option.showBreaks.tooltip")));
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showPlanning"), checkBoxPanel
        .getString("calendar.option.planning"), true).setTooltip(checkBoxPanel.getString("calendar.option.planning.tooltip")));
    if (Configuration.getInstance().isAddressManagementConfigured() == true) {
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showBirthdays"), checkBoxPanel
          .getString("calendar.option.birthdays"), true));
    }
    checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "showStatistics"), checkBoxPanel
        .getString("calendar.option.statistics"), true).setTooltip(checkBoxPanel.getString("calendar.option.statistics.tooltip")));
  }

  private boolean isOtherUsersAllowed()
  {
    return UserRights.getAccessChecker().isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
        ProjectForgeGroup.PROJECT_MANAGER);
  }

  /**
   * @param showOptions the showOptions to set
   * @return this for chaining.
   */
  public CalendarPageSupport setShowOptions(final boolean showOptions)
  {
    this.showOptions = showOptions;
    return this;
  }

  /**
   * @param showTimsheetsSelectors the showTimsheetsSelectors to set
   * @return this for chaining.
   */
  public CalendarPageSupport setShowTimsheetsSelectors(final boolean showTimsheetsSelectors)
  {
    this.showTimsheetsSelectors = showTimsheetsSelectors;
    return this;
  }

  /**
   * @return the userSelectPanel
   */
  public UserSelectPanel getUserSelectPanel()
  {
    return userSelectPanel;
  }
}
