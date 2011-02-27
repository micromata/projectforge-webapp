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

import java.util.Date;

import org.projectforge.Version;
import org.projectforge.common.ReflectionToString;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlObject;

/**
 * Represents a update script (groovy) de-serialized from an xml file.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@XmlObject(alias = "update")
public class UpdateEntryScript extends UpdateEntry
{
  private static final long serialVersionUID = -1783353746958368966L;

  @XmlField
  private String regionId;

  @XmlField
  private Version version;

  @XmlField
  private Date date;

  @XmlField
  private String description;

  @XmlField(alias = "pre-check", asCDATA = true)
  private String preCheck;

  @XmlField(asCDATA = true)
  private String script;

  private transient GroovyResult preCheckResult;

  private transient GroovyResult runningResult;

  @Override
  public String getRegionId()
  {
    return this.regionId;
  }

  public void setRegionId(String regionId)
  {
    this.regionId = regionId;
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
  public Date getDate()
  {
    return this.date;
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

  /**
   * Groovy script containing pre-checks for a data base update. This script returns "OK" if the pre conditions are full-filled or an error
   * message otherwise.
   */
  public String getPreCheck()
  {
    return preCheck;
  }

  public void setPreCheck(String preCheck)
  {
    this.preCheck = preCheck;
  }

  /**
   * Groovy script containing a data base update and migration script. This script returns "OK" or an error message if errors occurred.
   */
  public String getScript()
  {
    return script;
  }

  public void setScript(String script)
  {
    this.script = script;
  }

  @Override
  public String getPreCheckResult()
  {
    return preCheckResult != null ? String.valueOf(preCheckResult.getResult()) : null;
  }

  public void setPreCheckResult(GroovyResult preCheckResult)
  {
    this.preCheckResult = preCheckResult;
  }

  public String getRunningResult()
  {
    return runningResult != null ? String.valueOf(runningResult.getResult()) : null;
  }

  public void setRunningResult(GroovyResult runningResult)
  {
    this.runningResult = runningResult;
  }

  @Override
  public String toString()
  {
    final ReflectionToString tos = new ReflectionToString(this);
    return tos.toString();
  }

  @Override
  public UpdatePreCheckStatus runPreCheck()
  {
    this.preCheckStatus = SystemUpdater.instance().runPreCheck(this);
    return this.preCheckStatus;
  }

  @Override
  public UpdateRunningStatus runUpdate()
  {
    this.runningStatus = SystemUpdater.instance().runUpdate(this);
    return this.runningStatus;
  }
}
