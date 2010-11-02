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

package org.projectforge.book;

import org.apache.commons.lang.StringUtils;
import org.projectforge.core.I18nEnum;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 *         <ul>
 *         <li>MISSED - Book not found (espacially after an inventory).</li>
 *         <li>PRESENT - Book is present at the office ore lend out is known.</li>
 *         <li>DISPOSED - Book is disposed.</li>
 *         </ul>
 */
public enum BookStatus implements I18nEnum
{
  PRESENT("present"), MISSED("missed"), DISPOSED("disposed"), UNKNOWN("unknown");

  private String key;

  public static BookStatus get(String s)
  {
    if (StringUtils.isEmpty(s) == true) {
      return null;
    }
    if ("MISSED".equals(s) == true) {
      return MISSED;
    } else if ("PRESENT".equals(s) == true) {
      return PRESENT;
    } else if ("DISPOSED".equals(s) == true) {
      return DISPOSED;
    } else if ("UNKNOWN".equals(s) == true) {
      return UNKNOWN;
    }
    throw new UnsupportedOperationException("Unknown BookStatus: '" + s + "'");
  }

  /**
   * @return The full i18n key including the i18n prefix "book.status.".
   */
  public String getI18nKey()
  {
    return "book.status." + key;
  }

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  BookStatus(String key)
  {
    this.key = key;
  }
}
