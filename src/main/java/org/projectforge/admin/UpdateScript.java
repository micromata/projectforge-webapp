/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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
import org.projectforge.scripting.GroovyResult;
import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlObject;

/**
 * Represents a update script (groovy) de-serialized from an uploaded file.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@XmlObject(alias = "update")
public class UpdateScript implements Serializable
{
  private static final long serialVersionUID = -1783353746958368966L;

  @XmlField(asAttribute = true)
  private String version;

  @XmlField(asAttribute = true, alias = "war-file")
  private String warFile;

  @XmlField(alias = "skip-version")
  private boolean skipVersion;

  private boolean experimental;

  @XmlField(alias = "pre-check", asCDATA = true)
  private String preCheck;

  @XmlField(asCDATA = true)
  private String script;

  private transient boolean visible;

  private transient UpdatePreCheckStatus preCheckStatus = UpdatePreCheckStatus.UNKNOWN;

  private transient GroovyResult preCheckResult;

  private transient UpdateRunningStatus runningStatus = UpdateRunningStatus.UNKNOWN;

  private transient GroovyResult runningResult;

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  /**
   * Name of the war file matching the new version for download.
   * @return
   */
  public String getWarFile()
  {
    return warFile;
  }

  public void setWarFile(String warFile)
  {
    this.warFile = warFile;
  }

  /**
   * Is it possible to skip this version while installing update files of different versions at once?
   */
  public boolean isSkipVersion()
  {
    return skipVersion;
  }

  public void setSkipVersion(boolean skipVersion)
  {
    this.skipVersion = skipVersion;
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

  public GroovyResult getPreCheckResult()
  {
    return preCheckResult;
  }

  public void setPreCheckResult(GroovyResult preCheckResult)
  {
    this.preCheckResult = preCheckResult;
  }

  public GroovyResult getRunningResult()
  {
    return runningResult;
  }

  public void setRunningResult(GroovyResult runningResult)
  {
    this.runningResult = runningResult;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  /**
   * This flag indicates that this script is not visible for productive systems.
   */
  public boolean isExperimental()
  {
    return experimental;
  }

  public void setExperimental(boolean experimental)
  {
    this.experimental = experimental;
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

  @Override
  public String toString()
  {
    final ReflectionToString tos = new ReflectionToString(this);
    return tos.toString();
  }
}
