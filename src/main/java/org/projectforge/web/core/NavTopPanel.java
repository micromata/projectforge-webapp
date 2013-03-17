/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserRights;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.FavoritesMenu;
import org.projectforge.web.LayoutSettingsPage;
import org.projectforge.web.LoginPage;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.core.menuconfig.MenuConfig;
import org.projectforge.web.dialog.ModalDialog;
import org.projectforge.web.mobile.MenuMobilePage;
import org.projectforge.web.user.ChangePasswordPage;
import org.projectforge.web.user.MyAccountEditPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.FeedbackPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * Displays the favorite menu.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class NavTopPanel extends NavAbstractPanel
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NavTopPanel.class);

  private static final long serialVersionUID = -7858806882044188339L;

  private FavoritesMenu favoritesMenu;

  private final AccessChecker accessChecker;

  private final UserXmlPreferencesCache userXmlPreferencesCache;

  private BookmarkDialog bookmarkDialog;

  public NavTopPanel(final String id, final UserXmlPreferencesCache userXmlPreferencesCache, final AccessChecker accessChecker)
  {
    super(id);
    this.userXmlPreferencesCache = userXmlPreferencesCache;
    this.accessChecker = accessChecker;
  }

  public void init(final AbstractSecuredPage page)
  {
    getMenu();
    this.favoritesMenu = FavoritesMenu.get(userXmlPreferencesCache, accessChecker);
    final WebMarkupContainer goMobile = new WebMarkupContainer("goMobile");
    add(goMobile);
    if (page.getMySession().isMobileUserAgent() == true) {
      goMobile.add(new BookmarkablePageLink<Void>("link", MenuMobilePage.class));
    } else {
      goMobile.setVisible(false);
    }
    final BookmarkablePageLink<Void> layoutSettingsMenuLink = new BookmarkablePageLink<Void>("layoutSettingsMenuLink",
        LayoutSettingsPage.class);
    if (UserRights.getAccessChecker().isRestrictedUser() == true) {
      // Not visibible for restricted users:
      layoutSettingsMenuLink.setVisible(false);
    }
    add(new MenuConfig("menuconfig", getMenu(), favoritesMenu));
    @SuppressWarnings("serial")
    final Form<String> searchForm = new Form<String>("searchForm") {
      private String searchString;

      /**
       * @see org.apache.wicket.markup.html.form.Form#onSubmit()
       */
      @Override
      protected void onSubmit()
      {
        if (StringUtils.isNotBlank(searchString) == true) {
          final SearchPage searchPage = new SearchPage(new PageParameters(), searchString);
          setResponsePage(searchPage);
        }
        super.onSubmit();
      }
    };
    add(searchForm);
    final TextField<String> searchField = new TextField<String>("searchField", new PropertyModel<String>(searchForm, "searchString"));
    WicketUtils.setPlaceHolderAttribute(searchField, getString("search.search"));
    searchForm.add(searchField);
    add(layoutSettingsMenuLink);
    add(new BookmarkablePageLink<Void>("feedbackLink", FeedbackPage.class));
    {
      @SuppressWarnings("serial")
      final AjaxLink<Void> showBookmarkLink = new AjaxLink<Void>("showBookmarkLink") {
        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        public void onClick(final AjaxRequestTarget target)
        {
          bookmarkDialog.open(target);
          // Redraw the content:
          bookmarkDialog.redraw().addContent(target);
        }
      };
      add(showBookmarkLink);
      addBookmarkDialog();
    }
    {
      add(new Label("user", PFUserContext.getUser().getFullname()));
      if (accessChecker.isRestrictedUser() == true) {
        // Show ChangePaswordPage as my account for restricted users.
        final BookmarkablePageLink<Void> changePasswordLink = new BookmarkablePageLink<Void>("myAccountLink", ChangePasswordPage.class);
        add(changePasswordLink);
      } else {
        final BookmarkablePageLink<Void> myAccountLink = new BookmarkablePageLink<Void>("myAccountLink", MyAccountEditPage.class);
        add(myAccountLink);
      }
      @SuppressWarnings("serial")
      final Link<String> logoutLink = new Link<String>("logoutLink") {
        @Override
        public void onClick()
        {
          LoginPage.logout((MySession) getSession(), (WebRequest) getRequest(), (WebResponse) getResponse(), userXmlPreferencesCache);
          setResponsePage(LoginPage.class);
        };
      };
      add(logoutLink);
    }
    addCompleteMenu();
    addFavoriteMenu();
  }

  @SuppressWarnings("serial")
  private void addCompleteMenu()
  {
    final Label totalMenuSuffixLabel = new MenuSuffixLabel("totalMenuCounter", new Model<Integer>() {
      @Override
      public Integer getObject()
      {
        int counter = 0;
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
    add(totalMenuSuffixLabel);

    final RepeatingView completeMenuCategoryRepeater = new RepeatingView("completeMenuCategoryRepeater");
    add(completeMenuCategoryRepeater);
    if (menu.getMenuEntries() != null) {
      for (final MenuEntry menuEntry : menu.getMenuEntries()) {
        if (menuEntry.getSubMenuEntries() == null) {
          continue;
        }
        // Now we add a new menu area (title with sub menus):
        final WebMarkupContainer categoryContainer = new WebMarkupContainer(completeMenuCategoryRepeater.newChildId());
        completeMenuCategoryRepeater.add(categoryContainer);
        categoryContainer.add(new Label("menuCategoryLabel", getString(menuEntry.getI18nKey())));
        final Label areaSuffixLabel = getSuffixLabel(menuEntry);
        categoryContainer.add(areaSuffixLabel);

        // final WebMarkupContainer subMenuContainer = new WebMarkupContainer("subMenu");
        // categoryContainer.add(subMenuContainer);
        if (menuEntry.hasSubMenuEntries() == false) {
          // subMenuContainer.setVisible(false);
          continue;
        }

        final RepeatingView completeSubMenuRepeater = new RepeatingView("completeSubMenuRepeater");
        categoryContainer.add(completeSubMenuRepeater);
        for (final MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
          if (subMenuEntry.getSubMenuEntries() != null) {
            log.error("Oups: sub sub menus not supported: " + menuEntry.getId() + " has child menus which are ignored.");
          }
          // Now we add the next menu entry to the area:
          final WebMarkupContainer subMenuItem = new WebMarkupContainer(completeSubMenuRepeater.newChildId());
          completeSubMenuRepeater.add(subMenuItem);
          final AbstractLink link = getMenuEntryLink(subMenuEntry, true);
          if (link != null) {
            subMenuItem.add(link);
          } else {
            subMenuItem.setVisible(false);
          }
        }
      }
    }

  }

  private void addFavoriteMenu()
  {
    // Favorite menu:
    final RepeatingView menuRepeater = new RepeatingView("menuRepeater");
    add(menuRepeater);
    final Collection<MenuEntry> menuEntries = favoritesMenu.getMenuEntries();
    if (menuEntries != null) {
      for (final MenuEntry menuEntry : menuEntries) {
        // Now we add a new menu area (title with sub menus):
        final WebMarkupContainer menuItem = new WebMarkupContainer(menuRepeater.newChildId());
        menuRepeater.add(menuItem);
        final AbstractLink link = getMenuEntryLink(menuEntry, true);
        if (link == null) {
          menuItem.setVisible(false);
          continue;
        }
        menuItem.add(link);

        final WebMarkupContainer subMenuContainer = new WebMarkupContainer("subMenu");
        menuItem.add(subMenuContainer);
        final WebMarkupContainer caret = new WebMarkupContainer("caret");
        link.add(caret);
        if (menuEntry.hasSubMenuEntries() == false) {
          subMenuContainer.setVisible(false);
          caret.setVisible(false);
          continue;
        }
        menuItem.add(AttributeModifier.append("class", "dropdown"));
        link.add(AttributeModifier.append("class", "dropdown-toggle"));
        link.add(AttributeModifier.append("data-toggle", "dropdown"));
        final RepeatingView subMenuRepeater = new RepeatingView("subMenuRepeater");
        subMenuContainer.add(subMenuRepeater);
        for (final MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
          // Now we add the next menu entry to the area:
          if (subMenuEntry.hasSubMenuEntries() == false) {
            final WebMarkupContainer subMenuItem = new WebMarkupContainer(subMenuRepeater.newChildId());
            subMenuRepeater.add(subMenuItem);
            // Subsubmenu entries aren't yet supported, show only the sub entries without children, otherwise only the children are
            // displayed.
            final AbstractLink subLink = getMenuEntryLink(subMenuEntry, true);
            if (subLink == null) {
              subMenuItem.setVisible(false);
              continue;
            }
            subMenuItem.add(subLink);
            continue;
          }

          // final WebMarkupContainer subsubMenuContainer = new WebMarkupContainer("subsubMenu");
          // subMenuItem.add(subsubMenuContainer);
          // if (subMenuEntry.hasSubMenuEntries() == false) {
          // subsubMenuContainer.setVisible(false);
          // continue;
          // }
          // final RepeatingView subsubMenuRepeater = new RepeatingView("subsubMenuRepeater");
          // subsubMenuContainer.add(subsubMenuRepeater);
          for (final MenuEntry subsubMenuEntry : subMenuEntry.getSubMenuEntries()) {
            // Now we add the next menu entry to the sub menu:
            final WebMarkupContainer subMenuItem = new WebMarkupContainer(subMenuRepeater.newChildId());
            subMenuRepeater.add(subMenuItem);
            // Subsubmenu entries aren't yet supported, show only the sub entries without children, otherwise only the children are
            // displayed.
            final AbstractLink subLink = getMenuEntryLink(subsubMenuEntry, true);
            if (subLink == null) {
              subMenuItem.setVisible(false);
              continue;
            }
            subMenuItem.add(subLink);
            // final WebMarkupContainer subsubMenuItem = new WebMarkupContainer(subsubMenuRepeater.newChildId());
            // subsubMenuRepeater.add(subsubMenuItem);
            // final AbstractLink subsubLink = getMenuEntryLink(subsubMenuEntry, subsubMenuItem);
            // subsubMenuItem.add(subsubLink);
          }
        }
      }
    }
  }

  private void addBookmarkDialog()
  {
    final AbstractSecuredPage parentPage = (AbstractSecuredPage) getPage();
    bookmarkDialog = new BookmarkDialog(parentPage.newModalDialogId());
    bookmarkDialog.setOutputMarkupId(true);
    parentPage.add(bookmarkDialog);
    bookmarkDialog.init();
  }

  @SuppressWarnings("serial")
  private class BookmarkDialog extends ModalDialog
  {
    /**
     * @param id
     */
    public BookmarkDialog(final String id)
    {
      super(id);
    }

    @Override
    public void init()
    {
      setTitle(getString("bookmark.title"));
      init(new Form<String>(getFormId()));
      gridBuilder.newFormHeading(""); // Otherwise it's empty and an IllegalArgumentException is thrown.
    }

    private BookmarkDialog redraw()
    {
      clearContent();
      final AbstractSecuredPage page = (AbstractSecuredPage) NavTopPanel.this.getPage();
      {
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("bookmark.directPageLink")).setLabelSide(false);
        final TextArea<String> textArea = new TextArea<String>(fs.getTextAreaId(), new Model<String>(page.getPageAsLink()));
        fs.add(textArea);
        textArea.add(AttributeModifier.replace("onClick", "$(this).select();"));
      }
      final PageParameters params = page.getBookmarkableInitialParameters();
      if (params.isEmpty() == false) {
        final FieldsetPanel fs = gridBuilder.newFieldset(getString(page.getTitleKey4BookmarkableInitialParameters())).setLabelSide(false);
        final TextArea<String> textArea = new TextArea<String>(fs.getTextAreaId(), new Model<String>(page.getPageAsLink(params)));
        fs.add(textArea);
        textArea.add(AttributeModifier.replace("onClick", "$(this).select();"));
      }
      return this;
    }
  }
}
