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

package org.projectforge.user;

import org.projectforge.core.I18nEnum;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public enum LoginResultStatus implements I18nEnum
{
  ADMIN_LOGIN_REQUIRED("adminLoginRequired"), FAILED("error.loginFailed"), LOGIN_EXPIRED("error.loginExpired"), SUCCESS("success");

  private String key;

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  LoginResultStatus(final String key)
  {
    this.key = key;
  }

  public boolean isIn(final LoginResultStatus... loginResult)
  {
    for (final LoginResultStatus art : loginResult) {
      if (this == art) {
        return true;
      }
    }
    return false;
  }

  public String getI18nKey()
  {
    return "login." + key;
  }
}
