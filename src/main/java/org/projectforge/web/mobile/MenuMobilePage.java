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

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.web.LoginPage;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.UserFilter;
import org.projectforge.web.address.AddressMobileListPage;
import org.projectforge.web.wicket.MySession;

public class MenuMobilePage extends AbstractSecuredMobilePage
{

  @SpringBean(name = "configuration")
  private Configuration configuration;

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  public MenuMobilePage()
  {
    this(new PageParameters());
  }

  @SuppressWarnings( { "serial", "unchecked"})
  public MenuMobilePage(final PageParameters parameters)
  {
    super(parameters);
    if (getUser().getAttribute(UserFilter.USER_ATTR_STAY_LOGGED_IN) != null) {
      getUser().removeAttribute(UserFilter.USER_ATTR_STAY_LOGGED_IN);
      final RecentMobilePageInfo pageInfo = (RecentMobilePageInfo) userXmlPreferencesCache.getEntry(getUserId(),
          AbstractSecuredMobilePage.USER_PREF_RECENT_PAGE);
      if (pageInfo != null && pageInfo.getPageClass() != null) {
        throw new RestartResponseException((Class) pageInfo.getPageClass(), pageInfo.restorePageParameters());
      }
    }
    setNoBackButton();
    final ListViewPanel listViewPanel = new ListViewPanel("menu");
    add(listViewPanel);
    listViewPanel.add(new ListViewItemPanel(listViewPanel.newChildId(), getString("menu.main.title")));
    if (configuration.isAddressManagementConfigured() == true) {
      listViewPanel.add(new ListViewItemPanel(listViewPanel.newChildId(), AddressMobileListPage.class, getString("address.title.heading")));
    }
    listViewPanel.add(new ListViewItemPanel(listViewPanel.newChildId(), new Link<String>(ListViewItemPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        LoginPage.logout((MySession) getSession(), (WebRequest) getRequest(), (WebResponse) getResponse(), userXmlPreferencesCache,
            menuBuilder);
        setResponsePage(LoginMobilePage.class);
      }
      
    }, getString("menu.logout")) {
    });
  }

  @Override
  protected String getTitle()
  {
    return getString("menu.main.title");
  }
}
