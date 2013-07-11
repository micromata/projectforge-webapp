/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils.date;

/**
 * <p>If you want to know if a given date is a holiday, then use implementations of this interface.</p>
 * 
 * @author Sweeps &lt;pf@byte-storm.com&gt;
 *
 */
public interface HolidayChecker
{
  /**
   * Returns whether the given date is a holiday or not.
   * 
   * @param year the year of the date
   * @param month the month of the year
   * @param day the day of the month
   * @return whether the date is a holiday or not
   */
  public boolean isHoliday(int year, int month, int day);
}
