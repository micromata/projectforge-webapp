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

import net.ftlines.wicket.fullcalendar.ViewType;

import org.joda.time.DateMidnight;
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
  private DateMidnight startDate;

  @XStreamAsAttribute
  private Date current;

  @XStreamAsAttribute
  private Boolean showBirthdays;

  @XStreamAsAttribute
  private Integer userId;

  @XStreamAsAttribute
  private ViewType viewType;

  public CalendarFilter()
  {
    startDate = new DateMidnight();
    userId = PFUserContext.getUserId();
  }

  /**
   * @return the startDate
   */
  public DateMidnight getStartDate()
  {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   * @return this for chaining.
   */
  public void setStartDate(final DateMidnight startDate)
  {
    if (startDate != null) {
      this.startDate = startDate.plusDays(1);
    } else {
      this.startDate = new DateMidnight();
    }
  }

  public boolean isShowBirthdays()
  {
    return showBirthdays == Boolean.TRUE;
  }

  public void setShowBirthdays(final boolean showBirthdays)
  {
    this.showBirthdays = showBirthdays;
  }

  public Integer getUserId()
  {
    return userId;
  }

  public void setUserId(final Integer userId)
  {
    this.userId = userId;
  }

  /**
   * @return the viewType
   */
  public ViewType getViewType()
  {
    return viewType != null ? viewType : ViewType.AGENDA_WEEK;
  }

  /**
   * @param viewType the viewType to set
   * @return this for chaining.
   */
  public CalendarFilter setViewType(final ViewType viewType)
  {
    this.viewType = viewType;
    return this;
  }

  /**
   * Was used from old calendar.
   */
  @Deprecated
  public Date getCurrent()
  {
    return null;
  }

  @Deprecated
  public void setCurrent(final Date current)
  {
    this.current = current;
  }
}
