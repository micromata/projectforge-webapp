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

import org.projectforge.Version;
import org.projectforge.common.ReflectionToString;

/**
 * Represents a update (written in Java).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class UpdateEntryImpl extends UpdateEntry
{
  private static final long serialVersionUID = -1178486631632477422L;

  private Version version;

  private String description;

  public UpdateEntryImpl()
  {
  }

  public UpdateEntryImpl(final String versionString, final String description)
  {
    this.version = new Version(versionString);
    this.description = description;
  }

  @Override
  public Version getVersion()
  {
    return version;
  }

  @Override
  public void setVersion(final Version version)
  {
    this.version = version;
  }

  @Override
  public String getDescription()
  {
    return this.description;
  }

  @Override
  public void setDescription(final String description)
  {
    this.description = description;
  }

  @Override
  public String getPreCheckResult()
  {
    return this.preCheckStatus != null ? this.preCheckStatus.toString() : "";
  }

  @Override
  public String getRunningResult()
  {
    return this.runningStatus != null ? this.runningStatus.toString() : "";
  }

  @Override
  public String toString()
  {
    final ReflectionToString tos = new ReflectionToString(this);
    return tos.toString();
  }
}
