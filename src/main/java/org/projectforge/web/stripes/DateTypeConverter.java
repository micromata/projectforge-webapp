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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.regex.Pattern;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserContext;


/**
 * Modified from stripes source.
 */
public class DateTypeConverter implements TypeConverter<Date>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DateTypeConverter.class);

  private Locale locale;

  /**
   * Used by Stripes to set the input locale. Once the locale is set a number of DateFormat instances are created ready to convert any
   * input.
   */
  public void setLocale(Locale locale)
  {
    this.locale = locale;
  }

  /**
   * @return the current input locale.
   */
  public Locale getLocale()
  {
    return locale;
  }

  /**
   * <p>
   * A pattern used to pre-process Strings before the parsing attempt is made. Since SimpleDateFormat strictly enforces that the separator
   * characters in the input are the same as those in the pattern, this regular expression is used to remove commas, slashes, hyphens and
   * periods from the input String (replacing them with spaces) and to collapse multiple white-space characters into a single space.
   * </p>
   * 
   * <p>
   * This pattern can be changed by providing a different value under the <code>'stripes.dateTypeConverter.preProcessPattern'</code> key
   * in the resource bundle. The default value is <code>(?&lt;!GMT)[\\s,-/\\.]+</code>.
   * </p>
   */
  public static final Pattern PRE_PROCESS_PATTERN = Pattern.compile("(?<!GMT)[\\s,-/\\.]+");

  /** The default set of date patterns used to parse dates with SimpleDateFormat. */
  public static final String[] formatStrings = new String[] { "d MMM yy", "yyyy M d", "yyyy MMM d", "EEE MMM dd HH:mm:ss zzz yyyy"};

  /** The key used to look up the localized format strings from the resource bundle. */
  public static final String KEY_FORMAT_STRINGS = "stripes.dateTypeConverter.formatStrings";

  /** The key used to look up the pre-process pattern from the resource bundle. */
  public static final String KEY_PRE_PROCESS_PATTERN = "stripes.dateTypeConverter.preProcessPattern";

  /**
   * Returns an array of format strings that will be used, in order, to try and parse the date. This method can be overridden to make
   * DateTypeConverter use a different set of format Strings. Given that pre-processing converts most common separator characters into
   * spaces, patterns should be expressed with spaces as separators, not slashes, hyphens etc.
   */
  protected String[] getFormatStrings()
  {
    try {
      return getResourceString(KEY_FORMAT_STRINGS).split(", *");
    } catch (MissingResourceException mre) {
      // First get the locale specific date format patterns
      int[] dateFormats = { DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG};
      String[] formatStrings = new String[dateFormats.length + DateTypeConverter.formatStrings.length];

      for (int i = 0; i < dateFormats.length; i++) {
        SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(dateFormats[i], locale);
        formatStrings[i] = preProcessInput(dateFormat.toPattern());
      }

      // Then copy the default format strings over too
      System.arraycopy(DateTypeConverter.formatStrings, 0, formatStrings, dateFormats.length, DateTypeConverter.formatStrings.length);

      return formatStrings;
    }
  }

  /**
   * Attempts to convert a String to a Date object. Pre-processes the input by invoking the method preProcessInput(), then uses an ordered
   * list of DateFormat objects (supplied by getDateFormats()) to try and parse the String into a Date.
   */
  public Date convert(String input, Class< ? extends Date> targetType, Collection<ValidationError> errors)
  {
    String[] formatStrings = getFormatStrings();
    SimpleDateFormat[] dateFormats = new SimpleDateFormat[formatStrings.length];

    for (int i = 0; i < formatStrings.length; i++) {
      dateFormats[i] = new SimpleDateFormat(formatStrings[i], locale);
      dateFormats[i].setLenient(false);
      if (ClassUtils.isAssignable(targetType, java.sql.Date.class) == false) {
        // Set time zone not for java.sql.Date, because e. g. for Europe/Berlin the date 1970-11-21 will
        // result in 1970-11-20 23:00:00 UTC and therefore 1970-11-20!
        dateFormats[i].setTimeZone(PFUserContext.getTimeZone());
      }
    }

    // Step 1: pre-process the input to make it more palatable
    String parseable = preProcessInput(input);

    // Step 2: try really hard to parse the input
    Date date = null;

     if (StringUtils.isNumeric(input) == true) {
      long millis = NumberHelper.parseLong(input);
      date = new Date(millis);
    } else {
      for (DateFormat format : dateFormats) {
        try {
          date = format.parse(parseable);
          break;
        } catch (ParseException pe) { /* Do nothing, we'll get lots of these. */
        }
      }
    }
    // Step 3: If we successfully parsed, return a date, otherwise send back an error
    if (date != null) {
      if (ClassUtils.isAssignable(targetType, java.sql.Date.class) == true) {
        DayHolder day = new DayHolder(date);
        return day.getSQLDate();
      }
      return date;
    } else {
      log.error("Unparseable date string: " + input);
      errors.add(new ScopedLocalizableError("converter.date", "invalidDate"));
      return null;
    }
  }

  /**
   * Returns the regular expression pattern used in the pre-process method. Looks for a pattern in the resource bundle under the key
   * 'stripes.dateTypeConverter.preProcessPattern'. If no value is found, the pattern <code>(?&lt;!GMT)[\\s,-/\\.]+</code> is used by
   * default. The pattern is used by preProcessInput() to replace all matches by single spaces.
   */
  protected Pattern getPreProcessPattern()
  {
    try {
      return Pattern.compile(getResourceString(KEY_PRE_PROCESS_PATTERN));
    } catch (MissingResourceException exc) {
      return DateTypeConverter.PRE_PROCESS_PATTERN;
    }
  }

  /**
   * Pre-processes the input String to improve the chances of parsing it. First uses the regular expression Pattern returned by
   * getPreProcessPattern() to remove all separator chars and ensure that components are separated by single spaces. Then invokes
   * {@link #checkAndAppendYear(String)} to append the year to the date in case the date is in a format like "12/25" which would otherwise
   * fail to parse.
   */
  protected String preProcessInput(String input)
  {
    input = getPreProcessPattern().matcher(input.trim()).replaceAll(" ");
    input = checkAndAppendYear(input);
    return input;
  }

  /**
   * Checks to see how many 'parts' there are to the date (separated by spaces) and if there are only two parts it adds the current year to
   * the end by geting the Locale specific year string from a Calendar instance.
   * 
   * @param input the date string after the pre-process pattern has been run against it
   * @return either the date string as is, or with the year appended to the end
   */
  protected String checkAndAppendYear(String input)
  {
    // Count the spaces, date components = spaces + 1
    int count = 0;
    for (char ch : input.toCharArray()) {
      if (ch == ' ')
        ++count;
    }

    // Looks like we probably only have a day and month component, that won't work!
    if (count == 1) {
      input += " " + Calendar.getInstance(locale).get(Calendar.YEAR);
    }
    return input;
  }

  /** Convenience method to fetch a property from the resource bundle. */
  protected String getResourceString(String key) throws MissingResourceException
  {
    return StripesFilter.getConfiguration().getLocalizationBundleFactory().getErrorMessageBundle(locale).getString(key);

  }
}
