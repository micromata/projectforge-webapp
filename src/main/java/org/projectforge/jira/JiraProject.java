/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.jira;

import java.io.Serializable;

import org.projectforge.core.UserPrefParameter;

/**
 * Favorite entry for a JIRA-Project.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class JiraProject implements Serializable
{
  private static final long serialVersionUID = -1573788962309994887L;

  @UserPrefParameter(i18nKey = "userPref.area.jira.project.pid", tooltipI18nKey = "userPref.area.jira.project.pid.tooltip", required = true)
  private Integer pid;

  /**
   * JIRA's project id (pid).
   */
  public Integer getPid()
  {
    return pid;
  }

  public void setPid(final Integer pid)
  {
    this.pid = pid;
  }
}
