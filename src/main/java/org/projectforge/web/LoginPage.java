/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.admin.SystemUpdatePage;
import org.projectforge.web.core.LogoServlet;
import org.projectforge.web.mobile.LoginMobilePage;
import org.projectforge.web.wicket.AbstractUnsecureBasePage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketApplication;
import org.projectforge.web.wicket.WicketUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class LoginPage extends AbstractUnsecureBasePage
{
  private static final long serialVersionUID = 4457817484456315374L;

  public static final String REQUEST_PARAM_LOGOUT = "logout";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoginPage.class);

  @SpringBean(name = "configuration")
  private Configuration configuration;

  @SpringBean(name = "initDatabaseDao")
  private InitDatabaseDao initDatabaseDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private WebMarkupContainer errorsContainer;

  private String errorMessage;

  /**
   * Only needed if the data-base needs an update first (may-be the PFUserDO can't be read because of unmatching tables).
   */
  @SpringBean(name = "dataSource")
  private DataSource dataSource;

  private LoginForm form = null;

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
    if (getMySession().isMobileUserAgent() == true && UserFilter.isUpdateRequiredFirst() == false) {
      throw new RestartResponseException(LoginMobilePage.class);
    }
    final PFUserDO wicketSessionUser = getMySession().getUser();
    final PFUserDO sessionUser = UserFilter.getUser(WicketUtils.getHttpServletRequest(getRequest()));
    // Sometimes the wicket session user is given but the http session user is lost (re-login required).
    if (wicketSessionUser != null && sessionUser != null && wicketSessionUser.getId() == sessionUser.getId()) {
      throw new RestartResponseException(WicketApplication.DEFAULT_PAGE);
    }
    if (initDatabaseDao.isEmpty() == true) {
      log.info("Data-base is empty: redirect to SetupPage...");
      throw new RestartResponseException(SetupPage.class);
    }
    final String logoServlet = LogoServlet.getBaseUrl();
    if (logoServlet != null) {
      body.add(new ContextImage("logoLeftImage", logoServlet));
    } else {
      body.add(new Label("logoLeftImage", "[invisible]").setVisible(false));
    }
    form = new LoginForm(this);
    body.add(AttributeModifier.replace("class", "loginpage"));
    body.add(form);
    form.init();
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

  public static boolean isAdminUser(final PFUserDO user, final DataSource dataSource)
  {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    String sql = "select pk from t_group where name=?";
    final int adminGroupId = jdbc.queryForInt(sql, new Object[] { ProjectForgeGroup.ADMIN_GROUP.getKey()});
    sql = "select count(*) from t_group_user where group_id=? and user_id=?";
    final int count = jdbc.queryForInt(sql, new Object[] { adminGroupId, user.getId()});
    if (count != 1) {
      log.info("Admin login for maintenance (data-base update) failed for user '"
          + user.getUsername()
          + "' (user not member of admin group).");
      return false;
    }
    return true;
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
  @SuppressWarnings({ "unchecked", "rawtypes"})
  public static String internalCheckLogin(final WebPage page, final UserDao userDao, final DataSource dataSource, final String username,
      final String password, final boolean userWantsToStayLoggedIn, final Class< ? extends WebPage> defaultPage)
  {
    final String encryptedPassword = userDao.encryptPassword(password);
    PFUserDO user = null;
    if (UserFilter.isUpdateRequiredFirst() == true) {
      // Only administrator login is allowed. The login is checked without Hibernate because the data-base schema may be out-dated for
      // Hibernate isn't functioning.
      final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
      try {
        final PFUserDO resUser = new PFUserDO();
        final String sql = "select pk, firstname, lastname from t_pf_user where username=? and password=? and deleted=false";
        jdbc.query(sql, new Object[] { username, encryptedPassword}, new ResultSetExtractor() {
          @Override
          public Object extractData(final ResultSet rs) throws SQLException, DataAccessException
          {
            if (rs.next() == true) {
              final int pk = rs.getInt("pk");
              final String firstname = rs.getString("firstname");
              final String lastname = rs.getString("lastname");
              resUser.setId(pk);
              resUser.setUsername(username).setFirstname(firstname).setLastname(lastname);
            }
            return null;
          }
        });
        if (resUser.getUsername() == null) {
          log.info("Admin login for maintenance (data-base update) failed for user '" + username + "' (user/password not found).");
          return "login.error.loginFailed";
        }
        if (isAdminUser(resUser, dataSource) == false) {
          return "login.adminLoginRequired";
        }
        user = resUser;
        internalLogin(page, user);
        log.info("Admin login for maintenance (data-base update) successful for user '" + username + "'.");
        throw new RestartResponseException(SystemUpdatePage.class);
      } catch (final Exception ex) {
        log.error(ex.getMessage(), ex);
      }
    } else {
      user = userDao.authenticateUser(username, encryptedPassword);
    }
    if (user != null) {
      log.info("User with valid username/password: " + username + "/" + encryptedPassword);
      if (user.isDeleted() == true) {
        log.info("User has no system access (is deleted): " + user.getDisplayUsername());
        return "login.error.loginExpired";
      } else {
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
        if (page.continueToOriginalDestination() == false) {
          // Redirect only if not a redirect is set by Wicket.
          throw new RestartResponseException(defaultPage);
        }
        return null;
      }
    } else {
      log.info("User login failed: " + username + "/" + encryptedPassword);
      return "login.error.loginFailed";
    }
  }

  protected String checkLogin()
  {
    return internalCheckLogin(this, userDao, dataSource, form.getUsername(), form.getPassword(), form.isStayLoggedIn(),
        WicketUtils.getDefaultPage());
  }

  @Override
  protected String getTitle()
  {
    return getString("login.title");
  }
}
