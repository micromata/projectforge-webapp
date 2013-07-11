/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A store for managing multiple navigation bars.
 */
public class NavigationBars
{
  private static Map<String, List<NavigationItem>> items = new HashMap<String, List<NavigationItem>>();

  /**
   * Adds an item to a navigation bar
   * @param navigationBarName Name of the navigation bar that the item is added to
   * @param item The item to add
   */
  public static void addItem(final String navigationBarName, final NavigationItem item) {
    getNavigationBar(navigationBarName).add(item);
  }

  /**
   * Fetches the list of NavigationItems for the given navigation bar name. If no such list exists, one is created.
   * @param navigationBarName name of the navigation bar
   * @return A list of NavigationItems
   */
  public static List<NavigationItem> getNavigationBar(final String navigationBarName)
  {
    List<NavigationItem> navList = items.get(navigationBarName);
    if (navList == null) {
      navList = new ArrayList<NavigationItem>();
      items.put(navigationBarName, navList);
    }
    return navList;
  }

}
