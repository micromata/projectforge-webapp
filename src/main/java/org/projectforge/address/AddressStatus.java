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

/**
 * Status of address quality.<br/>
 * <ul>
 * <li>UPTODATE - At last time of check or modification, the address seems to be updated.</li>
 * <li>OUTDATED - Address needs update.</li>
 * <li>LEAVED - Person has leaved the company.</li>
 * </ul>
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public enum AddressStatus
{
  UPTODATE("uptodate"), OUTDATED("outdated"), LEAVED("leaved");

  /**
   * List of all available values.
   */
  public static final AddressStatus[] LIST = new AddressStatus[] { UPTODATE, OUTDATED, LEAVED};

  private String key;

  public static AddressStatus get(String s)
  {
    if (StringUtils.isEmpty(s) == true) {
      return null;
    }
    if ("LEAVED".equals(s) == true) {
      return LEAVED;
    } else if ("OUTDATED".equals(s) == true) {
      return OUTDATED;
    } else if ("UPTODATE".equals(s) == true) {
      return UPTODATE;
    }
    throw new UnsupportedOperationException("Unknown AddressStatus: '" + s + "'");
  }

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  AddressStatus(String key)
  {
    this.key = key;
  }
}
