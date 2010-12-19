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
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.mobile.LoginMobilePage;
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

  private LoginForm form;

  String targetUrlAfterLogin;

  @Override
  protected void thisIsAnUnsecuredPage()
  {
  }

  public LoginPage(final PageParameters parameters)
  {
    super(parameters);
    if (getMySession().isMobileUserAgent() == true) {
      setResponsePage(LoginMobilePage.class);
      return;
    }
    final PFUserDO wicketSessionUser = getMySession().getUser();
    final PFUserDO sessionUser = UserFilter.getUser(((WebRequest) getRequest()).getHttpServletRequest());
    // Sometimes the wicket session user is given but the http session user is lost (re-login required).
    if (wicketSessionUser != null && sessionUser != null && wicketSessionUser.getId() == sessionUser.getId()) {
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
    form = new LoginForm(this);
    body.add(new SimpleAttributeModifier("class", "loginpage"));
    body.add(form);
    form.init();
  }

  public static void internalLogin(final WebPage page, final PFUserDO user)
  {
    ((MySession) page.getSession()).login(user);
    UserFilter.login(((WebRequest) page.getRequest()).getHttpServletRequest(), user);
  }

  private void login(final PFUserDO user)
  {
    internalLogin(this, user);
  }

  public static void internalCheckLogin(final WebPage page, final UserDao userDao, final String username, final String password,
      final boolean userWantsToStayLoggedIn, final Class< ? extends WebPage> defaultPage, final String targetUrlAfterLogin)
  {
    final String encryptedPassword = userDao.encryptPassword(password);
    final PFUserDO user = userDao.authenticateUser(username, encryptedPassword);
    if (user != null) {
      log.info("User with valid username/password: " + username + "/" + encryptedPassword);
      if (user.isDeleted() == true) {
        log.info("User has no system access (is deleted): " + user.getDisplayUsername());
        return;
      } else {
        log.info("User successfully logged in: " + user.getDisplayUsername());
        if (userWantsToStayLoggedIn == true) {
          final PFUserDO loggedInUser = userDao.internalGetById(user.getId());
          final Cookie cookie = new Cookie("stayLoggedIn", loggedInUser.getId()
              + ":"
              + loggedInUser.getUsername()
              + ":"
              + userDao.getStayLoggedInKey(user.getId()));
          UserFilter.addCookie(((WebRequest) page.getRequest()).getHttpServletRequest(), ((WebResponse) page.getResponse())
              .getHttpServletResponse(), cookie);
        }
        internalLogin(page, user);
        if (targetUrlAfterLogin != null) {
          page.getRequestCycle().setRequestTarget(new RedirectRequestTarget(targetUrlAfterLogin));
        } else {
          page.setResponsePage(defaultPage);
        }
        return;
      }
    } else {
      log.info("User login failed: " + username + "/" + encryptedPassword);
    }
    return;
  }

  protected void checkLogin()
  {
    internalCheckLogin(this, userDao, form.getUsername(), form.getPassword(), form.isStayLoggedIn(), WicketUtils.getDefaultPage(),
        targetUrlAfterLogin);
  }

  @Override
  protected String getTitle()
  {
    return getString("login.title");
  }
}
