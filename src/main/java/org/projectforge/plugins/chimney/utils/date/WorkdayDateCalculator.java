/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils.date;

import static org.joda.time.DateTimeConstants.SATURDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>Skips non-workdays in its date calculations.</p>
 * 
 * <p>The HolidayChecker is optional (can be null), but when given is used to remove the holidays from the set of workdays.</p>
 * 
 * @author Sweeps &lt;pf@byte-storm.com&gt;
 *
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class WorkdayDateCalculator implements DateCalculator
{
  private HolidayChecker holidayChecker;

  public WorkdayDateCalculator() {
    super();
  }

  public WorkdayDateCalculator(final HolidayChecker holidayChecker)
  {
    super();
    this.setHolidayChecker(holidayChecker);
  }

  /**
   * Skips non-workdays (weekends, holidays) in its calculation.
   * 
   * @see org.projectforge.plugins.chimney.utils.date.DateCalculator#plus(org.joda.time.ReadableDateTime, int)
   */
  @Override
  public ReadableDateTime plus(final ReadableDateTime date, final int days)
  {
    DateTime newDate = date.toDateTime();
    for (int i = days ; i > 0 ; i--) {
      do {
        newDate = newDate.plusDays(1);
      } while (!isWorkday(newDate));
    }
    return newDate;
  }

  public boolean isWorkday(final ReadableDateTime date)
  {
    return !isWeekend(date) && !isHoliday(date);
  }

  public boolean isWeekend(final ReadableDateTime date)
  {
    final int dayOfWeek = date.getDayOfWeek();
    return dayOfWeek == SATURDAY || dayOfWeek == SUNDAY;
  }

  public boolean isHoliday(final ReadableDateTime date)
  {
    if (holidayChecker != null) {
      return holidayChecker.isHoliday(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    } else {
      return false;
    }
  }

  /**
   * Skips non-workdays (weekends, holidays) in its calculation.
   * 
   * @see org.projectforge.plugins.chimney.utils.date.DateCalculator#minus(org.joda.time.ReadableDateTime, int)
   */
  @Override
  public ReadableDateTime minus(final ReadableDateTime date, final int days)
  {
    DateTime newDate = date.toDateTime();
    for (int i = days ; i > 0 ; i--) {
      do {
        newDate = newDate.minusDays(1);
      } while (!isWorkday(newDate));
    }
    return newDate;
  }

  public void setHolidayChecker(final HolidayChecker holidayChecker)
  {
    this.holidayChecker = holidayChecker;
  }

  public HolidayChecker getHolidayChecker()
  {
    return holidayChecker;
  }

}
