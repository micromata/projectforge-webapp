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

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.wicket.FeedbackPage;
import org.projectforge.web.wicket.WicketApplication;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class NavTopPanel extends NavAbstractPanel
{
  private static final long serialVersionUID = -7858806882044188339L;

  public NavTopPanel(final String id)
  {
    super(id);
  }

  public void init()
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

    add(new BookmarkablePageLink<Void>("feedbackLink", FeedbackPage.class));
    getMenu();

    // Main menu:
    final RepeatingView menuRepeater = new RepeatingView("menuRepeater");
    add(menuRepeater);
    final List<MenuEntry> menuEntries = menu.getFavoriteMenuEntries();
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
          subMenuItem.add(new WebMarkupContainer("subsubMenu").setVisible(false));
        }
      }
    }
  }
}
