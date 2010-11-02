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

package org.projectforge.web.user;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.projectforge.user.UserDao;
import org.projectforge.web.MessageAction;
import org.projectforge.web.core.ExtendedActionBean;


@StrictBinding
@UrlBinding("/secure/user/ChangePassword.action")
public class ChangePasswordAction extends ExtendedActionBean
{
  private static final Logger log = Logger.getLogger(ChangePasswordAction.class);

  @Validate(required = true)
  private String oldPassword;

  @Validate(required = true)
  private String newPassword;

  @Validate(required = true)
  private String passwordRepeat;
  
  private UserDao userDao;
  
  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }
  
  public static void validatePasswordChange(ExtendedActionBean action, UserDao userDao, String password, String passwordRepeat)
  {
    if (StringUtils.isNotEmpty(password)) {
      if (password.equals(passwordRepeat) == false) {
        action.addError("newPassword", "user.error.passwordAndRepeatDoesNotMatch");
        return;
      }
    }
    String errorMsgKey = userDao.checkPasswordQuality(password);
    if (errorMsgKey != null) {
      action.addError("newPassword", errorMsgKey);
    }
  }

  @DefaultHandler
  @DontValidate
  public Resolution init()
  {
    return new ForwardResolution("/WEB-INF/jsp/user/changePassword.jsp");
  }

  public Resolution change()
  {
    getLogger().debug("change password");
    validatePasswordChange(this, userDao, newPassword, passwordRepeat);
    if (hasErrors() == true) {
      oldPassword = newPassword = passwordRepeat = "";
      return getContext().getSourcePageResolution();
    }
    String errorMsgKey = userDao.changePassword(getContext().getUser(), oldPassword, newPassword);
    oldPassword = newPassword = passwordRepeat = "";
    if (errorMsgKey != null) {
      addGlobalError(errorMsgKey);
      return getContext().getSourcePageResolution();
    }
    return MessageAction.getForwardResolution(this, "user.changePassword.msg.passwordSuccessfullyChanged");
  }

  @DontValidate
  public Resolution cancel()
  {
    getLogger().debug("cancel changing password");
    String action = new LocalizableMessage("user.changePassword.title").getMessage(getContext().getLocale());
    return MessageAction.getForwardResolution(this, "message.cancelAction", action);
  }

  /**
   * Return always empty string due to security reasons.
   * @return Empty string.
   */
  public String getOldPassword()
  {
    return "";
  }

  public void setOldPassword(String password)
  {
    this.oldPassword = password;
  }

  /**
   * Return always empty string due to security reasons.
   * @return Empty string.
   */
  public String getNewPassword()
  {
    return "";
  }

  public void setNewPassword(String newPassword)
  {
    this.newPassword = newPassword;
  }

  /**
   * Return always empty string due to security reasons.
   * @return Empty string.
   */
  public String getPasswordRepeat()
  {
    return "";
  }

  public void setPasswordRepeat(String passwordCheck)
  {
    this.passwordRepeat = passwordCheck;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected void storeToFlowScope()
  {
    // Not needed.
  }
}
