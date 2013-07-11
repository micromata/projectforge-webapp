/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadablePeriod;

public class GanttScheduledActivity
{
  private DateTime begin;

  private DateTime end;

  public GanttScheduledActivity(final ReadableDateTime begin, final ReadableDateTime end) {
    setBegin(begin);
    setEnd(end);
  }

  public GanttScheduledActivity(final ReadableDateTime begin, final ReadablePeriod duration) {
    setBegin(begin);
    setDuration(duration);
  }

  public GanttScheduledActivity(final ReadablePeriod duration, final ReadableDateTime end) {
    setEnd(begin);
    setDurationFromEnd(duration);
  }

  public void setBegin(final ReadableDateTime begin)
  {
    if (end != null && end.isBefore(begin)) {
      throw new IllegalArgumentException("Begin must not be before end");
    }
    this.begin = new DateTime(begin);
  }

  public ReadableDateTime getBegin()
  {
    return begin;
  }

  public void setEnd(final ReadableDateTime end)
  {
    if (begin != null && begin.isAfter(end)) {
      throw new IllegalArgumentException("End must not be before begin");
    }
    this.end = new DateTime(end);
  }

  public ReadableDateTime getEnd()
  {
    return end;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((begin == null) ? 0 : begin.hashCode());
    result = prime * result + ((end == null) ? 0 : end.hashCode());
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
    final GanttScheduledActivity other = (GanttScheduledActivity) obj;
    if (begin == null) {
      if (other.begin != null)
        return false;
    } else if (!begin.equals(other.begin))
      return false;
    if (end == null) {
      if (other.end != null)
        return false;
    } else if (!end.equals(other.end))
      return false;
    return true;
  }

  public ReadablePeriod getDuration()
  {
    return new Period(begin, end).plusDays(1);
  }

  public void setDuration(final ReadablePeriod duration)
  {
    end = new DateTime(begin).plus(duration);
  }

  public void setDurationFromEnd(final ReadablePeriod duration)
  {
    begin = new DateTime(end).minus(duration);
  }

  @Override
  public String toString() {
    return String.format("%s <- %s -> %s", begin.toString(), getDuration().toString(), end.toString());
  }
}
