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

package org.projectforge.admin;

import java.io.Serializable;

import org.projectforge.Version;
import org.projectforge.common.ReflectionToString;

/**
 * Represents a update entry (Groovy or Java).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class UpdateEntry implements Serializable, Comparable<UpdateEntry>
{
  private static final long serialVersionUID = -8205244215928531249L;

  protected transient UpdatePreCheckStatus preCheckStatus = UpdatePreCheckStatus.UNKNOWN;

  protected transient UpdateRunningStatus runningStatus;

  public abstract Version getVersion();

  public abstract void setVersion(final Version version);

  /**
   * Should be of iso format: 2011-02-28 (yyyy-MM-dd).
   */
  public abstract String getDate();

  /**
   * Identifier of the software region: core for ProjectForge core or plugin identifier.
   */
  public abstract String getRegionId();

  public UpdatePreCheckStatus getPreCheckStatus()
  {
    return preCheckStatus;
  }

  public void setPreCheckStatus(UpdatePreCheckStatus preCheckStatus)
  {
    this.preCheckStatus = preCheckStatus;
  }

  public UpdateRunningStatus getRunningStatus()
  {
    return runningStatus;
  }

  public void setRunningStatus(UpdateRunningStatus runningStatus)
  {
    this.runningStatus = runningStatus;
  }

  public abstract String getDescription();

  public abstract void setDescription(final String description);

  public abstract UpdatePreCheckStatus runPreCheck();

  public abstract UpdateRunningStatus runUpdate();

  public abstract String getPreCheckResult();

  public abstract String getRunningResult();

  @Override
  public String toString()
  {
    final ReflectionToString tos = new ReflectionToString(this);
    return tos.toString();
  }

  /**
   * Compares the dates of the both entries in descending order. For equal dates, the region id and the version is compared.
   * @param o
   */
  @Override
  public int compareTo(final UpdateEntry o)
  {
    int res = o.getDate().compareTo(getDate());
    if (res != 0) {
      return res;
    }
    res = o.getRegionId().compareTo(getRegionId());
    if (res != 0) {
      return res;
    }
    return o.getVersion().compareTo(getVersion());
  }
}
