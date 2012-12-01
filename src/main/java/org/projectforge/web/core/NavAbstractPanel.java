/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.core;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.FavoritesMenu;
import org.projectforge.web.Menu;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class NavAbstractPanel extends Panel
{
  private static final long serialVersionUID = -1019454504282157440L;

  public static final String USER_PREF_MENU_KEY = "usersMenu";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NavAbstractPanel.class);

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  protected Menu menu;

  protected FavoritesMenu favoritesMenu;

  public NavAbstractPanel(final String id)
  {
    super(id);
  }

  protected AbstractLink getMenuEntryLink(final MenuEntry menuEntry, final boolean renderLabelBodyOnly)
  {
    final AbstractLink link;
    if (menuEntry.isWicketPage() == true) {
      if (menuEntry.getParams() == null) {
        link = new BookmarkablePageLink<String>("link", menuEntry.getPageClass());
      } else {
        final PageParameters params = WicketUtils.getPageParameters(menuEntry.getParams());
        link = new BookmarkablePageLink<String>("link", menuEntry.getPageClass(), params);
      }
    } else {
      link = new ExternalLink("link", WicketUtils.getUrl(getResponse(), menuEntry.getUrl(), true));
    }
    if (menuEntry.isNewWindow() == true) {
      link.add(AttributeModifier.replace("target", "_blank"));
    }
    final String i18nKey = menuEntry.getI18nKey();
    link.add(new Label("label", i18nKey != null ? getString(i18nKey) : menuEntry.getName()).setRenderBodyOnly(renderLabelBodyOnly));
    final Label menuSuffixLabel = getSuffixLabel(menuEntry);
    link.add(menuSuffixLabel);
    return link;
  }

  protected Label getSuffixLabel(final MenuEntry menuEntry)
  {
    final Label suffixLabel;
    if (menuEntry != null && menuEntry.getNewCounterModel() != null) {
      suffixLabel = new MenuSuffixLabel(menuEntry.getNewCounterModel());
      if (menuEntry != null && menuEntry.getNewCounterTooltip() != null) {
        WicketUtils.addTooltip(suffixLabel, getString(menuEntry.getNewCounterTooltip()));
      }
    } else {
      suffixLabel = new Label("suffix");
      suffixLabel.setVisible(false);
    }
    return suffixLabel;
  }

  public Menu getMenu()
  {
    if (menu != null) {
      return menu;
    }
    AbstractSecuredPage securedPage = null;
    if (getPage() instanceof AbstractSecuredPage) {
      securedPage = ((AbstractSecuredPage) getPage());
      menu = (Menu) securedPage.getUserPrefEntry(USER_PREF_MENU_KEY);
      if (menu != null) {
        return menu;
      }
    }
    if (log.isDebugEnabled() == true) {
      log.debug("Build new menu.");
    }
    menu = menuBuilder.getMenu(PFUserContext.getUser());
    if (securedPage != null) {
      securedPage.putUserPrefEntry(USER_PREF_MENU_KEY, menu, false);
    }
    return menu;
  }
}
