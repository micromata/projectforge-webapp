/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.projectforge.core.DisplayHistoryEntry;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class TeamEventDiff implements Serializable
{
  private static final long serialVersionUID = -7792575093285118058L;

  protected Date lastEmail = null;

  protected List<DisplayHistoryEntry> entries = null;

  private String location = null;

  private Timestamp startDate = null;

  private Timestamp endDate = null;

  private String recurrenceRule = null;

  private SortedSet<TeamEventAttendeeDO> attendees = null;

  private boolean locationChanged, dateChanged, recurrenceChanged, statusChanged;


  public TeamEventDiff(final TeamEventDO event) {
    if (event != null) {
      location = event.getLocation();
      startDate = event.getStartDate();
      endDate = event.getEndDate();
      recurrenceRule = event.getRecurrenceRule();
      if (event.getAttendees() != null && event.getAttendees().isEmpty() == false) {
        attendees = new TreeSet<TeamEventAttendeeDO>();
        for (final TeamEventAttendeeDO attendee: event.getAttendees()) {
          attendees.add(attendee);
        }
      }
    }
    locationChanged = dateChanged = recurrenceChanged = statusChanged = false;
  }

  public void computeChanges(final TeamEventDO event, final List<DisplayHistoryEntry> entries) {
    this.lastEmail = event.getLastEmail();
    if (entries != null && entries.isEmpty() == false && lastEmail != null) {
      this.entries = new ArrayList<DisplayHistoryEntry>();
      for (final DisplayHistoryEntry orgEntry: entries) {
        if (orgEntry.getTimestamp().getTime() > lastEmail.getTime()) {
          this.entries.add(orgEntry);
        }
      }
      computeHistoryChanges();
    }
    if (StringUtils.equals(this.location, event.getLocation()) == false) {
      locationChanged = true;
    }
    if (this.startDate != null && event.getStartDate() != null &&
        this.endDate != null && event.getEndDate() != null) {
      if (this.startDate.equals(event.getStartDate()) == false ||
          this.endDate.equals(event.getEndDate()) == false) {
        dateChanged = true;
      }
    }
    if (StringUtils.equals(this.recurrenceRule, event.getRecurrenceRule()) == false) {
      recurrenceChanged = true;
    }
    if (this.attendees.size() != event.getAttendees().size()) {
      statusChanged = true;
      return;
    }
    final TeamEventAttendeeDO[] array01 = event.getAttendees().toArray(new TeamEventAttendeeDO[0]);
    final TeamEventAttendeeDO[] array02 = attendees.toArray(new TeamEventAttendeeDO[0]);
    for (int i=0; i < attendees.size(); i++) {
      if(array01[i].equals(array02[i]) == false) {
        statusChanged = true;
      }
    }
  }

  public boolean isLocationChanged()
  {
    return locationChanged;
  }

  public boolean isDateChanged()
  {
    return dateChanged;
  }

  public boolean isRecurrenceChanged()
  {
    return recurrenceChanged;
  }

  public boolean isStatusChanged()
  {
    return statusChanged;
  }

  private void computeHistoryChanges() {
    locationChanged = hasLocationChanged();
    dateChanged = hasDateChanged();
    recurrenceChanged = hasRecurrenceChanged();
    statusChanged = hasStatusChanged();
  }

  private boolean hasLocationChanged() {
    for (final DisplayHistoryEntry entry : entries) {
      if (StringUtils.contains(entry.getPropertyName(), "location") == true) {
        return true;
      }
    }
    return false;
  }
  private boolean hasDateChanged() {
    for (final DisplayHistoryEntry entry : entries) {
      if (StringUtils.contains(entry.getPropertyName(), "startDate") == true ||
          StringUtils.contains(entry.getPropertyName(), "endDate") == true ) {
        return true;
      }
    }
    return false;
  }
  private boolean hasRecurrenceChanged() {
    for (final DisplayHistoryEntry entry : entries) {
      if (StringUtils.contains(entry.getPropertyName(), "recurrenceRule") == true) {
        return true;
      }
    }
    return false;
  }
  private boolean hasStatusChanged() {
    for (final DisplayHistoryEntry entry : entries) {
      if (StringUtils.contains(entry.getPropertyName(), "status") == true) {
        return true;
      }
    }
    return false;
  }

}
