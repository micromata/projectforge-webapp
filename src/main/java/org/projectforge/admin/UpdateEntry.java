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

package org.projectforge.admin;

import java.io.Serializable;

import org.projectforge.common.ReflectionToString;
import org.projectforge.xml.stream.XmlField;

/**
 * Represents a update entry (Groovy or Java).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class UpdateEntry implements Serializable
{
  private static final long serialVersionUID = -8205244215928531249L;

  @XmlField(asAttribute = true)
  private String version;

  @XmlField
  private String description;

  protected transient UpdatePreCheckStatus preCheckStatus = UpdatePreCheckStatus.UNKNOWN;

  protected transient UpdateRunningStatus runningStatus = UpdateRunningStatus.UNKNOWN;

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }
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

  public String getDescription()
  {
    return description;
  }
  
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
}
