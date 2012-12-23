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

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.property.RRule;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;
import org.projectforge.plugins.teamcal.TeamCalConfig;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;

/**
 * Overview of used (and may-be planned) fields:
 * <ul>
 * <li><b>ATTENDEE</b>: ATTENDEE;MEMBER="mailto:DEV-GROUP@example.com": mailto:joecool@example.com</li>
 * <li><b>CONTACT</b>: CONTACT:Jim Dolittle\, ABC Industries\, +1-919-555-1234</li>
 * <li><b>DTEND</b> - End date (DATE-TIME)</li>
 * <li><b>DTSTAMP</b> - Time of creation (DATE-TIME)</li>
 * <li><b>DTSTART</b> - Start date (DATE-TIME)</li>
 * <li><b>EXDATE</b> - exception dates of recurrence (list of DATE-TIME)</li>
 * <li><b>LAST-MODIFIED</b> - (DATE-TIME)</li>
 * <li><b>PARTSTAT</b>=DECLINED:mailto:jsmith@example.com</li>
 * <li><b>ORGANIZER</b>: ORGANIZER;CN=John Smith:mailto:jsmith@example.com</li>
 * <li><b>RDATE</b> - Dates of recurrence</li>
 * <li><b>RRULE</b> - Rule of recurrence: RRULE:FREQ=DAILY;UNTIL=19971224T000000Z</li>
 * <li><b>UID</b>: UID:19960401T080045Z-4000F192713-0052@example.com
 * </ul>
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_CALENDAR_EVENT")
public class TeamEventDO extends DefaultBaseDO implements TeamEvent, Cloneable
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

  private transient RRule recurrenceRuleObject;

  private String recurrenceRule, recurrenceExDate;

  private java.util.Date recurrenceUntil;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String note;

  private String attendees;

  /**
   * Clear fields for viewers with minimal access. If you add new fields don't forget to clear these fields here.
   */
  public void clearFields()
  {
    subject = location = note = attendees = null;
  }

  public TeamEventDO()
  {

  }

  @Transient
  public String getUid()
  {
    return String.valueOf(getId()) + "@" + TeamCalConfig.get().getDomain();
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
   * RRULE (rfc5545)
   * @return the recurrence
   */
  public String getRecurrenceRule()
  {
    return recurrenceRule;
  }

  /**
   * @param recurrenceRule the recurrence to set
   * @return this for chaining.
   */
  public TeamEventDO setRecurrenceRule(final String recurrenceRule)
  {
    this.recurrenceRule = recurrenceRule;
    this.recurrenceRuleObject = null;
    recalculate();
    return this;
  }

  /**
   * @param recurData
   * @return this for chaining.
   */
  @Transient
  public TeamEventDO setRecurrence(final TeamEventRecurrenceData recurData) {
    final String rruleString = TeamEventUtils.calculateRRule(recurData);
    setRecurrenceRule(rruleString);
    return this;
  }

  /**
   * Will be renewed if {@link #setRecurrenceRule(String)} is called.
   * @return the recurrenceRuleObject
   */
  @Transient
  public RRule getRecurrenceRuleObject()
  {
    if (recurrenceRuleObject == null) {
      recalculate();
    }
    return recurrenceRuleObject;
  }

  /**
   * @return true if any recurrenceRule is given, otherwise false.
   */
  @Transient
  public boolean hasRecurrence()
  {
    return StringUtils.isNotBlank(this.recurrenceRule);
  }

  /**
   * The recurrenceUntil date is calculated by the recurrenceRule string if given, otherwise the date is set to null.
   * @see org.projectforge.core.AbstractBaseDO#recalculate()
   */
  @Override
  public void recalculate()
  {
    super.recalculate();
    recurrenceRuleObject = ICal4JUtils.calculateRecurrenceRule(recurrenceRule);
    if (recurrenceRuleObject == null || recurrenceRuleObject.getRecur() == null) {
      this.recurrenceUntil = null;
      return;
    }
    this.recurrenceUntil = recurrenceRuleObject.getRecur().getUntil();
  }

  /**
   * Will be renewed if {@link #setRecurrenceRule(String)} is called.
   * @return the recurrenceRuleObject
   */
  @Transient
  public Recur getRecurrenceObject()
  {
    final RRule rrule = getRecurrenceRuleObject();
    return rrule != null ? rrule.getRecur() : null;
  }

  /**
   * EXDATE (rfc5545)
   * @return the recurrenceExDate
   */
  @Column(name = "recurrence_ex_date")
  public String getRecurrenceExDate()
  {
    return recurrenceExDate;
  }

  /**
   * @param recurrenceExDate the recurrenceExDate to set
   * @return this for chaining.
   */
  public TeamEventDO setRecurrenceExDate(final String recurrenceExDate)
  {
    this.recurrenceExDate = recurrenceExDate;
    return this;
  }

  /**
   * If not given the recurrence will never ends.
   * @return the recurrenceEndDate
   */
  @Column(name = "recurrence_until")
  public java.util.Date getRecurrenceUntil()
  {
    return recurrenceUntil;
  }

  /**
   * Please note: Do not set this property manually! It's set automatically by the recurrence rule! Otherwise the display of calendar events
   * may be incorrect. <br/>
   * This field exist only for data-base query purposes.
   * @param recurrenceUntil the recurrenceEndDate to set
   * @return this for chaining.
   */
  public TeamEventDO setRecurrenceUntil(final java.util.Date recurrenceUntil)
  {
    this.recurrenceUntil = recurrenceUntil;
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

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (allDay ? 1231 : 1237);
    result = prime * result + ((attendees == null) ? 0 : attendees.hashCode());
    result = prime * result + ((calendar == null) ? 0 : calendar.hashCode());
    result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
    result = prime * result + ((recurrenceExDate == null) ? 0 : recurrenceExDate.hashCode());
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    result = prime * result + ((note == null) ? 0 : note.hashCode());
    result = prime * result + ((recurrenceRule == null) ? 0 : recurrenceRule.hashCode());
    result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
    result = prime * result + ((subject == null) ? 0 : subject.hashCode());
    return result;
  }

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
    if (recurrenceExDate == null) {
      if (other.recurrenceExDate != null)
        return false;
    } else if (!recurrenceExDate.equals(other.recurrenceExDate))
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
    if (recurrenceRule == null) {
      if (other.recurrenceRule != null)
        return false;
    } else if (!recurrenceRule.equals(other.recurrenceRule))
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

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public TeamEventDO clone()
  {
    final TeamEventDO clone = new TeamEventDO();
    clone.startDate = this.startDate;
    clone.endDate = this.endDate;
    clone.allDay = this.allDay;
    clone.subject = this.subject;
    clone.location = this.location;
    clone.attendees = this.attendees;
    clone.recurrenceExDate = this.recurrenceExDate;
    clone.recurrenceRule = this.recurrenceRule;
    clone.recurrenceUntil = this.recurrenceUntil;
    return clone;
  }
}
