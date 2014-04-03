/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.addresses;

import org.apache.commons.lang.StringUtils;
import org.projectforge.core.I18nEnum;

public enum AddressType implements I18nEnum
{
  BUSINESS("business"), POSTAL("postal"), PRIVATE("private");

  public static final String I18N_KEY_ADDRESSTYPE_PREFIX = "addresstype.";

  /**
   * List of all available values.
   */
  public static final AddressType[] LIST = new AddressType[] { BUSINESS, POSTAL, PRIVATE };

  private String key;

  public static AddressType get(final String s)
  {
    if (StringUtils.isEmpty(s) == true) {
      return null;
    }
    if ("BUSINESS".equals(s) == true) {
      return BUSINESS;
    } else if ("POSTAL".equals(s) == true) {
      return POSTAL;
    } else if ("PRIVATE".equals(s) == true) {
      return PRIVATE;
    }
    throw new UnsupportedOperationException("Unknown AddressType: '" + s + "'");
  }

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  AddressType(final String key)
  {
    this.key = key;
  }

  /**
   * @see org.projectforge.core.I18nEnum#getI18nKey()
   */
  @Override
  public String getI18nKey()
  {
    return I18N_KEY_ADDRESSTYPE_PREFIX + key;
  }
}
