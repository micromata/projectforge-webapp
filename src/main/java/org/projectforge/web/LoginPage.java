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

package org.projectforge.web;

import javax.servlet.http.Cookie;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketUtils;

public class LoginPage extends AbstractBasePage
{
  static final String FIRST_PSEUDO_SETUP_USER = "firstPseudoSetupUser";

  public static final String REQUEST_PARAM_LOGOUT = "logout";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoginPage.class);

  @SpringBean(name = "initDatabaseDao")
  private InitDatabaseDao initDatabaseDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "userXmlPreferencesCache")
  private UserXmlPreferencesCache userXmlPreferencesCache;

  private LoginForm form;

  String targetUrlAfterLogin;

  @Override
  protected void thisIsAnUnsecuredPage()
  {
  }

  public LoginPage(final PageParameters parameters)
  {
    super(parameters);
    if (parameters.containsKey(REQUEST_PARAM_LOGOUT) == true) {
      logout();
    } else {
      final PFUserDO user = ((MySession) getSession()).getUser();
      if (user != null) {
        setResponsePage(WicketUtils.getDefaultPage());
        return;
      }
      if (initDatabaseDao.isEmpty() == true) {
        final PFUserDO pseudoUser = new PFUserDO();
        pseudoUser.setUsername(FIRST_PSEUDO_SETUP_USER);
        pseudoUser.setId(-1);
        login(pseudoUser);
        setResponsePage(new SetupPage(new PageParameters()));
        return;
      }
      targetUrlAfterLogin = UserFilter.getTargetUrlAfterLogin(((WebRequest) getRequest()).getHttpServletRequest());
    }
    form = new LoginForm(this);
    body.add(new SimpleAttributeModifier("class", "loginpage"));
    body.add(form);
    form.init();
  }

  private void login(final PFUserDO user)
  {
    ((MySession) getSession()).login(user);
    UserFilter.login(((WebRequest) getRequest()).getHttpServletRequest(), user);
  }

  /** Logs the user out by invalidating the session. */
  private void logout()
  {
    final PFUserDO user = ((MySession) getSession()).getUser();
    if (user != null) {
      userXmlPreferencesCache.flushToDB(user.getId());
      userXmlPreferencesCache.clear(user.getId());
    }
    ((MySession) getSession()).logout();
    final Cookie stayLoggedInCookie = UserFilter.getStayLoggedInCookie(((WebRequest) getRequest()).getHttpServletRequest());
    if (stayLoggedInCookie != null) {
      stayLoggedInCookie.setMaxAge(0);
      stayLoggedInCookie.setValue("");
      stayLoggedInCookie.setPath("/");
      ((WebResponse) getResponse()).addCookie(stayLoggedInCookie);
    }
  }

  protected void checkLogin()
  {
    final String encryptedPassword = userDao.encryptPassword(form.getPassword());
    final String username = form.getUsername();
    final PFUserDO user = userDao.authenticateUser(username, encryptedPassword);
    if (user != null) {
      log.info("User with valid username/password: " + username + "/" + encryptedPassword);
      if (user.isDeleted() == true) {
        log.info("User has no system access (is deleted): " + user.getDisplayUsername());
        return;
      } else {
        log.info("User successfully logged in: " + user.getDisplayUsername());
        final boolean userWantsToStayLoggedIn = form.isStayLoggedIn();
        if (userWantsToStayLoggedIn == true) {
          final PFUserDO loggedInUser = userDao.internalGetById(user.getId());
          final Cookie cookie = new Cookie("stayLoggedIn", loggedInUser.getId()
              + ":"
              + loggedInUser.getUsername()
              + ":"
              + userDao.getStayLoggedInKey(user.getId()));
          UserFilter.addCookie(((WebRequest) getRequest()).getHttpServletRequest(), ((WebResponse) getResponse()).getHttpServletResponse(),
              cookie);
        }
        login(user);
        if (targetUrlAfterLogin != null) {
          getRequestCycle().setRequestTarget(new RedirectRequestTarget(targetUrlAfterLogin));
        } else {
          setResponsePage(WicketUtils.getDefaultPage());
        }
        return;
      }
    } else {
      log.info("User login failed: " + username + "/" + encryptedPassword);
    }
    return;
  }

  @Override
  protected String getTitle()
  {
    return getString("login.title");
  }
}
