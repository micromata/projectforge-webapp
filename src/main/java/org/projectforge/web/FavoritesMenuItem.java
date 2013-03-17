/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web;

import java.util.List;

/**
 * It's only used for deserialization with JSON.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class FavoritesMenuItem
{
  private String content;

  private String ref;

  private List<FavoritesMenuItem> children;

  /**
   * @return the content
   */
  public String getContent()
  {
    return content;
  }

  /**
   * @return the ref
   */
  public String getRef()
  {
    return ref;
  }

  /**
   * @return the children
   */
  public List<FavoritesMenuItem> getChildren()
  {
    return children;
  }
}
