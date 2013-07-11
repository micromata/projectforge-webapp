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
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.springframework.util.StringUtils;

/**
 * Bidirectional converter between Joda's DataTime and String objects
 * @author Sweeps <pf@byte-storm.com>
 */
public class ChimneyJodaDateConverter<C> implements IConverter<C>
{
  private static final long serialVersionUID = 1182652935442125242L;

  //public static final String DATE_TIME_PATTERN = "dd.MM.yyyy";
  private static DateTimeFormatter formatter = DateTimeFormat.forPattern(DateFormats.getFormatString(DateFormatType.DATE));

  @SuppressWarnings("unchecked")
  @Override
  public C convertToObject(final String value, final Locale locale) {
    return (C) (StringUtils.hasText(value) ? formatter.withLocale(locale).parseDateTime(value) : null);
  }

  @Override
  public String convertToString(final C value, final Locale locale) {
    return value == null ? new String() : formatter.withLocale(locale).print((ReadableInstant) value);
  }

}
