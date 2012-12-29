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

package org.projectforge.web;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.web.core.NavAbstractPanel;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class CustomizeMenuPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = 8587252641914110851L;

  private String menuAsXml;

  @SuppressWarnings("serial")
  public CustomizeMenuPage(final PageParameters parameters)
  {
    super(parameters);
    final FavoritesMenu favoriteMenu = FavoritesMenu.get(userXmlPreferencesCache, accessChecker);
    buildCustMenu(favoriteMenu);
    final Menu menu = (Menu) getUserPrefEntry(NavAbstractPanel.USER_PREF_MENU_KEY);
    buildCompleteMenu(menu);
    body.add(new Label("i18nVars", "var enterNewName = \""
        + getString("menu.customize.enterNewName")
        + "\";\nvar create = \""
        + getString("menu.customize.create")
        + "\";\nvar remove = \""
        + getString("menu.customize.remove")
        + "\";\nvar rename = \""
        + getString("menu.customize.rename")
        + "\";").setEscapeModelStrings(false));
    final Form<Void> form = new Form<Void>("form");
    body.add(form);
    final boolean debugModeAndWicketTags = getWicketApplication().isDevelopmentSystem() == true
        && getWicketApplication().isStripWicketTags() == false;
    body.add(new WebMarkupContainer("hintForDevelopers").setVisible(debugModeAndWicketTags));
    form.add(new HiddenField<String>("menuAsXml", new PropertyModel<String>(this, "menuAsXml")));
    form.add(new Button("updateButton") {
      /**
       * @see org.apache.wicket.markup.html.form.Button#onSubmit()
       */
      @Override
      public void onSubmit()
      {
        favoriteMenu.readFromXml(menuAsXml, FavoritesMenu.ParseMode.JS_TREE);
        favoriteMenu.storeAsUserPref();
        setResponsePage(CustomizeMenuPage.class);
      }
    });
  }

  /**
   * The menu tree to customize.
   */
  private void buildCustMenu(final FavoritesMenu favoriteMenu)
  {
    final RepeatingView menuRepeater = new RepeatingView("custMenuEntry");
    body.add(menuRepeater);
    for (final MenuEntry menuEntry : favoriteMenu.getMenuEntries()) {
      addCustMenuEntry(menuRepeater, menuEntry);
    }
  }

  /**
   * Used by buildCustMenu for recursive building of all menu entries.
   */
  private void addCustMenuEntry(final RepeatingView menuRepeater, final MenuEntry menuEntry)
  {
    final Fragment frag = new Fragment(menuRepeater.newChildId(), "custMenuEntryFragment", body);
    menuRepeater.add(frag);
    final WebMarkupContainer li = new WebMarkupContainer("li");
    frag.add(li);
    if (menuEntry.getPageClass() != null || menuEntry.getUrl() != null) {
      li.add(AttributeModifier.append("rel", "leaf"));
      final String id = "c-" + menuEntry.getId();
      li.setOutputMarkupId(true).setMarkupId(id);
    }
    final String i18nKey = menuEntry.getI18nKey();
    li.add(new Label("label", i18nKey != null ? getString(menuEntry.getI18nKey()) : menuEntry.getName()));
    final WebMarkupContainer subMenu = new WebMarkupContainer("subMenu");
    li.add(subMenu);
    if (menuEntry.hasSubMenuEntries() == false) {
      subMenu.setVisible(false);
      return;
    }
    final RepeatingView subMenuRepeater = new RepeatingView("subMenuEntry");
    subMenu.add(subMenuRepeater);
    for (final MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
      addCustMenuEntry(subMenuRepeater, subMenuEntry);
    }
  }

  /**
   * Complete menu of the user for drag&drop.
   */
  private void buildCompleteMenu(final Menu menu)
  {
    final RepeatingView menuRepeater = new RepeatingView("completeMenuEntry");
    body.add(menuRepeater);
    for (final MenuEntry menuEntry : menu.getMenuEntries()) {
      if (menuEntry.hasSubMenuEntries() == false) {
        continue;
      }
      addCompleteMenuEntry(menuRepeater, menuEntry);
    }
  }

  /**
   * Used by buildCompleteMenu for recursive building of all menu entries.
   */
  private void addCompleteMenuEntry(final RepeatingView menuRepeater, final MenuEntry menuEntry)
  {
    final Fragment frag = new Fragment(menuRepeater.newChildId(), "completeMenuEntryFragment", body);
    menuRepeater.add(frag);
    final WebMarkupContainer li = new WebMarkupContainer("li");
    frag.add(li);
    final Label label = new Label("label", getString(menuEntry.getI18nKey()));
    if (menuEntry.getPageClass() != null || menuEntry.getUrl() != null) {
      li.add(AttributeModifier.append("rel", "leaf"));
    }
    label.setOutputMarkupId(true).setMarkupId("o-" + menuEntry.getId());
    li.add(label);
    final WebMarkupContainer subMenu = new WebMarkupContainer("subMenu");
    li.add(subMenu);
    if (menuEntry.hasSubMenuEntries() == false) {
      li.add(AttributeModifier.append("class", "jstree-draggable"));
      subMenu.setVisible(false);
      return;
    }
    final RepeatingView subMenuRepeater = new RepeatingView("subMenuEntry");
    subMenu.add(subMenuRepeater);
    for (final MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
      addCompleteMenuEntry(subMenuRepeater, subMenuEntry);
    }
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.render(JavaScriptReferenceHeaderItem.forUrl("scripts/jquery.jstree/jquery.jstree.js"));
    response.render(JavaScriptReferenceHeaderItem.forUrl("scripts/jquery.jstree/jquery.hotkeys.js"));
  }

  @Override
  protected String getTitle()
  {
    return getString("menu.customize.title");
  }

}
