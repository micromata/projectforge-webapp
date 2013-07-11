/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.navigation;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.resources.ImageResources;

/**
 * Class for creating the navigation bars of the chimney plugin.
 * Pages can register with this class to get an link in a navigation bar.
 * An arbitrary number of navigation bars can be registered, identified by name.
 * Parameters can optionally be passed to all link targets.
 */
public class NavigationBorder extends Border
{
  private static final long serialVersionUID = -4615364099918195003L;

  /**
   * @param id Wicket id that is replaced with this border component
   * @param navigationBarName Name of the navigation bar to be created. This selects the set of items in the navigation bar.
   * @param breadcrumb A breadcrumb to inform the user about his current position in the system, an array like { "first", "second", "third" } will be displayed as something like first->second->third
   * @param linkParameters Parameters that are added to all links in the navigation bar, useful for passing context information between different pages (e.g. project id)
   */
  public NavigationBorder(final String id, final String navigationBarName, final String[] breadcrumb, final PageParameters linkParameters)
  {
    super(id);

    // Add a navigation entry for each registered navigation item of the selected navigation item set
    createNavEntryMarkup(NavigationBars.getNavigationBar(navigationBarName), linkParameters);
    // Create Breadcrumb from string array
    createBreadcrumb(breadcrumb);

    addToBorder(new Image("chimney_logo", ImageResources.CHIMNEY_LOGO));

  }

  private void createBreadcrumb(final String[] breadcrumb)
  {
    final StringBuilder sb = new StringBuilder();
    if (breadcrumb != null)
      for (int i=0; i<breadcrumb.length; i++) {
        if (i > 0)
          sb.append(" &rarr; ");
        sb.append(getString(breadcrumb[i]));
      }
    addToBorder(new Label("breadcrumb", sb.toString()).setEscapeModelStrings(false));
  }

  private void createNavEntryMarkup(final List<NavigationItem> items, final PageParameters linkParameters)
  {
    @SuppressWarnings("serial")
    final
    ListView<NavigationItem> listview = new ListView<NavigationItem>("nav_item", items) {
      @Override
      protected void populateItem(final ListItem<NavigationItem> item) {
        final BookmarkablePageLink<Void> link = item.getModelObject().getLinkInstance("nav_link", "linktext");
        // pass parameters, if any
        if (linkParameters != null)
          link.getPageParameters().overwriteWith(linkParameters);
        item.add(link);
      }
    };
    addToBorder(listview);
  }
}
