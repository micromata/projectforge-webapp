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

package org.projectforge.web.core;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.Menu;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MenuPanel extends Panel
{
  private static final long serialVersionUID = -7858806882044188339L;

  private static final String USER_PREF_MENU_KEY = "usersMenu";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MenuPanel.class);

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  private Menu menu;

  public MenuPanel(String id)
  {
    super(id);
    add(CSSPackageResource.getHeaderContribution("scripts/jquery-ui-1-2.8.2.custom/css/custom-theme/jquery-ui-.custom.css"));
    add(JavascriptPackageResource.getHeaderContribution("scripts/jquery-ui-1-2.8.2.custom/js/jquery-ui-1.8.2.custom.min.js"));
    add(JavascriptPackageResource.getHeaderContribution("scripts/menu.js"));
    // should be included in jqueryui, uncomment if something is missing
    // add(JavascriptPackageResource.getHeaderContribution("scripts/jquery.dimensions.min.js"));
  }

  @SuppressWarnings("serial")
  public void init()
  {
    getMenu();

    final WebMarkupContainer mainMenuContainer = new WebMarkupContainer("mainMenu");
    add(mainMenuContainer);
    mainMenuContainer.add(new AbstractDefaultAjaxBehavior() {
      @Override
      protected void onComponentTag(final ComponentTag tag)
      {
        super.onComponentTag(tag);
        final String javascript = // One click opens the main menu and and the next click closes the main menu
        "$('#normal .main').toggleClass('active');"
        // If the main menu is open after toggling...
            + "if($('.main').hasClass('active')){"
            // enable sortable...
            + "$('#personal, #nav ul').sortable('enable');"
            // and add blue border around the personal menu...
            + "$('ul#personal').addClass('dotted');"
            // main menu is now closed...
            + "} else {"
            // disable sortable....
            + "$('#personal, #nav ul').sortable('disable');"
            // Call back serialized menu...
            + "{"
            + generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "&favoritesMenu=' + $('#personal, #nav ul').sortable('toArray')")
            + "return false;}"
            // remove the blue border around the personal menu
            + "  $('ul#personal').removeClass('dotted');"
            + "}";
        tag.put("onclick", javascript);
      }

      @Override
      protected void respond(final AjaxRequestTarget target)
      {
        final RequestCycle requestCycle = RequestCycle.get();
        final String favoritesMenu = requestCycle.getRequest().getParameter("favoritesMenu");
        log.info("FavoritesMenu: " + favoritesMenu);
      }
    });

    // Favorite menu:
    final RepeatingView favoriteMenuEntryRepeater = new RepeatingView("favoriteMenuEntryRepeater");
    mainMenuContainer.add(favoriteMenuEntryRepeater);
    boolean isFirst = true;
    for (final MenuEntry favoriteMenuEntry : menu.getFavoriteMenuEntries()) {
      final WebMarkupContainer favoriteMenuEntryContainer = new WebMarkupContainer(favoriteMenuEntryRepeater.newChildId());
      favoriteMenuEntryRepeater.add(favoriteMenuEntryContainer);
      favoriteMenuEntryContainer.add(new SimpleAttributeModifier("id", favoriteMenuEntry.getId()));
      if (isFirst == true) {
        // favoriteMenuEntryContainer.add(new SimpleAttributeModifier("class", "first"));
        isFirst = false;
      }
      final AbstractLink link = getMenuEntryLink(favoriteMenuEntry);
      favoriteMenuEntryContainer.add(link);
    }

    // Main menu:
    final RepeatingView menuAreaRepeater = new RepeatingView("menuAreaRepeater");
    mainMenuContainer.add(menuAreaRepeater);

    int counter = 0;
    for (final MenuEntry menuAreaEntry : menu.getMenuEntries()) {
      if (menuAreaEntry.getSubMenuEntries() == null) {
        log.error("Oups: menu without sub menus not supported: " + menuAreaEntry.getId());
        continue;
      }
      // Now we add a new menu area (title with sub menus):
      final WebMarkupContainer menuAreaContainer = new WebMarkupContainer(menuAreaRepeater.newChildId());
      menuAreaRepeater.add(menuAreaContainer);
      final WebMarkupContainer menuAreaItem = new WebMarkupContainer("menuArea");
      menuAreaContainer.add(menuAreaItem);
      if (menuAreaEntry.isFirst() == true) {
        menuAreaItem.add(new SimpleAttributeModifier("class", "first"));
      }
      menuAreaItem.add(new Label("areaTitle", getString(menuAreaEntry.getI18nKey())));
      final Label areaSuffixLabel = getSuffixLabel(menuAreaEntry);
      menuAreaItem.add(areaSuffixLabel);
      final RepeatingView menuEntryRepeater = new RepeatingView("menuEntryRepeater");
      menuAreaItem.add(menuEntryRepeater);
      for (final MenuEntry menuEntry : menuAreaEntry.getSubMenuEntries()) {
        if (menuEntry.getSubMenuEntries() != null) {
          log.error("Oups: sub sub menus not supported: " + menuAreaEntry.getId() + " has child menus which are ignored.");
        }
        // Now we add the next menu entry to the area:
        final WebMarkupContainer menuEntryLi = new WebMarkupContainer(menuEntryRepeater.newChildId());
        menuEntryRepeater.add(menuEntryLi);
        menuEntryLi.add(new SimpleAttributeModifier("id", menuEntry.getId()));
        final AbstractLink link = getMenuEntryLink(menuEntry);
        menuEntryLi.add(link);
      }
      if (counter++ == 3) {
        final WebMarkupContainer tipRow = new WebMarkupContainer("tipRow");
        menuAreaContainer.add(tipRow);
        tipRow.add(new Label("tip", getString("menu.main.tip1")).setEscapeModelStrings(false));
      } else {
        menuAreaContainer.add(new Label("tipRow", "invisible").setVisible(false));
      }
    }
    // Now we append the second tip row at the bottom of the menu (independant from the number of menu areas):
    final WebMarkupContainer container = new WebMarkupContainer(menuAreaRepeater.newChildId());
    menuAreaRepeater.add(container);
    final WebMarkupContainer li = new WebMarkupContainer("menuArea");
    container.add(li.setVisible(false));
    final WebMarkupContainer tipRow = new WebMarkupContainer("tipRow");
    container.add(tipRow);
    tipRow.add(new Label("tip", getString("menu.main.tip2")).setEscapeModelStrings(false));
  }

  private AbstractLink getMenuEntryLink(final MenuEntry menuEntry)
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
      link.add(new SimpleAttributeModifier("target", "_blank"));
    }
    link.add(new Label("label", getString(menuEntry.getI18nKey())).setRenderBodyOnly(true));
    final Label menuSuffixLabel = getSuffixLabel(menuEntry);
    link.add(menuSuffixLabel);
    return link;
  }

  @SuppressWarnings("serial")
  private Label getSuffixLabel(final MenuEntry menuEntry)
  {
    final Label suffixLabel;
    if (menuEntry.getNewCounterModel() != null) {
      suffixLabel = new Label("suffix", new Model<String>() {
        @Override
        public String getObject()
        {
          final Integer counter = menuEntry.getNewCounterModel().getObject();
          if (NumberHelper.greaterZero(counter) == true) {
            return String.valueOf(counter);
          } else {
            return "";
          }
        }
      }) {
        @Override
        public boolean isVisible()
        {
          final Integer counter = menuEntry.getNewCounterModel().getObject();
          return NumberHelper.greaterZero(counter) == true;
        }
      };
    } else {
      suffixLabel = new Label("suffix");
      suffixLabel.setVisible(false);
    }
    if (menuEntry.getNewCounterTooltip() != null) {
      WicketUtils.addTooltip(suffixLabel, getString(menuEntry.getNewCounterTooltip()));
    }
    return suffixLabel;
  }

  private Menu getMenu()
  {
    AbstractSecuredPage securedPage = null;
    if (getPage() instanceof AbstractSecuredPage) {
      securedPage = ((AbstractSecuredPage) getPage());
      menu = (Menu) securedPage.getUserPrefEntry(USER_PREF_MENU_KEY);
      if (menu != null) {
        return menu;
      }
    }
    if (menu != null) { // After getting menu from user pref entry, because otherwise resetMenu() doesn't work if menu is stored in this
      // panel.
      return menu;
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
