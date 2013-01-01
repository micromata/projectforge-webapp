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

package org.projectforge.web;

import org.projectforge.core.I18nEnum;


public enum BrowserScreenWidthType implements I18nEnum
{
  NARROW("narrow"), NORMAL("normal"), WIDE("wide");

  public boolean isIn(final BrowserScreenWidthType... type) {
    for (final BrowserScreenWidthType t : type) {
      if (this == t) {
        return true;
      }
    }
    return false;
  }


  private String key;

  /**
   * @return The full i18n key including the i18n prefix "layout.settings.browserScreenWidth.".
   */
  public String getI18nKey()
  {
    return "layout.settings.browserScreenWidth." + key;
  }

  private BrowserScreenWidthType(final String key)
  {
    this.key = key;
  }
}
