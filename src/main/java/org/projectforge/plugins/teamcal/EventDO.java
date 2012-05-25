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

package org.projectforge.plugins.teamcal;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.RecurrenceInterval;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_CALENDAR_EVENT")
public class EventDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -9205582135590380919L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String subject;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String location;

  private boolean allDay;

  private Date startDate;

  private Date endDate;

  @IndexedEmbedded
  private CalendarDO calendar;

  private RecurrenceInterval recurrenceInterval;

  private Integer recurrenceAmount;

  private Date recurrenceEndDate;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String note;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String attendees;

  @Column(length = Constants.LENGTH_TITLE)
  public String getSubject()
  {
    return subject;
  }

  /**
   * @param title
   * @return this for chaining.
   */
  public EventDO setSubject(final String subject)
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
  public EventDO setLocation(final String location)
  {
    this.location = location;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_fk")
  /**
   * @return the calendar
   */
  public CalendarDO getCalendar()
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
  public EventDO setCalendar(final CalendarDO calendar)
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
  public EventDO setAllDay(final boolean allDay)
  {
    this.allDay = allDay;
    return this;
  }

  /**
   * @return the startDate
   */
  @Column(name = "start_date")
  public Date getStartDate()
  {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   * @return this for chaining.
   */
  public EventDO setStartDate(final Date startDate)
  {
    this.startDate = startDate;
    return this;
  }

  /**
   * @return the endDate
   */
  @Column(name = "end_date")
  public Date getEndDate()
  {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   * @return this for chaining.
   */
  public EventDO setEndDate(final Date endDate)
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
  public EventDO setNote(final String note)
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
  public EventDO setAttendees(final String attendees)
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
  public EventDO setRecurrenceInterval(final RecurrenceInterval recurrenceInterval)
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
  public EventDO setRecurrenceAmount(final Integer recurrenceAmount)
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
  public EventDO setRecurrenceEndDate(final Date recurrenceEndDate)
  {
    this.recurrenceEndDate = recurrenceEndDate;
    return this;
  }
}
