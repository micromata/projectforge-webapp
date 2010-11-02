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

package org.projectforge.web.calendar;

import java.io.Serializable;
import java.util.Date;

import org.projectforge.user.PFUserContext;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Some settings for the SelectDateAction stored in the flow scope (configurable by the caller).
 */
@XStreamAlias("dateFilter")
public class CalendarFilter implements Serializable
{
  private static final long serialVersionUID = -4154764049316136395L;

  @XStreamAsAttribute
  private Date current;

  @XStreamAsAttribute
  private Boolean showBirthdays;

  @XStreamAsAttribute
  private Integer userId;

  public CalendarFilter()
  {
    current = new Date();
    userId = PFUserContext.getUserId();
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

  public boolean isShowBirthdays()
  {
    return showBirthdays == Boolean.TRUE;
  }

  public void setShowBirthdays(boolean showBirthdays)
  {
    this.showBirthdays = showBirthdays;
  }

  public Integer getUserId()
  {
    return userId;
  }

  public void setUserId(Integer userId)
  {
    this.userId = userId;
  }
}
