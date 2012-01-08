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

package org.projectforge.web.calendar;

import java.io.Serializable;
import java.util.Date;

/**
 * Some settings for the SelectDateAction stored in the flow scope (configurable by the caller).
 */
public class SelectDateActionSettings implements Serializable
{
  private static final long serialVersionUID = -6770691822755824722L;

  private Date current;

  private String periodStart;

  private String periodStop;

  private Boolean showTimesheets;

  private Boolean showBirthdays;

  public SelectDateActionSettings()
  {
    current = new Date();
  }

  public Date getCurrent()
  {
    return current;
  }

  public void setCurrent(Date current)
  {
    if (current != null) {
    this.current = current;
    } else {
      this.current = new Date();
    }
  }

  public String getPeriodStart()
  {
    return periodStart;
  }

  public void setPeriodStart(String periodStart)
  {
    this.periodStart = periodStart;
  }

  public String getPeriodStop()
  {
    return periodStop;
  }

  public void setPeriodStop(String periodStop)
  {
    this.periodStop = periodStop;
  }

  public boolean isShowBirthdays()
  {
    return showBirthdays == Boolean.TRUE;
  }

  public void setShowBirthdays(boolean showBirthdays)
  {
    this.showBirthdays = showBirthdays;
  }

  public boolean isShowTimesheets()
  {
    return showTimesheets == Boolean.TRUE;
  }

  public void setShowTimesheets(boolean showTimesheets)
  {
    this.showTimesheets = showTimesheets;
  }
}
