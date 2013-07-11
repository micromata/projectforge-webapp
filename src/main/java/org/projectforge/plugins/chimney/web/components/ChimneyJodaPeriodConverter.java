/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;
import org.joda.time.MutablePeriod;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;

/**
 * Converter for transforming Joda Periods into Strings and vice versa.
 * @author Sweeps <pf@byte-storm.com>
 */
public class ChimneyJodaPeriodConverter<P> implements IConverter<P>
{
  private static final long serialVersionUID = 1182652935442125242L;

  // formatter for printing periods, e.g. 1d 5h
  private static PeriodFormatter formatter = new PeriodFormatterBuilder()
  .appendDays().appendSuffix("d")
  .appendSeparator(" ")
  .appendHours().appendSuffix("h").toFormatter();

  // parser for period strings, supported patterns: 1d 5h | 1d;5h | 1d,5h | 1d5h
  private static PeriodParser parser = new PeriodFormatterBuilder()
  .appendDays().appendSuffix("d")
  .appendSeparator(" ", " ", new String[] {",", ";", ""})
  .appendHours().appendSuffix("h").toParser();

  @SuppressWarnings("unchecked")
  @Override
  public P convertToObject(final String value, final Locale locale) {
    /**
     * make a detour with MutablePeriod, because Period itself does not implement
     * ReadWritablePeriod which is necessary for parsing
     */
    final ReadWritablePeriod mutablePeriod = new MutablePeriod();
    parser.parseInto(mutablePeriod, value, 0, locale);

    return (P) mutablePeriod.toPeriod();
  }

  public static PeriodFormatter getFormatter(){
    return formatter;
  }

  @Override
  public String convertToString(final P value, final Locale locale) {
    return formatter.print((ReadablePeriod)value);
  }
}
