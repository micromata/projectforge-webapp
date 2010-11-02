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

import java.util.Date;
import java.util.Locale;

import net.sourceforge.stripes.validation.DefaultTypeConverterFactory;
import net.sourceforge.stripes.validation.TypeConverter;

public class TypeConverterFactory extends DefaultTypeConverterFactory
{
  @SuppressWarnings("unchecked")
  @Override
  public TypeConverter<?> getTypeConverter(Class forType, Locale locale) throws Exception
  {
    if (Date.class.isAssignableFrom(forType)) {
      TypeConverter<Date> tc = new DateTypeConverter();
      tc.setLocale(locale);
      return tc;
    }
    return super.getTypeConverter(forType, locale);
  }

  /*public TypeConverter getFormatter(Class clazz, Locale locale, String formatType, String formatPattern)
  {
    Formatter formatter = null;
    if (Date.class.isAssignableFrom(clazz)) {
      formatter = new DateFormatter();
      formatter.setFormatType(formatType);
      formatter.setFormatPattern(formatPattern);
      formatter.setLocale(locale);
      formatter.init();
      return formatter;
    }
    return super.getFormatter(clazz, locale, formatType, formatPattern);
  }*/
}
