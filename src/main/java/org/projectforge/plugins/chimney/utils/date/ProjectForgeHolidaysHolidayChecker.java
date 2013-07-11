/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils.date;

import org.joda.time.DateTime;
import org.projectforge.calendar.Holidays;

public class ProjectForgeHolidaysHolidayChecker implements HolidayChecker
{
  @Override
  public boolean isHoliday(final int year, final int month, final int day)
  {
    return Holidays.getInstance().isHoliday(year, new DateTime(year, month, day, 13, 37).getDayOfYear());
  }
}
