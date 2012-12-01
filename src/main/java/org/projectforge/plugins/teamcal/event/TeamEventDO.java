/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.RecurrenceInterval;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_CALENDAR_EVENT")
public class TeamEventDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -9205582135590380919L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String subject;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String location;

  private boolean allDay;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.MINUTE)
  private Timestamp startDate;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.MINUTE)
  private Timestamp endDate;

  @IndexedEmbedded(depth = 1)
  private TeamCalDO calendar;

  private RecurrenceInterval recurrenceInterval;

  private Integer recurrenceAmount;

  private Date recurrenceEndDate;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String note;

  private String attendees;

  public TeamEventDO()
  {

  }

  @Column(length = Constants.LENGTH_SUBJECT)
  public String getSubject()
  {
    return subject;
  }

  /**
   * @param title
   * @return this for chaining.
   */
  public TeamEventDO setSubject(final String subject)
  {
    this.subject = subject;
    return this;
  }

  @Column(length = Constants.LENGTH_SUBJECT)
  /**
   * @return the location
   */
  public String getLocation()
  {
    return location;
  }

  /**
   * @param location the location to set
   * @return this for chaining.
   */
  public TeamEventDO setLocation(final String location)
  {
    this.location = location;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "calendar_fk")
  /**
   * @return the calendar
   */
  public TeamCalDO getCalendar()
  {
    return calendar;
  }

  @Transient
  public Integer getCalendarId()
  {
    return calendar != null ? calendar.getId() : null;
  }

  /**
   * @param calendar the calendar to set
   * @return this for chaining.
   */
  public TeamEventDO setCalendar(final TeamCalDO calendar)
  {
    this.calendar = calendar;
    return this;
  }

  /**
   * @return the allDay
   */
  @Column(name = "all_day")
  public boolean isAllDay()
  {
    return allDay;
  }

  /**
   * @param allDay the allDay to set
   * @return this for chaining.
   */
  public TeamEventDO setAllDay(final boolean allDay)
  {
    this.allDay = allDay;
    return this;
  }

  /**
   * @return the startDate
   */
  @Column(name = "start_date")
  public Timestamp getStartDate()
  {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   * @return this for chaining.
   */
  public TeamEventDO setStartDate(final Timestamp startDate)
  {
    this.startDate = startDate;
    return this;
  }

  /**
   * @return the endDate
   */
  @Column(name = "end_date")
  public Timestamp getEndDate()
  {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   * @return this for chaining.
   */
  public TeamEventDO setEndDate(final Timestamp endDate)
  {
    this.endDate = endDate;
    return this;
  }

  /**
   * @return the note
   */
  @Column
  public String getNote()
  {
    return note;
  }

  /**
   * @param note the note to set
   * @return this for chaining.
   */
  public TeamEventDO setNote(final String note)
  {
    this.note = note;
    return this;
  }

  /**
   * @return the attendees
   */
  @Column
  public String getAttendees()
  {
    return attendees;
  }

  /**
   * @param attendees the attendees to set
   * @return this for chaining.
   */
  public TeamEventDO setAttendees(final String attendees)
  {
    this.attendees = attendees;
    return this;
  }

  /**
   * @return the recurrenceInterval
   */
  @Column(name = "recurrence_interval")
  public RecurrenceInterval getRecurrenceInterval()
  {
    return recurrenceInterval;
  }

  /**
   * @param recurrenceInterval the recurrenceInterval to set
   * @return this for chaining.
   */
  public TeamEventDO setRecurrenceInterval(final RecurrenceInterval recurrenceInterval)
  {
    this.recurrenceInterval = recurrenceInterval;
    return this;
  }

  /**
   * @return the recurrenceAmount
   */
  @Column(name = "recurrence_amount")
  public Integer getRecurrenceAmount()
  {
    return recurrenceAmount;
  }

  /**
   * @param recurrenceAmount the recurrenceAmount to set
   * @return this for chaining.
   */
  public TeamEventDO setRecurrenceAmount(final Integer recurrenceAmount)
  {
    this.recurrenceAmount = recurrenceAmount;
    return this;
  }

  /**
   * @return the recurrenceEndDate
   */
  @Column(name = "recurrence_end_date")
  public Date getRecurrenceEndDate()
  {
    return recurrenceEndDate;
  }

  /**
   * @param recurrenceEndDate the recurrenceEndDate to set
   * @return this for chaining.
   */
  public TeamEventDO setRecurrenceEndDate(final Date recurrenceEndDate)
  {
    this.recurrenceEndDate = recurrenceEndDate;
    return this;
  }

  @Transient
  public TimePeriod getTimePeriod()
  {
    return new TimePeriod(startDate, endDate, true);
  }

  /**
   * @return Duration in millis if startTime and stopTime is given and stopTime is after startTime, otherwise 0.
   */
  @Transient
  public long getDuration()
  {
    return getTimePeriod().getDuration();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (allDay ? 1231 : 1237);
    result = prime * result + ((attendees == null) ? 0 : attendees.hashCode());
    result = prime * result + ((calendar == null) ? 0 : calendar.hashCode());
    result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    result = prime * result + ((note == null) ? 0 : note.hashCode());
    result = prime * result + ((recurrenceAmount == null) ? 0 : recurrenceAmount.hashCode());
    result = prime * result + ((recurrenceEndDate == null) ? 0 : recurrenceEndDate.hashCode());
    result = prime * result + ((recurrenceInterval == null) ? 0 : recurrenceInterval.hashCode());
    result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
    result = prime * result + ((subject == null) ? 0 : subject.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final TeamEventDO other = (TeamEventDO) obj;
    if (allDay != other.allDay)
      return false;
    if (attendees == null) {
      if (other.attendees != null)
        return false;
    } else if (!attendees.equals(other.attendees))
      return false;
    if (calendar == null) {
      if (other.calendar != null)
        return false;
    } else if (!calendar.equals(other.calendar))
      return false;
    if (endDate == null) {
      if (other.endDate != null)
        return false;
    } else if (!endDate.equals(other.endDate))
      return false;
    if (location == null) {
      if (other.location != null)
        return false;
    } else if (!location.equals(other.location))
      return false;
    if (note == null) {
      if (other.note != null)
        return false;
    } else if (!note.equals(other.note))
      return false;
    if (recurrenceAmount == null) {
      if (other.recurrenceAmount != null)
        return false;
    } else if (!recurrenceAmount.equals(other.recurrenceAmount))
      return false;
    if (recurrenceEndDate == null) {
      if (other.recurrenceEndDate != null)
        return false;
    } else if (!recurrenceEndDate.equals(other.recurrenceEndDate))
      return false;
    if (recurrenceInterval != other.recurrenceInterval)
      return false;
    if (startDate == null) {
      if (other.startDate != null)
        return false;
    } else if (!startDate.equals(other.startDate))
      return false;
    if (subject == null) {
      if (other.subject != null)
        return false;
    } else if (!subject.equals(other.subject))
      return false;
    return true;
  }

}
