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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.calendar.SelectDateAction;
import org.projectforge.web.core.BaseActionBean;
import org.projectforge.web.core.LoginChecker;
import org.projectforge.web.core.LoginFilter;
import org.projectforge.web.wicket.WicketUtils;

/**
 */
@UrlBinding("/secure/Login.action")
public class LoginAction extends BaseActionBean
{
  public static final String FIRST_PSEUDO_SETUP_USER = "firstPseudoSetupUser";

  private Configuration configuration;

  private LoginChecker loginChecker;

  private InitDatabaseDao initDatabaseDao;

  /**
   * Only for stripes for setting focus. Field not needed.
   * @return null
   */
  public String getUsername()
  {
    return null;
  }

  /** Only for stripes for setting focus. Field not needed. Do nothing. */
  public void setUsername(String username)
  {
  }

  @DefaultHandler
  public Resolution action()
  {
    if (getContext().getUser() != null) {
      if (getContext().getRequestParameter("logout") != null) {
        loginChecker.logout(getContext().getRequest(), getContext().getResponse(), getContext().getUser());
        getContext().logout();
        // TODO: besser eine ordentliche Methode: afterLogin und afterLogout.
        rebuildMenu();
      } else {
        rebuildMenu();
        setUserLocale();
        return new RedirectResolution("/" + WicketUtils.getDefaultPageUrl());
      }
    } else if (initDatabaseDao.isEmpty() == true) {
      final PFUserDO pseudoUser = new PFUserDO();
      pseudoUser.setUsername(FIRST_PSEUDO_SETUP_USER);
      pseudoUser.setId(-1);
      getContext().getRequest().getSession().setAttribute(LoginFilter.USER_ATTRIBUTE, pseudoUser);
      return new RedirectResolution("/" + WicketUtils.getBookmarkablePageUrl(SetupPage.class));
    }
    return new ForwardResolution("/WEB-INF/jsp/login.jsp");
  }

  public Resolution other()
  {
    rebuildMenu();
    setUserLocale();
    return new ForwardResolution(SelectDateAction.class);
  }

  public String getMessageOfTheDay()
  {
    return configuration.getStringValue(ConfigurationParam.MESSAGE_OF_THE_DAY);
  }

  private void setUserLocale()
  {
    PFUserContext.getLocale(getContext().getRequestLocale());
  }

  public void setConfiguration(Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setInitDatabaseDao(InitDatabaseDao initDatabaseDao)
  {
    this.initDatabaseDao = initDatabaseDao;
  }

  public void setLoginChecker(LoginChecker loginChecker)
  {
    this.loginChecker = loginChecker;
  }
}
