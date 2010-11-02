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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.validation.BigDecimalTypeConverter;
import net.sourceforge.stripes.validation.ValidationError;

/**
 * HOTFIX (workaround): Modified from stripes source. Supports Euro is supported. Please note: Currency has nothing to do with the locale as in Stripes assumed!
 */
public class CurrencyTypeConverter extends BigDecimalTypeConverter
{
  /**
   * Removes "€" at the end of the input and calls super. Since Stripes 1.5 the € symbol doesn't seem to work properbly.
   * @see BigDecimalTypeConverter#convert(String, Class, Collection)
   */
  public BigDecimal convert(String input, Class< ? extends BigDecimal> targetType, Collection<ValidationError> errors)
  {
    input = modifyCurrencyString(input);
    return super.convert(input, targetType, errors);
  }
  
  String modifyCurrencyString(String input) {
    if (StringUtils.isNotBlank(input) == true) {
      StringTokenizer tokenizer = new StringTokenizer(input); // White spaces as tokenizer
      if (tokenizer.countTokens() == 2) {
        String str = tokenizer.nextToken();
        if ("€".equals(tokenizer.nextToken()) == true) {
          return str;
        }
      }
    }
    return input;
  }
}
