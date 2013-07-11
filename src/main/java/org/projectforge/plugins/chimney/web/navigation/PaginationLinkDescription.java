/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.navigation;

import java.io.Serializable;

import org.apache.wicket.request.resource.ResourceReference;

/**
 * Helper class for determining if/how to display next/previous links
 */
public class PaginationLinkDescription implements Serializable {
  private static final long serialVersionUID = -2447172544388496353L;

  public boolean displayLink = false;
  public String linkText;
  public ResourceReference linkImage;
  public int width = -1;
  public int height = -1;

  public void sanitize() {
    if (displayLink) {
      // displaying a link does not make sense if there is nothing to display
      if (linkText==null && linkImage == null)
        displayLink = false;
    }
  }
}
