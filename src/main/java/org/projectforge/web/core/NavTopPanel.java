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

package org.projectforge.web.core;

import java.util.Collection;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.CustomizeMenuPage;
import org.projectforge.web.FavoritesMenu;
import org.projectforge.web.LayoutSettingsPage;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.mobile.MenuMobilePage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.FeedbackPage;
import org.projectforge.web.wicket.WicketApplication;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.DialogPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * Displays the favorite menu.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class NavTopPanel extends NavAbstractPanel
{
  private static final long serialVersionUID = -7858806882044188339L;

  private static final String BOOKMARK_DIALOG_ID = "bookmarkModalWindow";

  private final FavoritesMenu favoritesMenu;

  private ModalWindow bookmarkModalWindow;

  public NavTopPanel(final String id, final UserXmlPreferencesCache userXmlPreferencesCache, final AccessChecker accessChecker)
  {
    super(id);
    this.favoritesMenu = FavoritesMenu.get(userXmlPreferencesCache, accessChecker);
  }

  public void init(final AbstractSecuredPage page)
  {
    @SuppressWarnings("serial")
    final Model<String> alertMessageModel = new Model<String>() {
      @Override
      public String getObject()
      {
        if (WicketApplication.getAlertMessage() == null) {
          return "neverDisplayed";
        }
        return WicketApplication.getAlertMessage();
      }
    };
    @SuppressWarnings("serial")
    final WebMarkupContainer alertMessageContainer = new WebMarkupContainer("alertMessageContainer") {
      @Override
      public boolean isVisible()
      {
        return (WicketApplication.getAlertMessage() != null);
      }
    };
    add(alertMessageContainer);
    final Label alertMessageLabel = new Label("alertMessage", alertMessageModel);
    alertMessageContainer.add(alertMessageLabel.setRenderBodyOnly(true));

    if (page.getMySession().isMobileUserAgent() == true) {
      add(new BookmarkablePageLink<Void>("goMobile", MenuMobilePage.class));
    } else {
      add(new WebMarkupContainer("goMobile").setVisible(false));
    }
    add(new BookmarkablePageLink<Void>("customizeMenuLink", CustomizeMenuPage.class));
    add(new BookmarkablePageLink<Void>("layoutSettingsMenuLink", LayoutSettingsPage.class));
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
          showBookmarkModalWindow(target);
        }
      };
      add(showBookmarkLink);
      bookmarkModalWindow = new ModalWindow(BOOKMARK_DIALOG_ID);
      bookmarkModalWindow.setInitialHeight(200);
      add(bookmarkModalWindow);
    }
    getMenu();

    // Main menu:
    final RepeatingView menuRepeater = new RepeatingView("menuRepeater");
    add(menuRepeater);
    final Collection<MenuEntry> menuEntries = favoritesMenu.getMenuEntries();
    if (menuEntries != null) {
      for (final MenuEntry menuEntry : menuEntries) {
        // Now we add a new menu area (title with sub menus):
        final WebMarkupContainer menuItem = new WebMarkupContainer(menuRepeater.newChildId());
        menuRepeater.add(menuItem);
        final AbstractLink link = getMenuEntryLink(menuEntry, false);
        menuItem.add(link);

        final WebMarkupContainer subMenuContainer = new WebMarkupContainer("subMenu");
        menuItem.add(subMenuContainer);
        if (menuEntry.hasSubMenuEntries() == false) {
          subMenuContainer.setVisible(false);
          continue;
        }

        final RepeatingView subMenuRepeater = new RepeatingView("subMenuRepeater");
        subMenuContainer.add(subMenuRepeater);
        for (final MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
          // Now we add the next menu entry to the area:
          final WebMarkupContainer subMenuItem = new WebMarkupContainer(subMenuRepeater.newChildId());
          subMenuRepeater.add(subMenuItem);
          final AbstractLink subLink = getMenuEntryLink(subMenuEntry, false);
          subMenuItem.add(subLink);

          final WebMarkupContainer subsubMenuContainer = new WebMarkupContainer("subsubMenu");
          subMenuItem.add(subsubMenuContainer);
          if (subMenuEntry.hasSubMenuEntries() == false) {
            subsubMenuContainer.setVisible(false);
            continue;
          }
          final RepeatingView subsubMenuRepeater = new RepeatingView("subsubMenuRepeater");
          subsubMenuContainer.add(subsubMenuRepeater);
          for (final MenuEntry subsubMenuEntry : subMenuEntry.getSubMenuEntries()) {
            // Now we add the next menu entry to the sub menu:
            final WebMarkupContainer subsubMenuItem = new WebMarkupContainer(subsubMenuRepeater.newChildId());
            subsubMenuRepeater.add(subsubMenuItem);
            final AbstractLink subsubLink = getMenuEntryLink(subsubMenuEntry, false);
            subsubMenuItem.add(subsubLink);
          }
        }
      }
    }
  }

  @SuppressWarnings("serial")
  protected void showBookmarkModalWindow(final AjaxRequestTarget target)
  {
    // Close dialog
    final DialogPanel closeDialog = new DialogPanel(bookmarkModalWindow, getString("bookmark.title"));
    bookmarkModalWindow.setContent(closeDialog);

    final DivPanel content = new DivPanel(closeDialog.newChildId());
    closeDialog.add(content);
    FieldsetPanel fs = new FieldsetPanel(content, getString("bookmark.directPageLink")).setLabelSide(false);
    final AbstractSecuredPage page = (AbstractSecuredPage) getPage();
    fs.add(new TextArea<String>(fs.getTextAreaId(), new Model<String>(page.getPageAsLink())));
    final PageParameters params = page.getBookmarkableInitialParameters();
    if (params.isEmpty() == false) {
      fs = new FieldsetPanel(content, getString(page.getTitleKey4BookmarkableInitialParameters())).setLabelSide(false);
      fs.add(new TextArea<String>(fs.getTextAreaId(), new Model<String>(page.getPageAsLink(params))));
      bookmarkModalWindow.setInitialHeight(400);
    }

    final AjaxButton closeButton = new AjaxButton(SingleButtonPanel.WICKET_ID, new Model<String>("close")) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        bookmarkModalWindow.close(target);
      }

      /**
       * @see org.apache.wicket.ajax.markup.html.form.AjaxButton#onError(org.apache.wicket.ajax.AjaxRequestTarget,
       *      org.apache.wicket.markup.html.form.Form)
       */
      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
      }
    };
    closeButton.setDefaultFormProcessing(false); // No validation
    final SingleButtonPanel closeButtonPanel = new SingleButtonPanel(closeDialog.newButtonChildId(), closeButton, getString("close"),
        SingleButtonPanel.DEFAULT_SUBMIT);
    closeDialog.addButton(closeButtonPanel);

    bookmarkModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      public boolean onCloseButtonClicked(final AjaxRequestTarget target)
      {
        return true;
      }
    });
    bookmarkModalWindow.show(target);
  }
}
