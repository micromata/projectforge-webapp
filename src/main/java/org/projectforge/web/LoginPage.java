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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.ProjectForgeVersion;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.Login;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.admin.SystemUpdatePage;
import org.projectforge.web.mobile.LoginMobilePage;
import org.projectforge.web.wicket.AbstractUnsecureBasePage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketUtils;

public class LoginPage extends AbstractUnsecureBasePage
{
  private static final long serialVersionUID = 4457817484456315374L;

  public static final String REQUEST_PARAM_LOGOUT = "logout";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoginPage.class);

  // Used by LoginMobilePage
  private static final String PARAMETER_KEY_FORCE_NON_MOBILE = "forceNonMobile";

  @SpringBean(name = "configuration")
  private Configuration configuration;

  @SpringBean(name = "initDatabaseDao")
  private InitDatabaseDao initDatabaseDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private WebMarkupContainer errorsContainer;

  private String errorMessage;

  private LoginForm form = null;

  /**
   * Add parameter to force non-mobile version. This avoids a redirect to the LoginMobilePage and is used by LoginMobilePage.
   * @return PageParameters.
   */
  public static PageParameters forceNonMobile()
  {
    final PageParameters params = new PageParameters();
    params.add(PARAMETER_KEY_FORCE_NON_MOBILE, "true");
    return params;
  }

  public static void logout(final MySession mySession, final WebRequest request, final WebResponse response,
      final UserXmlPreferencesCache userXmlPreferencesCache, final MenuBuilder menuBuilder)
  {
    final PFUserDO user = mySession.getUser();
    if (user != null) {
      userXmlPreferencesCache.flushToDB(user.getId());
      userXmlPreferencesCache.clear(user.getId());
      if (menuBuilder != null) {
        menuBuilder.expireMenu(user.getId());
      }
    }
    mySession.logout();
    final Cookie stayLoggedInCookie = UserFilter.getStayLoggedInCookie(WicketUtils.getHttpServletRequest(request));
    if (stayLoggedInCookie != null) {
      stayLoggedInCookie.setMaxAge(0);
      stayLoggedInCookie.setValue(null);
      stayLoggedInCookie.setPath("/");
      response.addCookie(stayLoggedInCookie);
    }
  }

  public static void logout(final MySession mySession, final WebRequest request, final WebResponse response,
      final UserXmlPreferencesCache userXmlPreferencesCache)
  {
    logout(mySession, request, response, userXmlPreferencesCache, null);
  }

  public static void logout(final MySession mySession, final HttpServletRequest request, final HttpServletResponse response,
      final UserXmlPreferencesCache userXmlPreferencesCache, final MenuBuilder menuBuilder)
  {
    final PFUserDO user = mySession.getUser();
    if (user != null) {
      userXmlPreferencesCache.flushToDB(user.getId());
      userXmlPreferencesCache.clear(user.getId());
      if (menuBuilder != null) {
        menuBuilder.expireMenu(user.getId());
      }
    }
    mySession.logout();
    final Cookie stayLoggedInCookie = UserFilter.getStayLoggedInCookie(request);
    if (stayLoggedInCookie != null) {
      stayLoggedInCookie.setMaxAge(0);
      stayLoggedInCookie.setValue(null);
      stayLoggedInCookie.setPath("/");
      response.addCookie(stayLoggedInCookie);
    }
  }

  @Override
  protected void thisIsAnUnsecuredPage()
  {
  }

  @SuppressWarnings("serial")
  public LoginPage(final PageParameters parameters)
  {
    super(parameters);
    if (getMySession().isMobileUserAgent() == true
        && UserFilter.isUpdateRequiredFirst() == false
        && "true".equals(WicketUtils.getAsString(parameters, PARAMETER_KEY_FORCE_NON_MOBILE)) == false) {
      throw new RestartResponseException(LoginMobilePage.class);
    }
    final PFUserDO wicketSessionUser = getMySession().getUser();
    final PFUserDO sessionUser = UserFilter.getUser(WicketUtils.getHttpServletRequest(getRequest()));
    // Sometimes the wicket session user is given but the http session user is lost (re-login required).
    if (wicketSessionUser != null && sessionUser != null && wicketSessionUser.getId() == sessionUser.getId()) {
      throw new RestartResponseException(WicketUtils.getDefaultPage());
    }
    if (initDatabaseDao.isEmpty() == true) {
      log.info("Data-base is empty: redirect to SetupPage...");
      throw new RestartResponseException(SetupPage.class);
    }
    form = new LoginForm(this);
    body.add(AttributeModifier.replace("class", "loginpage"));
    body.add(form);
    form.init();
    body.add(new Label("welcome", getLocalizedMessage("login.welcome", ProjectForgeVersion.YEAR)));
    final WebMarkupContainer administratorLoginNeeded = new WebMarkupContainer("administratorLoginNeeded");
    body.add(administratorLoginNeeded);
    if (UserFilter.isUpdateRequiredFirst() == false) {
      administratorLoginNeeded.setVisible(false);
    }
    {
      final String messageOfTheDay = configuration.getStringValue(ConfigurationParam.MESSAGE_OF_THE_DAY);
      final WebMarkupContainer container = new WebMarkupContainer("messageOfTheDay");
      body.add(container.setVisible(StringUtils.isNotBlank(messageOfTheDay)));
      final Label messageOfTheDayLabel = new Label("msg", messageOfTheDay);
      container.add(messageOfTheDayLabel.setEscapeModelStrings(false));
    }
    errorsContainer = new WebMarkupContainer("errors");
    body.add(errorsContainer.setVisible(false));
    errorsContainer.add(new Label("msg", new Model<String>() {
      @Override
      public String getObject()
      {
        return StringUtils.defaultString(errorMessage);
      }
    }));
  }

  void addError(final String msg)
  {
    errorMessage = msg;
    errorsContainer.setVisible(true);
  }

  public static void internalLogin(final WebPage page, final PFUserDO user)
  {
    ((MySession) page.getSession()).login(user, page.getRequest());
    UserFilter.login(WicketUtils.getHttpServletRequest(page.getRequest()), user);
  }

  /**
   * @param page
   * @param userDao
   * @param dataSource
   * @param username
   * @param password
   * @param userWantsToStayLoggedIn
   * @param defaultPage
   * @param targetUrlAfterLogin
   * @return i18n key of the validation error message if not successfully logged in, otherwise null.
   */
  public static String internalCheckLogin(final WebPage page, final UserDao userDao, final String username, final String password,
      final boolean userWantsToStayLoggedIn, final Class< ? extends WebPage> defaultPage)
  {
    final LoginResult loginResult = Login.getInstance().checkLogin(username, password);
    final PFUserDO user = loginResult.getUser();
    if (user == null || loginResult.getLoginResultStatus() != LoginResultStatus.SUCCESS) {
      return loginResult.getLoginResultStatus().getI18nKey();
    }
    if (UserFilter.isUpdateRequiredFirst() == true) {
      internalLogin(page, user);
      log.info("Admin login for maintenance (data-base update) successful for user '" + username + "'.");
      throw new RestartResponseException(SystemUpdatePage.class);
    }
    log.info("User successfully logged in: " + user.getDisplayUsername());
    if (userWantsToStayLoggedIn == true) {
      final PFUserDO loggedInUser = userDao.internalGetById(user.getId());
      final Cookie cookie = new Cookie("stayLoggedIn", loggedInUser.getId()
          + ":"
          + loggedInUser.getUsername()
          + ":"
          + userDao.getStayLoggedInKey(user.getId()));
      UserFilter.addStayLoggedInCookie(WicketUtils.getHttpServletRequest(page.getRequest()),
          WicketUtils.getHttpServletResponse(page.getResponse()), cookie);
    }
    internalLogin(page, user);
    // Do not redirect to requested page in maintenance mode (update required first):
    if (UserFilter.isUpdateRequiredFirst() == true) {
      throw new RestartResponseException(SystemUpdatePage.class);
    }
    page.continueToOriginalDestination();
    // Redirect only if not a redirect is set by Wicket.
    throw new RestartResponseException(defaultPage);
  }

  protected String checkLogin()
  {
    return internalCheckLogin(this, userDao, form.getUsername(), form.getPassword(), form.isStayLoggedIn(), WicketUtils.getDefaultPage());
  }

  @Override
  protected String getTitle()
  {
    return getString("login.title");
  }
}
