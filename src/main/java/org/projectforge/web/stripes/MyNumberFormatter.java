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

import java.util.Locale;

import net.sourceforge.stripes.format.Formatter;

import org.apache.commons.lang.StringUtils;

/**
 * Formats numbers.<br/> Examples:<br/> formatType="digits" (default), formatPattern="2": 00, 01, 09, 10, ...;<br/> formatType="digits",
 * formatPattern="3": 000, 001, 009, 010, 099, 100, ...<br/> Uses StringUtils.leftPad(input, 3, '0');
 * @see StringUtils#leftPad(String, int, char)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MyNumberFormatter implements Formatter<Integer>
{
  private int length;

  /** No operation (only default "digits" supported). */
  public void setFormatType(String formatType)
  {
  }

  /** length of number (default is 3). */
  public void setFormatPattern(String formatPattern)
  {
    if (StringUtils.isBlank(formatPattern) == true) {
      this.length = 3;
    } else {
      this.length = Integer.parseInt(formatPattern);
    }
  }

  /** No operation. */
  public void setLocale(Locale locale)
  {
  }

  public void init()
  {
    if (this.length == 0) {
      length = 3;
    }
  }

  /**
   * Formats the number.
   * @see StringUtils#leftPad(String, int, char)
   */
  public String format(Integer input)
  {
    return StringUtils.leftPad(String.valueOf(input), length, '0');
  }
}
