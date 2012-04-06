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

package org.projectforge.web.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.LoginPage;
import org.projectforge.web.Menu;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.core.LogoServlet;
import org.projectforge.web.core.MenuSuffixLabel;
import org.projectforge.web.core.NavSidePanel;
import org.projectforge.web.core.NavTopPanel;
import org.projectforge.web.user.MyAccountEditPage;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.flowlayout.MyComponentsRepeater;

/** All pages with required login should be derived from this page. */
public abstract class AbstractSecuredPage extends AbstractSecuredBasePage
{
  private static final long serialVersionUID = -8721451198050398835L;

  /**
   * List to create content menu in the desired order before creating the RepeatingView.
   */
  protected MyComponentsRepeater<ContentMenuEntryPanel> contentMenu;

  /**
   * List to create content menu in the desired order before creating the RepeatingView.
   */
  protected MyComponentsRepeater<ContentMenuEntryPanel> contentRightMenu;

  /**
   * If set then return after save, update or cancel to this page. If not given then return to given list page.
   */
  protected WebPage returnToPage;

  public AbstractSecuredPage(final PageParameters parameters)
  {
    super(parameters);
    final String logoServlet = LogoServlet.getBaseUrl();
    if (logoServlet != null) {
      body.add(new ContextImage("logoLeftImage", logoServlet));
    } else {
      body.add(new Label("logoLeftImage", "[invisible]").setVisible(false));
    }
    final NavSidePanel menuPanel = new NavSidePanel("mainMenu");
    body.add(menuPanel);
    menuPanel.init();
    @SuppressWarnings("serial")
    final Label sideMenuSuffixLabel = new MenuSuffixLabel("totalMenuCounter", new Model<Integer>() {
      @Override
      public Integer getObject()
      {
        int counter = 0;
        final Menu menu = menuPanel.getMenu();
        if (menu.getMenuEntries() == null) {
          return counter;
        }
        for (final MenuEntry menuEntry : menu.getMenuEntries()) {
          final IModel<Integer> newCounterModel = menuEntry.getNewCounterModel();
          if (newCounterModel != null && newCounterModel.getObject() != null) {
            counter += newCounterModel.getObject();
          }
        }
        return counter;
      };
    });
    body.add(sideMenuSuffixLabel);
    final NavTopPanel favoriteMenuPanel = new NavTopPanel("favoriteMenu", userXmlPreferencesCache, accessChecker);
    body.add(favoriteMenuPanel);
    favoriteMenuPanel.init();
    // body.add(new Label("username", getUser().getFullname()));
    final BookmarkablePageLink<Void> myAccountLink = new BookmarkablePageLink<Void>("myAccountLink", MyAccountEditPage.class);
    body.add(myAccountLink);
    @SuppressWarnings("serial")
    final Link<String> logoutLink = new Link<String>("logoutLink") {
      @Override
      public void onClick()
      {
        LoginPage.logout((MySession) getSession(), (WebRequest) getRequest(), (WebResponse) getResponse(), userXmlPreferencesCache);
        setResponsePage(LoginPage.class);
      };
    };
    body.add(logoutLink);

    contentMenu = new MyComponentsRepeater<ContentMenuEntryPanel>("contentMenuRepeater");
    body.add(contentMenu.getRepeatingView());
    contentRightMenu = new MyComponentsRepeater<ContentMenuEntryPanel>("contentRightMenuRepeater");
    body.add(contentRightMenu.getRepeatingView());
  }

  /**
   * If set then return after save, update or cancel to this page. If not given then return to given list page. As an alternative you can
   * set the returnToPage as a page parameter (if supported by the derived page).
   * @param returnToPage
   */
  public AbstractSecuredPage setReturnToPage(final WebPage returnToPage)
  {
    this.returnToPage = returnToPage;
    return this;
  }

  protected void addContentMenuEntry(final ContentMenuEntryPanel panel)
  {
    this.contentMenu.add(panel);
  }

  protected String getNewContentMenuChildId()
  {
    return this.contentMenu.newChildId();
  }

  protected void addContentRightMenuEntry(final ContentMenuEntryPanel panel)
  {
    this.contentRightMenu.add(panel);
  }

  protected String getNewContentRightMenuChildId()
  {
    return this.contentRightMenu.newChildId();
  }

  @Override
  protected void onBeforeRender()
  {
    contentMenu.render();
    contentRightMenu.render();
    super.onBeforeRender();
  }
}
