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
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.web.LoginPage;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.address.AddressListMobilePage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.user.MyAccountEditPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.PresizedImage;

public class MenuMobilePage extends AbstractSecuredMobilePage
{
  @SpringBean(name = "configuration")
  private Configuration configuration;

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  @SuppressWarnings("serial")
  public MenuMobilePage(final PageParameters parameters)
  {
    super(parameters);
    leftnavContainer.setVisible(false);
    final PageItemPanel pageItemPanel = new PageItemPanel("menu");
    add(pageItemPanel);
    final String optimized = getString("mobile.optimized");
    PresizedImage image;
    if (configuration.isAddressManagementConfigured() == true) {
      image = new PresizedImage("image", getResponse(), MobileWebConstants.THUMB_IMAGE_CONTACTS);
      pageItemPanel.add(new PageItemEntryMenuPanel(pageItemPanel.newChildId(), AddressListMobilePage.class, image,
          getString("address.title.heading"), null));
    }
    image = new PresizedImage("image", getResponse(), MobileWebConstants.THUMB_IMAGE_CLOCK);
    pageItemPanel.add(new PageItemEntryMenuPanel(pageItemPanel.newChildId(), TimesheetListPage.class, image,
        getString("timesheet.title.heading"), null));
    image = new PresizedImage("image", getResponse(), MobileWebConstants.THUMB_IMAGE_CALENDAR);
    pageItemPanel.add(new PageItemEntryMenuPanel(pageItemPanel.newChildId(), CalendarPage.class, image, getString("calendar.title"), null));
    image = new PresizedImage("image", getResponse(), MobileWebConstants.THUMB_IMAGE_SETTINGS);
    pageItemPanel.add(new PageItemEntryMenuPanel(pageItemPanel.newChildId(), MyAccountEditPage.class, image, getString("menu.myAccount"),
        null));
    image = new PresizedImage("image", getResponse(), MobileWebConstants.THUMB_IMAGE_START);
    pageItemPanel.add(new PageItemEntryMenuPanel(pageItemPanel.newChildId(), image, getString("menu.logout"), optimized) {
      @Override
      protected void onClick()
      {
        LoginPage
        .logout((MySession) getSession(), (WebRequest) getRequest(), (WebResponse) getResponse(), userXmlPreferencesCache, menuBuilder);
        setResponsePage(LoginMobilePage.class);
      }
    });
  }

  @Override
  protected String getTitle()
  {
    return getString("menu.main.title");
  }
}
