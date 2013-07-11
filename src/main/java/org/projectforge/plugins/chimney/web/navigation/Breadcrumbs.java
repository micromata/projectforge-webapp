/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.navigation;

import java.util.HashMap;
import java.util.Map;

/**
 * A store for managing breadcrumbs.
 */
public class Breadcrumbs
{
  private static Map<String, NavigationItem> items = new HashMap<String, NavigationItem>();

  /**
   * Adds a breadcrumb to the internal map
   * @param breadcrumbName Name of the breadcrumb with which it can later be fetched
   * @param item The item to add
   */
  public static void addItem(final String breadcrumbName, final NavigationItem item) {
    items.put(breadcrumbName, item);
  }

  /**
   * Retrieves a breadcrumb using its name
   * @param breadcrumbName Name of the breadcrumb
   * @return a NavigationItem matching breadcrumbName or null
   */
  public static NavigationItem getItem(final String breadcrumbName) {
    return items.get(breadcrumbName);
  }
}
