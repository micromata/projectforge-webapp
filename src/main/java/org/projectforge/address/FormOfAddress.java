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

package org.projectforge.address;

import org.apache.commons.lang.StringUtils;
import org.projectforge.core.I18nEnum;

public enum FormOfAddress implements I18nEnum
{
  MISTER("mister"), MISS("miss"), UNKNOWN("unknown");

  /**
   * List of all available values.
   */
  public static final FormOfAddress[] LIST = new FormOfAddress[] { MISTER, MISS, UNKNOWN};

  private String key;

  public static FormOfAddress get(String s)
  {
    if (StringUtils.isEmpty(s) == true) {
      return null;
    }
    if ("MISTER".equals(s) == true) {
      return MISTER;
    } else if ("MISS".equals(s) == true) {
      return MISS;
    } else if ("UNKNOWN".equals(s) == true) {
      return UNKNOWN;
    }
    throw new UnsupportedOperationException("Unknown Anrede" + ": '" + s + "'");
  }

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  /**
   * @return The full i18n key including the i18n prefix "fibu.auftrag.status.".
   */
  public String getI18nKey()
  {
    return "address.form." + key;
  }

  FormOfAddress(String key)
  {
    this.key = key;
  }
}
