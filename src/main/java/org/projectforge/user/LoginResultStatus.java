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
  ADMIN_LOGIN_REQUIRED("adminLoginRequired"), /** This account is locked for x seconds due to failed login attempts. */
  LOGIN_TIME_OFFSET("timeOffset"), FAILED("error.loginFailed"), LOGIN_EXPIRED("error.loginExpired"), SUCCESS("success");

  private String key;

  private String msgParam;

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
    for (final LoginResultStatus status : loginResult) {
      if (this == status) {
        return true;
      }
    }
    return false;
  }

  public String getI18nKey()
  {
    return "login." + key;
  }

  public String getLocalizedMessage()
  {
    if (this == LOGIN_TIME_OFFSET) {
      // msgParam is seconds.
      return PFUserContext.getLocalizedMessage(getI18nKey(), msgParam);
    }
    return PFUserContext.getLocalizedString(getI18nKey());
  }

  /**
   * Used for {@link #LOGIN_TIME_OFFSET} as parameter for seconds.
   * @param msgParam the msgParam to set
   * @return this for chaining.
   */
  public LoginResultStatus setMsgParam(final String msgParam)
  {
    this.msgParam = msgParam;
    return this;
  }

  /**
   * @return the msgParam
   */
  public String getMsgParam()
  {
    return msgParam;
  }
}
