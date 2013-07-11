/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils.date;

import org.joda.time.ReadableDateTime;

/**
 * <p>Used for date calculations exceeding simple DateTime operations.</p>
 * 
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public interface DateCalculator
{
  /**
   * Adds the number of days to the given date.
   * 
   * @param date origin
   * @param numDays number of days to add
   * @return new date with the number of days added
   */
  public ReadableDateTime plus(ReadableDateTime date, int numDays);

  /**
   * Subtracts the number of days from the given date.
   * 
   * @param date origin
   * @param numDays number of days to remove
   * @return new date with the number of days removed
   */
  public ReadableDateTime minus(ReadableDateTime date, int numDays);
}
