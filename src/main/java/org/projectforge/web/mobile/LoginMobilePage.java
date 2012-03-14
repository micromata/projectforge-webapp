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

package org.projectforge.web.mobile;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.AppVersion;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.LoginPage;
import org.projectforge.web.UserFilter;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketUtils;

public class LoginMobilePage extends AbstractMobilePage
{
  @SpringBean(name = "configuration")
  private Configuration configuration;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "userXmlPreferencesCache")
  protected UserXmlPreferencesCache userXmlPreferencesCache;

  /**
   * Only needed if the data-base needs an update first (may-be the PFUserDO can't be read because of unmatching tables).
   */
  @SpringBean(name = "dataSource")
  private DataSource dataSource;

  private final LoginMobileForm form;

  @Override
  protected void thisIsAnUnsecuredPage()
  {
  }

  @SuppressWarnings({ "unchecked", "rawtypes"})
  public LoginMobilePage(final PageParameters parameters)
  {
    super(parameters);
    final PFUserDO wicketSessionUser = ((MySession) getSession()).getUser();
    final PFUserDO sessionUser = UserFilter.getUser(WicketUtils.getHttpServletRequest(getRequest()));
    // Sometimes the wicket session user is given but the http session user is lost (re-login required).
    if (wicketSessionUser != null && sessionUser != null && wicketSessionUser.getId() == sessionUser.getId()) {
      final Integer userId = sessionUser.getId();
      final RecentMobilePageInfo pageInfo = (RecentMobilePageInfo) userXmlPreferencesCache.getEntry(userId,
          AbstractSecuredMobilePage.USER_PREF_RECENT_PAGE);
      if (pageInfo != null && pageInfo.getPageClass() != null) {
        throw new RestartResponseException((Class) pageInfo.getPageClass(), pageInfo.restorePageParameters());
      } else {
        throw new RestartResponseException(WicketUtils.getDefaultMobilePage());
      }
    }
    setNoBackButton();
    form = new LoginMobileForm(this);
    add(form);
    form.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final String messageOfTheDay = configuration.getStringValue(ConfigurationParam.MESSAGE_OF_THE_DAY);
    if (StringUtils.isBlank(messageOfTheDay) == true) {
      add(new Label("messageOfTheDay", "[invisible]").setVisible(false));
    } else {
      add(new Label("messageOfTheDay", messageOfTheDay).setEscapeModelStrings(false));
    }
  }

  @Override
  protected Component getTopCenter()
  {
    return new Label(AbstractMobilePage.TOP_CENTER_ID, AppVersion.APP_TITLE);
  }

  protected void checkLogin()
  {
    LoginPage.internalCheckLogin(this, userDao, dataSource, form.getUsername(), form.getPassword(), form.isStayLoggedIn(), WicketUtils
        .getDefaultMobilePage());
  }

  @Override
  protected String getTitle()
  {
    return getString("login.title");
  }
}
