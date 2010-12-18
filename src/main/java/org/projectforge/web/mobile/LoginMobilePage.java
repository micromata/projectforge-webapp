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

package org.projectforge.web.mobile;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
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

  private LoginMobileForm form;

  String targetUrlAfterLogin;

  @Override
  protected void thisIsAnUnsecuredPage()
  {
  }

  public LoginMobilePage(final PageParameters parameters)
  {
    super(parameters);
    final PFUserDO wicketSessionUser = ((MySession) getSession()).getUser();
    final PFUserDO sessionUser = UserFilter.getUser(((WebRequest) getRequest()).getHttpServletRequest());
    // Sometimes the wicket session user is given but the http session user is lost (re-login required).
    if (wicketSessionUser != null && sessionUser != null && wicketSessionUser.getId() == sessionUser.getId()) {
      setResponsePage(WicketUtils.getDefaultMobilePage());
      return;
    }
    targetUrlAfterLogin = UserFilter.getTargetUrlAfterLogin(((WebRequest) getRequest()).getHttpServletRequest());
    form = new LoginMobileForm(this);
    add(form);
    form.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final WebMarkupContainer messageOfTheDayArea = new WebMarkupContainer("messageOfTheDayArea");
    add(messageOfTheDayArea);
    final String messageOfTheDay = configuration.getStringValue(ConfigurationParam.MESSAGE_OF_THE_DAY);
    if (StringUtils.isBlank(messageOfTheDay) == true) {
      messageOfTheDayArea.setVisible(false);
    } else {
      final Label messageOfTheDayLabel = new Label("messageOfTheDay", messageOfTheDay);
      messageOfTheDayArea.add(messageOfTheDayLabel.setVisible(StringUtils.isNotBlank(messageOfTheDay)));
    }
  }

  protected void checkLogin()
  {
    LoginPage.internalCheckLogin(this, userDao, form.getUsername(), form.getPassword(), form.isStayLoggedIn(), WicketUtils
        .getDefaultMobilePage(), targetUrlAfterLogin);
  }

  @Override
  protected String getTitle()
  {
    return getString("login.title");
  }
}
