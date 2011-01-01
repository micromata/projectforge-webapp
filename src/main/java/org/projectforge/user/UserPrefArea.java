/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.user;

import org.projectforge.fibu.KundeFavorite;
import org.projectforge.fibu.ProjektFavorite;
import org.projectforge.jira.JiraProject;
import org.projectforge.task.TaskFavorite;
import org.projectforge.timesheet.TimesheetDO;


/**
 * User preferences are supported by different areas. These areas are defined inside this enum.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public enum UserPrefArea
{
  KUNDE_FAVORITE(KundeFavorite.class, "kunde.favorite"), PROJEKT_FAVORITE(ProjektFavorite.class, "projekt.favorite"), TASK_FAVORITE(
      TaskFavorite.class, "task.favorite"), TIMESHEET_TEMPLATE(TimesheetDO.class, "timesheet.template"), USER_FAVORITE(UserFavorite.class,
      "user.favorite"), JIRA_PROJECT(JiraProject.class, "jira.project");

  private String key;

  private Class< ? > beanType;

  /**
   * The key will be used e. g. for i18n (only the suffix not the base i18n key).
   * @return
   */
  public String getKey()
  {
    return key;
  }

  /**
   * Get the whole i18n key.
   * @return
   */
  public String getI18nKey()
  {
    return "user.pref.area." + key;
  }

  /**
   * The type corresponding to this UserPrefArea. This is the bean for which the annotated fields are stored as UserPrefParameterDO's.
   * @return
   */
  public Class< ? > getBeanType()
  {
    return beanType;
  }

  UserPrefArea(final Class< ? > clazz, final String key)
  {
    this.beanType = clazz;
    this.key = key;
  }

  public boolean isIn(final UserPrefArea... userPrefAreas)
  {
    for (final UserPrefArea area : userPrefAreas) {
      if (this == area) {
        return true;
      }
    }
    return false;
  }
}
