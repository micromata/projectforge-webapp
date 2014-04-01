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

import org.projectforge.core.I18nEnum;

/**
 * 
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
public enum AddressEntryType implements I18nEnum
{
  BUSINESS("business", 1), POSTAL("postal", 2), PRIVATE("private", 3);

  private String key;

  private int ordering;

  public static final String I18N_KEY_ADDRESSENTRY_PREFIX = "addressentry.";

  /**
   * @return The full i18n key including I18N_KEY_ADDRESSENTRY_PREFIX.
   */
  public String getI18nKey()
  {
    return I18N_KEY_ADDRESSENTRY_PREFIX + key;
  }

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  private AddressEntryType(final String key, final int ordering)
  {
    this.key = key;
    this.ordering = ordering;
  }

  public static Object getValue(final AddressEntryType adressEntryType)
  {
    Object value = new Object();
    // The missing breaks are intentionally: this way the key of the case itself and all the cases higher are added.
    switch (adressEntryType) {
      case BUSINESS:
        value = BUSINESS;
        break;
      case POSTAL:
        value = POSTAL;
        break;
      case PRIVATE:
        value = PRIVATE;
        break;
    }
    return value;
  }

  /**
   * @return the ordering
   */
  public int getOrdering()
  {
    return ordering;
  }

  /**
   * @param ordering the ordering to set
   */
  public void setOrdering(final int ordering)
  {
    this.ordering = ordering;
  }

}
