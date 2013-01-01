/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.wicket.converter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.convert.IConverter;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LanguageConverter implements IConverter
{
  private static final long serialVersionUID = 2554286471459716772L;

  /**
   * Contains a map for all used user locales. Each map contains the display name of a locale as key and the locale object as value.
   */
  private final static Map<Locale, Map<String, Locale>> localeMap = new HashMap<Locale, Map<String, Locale>>();

  public static final String getLanguageAsString(final Locale language, final Locale locale)
  {
    if (language == null) {
      return "";
    }
    return language.getDisplayName(locale);
  }

  public static final Locale getLanguage(final String language, final Locale locale)
  {
    synchronized (localeMap) {
      if (localeMap.containsKey(locale) == false) {
        final Map<String, Locale> m = new HashMap<String, Locale>();
        for (final Locale lc : Locale.getAvailableLocales()) {
          m.put(lc.getDisplayName(locale), lc);
        }
        localeMap.put(locale, m);
      }
    }
    return localeMap.get(locale).get(language);
  }

  /**
   * Will be called if convert to Object fails. Does nothing at default.
   */
  protected void error()
  {
  }

  /**
   * Uses all available locales and compares the string value with the display name (in the given locale).
   * 
   * @param value The string representation.
   * @param locale The user's locale to use the correct translation.
   * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String, java.util.Locale)
   */
  @Override
  public Object convertToObject(final String value, final Locale locale)
  {
    if (StringUtils.isEmpty(value) == true) {
      return null;
    }
    final String lvalue = value.toLowerCase(locale);
    for (final Locale lc : Locale.getAvailableLocales()) {
      if (getLanguageAsString(lc, locale).toLowerCase().equals(lvalue) == true) {
        return lc;
      }
    }
    error();
    return null;
  }

  /**
   * @param value The locale to convert.
   * @param locale The user's locale to use the correct translation.
   * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object, java.util.Locale)
   * @see Locale#getDisplayCountry(Locale)
   */
  @Override
  public String convertToString(final Object value, final Locale locale)
  {
    if (value == null) {
      return null;
    }
    final Locale language = (Locale) value;
    return getLanguageAsString(language, locale);
  }
}
