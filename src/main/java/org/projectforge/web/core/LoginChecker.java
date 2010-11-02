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

package org.projectforge.web.core;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

import de.micromata.user.UserInfo;

public class LoginChecker
{
  private static final String COOKIE_NAME_FOR_STAY_LOGGED_IN = "stayLoggedIn";

  private static final int COOKIE_MAX_AGE = 30 * 24 * 3600; // 30 days.

  public static class AfterLoginAction
  {
    String urlAfter;

    public void setUrlAfter(String urlAfter)
    {
      this.urlAfter = urlAfter;
    }
  }

  private final static Logger log = Logger.getLogger(LoginChecker.class);

  private UserDao userDao;

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  /**
   * Deletes the stay-logged-in cookie, if exists.
   * @param response
   * @param user
   */
  public void logout(final HttpServletRequest request, final HttpServletResponse response, final PFUserDO user)
  {
    final Cookie stayLoggedInCookie = getStayLoggedInCookie(request);
    if (stayLoggedInCookie != null) {
      stayLoggedInCookie.setMaxAge(0);
      stayLoggedInCookie.setValue("");
      stayLoggedInCookie.setPath("/");
      response.addCookie(stayLoggedInCookie);
    }
  }

  private Cookie getStayLoggedInCookie(final HttpServletRequest request)
  {
    final Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (final Cookie cookie : cookies) {
        if (COOKIE_NAME_FOR_STAY_LOGGED_IN.equals(cookie.getName()) == true) {
          return cookie;
        }
      }
    }
    return null;
  }

  /**
   * Will be called, if the user is not logged in. Checks the stay-logged-in cookie.
   * @return the user, if the cookie for staying logged in was found, otherwise null.
   */
  public UserInfo checkLogon(final HttpServletRequest request, final HttpServletResponse response)
  {
    final Cookie stayLoggedInCookie = getStayLoggedInCookie(request);
    if (stayLoggedInCookie != null) {
      final String value = stayLoggedInCookie.getValue();
      if (StringUtils.isBlank(value) == true) {
        return null;
      }
      final String[] values = value.split(":");
      if (values == null || values.length != 3) {
        log.warn("Invalid cookie found: " + value);
        return null;
      }
      final Integer userId = NumberHelper.parseInteger(values[0]);
      final PFUserDO user = userDao.internalGetById(userId);
      if (user == null) {
        log.warn("Invalid cookie found (user not found): " + value);
        return null;
      }
      if (user.getUsername().equals(values[1]) == false) {
        log.warn("Invalid cookie found (user name wrong, maybe changed): " + value);
        return null;
      }
      if (values[2] == null || values[2].equals(user.getStayLoggedInKey()) == false) {
        log.warn("Invalid cookie found (stay-logged-in key, maybe renewed and/or user password changed): " + value);
        return null;
      }
      stayLoggedInCookie.setMaxAge(COOKIE_MAX_AGE);
      stayLoggedInCookie.setPath("/");
      response.addCookie(stayLoggedInCookie); // Refresh cookie.
      log.info("User successfully logged in using stay-logged-in method: " + user.getDisplayUsername());
      return user;
    }
    return null;
  }

  /**
   * Returns the UserInfo if login is successful, otherwise null (e. g. for wrong username password combination and for deleted users).
   * Stores also a stay-logged-in cookie if the selected.
   * @see de.micromata.user.PasswordChecker#checkLogon(java.lang.String, java.lang.String,
   *      de.micromata.user.PasswordChecker.AfterLoginAction)
   */
  public UserInfo checkLogon(final HttpServletRequest request, final HttpServletResponse response, final String username,
      final String password, final AfterLoginAction after)
  {
    log.debug("LoginEditCheckAction.execute");
    String encryptedPassword = userDao.encryptPassword(password);

    final PFUserDO user = userDao.authenticateUser(username, encryptedPassword);
    if (user != null) {
      log.info("User with valid username/password: " + username + "/" + encryptedPassword);
      if (user.isDeleted() == true) {
        log.info("User has no system access (is deleted): " + user.getDisplayUsername());
        return null;
      } else {
        log.info("User successfully logged in: " + user.getDisplayUsername());
        final String stayLoggedIn = request.getParameter(COOKIE_NAME_FOR_STAY_LOGGED_IN);
        final boolean userWantsToStayLoggedIn = stayLoggedIn != null && "true".equals(stayLoggedIn);
        if (userWantsToStayLoggedIn == true) {
          final PFUserDO loggedInUser = userDao.internalGetById(user.getId());
          final Cookie cookie = new Cookie("stayLoggedIn", loggedInUser.getId()
              + ":"
              + loggedInUser.getUsername()
              + ":"
              + userDao.getStayLoggedInKey(user.getId()));
          cookie.setMaxAge(COOKIE_MAX_AGE);
          cookie.setSecure(true);
          cookie.setPath("/");
          response.addCookie(cookie);
        }
        return user;
      }
    } else {
      log.info("User login failed: " + username + "/" + encryptedPassword);
    }
    return null;
  }
}
