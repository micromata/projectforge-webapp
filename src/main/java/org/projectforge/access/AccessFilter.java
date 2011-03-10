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

package org.projectforge.access;

import java.io.Serializable;

import org.projectforge.core.BaseSearchFilter;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class AccessFilter extends BaseSearchFilter implements Serializable
{
  private static final long serialVersionUID = -1358813343070331969L;

  private Integer taskId, groupId, userId;

  private boolean recursive, inherit;

  public AccessFilter()
  {
  }

  public AccessFilter(final BaseSearchFilter filter)
  {
    super(filter);
  }

  public Integer getTaskId()
  {
    return taskId;
  }

  /**
   * @param taskId
   * @return this for chaining.
   */
  public AccessFilter setTaskId(Integer taskId)
  {
    this.taskId = taskId;
    return this;
  }

  public boolean isRecursive()
  {
    return recursive;
  }

  /**
   * @param recursive
   * @return this for chaining.
   */
  public AccessFilter setRecursive(boolean recursive)
  {
    this.recursive = recursive;
    return this;
  }

  public boolean isInherit()
  {
    return inherit;
  }

  /**
   * @param inherit
   * @return this for chaining.
   */
  public AccessFilter setInherit(boolean inherit)
  {
    this.inherit = inherit;
    return this;
  }

  public Integer getGroupId()
  {
    return groupId;
  }

  /**
   * @param groupId
   * @return this for chaining.
   */
  public AccessFilter setGroupId(Integer groupId)
  {
    this.groupId = groupId;
    return this;
  }

  /**
   * For checking the access rights for an user.
   */
  public Integer getUserId()
  {
    return userId;
  }

  /**
   * @param userId
   * @return this for chaining.
   */
  public AccessFilter setUserId(Integer userId)
  {
    this.userId = userId;
    return this;
  }
}
