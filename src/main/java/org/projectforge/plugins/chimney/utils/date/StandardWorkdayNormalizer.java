/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils.date;

import org.joda.time.Duration;
import org.joda.time.Period;

public class StandardWorkdayNormalizer
{
  final static int STANDARD_WORKDAY_HOURS = 8;

  public static Period toNormalizedPeriod(final Period standardPeriod)
  {
    if (standardPeriod == null) {
      return null;
    }
    final int hours = standardPeriod.getHours();
    final int days = standardPeriod.getDays();
    final int addDays = hours / STANDARD_WORKDAY_HOURS;
    final int remainingHours = hours % STANDARD_WORKDAY_HOURS;
    Period normalizedPeriod = new Period(standardPeriod);
    normalizedPeriod = normalizedPeriod.withDays(days + addDays);
    normalizedPeriod = normalizedPeriod.withHours(remainingHours);
    return normalizedPeriod;
  }

  public static Duration toNormalizedDuration(final Period standardPeriod)
  {
    if (standardPeriod == null) {
      return null;
    }
    final int hours = standardPeriod.getHours() + (standardPeriod.getDays() * STANDARD_WORKDAY_HOURS);
    return new Duration(hours * 60L * 60L * 1000L);
  }
}
