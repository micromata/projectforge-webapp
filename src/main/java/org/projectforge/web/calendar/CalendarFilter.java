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

  /**
   * Was used by old calendar.
   */
  @SuppressWarnings("unused")
  @XStreamAsAttribute
  @Deprecated
  private Date current;

  @XStreamAsAttribute
  private Boolean showBirthdays;

  @XStreamAsAttribute
  private Boolean showStatistics;

  @XStreamAsAttribute
  private Boolean slot30;

  @XStreamAsAttribute
  private Integer userId;

  @XStreamAsAttribute
  private Integer firstHour = 8;

  @XStreamAsAttribute
  private Boolean showPlanning;

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
      this.startDate = startDate;
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

  /**
   * @return the showStatistics
   */
  public boolean isShowStatistics()
  {
    return showStatistics == Boolean.TRUE;
  }

  /**
   * @param showStatistics the showStatistics to set
   * @return this for chaining.
   */
  public CalendarFilter setShowStatistics(final boolean showStatistics)
  {
    this.showStatistics = showStatistics;
    return this;
  }

  /**
   * @return the showPlanning
   */
  public Boolean isShowPlanning()
  {
    return showPlanning == Boolean.TRUE;
  }

  /**
   * @param showPlanning the showPlanning to set
   * @return this for chaining.
   */
  public CalendarFilter setShowPlanning(final Boolean showPlanning)
  {
    this.showPlanning = showPlanning;
    return this;
  }

  /**
   * If true then the slot is 30 minutes otherwise 15 minutes.
   * @return the slot30
   */
  public boolean isSlot30()
  {
    return slot30 == Boolean.TRUE;
  }

  /**
   * @param slot30 the slot30 to set
   * @return this for chaining.
   */
  public CalendarFilter setSlot30(final boolean slot30)
  {
    this.slot30 = slot30;
    return this;
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
   * @return the firstHour to display in week mode of calendar.
   */
  public Integer getFirstHour()
  {
    return (firstHour != null && firstHour < 24) ? firstHour : 8;
  }

  /**
   * @param firstHour the firstHour to set
   * @return this for chaining.
   */
  public CalendarFilter setFirstHour(final Integer firstHour)
  {
    this.firstHour = firstHour;
    return this;
  }

  /**
   * Was used by old calendar.
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
