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

import net.sourceforge.stripes.format.DefaultFormatterFactory;
import net.sourceforge.stripes.format.Formatter;

public class FormatterFactory extends DefaultFormatterFactory
{
  @SuppressWarnings("unchecked")
  @Override
  public Formatter getFormatter(Class clazz, Locale locale, String formatType, String formatPattern)
  {
    Formatter formatter = null;
    if (Date.class.isAssignableFrom(clazz)) {
      formatter = new DateFormatter();
    } else if ("digits".equals(formatType) == true) {
      formatter = new MyNumberFormatter();
    }
    if (formatter != null) {
      formatter.setFormatType(formatType);
      formatter.setFormatPattern(formatPattern);
      formatter.setLocale(locale);
      formatter.init();
      return formatter;
    }
    return super.getFormatter(clazz, locale, formatType, formatPattern);
  }
}
