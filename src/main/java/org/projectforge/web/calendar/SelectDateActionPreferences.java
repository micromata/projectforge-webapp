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

package org.projectforge.web.calendar;

import java.io.Serializable;
import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Some settings for the SelectDateAction stored in the flow scope (configurable by the caller) and persisted in the data base.
 */
@XStreamAlias("selectDateActionSettings")
public class SelectDateActionPreferences implements Serializable
{
  private static final long serialVersionUID = -6770691822755824722L;

  @XStreamAsAttribute
  private Date current;

  @XStreamAsAttribute
  private Boolean showBirthdays;

  public Date getCurrent()
  {
    return current;
  }

  public void setCurrent(Date current)
  {
    if (this.current == null) {
      this.current = new Date();
    }
    this.current = current;
  }

  /**
   * If true then birthday will be shown in the normal calendar view.
   * @return
   */
  public boolean isShowBirthdays()
  {
    return showBirthdays;
  }

  public void setShowBirthdays(boolean showBirthdays)
  {
    this.showBirthdays = showBirthdays;
  }
}
