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

package org.projectforge.core;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;

public class SearchFilter extends BaseSearchFilter
{
  private static final long serialVersionUID = 5850672386075331163L;

  private TaskDO task;

  private PFUserDO modifiedByUser;

  public SearchFilter()
  {
    this.maxRows = 3;
  }

  @Override
  public void setStartTimeOfLastModification(final Date startTimeOfLastModification)
  {
    if (startTimeOfLastModification == null) {
      super.setStartTimeOfLastModification(null);
      return;
    }
    final DateHolder dh = new DateHolder(startTimeOfLastModification, DatePrecision.MILLISECOND);
    dh.setBeginOfDay();
    super.setStartTimeOfLastModification(dh.getDate());
  }

  @Override
  public void setStopTimeOfLastModification(final Date stopTimeOfLastModification)
  {
    if (stopTimeOfLastModification == null) {
      super.setStopTimeOfLastModification(null);
      return;
    }
    final DateHolder dh = new DateHolder(stopTimeOfLastModification, DatePrecision.MILLISECOND);
    dh.setEndOfDay();
    super.setStopTimeOfLastModification(dh.getDate());
  }

  public PFUserDO getModifiedByUser()
  {
    return modifiedByUser;
  }

  public void setModifiedByUser(PFUserDO modifiedByUser)
  {
    this.modifiedByUser = modifiedByUser;
    this.modifiedByUserId = modifiedByUser != null ? modifiedByUser.getId() : null;
  }

  public TaskDO getTask()
  {
    return task;
  }

  public Integer getTaskId()
  {
    return task != null ? task.getId() : null;
  }

  public void setTask(TaskDO task)
  {
    this.task = task;
  }

  /**
   * @return true, if no field for search is set (ignores task).
   */
  public boolean isEmpty()
  {
    return StringUtils.isEmpty(searchString) == true
        && modifiedByUserId == null
        && startTimeOfLastModification == null
        && stopTimeOfLastModification == null;
  }

  public void reset()
  {
    super.reset();
    this.searchString = "";
    this.useModificationFilter = false;
    this.modifiedByUserId = null;
    this.startTimeOfLastModification = null;
    this.stopTimeOfLastModification = null;
    this.task = null;
  }
}
