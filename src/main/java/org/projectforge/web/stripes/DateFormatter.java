/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.stripes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.projectforge.user.PFUserContext;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.format.Formatter;

public class DateFormatter implements Formatter<Date>
{
  /** Maintains a map of named formats that can be used instead of patterns. */
  protected static final Map<String, Integer> namedPatterns = new HashMap<String, Integer>();

  static {
    namedPatterns.put("short", DateFormat.SHORT);
    namedPatterns.put("medium", DateFormat.MEDIUM);
    namedPatterns.put("long", DateFormat.LONG);
    namedPatterns.put("full", DateFormat.FULL);
  }

  private String formatType;

  private String formatPattern;

  private Locale locale;

  private DateFormat format;

  private TimeZone timeZone;

  /** Sets the format type to be used to render dates as Strings. */
  public void setFormatType(String formatType)
  {
    this.formatType = formatType;
  }

  /** Sets the named format string or date pattern to use to format the date. */
  public void setFormatPattern(String formatPattern)
  {
    this.formatPattern = formatPattern;
  }

  /** Sets the locale that output String should be in. */
  public void setLocale(Locale locale)
  {
    this.locale = locale;
  }

  public void setTimeZone(TimeZone timeZone)
  {
    this.timeZone = timeZone;
  }

  /**
   * Constructs the DateFormat used for formatting, based on the values passed to the various setter methods on the class. If the
   * formatString is one of the named formats then a DateFormat instance is created of the specified type and format, otherwise a
   * SimpleDateFormat is constructed using the pattern provided and the formatType is ignored.
   * 
   * @throws StripesRuntimeException if the formatType is not one of 'date', 'time' or 'datetime'.
   */
  public void init()
  {
    // Default these values if they were not supplied
    if (formatPattern == null) {
      formatPattern = "short";
    }

    if (formatType == null) {
      formatType = "date";
    }

    String lcFormatString = formatPattern.toLowerCase();
    String lcFormatType = formatType.toLowerCase();

    // Now figure out how to construct our date format for our locale
    if (namedPatterns.containsKey(lcFormatString)) {

      if (lcFormatType.equals("date")) {
        format = DateFormat.getDateInstance(namedPatterns.get(lcFormatString), locale);
      } else if (lcFormatType.equals("datetime")) {
        format = DateFormat.getDateTimeInstance(namedPatterns.get(lcFormatString), namedPatterns.get(lcFormatString), locale);
      } else if (lcFormatType.equals("time")) {
        format = DateFormat.getTimeInstance(namedPatterns.get(lcFormatString));
      } else {
        throw new StripesRuntimeException("Invalid formatType for Date: "
            + formatType
            + ". Allowed types are 'date', 'time' and 'datetime'.");
      }
    } else {
      format = new SimpleDateFormat(formatPattern, locale);
    }
    timeZone = PFUserContext.getTimeZone();
    if (timeZone != null) {
      format.setTimeZone(timeZone);
    }
  }
  
  /** Formats a Date as a String using the rules supplied when the formatter was built. */
  public String format(Date input)
  {
    return this.format.format(input);
  }
}
